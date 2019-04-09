package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Paths;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.graphics.PolarPlot;

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
  public ActionPolarPlot(String name, String desc, ProtAnalysisUi ui) {
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
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    int h = 0;
    for (STmap mapCell : stMap) {
      try {
        PolarPlot pp = new PolarPlot(mapCell, options.gradientPoint);
        pp.labels = true;
        String fileToSave = Paths.get(qconfLoader.getQp().getPath(),
                qconfLoader.getQp().getFileName() + "_" + h + FileExtensions.polarPlotSuffix)
                .toString();
        pp.generatePlot(fileToSave);
        logger.info("Polar plot saved in " + fileToSave);
        h++;
      } catch (IOException ex) {
        new QuimpException(ex, ui.getModel().getSink()).handleException(null,
                "Problem with saving polar plots");
        break; // break loop
      }
    }
  }

}
