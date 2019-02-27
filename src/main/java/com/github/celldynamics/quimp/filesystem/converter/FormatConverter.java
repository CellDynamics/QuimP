package com.github.celldynamics.quimp.filesystem.converter;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.CellStats;
import com.github.celldynamics.quimp.FrameStatistics;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.Node;
import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.PointsList;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.SnakeHandler;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.filesystem.ANAParamCollection;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.filesystem.StatsCollection;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.plugin.ana.ANAp;
import com.github.celldynamics.quimp.plugin.ana.ChannelStat;
import com.github.celldynamics.quimp.plugin.qanalysis.FluoMap;
import com.github.celldynamics.quimp.plugin.qanalysis.Qp;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.CsvWritter;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ch.qos.logback.classic.Logger;

/**
 * This class allows for converting between paQP and QCONF and vice versa.
 * 
 * <p>The following conversion are supported:<br>
 * <b>paQP->QCONF</b><br>
 * [+] paQP->QCONF<br>
 * [+] snQP->QCONF<br>
 * [+] maQP->QCONF<br>
 * [+] stQP->QCONF<br>
 * <b>QCONF->paQP</b><br>
 * [+] QCONF->paQP<br>
 * [+] QCONF->snQP<br>
 * [+] QCONF->maQP<br>
 * [+] QCONF->stQP<br>
 * [-] QCONF->tiffs<br>
 * 
 * <p>This class can be also used to extract data from QCONF {@link DataContainer} and save them as
 * plain csv files.
 * 
 * <p><b>Note</b>
 * 
 * <p>Images are generated regardless used file format in QuimP Q module.
 * 
 * <p>This method is related to fields that are non-transient and any change in serialised classes
 * should be reflected here.
 * 
 * <p>Due to randomness during creating Snakes or Outlines (head node is picked randomly on
 * {@link Shape#removePoint(PointsList, boolean)} these objects stored in QCONF may differ from snQP
 * representation. There are the following rules ({@link DataContainer}):
 * <ol>
 * <li>paQP--QCONF conversion</li>
 * <ol>
 * <li>nest:liveSnake ({@link SnakeHandler#getLiveSnake()}) is filled but <b>should not be
 * used</b></li>
 * <li>nest:finalSnake ({@link SnakeHandler#getStoredSnake(int)}) is filled and it is converted from
 * outlines (which are read from snQP file).
 * Starting node may be different than in snQP due to conversion between Snakes and Outlines.
 * <li>ECMMState:outlines ({@link DataContainer#getEcmmState()}) contains data read from
 * snQP file. This field is created always even if
 * ECMM module has not been run. therefore ECMMState can contain pure BOA snakes as well.
 * </ol>
 * <li>QCONF--paQP</li>
 * <ol>
 * <li>Snakes in snQP are in the same order like:
 * <ol>
 * <li>nest:finalSnakes ({@link SnakeHandler#getStoredSnake(int)}) if there is no ECCM data
 * (position is used)
 * <li>ECMMState:outlines ({@link DataContainer#getEcmmState()}) if there is ECMM data (coord is
 * used)
 * </ol>
 * </ol>
 * </ol>
 * 
 * <p>Generally converter expects correct structure of paQP experiment, e.g. if there are many
 * cells (_0.paQP, _1.paQP, etc), all cases should have other modules run on them with the same
 * parameters (e.g. map resolution). There should be the same set of files for each case.
 * 
 * <p>See {@link #doConversion()} for supported files.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverter {
  protected static Logger logger =
          (Logger) LoggerFactory.getLogger(FormatConverter.class.getName());
  private QconfLoader qcL;
  private Path path; // path of file extracted from qcL
  private Path filename; // file name extracted from qcL, no extension

  /**
   * Order of parameters saved by {@link #saveOutline(Outline, CsvWritter)}.
   */
  public static final String[] headerEcmmOutline = {
      //!> order of data, must follow writeLine below
      "charge",
      "distance",
      "fluo-ch1_x",
      "fluo-ch1_y",
      "fluo-ch1_i",
      "fluo-ch2_x",
      "fluo-ch2_y",
      "fluo-ch2_i",
      "fluo-ch3_x", 
      "fluo-ch3_y", 
      "fluo-ch3_i",
      "curvLoc",
      "curvSmooth",
      "curvSum",
      "coord",
      "gLandCoord",
      "node_x",
      "node_y",
      "normal_x",
      "normal_y",
      "tan_x",
      "tan_y",
      "position",
      "frozen"        
      };
  //!<

  /**
   * Do nothing.
   * 
   * @see #attachFile(File)
   */
  public FormatConverter() {
  }

  /**
   * Construct FormatConverter from provided file.
   * 
   * @param fileToConvert file to convert
   * @throws QuimpException if input file can not be loaded
   */
  public FormatConverter(File fileToConvert) throws QuimpException {
    this();
    attachFile(fileToConvert);
  }

  /**
   * Attach file for conversion.
   * 
   * <p>Do the same job as {@link #FormatConverter(File)} but can be used if
   * {@link #FormatConverter()}
   * was used.
   * 
   * @param fileToConvert file to convert
   * @throws QuimpException if input file can not be loaded
   */
  public void attachFile(File fileToConvert) throws QuimpException {
    logger.info("Converting file: " + fileToConvert.getName());
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
    logger.debug("Use provided QconfLoader");
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
                "Supported conversions\n"
                + "paQP->QCONF features:\n"
                + " [+] paQP->QCONF\n"
                + " [+] snQP->QCONF\n"
                + " [+] maQP->QCONF\n"
                + " [+] stQP->QCONF\n"
                + "QCONF->paQP features:\n"
                + " [+] QCONF->paQP\n"
                + " [+] QCONF->snQP\n"
                + " [+] QCONF->maQP\n"
                + " [+] QCONF->stQP\n"
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
   * @throws IOException on saving QCONF
   */
  private void generateNewDataFiles() throws QuimpException, IOException {
    if (qcL.isFileLoaded() == QParams.NEW_QUIMP) {
      throw new IllegalArgumentException("Can not convert from new format to new");
    }
    boolean ecmmRun = false;
    logger.info("File is in old format, new format will be created");
    // create storage
    DataContainer dt = new DataContainer();
    dt.BOAState = new BOAState(qcL.getImage());
    dt.ECMMState = null;
    dt.BOAState.nest = new Nest();
    // dT.ANAState = new ANAStates();
    ArrayList<STmap> maps = new ArrayList<>(); // temporary - we do not know number of cells

    // extract paQP number from file name (loaded in constructor)
    int last = filename.toString().lastIndexOf('_'); // position of _ before number
    if (last < 0) {
      throw new QuimpException(
              "Input file name must be in format name_XX.paQP, where XX is cell number.");
    }

    // check which file number user selected. End program if user made mistake
    try {
      int numofpaqp; // number extracted from paQP name
      numofpaqp = Integer
              .parseInt(filename.toString().substring(last + 1, filename.toString().length()));
      if (numofpaqp != 0) { // warn user if not first file selected
        throw new QuimpException("Selected paQP file is not first (not a _0.paQP file).");
      }
    } catch (NumberFormatException e) {
      throw new QuimpException("Number can not be found in file paQP name. "
              + "Check if file name is in format name_XX.paQP, where XX is cell number.");
    }
    // cut last number from file name name
    String orginal = filename.toString().substring(0, last);

    int i; // PaQP files counter
    // run conversion for all paQP files. conversion always starts from 0
    i = 0;
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
        logger.info("Attempting to process " + filetoload.getName());
        if (!filetoload.exists()) { // if does not exist - end loop
          logger.info("File " + filetoload.toString() + " does not exist. Finishing conversion.");
          break;
        }
        // optimisation - first file is already loaded, skip it
        if (i != 0) {
          qcL = new QconfLoader(filetoload); // re-load it
        } else {
          // assume that BOA params are taken from file 0_.paQP
          dt.BOAState.loadParams(qcL.getQp()); // load parameters only once (but not frameinterval,)
          dt.BOAState.boap.setImageFrameInterval(qcL.getQp().getFrameInterval());
          dt.BOAState.boap.setImageScale(qcL.getQp().getImageScale());
        }
        logger.info(toString());
        // initialize snakes (from snQP files)
        logger.info("... Reading snakes");
        // check if ECMM was run on this snake file
        ecmmRun = qcL.getQp().verifyEcmminpsnQP();
        BOA_.qState = dt.BOAState; // for compatibility - create static
        oh = new OutlineHandler(qcL.getQp()); // restore OutlineHandler
        if (ecmmRun) {
          dt.ECMMState = new OutlinesCollection();
          dt.ECMMState.oHs.add(oh); // store in ECMM object
        } else {
          logger.info("... Reading snakes - no ECMM data");
        }
        dt.BOAState.nest.addOutlinehandler(oh); // covert ECMM to Snake and store in BOA section

        // load maps and store in QCONF
        stMap = new STmap();
        int readMap = 0; // check if we loaded at least one we expected 5 remaining as well
        try {
          logger.info("\tReading " + qcL.getQp().getMotilityFile().getName());
          stMap.setMotMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getMotilityFile()));
          readMap++;
        } catch (IOException e) {
          logger.info(e.getMessage());
          logger.debug(e.getMessage(), e);
        }
        try {
          logger.info("\tReading " + qcL.getQp().getConvexFile().getName());
          stMap.setConvMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getConvexFile()));
          readMap++;
        } catch (IOException e) {
          logger.info(e.getMessage());
          logger.debug(e.getMessage(), e);
        }
        try {
          logger.info("\tReading " + qcL.getQp().getCoordFile().getName());
          stMap.setCoordMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getCoordFile()));
          readMap++;
        } catch (IOException e) {
          logger.info(e.getMessage());
          logger.debug(e.getMessage(), e);
        }
        try {
          logger.info("\tReading " + qcL.getQp().getOriginFile().getName());
          stMap.setOriginMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getOriginFile()));
          readMap++;
        } catch (IOException e) {
          logger.debug(e.getMessage(), e);
          logger.info(e.getMessage());
        }
        try {
          logger.info("\tReading " + qcL.getQp().getxmapFile().getName());
          stMap.setxMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getxmapFile()));
          readMap++;
        } catch (IOException e) {
          logger.info(e.getMessage());
          logger.debug(e.getMessage(), e);
        }
        try {
          logger.info("\tReading " + qcL.getQp().getymapFile().getName());
          stMap.setyMap(QuimPArrayUtils.file2Array(",", qcL.getQp().getymapFile()));
          readMap++;
        } catch (IOException e) {
          logger.info(e.getMessage());
          logger.debug(e.getMessage(), e);
        }
        // number of read maps check (expecting all read if there is at leas one)
        if (readMap >= 1 && readMap < 6) {
          logger.warn("It seems that you have missing maps. Perhaps your dataset is incomplete. "
                  + "This may lead to invalid QCONF file.");
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
            // Fluoromap exist, so other must do as they are generated together, check this
            if (stMap.getT() == 0) { // no map loaded above (fluoro and other are generated togethe)
              logger.warn("It seems that you have fluorosence map but other maps are missing."
                      + " Perhaps your dataset is incomplete. "
                      + "This may lead to invalid QCONF file.");
            }
            break; // assume without checking that all maps are the same
          }
        }
        // if p,t are non zero we know that there is at least one map
        // try to read 3 channels for current paQP
        for (File ff : qcL.getQp().getFluFiles()) {
          // create Fluoro data holder
          FluoMap chm = new FluoMap(t, p, channel);
          if (ff.exists()) { // read file stored in paQP for channel
            logger.info("\tReading " + ff.getName());
            chm.setMap(QuimPArrayUtils.file2Array(",", ff)); // it sets it enabled
          } else {
            chm.setEnabled(false); // not existing, disable
            logger.info("File " + ff.toString() + " not found.");
          }
          stMap.fluoMaps[channel - 1] = chm;
          channel++;
        }
        // add container if there is at least one map
        if (stMap.getT() != 0 || t != 0) {
          maps.add(stMap);
        }
        // ANAState - add ANAp references for every processed paQP, set only non-transient fields
        logger.info("\tFilling ANA");
        ANAp anapTmp = new ANAp();
        anapTmp.scale = qcL.getQp().getImageScale(); // set scale used by setCortextWidthScale
        anapTmp.setCortextWidthScale(qcL.getQp().cortexWidth); // sets also cortexWidthPixel
        anapTmp.fluTiffs = qcL.getQp().fluTiffs; // set files
        anaP.aS.add(anapTmp); // store in ANAParamCollection

        i++; // go to next paQP
      } while (true); // exception thrown by QconfLoader will stop this loop, e.g. trying to load
      // nonexiting file

      // process stQP - all files
      logger.info("\tReading stats");

      StatFileParser obj = new StatFileParser(Paths.get(qcL.getQp().getPath(), orginal).toString());
      List<Path> statFiles = obj.getAllFiles(); // count stQP files
      // and do simple checking
      if (i != statFiles.size()) { // i counted from 0
        logger.warn("It seems that number of stQP files is different than number of paQP files."
                + " Perhaps your dataset is incomplete. This may lead to invalid QCONF file.");
      }
      ArrayList<CellStats> stats = obj.importStQp();
      dt.Stats = new StatsCollection();
      dt.Stats.setStatCollection(stats);

    } catch (

    Exception e) { // repack exception with proper message about defective file
      throw new QuimpException(
              "File " + filetoload.toString() + " can not be processed: " + e.getMessage(), e);
    }
    // do simple map checking - find if all (none) paQP had (had not) maps.
    int count = 0;
    for (STmap tmp : maps) {
      if (tmp.getT() == 0) {
        count++;
      } else {
        count--;
      }
    }
    if (Math.abs(count) != maps.size()) {
      logger.warn("It seems that some paQP cases have missing maps."
              + " Perhaps your dataset is incomplete. This may lead to invalid QCONF file.");
    }
    // save DataContainer using Serializer
    if (!maps.isEmpty()) {
      dt.QState = maps.toArray(new STmap[0]); // convert to array
    } else {
      dt.QState = null;
    }
    if (ecmmRun) {
      dt.ANAState = anaP;
    } else {
      dt.ANAState = null;
    }

    Serializer<DataContainer> n;
    n = new Serializer<>(dt, QuimP.TOOL_VERSION);
    n.setPretty();
    n.save(path + File.separator + orginal + FileExtensions.newConfigFileExt);
    n = null;
  }

  /**
   * Recreate paQP, snQP, stQP and maQP files from QCONF.
   * 
   * <p>Files are created in directory where QCONF is located.
   * 
   * @throws IOException on file saving error
   * @throws QuimpException if requested data are not available in QCONF
   * 
   */
  private void generateOldDataFiles() throws IOException, QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    logger.info("File is in new format, old format will be created");
    logger.info(toString());
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    if (dt.getEcmmState() == null) {
      logger.warn("ECMM analysis is not present in QCONF");
      generatepaQP(); // no ecmm data write snakes only
    } else {
      generatesnQP(); // write ecmm data
    }
    if (qcL.isStatsPresent()) {
      saveStats();
    } else {
      logger.warn("Statistics are not present in QCONF");
    }
    if (qcL.isQPresent()) {
      saveMaps(STmap.ALLMAPS);
    } else {
      logger.warn("Q analysis is not present in QCONF");
    }
  }

  /**
   * Save selected maps to files. Support only new file format. Throws
   * {@link IllegalArgumentException} when run from object constructed from old format.
   * 
   * <p>Follow naming convention: <i>ROOT_N_MAPNAME.maQP</i>, where <tt>ROOT</tt> is corename of
   * QCONF file,(<i>ROOT.QCONF</i>), <tt>N</tt> is cell number and <tt>MAPNAME</tt> follows
   * supported map extensions.
   * 
   * @param maps map defined in {@link STmap}
   * @throws QuimpException if maps are not available
   */
  public void saveMaps(int maps) throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    int activeHandler = 0;
    // replace location to location of QCONF
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.BOAState.boap.setOutputFileCore(path + File.separator + filename.toString());
    String name = STmap.LOGGER.getName();
    STmap.LOGGER = logger; // FIXME replace
    Qp params = new Qp();
    try {
      for (STmap stmap : qcL.getQ()) {
        ((QParamsQconf) qcL.getQp()).setActiveHandler(activeHandler++);
        stmap.setParams(params);
        params.setup(qcL.getQp());
        stmap.saveMaps(maps);
      }
    } finally {
      STmap.LOGGER = LoggerFactory.getLogger(name);
    }
  }

  /**
   * Save each snake centroid for each frame.
   * 
   * <p>Produce files /path/core_cellNo_boacentroid.csv
   * 
   * @throws QuimpException if BOA structure is not available
   */
  public void saveBoaCentroids() throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    int activeHandler = 0;
    CsvWritter csv = null;
    for (SnakeHandler sh : qcL.getBOA().nest.getHandlers()) {
      try {
        csv = new CsvWritter(getFeatureFileName("boacentroid", activeHandler, ".csv"), "#frame",
                "centroid_x", "centroid_y");
        logger.info("\tSaved Boa centroids at: " + csv.getPath().getFileName());
        int sf = sh.getStartFrame();
        int ef = sh.getEndFrame();
        for (int f = sf; f <= ef; f++) {
          Snake snake = sh.getStoredSnake(f);
          ExtendedVector2d centroid = snake.getCentroid();
          csv.writeLine((double) f, centroid.x, centroid.y);
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }

  }

  /**
   * Save all data associated with BOA analysis.
   * 
   * <p>Produce files /path/core_cellNo_snake-frame_no.csv with snake data for each frame separately
   * or files /path/core_cellNo_snake.csv with all data in one file (for same snake).
   * 
   * <p>Output file contain only parameters directly included in QCONF in contrary to e.g. snQP
   * files (or results of QCONF->paQP conversion) that contain some extra data calculated.
   * 
   * @param separateFiles Control whether files should be broken into separate files for each frame
   *        (true) and for each object or store all frames in one file (false).
   * @throws QuimpException if BOA structure is not available
   */
  public void saveBoaSnakes(boolean separateFiles) throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    //!> order of data, must follow writeLine below
    final String[] params = {
        "vel_x",
        "vel_y",
        "F-total_x",
        "F-total_y",
        "node_x",
        "node_y",
        "normal_x",
        "normal_y",
        "tan_x",
        "tan_y",
        "position",
        "frozen"};
    //!<
    CsvWritter csv = null;
    int activeHandler = 0;
    for (SnakeHandler sh : qcL.getBOA().nest.getHandlers()) {
      int sf = sh.getStartFrame();
      int ef = sh.getEndFrame();
      try {
        for (int f = sf; f <= ef; f++) {
          Snake snake = sh.getStoredSnake(f);
          if (separateFiles == true) { // create for each frame
            csv = new CsvWritter(getFeatureFileName("snake-frame" + f, activeHandler, ".csv"),
                    params);
          } else if (f == sf) { // create only once on first frame
            csv = new CsvWritter(getFeatureFileName("snake", activeHandler, ".csv"), params);

          }
          if (separateFiles == false) {
            csv.writeLine("#frame " + f); // just add break if one file outputed
          }
          logger.info("\tSaved snakes at: " + csv.getPath().getFileName());
          Iterator<Node> it = snake.iterator();
          while (it.hasNext()) {
            Node n = it.next();
            //!>
            csv.writeLine(
                    n.getVel().x,
                    n.getVel().y,
                    n.getF_total().x,
                    n.getF_total().y,
                    n.getPoint().x,
                    n.getPoint().y,
                    n.getNormal().x,
                    n.getNormal().y,
                    n.getTangent().x,
                    n.getTangent().y,
                    n.getPosition(),
                    n.isFrozen() ? 1.0 : 0.0);
            //!<
          }
          if (separateFiles == true) {
            csv.close(); // after frame
          }
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }
  }

  /**
   * Save all data associated with ECMM and ANA analysis.
   * 
   * <p>Produce files /path/core_cellNo_outline-frame_no.csv with snake data for each frame
   * separately or files /path/core_cellNo_outline.csv with all data in one file (for same snake).
   * 
   * <p>Output file contain only parameters directly included in QCONF in contrary to e.g. snQP
   * files (or results of QCONF->paQP conversion) that contain some extra data calculated.
   * 
   * @param separateFiles Control whether files should be broken into separate files for each frame
   *        (true) and for each object or store all frames in one file (false).
   * @throws QuimpException if ECMM has not been run
   */
  public void saveEcmmOutlines(boolean separateFiles) throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    CsvWritter csv = null;
    int activeHandler = 0;
    for (OutlineHandler oh : qcL.getEcmm().oHs) {
      int sf = oh.getStartFrame();
      int ef = oh.getEndFrame();
      try {
        for (int f = sf; f <= ef; f++) {
          Outline outline = oh.getStoredOutline(f);
          if (separateFiles == true) { // create for each frame
            csv = new CsvWritter(getFeatureFileName("outline-frame" + f, activeHandler, ".csv"),
                    headerEcmmOutline);
          } else if (f == sf) { // create only once on first frame
            csv = new CsvWritter(getFeatureFileName("outline", activeHandler, ".csv"),
                    headerEcmmOutline);
          }
          if (separateFiles == false) {
            csv.writeLine("#frame " + f); // just add break if one file outputed
          }
          saveOutline(outline, csv);
          if (separateFiles == true) {
            csv.close(); // after frame
          }
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }
  }

  /**
   * Save specified outline to {@link CsvWritter}.
   * 
   * @param outline outline to save
   * @param csv opened csv object
   */
  public static void saveOutline(Outline outline, CsvWritter csv) {
    logger.info("\tSaved outlines at: " + csv.getPath().getFileName());
    Iterator<Vert> it = outline.iterator();
    while (it.hasNext()) {
      Vert n = it.next();
      //!>
      csv.writeLine(
              n.charge,
              n.distance,
              n.fluores[0].x,
              n.fluores[0].y,
              n.fluores[0].intensity,
              n.fluores[1].x,
              n.fluores[1].y,
              n.fluores[1].intensity,
              n.fluores[2].x,
              n.fluores[2].y,
              n.fluores[2].intensity,
              n.getCurvatureLocal(),
              n.curvatureSmoothed,
              n.curvatureSum,
              n.coord,
              n.gLandCoord,
              n.getPoint().x,
              n.getPoint().y,
              n.getNormal().x,
              n.getNormal().y,
              n.getTangent().x,
              n.getTangent().y,
              n.getPosition(),
              n.isFrozen() ? 1.0 : 0.0);
      //!<
    }
  }

  /**
   * Save statistic data associated with ANA analysis for all three channels.
   * 
   * <p>Produce files /path/core_cellNo_fluostats.csv with ANA data data for each cell along frames.
   * 
   * <p>Output file contains raw parameters that are also available in stQP file but not exactly the
   * same as some of stQP parameters are computed from those raw.
   * 
   * @throws QuimpException if stats are not available. Note that if ANA has not been run this
   *         method can still produce valid but empty output.
   * @see #saveStats()
   * @see #saveStatGeom()
   */
  public void saveStatFluores() throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    //!> order of data, must follow writeLine below
    final String[] params = {
        "#frame",
        "innerAreaCh1",
        "totalFluorCh1",
        "cortexWidthCh1",
        "meanFluorCh1",
        "meanInnerFluorCh1",
        "totalInnerFluorCh1",
        "cortexAreaCh1",
        "totalCorFluoCh1",
        "meanCorFluoCh1", 
        "percCortexFluoCh1", 
        "innerAreaCh2",
        "totalFluorCh2",
        "cortexWidthCh2",
        "meanFluorCh2",
        "meanInnerFluorCh2",
        "totalInnerFluorCh2",
        "cortexAreaCh2",
        "totalCorFluoCh2",
        "meanCorFluoCh2", 
        "percCortexFluoCh2",
        "innerAreaCh3",
        "totalFluorCh3",
        "cortexWidthCh3",
        "meanFluorCh3",
        "meanInnerFluorCh3",
        "totalInnerFluorCh3",
        "cortexAreaCh3",
        "totalCorFluoCh3",
        "meanCorFluoCh3", 
        "percCortexFluoCh3" 
        };
    //!<
    CsvWritter csv = null;
    int activeHandler = 0;
    StatsCollection st = qcL.getStats();
    for (CellStats cs : st.getStatCollection()) { // along cells
      try {
        csv = new CsvWritter(getFeatureFileName("fluostats", activeHandler, ".csv"), params);
        logger.info("\tSaved fluorosence stats at: " + csv.getPath().getFileName());
        for (FrameStatistics fs : cs.getFramestat()) { // along frames
          ChannelStat[] ch = fs.channels;
          //!>
          csv.writeLine(
                  (double)fs.frame,
                  ch[0].innerArea,
                  ch[0].totalFluor,
                  ch[0].cortexWidth,
                  ch[0].meanFluor,
                  ch[0].meanInnerFluor,
                  ch[0].totalInnerFluor,
                  ch[0].cortexArea,
                  ch[0].totalCorFluo,
                  ch[0].meanCorFluo,
                  ch[0].percCortexFluo,
                  ch[1].innerArea,
                  ch[1].totalFluor,
                  ch[1].cortexWidth,
                  ch[1].meanFluor,
                  ch[1].meanInnerFluor,
                  ch[1].totalInnerFluor,
                  ch[1].cortexArea,
                  ch[1].totalCorFluo,
                  ch[1].meanCorFluo,
                  ch[1].percCortexFluo,
                  ch[2].innerArea,
                  ch[2].totalFluor,
                  ch[2].cortexWidth,
                  ch[2].meanFluor,
                  ch[2].meanInnerFluor,
                  ch[2].totalInnerFluor,
                  ch[2].cortexArea,
                  ch[2].totalCorFluo,
                  ch[2].meanCorFluo,
                  ch[2].percCortexFluo
          );
          //!<
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }
  }

  /**
   * Save statistic data associated with ECMM analysis for all three channels.
   * 
   * <p>Produce files /path/core_cellNo_geomstats.csv with ANA data data for each cell along frames.
   * 
   * <p>Output file contains raw parameters that are also available in stQP file but not exactly the
   * same as some of stQP parameters are computed from those raw.
   * 
   * @throws QuimpException if stats are not available. Note that if ANA has not been run this
   *         method can still produce valid but empty output.
   * @see #saveStats()
   * @see #saveStatFluores()
   */
  public void saveStatGeom() throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    //!> order of data, must follow writeLine below
    final String[] params = {
        "#frame",
        "area",
        "elongation",
        "circularity",
        "perimiter",
        "displacement",
        "dist",
        "persistance",
        "speed",
        "persistanceToSource", 
        "dispersion", 
        "extension",
        "centroid_x",
        "centroid_y"
        };
    //!<
    CsvWritter csv = null;
    int activeHandler = 0;
    StatsCollection st = qcL.getStats();
    for (CellStats cs : st.getStatCollection()) { // along cells
      try {
        csv = new CsvWritter(getFeatureFileName("geomstats", activeHandler, ".csv"), params);
        logger.info("\tSaved geometrical stats at: " + csv.getPath().getFileName());
        for (FrameStatistics fs : cs.getFramestat()) { // along frames
          //!>
          csv.writeLine(
                  (double)fs.frame,
                  fs.area,
                  fs.elongation,
                  fs.circularity,
                  fs.perimiter,
                  fs.displacement,
                  fs.dist,
                  fs.persistance,
                  fs.speed,
                  fs.persistanceToSource,
                  fs.dispersion,
                  fs.extension,
                  fs.centroid.x,
                  fs.centroid.y
          );
        //!<
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }
  }

  /**
   * Save each outline centroid for each frame.
   * 
   * <p>Produce files /path/core_cellNo_ecmmcentroid.csv
   * 
   * @throws QuimpException if ECMM has not been run
   */
  public void saveEcmmCentroids() throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("New format required.");
    }
    int activeHandler = 0;
    CsvWritter csv = null;
    for (OutlineHandler oh : qcL.getEcmm().oHs) {
      try {
        csv = new CsvWritter(getFeatureFileName("ecmmcentroid", activeHandler, ".csv"), "#frame",
                "centroid_x", "centroid_y");
        logger.info("\tSaved ecmm centroids at: " + csv.getPath().getFileName());
        int sf = oh.getStartFrame();
        int ef = oh.getEndFrame();
        for (int f = sf; f <= ef; f++) {
          Outline outline = oh.getStoredOutline(f);
          ExtendedVector2d centroid = outline.getCentroid();
          csv.writeLine((double) f, centroid.x, centroid.y);
        }
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
        if (csv != null) {
          csv.close();
        }
      }
    }

  }

  /**
   * Create stQP file using internally stored Stats from QCONF.
   * 
   * @throws QuimpException when write of stQP file failed
   * @see #saveStatFluores()
   * @see #saveStatGeom()
   */
  public void saveStats() throws QuimpException {
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    int activeHandler = 0;
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    Iterator<CellStats> csI = qcL.getStats().getStatCollection().iterator();
    do {
      Path p = getFeatureFileName("", activeHandler, FileExtensions.statsFileExt);
      ((QParamsQconf) qcL.getQp()).setActiveHandler(activeHandler);
      CellStats cs = csI.next();
      try {
        FrameStatistics.write(cs.getFramestat().toArray(new FrameStatistics[0]),
                ((QParamsQconf) qcL.getQp()).getStatsQP(), dt.BOAState.boap.getImageScale(),
                dt.BOAState.boap.getImageFrameInterval());
        logger.info("\tSaved stats at: " + p.getFileName());
      } catch (IOException e) {
        logger.error("Can not write file");
      } finally {
        activeHandler++;
      }
    } while (csI.hasNext());

  }

  /**
   * Produce file name basing on loaded QCONF (in the same folder and with the same core) extending
   * it by _cellNo and featName. Initializes also {@link BOAState} structures.
   * 
   * @param featName /path/core_cellNo_featName.ext
   * @param cellNo /path/core_cellNo_featName.ext
   * @param ext /path/core_cellNo_featName.ext (with dot)
   * @return /path/core_cellNo_featName.ext
   */
  Path getFeatureFileName(String featName, int cellNo, String ext) {
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.BOAState.boap.setOutputFileCore(path + File.separator + filename.toString());
    String fi = dt.BOAState.boap.getOutputFileCore().toPath().toString();
    fi = fi + "_" + cellNo + "_" + featName + ext;
    return Paths.get(fi);
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
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    logger.info("\tCreating configuration files");
    // replace location to location of QCONF
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.getBOAState().boap.setOutputFileCore(path + File.separator + filename.toString());
    logger.info("\tCreating snake files");
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
    if (qcL.isFileLoaded() == QParams.QUIMP_11) {
      throw new IllegalArgumentException("Can not convert from old format to old");
    }
    int activeHandler = 0;
    // replace location to location of QCONF
    DataContainer dt = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
    dt.BOAState.boap.setOutputFileCore(path + File.separator + filename.toString());
    Iterator<OutlineHandler> ohi = dt.getEcmmState().oHs.iterator();
    do {
      logger.info("\tCreating snake file " + activeHandler);
      ((QParamsQconf) qcL.getQp()).setActiveHandler(activeHandler);
      OutlineHandler oh = ohi.next();
      oh.writeOutlines(((QParamsQconf) qcL.getQp()).getSnakeQP(), true);
      logger.info("\tCreating parameter file " + activeHandler);
      ((QParamsQconf) qcL.getQp()).writeOldParams();
      activeHandler++;
    } while (ohi.hasNext());

  }

  /**
   * Perform conversion depending on which file has been loaded.
   * 
   * <p>Supported conversions:
   * QCONF->paQP -- paQP, snQP, stQP, maQP files are generated (if data present in QCONF)
   * paQP->QCONF -- paQP, snQP, stQP, maQP are supported
   * 
   * @throws QuimpException on every error redirected to GUI. This is final method called from
   *         caller. All exceptions during conversion are collected and converted here to GUI.
   */
  public void doConversion() throws QuimpException {
    try {
      switch (qcL.isFileLoaded()) {
        case QParams.NEW_QUIMP:
          generateOldDataFiles();
          break;
        case QParams.QUIMP_11:
          Map<Integer, String> ret = QconfLoader.validatePaqp(
                  qcL.getQp().getPathasPath().resolve(qcL.getQp().getParamFile().toPath()));
          if (!ret.isEmpty()) {
            logger.warn("Sanity check returned the following warnings for paQP structure:");
            ret.values().stream().forEach(i -> Arrays.asList(i.split(QconfLoader.SEPARATOR))
                    .stream().forEach(j -> logger.warn(j)));
            logger.warn("FormatConverter will try to convert such file but it may"
                    + " lead to invalid QCONF file");
          }
          generateNewDataFiles();
          break;
        default:
          throw new IllegalArgumentException(
                  "QconfLoader returned unknown version of QuimP or error: " + qcL.isFileLoaded());
      }
    } catch (IOException qe) {
      throw new QuimpException(qe);
    }
  }

  /**
   * Return type of loaded file or 0 if not loaded yet.
   * 
   * @return {@link QconfLoader#QCONF_INVALID} or {@link QParams#NEW_QUIMP},
   *         {@link QParams#QUIMP_11}
   */
  public int isFileLoaded() {
    if (qcL == null) {
      return QconfLoader.QCONF_INVALID;
    } else {
      return qcL.isFileLoaded();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String ret = "";
    switch (qcL.isFileLoaded()) {
      case QParams.NEW_QUIMP:
        ret = ret.concat("Experiment date: ")
                .concat(((QParamsQconf) qcL.getQp()).getFileVersion().getBuildstamp()).concat("\n");
        ret = ret.concat("File version: ")
                .concat(((QParamsQconf) qcL.getQp()).getFileVersion().getVersion()).concat("\n");
        ret = ret.concat("Is BOA analysis present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.isBOAPresent())).concat("\n");
        ret = ret.concat("Is ECMM analysis present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.isECMMPresent())).concat("\n");
        ret = ret.concat("Is ANA analysis present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.isANAPresent())).concat("\n");
        ret = ret.concat("Is Q analysis present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.isQPresent())).concat("\n");
        ret = ret.concat("Are stats present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.isStatsPresent())).concat("\n");
        return ret;
      case QParams.QUIMP_11:
        ret = ret.concat(qcL.getQp().getFileName()).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getParamFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getParamFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getSnakeQP().getName()).concat(" present? ")
                .concat(" -- ").concat(Boolean.toString(qcL.getQp().getSnakeQP().exists()))
                .concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getStatsQP().getName()).concat(" present? ")
                .concat(" -- ").concat(Boolean.toString(qcL.getQp().getStatsQP().exists()))
                .concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getMotilityFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getMotilityFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getConvexFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getConvexFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getCoordFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getCoordFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getOriginFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getOriginFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getxmapFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getxmapFile().exists())).concat("\n");
        ret = ret.concat("Is file ").concat(qcL.getQp().getymapFile().getName())
                .concat(" present? ").concat(" -- ")
                .concat(Boolean.toString(qcL.getQp().getymapFile().exists())).concat("\n");
        File[] tmpf = qcL.getQp().getFluFiles();
        for (File f : tmpf) {
          ret = ret.concat("Is file ").concat(f.getName()).concat(" present? ").concat(" -- ")
                  .concat(Boolean.toString(f.exists())).concat("\n");
        }
        return ret;
      case QParams.OLD_QUIMP:
        ret = "toString is not supported for this format";
        return ret;
      default:
        return "No file loaded or file damaged";
    }
  }

}
