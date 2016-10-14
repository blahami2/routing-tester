/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.logic;

import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.java8.Optional;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

    private static final int INTERVALS = 10;
    private static final int MAX_ATTEMPTS_PER_UNIT = 1000;

    public <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, Distance estimatedMaximalDistance, Metric metric, Graph<N, E> graph ) {
        RoutingAlgorithm<N, E> algorithm = new DijkstraAlgorithm<>();
        int range = (int) estimatedMaximalDistance.getValue();
        int intervals = INTERVALS;
        int inputsPerInterval = size / intervals;
        int intervalSize = range / intervals;
        NodePairGenerator nodePairGenerator = new NodePairGenerator();
        final int maxIterations = MAX_ATTEMPTS_PER_UNIT * size;
        List<List<DataSetElement<N, E>>> intervalList = new ArrayList<>();
        for ( int i = 0; i < intervals; i++ ) {
            intervalList.add( new ArrayList<>() );
        }
        Set<Integer> unfinished = IntStream.range( 0, intervals ).boxed().collect( Collectors.toSet() );
        Iterator<Pair<N, N>> generator = nodePairGenerator.generatorIterator( graph );
        int sum = 0;
        int maxSum = size - ( inputsPerInterval * intervals );
        int attempts = 0;
        int step = maxIterations / 100;
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        timeMeasurement.setTimeUnits( TimeUnits.MILLISECONDS );
        while ( ( !unfinished.isEmpty() || sum < maxSum ) && attempts++ < maxIterations ) {
            if ( attempts % step == 0 ) {
                System.out.println( "done " + ( attempts / step ) + " % attempts. Average dijkstra time = " + timeMeasurement.getTime().divide( attempts ).toString() );
            }
            Pair<N, N> pair = generator.next();
            timeMeasurement.continue_();
            Optional<Route<N, E>> optionalRoute = algorithm.route( graph, metric, pair.a, pair.b );
            timeMeasurement.pause();
            if ( optionalRoute.isPresent() ) {
                Route<N, E> route = optionalRoute.get();
                Distance distance = route.calculateDistance( metric );
                int length = (int) distance.getValue();
                int targetIntervalIdx = length / intervalSize;
                if ( targetIntervalIdx >= intervals ) {
                    targetIntervalIdx = intervals - 1;
                }
                if ( unfinished.contains( targetIntervalIdx ) ) {
                    List<DataSetElement<N, E>> targetInterval = intervalList.get( targetIntervalIdx );
                    if ( targetInterval.size() < inputsPerInterval ) {
                        targetInterval.add( new DataSetElement<>( pair.a, pair.b, route ) );
                        System.out.println( "added to bucket[" + targetIntervalIdx + "] => " + targetInterval.size() );
                    } else if ( targetInterval.size() <= inputsPerInterval && sum < maxSum ) {
                        targetInterval.add( new DataSetElement<>( pair.a, pair.b, route ) );
                        System.out.println( "added to bucket[" + targetIntervalIdx + "] => " + targetInterval.size() + ", sum[" + sum + "] < maxSum[" + maxSum + "]" );
                        sum++;
                    }
                    if ( targetInterval.size() >= inputsPerInterval ) {
                        unfinished.remove( targetIntervalIdx );
                        System.out.println( "bucket[" + targetIntervalIdx + "] finished!" );
                    }
                }
            }
        }
        return intervalList.stream()
                .flatMap(
                        x -> x.stream().sorted(
                                ( a, b ) -> a.getDistance( metric ).compareTo( b.getDistance( metric ) )
                        )
                ).collect( Collectors.toList() );
    }

}
