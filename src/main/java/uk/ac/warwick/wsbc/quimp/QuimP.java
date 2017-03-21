package uk.ac.warwick.wsbc.quimp;

import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/**
 * Static definitions.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimP {
  /**
   * Suffix added to every preference entry.
   */
  public static final String QUIMP_PREFS_SUFFIX = "QUIMP";
  /**
   * Desired length of line in message box.
   */
  public static final int LINE_WRAP = 60;

  /**
   * Quimp package version taken from jar.
   */
  public static final QuimpVersion TOOL_VERSION = new QuimpToolsCollection().getQuimPBuildInfo();

  /**
   * Switch on/off additional debug.
   * 
   * <p>This switch causes that additional debug information can be produced.
   */
  public static boolean SUPER_DEBUG =
          Boolean.parseBoolean(System.getProperty("quimpconfig.superDebug"));
  /**
   * This field keeps localisation of -quimp plugins.
   * 
   * <p>By default it is Fiji.app/plugins folder but it can be overwritten by setting system
   * property.
   */
  public static String PLUGIN_DIR = System.getProperty("quimpconfig.pluginDirectory");
  /**
   * This field is used for sharing information between bar and other plugins.
   * 
   * <p>It is read by {@link uk.ac.warwick.wsbc.quimp.filesystem.QuimpConfigFilefilter} which is
   * used by {@link uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader} for serving
   * {@link uk.ac.warwick.wsbc.quimp.QParams} object for client.
   */
  public static boolean newFileFormat = true;
}
