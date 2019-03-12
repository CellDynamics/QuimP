package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import ij.gui.Overlay;

/**
 * Action for clear overlay button.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionClearOverlay extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionClearOverlay(String name, String desc, ProtAnalysisUi ui) {
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
    // TrackVisualisation modified overlay. We do not need that object, only overlay
    Overlay overlay = ui.getImagePlus().getOverlay();
    if (overlay != null) {
      overlay.clear();
      ui.updateOverlay(ui.getImagePlus().getCurrentSlice());
    }

  }

}
