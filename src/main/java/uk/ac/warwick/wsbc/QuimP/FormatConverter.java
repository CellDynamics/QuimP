package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.QuimpException.MessageSinkTypes;
import uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer;
import uk.ac.warwick.wsbc.QuimP.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.QuimP.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.FluoMap;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * This class allows for recreating paQP and snQP files from new format QCONF and vice versa.
 * <p>
 * <b>Note</b>
 * <p>
 * Other files such as stQP, maps and images are generated regardless file format used during saving
 * in each QuimP module.
 * 
 * By default it is assumed that provided file is QCONF. This file is stored in class and used by
 * some of private methods for creating paQP and snQP files. For converting from old files, the file
 * provided to constructor is read but not used. It is re-read in loop by
 * {@link #generateNewDataFile()}.
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
        try {
            qcL = new QconfLoader(fileToConvert);
            path = Paths.get(fileToConvert.getParent());
            filename = Paths.get(qcL.getQp().getFileName()); // can contain xx_0 if old file loaded
        } catch (QuimpException e) { // rethrow with showing in UI
            e.setMessageSinkType(MessageSinkTypes.GUI);
            throw e;
        }
    }

    /**
     * Construct conversion object from QParamsQconf.
     * 
     * @param qP reference to QParamsQconf
     */
    public FormatConverter(QconfLoader qcL) {
        LOGGER.debug("Use provided QconfLoader");
        this.qcL = qcL;
        this.path = qcL.getQp().getPathasPath();
        this.filename = Paths.get(qcL.getQp().getFileName()); // can contain xx_0 if old file loaded
    }

    /**
     * Build QCONF from old datafile provided in constructor.
     * 
     * It must be _0 file.
     * 
     * @throws QuimpException on wrong inputs
     */
    public void generateNewDataFile() throws QuimpException {
        LOGGER.warn("\n----------------------------------------------------------\n"
                + "Functionalities not implemented yet:\n"
                + "    1. Exporting all maps, especially fluoromaps\n"
                + "----------------------------------------------------------\n");
        if (qcL.getConfVersion() == QParams.NEW_QUIMP)
            throw new IllegalArgumentException("Can not convert from new format to new");
        // create storage
        DataContainer dT = new DataContainer();
        dT.BOAState = new BOAState(qcL.getImage());
        dT.ECMMState = new OutlinesCollection();
        dT.BOAState.nest = new Nest();
        // dT.ANAState = new ANAStates();
        ArrayList<STmap> maps = new ArrayList<>(); // temporary - we do not know number of cells

        // extract paQP number from file name (loaded in constructor)
        int last = filename.toString().lastIndexOf('_'); // position of _ before number
        if (last < 0)
            throw new QuimpException("Input file must be in format name_XX.paQP",
                    MessageSinkTypes.GUI);
        // number must be 0
        try {
            int numofpaqp; // number extracted from paQP name
            numofpaqp = Integer.parseInt(
                    filename.toString().substring(last + 1, filename.toString().length()));
            if (numofpaqp != 0)
                throw new QuimpException(
                        "paQP file number is not 0. Check if you selected first file 0_.paQP",
                        MessageSinkTypes.GUI);
        } catch (NumberFormatException e) {
            throw new QuimpException(
                    "paQP file number can not be found. Check if file name is in format name_XX.paQP",
                    MessageSinkTypes.GUI);
        }
        // cut last number from file name name
        String orginal = filename.toString().substring(0, last);

        int i; // PaQP files counter
        // run conversion for all paQP files. conversion always starts from 0
        i = 0; // try to read paPQ starting from 0, before we tested if user pointed correct file
        File filetoload = new File(""); // store name_XX.paQP file in loop below
        OutlineHandler oH;
        STmap stMap;
        try {
            do {
                // paQP files with _xx number in name
                filetoload = Paths.get(qcL.getQp().getPath(),
                        orginal + "_" + i + FileExtensions.configFileExt).toFile();
                // optimisation - first file is already loaded, skip it
                if (i != 0) // it is checked whethet it is first file
                    qcL = new QconfLoader(filetoload); // re-load it
                // assume that BOA params are taken from file 0_.paQP
                if (i == 0)
                    dT.BOAState.loadParams(qcL.getQp()); // load parameters only once
                // initialize snakes (from snQP files)
                oH = new OutlineHandler(qcL.getQp()); // restore OutlineHandler
                dT.ECMMState.oHs.add(oH); // store in ECMM object
                BOA_.qState = dT.BOAState; // for compatibility - create static
                dT.BOAState.nest.addOutlinehandler(oH); // covert ECMM to Snake and store in BOA
                                                        // section
                // load maps and store in QCONF
                stMap = new STmap();
                try {
                    stMap.motMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getMotilityFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.convMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getConvexFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.coordMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getCoordFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.originMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getOriginFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.xMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getxFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.yMap = QuimPArrayUtils.file2Array(",", qcL.getQp().getyFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                // Fluoromap
                // try to read 3 channels for current paQP
                int channel = 1; // channel counter for fluoromaps
                for (File ff : qcL.getQp().getFluFiles()) {
                    FluoMap chm = new FluoMap(stMap.motMap.length, stMap.motMap[0].length, channel);
                    if (ff.exists()) {// read this file
                        chm.setMap(QuimPArrayUtils.file2Array(",", ff));
                        chm.setEnabled(true); // loaded and valid
                    } else
                        chm.setEnabled(false); // by default FluMaps are enabled (compatibility
                                               // reason)
                    stMap.fluoMaps[channel - 1] = chm;
                    channel++;
                }
                maps.add(stMap);
                i++; // go to next paQP
            } while (true); // exception thrown by QconfLoader will stop this loop, e.g. trying to
                            // load nonexiting file
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.warn("File " + filetoload.toString() + " can not be processed. Reason: "
                    + e.getMessage());
        }
        // save DataContainer using Serializer
        dT.QState = maps.toArray(new STmap[0]); // convert to array
        Serializer<DataContainer> n;
        n = new Serializer<>(dT, new QuimpToolsCollection().getQuimPBuildInfo());
        n.setPretty();
        try {
            n.save(path + File.separator + orginal + FileExtensions.newConfigFileExt);
        } catch (FileNotFoundException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("File " + orginal + " can not be saved. Reason: " + e.getMessage());
        }
        n = null;
    }

    /**
     * Recreate paQP and snQP files from QCONF.
     * <p>
     * Files are created in directory where QCONF is located.
     * 
     * @throws IOException
     * 
     */
    public void generateOldDataFile() throws IOException {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can not convert from old format to old");
        DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
        if (dT.getECMMState() == null)
            generatepaQP(); // no ecmm data write snakes only
        else
            generatesnQP(); // write ecmm data
    }

    /**
     * Create paQP and snQP file. Latter one contains only pure snake data.
     * <p>
     * Those files are always saved together. snQP file will contain only pure snake data. Files are
     * created in directory where QCONF is located.
     * 
     * @throws IOException
     */
    private void generatepaQP() throws IOException {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can not convert from old format to old");
        // replace location to location of QCONF
        DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
        dT.getBOAState().boap.setOutputFileCore(path + File.separator + filename.toString());
        dT.BOAState.nest.writeSnakes(); // write paQP and snQP together
    }

    /**
     * Rewrite snQP file using recent ECMM processed results.
     * <p>
     * Files are created in directory where QCONF is located.
     * 
     * @throws IOException
     * 
     */
    private void generatesnQP() throws IOException {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can not convert from old format to old");
        int activeHandler = 0;
        // replace location to location of QCONF
        DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
        dT.BOAState.boap.setOutputFileCore(path + File.separator + filename.toString());
        Iterator<OutlineHandler> oHi = dT.getECMMState().oHs.iterator();
        do {
            ((QParamsQconf) qcL.getQp()).setActiveHandler(activeHandler++);
            OutlineHandler oH = oHi.next();
            oH.writeOutlines(((QParamsQconf) qcL.getQp()).getSnakeQP(), true);
            ((QParamsQconf) qcL.getQp()).writeOldParams();
        } while (oHi.hasNext());

    }

    /**
     * Perform conversion depending on which file has been loaded.
     * 
     * @throws QuimpException on every error
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
