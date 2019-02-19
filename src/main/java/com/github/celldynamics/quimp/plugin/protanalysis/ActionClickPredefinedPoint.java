package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

/**
 * Set mode for {@link CustomCanvas} to get reference point only.
 * 
 * <p>Mode is reseted by {@link CustomCanvas#mousePressed(java.awt.event.MouseEvent)}
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionClickPredefinedPoint extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionClickPredefinedPoint(String name, String desc, CustomStackWindow ui) {
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
    JToggleButton src = (JToggleButton) e.getSource();
    if (src.isSelected()) {
      options.gradientPickActive.setTrue();
    } else {
      options.gradientPickActive.setFalse();
    }
    logger.trace(ui.getModel().getOptions().serialize());

  }
}
