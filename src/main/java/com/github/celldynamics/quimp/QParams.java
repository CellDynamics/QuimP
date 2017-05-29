package com.github.celldynamics.quimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState.BOAp;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;

/**
 * Container class for parameters defining the whole process of analysis in QuimP. Stores parameters
 * read from configuration files and provide them to different modules. Supports writing and reading
 * segmentation parameters from files (paQP). This class defines file format used for storing
 * parameters in file. Object of this class is used for creating local configuration objects for
 * ECMM and QAnalysis modules. Process only main paQP file. QuimP uses several files to store
 * segmentation results and algorithm parameters:
 * <ul>
 * <li>.paQP - core file, contains reference to images and parameters of algorithm. This file is
 * saved and processed by QParams class</li>
 * <li>.snQP - contains positions of all nodes for every frame</li>
 * <li>.stQP - basic shape statistics for every frame</li>
 * <li>.mapQP - maps described in documentation</li>
 * </ul>
 * 
 * <p>This class exists for compatibility purposes. Allows reading old files. There is also child
 * class
 * QParamsEsxhanger that is based on new file format. Because QParams is strongly integrated with
 * QuimP it has been left.
 * 
 * @author rtyson
 * @see BOAp
 * 
 */
public class QParams {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QParams.class.getName());

  /**
   * The Constant OLD_QUIMP. Denotes version of QuimP < Q11
   */
  public static final int OLD_QUIMP = 1;

  /**
   * The Constant QUIMP_11. Denotes version Q11
   */
  public static final int QUIMP_11 = 2;

  /**
   * The Constant NEW_QUIMP. Denotes version > Q11
   */
  public static final int NEW_QUIMP = 3;
  /**
   * Name of the case. Used to set <i>fileName</i> and <i>path</i>
   */
  private File paramFile;
  /**
   * Indicates format of data file.
   */
  public int paramFormat;
  /**
   * Name of the data file - without path and extension. Equals to name of the case. If
   * initialised from {@link QParamsQconf} contains underscored cell number as well.
   * 
   * @see com.github.celldynamics.quimp.BOAState.BOAp
   */
  private String fileName;
  /**
   * Path where user files exist.
   * 
   * @see com.github.celldynamics.quimp.BOAState.BOAp
   */
  private String path;
  private File segImageFile;
  private File snakeQP;

  /**
   * The stats QP.
   */
  protected File statsQP;
  /**
   * This field is set by direct call from ANA. Left here for compatibility reasons. Main holder
   * of fluTiffs is {@link com.github.celldynamics.quimp.plugin.ana.ANAp}
   */
  public File[] fluTiffs;

  private File convexFile;
  private File coordFile;
  private File motilityFile;
  private File originFile;
  private File xmapFile;
  private File ymapFile;
  private File[] fluFiles;

  private double imageScale;
  private double frameInterval;
  private int startFrame;
  private int endFrame;

  private int blowup;
  private double nodeRes;

  int nmax;
  int maxIterations;
  int sampleTan;
  int sampleNorm;

  double deltaT;
  double velCrit;
  double centralForce;
  double contractForce;
  double imageForce;
  double frictionForce;

  /**
   * Shrink value.
   */
  public double finalShrink;
  /**
   * The cortex width.
   */
  public double cortexWidth;

  /**
   * The key.
   */
  long key;

  /**
   * The sensitivity.
   */
  double sensitivity; // no longer used. blank holder
  /**
   * Indicate if <i>snQP</i> has been processed by ECMM (<tt>true</tt>). Set by checkECMMrun.
   */
  private boolean ecmmHasRun = false;

  /**
   * Check if ECMM was run.
   * 
   * @return the ecmmHasRun
   */
  public boolean isEcmmHasRun() {
    return ecmmHasRun;
  }

  /**
   * Instantiates a new q params.
   */
  public QParams() {

  }

  /**
   * Read basic information from <i>paQP</i> file such as its name and path. Initialise structures
   * 
   * @param p <i>paQP</i> file, should contain underscored cell number and absolute path.
   */
  public QParams(File p) {
    setParamFile(p);

    paramFormat = QParams.QUIMP_11;

    segImageFile = new File("/");
    snakeQP = new File("/");
    statsQP = new File("/");

    fluTiffs = new File[3];
    fluTiffs[0] = new File("/");
    fluTiffs[1] = new File("/");
    fluTiffs[2] = new File("/");

    imageScale = -1;
    frameInterval = -1;
    startFrame = -1;
    endFrame = -1;
    nmax = -1;
    blowup = -1;
    maxIterations = -1;
    sampleTan = -1;
    sampleNorm = -1;
    deltaT = -1;
    nodeRes = -1;
    velCrit = -1;
    centralForce = -1;
    contractForce = -1;
    imageForce = -1;
    frictionForce = -1;
    finalShrink = -1;
    cortexWidth = 0.7;
    key = -1;
    sensitivity = -1;
  }

  /**
   * Get name of parameter file (paQP).
   * 
   * @return the paramFile
   */
  public File getParamFile() {
    return paramFile;
  }

  /**
   * Set name of parameter file.
   * 
   * @param paramFile the paramFile to set. Extension will be removed.
   */
  public void setParamFile(File paramFile) {
    fileName = QuimpToolsCollection.removeExtension(paramFile.getName());
    this.paramFile = paramFile;
    path = paramFile.getParent();
    // path is used for producing other file names in directory where paramFile. This is in case
    // if paramFile does not contain path (should not happen)
    if (path == null) {
      throw new IllegalArgumentException("Input file: " + paramFile
              + " must contain path as other files will be created in its folder");
    }
  }

  /**
   * Get the name of parameter file but without extension.
   * 
   * @return the prefix. This name probably contains also underscored cell number. It is added
   *         when object of this is created from {@link QParamsQconf} and should be added when
   *         creating only this object QParams(File).
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * getPath.
   * 
   * @return the path of param file
   */
  public String getPath() {
    return path;
  }

  /**
   * getPathasPath.
   * 
   * @return the path of param file as Path
   */
  public Path getPathasPath() {
    return Paths.get(this.getPath());
  }

  /**
   * getNodeRes.
   * 
   * @return the nodeRes
   */
  public double getNodeRes() {
    return nodeRes;
  }

  /**
   * setNodeRes.
   * 
   * @param nodeRes the nodeRes to set
   */
  public void setNodeRes(double nodeRes) {
    this.nodeRes = nodeRes;
  }

  /**
   * getBlowup.
   * 
   * @return the blowup
   */
  public int getBlowup() {
    return blowup;
  }

  /**
   * setBlowup.
   * 
   * @param blowup the blowup to set
   */
  public void setBlowup(int blowup) {
    this.blowup = blowup;
  }

  /**
   * getImageScale.
   * 
   * @return the imageScale
   */
  public double getImageScale() {
    return imageScale;
  }

  /**
   * setImageScale.
   * 
   * @param imageScale the imageScale to set
   */
  public void setImageScale(double imageScale) {
    this.imageScale = imageScale;
  }

  /**
   * getFrameInterval.
   * 
   * @return the frameInterval
   */
  public double getFrameInterval() {
    return frameInterval;
  }

  /**
   * setFrameInterval.
   * 
   * @param frameInterval the frameInterval to set
   */
  public void setFrameInterval(double frameInterval) {
    this.frameInterval = frameInterval;
  }

  /**
   * getStartFrame.
   * 
   * @return the startFrame
   */
  public int getStartFrame() {
    return startFrame;
  }

  /**
   * setStartFrame.
   * 
   * @param startFrame the startFrame to set
   */
  public void setStartFrame(int startFrame) {
    this.startFrame = startFrame;
  }

  /**
   * getEndFrame.
   * 
   * @return the endFrame
   */
  public int getEndFrame() {
    return endFrame;
  }

  /**
   * setEndFrame.
   * 
   * @param endFrame the endFrame to set
   */
  public void setEndFrame(int endFrame) {
    this.endFrame = endFrame;
  }

  /**
   * Get snQP file name.
   * 
   * @return the snakeQP
   */
  public File getSnakeQP() {
    return snakeQP;
  }

  /**
   * Set snQP file name.
   * 
   * @param snakeQP the snakeQP to set
   */
  public void setSnakeQP(File snakeQP) {
    this.snakeQP = snakeQP;
  }

  /**
   * Get stats file name.
   * 
   * @return the statsQP
   */
  public File getStatsQP() {
    return statsQP;
  }

  /**
   * Get names of flu files.
   * 
   * @return the fluFiles
   */
  public File[] getFluFiles() {
    return fluFiles;
  }

  /**
   * Set stats file name.
   * 
   * @param statsQP the statsQP to set
   */
  public void setStatsQP(File statsQP) {
    this.statsQP = statsQP;
  }

  /**
   * Get name of BOA loaded image.
   * 
   * @return the segImageFile
   */
  public File getSegImageFile() {
    return segImageFile;
  }

  /**
   * Set name of BOA loaded image.
   * 
   * @param segImageFile the segImageFile to set
   */
  public void setSegImageFile(File segImageFile) {
    this.segImageFile = segImageFile;
  }

  /**
   * getConvexFile name.
   * 
   * @return the convexFile
   */
  public File getConvexFile() {
    return convexFile;
  }

  /**
   * setConvexFile name.
   * 
   * @param convexFile the convexFile to set
   */
  public void setConvexFile(File convexFile) {
    this.convexFile = convexFile;
  }

  /**
   * getCoordFile name.
   * 
   * @return the coordFile
   */
  public File getCoordFile() {
    return coordFile;
  }

  /**
   * setCoordFile name.
   * 
   * @param coordFile the coordFile to set
   */
  public void setCoordFile(File coordFile) {
    this.coordFile = coordFile;
  }

  /**
   * getMotilityFile name.
   * 
   * @return the motilityFile
   */
  public File getMotilityFile() {
    return motilityFile;
  }

  /**
   * setMotilityFile name.
   * 
   * @param motilityFile the motilityFile to set
   */
  public void setMotilityFile(File motilityFile) {
    this.motilityFile = motilityFile;
  }

  /**
   * getOriginFile name.
   * 
   * @return the originFile
   */
  public File getOriginFile() {
    return originFile;
  }

  /**
   * setOriginFile name.
   * 
   * @param originFile the originFile to set
   */
  public void setOriginFile(File originFile) {
    this.originFile = originFile;
  }

  /**
   * Get x coord map file.
   * 
   * @return the xmapFile
   */
  public File getxmapFile() {
    return xmapFile;
  }

  /**
   * Set x coord map file.
   * 
   * @param xmapFile the xmapFile to set
   */
  public void setxmapFile(File xmapFile) {
    this.xmapFile = xmapFile;
  }

  /**
   * Get y coord map file.
   * 
   * @return the ymapFile
   */
  public File getyFile() {
    return ymapFile;
  }

  /**
   * Set y coord map file.
   * 
   * @param ymapFile the ymapFile to set
   */
  public void setymapFile(File ymapFile) {
    this.ymapFile = ymapFile;
  }

  /**
   * Read the paQP file specified by paramFile
   * 
   * <p>See {@link #QParams(File)}. Create handles to files stored as names in <i>paQP</i>. Read
   * segmentation parameters.
   * 
   * @throws QuimpException on wrong file that can not be interpreted or read.
   * @throws IllegalStateException if input file is empty
   */
  public void readParams() throws QuimpException {
    paramFormat = QParams.OLD_QUIMP;
    BufferedReader d = null;
    try {
      d = new BufferedReader(new FileReader(paramFile));

      String l = d.readLine();
      if (l == null) {
        throw new IllegalStateException(
                "File " + paramFile.getAbsolutePath() + " seems to be empty");
      }
      if (!(l.length() < 2)) {
        String fileID = l.substring(0, 2);
        if (!fileID.equals("#p")) {
          IJ.error("Not compatible paramater file");
          throw new QuimpException("Not compatible paramater file");
        }
      } else {
        IJ.error("Not compatible paramater file");
        throw new QuimpException("Not compatible paramater file");
      }
      key = (long) QuimpToolsCollection.s2d(d.readLine()); // key
      segImageFile = new File(d.readLine()); // image file name

      String sn = d.readLine();
      // fileName = sn;
      if (!l.substring(0, 3).equals("#p2")) { // old format, fix file names
        sn = sn.substring(1); // strip the dot off snQP file name
        // fileName = fileName.substring(1); // strip the dot off file
        // name
        int lastDot = sn.lastIndexOf(".");

        String tempS = sn.substring(0, lastDot);
        statsQP = new File(paramFile.getParent() + tempS + FileExtensions.statsFileExt);
      }
      snakeQP = new File(paramFile.getParent() + "" + sn); // snQP file
      LOGGER.debug("snake file: " + snakeQP.getAbsolutePath());

      d.readLine(); // # blank line
      imageScale = QuimpToolsCollection.s2d(d.readLine());
      if (imageScale == 0) {
        IJ.log("Warning. Image scale was zero. Set to 1");
        imageScale = 1;
      }
      frameInterval = QuimpToolsCollection.s2d(d.readLine());

      d.readLine(); // skip #segmentation parameters
      nmax = (int) QuimpToolsCollection.s2d(d.readLine());
      deltaT = QuimpToolsCollection.s2d(d.readLine());
      maxIterations = (int) QuimpToolsCollection.s2d(d.readLine());
      nodeRes = QuimpToolsCollection.s2d(d.readLine());
      blowup = (int) QuimpToolsCollection.s2d(d.readLine());
      sampleTan = (int) QuimpToolsCollection.s2d(d.readLine());
      sampleNorm = (int) QuimpToolsCollection.s2d(d.readLine());
      velCrit = QuimpToolsCollection.s2d(d.readLine());

      centralForce = QuimpToolsCollection.s2d(d.readLine());
      contractForce = QuimpToolsCollection.s2d(d.readLine());
      frictionForce = QuimpToolsCollection.s2d(d.readLine());
      imageForce = QuimpToolsCollection.s2d(d.readLine());
      sensitivity = QuimpToolsCollection.s2d(d.readLine());

      if (l.substring(0, 3).equals("#p2")) { // new format
        paramFormat = QParams.QUIMP_11;
        // new params
        // # - new parameters (cortext width, start frame, end frame, final shrink, statsQP,
        // fluImage)
        d.readLine();
        cortexWidth = QuimpToolsCollection.s2d(d.readLine());
        startFrame = (int) QuimpToolsCollection.s2d(d.readLine());
        endFrame = (int) QuimpToolsCollection.s2d(d.readLine());
        finalShrink = QuimpToolsCollection.s2d(d.readLine());
        statsQP = new File(paramFile.getParent() + "" + d.readLine());

        d.readLine(); // # fluo channel tiffs
        fluTiffs[0] = new File(d.readLine());
        fluTiffs[1] = new File(d.readLine());
        fluTiffs[2] = new File(d.readLine());
      }
      this.guessOtherFileNames(); // generate handles of other files that will be created here
      checkECMMrun(); // check if snQP file is already processed by ECMM. Set ecmmHasRun
    } catch (IOException e) {
      throw new QuimpException(e);
    } finally {
      try {
        if (d != null) {
          d.close();
        }
      } catch (IOException e) {
        LOGGER.error("Can not close file " + e);
      }
    }
  }

  /**
   * Write params.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void writeParams() throws IOException {
    LOGGER.debug("Write " + FileExtensions.configFileExt + " at: " + paramFile);
    if (paramFile.exists()) {
      paramFile.delete();
    }

    Random generator = new Random();
    double d = generator.nextDouble() * 1000000; // 6 digit key to ID job
    key = Math.round(d);

    PrintWriter ppW = new PrintWriter(new FileWriter(paramFile), true);

    ppW.print("#p2 - QuimP parameter file (QuimP11). Created " + QuimpToolsCollection.dateAsString()
            + "\n");
    ppW.print(IJ.d2s(key, 0) + "\n");
    ppW.print(segImageFile.getAbsolutePath() + "\n");
    ppW.print(File.separator + snakeQP.getName() + "\n");
    // pPW.print(outFile.getAbsolutePath() + "\n");

    ppW.print("#Image calibration (scale, frame interval)\n");
    ppW.print(IJ.d2s(imageScale, 6) + "\n");
    ppW.print(IJ.d2s(frameInterval, 3) + "\n");

    // according to BOAState and /trac/QuimP/wiki/QuimpQp
    ppW.print("#segmentation parameters (" + "Maximum number of nodes, " + "ND, "
            + "Max iterations, " + "Node spacing, " + "Blowup, " + "Sample tan, " + "Sample norm, "
            + "Crit velocity, " + "Central F, " + "Contract F, " + "ND, " + "Image force, "
            + "ND)\n");
    ppW.print(IJ.d2s(nmax, 0) + "\n");
    ppW.print(IJ.d2s(deltaT, 6) + "\n");
    ppW.print(IJ.d2s(maxIterations, 6) + "\n");
    ppW.print(IJ.d2s(nodeRes, 6) + "\n");
    ppW.print(IJ.d2s(blowup, 6) + "\n");
    ppW.print(IJ.d2s(sampleTan, 0) + "\n");
    ppW.print(IJ.d2s(sampleNorm, 0) + "\n");
    ppW.print(IJ.d2s(velCrit, 6) + "\n");
    ppW.print(IJ.d2s(centralForce, 6) + "\n");
    ppW.print(IJ.d2s(contractForce, 6) + "\n");
    ppW.print(IJ.d2s(frictionForce, 6) + "\n");
    ppW.print(IJ.d2s(imageForce, 6) + "\n");
    ppW.print(IJ.d2s(sensitivity, 6) + "\n");

    ppW.print("# - new parameters (cortex width, start frame, end frame,"
            + " final shrink, statsQP, fluImage)\n");
    ppW.print(IJ.d2s(cortexWidth, 2) + "\n");
    ppW.print(IJ.d2s(startFrame, 0) + "\n");
    ppW.print(IJ.d2s(endFrame, 0) + "\n");
    ppW.print(IJ.d2s(finalShrink, 2) + "\n");
    ppW.print(File.separator + statsQP.getName() + "\n");

    ppW.print("# - Fluorescence channel tiff's\n");
    ppW.print(fluTiffs[0].getAbsolutePath() + "\n");
    ppW.print(fluTiffs[1].getAbsolutePath() + "\n");
    ppW.print(fluTiffs[2].getAbsolutePath() + "\n");

    ppW.print("#END");

    ppW.close();
  }

  /**
   * Traverse through current directory looking for <i>paQP</i> files.
   * 
   * <p><b>Remarks</b>
   * 
   * <p>Current <i>paQP</i> file (that passed to QParams(File)) is not counted.
   * 
   * @return Array of file handlers or empty array if there is no <i>paQP</i> files (except
   *         paramFile)
   */
  public File[] findParamFiles() {
    File[] otherPaFiles;
    File directory = new File(path);
    ArrayList<String> paFiles = new ArrayList<String>();

    if (directory.isDirectory()) {
      String[] filenames = directory.list();
      if (filenames == null) {
        otherPaFiles = new File[0];
        return otherPaFiles;
      }
      String extension;

      for (int i = 0; i < filenames.length; i++) {
        if (filenames[i].matches("\\.") || filenames[i].matches("\\.\\.")
                || filenames[i].matches(paramFile.getName())) {
          continue;
        }
        extension = QuimpToolsCollection.getFileExtension(filenames[i]);
        if (extension.matches(FileExtensions.configFileExt.substring(1))) {
          paFiles.add(filenames[i]);
          LOGGER.info("paFile: " + filenames[i]);
        }
      }
    }
    if (paFiles.isEmpty()) {
      otherPaFiles = new File[0];
      return otherPaFiles;
    } else {
      otherPaFiles = new File[paFiles.size()];
      for (int j = 0; j < otherPaFiles.length; j++) {
        otherPaFiles[j] =
                new File(directory.getAbsolutePath() + File.separator + (String) paFiles.get(j));
      }
      return otherPaFiles;
    }
  }

  /**
   * Generate names and handles of files associated with paQP that will be created in result of
   * analysis.
   */
  protected void guessOtherFileNames() {
    LOGGER.debug("prefix: " + fileName);

    convexFile = new File(path + File.separator + fileName + FileExtensions.convmapFileExt);

    coordFile = new File(path + File.separator + fileName + FileExtensions.coordmapFileExt);
    motilityFile = new File(path + File.separator + fileName + FileExtensions.motmapFileExt);
    originFile = new File(path + File.separator + fileName + FileExtensions.originmapFileExt);
    xmapFile = new File(path + File.separator + fileName + FileExtensions.xmapFileExt);
    ymapFile = new File(path + File.separator + fileName + FileExtensions.ymapFileExt);

    fluFiles = new File[3];
    fluFiles[0] = new File(
            path + File.separator + fileName + FileExtensions.fluomapFileExt.replace('%', '1'));
    fluFiles[1] = new File(
            path + File.separator + fileName + FileExtensions.fluomapFileExt.replace('%', '2'));
    fluFiles[2] = new File(
            path + File.separator + fileName + FileExtensions.fluomapFileExt.replace('%', '3'));

  }

  /**
   * Verify if <i>snQP</i> file has been already processed by ECMM.
   * 
   * <p>Processed files have <b>-ECMM</b> suffix on first line.
   * 
   * @throws IOException on file reading problem
   * @throws IllegalStateException if file is empty
   */
  private void checkECMMrun() throws IOException {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(snakeQP));
      String line = br.readLine(); // read first line
      if (line == null) {
        throw new IllegalStateException("File " + snakeQP.getAbsolutePath() + " seems to be empty");
      }
      String sub = line.substring(line.length() - 4, line.length());
      if (sub.matches("ECMM")) {
        LOGGER.info("ECMM has been run on this paFile data");
        ecmmHasRun = true;
      } else {
        ecmmHasRun = false;
      }
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        LOGGER.error("Can not close file " + e);
      }
    }
  }

}
