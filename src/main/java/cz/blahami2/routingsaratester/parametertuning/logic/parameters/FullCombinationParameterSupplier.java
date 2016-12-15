package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import java.util.Arrays;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class FullCombinationParameterSupplier implements ParameterSupplier {
    @Override
    public ParameterMessenger compute( int parameterCount, int valuesPerParameterCount ) {
        int[][] matrix = new int[(int) Math.pow( valuesPerParameterCount, parameterCount )][parameterCount];
        fillColumn( matrix, 0, matrix.length, parameterCount, valuesPerParameterCount, 0 );
        return new ParameterMessenger(
                Arrays.asList( Parameters.cellSize( valuesPerParameterCount ), Parameters.cellRatio( valuesPerParameterCount ), Parameters.coreRatio( valuesPerParameterCount ) ),
                matrix
        );
    }

    void fillColumn( int[][] matrix, int start, int end, int parameterCount, int valueCount, int idx ) {
        if ( idx >= parameterCount ) {
            return;
        }
        int length = end - start;
        int seqlen = length / valueCount;
        for ( int val = 0; val < valueCount; val++ ) {
            int s = start + ( seqlen * val );
            int e = start + ( seqlen * ( val + 1 ) );
            for ( int i = s; i < e; i++ ) {
                matrix[i][idx] = val;
            }
            fillColumn( matrix, s, e, parameterCount, valueCount, idx + 1 );
        }
    }
}
