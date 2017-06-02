package com.github.celldynamics.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.SnakeTest
 */
public class ShapeTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ShapeTest.class.getName());

  private Vert head;
  private Vert v1;
  private Vert v2;
  private Vert v3;
  TestShape test;

  /**
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    List<Vert> ret = com.github.celldynamics.quimp.VertTest.getRandomVertPointList();

    head = ret.get(0);
    v1 = ret.get(1);
    v2 = ret.get(2);
    v3 = ret.get(3);

    test = new TestShape(head, 4);
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    head = null;
    v1 = null;
    v2 = null;
    v3 = null;
    test = null;
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.Shape#setHead(com.github.celldynamics.quimp.PointsList)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSetHead() throws Exception {

    assertThat(head.isHead(), is(true));

    test.setHead(v1);
    assertThat(head.isHead(), is(false));
    assertThat(v1.isHead(), is(true));
    assertThat(test.getHead(), is(v1));

    test.setHead(v3);
    assertThat(head.isHead(), is(false));
    assertThat(v1.isHead(), is(false));
    assertThat(v3.isHead(), is(true));
    assertThat(test.getHead(), is(v3));
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.Shape#setHead(com.github.celldynamics.quimp.PointsList)}.
   * 
   * <p>Set same head as it was.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSetHead_1() throws Exception {

    assertThat(head.isHead(), is(true));

    test.setHead(head);
    assertThat(head.isHead(), is(true));
    assertThat(test.getHead(), is(head));

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.Shape#setHead(com.github.celldynamics.quimp.PointsList)}.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSetHead_noVert() throws Exception {
    assertThat(head.isHead(), is(true));

    Vert dummy = new Vert();
    dummy.setHead(true);
    test.setHead(dummy);
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Shape#checkIsHead()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCheckIsHead() throws Exception {
    assertThat(test.checkIsHead(), is(not(nullValue())));
    assertThat(test.checkIsHead(), is(head));
    head.setHead(false); // accidently remove head marker from wrapped list
    assertThat(test.checkIsHead(), is(nullValue()));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Shape#setHead(int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSetNewHead() throws Exception {
    assertThat(test.getHead(), is(head));
    test.setHead(23569856); // non existing
    assertThat(test.getHead(), is(head));

    test.setHead(3); // set to id=3
    assertThat(test.getHead(), is(v2));
    assertThat(head.isHead(), is(false));
  }

  /**
   * Test of Shape constructor.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testShape() throws Exception {
    // looped list with head
    TestShape ts = new TestShape(head, 4);
    assertThat(ts.getHead(), is(head));

    // looped list with head but given other vertex
    TestShape ts1 = new TestShape(v1, 4);
    assertThat(ts1.getHead(), is(head)); // original head is discovered

    // looped list no head
    head.setHead(false);
    TestShape ts2 = new TestShape(v1, 4);
    assertThat(ts2.getHead(), is(v1)); // head is set to current

  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Shape#validateShape()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateShape() throws Exception {
    assertThat(test.validateShape(), is(Shape.LIST_OK));
    test.getHead().head = false; // cancel head tag for head
    assertThat(test.validateShape(), is(Shape.BAD_HEAD | Shape.NO_HEAD));
    test.getHead().head = true; // restore it
    head.getNext().setNext(null); // but break linking
    assertThat(test.validateShape(), is(Shape.BAD_LINKING));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Shape#validateShape()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateShape_1() throws Exception {
    assertThat(test.validateShape(), is(Shape.LIST_OK));
    test.getHead().head = false; // cancel head tag for head
    assertThat(test.validateShape(), is(Shape.BAD_HEAD | Shape.NO_HEAD));
    test.getHead().head = true; // restore it
    head.getNext().setNext(null); // but break linking
    assertThat(test.validateShape(), is(Shape.BAD_LINKING));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Shape#validateShape()}. Wrong number of
   * given elements.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateShape_2() throws Exception {
    TestShape test1 = new TestShape(head, 2); // wrong number of points in list
    assertThat(test1.validateShape(), is(Shape.BAD_NUM_POINTS));
  }

  /**
   * Test of Shape constructor.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testShape_1() throws Exception {
    // not looped list no head
    head.setHead(false);
    head.setPrev(null);
    v3.setNext(null);
    TestShape ts2 = new TestShape(v1, 4);
  }

  /**
   * Test iterator.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testIterator() throws Exception {
    Iterator<Vert> iter = test.iterator();
    int tracknumber = 1;
    while (iter.hasNext()) {
      Vert v = iter.next();
      LOGGER.debug(v.toString());
      assertThat(v.getTrackNum(), is(tracknumber++)); // elements in order
    }
    assertThat(tracknumber, is(test.POINTS + 1)); // 4 points in list
  }

  /**
   * Test iterator. One element - looped
   * 
   * @throws Exception Exception
   */
  @Test
  public void testIterator_1() throws Exception {
    Vert vinit = com.github.celldynamics.quimp.VertTest.getRandomVert(1);
    vinit.setHead(true);
    vinit.setNext(vinit);
    vinit.setPrev(vinit);

    TestShape test = new TestShape(vinit, 1);
    Iterator<Vert> iter = test.iterator();
    int tracknumber = 1;
    while (iter.hasNext()) {
      Vert v = iter.next();
      LOGGER.debug(v.toString());
      assertThat(v.getTrackNum(), is(tracknumber++)); // elements in order
    }
    assertThat(tracknumber, is(test.POINTS + 1)); // 4 points in list
  }

  /**
   * Test class.
   * 
   * @author p.baniukiewicz
   *
   */
  class TestShape extends Shape<Vert> {

    /*
     * 
     */
    public TestShape(Vert h, int n) {
      super(h, n);
    }

    /*
     * 
     */
    public TestShape(Vert h) {
      super(h);
    }
  }

}