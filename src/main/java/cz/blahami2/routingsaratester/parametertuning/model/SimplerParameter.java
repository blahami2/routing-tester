package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public abstract class SimplerParameter<T extends Comparable<T>> implements Parameter<T> {

    private final T initialValue;
    private final T finalValue;

    public SimplerParameter( T initialValue, T finalValue ) {
        this.initialValue = initialValue;
        this.finalValue = finalValue;
    }

    @Override
    public T initialValue() {
        return initialValue;
    }

    @Override
    public T finalValue() {
        return finalValue;
    }
}
