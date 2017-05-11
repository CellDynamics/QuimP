package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 *
 */
public class ShapeTest {

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
    head = new Vert();
    head.setHead(true);
    v1 = new Vert();
    v2 = new Vert();
    v3 = new Vert();

    head.setNext(v1);
    v1.setPrev(head);
    v1.setNext(v2);

    v2.setPrev(v1);
    v2.setNext(v3);

    v3.setPrev(v2);
    v3.setNext(head);
    head.setPrev(v3);

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
   * {@link uk.ac.warwick.wsbc.quimp.Shape#setHead(uk.ac.warwick.wsbc.quimp.PointsList)}.
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
   * {@link uk.ac.warwick.wsbc.quimp.Shape#setHead(uk.ac.warwick.wsbc.quimp.PointsList)}.
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
   * {@link uk.ac.warwick.wsbc.quimp.Shape#setHead(uk.ac.warwick.wsbc.quimp.PointsList)}.
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