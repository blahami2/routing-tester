/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.model;

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
public class CounterTest {

    public CounterTest() {
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

    @Test
    public void testConstructorMinMax() {
        System.out.println( "constructor_2args" );
        Counter instance = new Counter( 0, 0 );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorMinMax_2() {
        System.out.println( "constructor_2args" );
        Counter instance = new Counter( 1, 0 );
    }

    @Test
    public void testConstructorInitMinMax() {
        System.out.println( "constructor_3args" );
        Counter instance = new Counter( 0, 0, 0 );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorInitMinMax_2() {
        System.out.println( "constructor_3args" );
        Counter instance = new Counter( 1, 0, 0 );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorInitMinMax_3() {
        System.out.println( "constructor_3args" );
        Counter instance = new Counter( -1, 0, 0 );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorInitMinMax_4() {
        System.out.println( "constructor_3args" );
        Counter instance = new Counter( 0, 1, 0 );
    }

//    /**
//     * Test of increment method, of class Counter.
//     */
//    @Test
//    public void testIncrement() {
//        System.out.println( "increment" );
//        Counter instance = new Counter();
//        int expResult = 0;
//        int result = instance.increment();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//    /**
//     * Test of decrement method, of class Counter.
//     */
//    @Test
//    public void testDecrement() {
//        System.out.println( "decrement" );
//        Counter instance = new Counter();
//        int expResult = 0;
//        int result = instance.decrement();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//    /**
//     * Test of getValue method, of class Counter.
//     */
//    @Test
//    public void testGetValue() {
//        System.out.println( "getValue" );
//        Counter instance = new Counter();
//        int expResult = 0;
//        int result = instance.getValue();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
}
