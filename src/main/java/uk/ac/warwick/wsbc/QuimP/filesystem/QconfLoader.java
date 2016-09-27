package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.awt.FileDialog;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.ANAStates;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.OutlineHandlers;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.StatsHandlers;

/**
 * Load QCONF or paQP file and initiate proper instance of {@link QParams} class.
 * <p>
 * Provide also methods for QCONF verification and loading image file associated with it with 
 * user assistance.
 * 
 * @author p.baniukiewicz
 *
 */
public class QconfLoader {
    private static final Logger LOGGER = LogManager.getLogger(QconfLoader.class.getName());
    /**
     * Stand for bad QCONF file that can not be loaded.
     */
    public final static int QCONF_INVALID = 0;
    /**
     * Main object holding loaded configuration file. It can be either traditional QParams or
     * QParamsQconf for newer format.  
     */
    private QParams qp = null;

    /**
     * Default constructor. 
     * 
     * Bring file dialog to load QCONF.
     * @throws QuimpException when QCONF can not be loaded
     */
    public QconfLoader() throws QuimpException {
        this(null);
    }

    /**
     * Parameterised constructor.
     * <p>
     * Main runner.
     * 
     * @param path Path to *.paQP/QCONF file. If <tt>null</tt> user is asked for this file
     * @throws QuimpException when QCONF can not be loaded
     */
    public QconfLoader(Path path) throws QuimpException {
        String directory; // directory with paQP
        String filename; // file name of paQP

        if (path == null) { // no file provided, ask user
            QuimpConfigFilefilter fileFilter = new QuimpConfigFilefilter(); // use default
            // engine for
            // finding extension
            FileDialog od = new FileDialog(IJ.getInstance(),
                    "Open paramater file " + fileFilter.toString());
            od.setFilenameFilter(fileFilter);
            od.setDirectory(OpenDialog.getLastDirectory());
            od.setMultipleMode(false);
            od.setMode(FileDialog.LOAD);
            od.setVisible(true);
            if (od.getFile() == null) {
                IJ.log("Cancelled - exiting...");
                return;
            }
            directory = od.getDirectory();
            filename = od.getFile();
        } else // use name provided in constructor
        {
            // getParent can return null
            directory = path.getParent() == null ? "" : path.getParent().toString();
            filename = path.getFileName() == null ? "" : path.getFileName().toString();
            LOGGER.debug("Use provided file:" + directory + " " + filename);
        }
        // detect old/new file format
        File paramFile = new File(directory, filename); // config file
        if (paramFile.getName().toLowerCase()
                .endsWith(QuimpConfigFilefilter.newFileExt.toLowerCase())) // TODO #152
            qp = new QParamsQconf(paramFile);
        else
            qp = new QParams(paramFile); // initialize general param storage
        qp.readParams(); // create associated files included in paQP and read params
    }

    /**
     * Try to load image associated with QCONF or paQP file.
     * 
     * If image has not been found, user is being asked to point relevant file.
     * @return Loaded image from QCONF or that pointed by user. <tt>null</tt> if user cancelled.
     */
    public ImagePlus getImage() {
        if (getQp() == null)
            return null;
        ImagePlus im;
        switch (getQp().paramFormat) {
            case QParams.NEW_QUIMP:
                LOGGER.debug(
                        "Attempt to open image: " + qp.getLoadedDataContainer().getBOAState().boap
                                .getOrgFile().getAbsolutePath());
                // try to load from QCONF
                im = IJ.openImage(
                        qp.getLoadedDataContainer().getBOAState().boap.getOrgFile().getPath());
                break;
            case QParams.QUIMP_11:
                LOGGER.debug("Attempt to open image: " + qp.getSegImageFile().toString());
                // try to load from QCONF
                im = IJ.openImage(qp.getSegImageFile().toString());
                break;
            default:
                im = null;
        }

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
        return im;

    }

    /**
     * Validate loaded QCONF file in accordance to modules run on it.
     * 
     * @return:
     * <ol>
     * <li> 0 if QCONF is not loaded properly.
     * <li> QParams.QUIMP_11 if it is in old format
     * <li> {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()} flags otherwise
     * </ol>
     */
    public int validateQconf() {
        if (getQp() == null) {
            return QconfLoader.QCONF_INVALID;
        }
        if (getQp().paramFormat != QParams.NEW_QUIMP) // do not check if old format
            return QParams.QUIMP_11;
        return getQp().getLoadedDataContainer().validateDataContainer();
    }

    /**
     * Just decoder of {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()}.
     *  
     * @return true if BOA module was run.
     */
    public boolean isBOAPresent() {
        int ret = validateQconf();
        if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11)
            return false;
        if ((ret & DataContainer.BOA_RUN) == DataContainer.BOA_RUN)
            return true;
        else
            return false;
    }

    /**
     * Just decoder of {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()}.
     *  
     * @return true if ECMM module was run.
     */
    public boolean isECMMPresent() {
        int ret = validateQconf();
        if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11)
            return false;
        if ((ret & DataContainer.ECMM_RUN) == DataContainer.ECMM_RUN)
            return true;
        else
            return false;

    }

    /**
     * Just decoder of {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()}.
     *  
     * @return true if ANA module was run.
     */
    public boolean isANAPresent() {
        int ret = validateQconf();
        if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11)
            return false;
        if ((ret & DataContainer.ANA_RUN) == DataContainer.ANA_RUN)
            return true;
        else
            return false;

    }

    /**
     * Just decoder of {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()}.
     *  
     * @return true if Q module was run.
     */
    public boolean isQPresent() {
        int ret = validateQconf();
        if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11)
            return false;
        if ((ret & DataContainer.Q_RUN) == DataContainer.Q_RUN)
            return true;
        else
            return false;
    }

    /**
     * Just decoder of {@link uk.ac.warwick.wsbc.QuimP.DataContainer.validateDataContainer()}.
     *  
     * @return true if stats are present.
     */
    private boolean isStatsPresent() {
        int ret = validateQconf();
        if (ret == QconfLoader.QCONF_INVALID || ret == QParams.QUIMP_11)
            return false;
        if ((ret & DataContainer.STATS_AVAIL) == DataContainer.STATS_AVAIL)
            return true;
        else
            return false;
    }

    /**
     * 
     * @return BOAState object from loaded configuration
     * @throws QuimpException when there is no such object in file or old format is used.
     */
    public BOAState getBOA() throws QuimpException {
        if (isBOAPresent())
            return getQp().getLoadedDataContainer().getBOAState();
        else
            throw new QuimpException("BOA data not found in QCONF file. Run BOA first.");
    }

    /**
     * 
     * @return ECMM object from loaded configuration
     * @throws QuimpException when there is no such object in file or old format is used.
     */
    public OutlineHandlers getECMM() throws QuimpException {
        if (isECMMPresent())
            return getQp().getLoadedDataContainer().getECMMState();
        else
            throw new QuimpException("ECMM data not found in QCONF file. Run ECMM first.");
    }

    /**
     * 
     * @return ANA object from loaded configuration
     * @throws QuimpException when there is no such object in file or old format is used.
     */
    public ANAStates getANA() throws QuimpException {
        if (isANAPresent())
            return getQp().getLoadedDataContainer().getANAState();
        else
            throw new QuimpException("ANA data not found in QCONF file. Run ANA first.");
    }

    /**
     * 
     * @return Q object from loaded configuration
     * @throws QuimpException when there is no such object in file or old format is used.
     */
    public STmap[] getQ() throws QuimpException {
        if (isQPresent())
            return getQp().getLoadedDataContainer().getQState();
        else
            throw new QuimpException("Q data not found in QCONF file. Run Q Analysis first.");
    }

    /**
     * 
     * @return Q object from loaded configuration
     * @throws QuimpException when there is no such object in file or old format is used.
     */
    public StatsHandlers getStats() throws QuimpException {
        if (isStatsPresent())
            return getQp().getLoadedDataContainer().getStats();
        else
            throw new QuimpException("Stats not found in QCONF file. Run BOA Analysis first.");
    }

    /**
     * @return the qp
     */
    public QParams getQp() {
        return qp;
    }

    /**
     * 
     * @return Version of loaded file, see {@link QParams}
     */
    public int getConfVersion() {
        return getQp().paramFormat;
    }

}
