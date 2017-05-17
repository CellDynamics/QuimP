package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Point2d;

/**
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.quimp.geom.filters.OutlineProcessorTest#testShrink()
 */
public class OutlineTest extends JsonKeyMatchTemplate<Outline> {

  /**
   * Configure test.
   * 
   * <p>do not use randomizer in JsonKeyMatchTemplate (we build object already.
   */
  public OutlineTest() {
    super(1, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    List<Vert> list = VertTest.getRandomVertPointList(); // get list of random vertexes
    Vert head = list.get(0); // get head of list

    obj = new Outline(head, list.size()); // build outline
    indir = "uk.ac.warwick.wsbc.quimp.Outline";
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#prepare()
   */
  @Override
  protected void prepare() throws Exception {
    super.prepare();
  }

  /**
   * Test of {@link uk.ac.warwick.wsbc.quimp.Outline#Outline(uk.ac.warwick.wsbc.quimp.Outline)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testOutline() throws Exception {
    // TODO add copy constructor
  }

  /**
   * Test method for
   * {@link uk.ac.warwick.wsbc.quimp.Outline#Outline(uk.ac.warwick.wsbc.quimp.Snake)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testOutlineSnake() throws Exception {
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(0, 0));
    list.add(new Point2d(1, 10));
    list.add(new Point2d(5, 9));
    list.add(new Point2d(6, 2));
    list.add(new Point2d(7, -1));
    Snake snake = new Snake(list, 10);
    Outline outline = new Outline(snake);

    assertThat(outline.getNumPoints(), is(snake.getNumPoints()));
    assertThat(outline.getCentroid(), is(snake.getCentroid()));
    assertThat(outline.nextTrackNumber, is(snake.nextTrackNumber));
    assertThat(outline.getHead(), instanceOf(Vert.class));
    Node n = snake.getHead();
    Vert v = outline.getHead();
    do {
      assertThat(v.getCurvatureLocal(), is(n.getCurvatureLocal()));
      assertThat(v.isHead(), is(n.isHead()));
      n = n.getNext();
      v = v.getNext();
    } while (!n.isHead());
  }

}
