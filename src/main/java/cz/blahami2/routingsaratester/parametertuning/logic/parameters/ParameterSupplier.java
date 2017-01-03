package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import cz.blahami2.routingsaratester.parametertuning.model.*;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.model.values.Number;
import cz.certicon.routing.utils.EffectiveUtils;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public interface ParameterSupplier {


    default List<Parameter<? extends Comparable<?>>> getParameters( int paramererCount, int valuesPerParameterCount ) {
        List<Parameter<? extends Comparable<?>>> parameters = new ArrayList<>();
        if ( paramererCount > 0 ) {
            parameters.add( Parameters.cellRatio( valuesPerParameterCount ) );
        }
        if ( paramererCount > 1 ) {
            parameters.add( Parameters.coreRatio( valuesPerParameterCount ) );
        }
        if ( paramererCount > 2 ) {
            parameters.add( Parameters.cellSize( valuesPerParameterCount ) );
        }
        if ( paramererCount > 3 ) {
            parameters.add( Parameters.lowIntervalLimit( valuesPerParameterCount ) );
        }
        if ( paramererCount > 4 ) {
            parameters.add( Parameters.lowIntervalProbability( valuesPerParameterCount ) );
        }
        if ( paramererCount > 5 ) {
            parameters.add( Parameters.numberOfAssemblyRuns( valuesPerParameterCount ) );
        }
        if ( paramererCount > 6 ) {
            parameters.add( Parameters.numberOfLayers( valuesPerParameterCount ) );
        }
        if ( paramererCount > 7 ) {
            parameters.add( Parameters.cellSizePerLevel( valuesPerParameterCount ) );
        }
        return parameters;
    }

    ParameterMessenger compute( int parameterCount, int valuesPerParameterCount );

    @Value
    class ParameterMessenger {
        List<Parameter<? extends Comparable<?>>> parameters;
        int[][] testMatrix;

        public int[][] getTestMatrix( int rows ) {
            if ( rows >= testMatrix.length ) {
                return testMatrix;
            }
            int[][] matrix = new int[rows][parameters.size()];
            for ( int i = 0; i < rows; i++ ) {
                EffectiveUtils.copyArray( testMatrix[i], matrix[i] );
            }
            return matrix;
        }
    }

    class Parameters {
        static Parameter<Number<DoubleNumber>> cellRatio( int size ) {
            return doubleAdditionParameter( 0.1, 1.0, size, ( input, value ) -> input.withCellRatio( value ) );
        }

        static Parameter<Number<DoubleNumber>> coreRatio( int size ) {
            return doubleAdditionParameter( 0.1, 1.0, size, ( input, value ) -> input.withCoreRatio( value ) );
        }

        static Parameter<Number<IntNumber>> cellSize( int size ) {
            return intMultiplicationParameter( 10, 4, size, ( input, value ) -> input.withCellSize( value ) );
        }

        static Parameter<Number<DoubleNumber>> lowIntervalLimit( int size ) {
            return doubleAdditionParameter( 0.1, 0.9, size, ( input, value ) -> input.withLowIntervalLimit( value ) );
        }

        static Parameter<Number<DoubleNumber>> lowIntervalProbability( int size ) {
            return doubleMultiplicationParameter( 0.01, 0.9, size, ( input, value ) -> input.withLowIntervalProbability( value ) );
        }

        static Parameter<Number<IntNumber>> numberOfAssemblyRuns( int size ) {
            return intMultiplicationParameter( 1, 2, size, ( input, value ) -> input.withNumberOfAssemblyRuns( value ) );
        }

        static Parameter<Number<IntNumber>> numberOfLayers( int size ) {
            return intAdditionParameter( 1, size, size, ( input, value ) -> input.withNumberOfLayers( value ) );
        }

        /**
         * TODO change to custom cell size per each level parameter
         *
         * @param size amount of values this parameter should provide
         * @return cell size per level parameter
         */
        static Parameter<Number<IntNumber>> cellSizePerLevel( int size ) {
            return intAdditionParameter( 1, 100, size, ( input, value ) -> input ); // does nothing with the input
        }

        static Parameter<Number<IntNumber>> intMultiplicationParameter( int initValue, int multiplier, int size, BiFunction<PreprocessingInput, Integer, PreprocessingInput> changeInput ) {
            IntNumber iv = IntNumber.of( initValue );
            IntNumber fv = IntNumber.of( (long) ( initValue * Math.pow( multiplier, size - 1 ) ) );
            IntNumber mult = IntNumber.of( multiplier );
            return new SimplerMultiplyParameter<IntNumber>( iv, fv, mult ) {
                @Override
                public PreprocessingInput changeInput( PreprocessingInput input, Number<IntNumber> currentValue ) {
                    return intChangeInput( changeInput, input, currentValue );
                }
            };
        }

        static Parameter<Number<IntNumber>> intAdditionParameter( int initValue, int finalValue, int size, BiFunction<PreprocessingInput, Integer, PreprocessingInput> changeInput ) {
            IntNumber iv = IntNumber.of( initValue );
            IntNumber fv = IntNumber.of( finalValue );
            IntNumber inc = IntNumber.of( ( finalValue - initValue ) / ( size - 1 ) );
            return new SimplerAddParameter<IntNumber>( iv, fv, inc ) {
                @Override
                public PreprocessingInput changeInput( PreprocessingInput input, Number<IntNumber> currentValue ) {
                    return intChangeInput( changeInput, input, currentValue );
                }
            };
        }

        static PreprocessingInput intChangeInput( BiFunction<PreprocessingInput, Integer, PreprocessingInput> changeInput, PreprocessingInput input, Number<IntNumber> currentValue ) {
            long val = currentValue.identity().getValue();
            if ( val <= 0 || Integer.MAX_VALUE < val ) {
                return changeInput.apply( input, Integer.MAX_VALUE );
            }
            return changeInput.apply( input, (int) val );
        }

        static Parameter<Number<DoubleNumber>> doubleAdditionParameter( double initValue, double finalValue, int size, BiFunction<PreprocessingInput, Double, PreprocessingInput> changeInput ) {
            DoubleNumber iv = DoubleNumber.of( initValue );
            DoubleNumber fv = DoubleNumber.of( finalValue );
            DoubleNumber inc = DoubleNumber.of( ( finalValue - initValue ) / ( size - 1 ) );
            return new SimplerAddParameter<DoubleNumber>( iv, fv, inc ) {
                @Override
                public PreprocessingInput changeInput( PreprocessingInput input, Number<DoubleNumber> currentValue ) {
                    return changeInput.apply( input, currentValue.identity().getValue() );
                }
            };
        }

        static Parameter<Number<DoubleNumber>> doubleMultiplicationParameter( double initValue, double finalValue, int size, BiFunction<PreprocessingInput, Double, PreprocessingInput> changeInput ) {
            DoubleNumber iv = DoubleNumber.of( initValue );
            DoubleNumber fv = DoubleNumber.of( finalValue );
            DoubleNumber mult = DoubleNumber.of( Math.pow( finalValue / initValue, 1.0 / ( size - 1 ) ) );
            return new SimplerMultiplyParameter<DoubleNumber>( iv, fv, mult ) {
                @Override
                public PreprocessingInput changeInput( PreprocessingInput input, Number<DoubleNumber> currentValue ) {
                    return changeInput.apply( input, currentValue.identity().getValue() );
                }
            };
        }
    }
}
