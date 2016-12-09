package cz.blahami2.routingsaratester.parametertuning;

import cz.blahami2.routingsaratester.parametertuning.model.Parameter;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class LatinSquareParameterTuningStrategyTest {
    @Test
    public void preprocessingInputIterator() throws Exception {
        Parameter<Integer> intParam = new Parameter<Integer>() {

            @Override
            public PreprocessingInput changeInput( PreprocessingInput input, Integer currentValue ) {
                return input.withNumberOfLayers( currentValue );
            }

            @Override
            public Integer initialValue() {
                return 1;
            }

            @Override
            public Integer finalValue() {
                return 4;
            }

            @Override
            public Integer increment( Integer currentValue ) {
                return currentValue + 1;
            }
        };
        Parameter<Double> doubleParam = new Parameter<Double>() {

            @Override
            public PreprocessingInput changeInput( PreprocessingInput input, Double currentValue ) {
                return input.withCellRatio( currentValue );
            }

            @Override
            public Double initialValue() {
                return Double.valueOf( 0.1 );
            }

            @Override
            public Double finalValue() {
                return Double.valueOf( 0.8 );
            }

            @Override
            public Double increment( Double currentValue ) {
                return currentValue + 0.2;
            }
        };
        LatinSquareParameterTuningStrategy instance = new LatinSquareParameterTuningStrategy( Arrays.asList( intParam, doubleParam ), new int[][]{ { 3, 1 }, { 0, 2 }, { 2, 0 }, { 1, 3 } } );
        int[] intRes = new int[]{ 4, 1, 3, 2 };
        double[] doubleRes = new double[]{ 0.3, 0.5, 0.1, 0.7 };
        int cnt = 0;
        for ( PreprocessingInput preprocessingInput : instance.preprocessingInputIterable() ) {
            assertThat( preprocessingInput.getNumberOfLayers(), equalTo( intRes[cnt] ) );
            assertEquals( doubleRes[cnt], preprocessingInput.getCellRatio(), 10E-6 );
            cnt++;
        }
    }

}