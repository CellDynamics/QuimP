package com.github.celldynamics.quimp;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

/**
 * Static configuration definitions.
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
  public static final boolean SUPER_DEBUG =
          Boolean.parseBoolean(System.getProperty("quimpconfig.superDebug"));
  /**
   * This field keeps localisation of -quimp plugins.
   * 
   * <p>By default it is Fiji.app/plugins folder but it can be overwritten by setting system
   * property.
   */
  public static final String PLUGIN_DIR = System.getProperty("quimpconfig.pluginDirectory");
  /**
   * This field is used for sharing information between bar and other plugins.
   * 
   * <p>It is read by {@link com.github.celldynamics.quimp.filesystem.FileDialogEx} which is
   * used by {@link com.github.celldynamics.quimp.filesystem.QconfLoader} for serving
   * {@link com.github.celldynamics.quimp.QParams} object for client.
   */
  public static final AtomicBoolean newFileFormat = new AtomicBoolean(true);
  /**
   * Maximum distance from snake centroid that triggers action on mouse click.
   */
  public static final int mouseSensitivity = 10;
}
