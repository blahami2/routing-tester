/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.logic;

import cz.blahami2.routingsaratester.common.model.Counter;
import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.values.Coordinate;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.utils.RandomUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java8.util.Optional;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class DataSetGenerator {

    private static final int INTERVALS = 10;
    private static final int MAX_ATTEMPTS_PER_UNIT = 1000;

    /**
     * Generates dataset following the given criteria
     *
     * @param <N> node type of the graph
     * @param <E> edge type of the graph
     * @param size amount of data elements, not guaranteed - see attemptsPerUnit
     * @param granularity amount of "buckets", higher the amount, more uniform
     * the distribution
     * @param attemptsPerUnit maximal amount of attempts per unit (size) for the
     * algorithm to fill all the buckets, then it terminates
     * @param estimatedMaximalDistance estimated distance of maximal route, no
     * inputs will be higher than this value, however, fewer inputs will be
     * found if it is too high
     * @param metric routing metric
     * @param graph graph to search on
     * @return dataset
     */
    public <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, int granularity, int attemptsPerUnit, Distance estimatedMaximalDistance, Metric metric, Graph<N, E> graph ) {
        RoutingAlgorithm<N, E> algorithm = new DijkstraAlgorithm<>();
        int range = (int) estimatedMaximalDistance.getValue();
        int intervals = granularity;
        int inputsPerInterval = size / intervals;
        int intervalSize = range / intervals;
        NodePairGenerator nodePairGenerator = new NodePairGenerator();
        final int maxIterations = attemptsPerUnit * size;
        List<List<DataSetElement<N, E>>> intervalList = new ArrayList<>();
        for ( int i = 0; i < intervals; i++ ) {
            intervalList.add( new ArrayList<>() );
        }
        /*
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
        }*/

        int numberOfNodes = 1000;
        NodeGenerator nodeGenerator = new NodeGenerator();
        Random rand = RandomUtils.createRandom();
        for ( int i = 0; i < inputsPerInterval; i++ ) {
            Set<N> nodeSet = new HashSet<>();
            Iterator<N> generatorIterator = nodeGenerator.generatorIterator( graph );
            while ( nodeSet.size() < numberOfNodes ) {
                nodeSet.add( generatorIterator.next() );
            }
            List<N> nodes = new ArrayList<>( nodeSet );
            List<Pair<N, N>> pairs = nodes.stream()
                    .flatMap( n1
                            -> nodes.stream().map( n2 -> new Pair<>( n1, n2 ) )
                    ).sorted( ( p1, p2 ) -> compare( p1, p2 ) )
                    .collect( Collectors.toList() );
            Set<Integer> unfinished1 = IntStream.range( 0, intervals ).boxed().collect( Collectors.toSet() );
            for ( int j = 0; j < intervals; j++ ) {
                if ( unfinished1.contains( j ) ) {
                    Counter direction = new Counter( -j, intervals - j - 1 );
                    for ( int k = 0; k < maxIterations && unfinished1.contains( j ); k++ ) {
                        int intervalIdx = j;
                        int minLength = intervalSize * j;
                        int maxLength = intervalSize * ( j + 1 );
                        int nodeIntervalSize = ( numberOfNodes * numberOfNodes ) / intervals;
                        int idx = nodeIntervalSize * ( j + direction.getValue() ) + rand.nextInt( nodeIntervalSize );
                        Pair<N, N> pair = pairs.get( idx );
                        Optional<Route<N, E>> optionalRoute = algorithm.route( metric, pair.a, pair.b );
                        if ( optionalRoute.isPresent() ) {
                            Route<N, E> route = optionalRoute.get();
                            Distance distance = route.calculateDistance( metric );
                            int length = (int) distance.getValue();
//                            System.out.println("interval[" + j + "], iteration[" + k + "], pair: " + pair.a.getId() + "-" + pair.b.getId() + ", length = " + length + ", min = " + minLength + ", max = " + maxLength + ", counter = " + direction);
                            if ( length < minLength ) {
                                direction.increment();
                            } else if ( length > maxLength ) {
                                direction.decrement();
                            } else {
                                List<DataSetElement<N, E>> targetInterval = intervalList.get( intervalIdx );
                                targetInterval.add( new DataSetElement<>( pair.a, pair.b, route ) );
                                unfinished1.remove( j );
                                System.out.println( "added to bucket[" + intervalIdx + "] => " + targetInterval.size() );
                            }
                        }
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

    private static <N extends Node<N, E>, E extends Edge<N, E>> int compare( Pair<N, N> pair1, Pair<N, N> pair2 ) {
        double dist1 = calculateDistance( pair1.a.getCoordinate(), pair1.b.getCoordinate() );
        double dist2 = calculateDistance( pair2.a.getCoordinate(), pair2.b.getCoordinate() );
        return Double.compare( dist1, dist2 );
    }

    private static double calculateDistance( Coordinate c1, Coordinate c2 ) {
        double diffLat = c1.getLatitude() - c2.getLatitude();
        double diffLon = c1.getLongitude() - c2.getLongitude();
        return diffLat * diffLat + diffLon * diffLon;
    }

}
