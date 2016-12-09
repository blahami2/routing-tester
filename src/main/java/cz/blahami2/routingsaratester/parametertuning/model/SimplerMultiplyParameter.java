package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.model.values.Number;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public abstract class SimplerMultiplyParameter<T extends Number> extends SimplerParameter<Number<T>> {
    private final T multiplier;

    public SimplerMultiplyParameter( Number<T> initialValue, Number<T> finalValue, T multiplier ) {
        super( initialValue, finalValue );
        this.multiplier = multiplier;
    }

    @Override
    public Number<T> increment( Number<T> currentValue ) {
        return currentValue.multiply( multiplier );
    }
}
