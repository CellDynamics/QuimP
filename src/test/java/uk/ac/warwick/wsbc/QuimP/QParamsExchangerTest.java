/**
 */
package uk.ac.warwick.wsbc.QuimP;

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

import uk.ac.warwick.wsbc.QuimP.BOAState.BOAp;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class QParamsExchangerTest {
    /**
     * Accessor to private fields.
     * 
     * Example of use:
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
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
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
    private File test1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        test1 = new File("src/test/resources/test2/Stack_cut.QCONF");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * @throws Exception
     */
    @Test
    public void testQParamsExchanger() throws Exception {
        QParamsQconf qp = new QParamsQconf(test1);
        assertThat(qp.getFileName(), is("Stack_cut"));
        assertThat(qp.getPath(), is(Paths.get("src", "test", "resources", "test2").toString()));
    }

    /**
     * Compare read parameters from Stack_cut.QCONF
     * 
     * pre: Two snakes from 1 to 30 frame, on 10th frame changed segmentation parameters, on 20th
     * selected filter
     * 
     * @throws Exception
     */
    @Test
    @Ignore("test1 must be saved in new DataContainer format")
    public void testReadParams() throws Exception {
        QParamsQconf qp = new QParamsQconf(test1);
        qp.readParams();

        Nest n = qp.getNest();
        assertThat(n.size(), is(2));

        SnakeHandler sH = n.getHandler(0);
        assertThat(sH.getStartFrame(), is(1));
        assertThat(sH.getEndFrame(), is(30));
        Snake s = sH.getStoredSnake(10);
        assertThat(s.POINTS, is(20));
        assertThat(s.countPoints(), is(s.POINTS));

        BOAp bp = qp.getLoadedDataContainer().BOAState.boap;
        assertThat(bp.getWIDTH(), is(512));

    }

    /**
     * no file
     * 
     * pre: there is no file
     * 
     * post: QuimpException
     * 
     * @throws Exception
     */
    @Test(expected = QuimpException.class)
    public void testReadParams_1() throws Exception {
        QParamsQconf qp = new QParamsQconf(new File("dff/ss.s"));
        qp.readParams();

    }

}
