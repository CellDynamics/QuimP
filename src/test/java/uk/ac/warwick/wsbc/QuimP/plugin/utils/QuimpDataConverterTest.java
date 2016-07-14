/**
 * @file QuimpDataConverterTest.java
 * @date 11 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.BOA_;
import uk.ac.warwick.wsbc.QuimP.Node;
import uk.ac.warwick.wsbc.QuimP.Snake;

/**
 * @author p.baniukiewicz
 * @date 11 Apr 2016
 *
 */
public class QuimpDataConverterTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER =
            LogManager.getLogger(QuimpDataConverterTest.class.getName());

    private double X[], Y[];
    private ArrayList<Point2d> list;
    private Snake snake;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        BOA_.qState = new BOAState(null);
        X = new double[] { 3.0, 6.0, 6.0, 3.0 };
        Y = new double[] { 2.0, 2.0, 6.0, 6.0 };
        list = new ArrayList<Point2d>(4);
        list.add(new Point2d(3, 2));
        list.add(new Point2d(6, 2));
        list.add(new Point2d(6, 6));
        list.add(new Point2d(3, 6));
        snake = new Snake(list, 1);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * @test testConversion_1
     * @pre list on input
     * @throws Exception
     */
    @Test
    public void testConversion_1() throws Exception {
        QuimpDataConverter dc = new QuimpDataConverter(list);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        Node n = s.getHead();
        int i = 0;
        do {
            assertEquals(X[i], n.getX(), 1e-5);
            assertEquals(Y[i], n.getY(), 1e-5);
            i++;
            n = n.getNext();
        } while (!n.isHead());

    }

    /**
     * @test testConversion_2
     * @pre XY on input
     * @throws Exception
     */
    @Test
    public void testConversion_2() throws Exception {
        QuimpDataConverter dc = new QuimpDataConverter(X, Y);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        Node n = s.getHead();
        int i = 0;
        do {
            assertEquals(X[i], n.getX(), 1e-5);
            assertEquals(Y[i], n.getY(), 1e-5);
            i++;
            n = n.getNext();
        } while (!n.isHead());

    }

    /**
     * @test testConversion_3
     * @pre snake on input
     * @throws Exception
     */
    @Test
    public void testConversion_3() throws Exception {
        QuimpDataConverter dc = new QuimpDataConverter(snake);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        Node n = s.getHead();
        int i = 0;
        do {
            assertEquals(X[i], n.getX(), 1e-5);
            assertEquals(Y[i], n.getY(), 1e-5);
            i++;
            n = n.getNext();
        } while (!n.isHead());

    }

    /**
     * @test testConversion_4
     * @pre snake null on input
     * @throws Exception
     */
    @Test
    public void testConversion_4() throws Exception {
        Snake input = null;
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

    /**
     * @test testConversion_5
     * @pre list null on input
     * @throws Exception
     */
    @Test
    public void testConversion_5() throws Exception {
        ArrayList<Point2d> input = null;
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

    /**
     * @test testConversion_6
     * @pre list o len on input
     * @throws Exception
     */
    @Test
    public void testConversion_6() throws Exception {
        ArrayList<Point2d> input = new ArrayList<>();
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

}
