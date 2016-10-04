/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.model;

import cz.blahami2.utils.table.model.DoubleListTableBuilder;
import cz.blahami2.utils.table.model.Table;
import cz.blahami2.utils.table.model.TableBuilder;
import java.util.Scanner;
import java.util.function.Function;
import javax.swing.JFrame;
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
public class GralPlotTest {

    private final Table<String> table;
    private final Function<String, Double> mapper;

    public GralPlotTest() {
        TableBuilder<String> builder = new DoubleListTableBuilder<>();
        builder.setCell( 0, 0, "Nula" );
        builder.setCell( 0, 1, "NulaJedna" );
        builder.setCell( 0, 2, "NulaDva" );
        builder.setCell( 1, 0, "Jedna" );
        builder.setCell( 1, 1, "JednaJedna" );
        builder.setCell( 1, 2, "JednaDva" );
        builder.setCell( 2, 0, "Dva" );
        builder.setCell( 2, 1, "DvaJedna" );
        builder.setCell( 2, 2, "DvaDva" );
        builder.setCell( 3, 0, "Tri" );
        builder.setCell( 3, 1, "TriJedna" );
        builder.setCell( 3, 2, "TriDva" );
        builder.setCell( 4, 0, "Ctyri" );
        builder.setCell( 4, 1, "CtyriJedna" );
        builder.setCell( 4, 2, "CtyriDva" );
        table = builder.build();
        mapper = x -> Double.valueOf( x.length() );
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setData method, of class GralPlot.
     */
    @Test
    public void testSetData_Table_Function() {
        System.out.println( "setData" );
        GralPlot instance = new GralPlot( table, mapper );
        instance.setData( table, mapper );
    }

    /**
     * Test of setData method, of class GralPlot.
     */
    @Test
    public void testSetData_5args() {
        System.out.println( "setData" );
        GralPlot instance = new GralPlot( table, mapper );
        instance.setData( table, mapper, 0, 1 );
    }

    /**
     * Test of display method, of class GralPlot.
     */
    @Test
    public void testDisplay() {
        System.out.println( "display" );
        GralPlot instance = new GralPlot( table, mapper );
        instance.setData( table, mapper );
        instance.display();
        instance.setData( table, mapper, 0, 1 );
        instance.display();
    }

    /**
     * Test of display method, of class GralPlot.
     */
    @Test
    public void testDisplay_JFrame() {
        System.out.println( "display" );
        JFrame frame = new JFrame( "Plot#1" );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        GralPlot instance = new GralPlot( table, mapper );
        instance.display( frame );
        frame.setVisible( true );
        frame = new JFrame( "Plot#2" );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        instance.setData( table, mapper, 0, 1 );
        instance.display( frame );
        frame.setVisible( true );
    }

}
