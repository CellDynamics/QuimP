/**
 * @file QuimpPluginCore.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import java.io.File;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.Tool;

/**
 * Add basic support for handling <i>QCONF</i> and <i>paQP</i> files.
 * <p>
 * Load provided file and construct {@link uk.ac.warwick.wsbc.QuimP.QParams.QParams(File) QParams}
 * or {@link uk.ac.warwick.wsbc.QuimP.QParamsQconf.QParamsQconf(File) QParamsQconf} object
 * depending on type of input file.
 * <p>
 * The main processing happens in constructor that performs the following operations:
 * <ol>
 * <li>Load provided configuration file.
 * <li>Run {@link validateQconf()}
 * <li>Run {@link showDialog()}
 * <li>Run {@link runFromPAQP()} or {@link runFromQCONF()}
 * </ol>
 * The three last should be replaced in child class.
 *  
 * @author p.baniukiewicz
 *
 */
public abstract class QuimpPluginCore {
    private static final Logger LOGGER = LogManager.getLogger(QuimpPluginCore.class.getName());
    /**
     * Main object holding loaded configuration file. It can be either traditional QParams or
     * QParamsQconf for newer format.  
     */
    protected QParams qp;

    /**
     * 
     */
    public QuimpPluginCore() {
        this(null);
    }

    /**
     * Parameterized constructor.
     * <p>
     * Main runner.
     * 
     * @param path Path to *.paQP/QCONF file. If <tt>null</tt> user is asked for this file
     */
    public QuimpPluginCore(Path path) {
        try {
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

            IJ.log("Analysis complete");
            IJ.showStatus("Finished");
        } catch (QuimpException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with running Analysis: " + e.getMessage());
        }
    }

    /**
     * Display user dialog.
     * <p>
     * Should return <tt>true</tt> when there is no dialog. <tt>false</tt> will stop processing.
     * 
     * @return true on success or false when dialog canceled.
     */
    public boolean showDialog() {
        return true;
    }

    /**
     * Validate whether loaded <i>QCONF</i> file contains correct data.
     * <p>
     * Check for presence ECMM and Q Analysis data in loaded QCONF.
     * 
     * @return <tt>true</tt> always for maintaining compatibility with
     * {@link uk.ac.warwick.wsbc.QuimP.Q_Analysis#validateQconf()}
     * and {@link uk.ac.warwick.wsbc.QuimP.ECMM_Mapping#validateQconf()}
     * 
     * @throws QuimpException When there is no ECMM or Q Analysis data in file
     */
    public boolean validateQconf() throws QuimpException {
        if (qp == null) {
            throw new QuimpException("QCONF file not loaded");
        }
        if (qp.paramFormat != QParams.NEW_QUIMP) // do not check if old format
            return true;
        if (qp.getLoadedDataContainer().ECMMState == null) {
            throw new QuimpException("ECMM data not found in QCONF file. Run ECMM first.");
        }
        if (qp.getLoadedDataContainer().QState == null) {
            throw new QuimpException(
                    "Q Analysis data not found in QCONF file. Run Q Analysis first.");
        }
        return true;
    }

    /**
     * Executed when input file is <i>QCONF</i>.
     */
    public void runFromQCONF() {
        LOGGER.warn("Not implemented here");
    }

    /**
     * Executed when input file is <i>paQP</i>.
     */
    public void runFromPAQP() {
        LOGGER.warn("Not implemented here");
    }

    /**
     * Getter for loaded configuration data.
     * 
     * Useful when this class is used only for loading configuration files. 
     * @return the qp
     */
    public QParams getQp() {
        return qp;
    }

}
