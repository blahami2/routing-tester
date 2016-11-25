/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.model;

import static cz.certicon.routing.utils.validation.Validation.*;
import lombok.ToString;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
@ToString
public class Counter {

    private int value = 0;
    private final int minValue;
    private final int maxValue;

    public Counter() {
        this.minValue = Integer.MIN_VALUE;
        this.maxValue = Integer.MAX_VALUE;
    }

    public Counter( int minValue, int maxValue ) {
        validateThat( valid()
                .and( "minValue <= maxValue", smallerOrEqualTo( minValue, maxValue ) ) );
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Counter( int initialValue, int minValue, int maxValue ) {
        validateThat( valid()
                .and( "minValue <= maxValue", smallerOrEqualTo( minValue, maxValue ) )
                .and( "initialValue >= minValue", greaterOrEqualTo( initialValue, minValue ) )
                .and( "initialValue <= maxValue", smallerOrEqualTo( initialValue, maxValue ) ) );
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int increment() {
        if ( value < maxValue ) {
            value++;
        }
        return value;
    }

    public int decrement() {
        if ( value > minValue ) {
            value--;
        }
        return value;
    }

    public int getValue() {
        return value;
    }
    
    

}
