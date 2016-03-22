/**
 * @file SnakePluginListTest.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

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
    private static final Logger LOGGER = LogManager.getLogger(SnakePluginListTest.class.getName());
    @Mock
    private PluginFactory pluginFactory;

    private SnakePluginList snakePluginList;
    private ConfigContainer cc;

    /**
     * Creates three fake plugins and fourth that will replace one of them
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cc = new ConfigContainer();
        snakePluginList = new SnakePluginList(3, pluginFactory);
        cc.activePluginList = snakePluginList;
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
        assertEquals(3, snakePluginList.getList().size());
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
        assertTrue(snakePluginList.isActive(2)); // default is true
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

    @Test
    public void testBeforeSerialize() throws Exception {
        snakePluginList.beforeSerialize();
        for (int i = 0; i < 3; i++) {
            IQuimpPlugin inst = snakePluginList.getInstance(i);
            assertEquals(inst.getVersion(), snakePluginList.getList().get(i).ver);
            assertEquals(snakePluginList.getInstance(i).getPluginConfig(),
                    snakePluginList.getList().get(i).config);
        }
    }

    @Test
    public void testSaveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        snakePluginList.beforeSerialize();
        LOGGER.trace(gson.toJson(cc));
        FileWriter f = new FileWriter(new File("/tmp/snakePluginList.json"));
        f.write(gson.toJson(cc));
        f.close();
    }

    @Test
    public void testloadConfig() throws IOException {
        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory));
        Gson gson = gsonbuilder.create();
        FileReader f = new FileReader(new File("/tmp/snakePluginList.json"));
        ConfigContainer localcc;
        localcc = gson.fromJson(f, ConfigContainer.class);
        f.close();

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());
        assertFalse(local.isActive(0));
        assertEquals("Test1", local.getList().get(0).name);
        assertEquals("1.2.3", local.getList().get(0).ver);

        // after plugin initialization - restore transient fields
        local.afterdeSerialize();
        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                local.getInstance(1).getPluginConfig());
        assertEquals(snakePluginList.getInstance(2).getPluginConfig(),
                local.getInstance(2).getPluginConfig());
    }

    /**
     * Wrong name of plugin
     * @throws IOException
     */
    @Test
    public void testloadConfig_bad() throws IOException {
        //formatoff
        String json = "{ \"version\": \"3.0.0\","
                + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {"
                + "\"sPluginList\": ["
                + "{"
                    + "\"isActive\": false,"
                    + "\"name\": \"Test10\"," // here wrong name
                    + "\"ver\": \"1.2.3\""
                + "},"
                + "{"
                    + "\"isActive\": true,"
                    + "\"name\": \"Test2\","
                    + "\"config\":"
                    + " {"
                        + "\"window\": \"10\""
                        + ",\"alpha\": \"-0.45\""
                    + "},"
                + "\"ver\": \"2.3.4\"},"
                + "{"
                    + "\"isActive\": true,"
                    + "\"name\": \"toDelete\","
                    + "\"ver\": \"2.3.4\""
                + "}]}}";
        //formaton

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());

        // after plugin initialization - restore transient fields
        local.afterdeSerialize();
        assertEquals(null, snakePluginList.getInstance(0));
    }

}

class ConfigContainer {
    public String version = "3.0.0";
    public String softwareName = "QuimP::BOA";
    public SnakePluginList activePluginList;
}

class SnakePluginListInstanceCreator implements InstanceCreator<SnakePluginList> {

    private int size;
    private PluginFactory pf;

    public SnakePluginListInstanceCreator(int size, PluginFactory pf) {
        this.size = size;
        this.pf = pf;
    }

    @Override
    public SnakePluginList createInstance(Type arg0) {
        return new SnakePluginList(size, pf);
    }

}
