package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Point2d;

import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

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

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. Lack of two points separated
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_1() throws Exception {
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    // ?
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    // ?
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 0.9);

    List<Point2d> expList = new ArrayList<>();
    expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);

    assertThat(outline.asList(), is(outlineExp.asList()));
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. Lack of three points neighbours just after head
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_2() throws Exception {
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    // ?
    // ?
    // ?
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    list.add(new Point2d(3, 1));
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 0.9);

    List<Point2d> expList = new ArrayList<>();
    expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);

    assertThat(outline.asList(), is(outlineExp.asList()));
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. Lack of three points neighbours far after head
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_3() throws Exception {
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    list.add(new Point2d(3, 0));
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    // list.add(new Point2d(4, 1));
    // list.add(new Point2d(3, 1));
    // list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 0.9);

    List<Point2d> expList = new ArrayList<>();
    expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);

    assertThat(outline.asList(), is(outlineExp.asList()));
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. All point every second to be removed (min distance 1.1)
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_4() throws Exception {
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    list.add(new Point2d(3, 0));
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    list.add(new Point2d(3, 1));
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 1.1);

    List<Point2d> expList = new ArrayList<>();
    expList.add(new Point2d(1, 0));
    // expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    // expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    // expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    // expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    // expList.add(new Point2d(1, 1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);

    assertThat(outline.asList(), is(outlineExp.asList()));
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. Point head to remove (point before too close)
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_5() throws Exception {
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.0); // head next
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    list.add(new Point2d(3, 0));
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    list.add(new Point2d(3, 1));
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    list.add(new Point2d(1, 0.1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 0.2);

    List<Point2d> expList = new ArrayList<>();
    // expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    expList.add(new Point2d(1, 0.1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);

    assertThat(outline.asList(), is(outlineExp.asList()));

    f.setDouble(Shape.class, 0.0); // head prev
    list.clear();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    list.add(new Point2d(3, 0));
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    list.add(new Point2d(3, 1));
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    list.add(new Point2d(1, 0.1));
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(1.9, 0.2);

    expList.clear();
    ;
    // expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    expList.add(new Point2d(1, 0.1));
    outlineExp = new QuimpDataConverter(expList).getOutline(0);

    f.setDouble(Shape.class, 0.5);
    assertThat(outline.asList(), is(outlineExp.asList()));
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Outline#correctDensity(double, double)}.
   * 
   * <p>Looped list with distances of 1.0. removing over inserting, inserted vertex can be next
   * removed if it is too close. Does not work viceversa as inserting does not update current vertex
   * but removing does. And removing is checked first
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCorrectDensity_6() throws Exception {
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.0); // head next
    List<Point2d> list = new ArrayList<>();
    list.add(new Point2d(1, 0));
    list.add(new Point2d(2, 0));
    list.add(new Point2d(3, 0));
    list.add(new Point2d(4, 0));
    list.add(new Point2d(5, 0));
    list.add(new Point2d(5, 1));
    list.add(new Point2d(4, 1));
    list.add(new Point2d(3, 1));
    list.add(new Point2d(2, 1));
    list.add(new Point2d(1, 1));
    list.add(new Point2d(1, 0.1));
    Outline outline;
    outline = new QuimpDataConverter(list).getOutline(0);
    outline.correctDensity(0.8, 0.7);

    List<Point2d> expList = new ArrayList<>();
    // expList.add(new Point2d(1, 0));
    expList.add(new Point2d(2, 0));
    expList.add(new Point2d(3, 0));
    expList.add(new Point2d(4, 0));
    expList.add(new Point2d(5, 0));
    expList.add(new Point2d(5, 1));
    expList.add(new Point2d(4, 1));
    expList.add(new Point2d(3, 1));
    expList.add(new Point2d(2, 1));
    expList.add(new Point2d(1, 1));
    expList.add(new Point2d(1, 0.1));
    Outline outlineExp = new QuimpDataConverter(expList).getOutline(0);
  }

}
