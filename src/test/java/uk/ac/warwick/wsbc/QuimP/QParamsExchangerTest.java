/**
 * @file QParamsExchangerTest.java
 * @date 26 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 */
public class QParamsExchangerTest {
    /**
     * Accessor to private fields
     * 
     * Example of use:
     * @code{.java}
     * Snake s = new Snake(pr, 1);
     * int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
     *           new Class[] { Snake.class });
     * @endcode   
     * 
     * @param name Name of private method
     * @param obj Reference to object 
     * @param param Array of parameters if any
     * @param paramtype Array of classes of \c param
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException         
     */
    static Object accessPrivate(String name, QParamsExchanger obj, Object[] param,
            Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
        prv.setAccessible(true);
        return prv.invoke(obj, param);
    }

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(QParamsExchangerTest.class.getName());
    private File test1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        test1 = new File("src/test/resources/test1/Stack_cut.QCONF");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testQParamsExchanger() throws Exception {
        QParamsExchanger qp = new QParamsExchanger(test1);
        assertThat(qp.prefix, is("Stack_cut"));
        assertThat(qp.path, is("src/test/resources/test1"));
    }

    /**
     * @test Compare read parameters from Stack_cut.QCONF
     * @pre Two snakes from  1 to 30 frame, on 10th frame changed segmentation parameters, on
     * 20th selected filter 
     * @throws Exception
     */
    @Test
    public void testReadParams() throws Exception {
        QParamsExchanger qp = new QParamsExchanger(test1);
        qp.readParams();

        Nest n = qp.getNest();
        assertThat(n.size(), is(2));

        SnakeHandler sH = n.getHandler(0);
        assertThat(sH.getStartFrame(), is(1));
        assertThat(sH.getEndFrame(), is(30));
        Snake s = sH.getStoredSnake(10);
        assertThat(s.POINTS, is(21));
        assertThat(s.countPoints(), is(s.POINTS));

    }

    /**
     * @test no file
     * @pre there is no file
     * @post QuimpException
     * @throws Exception
     */
    @Test(expected = QuimpException.class)
    public void testReadParams_1() throws Exception {
        QParamsExchanger qp = new QParamsExchanger(new File("dff/ss.s"));
        qp.readParams();

    }

}
