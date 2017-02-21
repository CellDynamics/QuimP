/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package uk.ac.warwick.wsbc.quimp;

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

import ij.IJ;
import uk.ac.warwick.wsbc.quimp.BOAState.BOAp;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
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
 * <p>
 * This class exists for compatibility purposes. Allows reading old files. There is also child class
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
     * The Constant OLD_QUIMP.
     */
    public static final int OLD_QUIMP = 1;

    /**
     * The Constant QUIMP_11.
     */
    public static final int QUIMP_11 = 2;

    /**
     * The Constant NEW_QUIMP.
     */
    public static final int NEW_QUIMP = 3;
    /**
     * Name of the case. Used to set <i>fileName</i> and <i>path</i>
     */
    private File paramFile;
    private File[] otherPaFiles;
    /**
     * Indicates format of data file.
     */
    public int paramFormat;
    /**
     * Name of the data file - without path and extension. Equals to name of the case. If
     * initialised from {@link QParamsQconf} contains underscored cell number as well.
     * 
     * @see uk.ac.warwick.wsbc.quimp.BOAState.BOAp
     */
    private String fileName;
    /**
     * Path where user files exist.
     * 
     * @see uk.ac.warwick.wsbc.quimp.BOAState.BOAp
     */
    private String path;
    private File segImageFile, snakeQP;

    /**
     * The stats QP.
     */
    protected File statsQP;
    /**
     * This field is set by direct call from ANA. Left here for compatibility reasons. Main holder
     * of fluTiffs is {@link uk.ac.warwick.wsbc.quimp.plugin.ana.ANAp}
     */
    public File[] fluTiffs;

    private File convexFile, coordFile, motilityFile, originFile, xFile, yFile;
    private File[] fluFiles;

    private double imageScale;
    private double frameInterval;
    private int startFrame, endFrame;

    private int blowup;
    private double nodeRes;

    /**
     * The sample norm.
     */
    int NMAX, max_iterations, sample_tan, sample_norm;

    /**
     * The f friction.
     */
    double delta_t, vel_crit, f_central, f_contract, f_image, f_friction;

    /**
     * Shrink value
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
     * @param p <i>paQP</i> file, should contain underscored cell number.
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
        NMAX = -1;
        blowup = -1;
        max_iterations = -1;
        sample_tan = -1;
        sample_norm = -1;
        delta_t = -1;
        nodeRes = -1;
        vel_crit = -1;
        f_central = -1;
        f_contract = -1;
        f_image = -1;
        f_friction = -1;
        finalShrink = -1;
        cortexWidth = 0.7;
        key = -1;
        sensitivity = -1;
    }

    /**
     * @return the paramFile
     */
    public File getParamFile() {
        return paramFile;
    }

    /**
     * @param paramFile the paramFile to set.
     */
    public void setParamFile(File paramFile) {
        fileName = QuimpToolsCollection.removeExtension(paramFile.getName());
        this.paramFile = paramFile;
        path = paramFile.getParent();
    }

    /**
     * @return the prefix. This name probably contains also underscored cell number. It is added
     *         when object of this is created from {@link QParamsQconf} and should be added when
     *         creating only this object QParams(File).
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the path of param file
     */
    public String getPath() {
        return path;
    }

    /**
     * 
     * @return the path of param file as Path
     */
    public Path getPathasPath() {
        return Paths.get(this.getPath());
    }

    /**
     * @return the nodeRes
     */
    public double getNodeRes() {
        return nodeRes;
    }

    /**
     * @param nodeRes the nodeRes to set
     */
    public void setNodeRes(double nodeRes) {
        this.nodeRes = nodeRes;
    }

    /**
     * @return the blowup
     */
    public int getBlowup() {
        return blowup;
    }

    /**
     * @param blowup the blowup to set
     */
    public void setBlowup(int blowup) {
        this.blowup = blowup;
    }

    /**
     * @return the imageScale
     */
    public double getImageScale() {
        return imageScale;
    }

    /**
     * @param imageScale the imageScale to set
     */
    public void setImageScale(double imageScale) {
        this.imageScale = imageScale;
    }

    /**
     * @return the frameInterval
     */
    public double getFrameInterval() {
        return frameInterval;
    }

    /**
     * @param frameInterval the frameInterval to set
     */
    public void setFrameInterval(double frameInterval) {
        this.frameInterval = frameInterval;
    }

    /**
     * @return the startFrame
     */
    public int getStartFrame() {
        return startFrame;
    }

    /**
     * @param startFrame the startFrame to set
     */
    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    /**
     * @return the endFrame
     */
    public int getEndFrame() {
        return endFrame;
    }

    /**
     * @param endFrame the endFrame to set
     */
    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    /**
     * @return the snakeQP
     */
    public File getSnakeQP() {
        return snakeQP;
    }

    /**
     * @param snakeQP the snakeQP to set
     */
    public void setSnakeQP(File snakeQP) {
        this.snakeQP = snakeQP;
    }

    /**
     * @return the statsQP
     */
    public File getStatsQP() {
        return statsQP;
    }

    /**
     * @return the fluFiles
     */
    public File[] getFluFiles() {
        return fluFiles;
    }

    /**
     * @param statsQP the statsQP to set
     */
    public void setStatsQP(File statsQP) {
        this.statsQP = statsQP;
    }

    /**
     * @return the segImageFile
     */
    public File getSegImageFile() {
        return segImageFile;
    }

    /**
     * @param segImageFile the segImageFile to set
     */
    public void setSegImageFile(File segImageFile) {
        this.segImageFile = segImageFile;
    }

    /**
     * @return the convexFile
     */
    public File getConvexFile() {
        return convexFile;
    }

    /**
     * @param convexFile the convexFile to set
     */
    public void setConvexFile(File convexFile) {
        this.convexFile = convexFile;
    }

    /**
     * @return the coordFile
     */
    public File getCoordFile() {
        return coordFile;
    }

    /**
     * @param coordFile the coordFile to set
     */
    public void setCoordFile(File coordFile) {
        this.coordFile = coordFile;
    }

    /**
     * @return the motilityFile
     */
    public File getMotilityFile() {
        return motilityFile;
    }

    /**
     * @param motilityFile the motilityFile to set
     */
    public void setMotilityFile(File motilityFile) {
        this.motilityFile = motilityFile;
    }

    /**
     * @return the originFile
     */
    public File getOriginFile() {
        return originFile;
    }

    /**
     * @param originFile the originFile to set
     */
    public void setOriginFile(File originFile) {
        this.originFile = originFile;
    }

    /**
     * @return the xFile
     */
    public File getxFile() {
        return xFile;
    }

    /**
     * @param xFile the xFile to set
     */
    public void setxFile(File xFile) {
        this.xFile = xFile;
    }

    /**
     * @return the yFile
     */
    public File getyFile() {
        return yFile;
    }

    /**
     * @param yFile the yFile to set
     */
    public void setyFile(File yFile) {
        this.yFile = yFile;
    }

    /**
     * Read the \a paQP file specified by paramFile (see
     * uk.ac.warwick.wsbc.quimp.QParams.QParams(File))
     * 
     * Create handles to files stored as names in <i>paQP</i>. Read segmentation parameters.
     * 
     * @throws QuimpException
     */
    public void readParams() throws QuimpException {
        paramFormat = QParams.OLD_QUIMP;
        try {
            BufferedReader d = new BufferedReader(new FileReader(paramFile));

            String l = d.readLine();
            if (!(l.length() < 2)) {
                String fileID = l.substring(0, 2);
                if (!fileID.equals("#p")) {
                    IJ.error("Not compatible paramater file");
                    d.close();
                    throw new QuimpException("QParams::Not compatible paramater file");
                }
            } else {
                IJ.error("Not compatible paramater file");
                d.close();
                throw new QuimpException("QParams::Not compatible paramater file");
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
            NMAX = (int) QuimpToolsCollection.s2d(d.readLine());
            delta_t = QuimpToolsCollection.s2d(d.readLine());
            max_iterations = (int) QuimpToolsCollection.s2d(d.readLine());
            nodeRes = (int) QuimpToolsCollection.s2d(d.readLine());
            blowup = (int) QuimpToolsCollection.s2d(d.readLine());
            sample_tan = (int) QuimpToolsCollection.s2d(d.readLine());
            sample_norm = (int) QuimpToolsCollection.s2d(d.readLine());
            vel_crit = QuimpToolsCollection.s2d(d.readLine());

            f_central = QuimpToolsCollection.s2d(d.readLine());
            f_contract = QuimpToolsCollection.s2d(d.readLine());
            f_friction = QuimpToolsCollection.s2d(d.readLine());
            f_image = QuimpToolsCollection.s2d(d.readLine());
            sensitivity = QuimpToolsCollection.s2d(d.readLine());

            if (l.substring(0, 3).equals("#p2")) { // new format
                paramFormat = QParams.QUIMP_11;
                // new params
                d.readLine(); // # - new parameters (cortext width, start frame,
                              // end frame, final shrink, statsQP, fluImage)
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
            d.close();
            this.guessOtherFileNames(); // generate handles of other files that will be created here
            checkECMMrun(); // check if snQP file is already processed by ECMM. Set ecmmHasRun
        } catch (Exception e) {
            throw new QuimpException(e);
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
        double d = generator.nextDouble() * 1000000; // 6 digit key to ID
                                                     // job
        key = Math.round(d);

        PrintWriter pPW = new PrintWriter(new FileWriter(paramFile), true);

        pPW.print("#p2 - QuimP parameter file (QuimP11). Created "
                + QuimpToolsCollection.dateAsString() + "\n");
        pPW.print(IJ.d2s(key, 0) + "\n");
        pPW.print(segImageFile.getAbsolutePath() + "\n");
        pPW.print(File.separator + snakeQP.getName() + "\n");
        // pPW.print(outFile.getAbsolutePath() + "\n");

        pPW.print("#Image calibration (scale, frame interval)\n");
        pPW.print(IJ.d2s(imageScale, 6) + "\n");
        pPW.print(IJ.d2s(frameInterval, 3) + "\n");

        // according to BOAState and /trac/QuimP/wiki/QuimpQp
        pPW.print("#segmentation parameters (" + "Maximum number of nodes, " + "ND, "
                + "Max iterations, " + "Node spacing, " + "Blowup, " + "Sample tan, "
                + "Sample norm, " + "Crit velocity, " + "Central F, " + "Contract F, " + "ND, "
                + "Image force, " + "ND)\n");
        pPW.print(IJ.d2s(NMAX, 0) + "\n");
        pPW.print(IJ.d2s(delta_t, 6) + "\n");
        pPW.print(IJ.d2s(max_iterations, 6) + "\n");
        pPW.print(IJ.d2s(nodeRes, 6) + "\n");
        pPW.print(IJ.d2s(blowup, 6) + "\n");
        pPW.print(IJ.d2s(sample_tan, 0) + "\n");
        pPW.print(IJ.d2s(sample_norm, 0) + "\n");
        pPW.print(IJ.d2s(vel_crit, 6) + "\n");
        pPW.print(IJ.d2s(f_central, 6) + "\n");
        pPW.print(IJ.d2s(f_contract, 6) + "\n");
        pPW.print(IJ.d2s(f_friction, 6) + "\n");
        pPW.print(IJ.d2s(f_image, 6) + "\n");
        pPW.print(IJ.d2s(sensitivity, 6) + "\n");

        pPW.print("# - new parameters (cortex width, start frame, end frame,"
                + " final shrink, statsQP, fluImage)\n");
        pPW.print(IJ.d2s(cortexWidth, 2) + "\n");
        pPW.print(IJ.d2s(startFrame, 0) + "\n");
        pPW.print(IJ.d2s(endFrame, 0) + "\n");
        pPW.print(IJ.d2s(finalShrink, 2) + "\n");
        pPW.print(File.separator + statsQP.getName() + "\n");

        pPW.print("# - Fluorescence channel tiff's\n");
        pPW.print(fluTiffs[0].getAbsolutePath() + "\n");
        pPW.print(fluTiffs[1].getAbsolutePath() + "\n");
        pPW.print(fluTiffs[2].getAbsolutePath() + "\n");

        pPW.print("#END");

        pPW.close();
    }

    /**
     * Traverse through current directory and sub-directories looking for <i>paQP</i> files.
     * 
     * <p>
     * <b>Remarks</b>
     * <p>
     * Current <i>paQP</i> file (that passed to QParams(File)) is not counted.
     * 
     * @return Array of file handlers or empty array if there is no <i>paQP</i> files (except
     *         paramFile)
     */
    public File[] findParamFiles() {
        File directory = new File(paramFile.getParent());
        ArrayList<String> paFiles = new ArrayList<String>();

        if (directory.isDirectory()) {
            String[] filenames = directory.list();
            String extension;

            for (int i = 0; i < filenames.length; i++) {
                if (filenames[i].matches(".") || filenames[i].matches("..")
                        || filenames[i].matches(paramFile.getName())) {
                    continue;
                }
                extension = QuimpToolsCollection.getFileExtension(filenames[i]);
                if (extension.matches(FileExtensions.configFileExt.substring(1))) {
                    paFiles.add(filenames[i]);
                    System.out.println("paFile: " + filenames[i]);
                }
            }
        }
        if (paFiles.isEmpty()) {
            otherPaFiles = new File[0];
            return otherPaFiles;
        } else {
            otherPaFiles = new File[paFiles.size()];
            for (int j = 0; j < otherPaFiles.length; j++) {
                otherPaFiles[j] = new File(
                        directory.getAbsolutePath() + File.separator + (String) paFiles.get(j));
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
        xFile = new File(path + File.separator + fileName + FileExtensions.xmapFileExt);
        yFile = new File(path + File.separator + fileName + FileExtensions.ymapFileExt);

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
     * Processed files have <b>-ECMM</b> suffix on first line.
     * 
     * @throws Exception
     */
    private void checkECMMrun() throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(snakeQP));
            String line = br.readLine(); // read first line

            String sub = line.substring(line.length() - 4, line.length());
            if (sub.matches("ECMM")) {
                LOGGER.info("ECMM has been run on this paFile data");
                ecmmHasRun = true;
            } else {
                ecmmHasRun = false;
            }
        } catch (Exception e) {
            LOGGER.debug("Can not find " + snakeQP.toString());
            throw e;
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
