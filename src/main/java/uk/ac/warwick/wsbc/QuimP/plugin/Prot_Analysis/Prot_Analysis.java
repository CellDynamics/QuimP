/**
 * @file Prot_Analysis.java
 * @date 13 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.io.File;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.Tool;

/**
 * @author p.baniukiewicz
 * @date 13 Aug 2016
 *
 */
public class Prot_Analysis {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Prot_Analysis.class.getName());
    private QParams qp; // QCONF or paQP data

    /**
     * Main constructor and runner - class entry point.
     * <p>
     * Left in this form for backward compatibility
     */
    public Prot_Analysis() {
        this(null);
    }

    /**
     * Parameterized constructor for tests.
     * 
     * @param path Path to *.paQP/QCONF file. If <tt>null</tt> user is asked for this file
     */
    public Prot_Analysis(Path path) {
        try {
            IJ.showStatus("Protrusion Analysis");
            IJ.log(new Tool().getQuimPversion());
            String directory; // directory with paQP
            String filename; // file name of paQP

            if (path == null) { // no file provided, ask user
                OpenDialog od =
                        new OpenDialog("Open paramater file " + BOAState.QCONFFILEEXT + ")...",
                                OpenDialog.getLastDirectory(), BOAState.QCONFFILEEXT);
                if (od.getFileName() == null) {
                    IJ.log("Cancelled - exiting...");
                    return;
                }
                directory = od.getDirectory();
                filename = od.getFileName();
            } else // use name provided in constructor
            {
                // getParent can return null
                directory = path.getParent() == null ? "" : path.getParent().toString();
                filename = path.getFileName() == null ? "" : path.getFileName().toString();
                LOGGER.debug("Use provided file:" + directory + " " + filename);
            }
            // detect old/new file format
            File paramFile = new File(directory, filename); // config file
            if (paramFile.getName().endsWith(BOAState.QCONFFILEEXT)) // new file format TODO #152
                qp = new QParamsQconf(paramFile);
            else
                qp = new QParams(paramFile); // initialize general param storage
            qp.readParams(); // create associated files included in paQP and read params
            // check conditions
            if (!validateQconf())
                return;
            // show dialog
            if (!showDialog()) { // if user cancelled dialog
                return; // do nothing
            }

            // prepare current OutlineHandler to process (read it using loaded paQP file)
            if (qp.paramFormat == QParams.QUIMP_11) { // if we have old format, read outlines from
                                                      // OutlineHandler
                runFromPAQP();
            } else if (qp.paramFormat == QParams.NEW_QUIMP) { // new format, everything is read by
                                                              // readParams, just extract it
                runFromQCONF();
            } else {
                throw new IllegalStateException("You can not be here in this time!");
            }

            IJ.log("Protrusion Analysis complete");
            IJ.showStatus("Finished");
        } catch (QuimpException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with running Protrusion Analysis: " + e.getMessage());
        }
    }

    private boolean showDialog() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Validate whether loaded QCONF file contains correct data.
     * <p>
     * Check for presence ECMM and Q Analysis data in loaded QCONF.
     * 
     * @return <tt>true</tt> always for maintaining compatibility with
     * {@link uk.ac.warwick.wsbc.QuimP.Q_Analysis#validateQconf()}
     * and {@link uk.ac.warwick.wsbc.QuimP.ECMM_Mapping#validateQconf()}
     * 
     * @throws QuimpException When there is no ECMM or Q Analysis data in file
     */
    private boolean validateQconf() throws QuimpException {
        if (qp == null) {
            throw new QuimpException("QCONF file not loaded");
        }
        if (qp.getLoadedDataContainer().ECMMState == null) {
            throw new QuimpException("ECMM data not found in QCONF file. Run ECMM first.");
        }
        if (qp.getLoadedDataContainer().QState != null) {
            throw new QuimpException(
                    "Q Analysis data not found in QCONF file. Run Q Analysis first.");
        }
        return true;
    }

    private void runFromQCONF() {
        throw new UnsupportedOperationException("Not implementes yet");

    }

    private void runFromPAQP() {
        throw new UnsupportedOperationException("Not implementes yet");

    }

}
