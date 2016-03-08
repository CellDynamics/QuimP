/**
 * @file Plugin2_.java
 * @date 17 Feb 2016
 */
package uk.ac.warwick.wsbc;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 *
 */
public class Plugin2_ implements IQuimpPoint2dFilter {

    private static final Logger LOGGER = LogManager.getLogger(Plugin2_.class.getName());

    private ParamList config;
    private List<Point2d> points;

    /**
     * 
     */
    public Plugin2_() {
        LOGGER.trace("Contructor of Plugin2 called");
        config = new ParamList();
    }

    @Override
    public int setup() {
        LOGGER.trace("setup of Plugin2 called");
        return DOES_SNAKES;
    }

    @Override
    public void setPluginConfig(final ParamList par) throws QuimpPluginException {
        LOGGER.trace("setPluginConfig of Plugin2 called");
        config.putAll(par);

    }

    @Override
    public ParamList getPluginConfig() {
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
    public List<Point2d> runPlugin() throws QuimpPluginException {
        LOGGER.trace("runPlugin of Plugin2 called");
        ArrayList<Point2d> out = new ArrayList<Point2d>();
        for (int i = 0; i < points.size(); i += 4)
            out.add(points.get(i));
        return out;
    }

    @Override
    public void attachData(List<Point2d> data) {
        LOGGER.trace("attachData of Plugin2 called");
        points = data;
    }

}
