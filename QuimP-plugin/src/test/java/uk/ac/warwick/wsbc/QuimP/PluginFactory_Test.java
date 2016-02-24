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
import java.util.HashSet;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;

/**
 * @author p.baniukiewicz
 * @date 9 Feb 2016
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginFactory_Test {
    private static final Logger LOGGER =
            LogManager.getLogger(PluginFactory_Test.class.getName());

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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
     * @pre Two dummy plugins in directory
     * @post Two plugins names \a Plugin1 and \a Plugin2
     */
    @Test
    public void test_GetPluginNames() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES);
        HashSet<String> hs = new HashSet<>(ar);
        assertTrue(hs.contains("Plugin1"));
        assertTrue(hs.contains("Plugin2"));
    }

    /**
     * Test method for {@link wsbc.QuimP.PluginFactory#getPluginNames(int)}.
     * 
     * @pre None plugins in directory
     * @post empty list
     */
    @Test
    public void test_GetPluginNames_noplugins() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES);
        assertTrue(ar.isEmpty());
    }

    /**
     * Test method for {@link wsbc.QuimP.PluginFactory#getPluginNames(int)}.
     * 
     * @pre Directory does not exist
     * @post empty list
     */
    @Test(expected = QuimpPluginException.class)
    public void test_GetPluginNames_nodir() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../fgrtg/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES);
        assertTrue(ar.isEmpty());
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(final String)}
     * This test creates instances of plugins and calls methods from them
     * storing and reading data from created object for plugin2
     * 
     * @pre Two dummy plugins in ../plugins_test/target/ directory of type
     * DOES_SNAKES
     */
    @Test
    public void test_GetInstance() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
        ParamList test = new ParamList();
        ParamList ret;
        test.put("window", "0.02");
        test.put("alfa", "10.0");
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
        assertEquals(Double.parseDouble(ret.get("Window")), 0.02, 1e-5);
        assertEquals(Double.parseDouble(ret.get("alfa")), 10, 1e-5);
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(final String)}
     * This test try to call plugin that does not exist
     * 
     * @pre Empty directory but existing
     */
    @Test
    public void test_GetInstance_noplugin() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/"));
        // we should be sure that this casting is
        @SuppressWarnings({ "unchecked" })
        // correct because we check plugin type before
        IQuimpPoint2dFilter<Vector2d> filter1 =
                (IQuimpPoint2dFilter<Vector2d>) pluginFactory
                        .getInstance("Plugin1");
        assertTrue(filter1 == null);
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
     * @throws QuimpPluginException
     */
    @Test
    public void test_scanDirectory()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
        Method m = pluginFactory.getClass().getDeclaredMethod("scanDirectory");
        m.setAccessible(true);
        File[] ret = (File[]) m.invoke(pluginFactory);
        for (File f : ret) {
            LOGGER.debug(f.getName());
        }
        assertTrue(ret != null && ret.length > 0);
        HashSet<String> r = new HashSet<String>();
        for (File f : ret)
            r.add(f.getName());
        assertTrue(r.contains("plugin2_quimp-0.0.1.jar"));
        assertTrue(r.contains("plugin1_quimp-0.0.1.jar"));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getClassName()}
     * 
     * @pre Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in
     * test directory
     * @post
     * Qualified name of class in plugin 2 must be correct
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws QuimpPluginException
     */
    @Test
    public void test_getClassName()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
        Class<?>[] args = new Class<?>[1];
        args[0] = File.class;
        Method m = pluginFactory.getClass().getDeclaredMethod("getClassName",
                args);
        m.setAccessible(true);
        File file = new File("../plugins_test/target/plugin2_quimp-0.0.1.jar");
        String ret = (String) m.invoke(pluginFactory, file);
        assertEquals("uk.ac.warwick.wsbc.Plugin2_", ret);

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
     * @throws QuimpPluginException
     */
    @Test
    public void test_getPluginType()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../plugins_test/target/"));
        Class<?>[] args = new Class<?>[2];
        args[0] = File.class;
        args[1] = String.class;
        Method m = pluginFactory.getClass().getDeclaredMethod("getPluginType",
                args);
        m.setAccessible(true);

        File file = new File("../plugins_test/target/plugin2_quimp-0.0.1.jar");
        int ret = (int) m.invoke(pluginFactory, file,
                "uk.ac.warwick.wsbc.Plugin2_");
        assertEquals(1, ret);

        file = new File("../plugins_test/target/plugin1_quimp-0.0.1.jar");
        ret = (int) m.invoke(pluginFactory, file,
                "uk.ac.warwick.wsbc.Plugin1_");
        assertEquals(1, ret);

    }

}
