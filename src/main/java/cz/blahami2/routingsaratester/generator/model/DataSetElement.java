/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.model;

import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Node;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 * @param <N> node type
 * @param <E> edge type
 */
@Value
public final class DataSetElement<N extends Node<N, E>, E extends Edge<N, E>> {
    
    private final N sourceNode;
    private final N targetNode;
    private final Route<N,E> route;
    
}
