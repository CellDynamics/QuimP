package com.github.celldynamics.quimp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.IQuimpCorePlugin;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

// TODO: Auto-generated Javadoc
/**
 * Test case of GSon, not related with any class in project.
 *
 * @author pl.baniukiewicz
 */
public class ConfigStreamerTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ConfigStreamerTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /** The pl. */
  private tSnakePluginList pl;

  /** The cc. */
  private ConfigContainer1 cc;

  /**
   * Setup.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    ParamList config = new ParamList();
    ParamList config1 = new ParamList();
    config.put("window", "10");
    config.put("alpha", "0.02");
    config.put("Beta", "-0.32");
    config1.put("window", "30");
    config1.put("alpha", "0.0");
    config1.put("Beta", "0.32");

    cc = new ConfigContainer1();
    pl = new tSnakePluginList(2);

    pl.snakePluginList.get(0).name = "Plugin1_quimp";
    pl.snakePluginList.get(0).ver = "0.01";
    pl.snakePluginList.get(0).config = config;
    pl.snakePluginList.get(0).isActive = true;
    pl.snakePluginList.get(0).ref = new IQuimpCorePlugin() {

      @Override
      public int showUi(boolean val) {
        return 0;

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
        return null;
      }

      @Override
      public ParamList getPluginConfig() {
        return null;
      }

      @Override
      public String about() {
        return null;
      }
    };

    pl.snakePluginList.get(1).name = "Plugin2_quimp";
    pl.snakePluginList.get(1).ver = "0.02";
    pl.snakePluginList.get(1).config = config1;
    pl.snakePluginList.get(1).isActive = false;
    pl.snakePluginList.get(1).ref = new IQuimpCorePlugin() {

      @Override
      public int showUi(boolean val) {
        return 0;

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
        return null;
      }

      @Override
      public ParamList getPluginConfig() {
        return null;
      }

      @Override
      public String about() {
        return null;
      }
    };

    cc.activePluginList = pl;

  }

  /**
   * test_create.
   * 
   * @throws IOException IOException
   */
  @Test
  public void test_create() throws IOException {
    Gson gson = new Gson();
    LOGGER.trace(gson.toJson(cc));
    FileWriter f = new FileWriter(new File(tmpdir + "t1.json"));
    f.write(gson.toJson(cc));
    f.close();

  }

  /**
   * test_create_pretty.
   * 
   * @throws IOException IOException
   */
  @Test
  public void test_create_pretty() throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    LOGGER.trace(gson.toJson(cc));
    FileWriter f = new FileWriter(new File(tmpdir + "t2.json"));
    f.write(gson.toJson(cc));
    f.close();
  }

  /**
   * test_load.
   * 
   * @throws IOException IOException
   */
  @Test
  @Ignore
  public void test_load() throws IOException {
    GsonBuilder gsonbuilder = new GsonBuilder();
    // http: //
    // stackoverflow.com/questions/18567719/gson-deserializing-nested-objects-with-instancecreator
    gsonbuilder.registerTypeAdapter(tSnakePluginList.class, new tSnakePluginListInstanceCreator(3));
    Gson gson = gsonbuilder.create();
    FileReader f = new FileReader(new File(tmpdir + "t2.json"));
    tSnakePluginList local;
    // local = new tSnakePluginList(2);
    local = gson.fromJson(f, tSnakePluginList.class);
    f.close();

    assertEquals("Plugin1_quimp", local.snakePluginList.get(0).name);
    assertEquals("0.32", local.snakePluginList.get(1).config.get("Beta"));

  }

  /**
   * test_create_pretty_static.
   * 
   * @throws IOException IOException
   */
  @Test
  @Ignore
  public void test_create_pretty_static() throws IOException {
    GsonBuilder gb = new GsonBuilder();
    gb.setPrettyPrinting();
    gb.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);
    Gson gson = gb.create();
    cc.bp = BOA_.qState;
    LOGGER.trace(gson.toJson(cc));

  }

}

class ConfigContainer1 {
  public String version = "3.0.0";
  public String softwareName = "QuimP::BOA";
  public tSnakePluginList activePluginList;
  public BOAState bp;

}

class tSnakePluginList {

  /**
   * Keeps all Plugin related information.
   * 
   * @author pl.baniukiewicz
   *
   */
  class Plugin {
    public IQuimpCorePlugin ref; //!< Reference to plugin instance
    public boolean isActive; //!< Is activate in GUI?
    public ParamList config;
    public String name;
    public String ver;

    /**
     * Main constructor.
     * 
     * @param ref Instance of plugin
     * @param isActive true if is active
     */
    Plugin(IQuimpCorePlugin ref, boolean isActive) {
      this.ref = ref;
      this.isActive = isActive;
      // ver = ref.getVersion();
    }
  }

  public ArrayList<Plugin> snakePluginList; //!< Holds list of plugins up to max allowed

  /**
   * Main constructor.
   * 
   * @param s Number of supported plugins
   */
  public tSnakePluginList(int s) {
    // Array that keeps references for SPLINE plugins activated by user
    // in QuimP GUI
    // initialize list with null pointers - this is how QuimP detect that
    // there is plugin selected
    snakePluginList = new ArrayList<Plugin>(s);
    for (int i = 0; i < s; i++) {
      snakePluginList.add(new Plugin(null, true));
    }
  }
}

class tSnakePluginListInstanceCreator implements InstanceCreator<tSnakePluginList> {

  private int size;

  public tSnakePluginListInstanceCreator(int size) {
    this.size = size;
  }

  @Override
  public tSnakePluginList createInstance(Type arg0) {
    return new tSnakePluginList(size);
  }

}
