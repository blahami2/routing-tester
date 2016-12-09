package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.model.values.Number;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public abstract class SimplerAddParameter<T extends Number> extends SimplerParameter<Number<T>> {

    private final T addition;

    public SimplerAddParameter( T initialValue, T finalValue, T addition ) {
        super( initialValue, finalValue );
        this.addition = addition;
    }

    @Override
    public Number<T> increment( Number<T> currentValue ) {
        return currentValue.add( addition );
    }
}
