package com.github.celldynamics.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState.BOAp;

/**
 * The Class QParamsExchangerTest.
 *
 * @author p.baniukiewicz
 */
public class QParamsExchangerTest {

  /**
   * Accessor to private fields.
   * 
   * <p>Example of use:
   * 
   * <pre>
   * <code>
   *     Snake s = new Snake(pr, 1);
   *     int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
   *             new Class[] { Snake.class });
   * </code>
   * </pre>
   *
   * @param name Name of private method
   * @param obj Reference to object
   * @param param Array of parameters if any
   * @param paramtype Array of classes of param
   * @return of private method
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  static Object accessPrivate(String name, QParamsQconf obj, Object[] param, Class<?>[] paramtype)
          throws NoSuchMethodException, SecurityException, IllegalAccessException,
          IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QParamsExchangerTest.class.getName());

  /** The test 1. */
  private File test1;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    test1 = new File("src/test/Resources-static/test2/Stack_cut.QCONF");
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test Q params exchanger.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQParamsExchanger() throws Exception {
    QParamsQconf qp = new QParamsQconf(test1);
    assertThat(qp.getFileName(), is("Stack_cut"));
    assertThat(qp.getPath(), is(Paths.get("src", "test", "Resources-static", "test2").toString()));
  }

  /**
   * Compare read parameters from Stack_cut.QCONF.
   * 
   * <p>pre: Two snakes from 1 to 30 frame, on 10th frame changed segmentation parameters, on 20th
   * selected filter
   * 
   * @throws Exception on error
   */
  @Test
  @Ignore("test1 must be saved in new DataContainer format")
  public void testReadParams() throws Exception {
    QParamsQconf qp = new QParamsQconf(test1);
    qp.readParams();

    Nest n = qp.getNest();
    assertThat(n.size(), is(2));

    SnakeHandler snakeHandler = n.getHandler(0);
    assertThat(snakeHandler.getStartFrame(), is(1));
    assertThat(snakeHandler.getEndFrame(), is(30));
    Snake s = snakeHandler.getStoredSnake(10);
    assertThat(s.getNumPoints(), is(20));
    assertThat(s.countPoints(), is(s.getNumPoints()));

    BOAp bp = qp.getLoadedDataContainer().BOAState.boap;
    assertThat(bp.getWidth(), is(512));

  }

  /**
   * no file.
   * 
   * <p>pre: there is no file
   * 
   * <p>post: QuimpException
   * 
   * @throws Exception on error
   */
  @Test(expected = QuimpException.class)
  public void testReadParams_1() throws Exception {
    QParamsQconf qp = new QParamsQconf(new File("dff/ss.s"));
    qp.readParams();

  }

}
