package cz.blahami2.routingsaratester.parametertuning.logic.parameters;

import java.util.Arrays;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class LatinSquareParameterSupplier implements ParameterSupplier {

    @Override
    public ParameterMessenger compute( int parameterCount, int valuesPerParameterCount ) {
        if ( parameterCount == 3 && valuesPerParameterCount == 2 ) {
            return new ParameterMessenger(
                    Arrays.asList(
                            Parameters.cellSize( valuesPerParameterCount ),
                            Parameters.cellRatio( valuesPerParameterCount ),
                            Parameters.coreRatio( valuesPerParameterCount )
                    ),
                    new int[][]{
                            { 0, 0, 0 },
                            { 0, 1, 1 },
                            { 1, 0, 1 },
                            { 1, 1, 0 }
                    }
            );
        } else if ( parameterCount == 8 && valuesPerParameterCount == 7 ) {
            return new ParameterMessenger(
                    Arrays.asList(
                            Parameters.cellRatio( valuesPerParameterCount ),
                            Parameters.coreRatio( valuesPerParameterCount ),
                            Parameters.cellSize( valuesPerParameterCount ),
                            Parameters.lowIntervalLimit( valuesPerParameterCount ),
                            Parameters.lowIntervalProbability( valuesPerParameterCount ),
                            Parameters.numberOfAssemblyRuns( valuesPerParameterCount ),
                            Parameters.numberOfLayers( valuesPerParameterCount ),
                            Parameters.cellSizePerLevel( valuesPerParameterCount )
                    ),
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
        } else {
            throw new IllegalArgumentException( "Unknown combination: parameterCount = " + parameterCount + " and valuesPerParameterCount = " + valuesPerParameterCount );
        }

    }

}
