package com.github.celldynamics.quimp;

import java.nio.file.Paths;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.engine.PluginFactory;

/**
 * This class create instance of PluginFactory
 *
 * @author p.baniukiewicz
 *
 */
public class PluginFactoryFactory {
  private static final PluginFactoryFactory instance = new PluginFactoryFactory();

  /**
   * 
   */
  public PluginFactoryFactory() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @return PluginFactory instance
   */
  public static PluginFactoryFactory getInstance() {
    return instance;
  }

  /**
   * Provide mocked PluginFactory object that uses sources of plugins available on path
   * 
   * @param path
   * @return mocked PluginFactory object
   * @throws QuimpPluginException
   */
  public static PluginFactory getPluginFactory(String path) throws QuimpPluginException {
    return new PluginFactory(Paths.get(path));

  }
}
