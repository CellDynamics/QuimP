package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

/**
 * Action for clear button.
 * 
 * <p>Removes selected pixels from current view.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public class ActionClearPoints extends ProtAnalysisAbstractAction {

  /**
   * Create action not related to UI element.
   * 
   * @param ui window reference
   */
  public ActionClearPoints(CustomStackWindow ui) {
    super(ui);
  }

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionClearPoints(String name, String desc, CustomStackWindow ui) {
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
    clear();
  }

  /**
   * Clear selected points.
   */
  void clear() {
    ui.getModel().selected.clear();
    ui.getCanvas().repaint();
    ui.updateStaticFields();
  }

}
