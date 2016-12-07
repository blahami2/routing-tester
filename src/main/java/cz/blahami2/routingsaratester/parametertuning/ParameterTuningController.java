/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.parametertuning;

import cz.blahami2.routingsaratester.plot.model.GralPlot;
import cz.blahami2.routingsaratester.testrunner.logic.TestRunner;
import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.blahami2.utils.table.data.CsvTableExporter;
import cz.blahami2.utils.table.data.TableExporter;
import cz.blahami2.utils.table.model.DoubleListTableBuilder;
import cz.blahami2.utils.table.model.Table;
import cz.blahami2.utils.table.model.TableBuilder;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.data.GraphDAO;
import cz.certicon.routing.data.SqliteGraphDAO;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.values.TimeUnits;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <html>
 * <table>
 * <tr><th>Parameter</th><th>start</th><th>end</th><th>increment</th></tr>
 * <tr><td>Strategy</td><td>bottom-up</td><td>top-down</td><td>nextStrategy()</td></tr>
 * <tr><td>Cell size:</td><td>1000</td><td>50000</td><td>*2</td></tr>
 * <tr><td>Cell ratio:</td><td>0.1</td><td>1</td><td>+0.1</td></tr>
 * <tr><td>Core ratio:</td><td>0.1</td><td>1; </td><td>+0.1</td></tr>
 * <tr><td>Low interval probability:</td><td>0.01</td><td>0.5</td><td>*2
 * </td></tr>
 * <tr><td>Low interval
 * &lt;0,?&gt;:</td><td>0.1</td><td>0.9</td><td>+0.1</td></tr>
 * <tr><td>Number of assembly runs:</td><td>1</td><td>1000</td><td>+50
 * </td></tr>
 * </table>
 * <table>
 * <tr><th>Parameter</th><th>Default value</th></tr>
 * <tr><td>Strategy:</td><td>bottom-up</td></tr>
 * <tr><td>Cell size:</td><td>10000</td></tr>
 * <tr><td>Cell ratio:</td><td>1</td></tr>
 * <tr><td>Core ratio:</td><td>0.1</td></tr>
 * <tr><td>Low interval probability:</td><td>0.03</td></tr>
 * <tr><td>Low interval &lt;0,?&gt;:</td><td>0.6</td></tr>
 * <tr><td>Number of assembly runs:</td><td>1000</td></tr>
 * </table>
 * </html>
 * Run parameter testing. Below is an example with cell size testing.
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ParameterTuningController implements Runnable {

    private static final PreprocessingInput DEFAULT_OPTIONS = new PreprocessingInput( 10000, 1, 0.1, 0.03, 0.6, 200, 3 ); // 10000, 1, 0.1, 0.03, 0.6, 200, 3 
    private final Properties properties;

    public ParameterTuningController( Properties properties ) {
        this.properties = properties;
    }

    @Override
    public void run() {
        try {
            TableBuilder<String> tableBuilder = new DoubleListTableBuilder<>();
            tableBuilder.setHeaders( Arrays.asList( "cell size", "cell ratio", "core ratio", "low interval prob", "low interval lim", "#assembly runs", "#cells", "min cell", "max cell", "median cell", "avg cell", "#cut edges", "filtering[ms]", "assembly[ms]", "length[ms]", "time[ms]" ) );
            GraphDAO graphDAO = new SqliteGraphDAO( properties );
            Graph graph = graphDAO.loadGraph();

            TableBuilder<Double> builder = new DoubleListTableBuilder<>();
            builder.setHeader( 0, "Cell size" ); // set x label
            builder.setHeader( 1, "#cut edges" ); // set y dataset #1 label
            int rowCounter = 0;

            System.out.println( "Testing cell size..." );
            for ( int cellSize = 10; cellSize < 1000; cellSize *= 2 ) {
                PreprocessingInput options = DEFAULT_OPTIONS.withCellSize( cellSize );
                TestRunner runner = new TestRunner( graph, options );
                TestResult result = runner.runForResult();
                tableBuilder.addRow( toList( options, result ) );

                builder.setCell( rowCounter, 0, (double) cellSize ); // set x cellSize
                builder.setCell( rowCounter, 1, (double) result.getNumberOfCutEdges() ); // set number of cut edges for the given cell size
                rowCounter++;
            }
            Table<String> table = tableBuilder.build();
            TableExporter exporter = new CsvTableExporter( CsvTableExporter.Delimiter.SEMICOLON );
            exporter.export( new File( "testing_result.csv" ), table, str -> str );

            // Create plot
            GralPlot plot = new GralPlot( builder.build(), Function.identity() ); // create new instance with the table and a mapper from Double to Double (see TableBuilder declaration)
            plot.export( new File( "graph.png" ) ); // export image into graph.png
        } catch ( IOException ex ) {
            Logger.getLogger( ParameterTuningController.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private List<String> toList( PreprocessingInput options, TestResult result ) {
        List<String> list = new ArrayList<>();
        list.add( Arrays.toString( options.getCellSizes() ) );
        list.add( Double.toString( options.getCellRatio() ) );
        list.add( Double.toString( options.getCoreRatio() ) );
        list.add( Double.toString( options.getLowIntervalProbability() ) );
        list.add( Double.toString( options.getLowIntervalLimit() ) );
        list.add( Integer.toString( options.getNumberOfAssemblyRuns() ) );
        list.add( Integer.toString( result.getNumberOfCells() ) );
        list.add( Integer.toString( result.getMinimalCellSize() ) );
        list.add( Integer.toString( result.getMaximalCellSize() ) );
        list.add( Integer.toString( result.getMedianCellSize() ) );
        list.add( Integer.toString( (int) result.getAverageCellSize() ) );
        list.add( Integer.toString( result.getNumberOfCutEdges() ) );
        list.add( Long.toString( result.getFilteringTime().getValue( TimeUnits.MILLISECONDS ) ) );
        list.add( Long.toString( result.getAssemblyTime().getValue( TimeUnits.MILLISECONDS ) ) );
        return list;
    }
}
