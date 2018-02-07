package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

/**
 * Allow to attach upload and download configuration from plugin.
 * 
 * @author p.baniukiewicz
 * @see QWindowBuilder
 * @see ParamList
 */
public interface IQuimpPluginExchangeData {

  /**
   * Pass to plugin its configuration data as pairs (key,value).
   * 
   * <p>This method is used for restoring configuration from config files maintained by caller.
   * Caller do not modify these values and only stores them as the were returned by
   * getPluginConfig() method.
   * 
   * <p>Numerical values should be passed as Double. setPluginConfig(ParamList) and
   * getPluginConfig() should use the same convention of key naming
   * and parameters casting
   * 
   * @param par plugin configuration.
   * @throws QuimpPluginException on problems with understanding parameters by plugin e.g. \b key
   *         is not understood or converting from \c String \b value to other type has not been
   *         successful.
   */
  void setPluginConfig(final ParamList par) throws QuimpPluginException;

  /**
   * Retrieve plugin configuration data as pairs (key,value>=).
   * 
   * <p>This configuration is not used by QuimP but it may be stored in QuimP configuration.
   * 
   * @return Plugin configuration
   * @see #setPluginConfig(ParamList)
   */
  ParamList getPluginConfig();
}
