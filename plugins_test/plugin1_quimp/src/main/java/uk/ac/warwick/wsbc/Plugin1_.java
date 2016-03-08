/**
 * @file Plugin1_.java
 * @date 17 Feb 2016
 */
package uk.ac.warwick.wsbc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.ViewUpdater;
import uk.ac.warwick.wsbc.QuimP.plugin.IPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * This filter moves every \c every node by distance equal to distance between ever-1 and every+1
 * node. Effect is visible if Node Spacing in segmentation is around 2 
 * 
 * To use this plugin one has to modify path with plugins in BOA call.
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 * @date 4 Mar 2016 Plugin does something useful
 *
 */
public class Plugin1_ extends QWindowBuilder
        implements IQuimpPoint2dFilter, IPluginSynchro, ChangeListener, ActionListener {

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
        // configure window, names of UI elements are also names of variables
        // exported/imported by set/getPluginConfig
        uiDefinition.put("name", "testDelete"); // name of window
        uiDefinition.put("every", "spinner, 1, 100, 1," + Integer.toString(every));
        buildWindow(uiDefinition); // construct ui (not shown yet)
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
        LOGGER.trace("showUI of Plugin1 called");
        toggleWindow();
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
        every = getIntegerFromUI("every");
        LOGGER.trace("runPlugin of Plugin1 called wit param every= " + every);
        ArrayList<Point2d> out = new ArrayList<>();
        Vector2d v;
        out.add(points.get(0));
        for (int i = 1; i < points.size() - 1; i++) {
            if (i % every == 0) {
                Vector2d cur = new Vector2d(points.get(i));
                Vector2d curm1 = new Vector2d(points.get(i - 1));
                Vector2d curp1 = new Vector2d(points.get(i + 1));
                v = new Vector2d(curp1.x - curm1.x, curp1.y - curm1.y);
                v = new Vector2d(-v.y, v.x);
                double l = v.length();
                v.normalize();
                v.scale(l); // move current node to distance current-1 - current+1
                cur.add(v);
                out.add(new Point2d(cur));
            } else
                out.add(points.get(i));
        }
        out.add(points.get(points.size() - 1));
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

    @Override
    public void buildWindow(final ParamList def) {
        super.buildWindow(def); // window must be built first

        ((JSpinner) ui.get("every")).addChangeListener(this); // attach listener to selected ui
        applyB.addActionListener(this); // attach listener to apply button
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        LOGGER.trace("stateChanged of Plugin1 called");
        if (isWindowVisible() == true)
            qcontext.updateView();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LOGGER.trace("actionPerformed of Plugin1 called");
        Object b = e.getSource();
        if (b == applyB) { // pressed apply, copy ui data to plugin
            qcontext.updateView(); // transfers data from ui to plugin and plot example on screen
        }
    }

}
