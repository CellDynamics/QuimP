/**
 * @file FakeSegmentationPlugin.java
 * @date 28 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Show UI for segmentation from masks and run it.
 * 
 * Modifies provided Nest reference on Apply. Update BOA screen on Apply button
 * 
 * @author p.baniukiewicz
 * @date 28 Jun 2016
 * @see uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder
 */
public class FakeSegmentationPlugin extends QWindowBuilder
        implements ActionListener, IQuimpPluginSynchro, IQuimpCorePlugin {
    private static final Logger LOGGER =
            LogManager.getLogger(FakeSegmentationPlugin.class.getName());

    private Nest nest; //!< reference to Nest object
    private ParamList uiDefinition; //!< window definition
    private int step; //!< discretization step
    private boolean smoothing; //!< use smoothing?
    private ImagePlus maskFile; //!< mask file
    private String maskFilename; //!< mask file name
    private ViewUpdater vu; //!< BOA context for updating it
    private ParamList params; //!< holds current configuration of plugin. Updated on plugin run

    /**
     * Construct object
     * 
     * @see uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder
     */
    public FakeSegmentationPlugin() {
        // defaults
        step = 1;
        smoothing = false;
        maskFilename = "";
        // define window controls
        uiDefinition = new ParamList(); // will hold ui definitions
        uiDefinition.put("name", "FakeSegmentationUI"); // name of window
        uiDefinition.put("Load Mask", "button, Load_mask");
        uiDefinition.put("step", "spinner, 1, 10001, 1," + Integer.toString(step)); // start, end,
                                                                                    // step, default
        uiDefinition.put("smoothing", "checkbox, interpolation," + Boolean.toString(smoothing)); // name,
        // default
        //!<
        uiDefinition.put("help", 
                "Load mask related to stack loaded already in BOA. It should be "
                + "8-bit image of size of original stack with black background and white objects"
                + "\nThe step option stands for discretisation step, 1 means that every pixel of"
                + "shape outline will be mapped to Snake node."
                + "\nSmoothing adds extr Spline interpolation to the shape");
        /**/
        buildWindow(uiDefinition);
        params = new ParamList();
    }

    /**
     * Destroy window on exit
     * 
     * @author p.baniukiewicz
     * @date 22 Apr 2016
     *
     */
    class myWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent we) {
            LOGGER.debug("Window closed");
            pluginWnd.dispose();
        }
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder#buildWindow(uk.ac.warwick.wsbc.QuimP.plugin.ParamList)
     */
    @Override
    public void buildWindow(ParamList def) {
        super.buildWindow(def);
        pluginWnd.addWindowListener(new myWindowAdapter()); // close not hide
        ((JButton) ui.get("Load Mask")).addActionListener(this);
        applyB.addActionListener(this);
    }

    /**
     * Implement UI logic, reaction on buttons
     * 
     * Run segmentation
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        if (b == ui.get("Load Mask")) { // read file with mask
            OpenDialog od = new OpenDialog("Load mask file", "");
            if (od.getPath() != null) // not canceled
                maskFile = IJ.openImage(od.getPath()); // try open image
            if (maskFile == null) {// not loaded
                JOptionPane.showMessageDialog(pluginWnd,
                        "Provided mask file: " + od.getFileName() + " could not be opened",
                        "Mask loading error", JOptionPane.ERROR_MESSAGE);
                maskFilename = "";
            } else
                maskFilename = od.getPath(); // Remember full patch for configuration
        }
        if (b == applyB) { // on apply read config and run
            step = getIntegerFromUI("step");
            smoothing = getBooleanFromUI("smoothing");
            try {
                runPlugin();
            } catch (QuimpPluginException e1) {
                e1.printStackTrace();
            }
            // update config for export, always handle current one
            params = new ParamList(getValues());
            params.setStringValue("maskFilename", maskFilename); // add extra entry to list
        }
    }

    /**
     * Pass ViewUpdater to plugin
     */
    @Override
    public void attachContext(ViewUpdater b) {
        vu = b;
    }

    /**
     * Transfer plugin configuration to QuimP
     * 
     * Only parameters mapped to UI by QWindowBuilder are supported directly by
     * getValues() Any other parameters created outside QWindowBuilder should be
     * added here manually.
     */
    public ParamList getPluginConfig() {
        return params; // return ready list. To avoid problems with fields that are not got from
        // QWindowBuilder, the list is created always on plugin run, not here
    }

    /**
     * Not used here
     * @return
     */
    @Override
    public int setup() {
        return 0;
    }

    /**
     * Not used here
     * @param par
     * @throws QuimpPluginException
     */
    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        // TODO Auto-generated method stub

    }

    @Override
    public void showUI(boolean val) {
        toggleWindow(val);

    }

    @Override
    public String getVersion() {
        return "Not versioned separately";
    }

    @Override
    public String about() {
        return "Convert masks int oSnakes\n" + "Author: Piotr Baniukiewicz\n"
                + "mail: p.baniukiewicz@warwick.ac.uk";
    }

    /**
     * Perform segmentation and modify Nest reference passed to this object
     * 
     * @see uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi
     * @see uk.ac.warwick.wsbc.QuimP.FakeSegmentation.FakeSegmentation(ImagePlus)
     */
    @Override
    public void runPlugin() throws QuimpPluginException {
        if (nest == null) // protection against null input
            return;
        if (maskFile == null) {// failed load
            LOGGER.warn("Load mask file first");
            return;
        }
        FakeSegmentation obj = new FakeSegmentation(maskFile); // create segmentation object
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        // set interpolation params for every tracker. They are used when converting from
        // SegmentedShapeRoi to points in SnakeHandler
        LOGGER.debug("step: " + step + " smooth: " + smoothing);
        for (ArrayList<SegmentedShapeRoi> asS : ret)
            for (SegmentedShapeRoi sS : asS)
                sS.setInterpolationParameters(step, smoothing);
        nest.cleanNest(); // remove old stuff
        nest.addHandlers(ret); // convert from array of SegmentedShapeRoi to SnakeHandlers
        vu.updateView(); // update view
    }

    @Override
    public void attachData(Nest data) {
        this.nest = data;
    }

}
