package uk.ac.warwick.wsbc.quimp.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.BOAState;
import uk.ac.warwick.wsbc.quimp.BOA_;
import uk.ac.warwick.wsbc.quimp.Node;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.Snake;

/**
 * @author p.baniukiewicz
 *
 */
public class QuimpDataConverterTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpDataConverterTest.class.getName());

  private double[] xc;
  private double[] yc;
  private ArrayList<Point2d> list;
  private Snake snake;

  /**
   * setUp.
   * 
   * @throws java.lang.Exception Exception
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
   * {@link uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter#getOutline(int)}.
   * 
   * <p>Check correct order of nodes.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetOutline() throws Exception {
    QuimpDataConverter dc = new QuimpDataConverter(list);
    Snake s = dc.getSnake(0);
    Outline o = dc.getOutline(0);
    assertEquals(s.getHead().getX(), o.getHead().getX(), 1e-5);
    assertEquals(s.getHead().getY(), o.getHead().getY(), 1e-5);

  }

}
