package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import cz.blahami2.routingsaratester.parametertuning.model.*;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import cz.certicon.routing.model.values.Number;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class LatinSquareParameterSupplier implements Function<Integer, LatinSquareParameterSupplier.LatinSquareParameterMessenger> {

    private static final int SIZE = 7;

    @Override
    public LatinSquareParameterMessenger apply( Integer integer ) {
        return new LatinSquareParameterMessenger(
                Arrays.asList( Parameters.CELL_RATIO, Parameters.CELL_SIZE ),
                new int[][]{
                        { 0, 0, 0, 0, 0, 0, 0, 0 },
                        { 0, 1, 2, 3, 4, 5, 6, 1 },
                        { 0, 2, 4, 6, 1, 3, 5, 2 },
                        { 0, 3, 6, 2, 5, 1, 4, 3 },
                        { 0, 4, 1, 5, 2, 6, 3, 4 },
                        { 0, 5, 3, 1, 6, 4, 2, 5 },
                        { 0, 6, 5, 4, 3, 2, 1, 6 },
                        { 1, 0, 6, 5, 4, 3, 2, 6 },
                        { 1, 1, 1, 1, 1, 1, 1, 0 },
                        { 1, 2, 3, 4, 5, 6, 0, 1 },
                        { 1, 3, 5, 0, 2, 4, 6, 2 },
                        { 1, 4, 0, 3, 6, 2, 5, 3 },
                        { 1, 5, 2, 6, 3, 0, 4, 4 },
                        { 1, 6, 4, 2, 0, 5, 3, 5 },
                        { 2, 0, 5, 3, 1, 6, 4, 5 },
                        { 2, 1, 0, 6, 5, 4, 3, 6 },
                        { 2, 2, 2, 2, 2, 2, 2, 0 },
                        { 2, 3, 4, 5, 6, 0, 1, 1 },
                        { 2, 4, 6, 1, 3, 5, 0, 2 },
                        { 2, 5, 1, 4, 0, 3, 6, 3 },
                        { 2, 6, 3, 0, 4, 1, 5, 4 },
                        { 3, 0, 4, 1, 5, 2, 6, 4 },
                        { 3, 1, 6, 4, 2, 0, 5, 5 },
                        { 3, 2, 1, 0, 6, 5, 4, 6 },
                        { 3, 3, 3, 3, 3, 3, 3, 0 },
                        { 3, 4, 5, 6, 0, 1, 2, 1 },
                        { 3, 5, 0, 2, 4, 6, 1, 2 },
                        { 3, 6, 2, 5, 1, 4, 0, 3 },
                        { 4, 0, 3, 6, 2, 5, 1, 3 },
                        { 4, 1, 5, 2, 6, 3, 0, 4 },
                        { 4, 2, 0, 5, 3, 1, 6, 5 },
                        { 4, 3, 2, 1, 0, 6, 5, 6 },
                        { 4, 4, 4, 4, 4, 4, 4, 0 },
                        { 4, 5, 6, 0, 1, 2, 3, 1 },
                        { 4, 6, 1, 3, 5, 0, 2, 2 },
                        { 5, 0, 2, 4, 6, 1, 3, 2 },
                        { 5, 1, 4, 0, 3, 6, 2, 3 },
                        { 5, 2, 6, 3, 0, 4, 1, 4 },
                        { 5, 3, 1, 6, 4, 2, 0, 5 },
                        { 5, 4, 3, 2, 1, 0, 6, 6 },
                        { 5, 5, 5, 5, 5, 5, 5, 0 },
                        { 5, 6, 0, 1, 2, 3, 4, 1 },
                        { 6, 0, 1, 2, 3, 4, 5, 1 },
                        { 6, 1, 3, 5, 0, 2, 4, 2 },
                        { 6, 2, 5, 1, 4, 0, 3, 3 },
                        { 6, 3, 0, 4, 1, 5, 2, 4 },
                        { 6, 4, 2, 0, 5, 3, 1, 5 },
                        { 6, 5, 4, 3, 2, 1, 0, 6 },
                        { 6, 6, 6, 6, 6, 6, 6, 0 }
                }
        );
    }

    @Value
    public static class LatinSquareParameterMessenger {
        List<Parameter<? extends Comparable<?>>> parameters;
        int[][] testMatrix;
    }

    private static class Parameters {
        static Parameter<Number<DoubleNumber>> CELL_RATIO = new SimplerAddParameter<DoubleNumber>( DoubleNumber.of( 0.1 ), DoubleNumber.of( 1.0 ), DoubleNumber.of( 0.9 / SIZE ) ) {
            @Override
            public PreprocessingInput changeInput( PreprocessingInput input, Number<DoubleNumber> currentValue ) {
                return input.withCellRatio( currentValue.identity().getValue() );
            }
        };
        static Parameter<Number<IntNumber>> CELL_SIZE = new SimplerMultiplyParameter<IntNumber>( IntNumber.of( 10 ), IntNumber.of( (long) ( 10 * Math.pow( 4, SIZE - 1 ) ) ), IntNumber.of( 4 ) ) {
            @Override
            public PreprocessingInput changeInput( PreprocessingInput input, Number<IntNumber> currentValue ) {
                long val = currentValue.identity().getValue();
                if ( val < 0 || Integer.MAX_VALUE < val ) {
                    return input.withCellSize( Integer.MAX_VALUE );
                }
                return input.withCellSize( (int) val );
            }
        };
    }
}
