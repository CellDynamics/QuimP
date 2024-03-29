package com.github.celldynamics.quimp.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.Node;
import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.Snake;

/**
 * The Class QuimpDataConverterTest.
 *
 * @author p.baniukiewicz
 */
public class QuimpDataConverterTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpDataConverterTest.class.getName());

  /** The xc. */
  private double[] xc;

  /** The yc. */
  private double[] yc;

  /** The list. */
  private ArrayList<Point2d> list;

  /** The list. */
  private ArrayList<Point2D> listawt;

  /** The snake. */
  private Snake snake;

  /**
   * setUp.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    BOA_.qState = new BOAState(null);
    xc = new double[] { 3.0, 6.0, 6.0, 3.0 };
    yc = new double[] { 2.0, 2.0, 6.0, 6.0 };
    list = new ArrayList<Point2d>(4);
    list.add(new Point2d(3, 2));
    list.add(new Point2d(6, 2));
    list.add(new Point2d(6, 6));
    list.add(new Point2d(3, 6));
    snake = new Snake(list, 1);

    listawt = new ArrayList<>(4);
    listawt.add(new Point(3, 2));
    listawt.add(new Point(6, 2));
    listawt.add(new Point(6, 6));
    listawt.add(new Point(3, 6));

  }

  /**
   * testConversion_1.
   * 
   * <p>pre: list on input
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversion_1() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(list);
    assertArrayEquals(xc, dc.getX(), 1e-5);
    assertArrayEquals(yc, dc.getY(), 1e-5);
    assertEquals(list, dc.getList());
    Snake s = dc.getSnake(snake.getSnakeID());
    Node n = s.getHead();
    int i = 0;
    do {
      assertEquals(xc[i], n.getX(), 1e-5);
      assertEquals(yc[i], n.getY(), 1e-5);
      i++;
      n = n.getNext();
    } while (!n.isHead());

  }

  /**
   * testConversion_1a.
   * 
   * <p>pre: list awt on input
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversion_1a() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(listawt);
    assertArrayEquals(xc, dc.getX(), 1e-5);
    assertArrayEquals(yc, dc.getY(), 1e-5);
    assertEquals(list, dc.getList());
    Snake s = dc.getSnake(snake.getSnakeID());
    Node n = s.getHead();
    int i = 0;
    do {
      assertEquals(xc[i], n.getX(), 1e-5);
      assertEquals(yc[i], n.getY(), 1e-5);
      i++;
      n = n.getNext();
    } while (!n.isHead());

  }

  /**
   * testConversion_2.
   * 
   * <p>pre: XY on input
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversion_2() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(xc, yc);
    assertArrayEquals(xc, dc.getX(), 1e-5);
    assertArrayEquals(yc, dc.getY(), 1e-5);
    assertEquals(list, dc.getList());
    Snake s = dc.getSnake(snake.getSnakeID());
    Node n = s.getHead();
    int i = 0;
    do {
      assertEquals(xc[i], n.getX(), 1e-5);
      assertEquals(yc[i], n.getY(), 1e-5);
      i++;
      n = n.getNext();
    } while (!n.isHead());

  }

  /**
   * testConversion_3.
   * 
   * <p>pre: snake on input
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversion_3() throws Exception {
    Node n = snake.getHead();
    QuimpDataConverter dc = new QuimpDataConverter(snake);
    assertArrayEquals(xc, dc.getX(), 1e-5);
    assertArrayEquals(yc, dc.getY(), 1e-5);
    assertEquals(list, dc.getList());
    Snake s = dc.getSnake(snake.getSnakeID());
    n = s.getHead();
    int i = 0;
    if (n.getY() == 2) {
      do {
        assertEquals(xc[i], n.getX(), 1e-5);
        assertEquals(yc[i], n.getY(), 1e-5);
        i++;
        n = n.getNext();
      } while (!n.isHead());
    }

  }

  /**
   * testConversion_4.
   * 
   * <p>pre: snake null on input
   * 
   * @throws Exception Exception
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
   * testConversion_5.
   * 
   * <p>pre: list null on input
   * 
   * @throws Exception Exception
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
   * testConversion_6.
   * 
   * <p>pre: list o len on input
   * 
   * @throws Exception Exception
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

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter#getOutline()}.
   * 
   * <p>Check correct order of nodes.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetOutline() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(list);
    Snake s = dc.getSnake(0);
    Outline o = dc.getOutline();
    assertEquals(s.getHead().getX(), o.getHead().getX(), 1e-5);
    assertEquals(s.getHead().getY(), o.getHead().getY(), 1e-5);

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter#getListofIntPoints()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetListofIntPoints() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(xc, yc);
    List<Point2D> ret = dc.getListofIntPoints();
    QuimpDataConverter dc1 = new QuimpDataConverter(ret);
    double[] xl = dc1.getX();
    double[] yl = dc1.getY();
    assertArrayEquals(yc, yl, 1e-5);
    assertArrayEquals(xc, xl, 1e-5);
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter#getListofDoublePoints()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetListofDoublePoints() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(xc, yc);
    List<Point2D> ret = dc.getListofDoublePoints();
    QuimpDataConverter dc1 = new QuimpDataConverter(ret);
    double[] xl = dc1.getX();
    double[] yl = dc1.getY();
    assertArrayEquals(yc, yl, 1e-5);
    assertArrayEquals(xc, xl, 1e-5);
  }

}
