/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.model;

import cz.certicon.routing.model.Identifiable;
import cz.certicon.routing.model.RouteData;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.Time;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
@Value
@EqualsAndHashCode
public class OutputElement implements Identifiable {

    long id;
    Length length;
    Time time;
    List<Long> edgeIds;

    public static OutputElement fromRoute( long id, RouteData<? extends Edge> routeData ) {
        return new OutputElement( id, routeData.getLength(), routeData.getTime(), routeData.getEdges().stream().map( e -> e.getId() ).collect( Collectors.toList() ) );
    }
}
