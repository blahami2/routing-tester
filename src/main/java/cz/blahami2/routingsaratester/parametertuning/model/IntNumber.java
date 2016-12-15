package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.model.values.Number;
import lombok.ToString;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
@ToString
public class IntNumber implements Number<IntNumber> {

    private long value;

    public IntNumber( long value ) {
        this.value = value;
    }

    public static IntNumber of( long value ) {
        return new IntNumber( value );
    }

    @Override
    public boolean isGreaterThan( IntNumber intNumber ) {
        return value > intNumber.value;
    }

    @Override
    public boolean isGreaterOrEqualTo( IntNumber intNumber ) {
        return value >= intNumber.value;
    }

    @Override
    public boolean isLowerThan( IntNumber intNumber ) {
        return value < intNumber.value;
    }

    @Override
    public boolean isLowerOrEqualTo( IntNumber intNumber ) {
        return value <= intNumber.value;
    }

    @Override
    public boolean isEqualTo( IntNumber intNumber ) {
        return value == intNumber.value;
    }

    @Override
    public boolean isPositive() {
        return value > 0;
    }

    @Override
    public boolean isNegative() {
        return value < 0;
    }

    @Override
    public IntNumber absolute() {
        return new IntNumber( Math.abs( value ) );
    }

    @Override
    public IntNumber add( IntNumber other ) {
        return new IntNumber( value + other.value );
    }

    @Override
    public IntNumber subtract( IntNumber other ) {
        return new IntNumber( value - other.value );
    }

    @Override
    public IntNumber divide( IntNumber other ) {
        return new IntNumber( value / other.value );
    }

    @Override
    public IntNumber multiply( IntNumber other ) {
        long a = value;
        long b = other.value;
        long ab = a * b;
        if((a > 0 && b > 0) || a < 0 && b < 0){
            if(ab <= 0){
                ab = Long.MAX_VALUE;
            }
        } else if(a == 0 || b == 0) {
            ab = 0;
        } else {
            if(ab >= 0){
                ab = Long.MIN_VALUE + 1;
            }
        }
        return new IntNumber( ab );
    }

    @Override
    public IntNumber identity() {
        return this;
    }

    @Override
    public int compareTo( Number<IntNumber> o ) {
        return Long.compare( value, o.identity().value );
    }

    public long getValue() {
        return value;
    }
}
