package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer;
import uk.ac.warwick.wsbc.QuimP.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * This class allows for recreating paQP and snQP files from new format QCONF and vice versa.
 * <p><b>Note</b><p>
 * Other files such as stQP, maps and images are generated regardless file format used during saving
 * in each QuimP module.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverter {
    private static final Logger LOGGER = LogManager.getLogger(FormatConverter.class.getName());
    private QconfLoader qcL;
    private Path path;

    /**
     * Construct FormatConverter from QCONF file.
     * 
     * @param newDataFile QCONF file
     */
    public FormatConverter(File newDataFile) {
        LOGGER.debug("Use provided file:" + newDataFile.toString());
        try {
            qcL = new QconfLoader(newDataFile);
            path = Paths.get(newDataFile.getParent());
        } catch (QuimpException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with running Analysis: " + e.getMessage());
        }
    }

    /**
     * Construct conversion object from QParamsQconf.
     *  
     * @param qP reference to QParamsQconf
     * @param path Path where converted files will be saved.
     */
    public FormatConverter(QconfLoader qcL, Path path) {
        LOGGER.debug("Use provided QconfLoader");
        this.qcL = qcL;
        this.path = path;
    }

    /**
     * Build QCONF from old datafiles provided in constructor.
     * @throws Exception 
     */
    public void generateNewDataFile() throws Exception {
        // <!
        LOGGER.warn("\n----------------------------------------------------------\n"
                + "Functionalities not implemented yet:\n"
                + "    1. Exporting all maps, especially fluoromaps\n"
                + "    2. Support of multi cell files not tested\n"
                + "----------------------------------------------------------\n");
        /**/
        if (qcL.getConfVersion() == QParams.NEW_QUIMP)
            throw new IllegalArgumentException("Can not convert from new format to new");
        DataContainer dT = new DataContainer();
        String orginal = qcL.getQp().getFileName(); // xxx_0
        // cut last number
        int last = orginal.lastIndexOf('_');
        if (last < 0)
            throw new QuimpException("Input file must be in format name_no.paQP");
        orginal = orginal.substring(0, last);
        int i = 0;
        // Create DataContainer
        dT.BOAState = new BOAState(qcL.getImage());
        dT.ECMMState = new OutlinesCollection();
        dT.BOAState.nest = new Nest();
        // dT.ANAState = new ANAStates();
        ArrayList<STmap> maps = new ArrayList<>(); // temporary - we do not know number of cells
        // temprary object - different for every _x.paQP
        QconfLoader local = new QconfLoader(Paths
                .get(qcL.getQp().getPath(), orginal + "_" + i + QuimpConfigFilefilter.oldFileExt)
                .toFile());
        // populate BOA seg parameters
        dT.BOAState.loadParams(local.getQp()); // load parameters
        // initialize snakes (from snQP files)
        OutlineHandler oH = new OutlineHandler(local.getQp()); // load them (for on cell)
        dT.ECMMState.oHs.add(oH); // store in ECMM object
        BOA_.qState = dT.BOAState; // for compatibility
        dT.BOAState.nest.addOutlinehandler(oH); // covert ECMM to Snake and store in BOA section
        STmap stMap = new STmap();
        try {
            stMap.motMap = QuimPArrayUtils.file2Array(",", local.getQp().getMotilityFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        try {
            stMap.convMap = QuimPArrayUtils.file2Array(",", local.getQp().getConvexFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        try {
            stMap.coordMap = QuimPArrayUtils.file2Array(",", local.getQp().getCoordFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        try {
            stMap.originMap = QuimPArrayUtils.file2Array(",", local.getQp().getOriginFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        try {
            stMap.xMap = QuimPArrayUtils.file2Array(",", local.getQp().getxFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        try {
            stMap.yMap = QuimPArrayUtils.file2Array(",", local.getQp().getyFile());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        // TODO Load flu data as well
        // TODO ANAstate as well
        // FluoMap ch1 = new FluoMap(stMap.motMap.length, stMap.motMap[0].length, 1);
        // ch1.map = QuimPArrayUtils.file2Array(",",local.getQp().get)
        maps.add(stMap);
        i++; // try to read 1,2,3... paPQ
        try {
            do {
                local = new QconfLoader(Paths.get(qcL.getQp().getPath(),
                        orginal + "_" + i + QuimpConfigFilefilter.oldFileExt).toFile());
                oH = new OutlineHandler(local.getQp()); // load them (for on cell)
                dT.ECMMState.oHs.add(oH); // store in ECMM object
                BOA_.qState = dT.BOAState; // for compatibility
                dT.BOAState.nest.addOutlinehandler(oH); // covert ECMM to Snake and store in BOA
                                                        // section
                stMap = new STmap();
                try {
                    stMap.motMap = QuimPArrayUtils.file2Array(",", local.getQp().getMotilityFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.convMap = QuimPArrayUtils.file2Array(",", local.getQp().getConvexFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.coordMap = QuimPArrayUtils.file2Array(",", local.getQp().getCoordFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.originMap =
                            QuimPArrayUtils.file2Array(",", local.getQp().getOriginFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.xMap = QuimPArrayUtils.file2Array(",", local.getQp().getxFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                try {
                    stMap.yMap = QuimPArrayUtils.file2Array(",", local.getQp().getyFile());
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
                maps.add(stMap);
                i++;
            } while (true); // exception thrown by QconfLoader will stop this loop
        } catch (Exception e) {
        }

        dT.QState = maps.toArray(new STmap[0]);
        Serializer<DataContainer> n;
        n = new Serializer<>(dT, new QuimpToolsCollection().getQuimPBuildInfo());
        n.setPretty();
        n.save(path + File.separator + orginal + QuimpConfigFilefilter.newFileExt);
        n = null;

    }

    /**
     * Recreate paQP and snQP files from QCONF.
     * <p>
     * Files are created in directory where QCONF is located.
     * 
     */
    public void generateOldDataFiles() {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can not convert from old format to old");
        try {
            DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
            if (dT.getECMMState() == null)
                generatepaQP(); // no ecmm data write snakes only
            else
                generatesnQP(); // write ecmm data
        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with running Analysis: " + e.getMessage());
        }
    }

    /**
     * Create paQP and snQP file. Latter one contains only pure snake data.
     * <p>
     * Those files are always saved together. snQP file will contain only pure snake data.
     * Files are created in directory where QCONF is located.
     * 
     * @throws IOException
     */
    public void generatepaQP() throws IOException {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can no convert from old format to old");
        // replace location to location of QCONF
        DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
        dT.getBOAState().boap
                .setOutputFileCore(QuimpToolsCollection.removeExtension(path.toString()));
        dT.BOAState.nest.writeSnakes(); // write paQP and snQP together
    }

    /**
     * Rewrite snQP file using recent ECMM processed results.
     * <p>
     * Files are created in directory where QCONF is located.
     * @throws QuimpException 
     * 
     */
    public void generatesnQP() throws IOException {
        if (qcL.getConfVersion() == QParams.QUIMP_11)
            throw new IllegalArgumentException("Can no convert from old format to old");
        int activeHandler = 0;
        // replace location to location of QCONF
        DataContainer dT = ((QParamsQconf) qcL.getQp()).getLoadedDataContainer();
        dT.BOAState.boap.setOutputFileCore(QuimpToolsCollection.removeExtension(path.toString()));
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
     * @throws Exception 
     * TODO Should not throw exception as generateOldDataFiles. Rework generateNewDataFile
     */
    public void doConversion() throws Exception {
        switch (qcL.getConfVersion()) {
            case QParams.NEW_QUIMP:
                generateOldDataFiles();
                break;
            case QParams.QUIMP_11:
                generateNewDataFile();
                break;
            default:
                throw new IllegalArgumentException("QuimP version not supported");
        }

    }

}
