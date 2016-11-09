/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester;

import cz.blahami2.routingsaratester.generator.controller.DataSetController;
import cz.blahami2.routingsaratester.common.data.FileInputDAO;
import cz.blahami2.routingsaratester.common.data.InputDAO;
import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.comparator.controller.ComparatorController;
import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.blahami2.routingsaratester.testrunner.logic.TestRunner;
import cz.blahami2.routingsaratester.plot.model.GralPlot;
import cz.blahami2.routingsaratester.testrunner.model.TestOptions;
import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.blahami2.utils.table.data.CsvTableExporter;
import cz.blahami2.utils.table.data.TableExporter;
import cz.blahami2.utils.table.model.DoubleListTableBuilder;
import cz.blahami2.utils.table.model.Table;
import cz.blahami2.utils.table.model.TableBuilder;
import cz.certicon.routing.algorithm.DijkstraAlgorithm;
import cz.certicon.routing.algorithm.RoutingAlgorithm;
import cz.certicon.routing.algorithm.sara.optimized.MultilevelDijkstra;
import cz.certicon.routing.algorithm.sara.optimized.data.OptimizedGraphDAO;
import cz.certicon.routing.algorithm.sara.optimized.model.OptimizedGraph;
import cz.certicon.routing.algorithm.sara.optimized.model.Route;
import cz.certicon.routing.algorithm.sara.preprocessing.BottomUpPreprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.algorithm.sara.preprocessing.Preprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.assembly.Assembler;
import cz.certicon.routing.algorithm.sara.preprocessing.assembly.GreedyAssembler;
import cz.certicon.routing.algorithm.sara.preprocessing.filtering.Filter;
import cz.certicon.routing.algorithm.sara.preprocessing.filtering.NaturalCutsFilter;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayBuilder;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayCreator;
import cz.certicon.routing.algorithm.sara.query.mld.MLDRecursiveRouteUnpacker;
import cz.certicon.routing.algorithm.sara.query.mld.MultilevelDijkstraAlgorithm;
import cz.certicon.routing.data.GraphDAO;
import cz.certicon.routing.data.GraphDataUpdater;
import cz.certicon.routing.data.GraphDeleteMessenger;
import cz.certicon.routing.data.SqliteGraphDAO;
import cz.certicon.routing.data.SqliteGraphDataDAO;
import cz.certicon.routing.data.SqliteGraphDataUpdater;
import cz.certicon.routing.data.basic.DataDestination;
import cz.certicon.routing.data.basic.DataSource;
import cz.certicon.routing.data.basic.FileDataDestination;
import cz.certicon.routing.data.basic.FileDataSource;
import cz.certicon.routing.data.processor.GraphComponentSearcher;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.graph.SaraEdge;
import cz.certicon.routing.model.graph.SaraGraph;
import cz.certicon.routing.model.graph.SaraNode;
import cz.certicon.routing.model.graph.preprocessing.ContractGraph;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.DisplayUtils;
import cz.certicon.routing.utils.RandomUtils;
import cz.certicon.routing.utils.collections.Iterator;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.progress.SimpleProgressListener;
import cz.certicon.routing.view.DebugViewer;
import cz.certicon.routing.view.JxDebugViewer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import java8.util.Optional;
import javax.swing.JFrame;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class Main {

    private static final PreprocessingInput DEFAULT_OPTIONS = new PreprocessingInput( 10000, 1, 0.1, 0.03, 0.6, 200, 3 );

    /**
     * @param args the command line arguments
     * @throws java.io.IOException coz i can
     */
    public static void main( String[] args ) throws IOException {
        Main main = new Main();
//        main.run();
//        main.test();
//        main.reduce();
//        main.testPlot();
//        main.testVisualiser();
//        main.generate();
        main.compareDijkstras();
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

    /*
    Cell size: 1000; 50000; *2
    Cell ratio: 0.1; 1; +0.1
    Core ratio: 0.1; 1; +0.1
    Low interval probability: 0.01; 0.5; *2
    Low interval <0,?>: 0.1; 0.9; +0.1
    Number of assembly runs: 1; 1000; +50
Default
    Cell size: 10000
    Cell ratio: 1
    Core ratio: 0.1
    Low interval probability: 0.03
    Low interval <0,?>: 0.6
    Number of assembly runs: 1000
     */
    public void run() throws IOException {
        TableBuilder<String> tableBuilder = new DoubleListTableBuilder<>();
        tableBuilder.setHeaders( Arrays.asList( "cell size", "cell ratio", "core ratio", "low interval prob", "low interval lim", "#assembly runs", "#cells", "min cell", "max cell", "median cell", "avg cell", "#cut edges", "filtering[ms]", "assembly[ms]", "length[ms]", "time[ms]" ) );
        GraphDAO graphDAO = new SqliteGraphDAO( loadProperties() );
        Graph graph = graphDAO.loadGraph();
        System.out.println( "Testing cell size..." );
        for ( int cellSize = 100; cellSize < 10000; cellSize *= 2 ) {
            PreprocessingInput options = DEFAULT_OPTIONS.withCellSize( cellSize );
            TestRunner runner = new TestRunner( graph, options );
            TestResult result = runner.runForResult();
            tableBuilder.addRow( toList( options, result ) );
        }
        Table<String> table = tableBuilder.build();
        TableExporter exporter = new CsvTableExporter( CsvTableExporter.Delimiter.SEMICOLON );
        exporter.export( new File( "testing_result.csv" ), table, str -> str );

    }

    public void test() throws IOException {
        GraphDAO graphDAO = new SqliteGraphDAO( loadProperties() );
//        PreprocessingInput input = DEFAULT_OPTIONS.withCellSize( 100 ).withNumberOfAssemblyRuns( 100 ).withNumberOfLayers( 1 );
        RandomUtils.setSeed( 123 );
        PreprocessingInput input = DEFAULT_OPTIONS
                .withCellSize( 20 )
                .withCellRatio( 1 )
                .withCoreRatio( 0.1 )
                .withLowIntervalProbability( 0.03 )
                .withLowIntervalLimit( 0.6 )
                .withNumberOfAssemblyRuns( 1 ) // 100
                .withNumberOfLayers( 1 ); // 5
        Graph graph = graphDAO.loadGraph();
        TestResult.TestResultBuilder builder = TestResult.builder();
//        System.out.println( "Filtering..." );
        Preprocessor preprocessor = new BottomUpPreprocessor();
        System.out.println( "Preprocessing..." );
        SaraGraph saraGraph = preprocessor.preprocess( graph, input, new IdSupplier( 0 ), new SimpleProgressListener( 10 ) {
            @Override
            public void onProgressUpdate( double d ) {
                System.out.printf( "Done: %d %%\n", (int) ( d * 100 ) );
            }
        } );
        System.out.println( "Saving..." );
        graphDAO.saveGraph( saraGraph );
        TimeLogger.setTimeUnits( TimeUnits.MILLISECONDS );
        int divisor = input.getNumberOfAssemblyRuns() * input.getNumberOfLayers();
        System.out.println( "Filtering time = " + TimeLogger.getTimeMeasurement( TimeLogger.Event.FILTERING ).getTime().toString() );
        System.out.println( "Assembly time = " + TimeLogger.getTimeMeasurement( TimeLogger.Event.ASSEMBLING ).getTime().toString() );
        System.out.println( "Assembly time/per unit = " + TimeLogger.getTimeMeasurement( TimeLogger.Event.ASSEMBLING ).getTime().divide( divisor ).toString() );
//        System.out.println( "#cutedges = " + (int) StreamSupport.stream( saraGraph.getEdges().spliterator(), true )
//                .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
//                .count() );
//        saraGraph = graphDAO.loadSaraGraph();
//        System.out.println( "#cutedges = " + (int) StreamSupport.stream( saraGraph.getEdges().spliterator(), true )
//                .filter( edge -> !edge.getSource().getParent().equals( edge.getTarget().getParent() ) )
//                .count() );
        DisplayUtils.display( saraGraph );
    }

    public void reduce() throws IOException {
        Properties properties = loadProperties();
        GraphDAO graphDAO = new SqliteGraphDAO( properties );
        System.out.println( "Loading graph..." );
        Graph graph = graphDAO.loadGraph();
        System.out.println( "Graph loaded. Searching for isolated areas..." );
        GraphDataUpdater dataUpdater = new SqliteGraphDataUpdater( properties );
        GraphDeleteMessenger isolatedAreas = new GraphComponentSearcher().findAllButLargest( graph );
        System.out.println( "Isolated areas found. Deleting..." );
        dataUpdater.deleteIsolatedAreas( isolatedAreas );
        System.out.println( "Deleted." );

    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream( "spatialite.properties" );
        properties.load( in );
        in.close();
        return properties;
    }

    public void testPlot() throws IOException {

        TableBuilder<String> builder = new DoubleListTableBuilder<>();
        builder.setHeader( 0, "X" );
        builder.setHeader( 1, "JednaY" );
        builder.setHeader( 2, "DvaY" );
        builder.setCell( 0, 0, "A" );
        builder.setCell( 0, 1, "NulaJedna" );
        builder.setCell( 0, 2, "NulaDva" );
        builder.setCell( 1, 0, "AA" );
        builder.setCell( 1, 1, "JednaJedna" );
        builder.setCell( 1, 2, "JednaDva" );
        builder.setCell( 2, 0, "AAA" );
        builder.setCell( 2, 1, "DvaJedna" );
        builder.setCell( 2, 2, "DvaDva" );
        builder.setCell( 3, 0, "AAAA" );
        builder.setCell( 3, 1, "TriJedna" );
        builder.setCell( 3, 2, "TriDva" );
        builder.setCell( 4, 0, "AAAAA" );
        builder.setCell( 4, 1, "CtyriJedna" );
        builder.setCell( 4, 2, "CtyriDva" );
        Table<String> table = builder.build();
        System.out.println( "Table:" + table );
        System.out.println( "Table:" + table.toString( x -> Integer.toString( x.length() ) ) );
        Function<String, Double> mapper = x -> Double.valueOf( x.length() );
        JFrame frame = new JFrame( "Plot#1" );
//        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        GralPlot instance = new GralPlot( table, mapper );
        instance.display( frame );
        frame.setVisible( true );
        instance.export( new File( "graph.png" ) );
//        frame = new JFrame( "Plot#2" );
//        frame.setSize( 800, 600 );
//        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//        instance.setData( table, mapper, 0, 1 );
//        instance.display( frame );
//        frame.setVisible( true );
    }

    public void testVisualiser() throws IOException {
        Properties properties = loadProperties();
        GraphDAO graphDAO = new SqliteGraphDAO( properties );
        Graph graph = graphDAO.loadGraph();
        DebugViewer viewer = new JxDebugViewer( new SqliteGraphDataDAO( properties ), graph, 500 );
        Random rand = new Random();
        List<Long> addedEdges = new ArrayList<>();
        for ( int i = 0; i < 20; i++ ) {
            int cnt = rand.nextInt( graph.getEdgeCount() );
            Iterator edges = graph.getEdges();
            for ( int j = 0; j < cnt - 1; j++ ) {
                edges.next();
            }
            long edgeId = ( (Edge) edges.next() ).getId();
            viewer.displayEdge( edgeId );
            addedEdges.add( edgeId );
        }
        for ( int i = 0; i < 20; i++ ) {
            int cnt = rand.nextInt( graph.getNodesCount() );
            Iterator nodes = graph.getNodes();
            for ( int j = 0; j < cnt - 1; j++ ) {
                nodes.next();
            }
            viewer.displayNode( ( (Node) nodes.next() ).getId() );
        }
        for ( int i = 0; i < 10; i++ ) {
            viewer.closeEdge( addedEdges.get( i ) );
        }
    }

    private void generate() {
        DataSetController controller = new DataSetController(
                new FileInputDAO(),
                new FileDataDestination( new File( "dataset_prague_time.txt" ) ),
                1000,
                Distance.newInstance( 3000 ), // 40000
                Metric.TIME
        );
        controller.run();
    }

    private void compareDijkstras() throws IOException {
        InputDAO inputDAO = new FileInputDAO();
        Input input = inputDAO.loadInput( new FileDataSource( new File( "dataset_prague_length.txt" ) ) );
        ComparatorController controller = new ComparatorController( loadProperties(), input, new SaraRunner(), new SaraRunner() );
        controller.run();
    }

    private static class ObjRunner implements ComparatorController.Runner {

        Graph graph;

        @Override
        public void prepare( Properties connectionProperties ) throws IOException {
            GraphDAO dao = new SqliteGraphDAO( connectionProperties );
            graph = dao.loadGraph();
        }

        @Override
        public boolean run( Input input ) {
            final RoutingAlgorithm alg = new DijkstraAlgorithm();
            return input.stream().map( new Function<InputElement, Boolean>() {
                @Override
                public Boolean apply( InputElement x ) {
                    Optional<cz.certicon.routing.model.Route> route = alg.route( graph, Metric.LENGTH, graph.getNodeById( x.getSourceNodeId() ), graph.getNodeById( x.getTargetNodeId() ) );
                    java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                    if ( !route.isPresent() ) {
                        return false;
                    }
                    return route.get().getEdgeList().stream()
                            .mapToLong( e -> ( (Edge) e ).getId() )
                            .allMatch( ( e ) -> ( edgeIdIterator.hasNext() && e == edgeIdIterator.next() ) );
                }
            } ).allMatch( x -> x );
        }
    }

    private static class OptRunner implements ComparatorController.Runner {

        OptimizedGraph graph;

        @Override
        public void prepare( Properties connectionProperties ) throws IOException {
            OptimizedGraphDAO dao = new OptimizedGraphDAO( connectionProperties );
            graph = dao.loadGraph();
        }

        @Override
        public boolean run( Input input ) {
            final MultilevelDijkstra alg = new MultilevelDijkstra();
            return input.stream().map( ( InputElement x ) -> {
                Optional<Route> route = alg.route( graph, x.getSourceNodeId(), x.getTargetNodeId(), Metric.LENGTH );
                java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                if ( !route.isPresent() ) {
                    return false;
                }
                return Arrays.stream( route.get().getEdges() )
                        .allMatch( ( e ) -> ( edgeIdIterator.hasNext() && e == edgeIdIterator.next() ) );
            } ).allMatch( x -> x );
        }
    }

    private static class SaraRunner implements ComparatorController.Runner {

        SaraGraph graph;
        OverlayBuilder overlay;

        @Override
        public void prepare( Properties connectionProperties ) throws IOException {
            OverlayCreator creator = new OverlayCreator();
            OverlayCreator.SaraSetup setup = creator.getSetup();

            setup.setSpatialModulePath( connectionProperties.getProperty( "spatialite_path" ) );
            String dbUrl = connectionProperties.getProperty( "url" ).substring( "jdbc:sqlite:".length() );
            dbUrl = dbUrl.substring( 0, dbUrl.length() - ".sqlite".length() );
            int lastSlashIdx = dbUrl.lastIndexOf( "/" );
            String dbFolder = dbUrl.substring( 0, lastSlashIdx + 1 );
            String dbName = dbUrl.substring( lastSlashIdx + 1 );
            setup.setDbFolder( dbFolder );
            setup.setRandomSeed( 123 );
            setup.setLayerCount( 5 );
            setup.setMaxCellSize( 20 );
            setup.setNumberOfAssemblyRuns( 1 );

            // D://prog-20-5.sqlite
            setup.setDbName( dbName );

            // punch and save
            //setup.runPunch = true;
            //no punch, load only
            setup.setRunPunch( true );

            overlay = creator.createBuilder();
            overlay.buildOverlays();

            graph = overlay.getGraph();
        }

        @Override
        public boolean run( Input input ) {
            MultilevelDijkstraAlgorithm alg = new MultilevelDijkstraAlgorithm();
            MLDRecursiveRouteUnpacker unpacker = new MLDRecursiveRouteUnpacker();
            IdSupplier counter = new IdSupplier( 0 );
            return input.stream().map( ( InputElement x ) -> {
                SaraNode source = graph.getNodeById( x.getSourceNodeId() );
                SaraNode target = graph.getNodeById( x.getTargetNodeId() );
                Optional<cz.certicon.routing.model.Route> route = alg.route( graph, overlay, Metric.LENGTH, source, target, unpacker );
                java.util.Iterator<Long> edgeIdIterator = x.getEdgeIds().iterator();
                if ( !route.isPresent() ) {
                    System.out.print( "Route not found for: " + x.getSourceNodeId() + " -> " + x.getTargetNodeId() );
                    System.out.print( " (" + ( source.getId() ) + " -> " + ( target.getId() ) + ")" );
                    System.out.println( ", not found " + counter.next() + "/" + input.size() );
                    return true;
                }
                boolean result = route.get().getEdgeList().stream()
                        .mapToLong( e -> ( (Edge) e ).getId() )
                        .allMatch( ( e ) -> ( edgeIdIterator.hasNext() && e == edgeIdIterator.next() ) );
                if ( !result ) {
                    System.out.println( "Routes do not match for id: " + x.getId() );
                    System.out.println( "Route ref: length = " + x.getLength() + ", time = " + x.getTime() + ", edges = " + x.getEdgeIds().stream().map( id -> id.toString() ).collect( Collectors.joining( " " ) ) );
                    System.out.println( "Route res: length = " + (int) route.get().calculateDistance( Metric.LENGTH ).getValue() + ", time = " + (int) route.get().calculateDistance( Metric.TIME ).getValue() + " s, edges = " + route.get().getEdgeList().stream().map( e -> ( (Edge) e ).getId() + "" ).collect( Collectors.joining( " " ) ) );
                }
                return result;
            } ).allMatch( x -> x );
        }

    }

}
