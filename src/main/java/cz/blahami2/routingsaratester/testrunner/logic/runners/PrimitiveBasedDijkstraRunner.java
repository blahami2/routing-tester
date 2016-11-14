/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.logic.runners;

import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.comparator.controller.ComparatorController;
import cz.certicon.routing.algorithm.sara.optimized.MultilevelDijkstra;
import cz.certicon.routing.algorithm.sara.optimized.data.OptimizedGraphDAO;
import cz.certicon.routing.algorithm.sara.optimized.model.OptimizedGraph;
import cz.certicon.routing.algorithm.sara.optimized.model.Route;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;
import java8.util.Optional;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class PrimitiveBasedDijkstraRunner implements ComparatorController.Runner {

    private OptimizedGraph graph;

    @Override
    public void prepare( Properties connectionProperties ) throws IOException {
        OptimizedGraphDAO dao = new OptimizedGraphDAO( connectionProperties );
        graph = dao.loadGraph();
    }

    @Override
    public boolean run( Input input, TimeMeasurement routeTime ) {
        final MultilevelDijkstra alg = new MultilevelDijkstra();
        IdSupplier counter = new IdSupplier( 0 );
        return input.stream().map( ( InputElement x ) -> {
            routeTime.continue_();
            Optional<Route> route = alg.route( graph, x.getSourceNodeId(), x.getTargetNodeId(), Metric.LENGTH );
            routeTime.pause();
            java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
            if ( !route.isPresent() ) {
                System.out.print( "Route not found for: " + x.getSourceNodeId() + " -> " + x.getTargetNodeId() );
                System.out.print( " (" + x.getSourceNodeId() + " -> " + x.getTargetNodeId() + ")" );
                System.out.println( ", not found " + counter.next() + "/" + input.size() );
                return true;
            }
            String expected = x.getEdgeIds().stream().map( e -> e + "" ).collect( Collectors.joining( " " ) );
            String actual = Arrays.stream( route.get().getEdges() ).mapToObj( e -> e + "" ).collect( Collectors.joining( " " ) );
            boolean result = Arrays.stream( route.get().getEdges() )
                    .filter( edge -> {
                        int e1 = graph.getEdgeById( edge );
                        int e2 = graph.getEdgeById( edgeIdIterator.next() );
                        if ( e1 == e2 ) {
                            return false;
                        }
                        if ( graph.getDistance( e1, Metric.LENGTH ) == ( graph.getDistance( e2, Metric.LENGTH ) )
                                && ( ( graph.getSource( e1 ) == graph.getSource( e2 ) && graph.getTarget( e1 ) == graph.getTarget( e2 ) )
                                || ( graph.getSource( e1 ) == graph.getTarget( e2 ) && graph.getTarget( e1 ) == graph.getSource( e2 ) ) ) ) {
                            return false;
                        }
                        return true;
                    } )
                    .count() == 0;
            if ( !result ) {
                System.out.println( "Routes do not match for id: " + x.getId() );
                System.out.println( "Route ref: length = " + x.getLength() + ", time = " + x.getTime() + ", edges = " + expected );
                System.out.println( "Route res: length = " + (int) route.get().calculateDistance( graph, Metric.LENGTH ) + ", time = " + (int) route.get().calculateDistance( graph, Metric.TIME ) + " s, edges = " + actual );
            }

            return result;
        } ).allMatch( x -> x );
    }
}
