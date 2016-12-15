package cz.blahami2.routingsaratester.parametertuning.logic;

import cz.blahami2.routingsaratester.parametertuning.ParameterTuningStrategy;
import cz.blahami2.routingsaratester.parametertuning.model.Parameter;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static cz.certicon.routing.utils.validation.Validation.*;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class CombinationParameterTuningStrategy implements ParameterTuningStrategy {

    private final PreprocessingInput defaultOptions;
    private final List<Parameter<? extends Comparable<?>>> parameters;
    private final int[][] testMatrix;
    private final Comparable<?>[][] inputMatrix;

    public CombinationParameterTuningStrategy( PreprocessingInput defaultOptions, List<Parameter<? extends Comparable<?>>> parameters, int[][] testMatrix ) {
        this.defaultOptions = defaultOptions;
        this.parameters = parameters;
        this.testMatrix = testMatrix;
        // assert testMatrix.length > 0
        validateThat( "testMatrix.length", greaterThan( testMatrix.length, 0 ) );
        // assert testMatrix[0].length == parameters.size();
        validateThat( "testMatrix[0].length", equalTo( testMatrix[0].length, parameters.size() ) );
        // assert foreach parameter:
        //      max = testMatrix.findMax(parameter)
        //      parameter.iterator.count() == max+1;
        this.inputMatrix = new Comparable<?>[parameters.size()][];
        for ( int i = 0; i < parameters.size(); i++ ) {
            Parameter parameter = parameters.get( i );
            int finalI = i;
            ArrayList<Comparable<?>> list = new ArrayList<>();
            for ( Comparable<?> o : (Iterable<? extends Comparable<?>>) parameter.iterable() ) {
                list.add( o );
            }
            int max = Arrays.stream( testMatrix ).mapToInt( row -> row[finalI] ).max().getAsInt();
            validateThat( "Parameter[" + i + "] has lower than matrix.max iterations", greaterOrEqualTo( list.size(), max + 1 ) );
            inputMatrix[i] = list.toArray( new Comparable<?>[list.size()] );
        }

    }

    @Override
    public Iterator<PreprocessingInput> preprocessingInputIterator() {
        return new Iterator<PreprocessingInput>() {
            int rowCounter = 0;

            @Override
            public boolean hasNext() {
                return rowCounter < testMatrix.length;
            }

            @Override
            public PreprocessingInput next() {
                PreprocessingInput input = defaultOptions;
                int[] row = testMatrix[rowCounter++];
                for ( int i = 0; i < parameters.size(); i++ ) {
                    Parameter parameter = parameters.get( i );
                    input = parameter.changeInput( input, inputMatrix[i][row[i]] );
                }
                return input;
            }
        };
    }

    /*
    00000000
    01234561
    02461352
    03625143
    04152634
    05316425
    06543216
    10654326
    11111110
    12345601
    13502462
    14036253
    15263044
    16420535
    20531645
    21065436
    22222220
    23456011
    24613502
    25140363
    26304154
    30415264
    31642055
    32106546
    33333330
    34560121
    35024612
    36251403
    40362513
    41526304
    42053165
    43210656
    44444440
    45601231
    46135022
    50246132
    51403623
    52630414
    53164205
    54321066
    55555550
    56012341
    60123451
    61350242
    62514033
    63041524
    64205315
    65432106
    66666660
    */
}
