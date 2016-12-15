package cz.blahami2.routingsaratester.parametertuning.view;

import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.model.values.TimeUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class PreprocessingInputViewer {
    public List<String> getHeaders() {
        return Arrays.asList(
                "cell size",
                "cell ratio",
                "core ratio",
                "low interval prob",
                "low interval lim",
                "#assembly runs",
                "#cells",
                "min cell",
                "max cell",
                "median cell",
                "avg cell",
                "#cut edges",
                "filtering[ms]",
                "assembly[ms]",
                "routing[ms]",
                "unpacking[ms]",
                "valid"
                );
    }

    public List<String> getData( PreprocessingInput preprocessingInput, TestResult result ) {
        List<String> list = new ArrayList<>();
        list.add( Arrays.toString( preprocessingInput.getCellSizes() ) );
        list.add( Double.toString( preprocessingInput.getCellRatio() ) );
        list.add( Double.toString( preprocessingInput.getCoreRatio() ) );
        list.add( Double.toString( preprocessingInput.getLowIntervalProbability() ) );
        list.add( Double.toString( preprocessingInput.getLowIntervalLimit() ) );
        list.add( Integer.toString( preprocessingInput.getNumberOfAssemblyRuns() ) );
        list.add( Integer.toString( result.getNumberOfCells() ) );
        list.add( Integer.toString( result.getMinimalCellSize() ) );
        list.add( Integer.toString( result.getMaximalCellSize() ) );
        list.add( Integer.toString( result.getMedianCellSize() ) );
        list.add( Integer.toString( (int) result.getAverageCellSize() ) );
        list.add( Integer.toString( result.getNumberOfCutEdges() ) );
        list.add( Long.toString( result.getFilteringTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getAssemblyTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getRoutingTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getUnpackTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Double.toString( result.getValidRatio() ) );
        return list;
    }
}
