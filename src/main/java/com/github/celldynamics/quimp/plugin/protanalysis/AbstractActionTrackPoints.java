package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.filesystem.QconfLoader;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;

/**
 * Generalisation of action for tracking points.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractActionTrackPoints extends ProtAnalysisAbstractAction {

  protected ImagePlus image; // image used for plotting overlay

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public AbstractActionTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (options.guiNewImage) { // if true = new image
      image = ui.getImagePlus().duplicate();
      Overlay overlay = image.getOverlay();
      if (overlay != null) {
        overlay.clear();
      }
    } else {
      image = ui.getImagePlus();
    }
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    track(qconfLoader);
    if (options.guiNewImage) {
      image.setTitle(WindowManager.makeUniqueName("Tracking"));
      image.show(); // show new image
    } else { // clear user selection at the end if no new image
      new ActionClearPoints(ui).clear();
    }
  }

  /**
   * Called on each action. Should plot tracks.
   * 
   * @param qconfLoader QCONF structure.
   */
  abstract void track(QconfLoader qconfLoader);

}
