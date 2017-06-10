package com.github.celldynamics.quimp.plugin;

import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPluginAttachImage {

  /**
   * @param img
   */
  public void attachImage(ImageProcessor img);
}
