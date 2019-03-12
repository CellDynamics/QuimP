package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;

import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.IEnumDataType;

/**
 * Action to set Enum based types.
 * 
 * <p>Set value for any datatype that implements {@link IEnumDataType} {@link ProtAnalysisOptions}.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionUpdateOptionsEnum extends ProtAnalysisAbstractAction {

  private IEnumDataType val;

  /**
   * Main constructor.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class
   * @param option reference to parameter to be changed
   */
  public ActionUpdateOptionsEnum(String name, String desc, ProtAnalysisUi ui,
          IEnumDataType option) {
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
    if (item instanceof Enum) { // for JComboBox<Number> just get selected value
      logger.trace("val: " + item.toString());
      val.setCurrent((Enum<?>) item);
    } else {
      throw new RuntimeException("This JComboBox is not supported");
    }

    logger.trace(ui.getModel().getOptions().serialize());

  }

}
