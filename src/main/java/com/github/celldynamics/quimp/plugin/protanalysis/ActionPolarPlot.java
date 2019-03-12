package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

/**
 * Action for generating polar plots for selected point.
 * 
 * <p>Read:
 * <ol>
 * <li>{@link ProtAnalysisOptions#gradientPoint}
 * </ol>
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionPolarPlot extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionPolarPlot(String name, String desc, CustomStackWindow ui) {
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
    // TODO Auto-generated method stub

  }

}
