package uk.ac.warwick.wsbc.QuimP.plugin.generatemask;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.Nest;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimP;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.QuimpException.MessageSinkTypes;
import uk.ac.warwick.wsbc.QuimP.Snake;
import uk.ac.warwick.wsbc.QuimP.SnakeHandler;
import uk.ac.warwick.wsbc.QuimP.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.filesystem.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.registration.Registration;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * Convert QCONF files to BW masks.
 * 
 * Use Snake data produced by BOA and stored in QCONF file.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMask_ implements IQuimpPlugin {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(GenerateMask_.class.getName());

    /**
     * Loaded QCONF file.
     * 
     * Initialised by {@link #loadFile(File)} through this constructor.
     */
    private QconfLoader qconfLoader; // main object representing loaded configuration file

    /**
     * default configuration parameters, for future using.
     */
    ParamList paramList = new ParamList();

    /**
     * Indicate that plugin is run as macro from script. Blocks all UIs.
     */
    private MessageSinkTypes runAsMacro = MessageSinkTypes.GUI;

    /**
     * Default constructor. Empty as plugin is run by {@link #run(String)} method called by IJ
     * because of PlugIn interface
     */
    public GenerateMask_() {
    }

    /**
     * Constructor that allows to provide own file.
     * 
     * @param paramFile it can be null to ask user fo file or it can be parameters string like that
     *        passed in macro.
     * @see #about()
     */
    public GenerateMask_(String paramFile) {
        run(paramFile);
    }

    /**
     * Load configuration file. (only if not loaded before).
     * 
     * Validates also all necessary datafields in loaded QCONF file. Set <tt>qconfLoader</tt> field
     * on success or set it to <tt>null</tt>.
     * 
     * @param paramFile
     * 
     * @throws QuimpException When QCONF could not be loaded or it does not meet requirements.
     */
    private void loadFile(File paramFile) throws QuimpException {
        if (qconfLoader == null || qconfLoader.getQp() == null) {
            // load new file
            qconfLoader = new QconfLoader(paramFile,
                    new QuimpConfigFilefilter(FileExtensions.newConfigFileExt));
            if (qconfLoader.getQp() == null)
                return; // not loaded
            if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
                // validate in case new format
                qconfLoader.getBOA(); // will throw exception if not present
            } else {
                qconfLoader = null; // failed load or checking
                throw new QuimpPluginException("QconfLoader returned unsupported version of QuimP."
                        + " Only new format can be loaded");
            }
        }
    }

    /**
     * Main runner.
     * 
     * @throws QuimpException
     */
    private void runFromQCONF() throws QuimpException {
        IJ.showStatus("Generate mask");
        BOAState bs = qconfLoader.getBOA();
        Nest nest = bs.nest;
        // create output image
        ImagePlus res = NewImage.createByteImage("test", bs.boap.getWIDTH(), bs.boap.getHEIGHT(),
                bs.boap.getFRAMES(), NewImage.FILL_BLACK);
        // get stacks reference
        ImageStack contourStack = res.getStack();
        res.setSlice(1); // set for first
        int frame; // frmaes counter (from 1)
        Snake snake;
        ImageProcessor contourIp; // processor taken from stack (ref)
        for (frame = 1; frame <= bs.boap.getFRAMES(); frame++) { // iterate over frames
            List<Integer> snakes = nest.getSnakesforFrame(frame); // find all SnakeHandlers on frame
            contourIp = contourStack.getProcessor(frame); // get processor from stack for frame
            contourIp.setColor(Color.WHITE); // set plotting color
            for (Integer snakeID : snakes) { // iterate over SnakeHandlers
                SnakeHandler sH = nest.getHandlerofId(snakeID); // get SH of snakeID
                if (sH != null) {
                    snake = sH.getStoredSnake(frame); // get snake from this handler and current
                                                      // frame
                    Roi roi = snake.asFloatRoi(); // convert to ROI
                    roi.setFillColor(Color.WHITE);
                    contourIp.fill(roi); // plot on current slice
                }
            }
        }
        res.show();
        // save in QCONF folder
        QParamsQconf qp = (QParamsQconf) qconfLoader.getQp();
        Path filename =
                Paths.get(qp.getPath(), qp.getFileName() + FileExtensions.generateMaskSuffix);
        IJ.saveAsTiff(res, filename.toString());
        IJ.log("Saved in: " + filename.toString());
        if (runAsMacro == MessageSinkTypes.GUI) {
            JOptionPane.showMessageDialog(
                    IJ.getInstance(), QuimpToolsCollection
                            .stringWrap("Image saved! (see log to find path)", QuimP.LINE_WRAP),
                    "Saved!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            IJ.log("Mask generated!");
        }
        IJ.showStatus("Finished");

    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#setup()
     */
    @Override
    public int setup() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#setPluginConfig(uk.ac.warwick.wsbc.QuimP.
     * plugin.ParamList)
     */
    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        paramList = new ParamList(par);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#getPluginConfig()
     */
    @Override
    public ParamList getPluginConfig() {
        return paramList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#showUI(boolean)
     */
    @Override
    public void showUI(boolean val) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "See QuimP version";
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#about()
     */
    @Override
    public String about() {
        return "Generate mask plugin.\n" + "Author: Piotr Baniukiewicz\n"
                + "mail: p.baniukiewicz@warwick.ac.uk\n" + "This plugin supports macro parameters\n"
                + "\tfilenam=path_to_QCONF";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(String arg) {
        File paramFile;
        String params;
        IJ.log(new QuimpToolsCollection().getQuimPversion());
        // decode possible params passed in macro or from constructor
        if (arg == null) {
            params = Macro.getOptions();
        } else {
            params = arg;
        }
        if (params == null) { // no params - set file to null to allow show file
                              // selector by QcobfLoader
            paramFile = null;
        } else { // there is something, parse it
            runAsMacro = MessageSinkTypes.CONSOLE; // set errors to console
            String val = Macro.getValue(params, "filename", null);
            if (val == null) {
                LOGGER.error("Wrong macro parameter\n" + about());
                return;
            }
            paramFile = new File(val);
        }
        // validate registered user
        new Registration(IJ.getInstance(), "QuimP Registration");
        // check whether config file name is provided or ask user for it
        try {
            IJ.showStatus("Generate Mask");
            loadFile(paramFile); // load configuration file given by paramFile and verify it
            if (qconfLoader.getQp() == null)
                return; // not loaded
            runFromQCONF();
        } catch (QuimpException qe) {
            qe.setMessageSinkType(runAsMacro);
            qe.handleException(IJ.getInstance(), "GenerateMask:");
        } catch (Exception e) { // catch all exceptions here
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with running GenerateMask plugin: " + e.getMessage());
        }

    }

}
