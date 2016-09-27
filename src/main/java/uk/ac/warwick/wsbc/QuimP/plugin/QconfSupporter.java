/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpConfigFilefilter;
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
@Deprecated
public abstract class QconfSupporter {
    private static final Logger LOGGER = LogManager.getLogger(QconfSupporter.class.getName());
    /**
     * Main object holding loaded configuration file. It can be either traditional QParams or
     * QParamsQconf for newer format.  
     */
    protected QParams qp;

    /**
     * 
     */
    public QconfSupporter() {
        this(null);
    }

    /**
     * Parameterized constructor.
     * <p>
     * Main runner.
     * 
     * @param path Path to *.paQP/QCONF file. If <tt>null</tt> user is asked for this file
     */
    public QconfSupporter(Path path) {
        try {
            IJ.log(new Tool().getQuimPversion());
            String directory; // directory with paQP
            String filename; // file name of paQP

            if (path == null) { // no file provided, ask user
                OpenDialog od = new OpenDialog(
                        "Open paramater file " + QuimpConfigFilefilter.newFileExt + ")...",
                        OpenDialog.getLastDirectory(), QuimpConfigFilefilter.newFileExt);
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
            if (paramFile.getName().endsWith(QuimpConfigFilefilter.newFileExt)) // new file format
                                                                                // TODO #152
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
     * Try to load image associated with QCONF file.
     * 
     * @return Loaded image from QCONF or that pointed by user.
     * @throws QuimpException When user canceled and image is not loaded
     */
    public ImagePlus getImage() throws QuimpException {
        LOGGER.debug("Attempt to open image: "
                + qp.getLoadedDataContainer().getBOAState().boap.getOrgFile().getAbsolutePath());
        // try to load from QCONF
        ImagePlus im =
                IJ.openImage(qp.getLoadedDataContainer().getBOAState().boap.getOrgFile().getPath());
        if (im == null) { // if failed ask user
            Object[] options = { "Load from disk", "Load from IJ", "Cancel" };
            int n = JOptionPane.showOptionDialog(IJ.getInstance(),
                    "The image pointed in loaded QCONF file can not be found.\n"
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
        }
        LOGGER.debug("Opened image: " + im);
        if (im == null)
            throw new QuimpException("No valid image provided");
        return im;
    }

    /**
     * Validate whether loaded <i>QCONF</i> file contains correct data.
     * <p>
     * Check for presence ECMM, and Q Analysis data in loaded QCONF.
     * <p>
     * <b>warning</b><p>
     * ANA is not obligatory and it is not checked here.
     * <p>
     * @return <tt>true</tt> always for maintaining compatibility with
     * {@link uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.Q_Analysis#validateQconf()}
     * and {@link uk.ac.warwick.wsbc.QuimP.plugin.ecmm.ECMM_Mapping#validateQconf()}
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
     * @throws QuimpException 
     */
    public void runFromQCONF() throws QuimpException {
        LOGGER.warn("Not implemented here");
    }

    /**
     * Executed when input file is <i>paQP</i>.
     * @throws QuimpException
     */
    public void runFromPAQP() throws QuimpException {
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
