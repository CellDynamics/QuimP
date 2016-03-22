/**
 * @file SnakePluginListTest.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SnakePluginListTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2_test.xml");
    }
    @Mock
    private PluginFactory pluginFactory;

    private SnakePluginList snakePluginList;

    /**
     * Creates three fake plugins and fourth that will replace one of them
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        snakePluginList = new SnakePluginList(3, pluginFactory);
        /**
         * This plugin does not have config
         */
        Mockito.when(pluginFactory.getInstance("Test1")).thenReturn(new IQuimpPlugin() {

            @Override
            public void showUI(boolean val) {
            }

            @Override
            public int setup() {
                return 0;
            }

            @Override
            public void setPluginConfig(ParamList par) throws QuimpPluginException {
            }

            @Override
            public String getVersion() {
                return "1.2.3";
            }

            @Override
            public ParamList getPluginConfig() {
                return null;
            }
        });
        /**
         * This has config
         */
        Mockito.when(pluginFactory.getInstance("Test2")).thenReturn(new IQuimpPlugin() {

            @Override
            public void showUI(boolean val) {
            }

            @Override
            public int setup() {
                return 0;
            }

            @Override
            public void setPluginConfig(ParamList par) throws QuimpPluginException {
            }

            @Override
            public String getVersion() {
                return "2.3.4";
            }

            @Override
            public ParamList getPluginConfig() {
                ParamList pl = new ParamList();
                pl.put("window", "10");
                pl.put("alpha", "-0.45");
                return pl;
            }
        });
        /**
         * This is for testing deletions
         */
        Mockito.when(pluginFactory.getInstance("toDelete")).thenReturn(new IQuimpPlugin() {

            @Override
            public void showUI(boolean val) {
            }

            @Override
            public int setup() {
                return 0;
            }

            @Override
            public void setPluginConfig(ParamList par) throws QuimpPluginException {
            }

            @Override
            public String getVersion() {
                return "2.3.4";
            }

            @Override
            public ParamList getPluginConfig() {
                return null;
            }
        });
        /**
         * This will replace plugin 0
         */
        Mockito.when(pluginFactory.getInstance("newInstance")).thenReturn(new IQuimpPlugin() {

            @Override
            public void showUI(boolean val) {
            }

            @Override
            public int setup() {
                return 0;
            }

            @Override
            public void setPluginConfig(ParamList par) throws QuimpPluginException {
            }

            @Override
            public String getVersion() {
                return "0.0.1";
            }

            @Override
            public ParamList getPluginConfig() {
                return null;
            }
        });
        snakePluginList.setInstance(0, "Test1", false); // slot 0
        snakePluginList.setInstance(1, "Test2", true); // slot 1
        snakePluginList.setInstance(2, "toDelete", true); // slot 2
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        snakePluginList = null;
    }

    /**
     * Test method for 
     * {@link wsbc.QuimP.SnakePluginList#SnakePluginList(int, uk.ac.warwick.wsbc.QuimP.PluginFactory)}.
     */
    @Test
    public void testSnakePluginListIntPluginFactory() throws Exception {
        assertEquals(3, snakePluginList.sPluginList.size());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#getInstance(int)}.
     */
    @Test
    public void testGetInstance() throws Exception {
        IQuimpPlugin inst = snakePluginList.getInstance(0);
        assertEquals("1.2.3", inst.getVersion());
        inst = snakePluginList.getInstance(1);
        assertEquals("2.3.4", inst.getVersion());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#isActive(int)}.
     */
    @Test
    public void testIsActive() throws Exception {
        assertFalse(snakePluginList.isActive(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#setInstance(int, java.lang.String)}.
     */
    @Test
    public void testSetInstance() throws Exception {
        IQuimpPlugin inst = snakePluginList.getInstance(0);
        assertEquals("1.2.3", inst.getVersion());
        snakePluginList.setInstance(0, "newInstance", true); // slot 0
        inst = snakePluginList.getInstance(0);
        assertEquals("0.0.1", inst.getVersion());
        assertTrue(snakePluginList.isActive(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#setActive(int, boolean)}.
     */
    @Test
    public void testSetActive() throws Exception {
        snakePluginList.setActive(0, true);
        assertTrue(snakePluginList.isActive(0));
        snakePluginList.setActive(0, false);
        assertFalse(snakePluginList.isActive(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#deletePlugin(int)}.
     */
    @Test
    public void testDeletePlugin() throws Exception {
        snakePluginList.deletePlugin(2);
        assertEquals(null, snakePluginList.getInstance(2));
        assertFalse(snakePluginList.isActive(2));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#isRefListEmpty()}.
     */
    @Test
    public void testIsRefListEmpty() throws Exception {
        assertFalse(snakePluginList.isRefListEmpty());
        snakePluginList.deletePlugin(0);
        assertFalse(snakePluginList.isRefListEmpty());
        snakePluginList.deletePlugin(1);
        assertFalse(snakePluginList.isRefListEmpty());
        snakePluginList.deletePlugin(2);
        assertTrue(snakePluginList.isRefListEmpty());

    }

}
