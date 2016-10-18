/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.model;

import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.model.values.TimeUnits;
import gnu.trove.map.TLongObjectMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class Output implements Iterable<OutputElement> {

    private final TLongObjectMap<OutputElement> map;

    private Output( TLongObjectMap<OutputElement> map ) {
        this.map = map;
    }

    public OutputElement getOutputElement( long inputId ) {
        return map.get( inputId );
    }

    @Override
    public Iterator<OutputElement> iterator() {
        return map.valueCollection().iterator();
    }

    @Override
    public String toString() {
        return "{"
                + StreamSupport.stream( spliterator(), false )
                .sorted( Comparator.comparing( OutputElement::getId ) )
                .map( e -> "{" + e.getId() + " "
                        + e.getLength().getValue( LengthUnits.METERS ) + " "
                        + e.getTime().getValue( TimeUnits.SECONDS ) + " "
                        + e.getEdgeIds().stream().map( x -> x.toString() ).collect( Collectors.joining( " " ) ) + "}" )
                .collect( Collectors.joining( "," ) )
                + "}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode( this.map );
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
        final Output other = (Output) obj;
        for ( long key : map.keys() ) {
            if ( !other.map.containsKey( key ) || !map.get( key ).equals( other.map.get( key ) ) ) {
                return false;
            }
        }
        return true;
    }

}
