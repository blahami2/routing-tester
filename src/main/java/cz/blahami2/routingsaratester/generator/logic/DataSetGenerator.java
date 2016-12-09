package cz.blahami2.routingsaratester.generator.logic;

import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;

import java.util.List;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public interface DataSetGenerator {

    /**
     * Generates dataset following the given criteria
     *
     * @param <N>         node type of the graph
     * @param <E>         edge type of the graph
     * @param size        amount of data elements, not guaranteed - see attemptsPerUnit
     * @param granularity amount of "buckets", higher the amount, more uniform
     *                    the distribution
     * @param metric      routing metric
     * @param graph       graph to search on
     * @return dataset
     */
    <N extends Node<N, E>, E extends Edge<N, E>> List<DataSetElement<N, E>> generateDataSet( int size, int granularity, Metric metric, Graph<N, E> graph );
}
