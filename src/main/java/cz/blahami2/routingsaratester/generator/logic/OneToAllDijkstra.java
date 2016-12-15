package cz.blahami2.routingsaratester.generator.logic;

import cz.certicon.routing.algorithm.OneToAllRoutingAlgorithm;
import cz.certicon.routing.data.SqliteGraphDataDAO;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.graph.State;
import cz.certicon.routing.model.queue.FibonacciHeap;
import cz.certicon.routing.model.queue.PriorityQueue;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.view.DebugViewer;
import cz.certicon.routing.view.JxDebugViewer;
import cz.certicon.routing.view.jxmap.AbstractJxMapViewer;
import java8.util.Optional;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class OneToAllDijkstra {

    private Optional<DebugViewer> debugViewer = Optional.empty();

    public void setDebugViewer( DebugViewer debugViewer ) {
        this.debugViewer = Optional.ofNullable( debugViewer );
    }

    public <N extends Node<N, E>, E extends Edge<N, E>> Optional<Route<N, E>> routeToRank( Metric metric, N sourceNode, int rank ) {
        Map<State<N, E>, Distance> nodeDistanceMap = new HashMap<>();
        PriorityQueue<State<N, E>> pqueue = new FibonacciHeap<>();
        putNodeDistance( nodeDistanceMap, pqueue, new State( sourceNode, null ), Distance.newZeroDistance() );
        Map<State, State> predecessorMap = new HashMap<>();
        Set<State> closedStates = new HashSet<>();
        Set<N> closedNodes = new HashSet<>();
        int rankCounter = 0;
        State finalState = null;
        while ( !pqueue.isEmpty() ) {
            State<N, E> state = pqueue.extractMin();
            debugViewer.ifPresent( dv -> {
                dv.displayNode( state.getNode().getId() );
                if ( !state.isFirst() ) {
                    dv.displayEdge( state.getEdge().getId() );
                }
            } );
//            System.out.println((rankCounter+1) + ":" + state);
            Distance distance = nodeDistanceMap.get( state );
            closedStates.add( state );
            if ( !closedNodes.contains( state.getNode() ) && ++rankCounter == rank ) {
                finalState = state;
                break;
            }
            closedNodes.add( state.getNode() );
            for ( E edge : state.getNode().getOutgoingEdges() ) {
                N targetNode = edge.getOtherNode( state.getNode() );
                State targetState = new State( targetNode, edge );
                if ( !closedStates.contains( targetState ) ) {
                    Distance targetDistance = ( nodeDistanceMap.containsKey( targetState ) ) ? nodeDistanceMap.get( targetState ) : Distance.newInfinityInstance();
                    Distance alternativeDistance = distance
                            .add( edge.getLength( metric ) )
                            .add( state.isFirst() ? Distance.newZeroDistance() : state.getNode().getTurnDistance( state.getEdge(), edge ) );
                    if ( alternativeDistance.isLowerThan( targetDistance ) ) {
                        putNodeDistance( nodeDistanceMap, pqueue, targetState, alternativeDistance );
                        predecessorMap.put( targetState, state );
                    }
                }
            }
        }

        Optional<Route<N, E>> result = Optional.empty();
        if ( finalState != null ) {
            Route.RouteBuilder<N, E> builder = Route.builder();
            State<N, E> currentState = finalState;
            while ( currentState != null && !currentState.isFirst() ) {
                builder.addAsFirst( currentState.getEdge() );
                currentState = predecessorMap.get( currentState );
            }
            result = Optional.of( builder.build() );
        }
        return result;
    }

    private <N extends Node<N, E>, E extends Edge<N, E>> void putNodeDistance( Map<State<N, E>, Distance> nodeDistanceMap, PriorityQueue<State<N, E>> pqueue, State<N, E> node, Distance distance ) {
        pqueue.decreaseKey( node, distance.getValue() );
        nodeDistanceMap.put( node, distance );
    }
}
