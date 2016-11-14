/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.comparator.controller;

import cz.blahami2.routingsaratester.common.model.Input;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ComparatorController {

    private final Runner firstRunner;
    private final Runner secondRunner;
    private final Properties properties;
    private final Input input;

    public ComparatorController( Properties connectionProperties, Input input, Runner firstRunner, Runner secondRunner ) {
        this.properties = connectionProperties;
        this.input = input;
        this.firstRunner = firstRunner;
        this.secondRunner = secondRunner;
    }

    public void run() throws IOException {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        System.out.println( "Preparing first..." );
        time.start();
        firstRunner.prepare( properties );
        time.stop();
        Time firstPrepareTime = time.getTime();
        System.out.println( "Done in " + firstPrepareTime.toString() );
        System.out.println( "Preparing second..." );
        time.start();
        secondRunner.prepare( properties );
        time.stop();
        Time secondPrepareTime = time.getTime();
        System.out.println( "Done in " + secondPrepareTime.toString() );
        System.out.println( "Warming up first..." );;
        firstRunner.run( input, TimeLogger.getTimeMeasurement( "Warmup1" ) );
        System.out.println( "Warming up second..." );
        secondRunner.run( input, TimeLogger.getTimeMeasurement( "Warmup2" ) );
        System.out.println( "Running first..." );
        boolean firstResult = firstRunner.run( input, TimeLogger.getTimeMeasurement( "Run1" ) );
        System.out.println( "Done in " + TimeLogger.getTimeMeasurement( "Run1" ).getTimeString() + " with result = " + firstResult );
        System.out.println( "Running second..." );
        boolean secondResult = secondRunner.run( input, TimeLogger.getTimeMeasurement( "Run2" ) );
        System.out.println( "Done in " + TimeLogger.getTimeMeasurement( "Run2" ).getTimeString() + " with result = " + secondResult );
    }

    public interface Runner {

        void prepare( Properties connectionProperties ) throws IOException;

        boolean run( Input input, TimeMeasurement routeTime );
    }
}
