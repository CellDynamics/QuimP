package com.github.celldynamics.quimp.filesystem.converter;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.PointsList;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.ANAParamCollection;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.ana.ANAp;
import com.github.celldynamics.quimp.plugin.qanalysis.FluoMap;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;

/**
 * This class allows for recreating paQP and snQP files from new format QCONF and vice versa.
 * 
 * <p><b>Note</b>
 * 
 * <p>Other files such as stQP, maps and images are generated regardless file format used during
 * saving
 * in each QuimP module.
 * 
 * <p>By default it is assumed that provided file is QCONF. This file is stored in class and used by
 * some of private methods for creating paQP and snQP files. For converting from old files, the file
 * provided to constructor is read but not used. It is re-read in loop by
 * {@link #generateNewDataFile()}.
 * 
 * <p>This method is related to fields that are non-transient and any change there should be
 * reflected here.
 * 
 * <p>Due to randomness during creating Snakes or Outlines (head node is picked randomly on
 * {@link Shape#removePoint(PointsList, boolean)} these objects stored in QCONF may differ from snQP
 * representation. There are the following rules:
 * <ol>
 * <li>snQP--QCONF conversion</li>
 * <ol>
 * <li>nest:liveSnake is filled but <b>should not be used</b></li>
 * <li>nest:finalSnake: is filled and it is a converted snQP. Starting node may be different than in
 * snQP
 * <li>ECMMState:outlines contains copied finalSnakes but due to conversion starting nodes can be
 * different than in nest:liveSnake
 * </ol>
 * <li>QCONF--paQP</li>
 * <ol>
 * <li>Snakes in snQP are in the same order like:
 * <ol>
 * <li>nest:finalSnakes if there is no ECCM data (position is used)
 * <li>ECMMState:outlines if there is ECMM data (coord is used)
 * </ol>
 * </ol>
 * </ol>
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(FormatConverter.class.getName());
  private QconfLoader qcL;
  private Path path; // path of file extracted from qcL
  private Path filename; // file name extracted from qcL, no extension

  /**
   * Construct FormatConverter from provided file.
   * 
   * @param fileToConvert file to convert
   * @throws QuimpException if input file can not be loaded
   */
  public FormatConverter(File fileToConvert) throws QuimpException {
    LOGGER.debug("Use provided file:" + fileToConvert.toString());
    qcL = new QconfLoader(fileToConvert);
    path = Paths.get(fileToConvert.getParent());
    filename = Paths.get(qcL.getQp().getFileName()); // can contain xx_0 if old file loaded
  }

  /**
   * Construct conversion object from QconfLoader.
   * 
   * @param qcL reference to QconfLoader
   */
  public FormatConverter(QconfLoader qcL) {
    LOGGER.debug("Use provided QconfLoader");
    this.qcL = qcL;
    this.path = qcL.getQp().getPathasPath();
    this.filename = Paths.get(qcL.getQp().getFileName()); // can contain xx_0 if old file loaded
  }

  /**
   * Show message with conversion capabilities.
   * 
   * @param frame parent frame
   */
  public void showConversionCapabilities(Frame frame) {
    //!>
    JOptionPane.showMessageDialog(frame,
                "This is experimental tool. It may not work correctly.\n"
                + "Supported conversions\n"
                + "paQP->QCONF features:\n"
                + " [+] paQP->QCONF\n"
                + " [+] snQP->QCONF\n"
                + " [+] maQP->QCONF\n"
                + " [-] stQP->QCONF\n"
                + "QCONF->paQP features:\n"
                + " [+] QCONF->paQP\n"
                + " [+] QCONF->snQP\n"
                + " [-] QCONF->maQP\n"
                + " [-] QCONF->stQP\n"
                + " [-] QCONF->tiffs",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
    //!<
  }

  /**
   * Build QCONF from old datafile provided in constructor.
   * 
   * <p>Input file given in constructor is considered as starting one. paQP files in successive
   * numbers are searched in the same directory. The internal <tt>qcL</tt> variable will be
   * overrode on this method call.
   * 
   * @throws QuimpException on wrong inputs
   * @throws FileNotFoundException on saving QCONF
   */
  private void generateNewDataFile() throws QuimpException, FileNotFoundException {
    //!>
    LOGGER.info("\n----------------------------------------------------------\n"
                + "Warning:\n"
                + "    1. Stats file stQP is not read during conversion\n"
                + "       and these data are not converted to QCONF.\n"
                + "----------------------------------------------------------\n");
    //!<
    if (qcL.getConfVersion() == QParams.NEW_QUIMP) {
      throw new QuimpException("Can not convert from new format to new", MessageSinkTypes.GUI);
    }
    // create storage
    DataContainer dt = new DataContainer();
    dt.BOAState = new BOAState(qcL.getImage());
    dt.ECMMState = new OutlinesCollection();
    dt.BOAState.nest = new Nest();
    // dT.ANAState = new ANAStates();
    ArrayList<STmap> maps = new ArrayList<>(); // temporary - we do not know number of cells

    // extract paQP number from file name (loaded in constructor)
    int last = filename.toString().lastIndexOf('_'); // position of _ before number
    if (last < 0) {
      throw new QuimpException(
              "Input file name must be in format name_XX.paQP, where XX is cell number.",
              MessageSinkTypes.GUI);
    }
    int numofpaqp; // number extracted from paQP name
    // check which file number user selected. End program if user made mistake
    try {
      numofpaqp = Integer
              .parseInt(filename.toString().substring(last + 1, filename.toString().length()));
      if (numofpaqp != 0) { // warn user if not first file selected
        int ret = JOptionPane.showConfirmDialog(IJ.getInstance(),
                QuimpToolsCollection.stringWrap(
                        "Selected paQP file is not first (not a _0.paQP file)."
                                + "Is it ok? (\"No\" will end conversion process)",
                        QuimP.LINE_WRAP),
                "Waring", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ret != JOptionPane.YES_OPTION) {
          return;
        }
      }
    } catch (NumberFormatException e) {
      throw new QuimpException(
              "paQP file number can not be found in file name. "
                      + "Check if file name is in format name_XX.paQP, where XX is cell number.",
              MessageSinkTypes.GUI);
    }
    // cut last number from file name name
    String orginal = filename.toString().substring(0, last);

    int i; // PaQP files counter
    // run conversion for all paQP files. conversion always starts from 0
    // try to read paPQ starting from given by user, before we tested if user pointed correct file
    i = numofpaqp;
    File filetoload = new File(""); // store name_XX.paQP file in loop below
    OutlineHandler oh;
    STmap stMap;
    ANAParamCollection anaP = new ANAParamCollection(); // holder for ANA config, for every cell
    try {
      do {
        // paQP files with _xx number in name
        filetoload =
                Paths.get(qcL.getQp().getPath(), orginal + "_" + i + FileExtensions.configFileExt)
                        .toFile();
        if (!filetoload.exists()) { // if does not exist - end loop
          LOGGER.warn("File " + filetoload.toString() + " does not exist.");
          break;
        }
        // optimisation - first file is already loaded, skip it
        if (i != numofpaqp) {
          qcL = new QconfLoader(filetoload); // re-load it
        }
        // assume that BOA params are taken from file 0_.paQP
        if (i == numofpaqp) {
          dt.BOAState.loadParams(qcL.getQp()); // load parameters only once (but not frameinterval,)
          dt.BOAState.boap.setImageFrameInterval(qcL.getQp().getFrameInterval());
          dt.BOAState.boap.setImageScale(qcL.getQp().getImageScale());
        }
        // initialize snakes (from snQP files)
        oh = new OutlineHandler(qcL.getQp()); // restore OutlineHandler
        dt.ECMMState.oHs.add(oh); // store in ECMM object
        BOA_.qState = dt.BOAState; // for compatibility - create static
        dt.BOAState.nest.addOutlinehandler(oh); // covert ECMM to Snake and store in BOA section
        // load maps and store in QCONF
        stMap = new STmap();
        try {
          stMap.motMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getMotilityFile());
        } catch (IOException e) {
          LOGGER.warn(e.getMessage());
          LOGGER.debug(e.getMessage(), e);
        }
        try {
          stMap.convMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getConvexFile());
        } catch (IOException e) {
          LOGGER.warn(e.getMessage());
          LOGGER.debug(e.getMessage(), e);
        }
        try {
          stMap.coordMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getCoordFile());
        } catch (IOException e) {
          LOGGER.warn(e.getMessage());
          LOGGER.debug(e.getMessage(), e);
        }
        try {
          stMap.originMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getOriginFile());
        } catch (IOException e) {
          LOGGER.debug(e.getMessage(), e);
          LOGGER.warn(e.getMessage());
        }
        try {
          stMap.xMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getxmapFile());
        } catch (IOException e) {
          LOGGER.warn(e.getMessage());
          LOGGER.debug(e.getMessage(), e);
        }
        try {
          stMap.yMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getyFile());
        } catch (IOException e) {
          LOGGER.warn(e.getMessage());
          LOGGER.debug(e.getMessage(), e);
        }
        // Fluoromap
        // first check if there is any FluMap
        int channel = 1; // channel counter for fluoromaps
        int p = 0; // sizes of flumap
        int t = 0; // sizes of flumap
        for (File ff : qcL.getQp().getFluFiles()) { // iterate over filenames
          // if any exist, get its size. Because if there is no maps at all we set this object to
          // null but if there is at least one we have to set all other maps to -1 array of size of
          // that available one
          if (ff.exists()) {
            double[][] tmpFluMap = QuimPArrayUtils.file2Array(",", ff);
            t = tmpFluMap.length;
            p = tmpFluMap[0].length;
            break; // assume without checking that all maps are the same
          }
        }
        // if p,t are non zero we know that there is at leas map
        // try to read 3 channels for current paQP
        for (File ff : qcL.getQp().getFluFiles()) {
          // create Fluoro data holder
          FluoMap chm = new FluoMap(t, p, channel);
          if (ff.exists()) { // read file stored in paQP for channel
            chm.setMap(QuimPArrayUtils.file2Array(",", ff)); // it sets it enabled
          } else {
            chm.setEnabled(false); // not existing, disable
            LOGGER.warn("File " + ff.toString() + " not found");
          }
          stMap.fluoMaps[channel - 1] = chm;
          channel++;
        }
        maps.add(stMap);
        // ANAState - add ANAp references for every processed paQP, set only non-transient
        // fields
        ANAp anapTmp = new ANAp();
        anapTmp.scale = qcL.getQp().getImageScale(); // set scale used by setCortextWidthScale
        anapTmp.setCortextWidthScale(qcL.getQp().cortexWidth); // sets also cortexWidthPixel
        anapTmp.fluTiffs = qcL.getQp().fluTiffs; // set files
        anaP.aS.add(anapTmp); // store in ANAParamCollection

        i++; // go to next paQP
      } while (true); // exception thrown by QconfLoader will stop this loop, e.g. trying to load
      // nonexiting file

    } catch (Exception e) { // repack exception with proper message about defective file
      throw new QuimpException(
              "File " + filetoload.toString() + " can not be processed: " + e.getMessage(), e,
              MessageSinkTypes.GUI);
    }
    // save DataContainer using Serializer
    dt.QState = maps.toArray(new STmap[0]); // convert to array
    dt.ANAState = anaP;

    Serializer<DataContainer> n;
    n = new Serializer<>(dt, QuimP.TOOL_VERSION);
    n.setPretty();
    n.save(path + File.separator + orginal + FileExtensions.newConfigFileExt);
    n = null;
  }

  /**
   * Recreate paQP and snQP files from QCONF.
   * 
   * <p>Files are created in directory where QCONF is located.
   * 
   * @throws IOException
   * 
   */
  private void generateOldDataFile() throws IOException {
    //!>
    LOGGER.info("\n----------------------------------------------------------\n"
                + "Warning:\n"
                + "    1. Only paQP and snQP files are converted.\n"
                + "----------------------------------------------------------\n");
    //!<
    if (qcL.getConfVersion() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    if (dt.getEcmmState() == null) {
      generatepaQP(); // no ecmm data write snakes only
    } else {
      generatesnQP(); // write ecmm data
    }
  }

  /**
   * Create paQP and snQP file. Latter one contains only pure snake data.
   * 
   * <p>Those files are always saved together. snQP file will contain only pure snake data. Files
   * are created in directory where QCONF is located.
   * 
   * @throws IOException on writing snakes
   */
  private void generatepaQP() throws IOException {
    if (qcL.getConfVersion() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    // replace location to location of QCONF
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.getBOAState().boap.setOutputFileCore(path + File.separator + filename.toString());
    dt.BOAState.nest.writeSnakes(); // write paQP and snQP together
  }

  /**
   * Rewrite snQP file using recent ECMM processed results.
   * 
   * <p>Files are created in directory where QCONF is located.
   * 
   * @throws IOException on writing old params
   * 
   */
  private void generatesnQP() throws IOException {
    if (qcL.getConfVersion() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    int activeHandler = 0;
    // replace location to location of QCONF
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.BOAState.boap.setOutputFileCore(path + File.separator + filename.toString());
    Iterator<OutlineHandler> ohi = dt.getEcmmState().oHs.iterator();
    do {
      ((QParamsQconf) qcL.getQp()).setActiveHandler(activeHandler++);
      OutlineHandler oh = ohi.next();
      oh.writeOutlines(((QParamsQconf) qcL.getQp()).getSnakeQP(), true);
      ((QParamsQconf) qcL.getQp()).writeOldParams();
    } while (ohi.hasNext());

  }

  /**
   * Perform conversion depending on which file has been loaded.
   * 
   * @throws QuimpException on every error redirected to GUI. This is final method called from
   *         caller. All exceptions during conversion are collected and converted here to GUI.
   */
  public void doConversion() throws QuimpException {
    try {
      switch (qcL.getConfVersion()) {
        case QParams.NEW_QUIMP:
          generateOldDataFile();
          break;
        case QParams.QUIMP_11:
          generateNewDataFile();
          break;
        default:
          throw new IllegalArgumentException("QuimP version not supported");
      }
    } catch (Exception qe) {
      throw new QuimpException(qe, MessageSinkTypes.GUI);
    }

  }

}
