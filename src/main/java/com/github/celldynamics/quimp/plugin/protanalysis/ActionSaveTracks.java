package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.IJ;

/**
 * Action (referenced as ActionListener) for saving tracks to csv if "Track" button is hit.
 * 
 * <p>Read:
 * <ol>
 * <li>{@link AbstractPluginOptions#paramFile}
 * <li>{@link ProtAnalysisOptions#saveTracks}
 * </ol>
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionSaveTracks extends ActionTrackPoints {

  /**
   * Action creator.
   * 
   * @param ui reference to outer class.
   */
  public ActionSaveTracks(ProtAnalysisUi ui) {
    super(ui);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    PrintWriter pw;
    if (options.saveTracks.getValue()) {
      QconfLoader qconfLoader = ui.getModel().getQconfLoader();
      STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
      HashMap<Integer, List<Point2D>> tmpSelected = extractPoints(stMap);
      for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
        Integer cellNo = entry.getKey(); // cell number
        List<Point2D> points = entry.getValue(); // users points
        MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
        mf.setMaxima(points);

        TrackCollection trackCollection = getTracks(stMap, cellNo, mf);
        try {
          Path fileToSave = Paths.get(qconfLoader.getQp().getPath(), "tracks_" + cellNo + ".csv");
          pw = new PrintWriter(new FileWriter(fileToSave.toFile()));
          trackCollection.saveTracks(pw);
          pw.flush();
          pw.close();
          logger.info("Saved tracks in " + fileToSave.toString());
          if (ui.getModel().getSink() == MessageSinkTypes.GUI
                  || ui.getModel().getSink() == MessageSinkTypes.IJERROR) {
            IJ.log("Saved tracks in " + fileToSave.toString());
          }
        } catch (IOException e) {
          new QuimpException(e, ui.getModel().getSink()).handleException(null,
                  "Exception thrown when saving tracks");
          break;
        }
      }
    }
  }

}
