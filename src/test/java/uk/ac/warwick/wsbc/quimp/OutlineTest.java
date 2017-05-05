package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Point2d;

/**
 * @author p.baniukiewicz
 *
 */
public class OutlineTest {

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

    assertThat(outline.POINTS, is(snake.POINTS));
    assertThat(outline.getCentroid(), is(snake.getCentroid()));
    assertThat(outline.nextTrackNumber, is(snake.nextTrackNumber));
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
