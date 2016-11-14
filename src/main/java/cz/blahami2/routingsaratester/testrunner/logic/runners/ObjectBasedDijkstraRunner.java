/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.logic.runners;

import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.comparator.controller.ComparatorController;
import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.data.GraphDAO;
import cz.certicon.routing.data.SqliteGraphDAO;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java8.util.Optional;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ObjectBasedDijkstraRunner implements ComparatorController.Runner {

    private Graph graph;

    @Override
    public void prepare( Properties connectionProperties ) throws IOException {
        GraphDAO dao = new SqliteGraphDAO( connectionProperties );
        graph = dao.loadGraph();
    }

    @Override
    public boolean run( Input input, TimeMeasurement routeTime ) {
        final RoutingAlgorithm alg = new DijkstraAlgorithm();
        IdSupplier counter = new IdSupplier( 0 );
        return input.stream().map( new Function<InputElement, Boolean>() {
            @Override
            public Boolean apply( InputElement x ) {
                routeTime.continue_();
                Optional<cz.certicon.routing.model.Route> route = alg.route( Metric.LENGTH, graph.getNodeById( x.getSourceNodeId() ), graph.getNodeById( x.getTargetNodeId() ) );
                routeTime.pause();
                java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                if ( !route.isPresent() ) {
                    System.out.print( "Route not found for: " + x.getSourceNodeId() + " -> " + x.getTargetNodeId() );
                    System.out.print( " (" + ( x.getSourceNodeId() ) + " -> " + ( x.getTargetNodeId() ) + ")" );
                    System.out.println( ", not found " + counter.next() + "/" + input.size() );
                    return true;
                }
                String expected = x.getEdgeIds().stream().map( e -> e + "" ).collect( Collectors.joining( " " ) );
                String actual = route.get().getEdgeList().stream().mapToLong( e -> ( (Edge) e ).getId() ).mapToObj( e -> e + "" ).collect( Collectors.joining( " " ) );
                boolean result = route.get().getEdgeList().stream()
                        .filter( edge -> {
                            Edge e1 = (Edge) edge;
                            Edge e2 = (Edge) graph.getEdgeById( edgeIdIterator.next() );
                            if ( e1.getId() == e2.getId() ) {
                                return false;
                            }
                            if ( e1.getLength( Metric.LENGTH ).equals( e2.getLength( Metric.LENGTH ) )
                                    && ( ( e1.getSource().equals( e2.getSource() ) && e1.getTarget().equals( e2.getTarget() ) )
                                    || ( e1.getSource().equals( e2.getTarget() ) && e1.getTarget().equals( e2.getSource() ) ) ) ) {
                                return false;
                            }
                            return true;
                        } )
                        .count() == 0;
                if ( !result ) {
                    System.out.println( "Routes do not match for id: " + x.getId() );
                    System.out.println( "Route ref: length = " + x.getLength() + ", time = " + x.getTime() + ", edges = " + expected );
                    System.out.println( "Route res: length = " + (int) route.get().calculateDistance( Metric.LENGTH ).getValue() + ", time = " + (int) route.get().calculateDistance( Metric.TIME ).getValue() + " s, edges = " + actual );
                }
                return result;
            }
        } ).allMatch( x -> x );
    }
}
