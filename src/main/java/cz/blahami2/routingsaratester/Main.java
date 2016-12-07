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
import cz.blahami2.routingsaratester.comparator.controller.ComparatorController;
import cz.blahami2.routingsaratester.parametertuning.ParameterTuningController;
import cz.blahami2.routingsaratester.testrunner.logic.TestRunner;
import cz.blahami2.routingsaratester.plot.model.GralPlot;
import cz.blahami2.routingsaratester.testrunner.logic.runners.ObjectBasedDijkstraRunner;
import cz.blahami2.routingsaratester.testrunner.logic.runners.ObjectBasedSaraRunner;
import cz.blahami2.routingsaratester.testrunner.logic.runners.PrimitiveBasedDijkstraRunner;
import cz.blahami2.routingsaratester.testrunner.model.TestResult;
import cz.blahami2.utils.table.data.CsvTableExporter;
import cz.blahami2.utils.table.data.TableExporter;
import cz.blahami2.utils.table.model.DoubleListTableBuilder;
import cz.blahami2.utils.table.model.Table;
import cz.blahami2.utils.table.model.TableBuilder;
import cz.certicon.routing.algorithm.sara.preprocessing.BottomUpPreprocessor;
import cz.certicon.routing.algorithm.sara.preprocessing.PreprocessingInput;
import cz.certicon.routing.algorithm.sara.preprocessing.Preprocessor;
import cz.certicon.routing.algorithm.sara.query.mld.EdgeDistancePair;
import cz.certicon.routing.data.GraphDAO;
import cz.certicon.routing.data.GraphDataDao;
import cz.certicon.routing.data.GraphDataUpdater;
import cz.certicon.routing.data.GraphDeleteMessenger;
import cz.certicon.routing.data.SqliteGraphDAO;
import cz.certicon.routing.data.SqliteGraphDataDAO;
import cz.certicon.routing.data.SqliteGraphDataUpdater;
import cz.certicon.routing.data.basic.FileDataDestination;
import cz.certicon.routing.data.basic.FileDataSource;
import cz.certicon.routing.data.basic.database.SimpleDatabase;
import cz.certicon.routing.data.processor.GraphComponentSearcher;
import cz.certicon.routing.model.Identifiable;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Cell;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.model.graph.SaraEdge;
import cz.certicon.routing.model.graph.SaraGraph;
import cz.certicon.routing.model.graph.SaraNode;
import cz.certicon.routing.model.graph.TurnTable;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.DatabaseUtils;
import cz.certicon.routing.utils.DisplayUtils;
import cz.certicon.routing.utils.RandomUtils;
import cz.certicon.routing.utils.collections.Iterator;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.utils.progress.ProgressListener;
import cz.certicon.routing.utils.progress.SimpleProgressListener;
import cz.certicon.routing.view.DebugViewer;
import cz.certicon.routing.view.JxDebugViewer;
import cz.certicon.routing.view.jxmap.AbstractJxMapViewer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class Main {

    private static final PreprocessingInput DEFAULT_OPTIONS = new PreprocessingInput( 10000, 1, 0.1, 0.03, 0.6, 200, 3 ); // 10000, 1, 0.1, 0.03, 0.6, 200, 3 

    /**
     * @param args the command line arguments
     * @throws java.io.IOException coz I can
     * @throws java.sql.SQLException coz I can
     */
    public static void main( String[] args ) throws IOException, SQLException {
        Main main = new Main();
//        main.run();
//        main.test();
//        main.reduce();
//        main.testPlot();
        main.testVisualiser();
//        main.generate();
//        main.compareDijkstras();
//        main.czRegions();
//        main.displayWanderingNodes();
//        main.displayGraph();
    }

//    private List<String> toList( PreprocessingInput options, TestResult result ) {
//        List<String> list = new ArrayList<>();
//        list.add( Arrays.toString( options.getCellSizes() ) );
//        list.add( Double.toString( options.getCellRatio() ) );
//        list.add( Double.toString( options.getCoreRatio() ) );
//        list.add( Double.toString( options.getLowIntervalProbability() ) );
//        list.add( Double.toString( options.getLowIntervalLimit() ) );
//        list.add( Integer.toString( options.getNumberOfAssemblyRuns() ) );
//        list.add( Integer.toString( result.getNumberOfCells() ) );
//        list.add( Integer.toString( result.getMinimalCellSize() ) );
//        list.add( Integer.toString( result.getMaximalCellSize() ) );
//        list.add( Integer.toString( result.getMedianCellSize() ) );
//        list.add( Integer.toString( (int) result.getAverageCellSize() ) );
//        list.add( Integer.toString( result.getNumberOfCutEdges() ) );
//        list.add( Long.toString( result.getFilteringTime().getValue( TimeUnits.MILLISECONDS ) ) );
//        list.add( Long.toString( result.getAssemblyTime().getValue( TimeUnits.MILLISECONDS ) ) );
//        return list;
//    }

    
    public void run() throws IOException {
        new ParameterTuningController(loadProperties()).run();
    }

    /**
     * Test single preprocessing run with result presentation on the map
     *
     * @throws IOException exception
     */
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

    /**
     * Removes all but the largest component. Watch out, there might be some
     * paths inside this region which are connected to the overall graph in the
     * other region, this method would remove them as well.
     *
     * @throws IOException exception
     */
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

    /**
     * Test plot generating
     *
     * @throws IOException exception
     */
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

    /**
     * Test graph presentation
     *
     * @throws IOException exception
     */
    public void testVisualiser() throws IOException {
        Properties properties = loadProperties();
        GraphDAO graphDAO = new SqliteGraphDAO( properties );
        Graph graph = graphDAO.loadGraph();
        DebugViewer viewer = new JxDebugViewer( new SqliteGraphDataDAO( properties ), graph, 0 );
        ( (AbstractJxMapViewer) viewer ).setCentering( true );
        Scanner sc = new Scanner( System.in );
        while ( true ) {
            String type = sc.next();
            if ( type.equals( "q" ) ) {
                System.exit( 0 );
            }
            long id = sc.nextLong();
            switch ( type ) {
                case "n":
                    viewer.displayNode( id );
                    break;
                case "e":
                    viewer.displayEdge( id );
                    break;
                case "ce":
                    viewer.closeEdge( id );
                    break;
                case "rn":
                    viewer.removeNode( id );
                    break;
                case "re":
                    viewer.removeEdge( id );
                    break;
                default:
                    System.out.println( "Wrong type: '" + type + "'" );
                    break;
            }
        }
//        Random rand = new Random();
//        List<Long> addedEdges = new ArrayList<>();
//        for ( int i = 0; i < 20; i++ ) {
//            int cnt = rand.nextInt( graph.getEdgeCount() );
//            Iterator edges = graph.getEdges();
//            for ( int j = 0; j < cnt - 1; j++ ) {
//                edges.next();
//            }
//            long edgeId = ( (Edge) edges.next() ).getId();
//            viewer.displayEdge( edgeId );
//            addedEdges.add( edgeId );
//        }
//        for ( int i = 0; i < 20; i++ ) {
//            int cnt = rand.nextInt( graph.getNodesCount() );
//            Iterator nodes = graph.getNodes();
//            for ( int j = 0; j < cnt - 1; j++ ) {
//                nodes.next();
//            }
//            viewer.displayNode( ( (Node) nodes.next() ).getId() );
//        }
//        for ( int i = 0; i < 10; i++ ) {
//            viewer.closeEdge( addedEdges.get( i ) );
//        }
    }

    /**
     * Generates dataset of given size
     */
    private void generate() {
        DataSetController controller = new DataSetController(
                new FileInputDAO(),
                new FileDataDestination( new File( "dataset_cz_length.txt" ) ),
                1000,
                Distance.newInstance( 250000 ), // 40000
                Metric.LENGTH
        );
        controller.run();
    }

    /**
     * Compares two implementation of Dijkstra (in terms of speed). Validates
     * both via provided dataset. See ComparatorController.Runner
     * implementations below.
     *
     * @throws IOException exception
     */
    private void compareDijkstras() throws IOException {
        InputDAO inputDAO = new FileInputDAO();
        Input input = inputDAO.loadInput( new FileDataSource( new File( "dataset_cz_length.txt" ) ) );
        ComparatorController controller = new ComparatorController( loadProperties(), input, new ObjectBasedSaraRunner(), new ObjectBasedDijkstraRunner() );
        controller.run();
    }

    private void czRegions() throws IOException, SQLException {
        reduce();
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.SECONDS );
        time.start();
//        RandomUtils.setSeed( 123 );
        Properties properties = loadProperties();
        GraphDAO czGraphDao = new SqliteGraphDAO( properties, false );

//        if ( true ) {
//            SaraGraph g = czGraphDao.loadSaraGraph();
//            DisplayUtils.display( g );
//            return;
//        }
        String urlBase = "jdbc:sqlite:C:\\Users\\blaha\\Documents\\NetBeansProjects\\RoutingParser\\";
        String[] files = {
            "routing_sara_kraj_jihomoravsky.sqlite",
            "routing_sara_kraj_karlovarsky.sqlite",
            "routing_sara_kraj_kralovehradecky.sqlite",
            "routing_sara_kraj_liberecky.sqlite",
            "routing_sara_kraj_moravskoslezsky.sqlite",
            "routing_sara_kraj_olomoucky.sqlite",
            "routing_sara_kraj_pardubicky.sqlite",
            "routing_sara_kraj_plzensky.sqlite",
            "routing_sara_kraj_stredocesky.sqlite",
            "routing_sara_kraj_ustecky.sqlite",
            "routing_sara_kraj_vysocina.sqlite",
            "routing_sara_kraj_jihocesky.sqlite",
            "routing_sara_kraj_zlinsky.sqlite"
        };
        String[] names = {
            "Jihomoravský kraj",
            "Karlovarský kraj",
            "Královéhradecký kraj",
            "Liberecký kraj",
            "Moravskoslezský kraj",
            "Olomoucký kraj",
            "Pardubický kraj",
            "Plzeňský kraj",
            "Středočaský kraj",
            "Ústecký kraj",
            "Kraj Vysočina",
            "Jihočeský kraj",
            "Zlínský kraj"

        };
        PreprocessingInput input = DEFAULT_OPTIONS
                .withCellSize( 20 )
                .withCellRatio( 1 )
                .withCoreRatio( 0.1 )
                .withLowIntervalProbability( 0.03 )
                .withLowIntervalLimit( 0.6 )
                .withNumberOfAssemblyRuns( 100 ) // 100
                .withNumberOfLayers( 3 ); // 5

        IdSupplier idSupplier = new IdSupplier( 0 );
        SimpleDatabase database = SimpleDatabase.newSqliteDatabase( properties );
        if ( !DatabaseUtils.columnExists( database, "nodes", "cell_id" ) ) {
            database.write( "ALTER TABLE nodes ADD cell_id INTEGER DEFAULT(-1)" );
        } else {
            database.write( "UPDATE nodes SET cell_id = -1" );
        }
        if ( DatabaseUtils.tableExists( database, "cells" ) ) {
            database.write( "DROP TABLE IF EXISTS cells" );
            database.write( "DROP INDEX IF EXISTS `idx_id_cells`" );
        }
        database.write( "CREATE TABLE cells (id INTEGER, parent INTEGER)" );
        if ( DatabaseUtils.tableExists( database, "regions" ) ) {
            database.write( "DROP TABLE IF EXISTS regions" );
            database.write( "DROP INDEX IF EXISTS `idx_id_regions`" );
        }
        database.write( "CREATE TABLE regions (id INTEGER, cell_id INTEGER, name TEXT)" );

        List<Cell> regionCells = new ArrayList<>();
        Preprocessor preprocessor = new BottomUpPreprocessor();
        Set<Node> remainingNodes = new HashSet<>();
        for ( int i = 0; i < files.length; i++ ) {
            String file = files[i];
            String name = names[i];
            long id = 1L << i;
            String url = urlBase + file;
            Properties p = (Properties) properties.clone();
            p.setProperty( "url", url );
            GraphDAO dao = new SqliteGraphDAO( p );
            Graph graph = dao.loadGraph();
            System.out.println( "Preprocessing: " + file );
            SaraGraph saraGraph = preprocessor.preprocess( graph, input, idSupplier, new SimpleProgressListener( 10 ) {
                @Override
                public void onProgressUpdate( double d ) {
                    System.out.printf( "Done: %d %%\n", (int) ( d * 100 ) );
                }
            } );
            czGraphDao.saveGraph( saraGraph );

            Set<Cell> cells = new HashSet<>();
            for ( SaraNode node : saraGraph.getNodes() ) {
                Cell cell = node.getParent();
                while ( cell.hasParent() ) {
                    cell = cell.getParent();
                }
                cells.add( cell );
            }
            Cell regionCell = new Cell( idSupplier.next() );
            regionCells.add( regionCell );
            for ( Cell cell : cells ) {
                database.write( "UPDATE cells SET parent =  " + regionCell.getId() + " WHERE id = " + cell.getId() );
            }
            database.write( "INSERT INTO regions (id, cell_id, name) VALUES (" + id + ", " + regionCell.getId() + ", '" + name + "')" );
        }
        Cell czCell = new Cell( idSupplier.next() );
        int cnt = 0;
        for ( Cell regionCell : regionCells ) {
            regionCell.setParent( czCell );
            database.write( "INSERT INTO cells (id,parent) VALUES (" + regionCell.getId() + "," + regionCell.getParent().getId() + ")" );
            System.out.println( "region: cell_id = " + regionCell.getId() + ", name = " + files[cnt++] );
        }
        database.write( "INSERT INTO cells (id,parent) VALUES (" + czCell.getId() + ",-1)" );
        database.write( "CREATE UNIQUE INDEX `idx_id_cells` ON `cells` (`id` ASC)" );
        database.write( "CREATE UNIQUE INDEX `idx_id_regions` ON `regions` (`id` ASC)" );
        database.close();

        Cell[] artificialCells = new Cell[input.getNumberOfLayers() + 2]; // +2 for regions and CZ itself
        for ( int i = 0; i < artificialCells.length; i++ ) {
            artificialCells[i] = new Cell( idSupplier.next() );
            if ( i > 0 ) {
                artificialCells[i - 1].setParent( artificialCells[i] );
            }
        }
        System.out.println( "Preprocessing done in " + time.getCurrentTimeString() );
//        System.out.println( "Removing cellless nodes and edges" );
//        for ( Cell artificialCell : artificialCells ) {
//            database.write( "INSERT INTO cells (id,parent) VALUES (" + artificialCell.getId() + "," + ( artificialCell.hasParent() ? artificialCell.getParent().getId() : -1 ) + ")" );
//        }
        // delete cell-less nodes and edges
        System.out.println( "Deleting cell-less nodes and edges" );
        time.start();
        SaraGraph g = czGraphDao.loadSaraGraph();
        PreparedStatement nodeDeleteStatement = database.preparedStatement( "DELETE FROM nodes WHERE id = ?" );
        PreparedStatement edgeDeleteStatement = database.preparedStatement( "DELETE FROM edges WHERE source = ? OR target = ?" );
        int statementCounter = 0;
        int batchSize = 1000;
        Map<SaraNode, Integer> neighbors = new HashMap<>();
        long[] ttNodes = new long[100];
//        for ( SaraNode node : g.getNodes() ) {
//            TurnTable turnTable = node.getTurnTable();
//            if ( node.getParent() != null && node.getParent().getId() > 0 && ttNodes[turnTable.getSize()] == 0 ) {
//                boolean valid = true;
//                for ( int i = 0; i < turnTable.getSize() && valid; i++ ) {
//                    for ( int j = 0; j < turnTable.getSize() && valid; j++ ) {
//                        if ( i == j ) {
//                            valid = turnTable.getCost( i, j ).isInfinite();
//                        } else {
//                            valid = turnTable.getCost( i, j ).isEqualTo( Distance.newInstance( 0 ) );
//                        }
//                    }
//                }
//                if ( valid ) {
//                    ttNodes[turnTable.getSize()] = node.getId();
//                }
//            }
//        }
        for ( SaraNode node : g.getNodes() ) {
            if ( node.getParent() == null || node.getParent().getId() <= 0 ) {
                nodeDeleteStatement.setLong( 1, node.getId() );
                nodeDeleteStatement.addBatch();
                edgeDeleteStatement.setLong( 1, node.getId() );
                edgeDeleteStatement.setLong( 2, node.getId() );
                edgeDeleteStatement.addBatch();
                if ( ++statementCounter % batchSize == 0 ) {
                    nodeDeleteStatement.executeBatch();
                    edgeDeleteStatement.executeBatch();
                }
                for ( SaraEdge edge : node.getEdges() ) {
                    SaraNode other = edge.getOtherNode( node );
                    int count = ( neighbors.containsKey( other ) ) ? neighbors.get( other ) : 0;
                    neighbors.put( other, count + 1 );
                }
            }
        }
        java.util.Iterator< Map.Entry<SaraNode, Integer>> neighborsIterator = neighbors.entrySet().iterator();
        while ( neighborsIterator.hasNext() ) {
            Map.Entry<SaraNode, Integer> entry = neighborsIterator.next();
            if ( entry.getValue() == entry.getKey().getEdgesCount() ) {
                nodeDeleteStatement.setLong( 1, entry.getKey().getId() );
                nodeDeleteStatement.addBatch();
                if ( ++statementCounter % batchSize == 0 ) {
                    nodeDeleteStatement.executeBatch();
                }
                neighborsIterator.remove();
            }
        }
        nodeDeleteStatement.executeBatch();
        edgeDeleteStatement.executeBatch();
        database.close();
        System.out.println( "Deleting outsider nodes and edges done in " + time.getCurrentTimeString() );
        // update turn tables for neighbors
        System.out.println( "Updating turn-tables for " + neighbors.size() + " neighbors" );
        time.start();
        ProgressListener progressListener = new SimpleProgressListener( neighbors.size() ) {
            @Override
            public void onProgressUpdate( double done ) {
                System.out.printf( "Done %d\n", (int) ( done * neighbors.size() ) );
            }
        };
        progressListener.init( neighbors.size(), 1.0 );
        ResultSet rs = database.read( "SELECT max(id) AS id FROM turn_tables" );
        int maxTtId = 0;
        if ( rs.next() ) {
            maxTtId = rs.getInt( "id" );
        }
        for ( int i = 0; i < ttNodes.length; i++ ) {
            if ( ttNodes[i] != 0 ) {
                rs = database.read( "SELECT turn_table_id FROM nodes WHERE id = " + ttNodes[i] );
                if ( rs.next() ) {
                    ttNodes[i] = rs.getInt( "turn_table_id" );
                }
            }
        }
        for ( Map.Entry<SaraNode, Integer> entry : neighbors.entrySet() ) {
            SaraNode node = entry.getKey();
            List<Long> edges = new ArrayList<>();
            List<Long> sources = new ArrayList<>();
            List<Long> targets = new ArrayList<>();
            rs = database.read( "SELECT id, source, target FROM edges WHERE source = " + node.getId() + " OR target = " + node.getId() );
            while ( rs.next() ) {
                edges.add( rs.getLong( "id" ) );
                sources.add( rs.getLong( "source" ) );
                targets.add( rs.getLong( "target" ) );
            }
            int edgeCount = edges.size(); // node.getEdgesCount() - entry.getValue();
            if ( ttNodes[edgeCount] == 0 ) {
                database.write( "INSERT INTO turn_tables (id, size) VALUES (" + ++maxTtId + ", " + edgeCount + ")" );
                for ( int i = 0; i < edgeCount; i++ ) {
                    for ( int j = 0; j < edgeCount; j++ ) {
                        database.write( "INSERT INTO turn_table_values (turn_table_id, row_id, column_id, value) VALUES (" + maxTtId + ", " + i + ", " + j + ", "
                                + ( i == j ? Distance.newInfinityInstance().getValue() : Distance.newInstance( 0 ).getValue() ) + ")" );
                    }
                }
                ttNodes[edgeCount] = maxTtId;
                System.out.println( "Creating artificial turn-table of size " + edgeCount + " with id = " + maxTtId );
            }
            if ( edges.size() == edgeCount && ttNodes[edgeCount] != 0 ) {
                for ( int i = 0; i < edges.size(); i++ ) {
                    long e = edges.get( i );
                    long s = sources.get( i );
                    long t = targets.get( i );
                    if ( node.getId() == s ) {
                        database.write( "UPDATE edges SET source_pos = " + i + " WHERE id = " + e );
                    } else {
                        database.write( "UPDATE edges SET target_pos = " + i + " WHERE id = " + e );
                    }
                }
                database.write( "UPDATE nodes SET turn_table_id = " + ttNodes[edgeCount] + " WHERE id = " + node.getId() );
            } else if ( edgeCount == 0 ) {
                // delete the node
                database.write( "DELETE FROM nodes WHERE id = " + node.getId() );
            } else {
                throw new IllegalStateException( "unknown turntable size: " + edgeCount + ", " + edges.size() );
            }
            progressListener.nextStep();
        }
        database.close();
        System.out.println( "Updating turn-tables done in " + time.getCurrentTimeString() );
        // display
        System.out.println( "Displaying..." );
        g = czGraphDao.loadSaraGraph();
        DisplayUtils.display( g );
    }

    private void displayWanderingNodes() throws IOException {
        Properties properties = loadProperties();
        GraphDAO czGraphDao = new SqliteGraphDAO( properties, false );
        GraphDataDao dataDao = new SqliteGraphDataDAO( properties );
        SaraGraph g = czGraphDao.loadSaraGraph();
        JxDebugViewer viewer = new JxDebugViewer( dataDao, g, 0 );
        viewer.setCentering( false );
        for ( SaraNode node : g.getNodes() ) {
            if ( node.getParent() == null || node.getParent().getId() == 0L ) {
//                System.out.println( "node#" + node.getId() );
                viewer.displayNode( node.getId() );
            }
        }
    }

    private void displayGraph() throws IOException {
        Properties properties = loadProperties();
        GraphDAO czGraphDao = new SqliteGraphDAO( properties );
        SaraGraph g = czGraphDao.loadSaraGraph();
        Set<Cell> regions = new HashSet<>();
        for ( SaraNode n : g.getNodes() ) {
            int layer = 0;
            Cell cell = n.getParent();
            while ( ++layer < 3 ) {
                cell = cell.getParent();
            }
            regions.add( cell );
        }
        List<Cell> regionList = new ArrayList<>( regions );
        regionList.sort( Identifiable.Comparators.createIdComparator() );
        for ( Cell cell : regionList ) {
            System.out.println( "region id = " + cell.getId() );
        }
        System.out.println( "Displaying..." );
        DisplayUtils.display( g );
    }
}
