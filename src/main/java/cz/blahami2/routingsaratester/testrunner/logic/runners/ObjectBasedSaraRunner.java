/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.logic.runners;

import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.blahami2.routingsaratester.comparator.controller.ComparatorController;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayBuilder;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.OverlayCreator;
import cz.certicon.routing.algorithm.sara.preprocessing.overlay.ZeroNode;
import cz.certicon.routing.algorithm.sara.query.mld.MLDFullMemoryRouteUnpacker;
import cz.certicon.routing.algorithm.sara.query.mld.MLDRecursiveRouteUnpacker;
import cz.certicon.routing.algorithm.sara.query.mld.MultilevelDijkstraAlgorithm;
import cz.certicon.routing.model.basic.IdSupplier;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Metric;
import cz.certicon.routing.model.graph.SaraGraph;
import cz.certicon.routing.model.graph.SaraNode;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;
import java8.util.Optional;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ObjectBasedSaraRunner implements ComparatorController.Runner {

    private SaraGraph graph;
    private OverlayBuilder overlay;

    @Override
    public void prepare( Properties connectionProperties ) throws IOException {
        OverlayBuilder.keepShortcuts = true;
        OverlayCreator creator = new OverlayCreator();
        OverlayCreator.SaraSetup setup = creator.getSetup();

        setup.setSpatialModulePath( connectionProperties.getProperty( "spatialite_path" ) );
        String dbUrl = connectionProperties.getProperty( "url" ).substring( "jdbc:sqlite:".length() );
        dbUrl = dbUrl.substring( 0, dbUrl.length() - ".sqlite".length() );
        int lastSlashIdx = dbUrl.lastIndexOf( "/" );
        String dbFolder = dbUrl.substring( 0, lastSlashIdx + 1 );
        String dbName = dbUrl.substring( lastSlashIdx + 1 );
        setup.setDbFolder( dbFolder );
//        setup.setRandomSeed( 123 );
//        setup.setLayerCount( 5 );
//        setup.setMaxCellSize( 20 );
//        setup.setNumberOfAssemblyRuns( 1 );

        // D://prog-20-5.sqlite
        setup.setDbName( dbName );

        // punch and save
        //setup.runPunch = true;
        //no punch, load only
        setup.setRunPunch( false );

        overlay = creator.createBuilder();
        overlay.buildOverlays();

        graph = overlay.getSaraGraph();
    }

    @Override
    public boolean run( Input input, TimeMeasurement routeTime ) {
        IdSupplier counter = new IdSupplier( 0 );
        return input.stream().map( ( InputElement x ) -> {
            MultilevelDijkstraAlgorithm alg = new MultilevelDijkstraAlgorithm();
            MLDFullMemoryRouteUnpacker unpacker = new MLDFullMemoryRouteUnpacker();
            SaraNode source = graph.getNodeById( x.getSourceNodeId() );
            SaraNode target = graph.getNodeById( x.getTargetNodeId() );
            ZeroNode zeroSource = overlay.getZeroNode( source );
            ZeroNode zeroTarget = overlay.getZeroNode( target );
            routeTime.continue_();
            Optional<cz.certicon.routing.model.Route> route = alg.route( overlay, Metric.LENGTH, zeroSource, zeroTarget, unpacker );
            routeTime.pause();
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
