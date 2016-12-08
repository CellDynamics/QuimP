package uk.ac.warwick.wsbc.QuimP.plugin.generatemask;

import java.awt.Color;
import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.Nest;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
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
 * @author p.baniukiewicz
 *
 */
public class GenerateMask implements IQuimpPlugin {
    static final Logger LOGGER = LoggerFactory.getLogger(GenerateMask.class.getName());

    /**
     * Loaded QCONF file.
     * 
     * Initialised by {@link #loadFile(File)} through this constructor.
     */
    private QconfLoader qconfLoader; // main object representing loaded configuration file
    // default configuration parameters, for future using
    ParamList paramList = new ParamList();

    /**
     * Default constructor.
     * 
     * Run parameterised constructor with <tt>null</tt> showing file selector.
     */
    public GenerateMask() {
        this(null);
    }

    /**
     * Constructor that allows to provide own file.
     * 
     * @param paramFile File to process.
     */
    public GenerateMask(File paramFile) {
        IJ.log(new QuimpToolsCollection().getQuimPversion());
        // validate registered user
        new Registration(IJ.getInstance(), "QuimP Registration");
        // check whether config file name is provided or ask user for it
        try {
            IJ.showStatus("Generate Mask");
            loadFile(paramFile); // load configuration file given by paramFile and verify it
            if (qconfLoader.getQp() == null)
                return; // not loaded
            runPlugin();
        } catch (Exception e) { // catch all exceptions here
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with run of GenerateMask plugin: " + e.getMessage());
        }
    }

    /**
     * Load configuration file. (only if not loaded before).
     * 
     * Validates also all necessary datafields in loaded QCONF file. Set <tt>qconfLoader</tt> field
     * on success or set it to <tt>null</tt>.
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
                throw new QuimpException("QconfLoader returned unsupported version of QuimP."
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
        BOAState bs = qconfLoader.getBOA();
        Nest nest = bs.nest;
        ImagePlus res = NewImage.createByteImage("test", bs.boap.getWIDTH(), bs.boap.getHEIGHT(),
                bs.boap.getFRAMES(), NewImage.FILL_BLACK);
        ImageStack contourStack = res.getStack();
        res.setSlice(1);
        int frame;
        Snake snake;
        ImageProcessor contourIp;
        for (frame = 1; frame <= bs.boap.getFRAMES(); frame++) {
            List<Integer> snakes = nest.getSnakesforFrame(frame);
            contourIp = contourStack.getProcessor(frame);
            contourIp.setColor(Color.WHITE);
            for (Integer snakeID : snakes) {
                SnakeHandler sH = nest.getHandlerofId(snakeID);
                if (sH != null) {
                    snake = sH.getStoredSnake(frame);
                    Roi roi = snake.asFloatRoi();
                    roi.setFillColor(Color.WHITE);
                    contourIp.fill(roi);
                }
            }
        }

        res.show();

    }

    @Override
    public int setup() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        paramList = new ParamList(par);
    }

    @Override
    public ParamList getPluginConfig() {
        return paramList;
    }

    @Override
    public void showUI(boolean val) {
    }

    @Override
    public String getVersion() {
        return "See QuimP version";
    }

    @Override
    public String about() {
        return "Generate mask plugin.\n" + "Author: Piotr Baniukiewicz\n"
                + "mail: p.baniukiewicz@warwick.ac.uk";
    }

    @Override
    public void runPlugin() throws QuimpPluginException {
        try {
            IJ.showStatus("Generate mask");
            runFromQCONF();
            IJ.log("Generate mask complete");
            IJ.showStatus("Finished");
        } catch (Exception e) { // catch all here and convert to expected type
            throw new QuimpPluginException(e);
        }

    }

}
