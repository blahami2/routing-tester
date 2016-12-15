/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.controller;

import cz.blahami2.routingsaratester.common.data.InputDAO;
import cz.blahami2.routingsaratester.generator.logic.DataSetGenerator;
import cz.blahami2.routingsaratester.generator.logic.MetricDataSetGenerator;
import cz.blahami2.routingsaratester.generator.model.DataSetElement;
import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.certicon.routing.data.GraphDAO;
import cz.certicon.routing.data.GraphDataDao;
import cz.certicon.routing.data.SqliteGraphDAO;
import cz.certicon.routing.data.SqliteGraphDataDAO;
import cz.certicon.routing.data.basic.DataDestination;
import cz.certicon.routing.model.Route;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.values.Distance;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.view.DebugViewer;
import cz.certicon.routing.view.JxDebugViewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class DataSetController implements Runnable {

    private final int size;
    private final int granularity;
    private final Metric metric;
    private final InputDAO inputDao;
    private final DataDestination destination;
    private final DataSetGenerator dataSetGenerator;

    public DataSetController( InputDAO inputDao, DataDestination destination, int size, int granularity, Metric metric, DataSetGenerator dataSetGenerator ) {
        this.inputDao = inputDao;
        this.destination = destination;
        this.size = size;
        this.granularity = granularity;
        this.metric = metric;
        this.dataSetGenerator = dataSetGenerator;
    }

    @Override
    public void run() {
        try {
            Graph graph = loadGraph();
//            DebugViewer debugViewer = new JxDebugViewer( new SqliteGraphDataDAO( loadProperties() ), graph, 1000 );
//            debugViewer.setCentering( false );
//            List<DataSetElement> dataSet = dataSetGenerator.generateDataSet( size, granularity, metric, graph, debugViewer );
            List<DataSetElement> dataSet = dataSetGenerator.generateDataSet( size, granularity, metric, graph );
            Input.Builder builder = Input.builder();
            int elementCounter = 1;
            for ( DataSetElement dataSetElement : dataSet ) {
                Route route = dataSetElement.getRoute();
                Length length = new Length( LengthUnits.METERS, (long) route.calculateDistance( Metric.LENGTH ).getValue() );
                Time time = new Time( TimeUnits.SECONDS, (long) route.calculateDistance( Metric.TIME ).getValue() );
                List<Edge> edgeList = route.getEdgeList();
                List<Long> edgeIds = edgeList.stream().map( e -> ( (Edge) e ).getId() ).collect( Collectors.toList() );
                InputElement inputElement = new InputElement( elementCounter++,
                        dataSetElement.getSourceNode().getId(),
                        dataSetElement.getTargetNode().getId(),
                        length, time, edgeIds );
                builder.add( inputElement );
            }
            Input input = builder.build();
            inputDao.saveInput( destination, input );
        } catch ( IOException ex ) {
            Logger.getLogger( DataSetController.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private Graph loadGraph() throws IOException {
        GraphDAO graphDAO = new SqliteGraphDAO( loadProperties() );
        Graph graph = graphDAO.loadGraph();
        return graph;
    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try ( InputStream in = getClass().getClassLoader().getResourceAsStream( "spatialite.properties" ) ) {
            properties.load( in );
        }
        return properties;
    }
}
