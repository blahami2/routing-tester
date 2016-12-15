package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.model.values.Number;
import cz.certicon.routing.utils.DoubleComparator;
import lombok.ToString;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
@ToString
public class DoubleNumber implements Number<DoubleNumber> {

    private final double EPS = 10E-9;

    private double value;

    public DoubleNumber( double value ) {
        this.value = value;
    }

    public static DoubleNumber of( double value ) {
        return new DoubleNumber( value );
    }

    @Override
    public boolean isGreaterThan( DoubleNumber doubleNumber ) {
        return DoubleComparator.isGreaterThan( value, doubleNumber.value, EPS );
    }

    @Override
    public boolean isGreaterOrEqualTo( DoubleNumber doubleNumber ) {
        return DoubleComparator.isGreaterOrEqualTo( value, doubleNumber.value, EPS );
    }

    @Override
    public boolean isLowerThan( DoubleNumber doubleNumber ) {
        return DoubleComparator.isLowerThan( value, doubleNumber.value, EPS );
    }

    @Override
    public boolean isLowerOrEqualTo( DoubleNumber doubleNumber ) {
        return DoubleComparator.isLowerOrEqualTo( value, doubleNumber.value, EPS );
    }

    @Override
    public boolean isEqualTo( DoubleNumber doubleNumber ) {
        return DoubleComparator.isEqualTo( value, doubleNumber.value, EPS );
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
    public DoubleNumber absolute() {
        return DoubleNumber.of( Math.abs( value ) );
    }

    @Override
    public DoubleNumber add( DoubleNumber other ) {
        return DoubleNumber.of( value + other.value );
    }

    @Override
    public DoubleNumber subtract( DoubleNumber other ) {
        return DoubleNumber.of( value - other.value );
    }

    @Override
    public DoubleNumber divide( DoubleNumber other ) {
        return DoubleNumber.of( value / other.value );
    }

    @Override
    public DoubleNumber multiply( DoubleNumber other ) {
        return DoubleNumber.of( value * other.value );
    }

    @Override
    public DoubleNumber identity() {
        return this;
    }

    @Override
    public int compareTo( Number<DoubleNumber> o ) {
        return DoubleComparator.compare( value, o.identity().value, EPS );
    }

    public double getValue() {
        return value;
    }
}
