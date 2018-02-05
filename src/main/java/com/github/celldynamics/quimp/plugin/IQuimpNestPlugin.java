package com.github.celldynamics.quimp.plugin;

/**
 * This core plugin accepts Nest as input and can modify it.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpNestPlugin extends IQuimpCorePlugin, IQuimpPluginAttachNest {

  /**
   * Runs filter and modify Nest object.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Plugin may be run without attached data. Plugin must deal with this
   * 
   * @throws QuimpPluginException on any problems during filter execution
   */
  void runPlugin() throws QuimpPluginException;

}
