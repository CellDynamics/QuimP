package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Create action for updating numbers in options from JComboBox<>.
 * 
 * <p>Only Number and String are supported as items of JComboBox.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionUpdateOptionsNumber extends ProtAnalysisAbstractAction {

  private MutableInt val;

  /**
   * Main constructor.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class
   * @param option reference to parameter to be changed
   */
  public ActionUpdateOptionsNumber(String name, String desc, CustomStackWindow ui,
          MutableInt option) {
    super(name, desc, ui);
    this.val = option;
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
    Object item = cmp.getSelectedItem();
    if (item instanceof Number) { // for JComboBox<Number> just get selected value
      Number num = (Number) item;
      val.setValue(num);
    } else {
      if (item instanceof String) { // for JComboBox<String> get index of selected item
        int num = cmp.getSelectedIndex();
        val.setValue(num);
      } else {
        throw new RuntimeException("This JComboBox is not supported");
      }
    }

    logger.trace(ui.getModel().getOptions().serialize());

  }

}
