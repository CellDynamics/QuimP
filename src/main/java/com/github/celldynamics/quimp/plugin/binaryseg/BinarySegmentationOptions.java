package com.github.celldynamics.quimp.plugin.binaryseg;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.EscapedPath;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

/**
 * Hold options for Binary Segmentation.
 * 
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentationOptions extends AbstractPluginOptions {

  /**
   * Plugin options in format of QWindowBuilder.
   * 
   * @see QWindowBuilder
   * @see BinarySegmentationView
   * @see BinarySegmentationView#SELECT_IMAGE
   * @see BinarySegmentationView#NAME
   * @see BinarySegmentationView#LOAD_MASK
   * @see BinarySegmentationView#CLEAR_NEST
   * @see BinarySegmentationView#RESTORE_SNAKE
   * @see BinarySegmentationView#STEP2
   */
  public ParamList options;

  /**
   * Keep file name of mask if loaded by Load button.
   * 
   * <p>Otherwise plugin looks into {@value BinarySegmentationView#SELECT_IMAGE} field in
   * Gui.
   */
  @EscapedPath
  public String maskFileName = "";

  /**
   * Path where to save QCONF.
   */
  @EscapedPath
  public String outputPath = "";

  /**
   * Reference image.
   * 
   * <p>This should be path to image that has been segmented. Required for producing valid Qconf. It
   * is not filled by GUI!
   */
  @EscapedPath
  public String originalImage = "";

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    BinarySegmentationOptions ret = new BinarySegmentationOptions();
    ret.options = new ParamList(this.options);
    ret.maskFileName = this.maskFileName;
    ret.originalImage = this.originalImage;
    ret.outputPath = outputPath;
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BinarySegmentationOptions [options=" + options + "]";
  }

}
