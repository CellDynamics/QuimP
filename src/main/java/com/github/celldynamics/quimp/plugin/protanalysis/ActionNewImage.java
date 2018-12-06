package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

/**
 * Update options if configuration option "new image" is selected.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionNewImage extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionNewImage(String name, String desc, CustomStackWindow ui) {
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
    ProtAnalysisOptions options = (ProtAnalysisOptions) ui.getModel().getOptions();
    options.guiNewImage = ((JCheckBox) e.getSource()).isSelected();
  }

}
