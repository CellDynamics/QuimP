/**
 * @file Plugin1.java
 * @date 17 Feb 2016
 */
package uk.ac.warwick.wsbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.plugin.snakes.IQuimpPoint2dFilter;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 *
 */
public class Plugin1 implements IQuimpPoint2dFilter<Vector2d> {

    private static final Logger LOGGER =
            LogManager.getLogger(Plugin1.class.getName());

    /**
     * 
     */
    public Plugin1() {
        LOGGER.trace("Contructor of Plugin1 called");
    }

    @Override
    public int setup() {
        LOGGER.trace("setup of Plugin1 called");
        return DOES_SNAKES + CHANGE_SIZE;
    }

    @Override
    public void setPluginConfig(HashMap<String, Object> par)
            throws QuimpPluginException {
        LOGGER.trace("setPluginConfig of Plugin1 called");

    }

    @Override
    public Map<String, Object> getPluginConfig() {
        LOGGER.trace("getPluginConfig of Plugin1 called");
        return null;
    }

    @Override
    public void showUI(boolean val) {
        LOGGER.trace("showUI of Plugin1 called");
    }

    @Override
    public String getVersion() {
        LOGGER.trace("getVersion of Plugin1 called");
        return "0.0.2";
    }

    @Override
    public List<Vector2d> runPlugin() throws QuimpPluginException {
        LOGGER.trace("runPlugin of Plugin1 called");
        return null;
    }

    @Override
    public void attachData(List<Vector2d> data) {
        LOGGER.trace("attachData of Plugin1 called");
    }

}
