package com.github.celldynamics.quimp.filesystem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOAState.BOAp;
import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.plugin.bar.QuimP_Bar;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.OpenDialog;

/**
 * Load QCONF or paQP file and initiate proper instance of {@link QParams} class.
 * 
 * <p>Provide also methods for QCONF verification and loading image file associated with it with
 * user
 * assistance.
 * 
 * @author p.baniukiewicz
 *
 */
public class QconfLoader {

  private Path qconfFile; // path to loaded file

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QconfLoader.class.getName());
  /**
   * Stand for bad QCONF file that can not be loaded.
   */
  public static final int QCONF_INVALID = 0; // Must be 0
  /**
   * Stand for bad paQP experiment that has some files missing.
   */
  public static final int PAQP_INVALID = QCONF_INVALID; // Must be 0
  /**
   * Stand for good paQP experiment that has all files.
   */
  public static final int PAQP_VALID = 2;
  /**
   * Stand for missing file in experiment.
   */
  public static final int SNQP_MISSING = 4;
  /**
   * Stand for missing file in experiment.
   */
  public static final int STQP_MISSING = 8;
  /**
   * Stand for missing file in experiment.
   */
  public static final int MAP_MISSING = 16;
  /**
   * Separator for error messages.
   * 
   * @see #validatePaqp(Path)
   */
  public static final String SEPARATOR = ";";
  /**
   * Main object holding loaded configuration file. It can be either traditional QParams or
   * QParamsQconf for newer format.
   */
  private QParams qp = null;

  /**
   * Default constructor.
   * 
   * <p>Bring file dialog to load QCONF.
   * 
   * @throws QuimpException when QCONF can not be loaded
   */
  public QconfLoader() throws QuimpException {
    this(null);
  }

  /**
   * Parametrised constructor. Allow to choose file selector filter.
   * 
   * @param file File *.paQP/QCONF. If <tt>null</tt> user is asked for this file.
   * @param fileExt pre-selection extension or <tt>null</tt> to use default selected in QuimP_Bar.
   * @throws QuimpException when file can not be loaded
   */
  public QconfLoader(File file, String fileExt) throws QuimpException {
    loader(file, fileExt);
  }

  /**
   * Parameterised constructor. Assume that active extension for configuration file is set by
   * QuimP_Bar.
   * 
   * @param file File *.paQP/QCONF. If <tt>null</tt> user is asked for this file
   * @throws QuimpException when file can not be loaded
   */
  public QconfLoader(File file) throws QuimpException {
    loader(file, null); // use default filter set in QuimP_Bar
  }

  /**
   * File loaded and initialiser for this class.
   * 
   * @param file File *.paQP/QCONF. If <tt>null</tt> user is asked for this file.
   * @param fileExt pre-selection extension or null to use default selected in QuimP_Bar.
   * @throws QuimpException when file can not be loaded
   * @see QuimP_Bar
   */
  private void loader(File file, String fileExt) throws QuimpException {
    String directory; // directory with paQP
    String filename; // file name of paQP

    if (file == null) { // no file provided, ask user
      FileDialogEx od = new FileDialogEx(IJ.getInstance(), fileExt);
      od.setDirectory(OpenDialog.getLastDirectory());
      if (od.showOpenDialog() == null) {
        IJ.log("Cancelled - exiting...");
        return;
      }
      directory = od.getDirectory();
      filename = od.getFile();
    } else { // use name provided in constructor
      Path path = file.toPath();
      // getParent can return null
      directory = (path.getParent() == null) ? "." : path.getParent().toString();
      if (path.getFileName() == null) {
        throw new QuimpException("Can not get file name to load: " + path.toString());
      }
      filename = path.getFileName().toString();
      LOGGER.debug("Use provided file:" + directory + " " + filename);
    }
    // detect old/new file format
    File paramFile = new File(directory, filename); // config file (copy of input)
    // TODO #152
    if (paramFile.getName().toLowerCase().endsWith(FileExtensions.newConfigFileExt.toLowerCase())) {
      qp = new QParamsQconf(paramFile);
    } else {
      qp = new QParams(paramFile); // initialize general param storage
    }
    qp.readParams(); // create associated files included in paQP and read params
    qconfFile = paramFile.toPath();
  }

  /**
   * Try to load image associated with QCONF or paQP file.
   * 
   * <p>If image has not been found, user is being asked to point relevant file. If file is loaded
   * from disk it updates <tt>orgFile</tt> in {@link BOAp}.
   * 
   * <p>If run in testing mode it tries to load an image from folder where QCONF is. Do not display
   * UI.
   * 
   * @return Loaded image from QCONF or that pointed by user. <tt>null</tt> if user cancelled or
   *         image has not been found.
   */
  public ImagePlus getImage() {
    if (getQp() == null) {
      return null;
    }
    ImagePlus im;
    File imagepath = null;
    switch (getQp().getParamFormat()) {
      case QParams.NEW_QUIMP:
        imagepath = ((QParamsQconf) qp).getLoadedDataContainer().getBOAState().boap.getOrgFile();
        break;
      case QParams.QUIMP_11:
        imagepath = qp.getSegImageFile();
        break;
      default:
        throw new IllegalArgumentException("Format not supported");
    }

    LOGGER.debug("Attempt to open image: " + imagepath.toString());
    // try to load from QCONF or paQP
    im = IJ.openImage(imagepath.getPath());

    if (im == null) { // if failed ask user
      // but first check against testing mode
      String skipReg = new PropertyReader().readProperty("quimpconfig.properties", "noRegWindow");
      if (Boolean.parseBoolean(skipReg) == true) {
        Path imName = imagepath.toPath().getFileName();
        Path dir = (qconfFile.getParent() == null) ? Paths.get(".") : qconfFile.getParent();
        LOGGER.debug("Testing mode, looking for image: " + dir.resolve(imName).toString());
        im = IJ.openImage(dir.resolve(imName).toString());
        return im; // do not modify paths in boap in testing mode
      }
      Object[] options = { "Load from disk", "Load from IJ", "Cancel" };
      int n = JOptionPane.showOptionDialog(IJ.getInstance(),
              "The image " + imagepath.getName()
                      + " pointed in loaded configuration file can not be found.\n"
                      + "Would you like to load it manually?",
              "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
              options, options[2]);
      if (n == JOptionPane.YES_OPTION) { // load from disk
        LOGGER.trace("Load from disk");
        OpenDialog od = new OpenDialog("Open image", OpenDialog.getLastDirectory(), "");
        if (od.getFileName() == null) {
          return null;
        }
        im = IJ.openImage(od.getDirectory() + od.getFileName());

      }
      if (n == JOptionPane.NO_OPTION) { // or open from ij
        LOGGER.trace("Load from IJ");
        Object[] images = WindowManager.getImageTitles();
        images = (images.length == 0) ? new Object[1] : images;
        Object message = "Select image";
        String s = (String) JOptionPane.showInputDialog(IJ.getInstance(), message,
                "Avaiable images", JOptionPane.PLAIN_MESSAGE, null, images, images[0]);
        im = WindowManager.getImage(s);

      }
      // replace old image paths in QCONF to new one
      if (im != null) {
        Path orgFile;
        FileInfo fileinfo = im.getOriginalFileInfo();
        if (fileinfo == null) {
          orgFile = Paths.get(File.separator, im.getTitle());
        } else {
          orgFile = Paths.get(fileinfo.directory, fileinfo.fileName);
        }
        try {
          if (isBOAPresent()) {
            getBOA().boap.setOrgFile(orgFile.toFile());
          }
        } catch (QuimpException e) {
          throw new Error(); // should never be here, we know there is BOA and we are on new path
        }
      }
    }
    LOGGER.debug("Opened image: " + im);
    return im;

  }

  /**
   * Validate loaded QCONF file in accordance to modules run on it.
   * 
   * <p>For certain cases this method may not be able to verify if QCONF is valid. This may happen
   * if QCONF was obtained from paQP files, where some of experiment files were missing (this should
   * no happen).
   * 
   * @return Values:
   *         <ol>
   *         <li>0 if QCONF is not loaded properly.
   *         <li>QParams.QUIMP_11 if it is in old format
   *         <li>{@link DataContainer#validateDataContainer()} flags otherwise
   *         </ol>
   * 
   * @see FormatConverter - may return defective QCONF with warnings.
   */
  public int validateQconf() {
    if (getQp() == null) {
      return QconfLoader.QCONF_INVALID;
    }
    if (getQp().getParamFormat() != QParams.NEW_QUIMP) {
      return QParams.QUIMP_11;
    }
    return ((QParamsQconf) getQp()).getLoadedDataContainer().validateDataContainer();
  }

  /**
   * Perform blind validation of accessible files without reading them.
   * 
   * <p>Check if for each cell in same experiment (identified by provided full name
   * /path/name_0.paQP) all other corresponding files exist.
   * 
   * @param firstFile full path to first paQP file in experiment
   * @return Map with keys defined in in this class: {@value #PAQP_INVALID}, {@value #PAQP_VALID},
   *         {@value #SNQP_MISSING}, {@value #STQP_MISSING}, {@value #MAP_MISSING} and String values
   *         in format problem description; problem description. E.g if two maps are missing both
   *         are logged in value for {@value #MAP_MISSING}. Empty Map stands for proper experiment
   *         structure.
   */
  public static Map<Integer, String> validatePaqp(Path firstFile) {
    HashMap<Integer, String> ret = new HashMap<>();

    Path folder = firstFile.getParent();
    Path corep = firstFile.getFileName();
    if (folder == null || corep == null) {
      throw new IllegalArgumentException("Wrong path");
    }
    String core = corep.toString();
    int up = core.lastIndexOf('_');
    if (up <= 0) {
      ret.put(PAQP_INVALID, "Incorect name."); // wrong name?
      return ret;
    } else {
      core = core.substring(0, up); // remove _0 from name
    }
    // iterate over paQP
    int l = 0;
    File file = folder.resolve(core + "_" + l + FileExtensions.configFileExt).toFile();
    int hadMap = 0;
    while (file.exists()) {
      // check snQP
      file = folder.resolve(core + "_" + l + FileExtensions.snakeFileExt).toFile();
      if (!file.exists()) {
        String prev = ret.get(SNQP_MISSING) == null ? "" : ret.get(SNQP_MISSING);
        prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
        ret.put(SNQP_MISSING, prev);
      }
      // check stQP
      file = folder.resolve(core + "_" + l + FileExtensions.statsFileExt).toFile();
      if (!file.exists()) {
        String prev = ret.get(STQP_MISSING) == null ? "" : ret.get(STQP_MISSING);
        prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
        ret.put(STQP_MISSING, prev);
      }
      // check maps
      // check if there is at least one
      if (folder.resolve(core + "_" + l + FileExtensions.convmapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.motmapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.coordmapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.originmapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.xmapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.ymapFileExt).toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.fluomapFileExt.replace('%', '1'))
                      .toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.fluomapFileExt.replace('%', '2'))
                      .toFile().exists()
              || folder.resolve(core + "_" + l + FileExtensions.fluomapFileExt.replace('%', '3'))
                      .toFile().exists()) {
        // so we expect all (except flumaps)
        hadMap++; // at least one paQP has maps
        file = folder.resolve(core + "_" + l + FileExtensions.convmapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
        file = folder.resolve(core + "_" + l + FileExtensions.motmapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
        file = folder.resolve(core + "_" + l + FileExtensions.coordmapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
        file = folder.resolve(core + "_" + l + FileExtensions.originmapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
        file = folder.resolve(core + "_" + l + FileExtensions.xmapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
        file = folder.resolve(core + "_" + l + FileExtensions.ymapFileExt).toFile();
        if (!file.exists()) {
          String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
          prev = prev.concat(SEPARATOR).concat("Missing " + file.getName() + " file");
          ret.put(MAP_MISSING, prev);
        }
      } else {
        hadMap--;
      }
      l++;
      file = folder.resolve(core + "_" + l + FileExtensions.configFileExt).toFile();
    }
    if (l == 0) {
      ret.put(PAQP_INVALID, "First file " + file.getName() + " is missing.");
    }
    // check case if one paQP does not have maps but other has
    if (Math.abs(hadMap) != l) {
      String prev = ret.get(MAP_MISSING) == null ? "" : ret.get(MAP_MISSING);
      prev = prev.concat(SEPARATOR).concat(
              "All maps are missing for one or more paQP files whereas avilable for other cases");
      ret.put(MAP_MISSING, prev);
    }
    return ret; // if size 0 - no issues

  }

  /**
   * Just decoder of
   * {@link com.github.celldynamics.quimp.filesystem.DataContainer#validateDataContainer()}.
   * 
   * @return true if BOA module was run.
   */
  public boolean isBOAPresent() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11) {
      return false;
    }
    if ((ret & DataContainer.BOA_RUN) == DataContainer.BOA_RUN) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Just decoder of
   * {@link com.github.celldynamics.quimp.filesystem.DataContainer#validateDataContainer()}.
   * 
   * @return true if ECMM module was run.
   */
  public boolean isECMMPresent() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11) {
      return false;
    }
    if ((ret & DataContainer.ECMM_RUN) == DataContainer.ECMM_RUN) {
      return true;
    } else {
      return false;
    }

  }

  /**
   * Just decoder of
   * {@link com.github.celldynamics.quimp.filesystem.DataContainer#validateDataContainer()}.
   * 
   * @return true if ANA module was run.
   */
  public boolean isANAPresent() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11) {
      return false;
    }
    if ((ret & DataContainer.ANA_RUN) == DataContainer.ANA_RUN) {
      return true;
    } else {
      return false;
    }

  }

  /**
   * Just decoder of
   * {@link com.github.celldynamics.quimp.filesystem.DataContainer#validateDataContainer()}.
   * 
   * @return true if Q module was run.
   */
  public boolean isQPresent() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11) {
      return false;
    }
    if ((ret & DataContainer.Q_RUN) == DataContainer.Q_RUN) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Just decoder of
   * {@link com.github.celldynamics.quimp.filesystem.DataContainer#validateDataContainer()}.
   * 
   * @return true if stats are present.
   */
  public boolean isStatsPresent() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11) {
      return false;
    }
    if ((ret & DataContainer.STATS_AVAIL) == DataContainer.STATS_AVAIL) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Query for BOA object.
   * 
   * @return BOAState object from loaded configuration
   * @throws QuimpException when there is no such object in file or old format is used.
   */
  public BOAState getBOA() throws QuimpException {
    if (isBOAPresent()) {
      return ((QParamsQconf) getQp()).getLoadedDataContainer().getBOAState();
    } else {
      throw new QuimpException("BOA data not found in QCONF file. Run BOA first.");
    }
  }

  /**
   * Query for ECMM object.
   * 
   * @return ECMM object from loaded configuration
   * @throws QuimpException when there is no such object in file or old format is used.
   */
  public OutlinesCollection getEcmm() throws QuimpException {
    if (isECMMPresent()) {
      return ((QParamsQconf) getQp()).getLoadedDataContainer().getEcmmState();
    } else {
      throw new QuimpException("ECMM data not found in QCONF file. Run ECMM first.");
    }
  }

  /**
   * Query for ANA object.
   * 
   * @return ANA object from loaded configuration
   * @throws QuimpException when there is no such object in file or old format is used.
   */
  public ANAParamCollection getANA() throws QuimpException {
    if (isANAPresent()) {
      return ((QParamsQconf) getQp()).getLoadedDataContainer().getANAState();
    } else {
      throw new QuimpException("ANA data not found in QCONF file. Run ANA first.");
    }
  }

  /**
   * Query for Q object.
   * 
   * @return Q object from loaded configuration
   * @throws QuimpException when there is no such object in file or old format is used.
   */
  public STmap[] getQ() throws QuimpException {
    if (isQPresent()) {
      return ((QParamsQconf) getQp()).getLoadedDataContainer().getQState();
    } else {
      throw new QuimpException("Q data not found in QCONF file. Run Q Analysis first.");
    }
  }

  /**
   * Query for Stats object.
   * 
   * @return Stats object from loaded configuration
   * @throws QuimpException when there is no such object in file or old format is used.
   */
  public StatsCollection getStats() throws QuimpException {
    if (isStatsPresent()) {
      return ((QParamsQconf) getQp()).getLoadedDataContainer().getStats();
    } else {
      throw new QuimpException("Stats not found in QCONF file. Run BOA Analysis first.");
    }
  }

  /**
   * Return QParams object.
   * 
   * @return the qp, can be null if loading dialog was cancelled.
   */
  public QParams getQp() {
    return qp;
  }

  /**
   * Return type of loaded file or 0 if not loaded yet.
   * 
   * @return {@link QconfLoader#QCONF_INVALID} or {@link QParams#NEW_QUIMP},
   *         {@link QParams#QUIMP_11}
   */
  public int isFileLoaded() {
    int ret = validateQconf();
    if (ret == QconfLoader.QCONF_INVALID) {
      return QconfLoader.QCONF_INVALID;
    } else {
      return getQp().getParamFormat();
    }
  }

  /**
   * Return path to loaded configuration file. A value here does not mean that file has been
   * successfully loaded.
   * 
   * @return path to loaded file
   * @see QParamsQconf#getParamFile()
   */
  public Path getQconfFile() {
    return qconfFile;
  }

}
