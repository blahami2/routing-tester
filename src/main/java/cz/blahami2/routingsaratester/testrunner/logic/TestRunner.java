/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.logic;

import cz.blahami2.routingsaratester.common.model.Counter;
import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.algorithm.sara.preprocessing.BottomUpPreprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.algorithm.sara.preprocessing.Preprocessor;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Cell;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.SaraEdge;
import cz.certicon.routing.model.graph.SaraGraph;
import cz.certicon.routing.model.graph.SaraNode;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.DisplayUtils;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.utils.progress.SimpleProgressListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java8.util.Optional;
import lombok.NonNull;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class TestRunner implements Runnable {

    private final Graph graph;
    private final PreprocessingInput input;
    private RoutingAlgorithm<SaraNode, SaraEdge> routingAlgorithm;
    private Collection<Input> testInput;
    private boolean display = false;
    private TestResult result;

    public TestRunner( @NonNull Graph graph, @NonNull PreprocessingInput input ) {
        this.graph = graph;
        this.input = input;
    }

    public TestRunner( @NonNull Graph graph, @NonNull PreprocessingInput input, @NonNull RoutingAlgorithm<SaraNode, SaraEdge> routingAlgorithm, @NonNull Input testInput ) {
        this.graph = graph;
        this.input = input;
        this.routingAlgorithm = routingAlgorithm;
        this.testInput = Arrays.asList( testInput );
    }

    public TestRunner( @NonNull Graph graph, @NonNull PreprocessingInput input, @NonNull RoutingAlgorithm<SaraNode, SaraEdge> routingAlgorithm, @NonNull Collection<Input> testInputs ) {
        this.graph = graph;
        this.input = input;
        this.routingAlgorithm = routingAlgorithm;
        this.testInput = testInputs;
    }

    public void addRoutingCriteria( @NonNull RoutingAlgorithm<SaraNode, SaraEdge> routingAlgorithm, @NonNull Input testInput ) {
        this.routingAlgorithm = routingAlgorithm;
        this.testInput = Arrays.asList( testInput );
    }

    public void addRoutingCriteria( @NonNull RoutingAlgorithm<SaraNode, SaraEdge> routingAlgorithm, @NonNull Collection<Input> testInputs ) {
        this.routingAlgorithm = routingAlgorithm;
        this.testInput = testInputs;
    }

    public void setDisplay( boolean on ) {
        this.display = on;
    }

    @Override
    public void run() {
        TimeMeasurement testRunnerTime = new TimeMeasurement();
        testRunnerTime.start();
        Preprocessor preprocessor = new BottomUpPreprocessor();
        System.out.println( "Preprocessing..." );
        SaraGraph bestResultGraph = preprocessor.preprocess( graph, input, new IdSupplier( 0 ), new SimpleProgressListener( 10 ) {
            @Override
            public void onProgressUpdate( double d ) {
                System.out.printf( "- progress: %.02f %%\n", d );
            }
        } );
        TestResult.TestResultBuilder builder = TestResult.builder();
        builder.filteringTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.FILTERING ).getTime() );
        builder.assemblyTime( TimeLogger.getTimeMeasurement( TimeLogger.Event.ASSEMBLING ).getTime() );
        Map<Cell, List<SaraNode>> cellMap = StreamSupport.stream( bestResultGraph.getNodes().spliterator(), true )
                .collect( Collectors.groupingBy( SaraNode::getParent ) );
        IntSummaryStatistics stats = cellMap.values().parallelStream().collect( Collectors.summarizingInt( List::size ) );
        builder.averageCellSize( stats.getAverage() );
        builder.maximalCellSize( stats.getMax() );
        builder.minimalCellSize( stats.getMin() );
        builder.medianCellSize( cellMap.values().parallelStream().mapToInt( List::size )
                .sorted().skip( cellMap.values().size() / 2 ).findFirst().orElseThrow( () -> new RuntimeException( "Cannot find median." ) ) ); // sort sizes, skip first half, select first from the rest
        builder.numberOfCells( (int) stats.getCount() );
        builder.numberOfCutEdges( (int) StreamSupport.stream( bestResultGraph.getEdges().spliterator(), true )
                .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
                .count() );
        if ( display ) {
            System.out.println( "Displaying..." );
            DisplayUtils.display( bestResultGraph );
        }
        if ( routingAlgorithm != null ) {
            System.out.println( "Routing..." );
            TimeMeasurement routingTimeMeasurement = new TimeMeasurement();
            Counter allCounter = new Counter();
            Counter validCounter = new Counter();
            testInput.forEach( ti -> {
                ti.forEach( ( InputElement x ) -> {
                    allCounter.increment();
                    SaraNode sourceNode = bestResultGraph.getNodeById( x.getSourceNodeId() );
                    SaraNode targetNode = bestResultGraph.getNodeById( x.getTargetNodeId() );
                    routingTimeMeasurement.continue_();
                    Optional<Route<SaraNode, SaraEdge>> optionalRoute = routingAlgorithm.route( bestResultGraph, ti.getMetric(), sourceNode, targetNode );
                    routingTimeMeasurement.pause();
                    if ( optionalRoute.isPresent() ) {
                        Route<SaraNode, SaraEdge> route = optionalRoute.get();
                        if ( equals( route.getEdgeList(), x.getEdgeIds() ) ) {
                            validCounter.increment();
                        }
                    }
                } );
            } );
            builder.validRatio( (double) validCounter.getValue() / (double) allCounter.getValue() );
            builder.routingTime( routingTimeMeasurement.getTime() );
        } else {
            builder.validRatio( 0.0 );
            builder.routingTime( new Time( TimeUnits.MILLISECONDS, 0 ) );
        }
        testRunnerTime.setTimeUnits( TimeUnits.MILLISECONDS );
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
