package com.github.celldynamics.quimp;

import java.nio.file.Paths;

import com.github.celldynamics.quimp.plugin.engine.PluginFactory;

// TODO: Auto-generated Javadoc
/**
 * This class create instance of PluginFactory.
 *
 * @author p.baniukiewicz
 */
public class PluginFactoryFactory {

  /** The Constant instance. */
  private static final PluginFactoryFactory instance = new PluginFactoryFactory();

  /**
   * Main constructor.
   */
  public PluginFactoryFactory() {
  }

  /**
   * Get instance of PluginFactory.
   * 
   * @return PluginFactory instance
   */
  public static PluginFactoryFactory getInstance() {
    return instance;
  }

  /**
   * Provide mocked PluginFactory object that uses sources of plugins available on path.
   * 
   * @param path plugin folder
   * @return mocked PluginFactory object
   */
  public static PluginFactory getPluginFactory(String path) {
    return new PluginFactory(Paths.get(path));

  }
}
