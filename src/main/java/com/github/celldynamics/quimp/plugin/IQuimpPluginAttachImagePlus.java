package com.github.celldynamics.quimp.plugin;

import ij.ImagePlus;

/**
 * Allow to attach image plus to filter.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPluginAttachImagePlus extends IQuimpPluginAttachImage {

  /**
   * Attach image plus to filter.
   * 
   * @param img image to attach.
   */
  public void attachImagePlus(ImagePlus img);
}
