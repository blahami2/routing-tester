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
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.algorithm.sara.preprocessing.BottomUpPreprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.algorithm.sara.preprocessing.Preprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.*;
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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import java8.util.Optional;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class TestRunner implements Runnable {

    private final Graph graph;
    private final PreprocessingInput input;
    private Collection<Input> testInput;
    private TestResult result;
    private Supplier<Preprocessor> preprocessorSupplier = () -> new BottomUpPreprocessor();

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
        TimeMeasurement testRunnerTime = new TimeMeasurement();
        testRunnerTime.start();
        // perform partitioning and crete sara graph
        Preprocessor preprocessor = preprocessorSupplier.get();
        System.out.println( "Preprocessing..." );
        SaraGraph saraGraph = preprocessor.preprocess( graph, input, new IdSupplier( 0 ), new SimpleProgressListener( 10 ) {
            @Override
            public void onProgressUpdate( double d ) {
                System.out.printf( "- progress: %.02f %%\n", d );
            }
        } );
        TestResult.TestResultBuilder builder = TestResult.builder();
        builder.filteringTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.FILTERING ).getTime() );
        builder.assemblyTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.ASSEMBLING ).getTime() );
        Map<Cell, List<SaraNode>> cellMap = StreamSupport.stream( saraGraph.getNodes().spliterator(), true )
                .collect( Collectors.groupingBy( SaraNode::getParent ) );
        IntSummaryStatistics stats = cellMap.values().parallelStream().collect( Collectors.summarizingInt( List::size ) );
        builder.averageCellSize( stats.getAverage() );
        builder.maximalCellSize( stats.getMax() );
        builder.minimalCellSize( stats.getMin() );
        builder.medianCellSize( cellMap.values().parallelStream().mapToInt( List::size )
                .sorted().skip( cellMap.values().size() / 2 ).findFirst().orElseThrow( () -> new RuntimeException( "Cannot find median." ) ) ); // sort sizes, skip first half, select first from the rest
        builder.numberOfCells( (int) stats.getCount() );
        builder.numberOfCutEdges( (int) StreamSupport.stream( saraGraph.getEdges().spliterator(), true )
                .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
                .count() );
        if ( display ) {
            System.out.println( "Displaying..." );
            DisplayUtils.display( saraGraph );
        }

        // create overlay graph
        OverlayBuilderSetup overlayBuilderSetup = new OverlayBuilderSetup();
        overlayBuilderSetup.setKeepSortcuts( false );
        overlayBuilderSetup.setMetric( EnumSet.allOf( Metric.class ) );
        OverlayBuilder overlay = new OverlayBuilder( saraGraph, overlayBuilderSetup );
        overlay.buildOverlays();

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
        testInput.forEach( ti -> {
            ti.forEach( ( InputElement x ) -> {
                allCounter.increment();
                // route
                MultilevelDijkstraAlgorithm alg = new MultilevelDijkstraAlgorithm( overlay, new MLDFullMemoryRouteUnpacker() );
                SaraNode source = saraGraph.getNodeById( x.getSourceNodeId() );
                SaraNode target = saraGraph.getNodeById( x.getTargetNodeId() );
                ZeroNode zeroSource = overlay.getZeroNode( source );
                ZeroNode zeroTarget = overlay.getZeroNode( target );
                Optional<Route<SaraNode, SaraEdge>> route = alg.route( Metric.LENGTH, zeroSource, zeroTarget );
                // validate
                java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                if ( route.isPresent() ) {
                    boolean result = route.get().getEdgeList().stream()
                            .mapToLong( e -> ( (Edge) e ).getId() )
                            .allMatch( ( e ) -> ( edgeIdIterator.hasNext() && e == edgeIdIterator.next() ) );
                    if ( !result ) {
                        System.out.println( "Routes do not match for id: " + x.getId() );
                        System.out.println( "Route ref: length = " + x.getLength() + ", time = " + x.getTime() + ", edges = " + x.getEdgeIds().stream().map( id -> id.toString() ).collect( Collectors.joining( " " ) ) );
                        System.out.println( "Route res: length = " + (int) route.get().calculateDistance( Metric.LENGTH ).getValue() + ", time = " + (int) route.get().calculateDistance( Metric.TIME ).getValue() + " s, edges = " + route.get().getEdgeList().stream().map( e -> ( (Edge) e ).getId() + "" ).collect( Collectors.joining( " " ) ) );
                    } else {
                        validCounter.increment();
                    }
                } else {
                    System.out.print( "Route not found for: " + x.getSourceNodeId() + " -> " + x.getTargetNodeId() );
                    System.out.print( " (" + ( source.getId() ) + " -> " + ( target.getId() ) + ")" );
                    System.out.println( ", not found " + notFoundCounter.increment() + "/" + allCounter.getValue() );
                }

                // stats
                routingTimeAcc.add( TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime() );
                unpackTimeAcc.add( TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime() );
                nodeExaminedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.NODES_EXAMINED ) ) );
                edgeRelaxedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.EDGES_RELAXED ) ) );
                edgeVisitedAcc.add( new IntNumber( StatsLogger.getValue( StatsLogger.Statistic.EDGES_VISITED ) ) );
            } );
        } );
        builder.validRatio( (double) validCounter.getValue() / (double) allCounter.getValue() );
        builder.routingTime( routingTimeAcc.get() );
        builder.unpackTime( unpackTimeAcc.get() );
        builder.examinedNodes( nodeExaminedAcc.get().getValue() );
        builder.relaxedEdges( edgeRelaxedAcc.get().getValue() );
        builder.visitedEdges( edgeVisitedAcc.get().getValue() );

        testRunnerTime.setTimeUnits( TimeUnits.SECONDS );
        System.out.println( "Done in " + testRunnerTime.getCurrentTimeString() );
        result = builder.build();
    }

    public TestResult getResult() {
        return result;
    }

    public TestResult runForResult() {
        run();
        return result;
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
