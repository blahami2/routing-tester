package cz.blahami2.routingsaratester.generator.logic;

import cz.certicon.routing.algorithm.OneToAllRoutingAlgorithm;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.graph.State;
import cz.certicon.routing.model.queue.FibonacciHeap;
import cz.certicon.routing.model.queue.PriorityQueue;
import cz.certicon.routing.model.values.Distance;
import java8.util.Optional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class OneToAllDijkstra {

    public <N extends Node<N, E>, E extends Edge<N, E>> Optional<Route<N, E>> routeToRank( Metric metric, N sourceNode, int rank ) {
        Map<State<N, E>, Distance> nodeDistanceMap = new HashMap<>();
        PriorityQueue<State<N, E>> pqueue = new FibonacciHeap<>();
        putNodeDistance( nodeDistanceMap, pqueue, new State( sourceNode, null ), Distance.newInstance( 0 ) );
        Map<State, State> predecessorMap = new HashMap<>();
        Set<State> closedStates = new HashSet<>();
        int rankCounter = 0;
        State finalState = null;
        while ( !pqueue.isEmpty() ) {
            State<N, E> state = pqueue.extractMin();
//            System.out.println((rankCounter+1) + ":" + state);
            Distance distance = nodeDistanceMap.get( state );
            closedStates.add( state );
            if ( ++rankCounter == rank ) {
                finalState = state;
                break;
            }
            for ( E edge : state.getNode().getOutgoingEdges() ) {
                N targetNode = edge.getOtherNode( state.getNode() );
                State targetState = new State( targetNode, edge );
                if ( !closedStates.contains( targetState ) ) {
                    Distance targetDistance = ( nodeDistanceMap.containsKey( targetState ) ) ? nodeDistanceMap.get( targetState ) : Distance.newInfinityInstance();
                    Distance alternativeDistance = distance
                            .add( edge.getLength( metric ) )
                            .add( state.isFirst() ? Distance.newInstance( 0 ) : state.getNode().getTurnDistance( state.getEdge(), edge ) );
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
