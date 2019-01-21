package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Update option from Radio Button (grouped).
 * 
 * <p>Radio buttons are grouped and their states are kept in one INT variable.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionUpdateOptionsRadio extends ProtAnalysisAbstractAction {

  private MutableInt val;
  private int valToSet;

  /**
   * Action creator. Boolean option.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   * @param option {@link ProtAnalysisOptions} option to change. Must be reference
   * @param valToSet Vale to set to this option if action is fired.
   */
  public ActionUpdateOptionsRadio(String name, String desc, CustomStackWindow ui, MutableInt option,
          int valToSet) {
    super(name, desc, ui);
    val = option;
    this.valToSet = valToSet;
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
    val.setValue(valToSet);
    logger.trace(ui.getModel().getOptions().serialize());
  }

}
