/**
 * @file Plugin1_.java
 * @date 17 Feb 2016
 */
package uk.ac.warwick.wsbc;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.ViewUpdater;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 *
 */
public class Plugin1_ extends QWindowBuilder implements IQuimpPoint2dFilter, IQuimpPluginSynchro {

    private static final Logger LOGGER = LogManager.getLogger(Plugin1_.class.getName());

    private List<Point2d> points;
    private ViewUpdater qcontext;
    private ParamList uiDefinition; //!< Definition of UI for this plugin
    private int every; // every point to delete

    /**
     * 
     */
    public Plugin1_() {
        LOGGER.trace("Contructor of Plugin1 called");
        this.every = 3;
        uiDefinition = new ParamList(); // will hold ui definitions
    }

    @Override
    public int setup() {
        LOGGER.trace("setup of Plugin1 called");
        return DOES_SNAKES + CHANGE_SIZE;
    }

    @Override
    public void setPluginConfig(final ParamList par) throws QuimpPluginException {
        LOGGER.trace("setPluginConfig of Plugin1 called");

    }

    @Override
    public ParamList getPluginConfig() {
        LOGGER.trace("getPluginConfig of Plugin1 called");
        return null;
    }

    @Override
    public void showUI(boolean val) {
        LOGGER.trace("showUI of Plugin1 called with val " + val);
        toggleWindow(val);
    }

    @Override
    public String getVersion() {
        LOGGER.trace("getVersion of Plugin1 called");
        return "0.0.2";
    }

    /**
     * Simply modify every \c every node
     */
    @Override
    public List<Point2d> runPlugin() throws QuimpPluginException {
        LOGGER.debug("runPlugin of Plugin1 called with param every= " + every);
        ArrayList<Point2d> out = new ArrayList<>();

        return out;
    }

    @Override
    public void attachData(List<Point2d> data) {
        LOGGER.trace("attachData of Plugin1 called");
        points = data;
    }

    @Override
    public void attachContext(final ViewUpdater b) {
        qcontext = b;
    }

}
