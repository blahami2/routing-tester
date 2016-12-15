package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import cz.blahami2.routingsaratester.parametertuning.model.DoubleNumber;
import cz.blahami2.routingsaratester.parametertuning.model.Parameter;
import cz.certicon.routing.model.values.Number;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ParameterSupplierTest {

    @Test
    public void testDoubleMultiplicationParameter() throws Exception {
        List<Double> values= new ArrayList<>();
        Parameter<Number<DoubleNumber>> numbers = ParameterSupplier.Parameters.doubleMultiplicationParameter( 0.1, 0.8, 4, ( input, value ) -> input );
        for ( Number<DoubleNumber> number : numbers ) {
            values.add( number.identity().getValue() );
        }
        List<Double> expected = Arrays.asList(0.1, 0.2, 0.4, 0.8);
        assertEquals( expected, values );
    }


}