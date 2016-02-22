import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter;

/**
 * Interpolation of points (X,Y) by means of running mean method
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 *
 */
public class MeanSnakeFilter
        implements IQuimpPoint2dFilter<Vector2d>, IPadArray {

    private static final Logger LOGGER = LogManager
            .getLogger(MeanSnakeFilter.class.getName());
    private QuimpDataConverter xyData; // input List converted to separate X and
                                       // Y arrays
    private int window; // size of processing window
    private HashMap<String, String[]> uiDefinition; // Definition of UI
    private QWindowBuilderInst uiInstance;

    /**
     * Create running mean filter.
     * 
     * All default parameters should be declared here. Non-default are passed by
     * setPluginConfig(HashMap<String, Object>)
     */
    public MeanSnakeFilter() {
        LOGGER.trace("Entering constructor");
        this.window = 7; // default value
        LOGGER.debug("Set default parameter: window=" + window);
        // create UI using QWindowBuilder
        uiDefinition = new HashMap<String, String[]>(); // will hold ui
                                                        // definitions
        uiDefinition.put("name", new String[] { "MeanFilter" }); // name of win
        // the name of this ui control is "system-wide", now it will define ui
        // and name of numerical data related to this ui and parameter
        uiDefinition.put("window", new String[] { "spinner", "1", "21", "2",
                Integer.toString(window) });
        uiDefinition.put("help", new String[] { "Window shoud be uneven" });
        uiInstance = new QWindowBuilderInst(); // create window object, class
                                               // QWindowBuilder is abstract so
                                               // it must be extended
        uiInstance.buildWindow(uiDefinition); // construct ui (not shown yet)
    }

    /**
     * Attach data to process.
     * 
     * Data are as list of vectors defining points of polygon. Passed points
     * should be sorted according to a clockwise or anti-clockwise direction
     * 
     * @param data Polygon points
     * @see wsbc.plugin.snakes.IQuimpPoint2dFilter.attachData(List<E>)
     */
    @Override
    public void attachData(List<Vector2d> data) {
        LOGGER.trace("Entering attachData");
        xyData = new QuimpDataConverter(data); // helper for converting from
                                               // List<Vector2d> to X[], Y[]
    }

    /**
     * Perform interpolation of data by a moving average filter with given
     * window
     * 
     * By default uses \b CIRCULAR padding. The window must be uneven, positive
     * and shorter than data vector. \c X and \c Y coordinates of points are
     * smoothed separately.
     * 
     * @return Filtered points as list of Vector2d objects
     * @throws QuimpPluginException
     * when: - window is even - window is longer or equal processed
     * data - window is negative
     */
    @Override
    public List<Vector2d> runPlugin() throws QuimpPluginException {
        // collect parameters from window
        window = uiInstance.getIntegerFromUI("window");
        LOGGER.debug(
                String.format("Run plugin with params: window %d", window));

        // do filtering
        int cp = window / 2; // left and right range of window
        double meanx = 0;
        double meany = 0; // mean of window
        int indexTmp; // temporary index after padding
        List<Vector2d> out = new ArrayList<Vector2d>();

        if (window % 2 == 0)
            throw new QuimpPluginException("Input argument must be uneven");
        if (window >= xyData.size())
            throw new QuimpPluginException("Processing window to long");
        if (window < 0)
            throw new QuimpPluginException("Processing window is negative");

        for (int c = 0; c < xyData.size(); c++) { // for every point in data
            meanx = 0;
            meany = 0;
            for (int cc = c - cp; cc <= c + cp; cc++) { // collect points in
                                                        // range c-2 c-1 c-0 c+1
                                                        // c+2 (for window=5)
                indexTmp = IPadArray.getIndex(xyData.size(), cc,
                        IPadArray.CIRCULARPAD);
                meanx += xyData.getX()[indexTmp];
                meany += xyData.getY()[indexTmp];
            }
            meanx = meanx / window;
            meany = meany / window;
            out.add(new Vector2d(meanx, meany));
        }
        return out;
    }

    /**
     * This method should return a flag word that specifies the filters
     * capabilities.
     * 
     * @return Configuration codes
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin
     * @see uk.ac.warwick.wsbc.plugin.IQuimpPlugin.setup()
     */
    @Override
    public int setup() {
        LOGGER.trace("Entering setup");
        return DOES_SNAKES;
    }

    /**
     * Configure plugin and overrides default values.
     * 
     * Supported keys:
     * -# \c window - size of window
     * 
     * @param par configuration as pairs <key,val>. Keys are defined by plugin
     * creator and plugin caller do not modify them.
     * @throws QuimpPluginException on wrong parameters list or wrong parameter
     * conversion
     * @see wsbc.plugin.IQuimpPlugin.setPluginConfig(HashMap<String, Object>)
     */
    @Override
    public void setPluginConfig(HashMap<String, String> par)
            throws QuimpPluginException {
        try {
            window = Integer.parseInt(par.get("window")); // by default all
                                                          // numeric values
                                                          // are passed as
                                                          // double
            uiInstance.setValues(par); // populate loaded values to UI
        } catch (Exception e) {
            // we should never hit this exception as parameters are not touched
            // by caller they are only passed to configuration saver and
            // restored from it
            throw new QuimpPluginException(
                    "Wrong input argument->" + e.getMessage(), e);
        }
    }

    /**
     * Transfer plugin configuration to QuimP
     * 
     * Only parameters mapped to UI by QWindowBuilder are supported directly by
     * getValues() Any other parameters created outside QWindowBuilder should be
     * added here manually.
     */
    @Override
    public Map<String, String> getPluginConfig() {
        return uiInstance.getValues();
    }

    @Override
    public void showUI(boolean val) {
        LOGGER.debug("Got message to show UI");
        uiInstance.toggleWindow();
    }

    @Override
    public String getVersion() {
        return null;
    }
}

/**
 * Instance private class for tested QWindowBuilder
 * 
 * Any overrides of UI methods can be done here. For example user can attach
 * listener to ui object.
 * 
 * @author p.baniukiewicz
 * @date 5 Feb 2016
 *
 */
class QWindowBuilderInst extends QWindowBuilder {

    /**
     * Example how to protect against even values for \c window parameter First
     * override BuildWindow method. It provides \a protected access to \c ui
     * HashMap list that keeps references to all ui elements under relevant
     * keys, the keys relates to those names that were put in ui definition
     * string. The initial values provided in ui definition string start from 1
     * with step 2, therefore only one possibility to get even value here is by
     * editing it manually
     * 
     * @param def window definition string
     * @see BuildWindow
     */
    @Override
    public void buildWindow(Map<String, String[]> def) {
        super.buildWindow(def); // window must be built first
        ChangeListener changeListner = new ChangeListener() { // create new
                                                              // listener that
                                                              // will be
                                                              // attached to ui
                                                              // element
            @Override
            public void stateChanged(ChangeEvent ce) {
                Object source = ce.getSource();
                JSpinner s = (JSpinner) ui.get("window"); // get ui element
                if (source == s) { // check if this event concerns it
                    LOGGER.debug("Spinner used");
                    if (((Double) s.getValue()).intValue() % 2 == 0)
                        s.setValue((Double) s.getValue() + 1);
                }

            }
        };
        // attach listener to selected ui
        ((JSpinner) ui.get("window")).addChangeListener(changeListner);
    }
}
