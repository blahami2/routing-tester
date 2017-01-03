package cz.blahami2.routingsaratester.parametertuning.view;

import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.model.values.TimeUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class PreprocessingInputViewer {
    public List<String> getHeaders() {
        return Arrays.asList(
                "cell size",
                "cell size init",
                "#layers",
                "cell ratio",
                "core ratio",
                "low interval prob",
                "low interval lim",
                "#assembly runs",
                "#cells",
                "#cells-total",
                "min cell",
                "min cell-total",
                "max cell",
                "max cell-total",
                "median cell",
                "avg cell",
                "avg cell-total",
                "#cut edges",
                "#cut edges-total",
                "filtering[ms]",
                "assembly[ms]",
                "overlay[ms]",
                "routing[ms]",
                "routings[ms]",
                "unpacking[ms]",
                "unpackings[ms]",
                "valid"
        );
    }

    public List<String> addRoutingHeaders( List<String> headers, int idx ) {
        ArrayList<String> newHeaders = new ArrayList<>( headers );
        newHeaders.add( "routing#" + idx + "[ms]" );
        newHeaders.add( "unpacking#" + idx + "[ms]" );
        return newHeaders;
    }

    public List<String> getData( PreprocessingInput preprocessingInput, TestResult result ) {
        List<String> list = new ArrayList<>();
        list.add( Arrays.toString( preprocessingInput.getCellSizes() ) );
        list.add( Integer.toString( preprocessingInput.getCellSizes()[0] ) );
        list.add( Integer.toString( preprocessingInput.getCellSizes().length ) );
        list.add( Double.toString( preprocessingInput.getCellRatio() ) );
        list.add( Double.toString( preprocessingInput.getCoreRatio() ) );
        list.add( Double.toString( preprocessingInput.getLowIntervalProbability() ) );
        list.add( Double.toString( preprocessingInput.getLowIntervalLimit() ) );
        list.add( Integer.toString( preprocessingInput.getNumberOfAssemblyRuns() ) );
        list.add( Arrays.toString( result.getNumberOfCells() ) );
        list.add( Integer.toString( result.getTotalNumberOfCells() ) );
        list.add( Arrays.toString( result.getMinimalCellSize() ) );
        list.add( Integer.toString( result.getTotalMinimalCellSize() ) );
        list.add( Arrays.toString( result.getMaximalCellSize() ) );
        list.add( Integer.toString( result.getTotalMaximalCellSize() ) );
        list.add( Arrays.toString( result.getMedianCellSize() ) );
        list.add( Arrays.toString( result.getAverageCellSize() ) );
        list.add( Double.toString( result.getTotalAverageCellSize() ) );
        list.add( Arrays.toString( result.getNumberOfCutEdges() ) );
        list.add( Integer.toString( result.getTotalNumberOfCutEdges() ) );
        list.add( Long.toString( result.getFilteringTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getAssemblyTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getOverlayTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getRoutingTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( "[" + Arrays.stream( result.getRoutingTimes() ).map( t -> Long.toString( t.getValue( TimeUnits.MILLISECONDS ) ) ).collect( Collectors.joining( ", " ) ) + "]" );
        list.add( Long.toString( result.getUnpackTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( "[" + Arrays.stream( result.getUnpackTimes() ).map( t -> Long.toString( t.getValue( TimeUnits.MILLISECONDS ) ) ).collect( Collectors.joining( ", " ) ) + "]" );
        list.add( Double.toString( result.getValidRatio() ) );
        return list;
    }
}
