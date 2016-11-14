/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.model;

import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.values.Distance;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 * @param <N> node type
 * @param <E> edge type
 */
@Value
public final class DataSetElement<N extends Node<N, E>, E extends Edge<N, E>> {

    private final N sourceNode;
    private final N targetNode;
    private final Route<N, E> route;

    private Map<Metric, Distance> distanceMap = new HashMap<>();

    public Distance getDistance( Metric metric ) {
        Distance dist = distanceMap.get( metric );
        if ( dist == null ) {
            dist = route.calculateDistance( metric );
            distanceMap.put( metric, dist );
        }
        return dist;
    }
}
