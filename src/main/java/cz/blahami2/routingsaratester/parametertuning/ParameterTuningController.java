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
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
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
        PreprocessingInputViewer preprocessingInputViewer = new PreprocessingInputViewer();
        try {
            // prepare table
            TableBuilder<String> tableBuilder = new DoubleListTableBuilder<>();
            tableBuilder.setHeaders( preprocessingInputViewer.getHeaders() );
            // load graph
            GraphDAO graphDAO = new SqliteGraphDAO( properties );
            // foreach parameter test
            for ( PreprocessingInput preprocessingInput : strategy.preprocessingInputIterable() ) {
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
                        // add result to table
                    }
                    tableBuilder.addRow( preprocessingInputViewer.getData( preprocessingInput, aggregateTestResult.divide( numberOfRuns ) ) );
//                } catch ( NullPointerException ex ) {
//                    System.out.println( "Input has failed." );
//                    ex.printStackTrace();
//                }
            }
            // export results
            Table<String> table = tableBuilder.build();
            TableExporter exporter = new CsvTableExporter( CsvTableExporter.Delimiter.SEMICOLON );
            exporter.export( resultFileSupplier.get(), table, str -> str );
        } catch ( IOException ex ) {
            Logger.getLogger( IterativeParameterTuningStrategy.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }
}
