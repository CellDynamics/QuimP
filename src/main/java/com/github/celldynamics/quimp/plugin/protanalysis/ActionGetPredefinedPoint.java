package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;

import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.GradientType;

/**
 * Update reference point from predefined settings.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionGetPredefinedPoint extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionGetPredefinedPoint(String name, String desc, CustomStackWindow ui) {
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
    JComboBox<?> cmp = ((JComboBox<?>) e.getSource());
    GradientType item = (GradientType) cmp.getSelectedItem();
    switch (item) {
      case LB_CORNER:
        options.gradientPoint = new Point2d(0, 0);
        break;
      case LU_CORNER:
        options.gradientPoint = new Point2d(0, model.getImage().getHeight() - 1);
        break;
      case RB_CORNER:
        options.gradientPoint = new Point2d(model.getImage().getWidth() - 1, 0);
        break;
      case RU_CORNER:
        options.gradientPoint =
                new Point2d(model.getImage().getWidth() - 1, model.getImage().getHeight() - 1);
        break;
      default:
        throw new IllegalArgumentException("Wrong gradient reference point.");
    }
    ui.updateStaticFields();
  }

}
