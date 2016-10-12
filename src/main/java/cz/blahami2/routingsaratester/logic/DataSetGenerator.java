/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.logic;

import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.utils.java8.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class DataSetGenerator {

    public <N extends Node<N, E>, E extends Edge<N, E>> Collection<Pair<N, N>> generateDataSet( int size, Length estimatedMaximalLength, Graph<N, E> graph ) {
        RoutingAlgorithm<N, E> algorithm = new DijkstraAlgorithm<>();
        int range = (int) estimatedMaximalLength.getValue( LengthUnits.METERS );
        int intervals = 10;
        int inputsPerInterval = size / intervals;
        int intervalSize = range / intervals;
        NodePairGenerator nodePairGenerator = new NodePairGenerator();

        List<List<Pair<N, N>>> intervalList = new ArrayList<>();
        for ( int i = 0; i < intervals; i++ ) {
            intervalList.add( new ArrayList<>() );
        }
        Set<Integer> unfinished = IntStream.range( 0, intervals ).boxed().collect( Collectors.toSet() );
        Iterator<Pair<N, N>> generator = nodePairGenerator.generatorIterator( graph );
        int sum = 0;
        int maxSum = size - ( inputsPerInterval * intervals );
        while ( !unfinished.isEmpty() || sum < maxSum ) {
            Pair<N, N> pair = generator.next();
            Optional<Route<N, E>> optionalRoute = algorithm.route( graph, Metric.LENGTH, pair.a, pair.b );
            if ( optionalRoute.isPresent() ) {
                Route<N, E> route = optionalRoute.get();
                int length = (int) route.getEdgeList().stream().mapToDouble( x -> x.getLength( Metric.LENGTH ).getValue() ).sum();
                int targetIntervalIdx = length / intervalSize;
                List<Pair<N, N>> targetInterval = intervalList.get( targetIntervalIdx );
                if ( targetInterval.size() < inputsPerInterval ) {
                    targetInterval.add( pair );
                } else if ( targetInterval.size() <= inputsPerInterval && sum < maxSum ) {
                    targetInterval.add( pair );
                    sum++;
                }
                if ( targetInterval.size() >= inputsPerInterval ) {
                    unfinished.remove( targetIntervalIdx );
                }
            }
        }
        return intervalList.stream().flatMap( x -> x.stream() ).collect( Collectors.toList() );
    }
}
