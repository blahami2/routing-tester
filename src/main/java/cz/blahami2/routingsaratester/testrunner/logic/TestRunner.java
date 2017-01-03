/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.logic;

import cz.blahami2.routingsaratester.common.model.Counter;
import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.parametertuning.model.IntNumber;
import cz.blahami2.routingsaratester.parametertuning.model.NumberAccumulator;
import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.sara.preprocessing.BottomUpPreprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.algorithm.sara.preprocessing.Preprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayBuilder;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayBuilderSetup;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.ZeroNode;
import cz.certicon.routing.algorithm.sara.query.mld.MLDFullMemoryRouteUnpacker;
import cz.certicon.routing.algorithm.sara.query.mld.MultilevelDijkstraAlgorithm;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.*;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.DisplayUtils;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.utils.progress.SimpleProgressListener;
import java8.util.Optional;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class TestRunner implements Runnable {

    private final Graph graph;
    private final PreprocessingInput input;
    private Collection<Input> testInput;
    private TestResult result;
    private Supplier<Preprocessor> preprocessorSupplier = () -> new BottomUpPreprocessor();
    private Logger logger;

    @Setter
    private boolean display = false;

    public TestRunner( @NonNull Graph graph, @NonNull PreprocessingInput input ) {
        this.graph = graph;
        this.input = input;
    }

    public void addRoutingCriteria( @NonNull Input testInput ) {
        this.testInput = Arrays.asList( testInput );
    }

    public void addRoutingCriteria( @NonNull Collection<Input> testInputs ) {
        this.testInput = testInputs;
    }

    public void setPreprocessingStrategy( @NonNull Supplier<Preprocessor> preprocessingStrategy ) {
        this.preprocessorSupplier = preprocessingStrategy;
    }

    @Override
    public void run() {
        try {
            logger = Logger.getLogger( getClass().getName() );
            try {
                FileHandler fh = new FileHandler( "test_runner.log", true );
                logger.addHandler( fh );
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter( formatter );
                logger.setUseParentHandlers( false );
            } catch ( SecurityException | IOException e ) {
                throw new IllegalStateException( e );
            }
            TimeMeasurement testRunnerTime = new TimeMeasurement();
            testRunnerTime.start();
            // perform partitioning and crete sara graph
            Preprocessor preprocessor = preprocessorSupplier.get();
            logger.info( input.toString() );
            System.out.println( "Preprocessing... input = " + input.toString() );
            SaraGraph saraGraph = preprocessor.preprocess( graph, input, new IdSupplier( 0 ), new SimpleProgressListener( 10 ) {
                @Override
                public void onProgressUpdate( double d ) {
                    System.out.printf( "- progress: %.02f %%\n", d );
                }
            } );
            TestResult.TestResultBuilder builder = TestResult.builder();
            builder.filteringTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.FILTERING ).getTime() );
            builder.assemblyTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.ASSEMBLING ).getTime() );
            builder = fillWithGraphData( builder, saraGraph );
            if ( display ) {
                System.out.println( "Displaying..." );
                DisplayUtils.display( saraGraph );
            }

            TimeLogger.log( TimeLogger.Event.OVERLAY, TimeLogger.Command.START );
            // create overlay graph
            OverlayBuilderSetup overlayBuilderSetup = new OverlayBuilderSetup();
            overlayBuilderSetup.setKeepSortcuts( true );
            OverlayBuilder overlay = new OverlayBuilder( saraGraph, overlayBuilderSetup );
            overlay.buildOverlays();
            TimeLogger.log( TimeLogger.Event.OVERLAY, TimeLogger.Command.STOP );
            builder.overlayTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.OVERLAY ).getTime() );

            // load test input
            // test graph with MLD
            System.out.println( "Routing..." );
            // - gather statistics
            // - gather time
            TimeMeasurement routingTimeMeasurement = new TimeMeasurement();
            Counter allCounter = new Counter();
            Counter notFoundCounter = new Counter();
            Counter validCounter = new Counter();
            NumberAccumulator<Time> routingTimeAcc = new NumberAccumulator<>( new Time( TimeUnits.MILLISECONDS, 0 ) );
            NumberAccumulator<Time> unpackTimeAcc = new NumberAccumulator<>( new Time( TimeUnits.MILLISECONDS, 0 ) );
            NumberAccumulator<IntNumber> nodeExaminedAcc = new NumberAccumulator<>( new IntNumber( 0 ) );
            NumberAccumulator<IntNumber> edgeRelaxedAcc = new NumberAccumulator<>( new IntNumber( 0 ) );
            NumberAccumulator<IntNumber> edgeVisitedAcc = new NumberAccumulator<>( new IntNumber( 0 ) );
            Time[] routingTimes = new Time[testInput.size()];
            Time[] unpackTimes = new Time[testInput.size()];
            Counter inputCounter = new Counter( 0, 0, testInput.size() );
            testInput.forEach( ti -> {
                int inputIdx = inputCounter.getValue();
                ti.forEach( ( InputElement x ) -> {
                    allCounter.increment();
                    // route
                    MultilevelDijkstraAlgorithm alg = new MultilevelDijkstraAlgorithm( overlay, new MLDFullMemoryRouteUnpacker() );
                    SaraNode source = saraGraph.getNodeById( x.getSourceNodeId() );
                    SaraNode target = saraGraph.getNodeById( x.getTargetNodeId() );
                    ZeroNode zeroSource = overlay.getZeroNode( source );
                    ZeroNode zeroTarget = overlay.getZeroNode( target );
                    Optional<Route<SaraNode, SaraEdge>> route;
                    try {
                        route = alg.route( ti.getMetric(), zeroSource, zeroTarget );
                        // validate
                        java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                        if ( route.isPresent() ) {
                            boolean result = route.get().getEdgeList().stream()
                                    .allMatch( ( e ) -> ( edgeIdIterator.hasNext() && equal( graph.getEdgeById( edgeIdIterator.next() ), e, ti.getMetric() ) ) );
                            if ( !result ) {
                                logger.warning( "Routes do not match for id: " + x.getId() + " at input " + inputIdx
                                        + "\nRoute ref: length = " + x.getLength() + ", time = " + x.getTime() + ", from = " + x.getSourceNodeId() + ", to = " + x.getTargetNodeId() + ", edges = " + x.getEdgeIds().stream().map( id -> id.toString() ).collect( Collectors.joining( " " ) )
                                        + "\nRoute res: length = " + (int) route.get().calculateDistance( Metric.LENGTH ).getValue() + ", time = " + (int) route.get().calculateDistance( Metric.TIME ).getValue() + " s, from = " + route.get().getSource().getId() + ", to = " + route.get().getTarget().getId() + ", edges = " + route.get().getEdgeList().stream().map( e -> ( (Edge) e ).getId() + "" ).collect( Collectors.joining( " " ) ) );
                            } else {
                                validCounter.increment();
                            }
                        } else {
                            logger.warning( "Route not found for: " + x.getSourceNodeId() + " -> " + x.getTargetNodeId() + " at input " + inputIdx
                                    + " (" + ( source.getId() ) + " -> " + ( target.getId() ) + ")"
                                    + ", not found " + notFoundCounter.increment() + "/" + allCounter.getValue() );
                        }
                    } catch ( IllegalArgumentException ex ) {
                        route = new DijkstraAlgorithm<SaraNode, SaraEdge>().route( ti.getMetric(), source, target );
                        if ( route.isPresent() ) {
                            logger.warning( "Route threw an IllegalArgumentException, but was found by regular dijkstra, route = " + source.getId() + " -> " + target.getId() + ", zero = " + zeroSource.getId() + " -> " + zeroTarget.getId() );
                        } else {
                            System.out.println( "Route not found by any algorithm: " + source.getId() + " -> " + target.getId() + ", zero = " + zeroSource.getId() + " -> " + zeroTarget.getId() );
                            throw ex;
                        }
                    }

                    // stats
                    routingTimeAcc.add( TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime() );
                    unpackTimeAcc.add( TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime() );
                    nodeExaminedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.NODES_EXAMINED ) ) );
                    edgeRelaxedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.EDGES_RELAXED ) ) );
                    edgeVisitedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.EDGES_VISITED ) ) );
                } );
                Time prevRouting = new Time( TimeUnits.MILLISECONDS, 0 );
                Time prevUnpack = new Time( TimeUnits.MILLISECONDS, 0 );
                for ( int i = 0; i < inputIdx; i++ ) {
                    prevRouting = prevRouting.add( routingTimes[i] );
                    prevUnpack = prevUnpack.add( unpackTimes[i] );
                }
                routingTimes[inputIdx] = routingTimeAcc.get().subtract( prevRouting );
                unpackTimes[inputIdx] = unpackTimeAcc.get().subtract( prevUnpack );
                inputCounter.increment();
            } );
            builder.validRatio( (double) validCounter.getValue() / (double) allCounter.getValue() );
            builder.routingTime( routingTimeAcc.get() );
            builder.routingTimes( routingTimes );
            builder.unpackTime( unpackTimeAcc.get() );
            builder.unpackTimes( unpackTimes );
            builder.examinedNodes( nodeExaminedAcc.get().getValue() );
            builder.relaxedEdges( edgeRelaxedAcc.get().getValue() );
            builder.visitedEdges( edgeVisitedAcc.get().getValue() );

            testRunnerTime.setTimeUnits( TimeUnits.SECONDS );
            System.out.println( "Done in " + testRunnerTime.getCurrentTimeString() );
            result = builder.build();
        } finally {
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
                handler.close();
            }
        }
    }


    public boolean equal( Edge e1, Edge e2, Metric metric ) {
        if ( e1.getId() == e2.getId() ) {
            return true;
        }
        if ( Math.abs( e1.getId() ) == Math.abs( e2.getId() ) ) {
            return true;
        }
        if ( e1.getLength( metric ).equals( e2.getLength( metric ) ) ) {
            if ( e1.getSource().getId() == e2.getSource().getId() ) {
                return e1.getTarget().getId() == e2.getTarget().getId();
            }
            return e1.getSource().getId() == e2.getTarget().getId() && e1.getTarget().getId() == e2.getSource().getId();
            // oneway?
        }
        return false;
    }

    public TestResult getResult() {
        return result;
    }

    public TestResult runForResult() {
        run();
        return result;
    }

    private TestResult.TestResultBuilder fillWithGraphData( TestResult.TestResultBuilder builder, SaraGraph saraGraph ) {
        int layers = input.getNumberOfLayers();
        double[] averageCellSizes = new double[layers];
        int[] maxCellSizes = new int[layers];
        int[] minCellSizes = new int[layers];
        int[] medianCellSizes = new int[layers];
        int[] numbersOfCells = new int[layers];
        int[] numbersOfCutEdges = new int[layers];
        for ( int i = 0; i < layers; i++ ) {
            Map<Cell, List<Parentable>> cellMap;
            if ( i == 0 ) {
                cellMap = StreamSupport.stream( saraGraph.getNodes().spliterator(), true )
                        .collect( Collectors.groupingBy( Parentable::getParent ) );
            } else {
                int finalI = i;
                cellMap = StreamSupport.stream( saraGraph.getNodes().spliterator(), true )
                        .map( n -> mapToIthParent( n, finalI ) )
                        .distinct()
                        .collect( Collectors.groupingBy( Parentable::getParent ) );
            }
            IntSummaryStatistics stats = cellMap.values().parallelStream().collect( Collectors.summarizingInt( List::size ) );
            averageCellSizes[i] = stats.getAverage();
            maxCellSizes[i] = stats.getMax();
            minCellSizes[i] = stats.getMin();
            medianCellSizes[i] = cellMap.values().parallelStream().mapToInt( List::size )
                    .sorted().skip( cellMap.values().size() / 2 ).findFirst().orElseThrow( () -> new RuntimeException( "Cannot find median." ) );// sort sizes, skip first half, select first from the rest
            numbersOfCells[i] = (int) stats.getCount();
            numbersOfCutEdges[i] = (int) StreamSupport.stream( saraGraph.getEdges().spliterator(), true )
                    .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
                    .count();

        }
        builder.averageCellSize( averageCellSizes );
        builder.totalAverageCellSize( Arrays.stream( averageCellSizes ).average().orElse( 0 ) );
        builder.maximalCellSize( maxCellSizes );
        builder.totalMaximalCellSize( Arrays.stream( maxCellSizes ).max().orElse( 0 ) );
        builder.minimalCellSize( minCellSizes );
        builder.totalMinimalCellSize( Arrays.stream( minCellSizes ).min().orElse( 0 ) );
        builder.medianCellSize( medianCellSizes );
        builder.numberOfCells( numbersOfCells );
        builder.totalNumberOfCells( Arrays.stream( numbersOfCells ).sum() );
        builder.numberOfCutEdges( numbersOfCutEdges );
        builder.totalNumberOfCutEdges( Arrays.stream( numbersOfCutEdges ).sum() );
        return builder;
    }

    private Cell mapToIthParent( Parentable p, int ith ) {
        if ( ith <= 0 ) {
            throw new IllegalArgumentException( "Must be 1th or higher" );
        }
        Cell c = p.getParent();
        for ( int i = 1; i < ith; i++ ) {
            c = c.getParent();
        }
        return c;
    }

    private static boolean equals( List<SaraEdge> edges, List<Long> edgeIds ) {
        if ( edges.size() != edgeIds.size() ) {
            return false;
        }
        for ( int i = 0; i < edges.size(); i++ ) {
            SaraEdge e = edges.get( i );
            Long eId = edgeIds.get( i );
            if ( e.getId() != eId ) {
                return false;
            }
        }
        return true;
    }

}
