package com.github.celldynamics.quimp.plugin.engine;

import java.io.File;

/**
 * Store basic plugin properties read from jar file.
 * 
 * @author p.baniukiewicz
 *
 */
public class PluginProperties {
  private File file; // handle to file on disk
  private int type; // type of plugin
  private String className; // name of plugin class
  private String version; // version returned from plugin

  /**
   * Version getter.
   * 
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * getClassName.
   * 
   * @return the className
   */
  public String getClassName() {
    return className;
  }

  /**
   * Construct plugin properties object.
   * 
   * @param file Reference to plugin file
   * @param className Qualified class name of plugin
   * @param type Type of plugin returned by IQuimpPlugin.setup() method
   * @param version Version of plugin returned from IQuimpPlugin.getVersion() method
   */
  PluginProperties(final File file, final String className, int type, final String version) {
    this.file = file;
    this.type = type;
    this.className = className;
    if (version == null) {
      this.version = "";
    } else {
      this.version = version;
    }
  }

  /**
   * File getter.
   * 
   * @return File object
   */
  public File getFile() {
    return file;
  }

  /**
   * Type getter.
   * 
   * @return Type of File plugin
   */
  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return "ClassName: " + className + " path: " + file + " type: " + type + " ver: " + version;
  }
}