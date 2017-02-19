package uk.ac.warwick.wsbc.QuimP.plugin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.Macro;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.QuimpException.MessageSinkTypes;
import uk.ac.warwick.wsbc.QuimP.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.filesystem.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.QuimP.registration.Registration;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * @author p.baniukiewicz
 *
 */
public abstract class PluginTemplate implements IQuimpPlugin {

    /**
     * Loaded QCONF file.
     * 
     * must be overridden by {@link #parseOptions(String)} or left null to force {@link QconfLoader}
     * to show file selector.
     */
    protected File paramFile = null;

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PluginTemplate.class.getName());

    /**
     * Indicate that plugin is run as macro from script. Blocks all UIs.
     */
    protected MessageSinkTypes runAsMacro = MessageSinkTypes.GUI;

    /**
     * Loaded QCONF file.
     * 
     * Initialised by {@link #loadFile(File)} through this constructor.
     */
    protected QconfLoader qconfLoader; // main object representing loaded configuration file

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

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#setup()
     */
    @Override
    public abstract int setup();

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#setPluginConfig(uk.ac.warwick.wsbc.QuimP.
     * plugin.ParamList)
     */
    @Override
    public abstract void setPluginConfig(ParamList par) throws QuimpPluginException;

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#getPluginConfig()
     */
    @Override
    public abstract ParamList getPluginConfig();

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#showUI(boolean)
     */
    @Override
    public abstract void showUI(boolean val);

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#getVersion()
     */
    @Override
    public abstract String getVersion();

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin#about()
     */
    @Override
    public abstract String about();

    /**
     * This method should assign all internal variables of child class to values read from options.
     * 
     * It should also deal with null and must set {@link #paramFile} variable.
     * 
     * @param options string in form key=val,key1=val1, etc or null
     */
    protected abstract void parseOptions(String options);

    /*
     * (non-Javadoc)
     * 
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(String arg) {

        String options;
        IJ.log(new QuimpToolsCollection().getQuimPversion());
        // decode possible params passed in macro or from constructor
        if (arg == null) {
            options = Macro.getOptions();
        } else {
            options = arg;
        }
        if (options == null) { // no params - set file to null to allow show file
                               // selector by QcobfLoader
            parseOptions(options);
        } else { // there is something, parse it
            runAsMacro = MessageSinkTypes.CONSOLE; // set errors to console
            parseOptions(options);
        }
        // validate registered user
        new Registration(IJ.getInstance(), "QuimP Registration");
        // check whether config file name is provided or ask user for it
        try {
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

    /**
     * @throws QuimpException
     */
    protected abstract void runFromQCONF() throws QuimpException;

}
