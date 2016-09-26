/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Choice;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpNestPlugin;
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
 * @see uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder
 */
public class BinarySegmentationPlugin extends QWindowBuilder
        implements ActionListener, IQuimpPluginSynchro, IQuimpNestPlugin, ItemListener {
    private static final Logger LOGGER =
            LogManager.getLogger(BinarySegmentationPlugin.class.getName());

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
    public BinarySegmentationPlugin() {
        // defaults
        step = 1;
        smoothing = false;
        maskFilename = "";
        // define window controls
        String str[] = WindowManager.getImageTitles(); // get opened windows
        String list = BOA_.NONE; // default nonselected
        for (String s : str)
            list = list + ',' + s; // form list of params for QWindowBuilder:Choice
        uiDefinition = new ParamList(); // will hold ui definitions
        uiDefinition.put("name", "BinarySegmentation"); // name of window
        uiDefinition.put("load mask", "button, Load_mask");
        uiDefinition.put("get opened", "choice," + list);
        uiDefinition.put("step", "spinner, 1, 10001, 1," + Integer.toString(step)); // start, end,
                                                                                    // step, default
        uiDefinition.put("smoothing", "checkbox, interpolation," + Boolean.toString(smoothing)); // name,
        // use http://www.freeformatter.com/java-dotnet-escape.html#ad-output for escaping
        //!<
        uiDefinition.put("help","<font size=\"3\"><p><strong>Load Mask</strong> - Load mask file. It should be 8-bit image of size of original stack with <span style=\"color: #ffffff; background-color: #000000;\">black background</span> and white objects.</p>\r\n<p><strong>Get Opened</strong> - Select mask already opened in ImageJ. Alternative to <em>Load Mask</em>, will override loaded file.</p>\r\n<p><strong>step</strong> - stand for discretisation density, 1.0 means that every pixel of the outline will be mapped to Snake node.</p>\r\n<p><strong>smoothing</strong>&nbsp;- add extra Spline interpolation to the shape</p></font>");
        /**/
        buildWindow(uiDefinition);
        params = new ParamList();
    }

    /**
     * Destroy window on exit
     * 
     * @author p.baniukiewicz
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
        // add preffered size to this window
        pluginWnd.setPreferredSize(new Dimension(300, 450));
        pluginWnd.pack();
        pluginWnd.setVisible(true);
        pluginWnd.addWindowListener(new myWindowAdapter()); // close not hide
        ((JButton) ui.get("load mask")).addActionListener(this);
        ((Choice) ui.get("get opened")).addItemListener(this);
        applyB.addActionListener(this);
    }

    /**
     * Implement UI logic, reaction on buttons
     * 
     * Run segmentation. Favor mask selected by Choice over button
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        String selectedMask = "";
        if (b == ui.get("load mask")) { // read file with mask
            OpenDialog od = new OpenDialog("Load mask file", "");
            if (od.getPath() != null) {// not canceled
                maskFile = IJ.openImage(od.getPath()); // try open image
                selectedMask = od.getFileName();
            }
        }
        // here verify whether mask is ok
        if (maskFile == null) {// not loaded
            JOptionPane.showMessageDialog(pluginWnd,
                    "Provided mask file: " + selectedMask + " could not be opened",
                    "Mask loading error", JOptionPane.ERROR_MESSAGE);
            maskFilename = "";
        } else
            maskFilename = selectedMask; // Remember full patch for configuration

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
     * @see uk.ac.warwick.wsbc.QuimP.BinarySegmentation.BinarySegmentation(ImagePlus)
     */
    @Override
    public void runPlugin() throws QuimpPluginException {
        if (nest == null) // protection against null input
            return;
        if (maskFile == null) {// failed load
            LOGGER.warn("Load mask file first");
            return;
        }
        try {
            LOGGER.info("Segmentation: " + maskFile.toString() + " params: " + params.toString());
            BinarySegmentation obj = new BinarySegmentation(maskFile); // create segmentation object
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
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(pluginWnd, "Error during execution: " + e.getMessage(),
                    "Processing error", JOptionPane.ERROR_MESSAGE);
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void attachData(Nest data) {
        this.nest = data;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        String selectedMask;
        if (e.getItemSelectable() == ui.get("get opened")) { // import opened image
            selectedMask = ((Choice) ui.get("get opened")).getSelectedItem(); // selected item
            if (!selectedMask.equals(BOA_.NONE)) // process only if NOT NONE
                maskFile = WindowManager.getImage(selectedMask);
            else
                maskFile = null;
            LOGGER.debug(
                    "Choice action - mask: " + maskFile != null ? maskFile.toString() : "null");
        }
    }

}
