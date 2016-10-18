/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.data;

import cz.blahami2.routingsaratester.common.data.FileInputDAO;
import cz.blahami2.routingsaratester.common.model.Input;
import cz.blahami2.routingsaratester.common.model.InputElement;
import cz.certicon.routing.data.basic.DataDestination;
import cz.certicon.routing.data.basic.DataSource;
import cz.certicon.routing.data.basic.FileDataDestination;
import cz.certicon.routing.data.basic.FileDataSource;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class FileInputDAOTest {

    File testFile;
    Input testInput;
    String stringRepresentation;

    public FileInputDAOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testFile = new File( "FileInputDAOTest.test" );
        System.out.println( "creating file: " + testFile.getAbsolutePath() );
        stringRepresentation = "1 2 3 4 5 6 7 8 9\n11 12 13 14 15 16 17 18 19\n";
        Input.Builder builder = Input.builder();
        builder.add( new InputElement( 1, 2, 3, new Length( LengthUnits.METERS, 4 ), new Time( TimeUnits.SECONDS, 5 ), Arrays.asList( 6L, 7L, 8L, 9L ) ) );
        builder.add( new InputElement( 11, 12, 13, new Length( LengthUnits.METERS, 14 ), new Time( TimeUnits.SECONDS, 15 ), Arrays.asList( 16L, 17L, 18L, 19L ) ) );
        testInput = builder.build();
    }

    @After
    public void tearDown() {
        System.out.println( "deleting file: " + testFile.getAbsolutePath() );
        boolean res = testFile.delete();
        System.out.println( "with result: " + res );
    }

    /**
     * Test of loadInput method, of class FileInputDAO.
     */
    @Test
    public void testLoadInput() throws Exception {
        System.out.println( "loadInput" );
        try ( FileWriter writer = new FileWriter( testFile ) ) {
            writer.append( stringRepresentation ).flush();
        }
        DataSource source = new FileDataSource( testFile );
        FileInputDAO instance = new FileInputDAO();
        Input input = instance.loadInput( source );
        assertEquals( toString( testInput ), toString( input ) );
    }

    /**
     * Test of saveInput method, of class FileInputDAO.
     */
    @Test
    public void testSaveInput() throws Exception {
        System.out.println( "saveInput" );
        try ( FileWriter writer = new FileWriter( testFile ) ) {
            writer.append( "" ).flush();
        }
        DataDestination destination = new FileDataDestination( testFile );
        FileInputDAO instance = new FileInputDAO();
        instance.saveInput( destination, testInput );
        Input actual = instance.loadInput( new FileDataSource( testFile ) );
        assertEquals( toString( testInput ), toString( actual ) );
    }

    private String toString( Input input ) {
        return "{"
                + StreamSupport.stream( input.spliterator(), false )
                .sorted( Comparator.comparing( InputElement::getId ) )
                .map( e -> "{" + e.getId() + " "
                        + e.getSourceNodeId() + " "
                        + e.getTargetNodeId() + " "
                        + e.getLength().getValue( LengthUnits.METERS ) + " "
                        + e.getTime().getValue( TimeUnits.SECONDS ) + " "
                        + e.getEdgeIds().stream().map( x -> x.toString() ).collect( Collectors.joining( " " ) ) + "}" )
                .collect( Collectors.joining( "," ) )
                + "}";
    }

}
