/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.logic;

import cz.blahami2.routingsaratester.model.TestOptions;
import cz.blahami2.routingsaratester.model.TestResult;
import cz.certicon.routing.algorithm.sara.preprocessing.assembly.Assembler;
import cz.certicon.routing.algorithm.sara.preprocessing.assembly.GreedyAssembler;
import cz.certicon.routing.algorithm.sara.preprocessing.filtering.Filter;
import cz.certicon.routing.algorithm.sara.preprocessing.filtering.NaturalCutsFilter;
import cz.certicon.routing.model.basic.MaxIdContainer;
import cz.certicon.routing.model.graph.Cell;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.SaraGraph;
import cz.certicon.routing.model.graph.SaraNode;
import cz.certicon.routing.model.graph.preprocessing.ContractGraph;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.DisplayUtils;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.view.JxPartitionViewer;
import cz.certicon.routing.view.PartitionViewer;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class TestRunner implements Runnable {

    private final Graph graph;
    private final TestOptions input;
    private TestResult result;

    public TestRunner( Graph graph, TestOptions input ) {
        this.graph = graph;
        this.input = input;
    }

    @Override
    public void run() {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        TestResult.TestResultBuilder builder = TestResult.builder();
        System.out.println( "Filtering..." );
        time.start();
        Filter filter = new NaturalCutsFilter( input.getCellRatio(), 1 / input.getCoreRatio(), input.getCellSize() );
        ContractGraph filteredGraph = filter.filter( graph );
        time.stop();
        System.out.println( "Assembly..." );
        builder.filteringTime( time.getTime() );
        time.start();
        Assembler assembler = new GreedyAssembler( input.getLowIntervalProbability(), input.getLowIntervalLimit(), input.getCellSize() );
        Optional<SaraGraph> best = Optional.empty();
        long bestCutEdgeCount = Long.MAX_VALUE;
        for ( int j = 0; j < input.getNumberOfAssemblyRuns(); j++ ) {
            SaraGraph saraGraph = assembler.assemble( graph, filteredGraph, new MaxIdContainer( 0 ), 3 );
            long cutEdgeCount = StreamSupport.stream( saraGraph.getEdges().spliterator(), true )
                    .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
                    .count();
            if ( cutEdgeCount < bestCutEdgeCount ) {
                best = Optional.of( saraGraph );
                bestCutEdgeCount = cutEdgeCount;
            }
            if ( j % 100 == 0 ) {
                System.out.println( "- assembled #" + j );
            }
        }
        time.stop();
        builder.assemblyTime( time.getTime() );
        SaraGraph bestResult = best.orElseThrow( () -> new RuntimeException( "No assembly runs." ) );
        Map<Cell, List<SaraNode>> cellMap = StreamSupport.stream( bestResult.getNodes().spliterator(), true )
                .collect( Collectors.groupingBy( SaraNode::getParent ) );
        IntSummaryStatistics stats = cellMap.values().parallelStream().collect( Collectors.summarizingInt( List::size ) );
        builder.averageCellSize( stats.getAverage() );
        builder.maximalCellSize( stats.getMax() );
        builder.minimalCellSize( stats.getMin() );
        builder.medianCellSize( cellMap.values().parallelStream().mapToInt( List::size )
                .sorted().skip( cellMap.values().size() / 2 ).findFirst().orElseThrow( () -> new RuntimeException( "Cannot find median." ) ) ); // sort sizes, skip first half, select first from the rest
        builder.numberOfCells( (int) stats.getCount() );
        builder.numberOfCutEdges( (int) StreamSupport.stream( bestResult.getEdges().spliterator(), true )
                .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
                .count() );
        System.out.println( "Displaying..." );
        DisplayUtils.display( bestResult );
        result = builder.build();
    }

    public TestResult getResult() {
        return result;
    }

    public TestResult runForResult() {
        run();
        return result;
    }
}
