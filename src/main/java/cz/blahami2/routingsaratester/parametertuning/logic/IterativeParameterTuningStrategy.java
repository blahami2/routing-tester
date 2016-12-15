/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.parametertuning.logic;

import cz.blahami2.routingsaratester.parametertuning.ParameterTuningStrategy;
import cz.blahami2.routingsaratester.parametertuning.model.Parameter;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;

import java.util.*;

/**
 * <html>
 * <table>
 * <tr><th>Parameter</th><th>start</th><th>end</th><th>increment</th></tr>
 * <tr><td>Strategy</td><td>bottom-up</td><td>top-down</td><td>nextStrategy()</td></tr>
 * <tr><td>Cell size:</td><td>1000</td><td>50000</td><td>*2</td></tr>
 * <tr><td>Cell ratio:</td><td>0.1</td><td>1</td><td>+0.1</td></tr>
 * <tr><td>Core ratio:</td><td>0.1</td><td>1; </td><td>+0.1</td></tr>
 * <tr><td>Low interval probability:</td><td>0.01</td><td>0.5</td><td>*2</td></tr>
 * <tr><td>Low interval &lt;0,?&gt;:</td><td>0.1</td><td>0.9</td><td>+0.1</td></tr>
 * <tr><td>Number of assembly runs:</td><td>1</td><td>1000</td><td>+50</td></tr>
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
public class IterativeParameterTuningStrategy implements ParameterTuningStrategy {

      private final PreprocessingInput defaultOptions;
    private final List<Parameter<? extends Comparable<?>>> parameters;

    public IterativeParameterTuningStrategy(PreprocessingInput defaultOptions, Collection<Parameter<? extends Comparable<?>>> parameters ) {
        this.defaultOptions = defaultOptions;
        this.parameters = new ArrayList<>( parameters );
    }

    @Override
    public Iterator<PreprocessingInput> preprocessingInputIterator() {
        return new Iterator<PreprocessingInput>() {
            Iterator<Parameter<? extends Comparable<?>>> parameterIterator = parameters.iterator();
            Iterator<PreprocessingInput> preprocessingInputIterator = null;

            @Override
            public boolean hasNext() {
                return parameterIterator.hasNext() ||
                        ( preprocessingInputIterator != null && preprocessingInputIterator.hasNext() );
            }

            @Override
            public PreprocessingInput next() {
                if ( !hasNext() ) {
                    throw new IllegalStateException( "No more elements available" );
                }
                if ( preprocessingInputIterator == null || !preprocessingInputIterator.hasNext() ) {
                    preprocessingInputIterator = parameterIterator.next().iterator( defaultOptions );
                }
                return preprocessingInputIterator.next();
            }
        };
    }


      /*
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
            Logger.getLogger( IterativeParameterTuningStrategy.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }*/
}
