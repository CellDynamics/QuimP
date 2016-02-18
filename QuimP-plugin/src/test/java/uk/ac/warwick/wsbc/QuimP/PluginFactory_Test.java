/**
 * @file PluginFactory_Test.java
 * @date 9 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.warwick.wsbc.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.plugin.snakes.IQuimpPoint2dFilter;

/**
 * @author p.baniukiewicz
 * @date 9 Feb 2016
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginFactory_Test {
    private static final Logger LOGGER =
            LogManager.getLogger(PluginFactory_Test.class.getName());
    private PluginFactory pluginFactory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link wsbc.QuimP.PluginFactory#getPluginNames(int)}.
     * 
     * @pre Two dummy plugins in src/test/resources/ directory
     * @post Two plugins names \a Plugin1 and \a Plugin2
     */
    @Test
    public void test_GetPluginNames() throws Exception {
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES);
        HashSet<String> hs = new HashSet<>(ar);
        assertTrue(hs.contains("Plugin1"));
        assertTrue(hs.contains("Plugin2"));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(final String)}
     * This test creates instances of plugins and calls methods from them
     * storing and reading data from created object for plugin2
     * 
     * @pre Two dummy plugins in src/test/resources/ directory of type
     * DOES_SNAKES
     */
    @Test
    public void test_GetInstance() throws Exception {
        HashMap<String, Object> test = new HashMap<>();
        Map<String, Object> ret;
        test.put("window", 0.02);
        test.put("alfa", 10.0);
        @SuppressWarnings("unchecked") // we should be sure that this casting is
        // correct because we check plugin type before
        IQuimpPoint2dFilter<Vector2d> filter1 =
                (IQuimpPoint2dFilter<Vector2d>) pluginFactory
                        .getInstance("Plugin1");
        assertEquals(filter1.getVersion(), "0.0.2");
        @SuppressWarnings("unchecked")
        IQuimpPoint2dFilter<Vector2d> filter2 =
                (IQuimpPoint2dFilter<Vector2d>) pluginFactory
                        .getInstance("Plugin2");
        filter2.setPluginConfig(test);
        ret = filter2.getPluginConfig();
        assertEquals((Double) ret.get("window"), 0.02, 1e-5);
        assertEquals((Double) ret.get("alfa"), 10, 1e-5);
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#scanDirectory()}
     * 
     * @pre Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in
     * test directory
     * @post
     * Return list of files that according to hardcoded criterion. For more
     * files they may be returned in random order.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void test_scanDirectory()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method m = pluginFactory.getClass().getDeclaredMethod("scanDirectory");
        m.setAccessible(true);
        File[] ret = (File[]) m.invoke(pluginFactory);
        for (File f : ret) {
            LOGGER.debug(f.getName());
        }
        assertEquals("plugin2_quimp-0.0.1.jar", ret[0].getName());
        assertEquals("plugin1_quimp-0.0.1.jar", ret[1].getName());
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getPluginType()}
     * 
     * @pre Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in
     * test directory
     * @post
     * Types \c DOES_SNAKES returned from plugins
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void test_getPluginType()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class<?>[] args = new Class<?>[2];
        args[0] = File.class;
        args[1] = String.class;
        Method m = pluginFactory.getClass().getDeclaredMethod("getPluginType",
                args);
        m.setAccessible(true);

        File file = new File("../plugins_test/target/plugin2_quimp-0.0.1.jar");
        int ret = (int) m.invoke(pluginFactory, file,
                "uk.ac.warwick.wsbc.Plugin2");
        assertEquals(1, ret);

        file = new File("../plugins_test/target/plugin1_quimp-0.0.1.jar");
        ret = (int) m.invoke(pluginFactory, file,
                "uk.ac.warwick.wsbc.Plugin1");
        assertEquals(1, ret);

    }

}
