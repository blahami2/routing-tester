/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 * @param <T> value type
 */
public interface Parameter<T extends Comparable<T>> {

    public String getHeader();

    public PreprocessingInput changeInput( PreprocessingInput input, T currentValue );

    public T initialValue();

    public T finalValue();

    public T increment( T currentValue );
}
