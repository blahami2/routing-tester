package cz.blahami2.routingsaratester.parametertuning.model;

import java.util.List;

import static cz.certicon.routing.utils.validation.Validation.*;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public abstract class ListParameter<T extends Comparable<T>> implements Parameter<T> {

    private final List<T> values;

    public ListParameter( List<T> values ) {
        this.values = values;
        validateThat( "values.size", greaterThan( values.size(), 0 ) );
    }

    @Override
    public T initialValue() {
        return values.get( 0 );
    }

    @Override
    public T finalValue() {
        return values.get( values.size() - 1 );
    }

    @Override
    public T increment( T currentValue ) {
        return values.get( values.indexOf( currentValue ) + 1 );
    }
}
