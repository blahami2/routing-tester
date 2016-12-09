package cz.blahami2.routingsaratester.parametertuning;

import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

import java.util.Iterator;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public interface ParameterTuningStrategy {

    Iterator<PreprocessingInput> preprocessingInputIterator();

    default Iterable<PreprocessingInput> preprocessingInputIterable() {
        return () -> preprocessingInputIterator();
    }

}
