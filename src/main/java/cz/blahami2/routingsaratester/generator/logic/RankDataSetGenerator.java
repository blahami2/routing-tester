package cz.blahami2.routingsaratester.generator.logic;

import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.view.DebugViewer;
import java8.util.Optional;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class RankDataSetGenerator implements DataSetGenerator {

    @Setter
    private int minRank;
    @Setter
    private DebugViewer debugViewer = null;

    /**
     * @param minRank starting Dijkstra's rank of the shortest path
     */
    public RankDataSetGenerator( int minRank ) {
        this.minRank = minRank;
    }

    @Override
    public <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, int granularity, Metric metric, Graph<N, E> graph, DebugViewer debugViewer ) {
        this.debugViewer = debugViewer;
        return generateDataSet( size, granularity, metric, graph );
    }

    public <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, int granularity, Metric metric, Graph<N, E> graph ) {
        int intervals = granularity;
        int inputsPerInterval = size / intervals;
        NodeGenerator nodeGenerator = new NodeGenerator();
        int maxRank = graph.getNodesCount();
        double multiplier = Math.pow( maxRank / minRank, 1.0 / ( intervals - 1 ) );
        OneToAllDijkstra algorithm = new OneToAllDijkstra();
        algorithm.setDebugViewer( debugViewer );
        List<DataSetElement<N, E>> result = new ArrayList<>();
        for ( int i = minRank; i <= maxRank; i *= multiplier ) {
            int finalI = i;
            List<DataSetElement<N, E>> elements = nodeGenerator.generatorStream( graph )
                    .map( node -> {
                        Optional<Route<N, E>> optRoute = algorithm.routeToRank( metric, node, finalI );
                        return optRoute.isPresent() ?
                                new DataSetElement<>( node, optRoute.get().getTarget(), optRoute.get() ) :
                                null;
//                        Route<N, E> route = optRoute.orElseThrow( () -> new IllegalStateException( "Did not find route long enough: required = " + finalI + ", possible: " + graph.getNodesCount() ) );
//                        return new DataSetElement<>( node, route.getTarget(), route );
                    } )
                    .filter( element -> element != null )
                    .limit( inputsPerInterval )
                    .collect( Collectors.toList() );
            result.addAll( elements );
            System.out.println( "Ranks #" + i + " - done." );
        }
        return result;
    }
}
