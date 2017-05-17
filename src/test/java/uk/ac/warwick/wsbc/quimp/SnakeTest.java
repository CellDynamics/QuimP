package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

/**
 * @author p.baniukiewicz
 *
 */
public class SnakeTest extends JsonKeyMatchTemplate<Snake> {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SnakeTest.class.getName());

  private QuimpVersion info = new QuimpVersion();

  /**
   * Configure test.
   * 
   * <p>do not use randomizer in JsonKeyMatchTemplate (we build object already.
   */
  public SnakeTest() {
    super(1, true);
  }

  /**
   * Setup.
   * 
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    BOA_.qState = new BOAState(null);
    Node head = NodeTest.getRandomNodePointList().get(0);
    obj = new Snake(head, 4, 1);

    indir = "uk.ac.warwick.wsbc.quimp.Snake";
  }

  /**
   * tearDown.
   * 
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    obj = null;
  }

  /**
   * Test of Snake Serializer.
   * 
   * <p>Pre: Snake is saved to disk
   * 
   * <p>Post: Snake is loaded and restored
   * 
   * @throws IOException IOException
   * @throws Exception Exception
   */
  @Test
  public void testSerializeSnake_1() throws IOException, Exception {
    obj.setHead(2);
    Serializer<Snake> serializer;
    serializer = new Serializer<>(obj, info);
    serializer.setPretty();
    serializer.save(tmpdir + "snake1.tmp");

    // load it
    Snake loaded;
    Serializer<Snake> loader = new Serializer<>(Snake.class, QuimP.TOOL_VERSION);
    loaded = loader.load(tmpdir + "snake1.tmp").obj;
    LOGGER.debug(loaded.toString());
    assertEquals(obj.getNumPoints(), loaded.getNumPoints());
    for (int i = 0; i < obj.getNumPoints(); i++) {
      Node s1 = obj.getHead();
      Node s2 = loaded.getHead();
      assertEquals(s1.isFrozen(), s2.isFrozen());
      assertEquals(s1.getF_total(), s2.getF_total());
      assertEquals(s1.getVel(), s2.getVel());
      assertEquals(s1.point, s2.point);
      assertEquals(s1.normal, s2.normal);
      assertEquals(s1.tan, s2.tan);
      assertEquals(s1.tracknumber, s2.tracknumber);
      assertEquals(s1.position, s2.position, 1e-6);

      s1 = s1.getNext();
      s2 = s2.getNext();
    }
    assertEquals(obj.alive, loaded.alive);
    assertEquals(obj.getSnakeID(), loaded.getSnakeID(), 1e-6);
    assertEquals(obj.isFrozen(), loaded.isFrozen());

    // copmapre using equals
    assertThat(loaded, is(obj));
  }

  /**
   * Test of copy constructor.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSnakeSnakeInt() throws Exception {
    Snake copy = new Snake(obj, obj.getSnakeID());
    assertThat(copy, is(obj));
    assertEquals(copy.hashCode(), obj.hashCode());
  }

  /**
   * Test of copy constructor.
   * 
   * <p>Pre: One node differs
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSnakeSnakeInt_2() throws Exception {
    Snake copy = new Snake(obj, obj.getSnakeID());
    Node n = copy.getHead().getNext();
    n.addVel(new ExtendedVector2d(3, 3));
    assertThat(copy, is(not(obj)));
    assertThat(copy.hashCode(), is(not(obj.hashCode())));
  }

}
