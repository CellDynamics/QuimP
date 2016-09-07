package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class allows for recreating paQP and snQP files from new format QCONF.
 * <p><b>Note</b><p>
 * Other files such as stQP, maps and images are generated regardless file format used during saving
 * in each QuimP module.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverter {
    private static final Logger LOGGER = LogManager.getLogger(FormatConverter.class.getName());
    private QParamsQconf qP;
    private Path path;

    /**
     * Construct FormatConverter from QCONF file.
     * 
     * @param newDataFile QCONF file
     */
    public FormatConverter(File newDataFile) {
        LOGGER.debug("Use provided file:" + newDataFile.toString());
        qP = new QParamsQconf(newDataFile);
        path = Paths.get(newDataFile.getAbsolutePath());
        try {
            qP.readParams();
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
    public FormatConverter(QParamsQconf qP, Path path) {
        LOGGER.debug("Use provided QParams");
        this.qP = qP;
        this.path = path;
    }

    /**
     * Recreate paQP and snQP files from QCONF.
     * <p>
     * Files are created in directory where QCONF is located.
     * 
     */
    public void generateOldDataFiles() {
        try {
            DataContainer dT = qP.getLoadedDataContainer();
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
        // replace location to location of QCONF
        DataContainer dT = qP.getLoadedDataContainer();
        dT.getBOAState().boap.setOutputFileCore(Tool.removeExtension(path.toString()));
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
        int activeHandler = 0;
        // replace location to location of QCONF
        DataContainer dT = qP.getLoadedDataContainer();
        dT.BOAState.boap.setOutputFileCore(Tool.removeExtension(path.toString()));
        Iterator<OutlineHandler> oHi = dT.getECMMState().oHs.iterator();
        do {
            qP.setActiveHandler(activeHandler++);
            OutlineHandler oH = oHi.next();
            oH.writeOutlines(qP.getSnakeQP(), true);
            qP.writeOldParams();
        } while (oHi.hasNext());

    }

}
