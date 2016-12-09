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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import java8.util.Optional;
import lombok.Setter;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class MetricDataSetGenerator implements DataSetGenerator {

    @Setter
    private int attemptsPerUnit;
    @Setter
    private Distance estimatedMaximalDistance;

    /**
     * @param attemptsPerUnit          maximal amount of attempts per unit (size) for the
     *                                 algorithm to fill all the buckets, then it terminates
     * @param estimatedMaximalDistance estimated distance of maximal route, no
     *                                 inputs will be higher than this value, however, fewer inputs will be
     *                                 found if it is too high
     */
    public MetricDataSetGenerator( int attemptsPerUnit, Distance estimatedMaximalDistance ) {
        this.attemptsPerUnit = attemptsPerUnit;
        this.estimatedMaximalDistance = estimatedMaximalDistance;
    }

    @Override
    public <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, int granularity, Metric metric, Graph<N, E> graph ) {
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
                                Comparator.comparing( a -> a.getDistance( metric ) )
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
