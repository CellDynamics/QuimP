package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.WindowManager;

/**
 * Action for track button.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public class ActionTrackPoints extends ProtAnalysisAbstractAction implements Action {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    track(qconfLoader);
  }

  void track(QconfLoader qconfLoader) {
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    TrackVisualisation.Image visStackStatic =
            new TrackVisualisation.Image(ui.getImagePlus().duplicate()); // FIXME no
    // duplicate
    visStackStatic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Static tracking"));
    MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
    mf.setMaxima(ui.getModel().selected); // FIXME max are in map coordinates not xy
    // for (STmap mapCell : stMap) {
    visStackStatic.addElementsToImage(stMap[0], null, mf);
    // }
    visStackStatic.getOriginalImage().show();
  }

}
