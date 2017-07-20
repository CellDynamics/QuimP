package com.github.celldynamics.quimp.filesystem;

import java.lang.reflect.Type;

import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.plugin.engine.PluginFactory;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

/**
 * Object builder for GSon and DataContainer class.
 * 
 * <p>This class is used on load JSon representation of DataContainer class. Rebuilds
 * snakePluginList
 * field that is not serialized. This field keeps current state of plugins.
 * 
 * @author p.baniukiewicz
 * @see Gson
 */
public class DataContainerInstanceCreator implements InstanceCreator<DataContainer> {

  private PluginFactory pf;
  private ViewUpdater vu;

  /**
   * Assign additional fields to DataContainer after serialization.
   * 
   * @param pf PluginFactory
   * @param vu ViewUpdater
   */
  public DataContainerInstanceCreator(final PluginFactory pf, final ViewUpdater vu) {
    this.pf = pf;
    this.vu = vu;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gson.InstanceCreator#createInstance(java.lang.reflect.Type)
   */
  @Override
  public DataContainer createInstance(Type arg0) {
    DataContainer dt = new DataContainer(pf, vu);
    return dt;
  }
}