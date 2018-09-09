package com.github.celldynamics.quimp.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;

/**
 * The Class BasicPolygonsTest.
 *
 * @author p.baniukiewicz
 */
public class BasicPolygonsTest {

  /** The points. */
  private ArrayList<Point2d> points;

  /** The point. */
  private ArrayList<Point2d> point;

  /** The points 2. */
  private ArrayList<Point2d> points2;

  /** The inpoints. */
  private ArrayList<Point2d> inpoints;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    points = new ArrayList<Point2d>();
    points.add(new Point2d(2, 2));
    points.add(new Point2d(9, 9));
    points.add(new Point2d(6, 4));
    points.add(new Point2d(12, 2));

    point = new ArrayList<Point2d>();
    point.add(new Point2d(2, 2));

    points2 = new ArrayList<Point2d>();
    points2.add(new Point2d(2, 2));
    points2.add(new Point2d(9, 9));

    inpoints = new ArrayList<Point2d>();
    inpoints.add(new Point2d(5, 4));
    inpoints.add(new Point2d(6, 5));
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.getPolyPerim(final List&lt;E&gt;).
   * 
   * <p>Pre: Four point polygon
   * 
   * <p>Post: Perimeter
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetPolyPerim() throws Exception {
    BasicPolygons b = new BasicPolygons();
    double p = b.getPolyPerim(points);
    assertEquals(32.055, p, 1e-5);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.getPolyPerim(final List&lt;E&gt;).
   * 
   * <p>Pre: Only one point
   * 
   * <p>Post: Perimeter equals 0
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetPolyPerim_1() throws Exception {
    BasicPolygons b = new BasicPolygons();
    double p = b.getPolyPerim(point);
    assertEquals(0.0, p, 1e-5);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.getPolyArea(final List&lt;E&gt;).
   * 
   * <p>Pre: Four point polygon
   * 
   * <p>Post: Area
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetPolyArea() throws Exception {
    BasicPolygons b = new BasicPolygons();
    double p = b.getPolyArea(points);
    assertEquals(17.0, p, 1e-5);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.getPolyArea(final List&lt;E&gt;).
   * 
   * <p>Pre: one point
   * 
   * <p>Post: Area equals 0
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetPolyArea_1() throws Exception {
    BasicPolygons b = new BasicPolygons();
    double p = b.getPolyArea(point);
    assertEquals(0.0, p, 1e-5);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.getPolyArea(final List&lt;E&gt;).
   * 
   * <p>Pre: two points
   * 
   * <p>Post:Area equals 0
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetPolyArea_2() throws Exception {
    BasicPolygons b = new BasicPolygons();
    double p = b.getPolyArea(points2);
    assertEquals(0.0, p, 1e-5);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.isPointInside(final List&lt;E&gt;, final Tuple2d)
   * 
   * <p>Pre: Point inside
   * 
   * <p>Post: Return true
   * 
   * @throws Exception Exception
   */
  @Test
  public void testIsPointInside() throws Exception {
    BasicPolygons b = new BasicPolygons();
    boolean p = b.isPointInside(points, inpoints.get(0));
    assertTrue(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.isPointInside(final List&lt;E&gt;, final Tuple2d).
   * 
   * <p>Pre: Point equals vertices
   * 
   * <p>Post: Return true
   * 
   * @throws Exception Exception
   */
  @Test
  public void testIsPointInside_1() throws Exception {
    BasicPolygons b = new BasicPolygons();
    boolean p = b.isPointInside(points, new Point2d(6, 4));
    assertTrue(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.isPointInside(final List&lt;E&gt;, final Tuple2d).
   * 
   * <p>Pre: Point outside
   * 
   * <p>Post: Return false
   * 
   * @throws Exception Exception
   */
  @Test
  public void testIsPointInside_2() throws Exception {
    BasicPolygons b = new BasicPolygons();
    boolean p = b.isPointInside(points, new Point2d(7, 4));
    assertFalse(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.arePointsInside(final List&lt;E&gt;, final
   * List&lt;E&gt;).
   * 
   * <p>Pre: Points inside
   * 
   * <p>Post: Return true
   * 
   * @throws Exception Exception
   */
  @Test
  public void testarePointsInside() throws Exception {
    BasicPolygons b = new BasicPolygons();
    boolean p = b.areAllPointsInside(points, inpoints);
    assertTrue(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.arePointsInside(final List&lt;E&gt;, final
   * List&lt;E&gt;).
   * 
   * <p>Pre: Points inside and one outside
   * 
   * <p>Post: Return false
   * 
   * @throws Exception Exception
   */
  @Test
  public void testarePointsInside_1() throws Exception {
    BasicPolygons b = new BasicPolygons();
    ArrayList<Point2d> c = new ArrayList<>(inpoints);
    c.add(new Point2d(10, 10));
    boolean p = b.areAllPointsInside(points, c);
    assertFalse(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.isanyPointInside(final List&lt;E&gt;, final
   * List&lt;E&gt;)
   * 
   * <p>Pre: Points inside and one outside
   * 
   * <p>Post: Return false
   * 
   * @throws Exception Exception
   */
  @Test
  public void testisanyPointInside() throws Exception {
    BasicPolygons b = new BasicPolygons();
    ArrayList<Point2d> c = new ArrayList<>(inpoints);
    c.add(new Point2d(10, 10));
    boolean p = b.areAllPointOutside(points, c);
    assertFalse(p);
  }

  /**
   * Test method for QuimP.geom.BasicPolygons.isanyPointInside(final List&lt;E&gt;, final
   * List&lt;E&gt;)
   * 
   * <p>Pre: All outside
   * 
   * <p>Post: Return true
   * 
   * @throws Exception Exception
   */
  @Test
  public void testisanyPointInside_1() throws Exception {
    BasicPolygons b = new BasicPolygons();
    ArrayList<Point2d> c = new ArrayList<>();
    c.add(new Point2d(10, 10));
    c.add(new Point2d(40, 40));
    boolean p = b.areAllPointOutside(points, c);
    assertTrue(p);
  }

}
