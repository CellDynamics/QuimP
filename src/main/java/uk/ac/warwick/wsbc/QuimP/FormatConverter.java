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
 * <p><b>Note</b>
 * Other files such as stQP, maps and images are generated regardless file format used during saving
 * in each QuimP module.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverter {
    private static final Logger LOGGER = LogManager.getLogger(FormatConverter.class.getName());
    private DataContainer dT;
    private QParamsQconf qP;
    private Path path;
    private File newDataFile;

    /**
     * Construct FormatConverter from QCONF file.
     * 
     * @param newDataFile QCONF file
     */
    public FormatConverter(File newDataFile) {
        this.newDataFile = newDataFile;
    }

    /**
     * Recreate paQP and snQP files from QCONF.
     * <p>
     * Files are created in directory where QCONF is located.
     * 
     */
    public void generateOldDataFiles() {
        LOGGER.debug("Use provided file:" + newDataFile.toString());
        qP = new QParamsQconf(newDataFile);
        path = Paths.get(newDataFile.getAbsolutePath());
        try {
            qP.readParams();
            dT = qP.getLoadedDataContainer();
            if (dT.ECMMState == null)
                generatepaQP(); // no ecmm data write snakes only
            else
                generatesnQP(); // write ecmm data
        } catch (QuimpException | IOException e) {
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
        dT.BOAState.boap.setOutputFileCore(Tool.removeExtension(path.toString()));
        dT.BOAState.nest.writeSnakes(); // write paQP and snQP together
    }

    /**
     * Rewrite snQP file using recent ECMM processed results.
     * <p>
     * Files are created in directory where QCONF is located.
     * @throws QuimpException 
     * 
     */
    public void generatesnQP() throws QuimpException {
        int activeHandler = 0;
        // replace location to location of QCONF
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
