import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.geom.BasicPolygons;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Implementation of HatFilter for removing convexities from polygon
 * 
 * This filter run mask of size \b M along path defined by vertexes on 2D plane.
 * The mask \b M contains smaller inner part called crown \b C. There is always
 * relation that \b C < \b M and \b M and \b C are uneven. For example for \b
 * M=9 and \b C=5 the mask is: \c MMCCCCCMM. For every position \a i of mask \b
 * M on path two distances are calculated: -# the distance \a dM that is total
 * length of path covered by \b M (sum of lengths of vectors between vertexes
 * V(i+1) and V(i) for all i included in \b M -# the distance \a dC that is
 * total length of curve with \b removed points from crown \b C.
 * 
 * For straight line \a dM and \a dC will be equal just because removing some
 * inner points does not change length of path. For strong curvature, if this
 * curvature is matched in position to crown window \b C, those length will
 * differ. The distance calculated without \b C points will be significantly
 * shorter. The ratio defined as: \f[ ratio=1-\frac{\left \|dC\right \|}{\left
 * \|dM\right \|} \f] All points inside window \b M for given \a i that belong
 * to crown \b C are removed if \a ratio for current \a i is bigger than
 * \f$\sigma\f$
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 */
public class HatSnakeFilter_ extends QWindowBuilder
        implements IQuimpPoint2dFilter<Vector2d>, IPadArray, ChangeListener, ActionListener {

    private static final Logger LOGGER = LogManager.getLogger(HatSnakeFilter_.class.getName());
    private final int DRAW_SIZE = 200; //!< size of draw area in window

    private int window; //!< filter's window size
    private int pnum; //!< how many protrusions to remove
    private double alev; //!< minimal acceptance level
    private List<Vector2d> points;
    private ParamList uiDefinition; //!< Definition of UI for this plugin
    private DrawPanel dp; //!< Here we will draw. This panel is plot in place of help field
    private ExPolygon p; //!< representation of snake as polygon
    private ExPolygon pout; //!< output polygon based on \c out
    private List<Vector2d> out; //!< output after filtering
    private JTextArea logArea;
    private int err; //!< general counter of log entries

    /**
     * Construct HatFilter Input array with data is virtually circularly padded
     */
    public HatSnakeFilter_() {
        LOGGER.trace("Entering constructor");
        this.window = 15;
        this.pnum = 1;
        this.alev = 0;
        LOGGER.debug("Set default parameter: window=" + window + " pnum=" + pnum + " alev=" + alev);
        // create UI using QWindowBuilder
        uiDefinition = new ParamList(); // will hold ui definitions
        // configure window, names of UI elements are also names of variables
        // exported/imported by set/getPluginConfig
        uiDefinition.put("name", "HatFilter"); // name of window
        uiDefinition.put("window", "spinner, 3, 51, 2," + Integer.toString(window));
        uiDefinition.put("pnum", "spinner, 1, 6, 1," + Integer.toString(pnum));
        uiDefinition.put("alev", "spinner, 0, 1,0.01," + Double.toString(alev));
        buildWindow(uiDefinition); // construct ui (not shown yet)
        points = null; // not attached yet
        pout = null; // not calculated yet
        err = 1;
    }

    /**
     * Attach data to process.
     * 
     * Data are as list of vectors defining points of polygon. Passed points
     * should be sorted according to a clockwise or anti-clockwise direction
     * 
     * @param data Polygon points
     * @see plugin.snakes.IQuimpPoint2dFilter.attachData(List<E>)
     * @warning \c data can be \c null here.
     */
    @Override
    public void attachData(List<Vector2d> data) {
        LOGGER.trace("Entering attachData");
        points = data;
        pout = null; // delete any processed polygon
        if (points == null) {// TODO may not be necessary if ExPolygon survives nulls
            LOGGER.warn("No data attached");
            return;
        }
        p = new ExPolygon(data); // create polygon from points
        p.fitPolygon(DRAW_SIZE); // adjust its size to draw window

    }

    /**
     * Main filter runner
     * 
     * This version assumes that user clicked Apply button to populate data from
     * UI to plugin or any other ui element.
     * 
     * @return Processed \a input list, size of output list may be different
     * than input. Empty output is also allowed.
     * @see HatSnakeFilter_.actionPerformed(ActionEvent)
     * @see HatSnakeFilter_.stateChanged(ChangeEvent)
     * @remarks User can expect that \c points will be always valid but they optionally may have
     * 0 length.
     */
    @Override
    public List<Vector2d> runPlugin() throws QuimpPluginException {
        // internal parameters are not updated here but when user click apply
        LOGGER.debug(String.format("Run plugin with params: window %d, pnum %d, alev %f", window,
                pnum, alev));

        int cp = window / 2; // left and right range of window
        List<Vector2d> out = new ArrayList<Vector2d>(); // output table for plotting temporary
                                                        // results of filter
        // check input conditions
        if (window % 2 == 0 || window < 0)
            throw new QuimpPluginException("Window must be uneven, positive and larger than 0");
        if (window >= points.size())
            throw new QuimpPluginException("Processing window to long");
        if (window < 3)
            throw new QuimpPluginException("Window should be larger than 2");
        if (pnum <= 0)
            throw new QuimpPluginException("Number of protrusions should be larger than 0");
        if (alev < 0)
            throw new QuimpPluginException("Acceptacne level should be positive");

        WindowIndRange indexTest = new WindowIndRange(); // temporary variable for keeping index
                                                         // that is tested to be or not in range
                                                         // winpos

        // Step 1 - Build circularity table
        ArrayList<Double> circ = new ArrayList<Double>(); // array to store circularity for window
                                                          // positions. Index is related to window
                                                          // position (negative shift in rotate)
        double tmpCirc;
        for (int r = 0; r < points.size(); r++) {
            LOGGER.debug("points: " + points.toString());
            // get all points except window. Window has constant position 0 - (window-1)
            List<Vector2d> sub = points.subList(window, points.size());
            LOGGER.debug("sub: " + sub.toString());
            tmpCirc = getCircularity(sub);
            LOGGER.debug("circ " + tmpCirc);
            // calculate weighting for circularity
            List<Vector2d> w = points.subList(0, window); // get window position
            LOGGER.debug("win: " + w.toString());
            tmpCirc /= getWeighting(w); // calculate weighting for window content
            circ.add(tmpCirc); // store weighted circularity for shape without window
            // move window to next position
            Collections.rotate(points, -1); // rotates by -1 what means that on first n positions
                                            // of points there are different values simulate window
                                            // first iter 0 1 2 3 4 5... (w=[0 1 2])
                                            // second itr 1 2 3 4 5 0... (w=[1 2 3])
                                            // last itera 5 0 1 2 3 4... (w=[5 0 1])

        }

        // Step 2 - Check criterion for all windows
        TreeSet<WindowIndRange> ind2rem = new TreeSet<>(); // <l;u> range of indexes to remove
        ArrayList<Double> circsorted = new ArrayList<>(circ); // need sorted but the old one as well
                                                              // to identify windows positions
        circsorted.sort(Collections.reverseOrder()); // sort in descending order
        LOGGER.debug("cirs: " + circsorted.toString());
        LOGGER.debug("circ: " + circ.toString());

        if (circsorted.get(0) < alev) // if maximal circularity smaller than acceptance level
            return points; // just return non-modified data;
        int found = 0; // how many protrusions we have found already
        int i = 0; // current index in circsorted - number of window to analyze. Real position of
                   // window on data can be retrieved by finding value from sorted array circularity
                   // in non sorted, where order of data is related to window position starting
                   // from 0 for most left point of window
        while (found < pnum) { // do as long as we find pnum protrusions (or to end of candidates)
            if (i >= circsorted.size()) { // no more data to check, probably we have less prot. pnum
                throw new QuimpPluginException("Can find next candidate. Use smaller window");
            }
            if (found > 0) {
                // find where it was before sorting and store in window positions
                int startpos = circ.indexOf(circsorted.get(i));
                // check if we have this index in indexes to remove
                indexTest.setRange(startpos, startpos + window - 1);
                if (!ind2rem.contains(indexTest)) {// index isnt in already collected indexes to rm
                    // store range of indexes that belongs to window
                    ind2rem.add(new WindowIndRange(startpos, startpos + window - 1));
                    found++;
                } else // search next candidate in sorted circularities
                    i++;
            } else { // first candidate always accepted
                // find where it was before sorting and store in window positions
                int startpos = circ.indexOf(circsorted.get(i));
                // store range of indexes that belongs to window
                ind2rem.add(new WindowIndRange(startpos, startpos + window - 1));
                i++;
                found++;
            }
        }
        LOGGER.debug("winpos: " + ind2rem.toString());

        // Step 3 - remove selected windows from input data
        // array will be copied to new one skipping points to remove
        for (i = 0; i < points.size(); i++) {
            indexTest.setSame(i); // set upper and lower index to the same value - allows to test
                                  // particular index for its presence in any defined range
            if (!ind2rem.contains(indexTest)) // check if any window position (l and u bound)
                out.add(new Vector2d(points.get(i))); // include tested point. Copy it to new array
                                                      // if not
        }

        return out;
    }

    /**
     * Calculate circularity of polygon
     * 
     * @param p Polygon vertices
     * @return circularity
     */
    private double getCircularity(final List<Vector2d> p) {
        double area;
        double perim;
        BasicPolygons<Vector2d> b = new BasicPolygons<>();
        area = b.getPolyArea(p);
        perim = b.getPolyPerim(p);

        return ((4 * Math.PI * area) / (perim * perim));
    }

    /**
     * Calculates weighting based on distribution of window points
     * 
     * Calculates lengths of vectors between ever point \c n and middle point \c nm in list \c p.
     * Then those lengths are averaged
     *  
     * @param p Polygon vertices
     * @return Weight
     */
    private double getWeighting(final List<Vector2d> p) {
        double len = 0;

        Vector2d middle = p.get(p.size() / 2 + 1); // middle point of window
        for (Vector2d v : p) {
            Vector2d vec = new Vector2d(middle); // vector between px and middle
            vec.sub(v);
            len += vec.length();
        }

        LOGGER.debug("w " + len / p.size());
        return len / p.size();
    }

    /**
     * Get length of vector v = v1-v2
     * 
     * Avoid creating new Vector2d object when using build-in Vector2d::sub
     * method method
     * 
     * @param v1 Vector
     * @param v2 Vector
     * @return ||v1-v2||
     */
    private double getLen(Vector2d v1, Vector2d v2) {
        double dx;
        double dy;
        dx = v1.x - v2.x;
        dy = v1.y - v2.y;

        return Math.sqrt(dx * dx + dy * dy);
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
        return DOES_SNAKES + CHANGE_SIZE;
    }

    /**
     * Configure plugin and overrides default values.
     * 
     * Supported keys:
     * <ol>
     * <li>\c window - size of main window
     * <li>\c crown - size of inner window
     * <li>\c sigma - cut-off value (see class description)
     * </ol>
     * 
     * @param par configuration as pairs <key,val>. Keys are defined by plugin
     * creator and plugin caller do not modify them.
     * @throws QuimpPluginException on wrong parameters list or wrong parameter
     * conversion
     * @see wsbc.plugin.IQuimpPlugin.setPluginConfig(HashMap<String, String>)
     */
    @Override
    public void setPluginConfig(final ParamList par) throws QuimpPluginException {
        try {
            window = par.getIntValue("window");
            pnum = par.getIntValue("pnum");
            alev = par.getDoubleValue("alev");
            setValues(par); // copy incoming parameters to UI
        } catch (Exception e) {
            // we should never hit this exception as parameters are not touched by caller they are
            // only passed to configuration saver and restored from it
            throw new QuimpPluginException("Wrong input argument->" + e.getMessage(), e);
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
    public ParamList getPluginConfig() {
        return getValues();
    }

    @Override
    public void showUI(boolean val) {
        LOGGER.debug("Got message to show UI");
        if (toggleWindow() == true)
            recalculatePlugin();
    }

    /**
     * Plugin is not versioned.
     */
    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Override of uk.ac.warwick.wsbc.plugin.utils.QWindowBuilder.BuildWindow()
     * 
     * The aim is to: -# attach listeners for spinners ro preventing even
     * numbers -# attach listener for build-in apply button -# add draw field
     * DrawPanel
     */
    @Override
    public void buildWindow(final ParamList def) {
        super.buildWindow(def); // window must be built first

        // attach listeners to ui to update window on new parameters
        ((JSpinner) ui.get("Window")).addChangeListener(this); // attach listener to selected ui
        ((JSpinner) ui.get("pnum")).addChangeListener(this); // attach listener to selected ui
        ((JSpinner) ui.get("alev")).addChangeListener(this); // attach listener to selected ui
        applyB.addActionListener(this); // attach listener to aplly button
        // in place of CENTER pane in BorderLayout layout from super.BuildWindow
        // we create few extra controls
        GridLayout gc = new GridLayout(2, 1, 5, 5);
        Panel jp = new Panel(); // panel in CENTER pane
        jp.setLayout(gc);

        dp = new DrawPanel(); // create drawable JFrame
        jp.add(dp);

        Panel jp1 = new Panel(); // subpanel for two text fields
        jp1.setLayout(gc);

        JTextArea helpArea = new JTextArea(); // default size of text area
        JScrollPane helpPanel = new JScrollPane(helpArea);
        helpArea.setEditable(false);
        helpArea.setText("About plugin\nPlugin window does not update when new cell"
                + " is selected. Please click APPLY to update");
        helpArea.setLineWrap(true); // with wrapping

        logArea = new JTextArea(); // default size of text area
        JScrollPane logPanel = new JScrollPane(logArea);
        logArea.setEditable(false);
        logArea.setText("Log:\n"); // set help text
        logArea.setLineWrap(true); // with wrapping

        jp1.add(helpPanel);
        jp1.add(logPanel);
        jp.add(jp1);

        pluginPanel.add(jp, BorderLayout.CENTER); // add in center position (in place of help zone)
        pluginWnd.pack();
    }

    /**
     * React on spinners changes.
     * 
     * Here used for updating view but it can be used for example for
     * auto-fixing even values provided by user:
     * 
     * @code{.java}
     * Object source = ce.getSource();
     * JSpinner s = (JSpinner)ui.get("window"); // get ui element
     * JSpinner s1 = (JSpinner)ui.get("crown"); // get ui element
     * if(source == s) { // check if this event concerns it
     * logger.debug("Spinner window used");
     * if(((Double)s.getValue()).intValue()%2==0 )
     * s.setValue((Double)s.getValue() + 1);
     * }
     * if(source == s1) { // check if this event concerns it
     * logger.debug("Spinner crown used");
     * if(((Double)s1.getValue()).intValue()%2==0 )
     * s1.setValue((Double)s1.getValue() + 1);
     * }
     * @endcode
     * 
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        if (isWindowVisible() == true) // prevent applying default values before setPluginConfig is
                                       // used because method is called on window creation
            recalculatePlugin();
    }

    /**
     * React on \b Apply button.
     * 
     * Here \b Apply button copies window content into plugin structures. This
     * is different approach than in LoessFilter and MeanFilter where window
     * content was copied while runPlugin() command
     * 
     * This button run plugin and creates preview of filtered data
     * 
     * @see uk.ac.warwick.wsbc.tools.images.filters.LoessFilter.runPlugin()
     * @see uk.ac.warwick.wsbc.tools.images.filters.MeanFilter.runPlugin()
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        if (b == applyB) { // pressed apply, copy ui data to plugin
            recalculatePlugin();
        }
    }

    /**
     * Recalculate plugin on every change of its parameter.
     * 
     * Used only for previewing. Repaint window as well
     */
    private void recalculatePlugin() {
        // check if we have correct data
        if (points == null) {
            LOGGER.warn("No data attached");
            return;
        }
        // transfer data from ui
        window = getIntegerFromUI("window");
        pnum = getIntegerFromUI("pnum");
        alev = getDoubleFromUI("alev");
        LOGGER.debug(
                String.format("Updated from UI: window %d, pnum %d, alev %f", window, pnum, alev));
        // run plugin for set parameters
        try {
            out = runPlugin(); // may throw exception if no data attached
            pout = new ExPolygon(out); // create new figure from out data
            pout.fitPolygon(DRAW_SIZE, p.initbounds, p.scale); // fit to size from original polygon,
                                                               // modified one will be centered to
                                                               // original one
            dp.repaint(); // repaint window
        } catch (QuimpPluginException e1) { // ignore exception in general
            LOGGER.error(e1);
            logArea.append("#" + err + ": " + e1.getMessage() + '\n');
            err++;
        }
    }

    /**
     * Class for plotting in center part of plugin derived from QWindowBuilder
     * 
     * @author p.baniukiewicz
     * @date 8 Feb 2016
     *
     */
    class DrawPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        DrawPanel() {
            setPreferredSize(new Dimension(DRAW_SIZE, DRAW_SIZE));
        }

        /**
         * Main plotting function.
         * 
         * Plots two polygons, original and processed in red
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, DRAW_SIZE, DRAW_SIZE);
            g.setColor(Color.WHITE);
            if (points != null) {
                g.drawPolygon(p);
            }
            if (pout != null) { // pout is initialized after first apply click
                g.setColor(Color.RED);
                g.drawPolygon(pout); // draw output polygon (processed)
            }
        }
    }
}

/**
 * Helper class supporting scaling and fitting polygon to DrawWindow
 * 
 * This class is strictly TreeSet related. \c equals method does not assure correct comparison
 * @author p.baniukiewicz
 * @date 8 Feb 2016
 *
 */
class ExPolygon extends Polygon {
    private static final Logger logger = LogManager.getLogger(ExPolygon.class.getName());
    private static final long serialVersionUID = 5870934217878285135L;
    public Rectangle initbounds; // initial size of polygon, before scaling
    public double scale; // current scale

    /**
     * Construct polygon from list of points.
     * 
     * @param data List of points
     */
    public ExPolygon(List<Vector2d> data) {
        // convert to polygon
        for (Vector2d v : data)
            addPoint((int) Math.round(v.getX()), (int) Math.round(v.getY()));
        initbounds = new Rectangle(getBounds()); // remember original size
        scale = 1;
    }

    /**
     * Scale polygon to fit in rectangular window of \c size
     * 
     * Method changes internal polygon representation. Fitting is done basing on
     * bounding box area.
     * 
     * @param size Size of window to fit polygon
     */
    public void fitPolygon(double size) {
        // set in 0,0
        translate((int) Math.round(-initbounds.getCenterX()),
                (int) Math.round(-initbounds.getCenterY()));
        // get size of bounding box
        Rectangle2D bounds = getBounds2D();
        // set scale according to window size
        if (bounds.getWidth() > bounds.getHeight())
            scale = bounds.getWidth();
        else
            scale = bounds.getHeight();
        scale = size / scale;
        scale *= 0.95; // little smaller than window
        for (int i = 0; i < npoints; i++) {
            xpoints[i] = (int) Math.round(xpoints[i] * scale);
            ypoints[i] = (int) Math.round(ypoints[i] * scale);
        }
        // center in window
        logger.debug("Scale is: " + scale + " BoundsCenters: " + bounds.getCenterX() + " "
                + bounds.getCenterY());
        translate((int) Math.round(bounds.getCenterX()) + (int) (size / 2),
                (int) Math.round(bounds.getCenterY()) + (int) (size / 2));
    }

    /**
     * Scale polygon to fit in rectangular window of \c size using pre-computed
     * bounding box and scale
     * 
     * Use for setting next polygon on base of previous, when next has different
     * shape but must be centered with previous one.
     * 
     * @param size Size of window to fit polygon
     * @param init Bounding box to fit new polygon
     * @param scale Scale of new polygon
     */
    public void fitPolygon(double size, Rectangle2D init, double scale) {
        // set in 0,0
        this.scale = scale;
        logger.debug("fitPolygon: Scale is: " + scale + " BoundsCenters: " + init.getCenterX() + " "
                + init.getCenterY());
        translate((int) Math.round(-init.getCenterX()), (int) Math.round(-init.getCenterY()));

        for (int i = 0; i < npoints; i++) {
            xpoints[i] = (int) Math.round(xpoints[i] * scale);
            ypoints[i] = (int) Math.round(ypoints[i] * scale);
        }
        translate((int) (size / 2), (int) (size / 2));
    }
}

/**
 * Class holding lower and upper index of window. supports comparisons.
 * 
 * @author p.baniukiewicz
 * @date 1 Mar 2016
 * @see WindowIndRange.compareTo(Object)
 */
class WindowIndRange implements Comparable<Object> {
    public int l, u;

    public WindowIndRange() {
        u = 0;
        l = 0;
    }

    /**
     * Create pair of indexes that define window
     * @param l lower index
     * @param u upper index
     */
    WindowIndRange(int l, int u) {
        setRange(l, u);
    }

    @Override
    public String toString() {
        return "{" + l + "," + u + "}";
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + l;
        result = 31 * result + u;
        return result;
    }

    /**
     * Compare two WindowIndRange objects.
     * @param obj
     * @return \c true only if ranges does not overlap
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final WindowIndRange other = (WindowIndRange) obj;
        if (u < other.l)
            return true;
        else if (l > other.u)
            return true;
        else
            return false;

    }

    /**
     * Compare two WindowIndRange objects.
     * 
     * The following rules of comparison are used:
     * -# If range1 is below range2 they are not equal
     * -# If range1 is above range2 they are not equal
     * -# They are qual in all other cases:
     *  -# They are sticked
     *  -# One includes other
     *  -# They overlap
     *  
     * @param obj Object to compare to \c this 
     * @return -1,0,1 expressing relations in windows positions
     */
    @Override
    public int compareTo(Object obj) {
        final WindowIndRange other = (WindowIndRange) obj;
        if (this == obj)
            return 0;

        if (u < other.l)
            return -1;
        else if (l > other.u)
            return 1;
        else
            return 0;
    }

    /**
     * Sets upper and lower indexes to the same value
     * 
     * @param i Value to set for \c u and \c l
     */
    public void setSame(int i) {
        l = i;
        u = i;
    }

    /**
     * Set pair of indexes that define window
     * 
     * @param l lower index
     * @param u upper index
     */
    public void setRange(int l, int u) {
        if (l > u) {
            this.l = u;
            this.u = l;
        } else {
            this.l = l;
            this.u = u;
        }
    }

}
