/**
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
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOAPoint2dFilter;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginFactory_Test {
    static final Logger LOGGER = LoggerFactory.getLogger(PluginFactory_Test.class.getName());

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
     * Test method for {@link PluginFactory#getPluginNames(int)}.
     * 
     * Pre: Two dummy plugins in directory
     * 
     * Post: Two plugins names Plugin1 and Plugin2
     * 
     * @throws Exception
     */
    @Test
    @Ignore("Test plugins must be recompiled")
    public void test_GetPluginNames() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/resources/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpCorePlugin.DOES_SNAKES);
        HashSet<String> hs = new HashSet<>(ar);
        assertTrue(hs.contains("Plugin1"));
        assertTrue(hs.contains("Plugin2"));
    }

    /**
     * Test method for {@link PluginFactory#getPluginNames(int)}.
     * 
     * Pre: None plugins in directory
     * 
     * Post: empty list
     * 
     * @throws Exception
     */
    @Test
    public void test_GetPluginNames_noplugins() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpCorePlugin.DOES_SNAKES);
        assertTrue(ar.isEmpty());
    }

    /**
     * Test method for {@link PluginFactory#getPluginNames(int)}.
     * 
     * Pre: Directory does not exist
     * 
     * Post: empty list
     * 
     * @throws Exception
     * 
     */
    @Test(expected = QuimpPluginException.class)
    public void test_GetPluginNames_nodir() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("../fgrtg/"));
        ArrayList<String> ar;
        ar = pluginFactory.getPluginNames(IQuimpCorePlugin.DOES_SNAKES);
        assertTrue(ar.isEmpty());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(String)} This test
     * creates instances of plugins and calls methods from them storing and reading data from
     * created object for plugin2
     * 
     * Pre: wo dummy plugins in ../Test-Plugins/target/ directory of type DOES_SNAKES
     * 
     * @throws Exception
     */
    @Test
    @Ignore("Test plugins must be recompiled")
    public void test_GetInstance() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/resources/"));
        ParamList test = new ParamList();
        ParamList ret;
        test.put("window", "0.02");
        test.put("alfa", "10.0");
        // correct because we check plugin type before
        IQuimpBOAPoint2dFilter filter1 =
                (IQuimpBOAPoint2dFilter) pluginFactory.getInstance("Plugin1");
        assertEquals(filter1.getVersion(), "0.0.2");
        IQuimpBOAPoint2dFilter filter2 =
                (IQuimpBOAPoint2dFilter) pluginFactory.getInstance("Plugin2");
        filter2.setPluginConfig(test);
        ret = filter2.getPluginConfig();
        assertEquals(Double.parseDouble(ret.get("Window")), 0.02, 1e-5);
        assertEquals(Double.parseDouble(ret.get("alfa")), 10, 1e-5);
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(String)} This test
     * try to call plugin that does not exist.
     * 
     * Pre: Empty directory but existing
     * 
     * @throws Exception
     */
    @Test
    public void test_GetInstance_noplugin() throws Exception {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/"));
        // we should be sure that this casting is correct because we check plugin type before
        IQuimpBOAPoint2dFilter filter1 =
                (IQuimpBOAPoint2dFilter) pluginFactory.getInstance("Plugin1");
        assertTrue(filter1 == null);
    }

    /**
     * Test method for private PluginFactory#scanDirectory().
     * 
     * Pre: Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in test directory
     * 
     * Post: Return list of files that according to hardcoded criterion. For more files they may be
     * returned in random order.
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws QuimpPluginException
     */
    @Test
    @Ignore("Test plugins must be recompiled")
    public void test_scanDirectory()
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/resources/"));
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
        assertTrue(r.contains("plugin2-quimp.jar"));
        assertTrue(r.contains("plugin1-quimp.jar"));
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.PluginFactory#getClassName()
     * 
     * Pre: Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in test directory
     * 
     * Post: Qualified name of class in plugin 2 must be correct
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws QuimpPluginException
     */
    @Test
    @Ignore("Test plugins must be recompiled")
    public void test_getClassName()
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/resources/"));
        Class<?>[] args = new Class<?>[1];
        args[0] = File.class;
        Method m = pluginFactory.getClass().getDeclaredMethod("getClassName", args);
        m.setAccessible(true);
        File file = new File("src/test/resources/plugin2-quimp.jar");
        String ret = (String) m.invoke(pluginFactory, file);
        assertEquals("uk.ac.warwick.wsbc.Plugin2_", ret);

    }

    /**
     * Test of reading type and version from plugins
     * 
     * Pre: Two jars plugin2_quimp-0.0.1.jar and plugin1_quimp-0.0.1.jar in test directory
     * 
     * Post: proper versions, types and qnames
     * 
     * @throws QuimpPluginException
     */
    @Test
    @Ignore("Test plugins must be recompiled")
    public void test_getAllPlugins() throws QuimpPluginException {
        PluginFactory pluginFactory;
        pluginFactory = new PluginFactory(Paths.get("src/test/resources/"));
        Map<String, PluginProperties> pp = pluginFactory.getRegisterdPlugins();
        PluginProperties p1 = pp.get("Plugin1");
        assertEquals(IQuimpCorePlugin.DOES_SNAKES, p1.getType());
        assertEquals("uk.ac.warwick.wsbc.Plugin1_", p1.getClassName());
        assertEquals("0.0.2", p1.getVersion());

        PluginProperties p2 = pp.get("Plugin2");
        assertEquals(IQuimpCorePlugin.DOES_SNAKES, p2.getType());
        assertEquals("uk.ac.warwick.wsbc.Plugin2_", p2.getClassName());
        assertEquals("0.0.1", p2.getVersion());

    }
}
