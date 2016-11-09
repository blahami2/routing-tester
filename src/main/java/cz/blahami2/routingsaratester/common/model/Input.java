/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.model;

import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.model.values.TimeUnits;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Getter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class Input implements Iterable<InputElement> {

    private final TLongObjectMap<InputElement> map;
    @Getter
    private final Metric metric;

    public Input( TLongObjectMap<InputElement> map, Metric metric ) {
        this.map = map;
        this.metric = metric;
    }

    public InputElement getInputElement( long inputId ) {
        return map.get( inputId );
    }

    @Override
    public Iterator<InputElement> iterator() {
        List<InputElement> elements = new ArrayList<>( map.valueCollection() );
        elements.sort( Comparator.comparing( InputElement::getId ) );
        return elements.iterator();
    }

    public Stream<InputElement> stream() {
        return StreamSupport.stream( spliterator(), false );
    }

    public Stream<InputElement> stream( boolean parallel ) {
        return StreamSupport.stream( spliterator(), parallel );
    }

    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return "{"
                + StreamSupport.stream( spliterator(), false )
                .sorted( Comparator.comparing( InputElement::getId ) )
                .map( e -> "{" + e.getId() + " "
                        + e.getSourceNodeId() + " "
                        + e.getTargetNodeId() + " "
                        + e.getLength().getValue( LengthUnits.METERS ) + " "
                        + e.getTime().getValue( TimeUnits.SECONDS ) + " "
                        + e.getEdgeIds().stream().map( x -> x.toString() ).collect( Collectors.joining( " " ) ) + "}" )
                .collect( Collectors.joining( "," ) )
                + "}";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode( this.map );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Input other = (Input) obj;
        for ( long key : map.keys() ) {
            if ( !other.map.containsKey( key ) || !map.get( key ).equals( other.map.get( key ) ) ) {
                return false;
            }
        }
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final TLongObjectMap<InputElement> m;
        private Metric metric = Metric.LENGTH;

        public Builder() {
            this.m = new TLongObjectHashMap<>();
        }

        public Builder setMetric( Metric metric ) {
            this.metric = metric;
            return this;
        }

        public Builder add( InputElement inputElement ) {
            m.put( inputElement.getId(), inputElement );
            return this;
        }

        public Builder add( Collection<? extends InputElement> inputElements ) {
            inputElements.stream().forEach( ( inputElement ) -> m.put( inputElement.getId(), inputElement ) );
            return this;
        }

        public Builder add( InputElement... inputElements ) {
            Arrays.stream( inputElements ).forEach( ( inputElement ) -> m.put( inputElement.getId(), inputElement ) );
            return this;
        }

        public Input build() {
            return new Input( m, metric );
        }
    }
}
