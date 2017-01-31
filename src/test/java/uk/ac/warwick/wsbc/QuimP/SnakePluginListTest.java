/**
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

// TODO: Auto-generated Javadoc
/**
 * Tests of SnakePluginList class and serialization
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SnakePluginListTest {

    /**
     * The tmpdir.
     */
    static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

    /**
     * Accessor to private fields.
     * 
     * @param name Name of private method
     * @param ref Object
     * @param obj
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    static void accessPrivate(String name, Class<SnakePluginList> ref, SnakePluginList obj)
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method prv = ref.getDeclaredMethod(name, (Class[]) null);
        prv.setAccessible(true);
        prv.invoke(obj, (Object[]) null);
    }

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SnakePluginListTest.class.getName());
    @Mock
    private PluginFactory pluginFactory;

    private SnakePluginList snakePluginList;
    private ConfigContainer cc;
    private String[] version;

    /**
     * Creates three fake plugins and fourth that will replace one of them
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cc = new ConfigContainer();
        version = new String[3];
        version[0] = "0.0.1";
        version[1] = "p.baniukiewicz";
        version[2] = "QuimP";
        snakePluginList = new SnakePluginList(3, pluginFactory, null);
        cc.activePluginList = snakePluginList;
        /**
         * This plugin does not have config
         */
        Mockito.when(pluginFactory.getInstance("Test1")).thenReturn(new IQuimpCorePlugin() {

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

            @Override
            public String about() {
                return "Test_1";
            }
        });
        /**
         * This has config
         */
        Mockito.when(pluginFactory.getInstance("Test2")).thenReturn(new IQuimpCorePlugin() {

            @Override
            public void showUI(boolean val) {
            }

            @Override
            public int setup() {
                return 0;
            }

            @Override
            public void setPluginConfig(ParamList par) throws QuimpPluginException {
                try {
                    @SuppressWarnings("unused")
                    int window = par.getIntValue("window");
                } catch (Exception e) {
                    throw new QuimpPluginException("Wrong input argument->" + e.getMessage(), e);
                }
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

            @Override
            public String about() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        /**
         * This is for testing deletions
         */
        Mockito.when(pluginFactory.getInstance("toDelete")).thenReturn(new IQuimpCorePlugin() {

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

            @Override
            public String about() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        /**
         * This will replace plugin 0
         */
        Mockito.when(pluginFactory.getInstance("newInstance")).thenReturn(new IQuimpCorePlugin() {

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

            @Override
            public String about() {
                // TODO Auto-generated method stub
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
     * {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#SnakePluginList(int, PluginFactory, ViewUpdater)}
     * 
     * @throws Exception
     */
    @Test
    public void testSnakePluginListIntPluginFactory() throws Exception {
        assertEquals(3, snakePluginList.getList().size());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#getInstance(int)}.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetInstance() throws Exception {
        IQuimpCorePlugin inst = snakePluginList.getInstance(0);
        assertEquals("1.2.3", inst.getVersion());
        assertEquals("Test_1", inst.about());
        inst = snakePluginList.getInstance(1);
        assertEquals("2.3.4", inst.getVersion());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#isActive(int)}.
     * 
     * @throws Exception
     */
    @Test
    public void testIsActive() throws Exception {
        assertFalse(snakePluginList.isActive(0));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#setInstance(int, String, boolean)}.
     * 
     * @throws Exception
     */
    @Test
    public void testSetInstance() throws Exception {
        IQuimpCorePlugin inst = snakePluginList.getInstance(0);
        assertEquals("1.2.3", inst.getVersion());
        snakePluginList.setInstance(0, "newInstance", true); // slot 0
        inst = snakePluginList.getInstance(0);
        assertEquals("0.0.1", inst.getVersion());
        assertTrue(snakePluginList.isActive(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#setActive(int, boolean)}.
     * 
     * @throws Exception
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
     * 
     * @throws Exception
     */
    @Test
    public void testDeletePlugin() throws Exception {
        snakePluginList.deletePlugin(2);
        assertEquals(null, snakePluginList.getInstance(2));
        assertTrue(snakePluginList.isActive(2)); // default is true
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.SnakePluginList#isRefListEmpty()}.
     * 
     * @throws Exception
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

    /**
     * Test of serialization
     * 
     * @throws Exception
     */
    @Test
    public void testBeforeSerialize() throws Exception {
        snakePluginList.beforeSerialize();
        for (int i = 0; i < 3; i++) {
            IQuimpCorePlugin inst = snakePluginList.getInstance(i);
            assertEquals(inst.getVersion(), snakePluginList.getVer(i));
            assertEquals(snakePluginList.getInstance(i).getPluginConfig(),
                    snakePluginList.getConfig(i));
        }
    }

    /**
     * Test of saving.
     * 
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void testSaveConfig() throws IOException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        snakePluginList.beforeSerialize();
        LOGGER.trace(gson.toJson(cc));
        FileWriter f = new FileWriter(new File(tmpdir + "snakePluginList.json"));
        f.write(gson.toJson(cc));
        f.close();
    }

    /**
     * Test of serialization
     * 
     * @throws FileNotFoundException
     */
    @Test
    public void testSaveConfig_serializer() throws FileNotFoundException {
        Serializer<SnakePluginList> s = new Serializer<>(snakePluginList, version);
        s.setPretty();
        s.save(tmpdir + "snakePluginList_serializer.json");
        LOGGER.trace(s.toString());
    }

    /**
     * Test of serialization
     * 
     * @throws FileNotFoundException
     */
    @Test
    public void testSaveConfig_serializer1() throws FileNotFoundException {
        Serializer<SnakePluginList> s = new Serializer<>(snakePluginList, version);
        LOGGER.trace(s.toString());
    }

    /**
     * Test of saving.
     * 
     * Pre: There is gap in plugin list
     * 
     * Post: Empty slot is saved with empty name.
     * 
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws QuimpPluginException
     */
    @Test
    public void testSaveConfig_gap()
            throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, QuimpPluginException {
        ConfigContainer localcc = new ConfigContainer();
        SnakePluginList localsnakePluginList = new SnakePluginList(3, pluginFactory, null);
        localcc.activePluginList = localsnakePluginList;
        localsnakePluginList.setInstance(0, "Test1", false); // slot 0
        localsnakePluginList.setInstance(2, "toDelete", true); // slot 2

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        snakePluginList.beforeSerialize();
        LOGGER.trace(gson.toJson(localcc));
        // FileWriter f = new FileWriter(new File("/tmp/snakePluginList.json"));
        // f.write(gson.toJson(cc));
        // f.close();
    }

    /**
     * Test of loading
     * 
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig()
            throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, QuimpPluginException {
        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        FileReader f = new FileReader(new File(tmpdir + "snakePluginList.json"));
        ConfigContainer localcc;
        localcc = gson.fromJson(f, ConfigContainer.class);
        f.close();

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());
        assertFalse(local.isActive(0));
        assertEquals("Test1", local.getName(0));
        assertEquals("1.2.3", local.getVer(0));

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                local.getInstance(1).getPluginConfig());
        assertEquals(snakePluginList.getInstance(2).getPluginConfig(),
                local.getInstance(2).getPluginConfig());
    }

    /**
     * Test of serialization
     * 
     * @throws Exception
     */
    @Test
    public void testloadConfig_serializer() throws Exception {
        //!<
        String json = "{\"className\":\"SnakePluginList\","
                + "\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],"
                + "\"createdOn\": \"Wed 2016.06.15 at 09:30:48 AM BST\","
                + "\"obj\":{\"sPluginList\":"
                + "[{\"isActive\":false,\"name\":\"Test1\",\"ver\":\"1.2.3\"},"
                + "{\"isActive\":true,\"name\":\"Test2\",\"config\":"
                + "{\"window\":\"10\",\"alpha\":\"-0.45\"},\"ver\":\"2.3.4\"},"
                + "{\"isActive\":true,\"name\":\"toDelete\",\"ver\":\"2.3.4\"}]}}";
        // */

        Serializer<SnakePluginList> out;
        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
        s.registerInstanceCreator(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        out = s.fromString(json);

        assertArrayEquals(out.version, version);

        assertEquals(3, out.obj.getList().size());
        assertFalse(out.obj.isActive(0));
        assertEquals("Test1", out.obj.getName(0));

        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                out.obj.getInstance(1).getPluginConfig());
        assertEquals(snakePluginList.getInstance(0).getVersion(),
                out.obj.getInstance(0).getVersion());
        assertEquals(snakePluginList.getInstance(2).getPluginConfig(),
                out.obj.getInstance(2).getPluginConfig());
    }

    /**
     * Try to load config where is more than one json structure
     * 
     * @throws QuimpPluginException
     */
    @Test
    @Ignore("Does not work - two json in one file")
    public void testloadConfig_1() throws QuimpPluginException {
        //!<
        String json = "{}" + "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test1\"," + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true,"
                + "\"name\": \"Test2\"," + "\"config\":" + " {" + "\"window\": \"10\""
                + ",\"alpha\": \"-0.45\"" + "}," + "\"ver\": \"2.3.4\"}," + "{"
                + "\"isActive\": true," + "\"name\": \"toDelete\"," + "\"ver\": \"2.3.4\"" + "}]}}";
        // */

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());
        assertFalse(local.isActive(0));
        assertEquals("Test1", local.getName(0));
        assertEquals("1.2.3", local.getVer(0));

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                local.getInstance(1).getPluginConfig());
        assertEquals(snakePluginList.getInstance(2).getPluginConfig(),
                local.getInstance(2).getPluginConfig());
    }

    /**
     * Only one plugin in middle
     * 
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_2() throws QuimpPluginException {
        //!<
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"\"," + "\"ver\": \"\"" + "}," + "{" + "\"isActive\": true,"
                + "\"name\": \"Test2\"," + "\"config\":" + " {" + "\"window\": \"10\""
                + ",\"alpha\": \"-0.45\"" + "}," + "\"ver\": \"2.3.4\"}," + "{"
                + "\"isActive\": true," + "\"name\": \"\"," + "\"ver\": \"\"" + "}]}}";
        // */

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());
        assertFalse(local.isActive(0));
        assertEquals("", local.getName(0));
        assertEquals("", local.getVer(0));

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                local.getInstance(1).getPluginConfig());

    }

    /**
     * Only one plugin in middle
     * 
     * @throws Exception
     */
    @Test
    public void testloadConfig_serializer_2() throws Exception {
        //!<
        String json = "{\"className\":\"SnakePluginList\","
                + "\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],"
                + "\"createdOn\": \"Wed 2016.06.15 at 09:30:48 AM BST\","
                + "\"obj\":{\"sPluginList\":" + "[{\"isActive\":false,\"name\":\"\",\"ver\":\"\"},"
                + "{\"isActive\":true,\"name\":\"Test2\",\"config\":"
                + "{\"window\":\"10\",\"alpha\":\"-0.45\"},\"ver\":\"2.3.4\"},"
                + "{\"isActive\":true,\"name\":\"\",\"ver\":\"\"}]}}";
        // */

        Serializer<SnakePluginList> out;
        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
        s.registerInstanceCreator(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        out = s.fromString(json);

        assertArrayEquals(out.version, version);

        assertEquals(3, out.obj.getList().size());
        assertTrue(out.obj.isActive(0));
        assertEquals("", out.obj.getName(0));

        assertEquals(snakePluginList.getInstance(1).getPluginConfig(),
                out.obj.getInstance(1).getPluginConfig());
        assertEquals(null, out.obj.getInstance(0));
        assertEquals(null, out.obj.getInstance(2));
    }

    /**
     * Test of loading not complete file
     * 
     * Pre: Wrong name of plugin in config
     * 
     * Post: This slot is null
     * 
     * @throws IOException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_bad() throws IOException, QuimpPluginException {
        //!<
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test10\"," // here wrong name
                + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true," + "\"name\": \"Test2\","
                + "\"config\":" + " {" + "\"window\": \"10\"" + ",\"alpha\": \"-0.45\"" + "},"
                + "\"ver\": \"2.3.4\"}," + "{" + "\"isActive\": true," + "\"name\": \"toDelete\","
                + "\"ver\": \"2.3.4\"" + "}]}}";
        // */

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());

        // after plugin initialization - restore transient fields

        local.afterSerialize();

        assertEquals(null, local.getInstance(0));
        assertEquals(snakePluginList.getInstance(1), local.getInstance(1));
        assertEquals(snakePluginList.getInstance(2), local.getInstance(2));
    }

    /**
     * Test of loading not bad field in file
     * 
     * Pre: Wrong name of plugin in config
     * 
     * Post: This slot is null
     * 
     * @throws Exception
     */
    @Test
    public void testloadConfig_serializer_bad() throws Exception {
        String json = "{\"className\":\"SnakePluginList\","
                + "\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],"
                + "\"createdOn\": \"Wed 2016.06.15 at 09:30:48 AM BST\","
                + "\"obj\":{\"sPluginList\":"
                + "[{\"isActive\":false,\"name\":\"Test10\",\"ver\":\"1.2.3\"},"
                + "{\"isActive\":true,\"name\":\"Test2\",\"config\":"
                + "{\"window\":\"10\",\"alpha\":\"-0.45\"},\"ver\":\"2.3.4\"},"
                + "{\"isActive\":true,\"name\":\"toDelete\",\"ver\":\"2.3.4\"}]}}";

        Serializer<SnakePluginList> out;
        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
        s.registerInstanceCreator(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));

        out = s.fromString(json);
        assertEquals(null, out.obj.getInstance(0));
        assertEquals(snakePluginList.getInstance(1), out.obj.getInstance(1));
        assertEquals(snakePluginList.getInstance(2), out.obj.getInstance(2));

    }

    /**
     * Test of loading plugin config with wrong version
     * 
     * pre: Incorrect version
     * 
     * post: Plugin loaded with message
     * 
     * @throws IOException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_bad1() throws IOException, QuimpPluginException {
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test1\"," + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true,"
                + "\"name\": \"Test2\"," + "\"config\":" + " {" + "\"window\": \"10\""
                + ",\"alpha\": \"-0.45\"" + "}," + "\"ver\": \"20.3.4\"}," // here wrong name
                + "{" + "\"isActive\": true," + "\"name\": \"toDelete\"," + "\"ver\": \"2.3.4\""
                + "}]}}";

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals("2.3.4", local.getInstance(1).getVersion());
    }

    /**
     * Test of loading plugin config with wrong version
     * 
     * pre: Incorrect version
     * 
     * post: Plugin loaded with message
     * 
     * @throws Exception
     */
    @Test
    public void testloadConfig_serializer_bad1() throws Exception {
        String json = "{\"className\":\"SnakePluginList\","
                + "\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],"
                + "\"createdOn\": \"Wed 2016.06.15 at 09:30:48 AM BST\","
                + "\"obj\":{\"sPluginList\":"
                + "[{\"isActive\":false,\"name\":\"Test1\",\"ver\":\"1.2.3\"},"
                + "{\"isActive\":true,\"name\":\"Test2\",\"config\":"
                + "{\"window\":\"10\",\"alpha\":\"-0.45\"},\"ver\":\"20.3.4\"},"
                + "{\"isActive\":true,\"name\":\"toDelete\",\"ver\":\"2.3.4\"}]}}";

        Serializer<SnakePluginList> out;
        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
        s.registerInstanceCreator(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));

        out = s.fromString(json);
        assertEquals("2.3.4", out.obj.getInstance(1).getVersion());
        assertEquals(snakePluginList.getInstance(0), out.obj.getInstance(0));
        assertEquals(snakePluginList.getInstance(1), out.obj.getInstance(1));
        assertEquals(snakePluginList.getInstance(2), out.obj.getInstance(2));
    }

    /**
     * Test of loading incompatibile config
     * 
     * This depends on plugin configuration. Wrong config is detected by exception thrown from
     * setPluginConfig() from IQuimpPlugin
     * 
     * pre: Incompatibile config
     * 
     * post: Plugin loaded but config not restored
     * 
     * @throws IOException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_bad2() throws IOException, QuimpPluginException {
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test1\"," + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true,"
                + "\"name\": \"Test2\"," + "\"config\":" + " {" + "\"window10\": \"5\"" // here
                                                                                        // wrong
                                                                                        // name
                + ",\"alpha\": \"-0.45\"" + "}," + "\"ver\": \"2.3.4\"}," + "{"
                + "\"isActive\": true," + "\"name\": \"toDelete\"," + "\"ver\": \"2.3.4\"" + "}]}}";

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());

        // after plugin initialization - restore transient fields

        local.afterSerialize();

        assertEquals(null, local.getInstance(1));
        assertEquals(snakePluginList.getInstance(0), local.getInstance(0));
    }

    /**
     * Test of loading incompatibile config.
     * 
     * This depends on plugin configuration. Wrong config is detected by exception thrown from
     * setPluginConfig() from IQuimpPlugin.
     * 
     * Pre: Incompatibile config
     * 
     * Post: Plugin loaded but config not restored
     * 
     * @throws Exception
     */
    @Test
    public void testloadConfig_serializer_bad2() throws Exception {
        String json = "{\"className\":\"SnakePluginList\","
                + "\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],"
                + "\"createdOn\": \"Wed 2016.06.15 at 09:30:48 AM BST\","
                + "\"obj\":{\"sPluginList\":"
                + "[{\"isActive\":false,\"name\":\"Test1\",\"ver\":\"1.2.3\"},"
                + "{\"isActive\":true,\"name\":\"Test2\",\"config\":"
                + "{\"window10\":\"10\",\"alpha\":\"-0.45\"},\"ver\":\"2.3.4\"},"
                + "{\"isActive\":true,\"name\":\"toDelete\",\"ver\":\"2.3.4\"}]}}";

        Serializer<SnakePluginList> out;
        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
        s.registerInstanceCreator(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));

        out = s.fromString(json);

        assertEquals(snakePluginList.getInstance(0), out.obj.getInstance(0));
        assertEquals(snakePluginList.getInstance(2), out.obj.getInstance(2));
        assertEquals(null, out.obj.getInstance(1));

    }

    /**
     * Test of loading incompatible config for plugin numbers
     * 
     * This situation must be detected on load and reported
     * 
     * pre: Less plugins
     * 
     * post: List is adjusted
     * 
     * @throws IOException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_bad3() throws IOException, QuimpPluginException {
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test1\"," + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true,"
                + "\"name\": \"toDelete\"," + "\"ver\": \"2.3.4\"" + "}]}}";

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(2, local.getList().size());

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals("1.2.3", local.getInstance(0).getVersion());
        assertEquals("2.3.4", local.getInstance(1).getVersion());
    }

    /**
     * Test of loading incompatible config - empty slot
     * 
     * pre: Empty slot
     * 
     * post: Correct order of plugins
     * 
     * @throws IOException
     * @throws QuimpPluginException
     */
    @Test
    public void testloadConfig_bad4() throws IOException, QuimpPluginException {
        String json = "{ \"version\": \"3.0.0\"," + "\"softwareName\": \"QuimP::BOA\","
                + " \"activePluginList\": {" + "\"sPluginList\": [" + "{" + "\"isActive\": false,"
                + "\"name\": \"Test1\"," + "\"ver\": \"1.2.3\"" + "}," + "{" + "\"isActive\": true," // see
                                                                                                     // testSaveConfig_gap
                + "\"name\": \"\"," + "\"ver\": \"\"}," + "{" + "\"isActive\": true,"
                + "\"name\": \"toDelete\"," + "\"ver\": \"2.3.4\"" + "}]}}";

        GsonBuilder gsonbuilder = new GsonBuilder();
        // http: //
        // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
        gsonbuilder.registerTypeAdapter(SnakePluginList.class,
                new SnakePluginListInstanceCreator(3, pluginFactory, null));
        Gson gson = gsonbuilder.create();
        ConfigContainer localcc;
        localcc = gson.fromJson(json, ConfigContainer.class);

        // test fields that exists without initialization of plugins
        SnakePluginList local = localcc.activePluginList; // newly created class
        assertEquals(3, local.getList().size());

        // after plugin initialization - restore transient fields
        local.afterSerialize();
        assertEquals("1.2.3", local.getInstance(0).getVersion());
        assertEquals(null, local.getInstance(1));
        assertEquals("2.3.4", local.getInstance(2).getVersion());
    }

}

class ConfigContainer {
    public String version = "3.0.0";
    public String softwareName = "QuimP::BOA";
    public SnakePluginList activePluginList;
}
