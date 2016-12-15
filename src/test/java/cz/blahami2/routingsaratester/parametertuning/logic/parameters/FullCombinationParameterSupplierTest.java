package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class FullCombinationParameterSupplierTest {
    @Test
    public void fillColumn() throws Exception {
        int[][] expected = new int[][]{
                { 0, 0, 0 },
                { 0, 0, 1 },
                { 0, 0, 2 },
                { 0, 1, 0 },
                { 0, 1, 1 },
                { 0, 1, 2 },
                { 0, 2, 0 },
                { 0, 2, 1 },
                { 0, 2, 2 },
                { 1, 0, 0 },
                { 1, 0, 1 },
                { 1, 0, 2 },
                { 1, 1, 0 },
                { 1, 1, 1 },
                { 1, 1, 2 },
                { 1, 2, 0 },
                { 1, 2, 1 },
                { 1, 2, 2 },
                { 2, 0, 0 },
                { 2, 0, 1 },
                { 2, 0, 2 },
                { 2, 1, 0 },
                { 2, 1, 1 },
                { 2, 1, 2 },
                { 2, 2, 0 },
                { 2, 2, 1 },
                { 2, 2, 2 }
        };
        int[][] actual = new int[27][3];
        new FullCombinationParameterSupplier().fillColumn( actual, 0, actual.length, 3, 3, 0 );
        assertThat( actual, equalTo( expected ) );
    }

}