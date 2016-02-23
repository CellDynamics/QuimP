/**
 * @file Plugin2.java
 * @date 17 Feb 2016
 */
package uk.ac.warwick.wsbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 *
 */
public class Plugin2_quimp implements IQuimpPoint2dFilter<Vector2d> {

    private static final Logger LOGGER =
            LogManager.getLogger(Plugin2_quimp.class.getName());

    private HashMap<String, String> config;

    /**
     * 
     */
    public Plugin2_quimp() {
        LOGGER.trace("Contructor of Plugin2 called");
        config = new HashMap<>();
    }

    @Override
    public int setup() {
        LOGGER.trace("setup of Plugin2 called");
        return DOES_SNAKES;
    }

    @Override
    public void setPluginConfig(HashMap<String, String> par)
            throws QuimpPluginException {
        LOGGER.trace("setPluginConfig of Plugin2 called");
        config.putAll(par);

    }

    @Override
    public Map<String, String> getPluginConfig() {
        LOGGER.trace("getPluginConfig of Plugin2 called");
        return config;
    }

    @Override
    public void showUI(boolean val) {
        LOGGER.trace("showUI of Plugin2 called");

    }

    @Override
    public String getVersion() {
        LOGGER.trace("getVersion of Plugin2 called");
        return "0.0.1";
    }

    @Override
    public List<Vector2d> runPlugin() throws QuimpPluginException {
        LOGGER.trace("runPlugin of Plugin2 called");
        return null;
    }

    @Override
    public void attachData(List<Vector2d> data) {
        LOGGER.trace("attachData of Plugin2 called");
    }

}
