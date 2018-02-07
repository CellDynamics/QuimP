package com.github.celldynamics.quimp.plugin;

import ij.process.ImageProcessor;

/**
 * Allow to attach image to filter.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPluginAttachImage {

  /**
   * Attach image to filter.
   * 
   * @param img image to attach.
   */
  public void attachImage(ImageProcessor img);

}
