/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

import java.util.Iterator;

/**
 * @param <T> value type
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public interface Parameter<T extends Comparable<T>> extends Iterable<T> {

    PreprocessingInput changeInput( PreprocessingInput input, T currentValue );

    default Iterable<T> iterable() {
        return () -> iterator();
    }

    @Override
    default Iterator<T> iterator() {
        return new Iterator<T>() {
            T currentValue = null;
            T finalValue = finalValue();

            @Override
            public boolean hasNext() {
                if(currentValue == null){
                    return initialValue().compareTo( finalValue ) <= 0;
                }
                return increment( currentValue ).compareTo( finalValue ) <= 0;
            }

            @Override
            public T next() {
                if ( currentValue == null ) {
                    currentValue = initialValue();
                } else {
                    currentValue = increment( currentValue );
                }
                return currentValue;
            }
        };
    }

    default Iterable<PreprocessingInput> iterable( PreprocessingInput defaultPreprocessingInput ) {
        return () -> iterator( defaultPreprocessingInput );
    }

    default Iterator<PreprocessingInput> iterator( PreprocessingInput defaultPreprocessingInput ) {
        final Iterator<T> it = iterator();
        return new Iterator<PreprocessingInput>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public PreprocessingInput next() {
                return changeInput( defaultPreprocessingInput, it.next() );
            }
        };
    }

    T initialValue();

    T finalValue();

    T increment( T currentValue );
}
