package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * Create action for updating booleans in options from JCheckboxes.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionUpdateOptionsBoolean extends ProtAnalysisAbstractAction {

  private MutableBoolean val;

  /**
   * Create action.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class
   * @param option option to change
   */
  public ActionUpdateOptionsBoolean(String name, String desc, CustomStackWindow ui,
          MutableBoolean option) {
    super(name, desc, ui);
    val = option;
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
    val.setValue(((JCheckBox) e.getSource()).isSelected());
    logger.trace(ui.getModel().getOptions().serialize());

  }

}
