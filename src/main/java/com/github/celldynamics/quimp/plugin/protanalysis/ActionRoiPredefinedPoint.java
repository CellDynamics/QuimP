package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.QuimpException;

import ij.gui.Roi;

/**
 * Get reference point from current roi.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionRoiPredefinedPoint extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionRoiPredefinedPoint(String name, String desc, ProtAnalysisUi ui) {
    super(name, desc, ui);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisAbstractAction#actionPerformed(
   * java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      Roi roi = model.getImage().getRoi();
      if (roi == null) {
        throw new QuimpException("No selection in the image", model.getSink());
      }
      options.gradientPoint = new Point2d(roi.getXBase(), roi.getYBase());
      model.getGui().updateStaticFields();
      model.getImage().deleteRoi();
    } catch (QuimpException e1) {
      e1.handleException(null, "Cannot access ROI.");
    }
  }

}
