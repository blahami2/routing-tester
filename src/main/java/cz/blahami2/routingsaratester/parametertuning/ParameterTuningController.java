package cz.blahami2.routingsaratester.parametertuning;

import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.parametertuning.logic.IterativeParameterTuningStrategy;
import cz.blahami2.routingsaratester.parametertuning.view.PreprocessingInputViewer;
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
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ParameterTuningController implements Runnable {

    private final Properties properties;
    private final Supplier<File> resultFileSupplier;
    private final List<Input> inputs;
    @Setter
    private int numberOfRuns = 1;
    @Setter
    private int skip = 0;

    @Setter
    private ParameterTuningStrategy strategy;

    public ParameterTuningController( Properties properties, Collection<Input> inputs, ParameterTuningStrategy strategy ) {
        this.properties = properties;
        this.inputs = new ArrayList<>( inputs );
        this.strategy = strategy;
        this.resultFileSupplier = () -> new File( "testing_result.csv" );
    }

    public ParameterTuningController( Properties properties, Collection<Input> inputs, ParameterTuningStrategy strategy, Supplier<File> resultFileSupplier ) {
        this.properties = properties;
        this.inputs = new ArrayList<>( inputs );
        this.strategy = strategy;
        this.resultFileSupplier = resultFileSupplier;
    }

    @Override
    public void run() {
        TimeMeasurement time = new TimeMeasurement( TimeUnits.NANOSECONDS );
        Set<TimeUnits> printedTimeUnits = EnumSet.allOf( TimeUnits.class );
        time.start();
        try {
            // load graph
            GraphDAO graphDAO = new SqliteGraphDAO( properties );
            int counter = 0;
            // foreach parameter test
            for ( PreprocessingInput preprocessingInput : strategy.preprocessingInputIterable() ) {
                if ( counter++ < skip ) {
                    continue;
                }
//                try {
                TestResult aggregateTestResult = null;
                for ( int i = 0; i < numberOfRuns; i++ ) {
                    Graph graph = graphDAO.loadGraph();
                    // perform partitioning and crete sara graph
                    // create overlay graph
                    // load test input
                    // test graph with MLD
                    // - gather statistics
                    // - gather time
                    TestRunner testRunner = new TestRunner( graph, preprocessingInput );
                    testRunner.addRoutingCriteria( inputs );
                    TestResult testResult = testRunner.runForResult();
                    if ( aggregateTestResult == null ) {
                        aggregateTestResult = testResult;
                    } else {
                        aggregateTestResult = aggregateTestResult.add( testResult );
                    }
                }
                // add result to table
                export( preprocessingInput, aggregateTestResult.divide( numberOfRuns ) );
                Logger.getLogger( getClass().getName() ).info( time.getCurrentTime().toString( printedTimeUnits ) );
//                } catch ( NullPointerException ex ) {
//                    System.out.println( "Input has failed." );
//                    ex.printStackTrace();
//                }
            }
        } catch ( IOException ex ) {
            Logger.getLogger( IterativeParameterTuningStrategy.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private void export( PreprocessingInput preprocessingInput, TestResult testResult ) throws IOException {
        PreprocessingInputViewer preprocessingInputViewer = new PreprocessingInputViewer();
        // prepare table
        TableBuilder<String> tableBuilder = new DoubleListTableBuilder<>();
        tableBuilder.setHeaders( preprocessingInputViewer.getHeaders() );
        tableBuilder.addRow( preprocessingInputViewer.getData( preprocessingInput, testResult ) );
        // export results
        Table<String> table = tableBuilder.build();
        TableExporter exporter = new CsvTableExporter( CsvTableExporter.Delimiter.SEMICOLON );
        exporter.export( resultFileSupplier.get(), table, str -> str, true );
    }
}
