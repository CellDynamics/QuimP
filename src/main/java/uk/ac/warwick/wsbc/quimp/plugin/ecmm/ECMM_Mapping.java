package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
// import java.util.Vector;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Richard Tyson. 23/09/2009. ECM Mapping Systems Biology DTC, Warwick University.
 */
import ij.IJ;
import ij.gui.YesNoCancelDialog;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.FormatConverter;
import uk.ac.warwick.wsbc.quimp.Nest;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.OutlineHandler;
import uk.ac.warwick.wsbc.quimp.QParams;
import uk.ac.warwick.wsbc.quimp.QParamsQconf;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.SnakeHandler;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.filesystem.DataContainer;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.registration.Registration;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/*
 * //!>
 * @startuml doc-files/ECMM_Mapping_1_UML.png
 * start
 * :Check registration;
 * if (input file given) then (no) 
 * :ask user; 
 * endif
 * :Load config file;
 * if (QUIMP_11 file) then (yes)
 * :process it;
 * :scan for other files;
 * repeat 
 * :process other file;
 * repeat while(more files?) else (no)
 * if(BOA data) then (no)
 * stop
 * endif
 * if(ECMM data) then (yes)
 * if(overwrite?) then (no)
 * end
 * endif
 * endif
 * :process it;
 * endif
 * end
 * @enduml
 * 
 * //!< 
 */
/**
 * Main ECMM implementation class.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 *
 */
public class ECMM_Mapping {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ECMM_Mapping.class.getName());

  private OutlineHandler oh;
  private OutlineHandler outputH;
  private OutlinesCollection outputOutlineHandlers; // output for new data file

  /**
   * The plot.
   */
  static ECMplot plot;
  private QconfLoader qconfLoader;

  /**
   * Constructor.
   * 
   * @param frames frame
   */
  public ECMM_Mapping(int frames) { // work around. b is nothing
    if (ECMp.plot) {
      plot = new ECMplot(frames);
    }
  }

  /**
   * Run analysis for given file.
   * 
   * @param qpFile path to paQP or QCONF file.
   */
  public ECMM_Mapping(String qpFile) {
    this(new File(qpFile));
  }

  /**
   * Main executive constructor.
   * 
   * <p>Process provided file and run the whole analysis.<br>
   * <img src="doc-files/ECMM_Mapping_1_UML.png"/><br>
   * 
   *
   * @param paramFile paQP or QCONF file to process.
   */
  public ECMM_Mapping(File paramFile) {
    about();
    IJ.showStatus("ECMM Analysis");
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    try {
      qconfLoader = new QconfLoader(paramFile); // load file
      if (qconfLoader == null || qconfLoader.getQp() == null) {
        return; // failed to load exit
      }
      if (qconfLoader.getConfVersion() == QParams.QUIMP_11) { // old path
        QParams qp;
        runFromPaqp();
        // old flow with paQP files - detect other paQP
        File[] otherPaFiles = qconfLoader.getQp().findParamFiles();
        if (otherPaFiles.length > 0) {
          YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Batch Process?",
                  "\tBatch Process?\n\n" + "Process other " + FileExtensions.configFileExt
                          + " files in the same folder with ECMM?\n"
                          + "[Files already run through ECMM will be skipped!]");
          if (yncd.yesPressed()) {
            ArrayList<String> runOn = new ArrayList<String>(otherPaFiles.length);
            ArrayList<String> skipped = new ArrayList<String>(otherPaFiles.length);

            for (int j = 0; j < otherPaFiles.length; j++) {
              plot.close();
              paramFile = otherPaFiles[j];
              qconfLoader = new QconfLoader(paramFile); // load file
              if (qconfLoader == null || qconfLoader.getQp() == null) {
                return; // failed to load exit
              }
              qp = qconfLoader.getQp();
              if (!qp.isEcmmHasRun()) {
                IJ.log("Running on " + otherPaFiles[j].getAbsolutePath());
                runFromPaqp();
                runOn.add(otherPaFiles[j].getName());
              } else {
                IJ.log("Skipped " + otherPaFiles[j].getAbsolutePath());
                skipped.add(otherPaFiles[j].getName());
              }

            }
            IJ.log("\n\nBatch - Successfully ran ECMM on:");
            for (int i = 0; i < runOn.size(); i++) {
              IJ.log(runOn.get(i));
            }
            IJ.log("\nSkipped:");
            for (int i = 0; i < skipped.size(); i++) {
              IJ.log(skipped.get(i));
            }
          } else {
            return; // no batch processing
          }
        }
      } else if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
        // validate in case new format
        qconfLoader.getBOA(); // will throw exception if not present
        if (qconfLoader.isECMMPresent()) {
          YesNoCancelDialog ync;
          ync = new YesNoCancelDialog(IJ.getInstance(), "Overwrite",
                  "You are about to override previous ECMM results. Is it ok?");
          if (!ync.yesPressed()) { // if no or cancel
            IJ.log("No changes done in input file.");
            return; // end}
          }
        }
        runFromQconf();
        IJ.log("The new data file " + qconfLoader.getQp().getParamFile().toString()
                + " has been updated by results of ECMM analysis.");
      } else {
        throw new IllegalStateException("QconfLoader returned unknown version of QuimP");
      }

      IJ.log("ECMM Analysis complete");
      IJ.showStatus("Finished");
    } catch (Exception e) { // catch all here
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Problem with running ECMM mapping: " + e.getMessage());
    }
  }

  /**
   * Default constructor called on plugin run from IJ GUI.
   */
  public ECMM_Mapping() {
    this((File) null); // ask user what to load
  }

  /**
   * Main executive for ECMM processing for QParamsExchanger (new file version).
   * 
   * @throws QuimpException on error
   * @throws IOException On problems with writing config files
   * @see <a href=
   *      "link">http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ConfigurationHandling</a>
   */
  private void runFromQconf() throws QuimpException, IOException {
    LOGGER.debug("Processing from new file format");
    Nest nest = ((QParamsQconf) qconfLoader.getQp()).getNest();
    outputOutlineHandlers = new OutlinesCollection(nest.size());
    for (int i = 0; i < nest.size(); i++) { // go over all snakes
      // For compatibility, all methods have the same syntax (assumes that there is only one
      // handler)
      ((QParamsQconf) qconfLoader.getQp()).setActiveHandler(i); // set current handler number.
      SnakeHandler sh = nest.getHandler(i);
      if (sh == null) {
        continue;
      }
      oh = new OutlineHandler(sh); // convert to outline, oh is global var
      ECMp.setup(qconfLoader.getQp());
      ECMp.setParams(oh.maxLength); // base params on outline in middle of
      // sequence
      if (ECMp.plot) {
        plot = new ECMplot(oh.getSize() - 1);
      }
      run(); // fills outputH
      outputOutlineHandlers.oHs.add(i, new OutlineHandler(outputH)); // store actual result
    }

    DataContainer dc = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer();
    dc.ECMMState = outputOutlineHandlers; // assign ECMM container to global output
    qconfLoader.getQp().writeParams(); // save global container
    // generate additional OLD files, disabled #263, enabled GH228
    if (QuimP.newFileFormat.get() == false) {
      FormatConverter foramtConvrter = new FormatConverter(qconfLoader);
      foramtConvrter.doConversion();
    }
  }

  /**
   * Display standard QuimP about message.
   */
  private void about() {
    IJ.log(new QuimpToolsCollection().getQuimPversion());
  }

  /**
   * Main executive for ECMM processing for QParams (old file version).
   * 
   * @throws QuimpException when OutlineHandler can not be read
   */
  private void runFromPaqp() throws QuimpException {
    oh = new OutlineHandler(qconfLoader.getQp());
    if (!oh.readSuccess) {
      throw new QuimpException("Could not read OutlineHandler");
    }

    ECMp.setup(qconfLoader.getQp());
    // System.out.println("sf " + ECMp.startFrame + ", ef " +
    // ECMp.endFrame);
    // System.out.println("outfile " + ECMp.OUTFILE.getAbsolutePath());
    ECMp.setParams(oh.maxLength); // base params on outline in middle of sequence
    if (ECMp.plot) {
      plot = new ECMplot(oh.getSize() - 1);
    }
    run();

    if (ECMp.saveTemp) {
      // ------ save a temporary version instead as to not over write the
      // old version
      File tempFile = new File(ECMp.OUTFILE.getAbsolutePath() + ".temp.txt");
      outputH.writeOutlines(tempFile, true);
      IJ.log("ECMM:137, saving to a temp file instead");
    } else {
      ECMp.INFILE.delete();
      outputH.writeOutlines(ECMp.OUTFILE, true);
    }

  }

  /**
   * Called by ANA if no ECCM results are in file (old path).
   * 
   * @param m OutlineHandler
   * @param ipr ImageProcessor
   * @param d cortex?
   * @return Processed outline
   */
  public OutlineHandler runByANA(OutlineHandler m, ImageProcessor ipr, double d) {
    oh = m;
    ECMp.image = ipr;
    ECMp.setParams(oh.maxLength);
    ECMp.startFrame = oh.getStartFrame();
    ECMp.endFrame = oh.getEndFrame();
    ECMp.plot = false;
    ECMp.ANA = true;
    ECMp.anaMigDist = d;
    ECMp.migQ = 1.5E-5; //
    ECMp.tarQ = -1.5E-5; // use same charge

    if (ECMp.plot) {
      plot = new ECMplot(oh.getSize() - 1);
    }

    // ECMp.setParams(m.indexGetOutline(0));

    // *******adjust params for ana***********
    ECMp.h = 0.9;
    ECMp.chargeDensity = 4;
    ECMp.d = 0.4;
    ECMp.maxVertF = 0.7;
    // *************************
    run();
    // IJ.log("ECM Mapping FINISHED");
    return outputH;
  }

  /**
   * Main executive for ECMM plugin.
   */
  private void run() {
    long time = System.currentTimeMillis();
    if (!ECMp.ANA) {
      IJ.log("ECMM resolution: " + ECMp.markerRes + "(av. spacing)\n");
    }

    outputH = new OutlineHandler(oh);
    ECMp.unSnapped = 0;
    // int skippedFrames = 0; // if a frame is skipped need to divide next
    // time point migration by 2, etc...

    Mapping map1;
    int f = ECMp.startFrame; // now in frames
    Outline o1 = oh.getOutline(f);
    // resolution is always as in segmentation - not now
    if (!ECMp.ANA) {
      if (Math.abs(ECMp.markerRes) > 0) {
        o1.setResolution(Math.abs(ECMp.markerRes));
      }
    }
    // LOGGER.trace("Outline o1:head =[" + o1.getHead().getX() + "," + o1.getHead().getY() +
    // "]");
    o1.resetAllCoords();
    o1.clearFluores();
    o1.calcCentroid(); // TODO this should be called as in case of Snakes

    outputH.save(o1, f);
    Outline o2;

    int stopAt = -1; // debug break

    for (; f <= oh.getEndFrame() - 1; f++) {
      if (f == stopAt) {
        ECMp.plot = true;
      }
      if (o1.checkCoordErrors()) {
        IJ.error("There was an error in tracking due to a bug (frame " + (f) + ")"
                + "\nPlease try again");
        break;
      }

      if (!ECMp.ANA) {
        IJ.showStatus("Running ECMM");
        IJ.showProgress(f, oh.getEndFrame());
        IJ.log("Mapping " + f + " to " + (f + 1));
      }

      o2 = oh.getOutline(f + 1);
      // o2 left as seen in the segmentation - i.e. marker res unchanged
      if (!ECMp.ANA && ECMp.markerRes > 0) {
        o2.setResolution(ECMp.markerRes); // must be done b4 intersects are calculated
      }
      o2.resetAllCoords();
      o2.clearFluores();

      if (!ECMp.ANA) {
        this.nudgeOverlaps(o1, o2); // ensure no points/edges lie directly on each other (to 1e-4)/
      }

      if (ECMp.plot) {
        plot.setDrawingFrame(f);
        plot.centre = o1.getCentroid();

        if (ECMp.drawInitialOutlines) {
          plot.setColor(0d, 0d, 1d);
          plot.drawOutline(o1);
          plot.setColor(0d, 1d, 0d);
          plot.drawOutline(o2);
          plot.setSlice(f);
        }
      }

      // OutlineHandler.writeSingle("o2.snQP", o2);
      // OutlineHandler.writeSingle("o1.snQP", o1);

      map1 = new Mapping(o1, o2);

      /*
       * if (map1.invalid) { //Use no sectors IJ.log(" invalid outline intersection,
       * attempting non-intersect mapping..."); plot.writeText("Non-intersect mapping");
       * ECMp.noSectors = true; map1 = new Mapping(o1, o2); ECMp.noSectors = false; }
       */

      o1 = map1.migrate();
      // System.out.println("num nodes: "+o1.getVerts());

      if (!ECMp.ANA) {
        // System.out.println("\n check final intersects");
        if (!ECMp.disableDensityCorrections) {
          if (o1.removeNanoEdges()) {
            // IJ.log(" result had some v.small edges- removed");
          }
          if (o1.cutSelfIntersects()) {
            IJ.log("    result self intersected - fixed");
            if (ECMp.plot) {
              plot.writeText("Fixed self intersection");
            }
          }

          if (ECMp.markerRes == 0) {
            o1.correctDensity(2 * 1.6, 2 / 1.6);
          } else {
            o1.correctDensity(ECMp.markerRes * 1.6, ECMp.markerRes / 1.6);
          }
        }
        if (ECMp.plot && ECMp.drawSolutionOutlines) {
          plot.setColor(1d, 0d, 0d);
          plot.drawOutline(o1);
        }
      }

      if (ECMp.ANA && ECMp.plot) {
        plot.setColor(0d, 0.7d, 0.7d);
        plot.drawOutline(o1);
      }

      // OutlineHandler.writeSingle("o2.snQP", o2);
      // OutlineHandler.writeSingle("o1.snQP", o1);

      o1.coordReset(); // reset the frame Coordinate system

      outputH.save(o1, f + 1);
      if (f == stopAt) {
        break;
      }
    }

    // IJ.log("Total iterations = " + ECMp.its);
    if (ECMp.plot) {
      plot.repaint();
    }

    if (!ECMp.ANA) {
      double timeSec = (System.currentTimeMillis() - time) / 1000d;
      IJ.showStatus("ECMM finished");
      IJ.log("ECMM finished in " + timeSec + " seconds.");
    }
    return;
  }

  private void nudgeOverlaps(Outline o1, Outline o2) {

    int state;
    double[] intersect = new double[2];
    Random rg = new Random();

    Vert na = o1.getHead();
    Vert nb;
    do {
      nb = o2.getHead();
      do {
        // check if points on top of each other
        if (nb.getX() == na.getX() && nb.getY() == na.getY()) {
          // use a minimum nudge of 0.01 (imageJ pixel accurracy
          na.setX(na.getX() + (rg.nextDouble() * 0.5) + 0.01);
          na.setY(na.getY() + (rg.nextDouble() * 0.5) + 0.01);
        }

        // check if lines are parallel
        state = ExtendedVector2d.segmentIntersection(na.getX(), na.getY(), na.getNext().getX(),
                na.getNext().getY(), nb.getX(), nb.getY(), nb.getNext().getX(), nb.getNext().getY(),
                intersect);
        if (state == -1 || state == -2) {
          // IJ.log(" outline parrallel -fixed");
          na.setX(na.getX() + (rg.nextDouble() * 0.5) + 0.01);
          na.setY(na.getY() + (rg.nextDouble() * 0.5) + 0.01);
        }

        nb = nb.getNext();
      } while (!nb.isHead());
      na = na.getNext();

    } while (!na.isHead());

  }
}
