package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.celldynamics.quimp.CellStats;
import com.github.celldynamics.quimp.FrameStatistics;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.filesystem.StatsCollection;

import ij.gui.Plot;

/**
 * Plot selected 2d data.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionPlot2d extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionPlot2d(String name, String desc, CustomStackWindow ui) {
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
    plot(qconfLoader);
  }

  void plot(QconfLoader qconfLoader) {
    int h = options.selActiveCellPlot.getValue();
    StatsCollection stats =
            ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getStats();

    try {
      // geom plots
      if (options.chbXcentrPlot.booleanValue()) {
        plotCentroidParamVsFrame("centroid-x", stats.getStatCollection().get(h), "X");
      }
      if (options.chbYcentrPlot.booleanValue()) {
        plotCentroidParamVsFrame("centroid-y", stats.getStatCollection().get(h), "Y");
      }
      if (options.chbPersistencePlot.booleanValue()) {
        plotGeomParamVsFrame("persistance", stats.getStatCollection().get(h));
      }

      if (options.chbDisplPlot.booleanValue()) {
        plotGeomParamVsFrame("displacement", stats.getStatCollection().get(h));
      }
      if (options.chbDistPlot.booleanValue()) {
        plotGeomParamVsFrame("dist", stats.getStatCollection().get(h));
      }
      if (options.chbSpeedPlot.booleanValue()) {
        plotGeomParamVsFrame("speed", stats.getStatCollection().get(h));
      }
      if (options.chbPerimPlot.booleanValue()) {
        plotGeomParamVsFrame("perimiter", stats.getStatCollection().get(h));
      }
      if (options.chbElongPlot.booleanValue()) {
        plotGeomParamVsFrame("elongation", stats.getStatCollection().get(h));
      }
      if (options.chbCircPlot.booleanValue()) {
        plotGeomParamVsFrame("circularity", stats.getStatCollection().get(h));
      }
      if (options.chbAreaPlot.booleanValue()) {
        plotGeomParamVsFrame("area", stats.getStatCollection().get(h));
      }

      // fluo plots
      if (options.chbTotFluPlot.booleanValue()) {
        plotFluoParamVsFrame("totalFluor", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbMeanFluPlot.booleanValue()) {
        plotFluoParamVsFrame("meanFluor", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbCortexWidthPlot.booleanValue()) {
        plotFluoParamVsFrame("cortexWidth", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbCytoAreaPlot.booleanValue()) {
        plotFluoParamVsFrame("innerArea", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbTotalCytoPlot.booleanValue()) {
        plotFluoParamVsFrame("totalInnerFluor", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbMeanCytoPlot.booleanValue()) {
        plotFluoParamVsFrame("meanInnerFluor", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbCortexAreaPlot.booleanValue()) {
        plotFluoParamVsFrame("cortexArea", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbTotalCtf2Plot.booleanValue()) {
        plotFluoParamVsFrame("totalCorFluo", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }
      if (options.chbManCtfPlot.booleanValue()) {
        plotFluoParamVsFrame("meanCorFluo", stats.getStatCollection().get(h),
                options.selActiveChannel.intValue());
      }

    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
            | IllegalAccessException e) {
      throw new RuntimeException("Illegal field name.");
    }

  }

  /**
   * Plot geometric feature versus frame number for specified cell.
   * 
   * @param name name of the filed in {@link FrameStatistics}
   * @param stats reference to cell stats
   * @throws SecurityException on wrong filed
   * @throws NoSuchFieldException on wrong filed
   * @throws IllegalAccessException on wrong filed
   * @throws IllegalArgumentException on wrong filed
   */
  public void plotGeomParamVsFrame(String name, CellStats stats) throws NoSuchFieldException,
          SecurityException, IllegalArgumentException, IllegalAccessException {
    Plot plot = new Plot(name, "time", name);
    ArrayList<Double> ar = new ArrayList<>();
    ArrayList<Double> f = new ArrayList<>();
    for (FrameStatistics fs : stats.getFramestat()) {
      Field field = fs.getClass().getDeclaredField(name);
      ar.add((double) field.getDouble(fs));
      f.add((double) fs.frame);
    }
    plot.addPoints(f, ar, Plot.CONNECTED_CIRCLES);
    plot.show();
  }

  /**
   * Plot fluorescence feature versus frame number for specified cell.
   * 
   * @param name name of the filed in {@link FrameStatistics}
   * @param stats reference to cell stats
   * @param ch channel number
   * @throws SecurityException on wrong filed
   * @throws NoSuchFieldException on wrong filed
   * @throws IllegalAccessException on wrong filed
   * @throws IllegalArgumentException on wrong filed
   */
  public void plotFluoParamVsFrame(String name, CellStats stats, int ch)
          throws NoSuchFieldException, SecurityException, IllegalArgumentException,
          IllegalAccessException {
    Plot plot = new Plot(name, "time", name);
    ArrayList<Double> ar = new ArrayList<>();
    ArrayList<Double> f = new ArrayList<>();
    for (FrameStatistics fs : stats.getFramestat()) {
      Field field = fs.channels[ch].getClass().getDeclaredField(name);
      ar.add((double) field.getDouble(fs.channels[ch]));
      f.add((double) fs.frame);
    }
    plot.addPoints(f, ar, Plot.CONNECTED_CIRCLES);
    plot.show();
  }

  /**
   * Plot geometric feature versus frame number for specified cell.
   * 
   * <p>Plot centroid that is object not a filed like those supported by
   * {@link #plotGeomParamVsFrame(String, CellStats)}
   * 
   * @param name name of the filed in {@link FrameStatistics}
   * @param stats reference to cell stats
   * @param type X or Y for x,y coordinate
   */
  public void plotCentroidParamVsFrame(String name, CellStats stats, String type) {
    Plot plot = new Plot(name, "time", name);
    ArrayList<Double> ar = new ArrayList<>();
    ArrayList<Double> f = new ArrayList<>();
    switch (type) {
      case "X":
        for (FrameStatistics fs : stats.getFramestat()) {
          ar.add(fs.centroid.getX());
          f.add((double) fs.frame);
        }
        break;
      case "Y":
        for (FrameStatistics fs : stats.getFramestat()) {
          ar.add(fs.centroid.getY());
          f.add((double) fs.frame);
        }
        break;
      default:
        throw new IllegalArgumentException("Wrong type.");
    }

    plot.addPoints(f, ar, Plot.CONNECTED_CIRCLES);
    plot.show();

  }

}
