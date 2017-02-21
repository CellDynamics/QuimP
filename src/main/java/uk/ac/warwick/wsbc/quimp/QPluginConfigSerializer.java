package uk.ac.warwick.wsbc.quimp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;

// TODO: Auto-generated Javadoc
/**
 * Serves save and load operations for plugin configuration. Saved is current plugin stack with
 * plugins settings.
 * 
 * This class adds additional layer with data to saved plugin configuration.
 * 
 * @author p.baniukiewicz
 */
@Deprecated
public class QPluginConfigSerializer {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QPluginConfigSerializer.class.getName());
  private transient GsonBuilder gsonbuilder;
  // Definition of top layer data saved
  @SuppressWarnings("unused")
  private String[] version; /*!< Version of QuimP added to top layer */

  /**
   * The software name.
   */
  public final String softwareName = "QuimP"; /*!< Name of the software */

  /**
   * The active plugin list.
   */
  public SnakePluginList activePluginList; /*!< Plugin configurations */

  /**
   * Main constructor
   * 
   * @param version Any information that reference current version of software
   * @param sp Data to save
   */
  public QPluginConfigSerializer(String[] version, SnakePluginList sp) {
    this.version = version;
    this.activePluginList = sp;
    gsonbuilder = new GsonBuilder();
  }

  /**
   * Do everything before save
   */
  private void beforeSave() {
    activePluginList.beforeSerialize();
  }

  /**
   * Do everything after load
   * 
   * @throws QuimpPluginException
   */
  private void afterLoad() throws QuimpPluginException {
    activePluginList.afterSerialize();
  }

  /**
   * Saves configuration packed with QPluginConfig class
   * 
   * @param filename Name of the file to save configuration
   * @throws FileNotFoundException
   */
  public void save(String filename) throws FileNotFoundException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    beforeSave();
    LOGGER.debug("Saving at: " + filename);
    LOGGER.debug(gson.toJson(this));
    PrintWriter f;
    f = new PrintWriter(new File(filename));
    gson.toJson(this, f);
    f.close();
  }

  /**
   * Loads QPluginConfigSerializer class instance from json file.
   * 
   * If any of underlying class requires special builder it should be provided before calling \c
   * load method by getBuilder().
   * 
   * @param filename File to load
   * @throws IOException
   * @return New object of QPluginConfigSerializer with values read from file \c filename
   * @throws QuimpPluginException
   * @see #getBuilder()
   */
  public QPluginConfigSerializer load(String filename) throws IOException, QuimpPluginException {
    Gson gson = gsonbuilder.create();
    FileReader f = new FileReader(new File(filename));
    QPluginConfigSerializer localref;
    localref = gson.fromJson(f, QPluginConfigSerializer.class);
    f.close();
    localref.afterLoad(); // rebuild objects
    return localref;
  }

  /**
   * Get GsonBuilder for registering constructors if necessary
   * 
   * @return gsonbuilder
   */
  public GsonBuilder getBuilder() {
    gsonbuilder = new GsonBuilder();
    return gsonbuilder;
  }

  /**
   * Returns created object after loading
   * 
   * @return SnakePluginList
   */
  public SnakePluginList getSnakePluginList() {
    return activePluginList;
  }

}