package uk.ac.warwick.wsbc.quimp.plugin.binaryseg;

import java.awt.Choice;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;
import uk.ac.warwick.wsbc.quimp.BOA_;
import uk.ac.warwick.wsbc.quimp.Nest;
import uk.ac.warwick.wsbc.quimp.ViewUpdater;
import uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpNestPlugin;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder;

/**
 * Show UI for segmentation from masks and run it.
 * 
 * <p>Modifies provided Nest reference on Apply. Update BOA screen on Apply button
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder
 */
public class BinarySegmentationPlugin extends QWindowBuilder
        implements ActionListener, IQuimpPluginSynchro, IQuimpNestPlugin, ItemListener {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentationPlugin.class.getName());

  private Nest nest; // reference to Nest object
  private ParamList uiDefinition; // window definition
  private int step; // discretization step
  private boolean smoothing; // use smoothing?
  private ImagePlus maskFile; // mask file
  private String maskFilename; // mask file name
  private ViewUpdater vu; // BOA context for updating it
  private ParamList params; // holds current configuration of plugin. Updated on plugin run

  /**
   * Construct object
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder
   */
  public BinarySegmentationPlugin() {
    // defaults
    step = 1;
    smoothing = false;
    maskFilename = "";
    // define window controls (selecter filled in buildWindow
    uiDefinition = new ParamList(); // will hold ui definitions
    uiDefinition.put("name", "BinarySegmentation"); // name of window
    uiDefinition.put("load mask", "button, Load_mask");
    uiDefinition.put("get opened", "choice," + BOA_.NONE);
    // start, end, step, default
    uiDefinition.put("step", "spinner, 1, 10001, 1," + Integer.toString(step));
    // name
    uiDefinition.put("smoothing", "checkbox, interpolation," + Boolean.toString(smoothing));
    // use http://www.freeformatter.com/java-dotnet-escape.html#ad-output for escaping
    //!>
    uiDefinition.put("help", "<font size=\"3\"><p><strong>Load Mask</strong> - Load mask file. "
            + "It should be 8-bit image of size of original stack with <span style=\"color:"
            + " #ffffff; background-color: #000000;\">black background</span> and white"
            + " objects." + "</p>\r\n<p><strong>Get Opened</strong> - Select mask already opened in"
            + " ImageJ."
            + " Alternative to <em>Load Mask</em>, will override loaded file.</p>\r\n<p>"
            + "<strong>step</strong> - stand for discretisation density, 1.0 means that every"
            + " pixel of the outline will be mapped to Snake node.</p>\r\n<p><strong>smoothing"
            + "</strong>&nbsp;- add extra Spline interpolation to the shape</p></font>");
    //!<
    buildWindow(uiDefinition);
    params = new ParamList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder#buildWindow(uk.ac.warwick.wsbc.quimp.
   * plugin.ParamList)
   */
  @Override
  public void buildWindow(ParamList def) {
    super.buildWindow(def);
    // add preffered size to this window
    pluginWnd.setPreferredSize(new Dimension(300, 450));
    pluginWnd.pack();
    pluginWnd.setVisible(true);
    // Destroy window on exit
    pluginWnd.addWindowListener(new WindowAdapter() {
      /*
       * (non-Javadoc)
       * 
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(WindowEvent we) {
        LOGGER.debug("Window closed");
        pluginWnd.dispose();
      }
    }); // close not hide
    // update selector
    pluginWnd.addWindowFocusListener(new WindowFocusListener() {
      private Choice getImage = (Choice) ui.get("get opened");
      private String lastSelected = "";

      @Override
      public void windowLostFocus(WindowEvent e) {
        lastSelected = getImage.getSelectedItem(); // remember on defocus. Will be restored on focus
      }

      @Override
      public void windowGainedFocus(WindowEvent e) {
        String[] str = WindowManager.getImageTitles(); // get opened windows
        getImage.removeAll();
        getImage.add(BOA_.NONE);
        for (String s : str) {
          getImage.add(s);
        }
        getImage.select(lastSelected); // restore previous. If not available already, 0 position is
        // selected
      }
    });
    ((JButton) ui.get("load mask")).addActionListener(this);
    ((Choice) ui.get("get opened")).addItemListener(this);
    applyB.addActionListener(this);
  }

  /**
   * Implement UI logic, reaction on buttons.
   * 
   * <p>Run segmentation. Favour mask selected by Choice over button
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object b = e.getSource();
    String selectedMask = "";
    if (b == ui.get("load mask")) { // read file with mask
      OpenDialog od = new OpenDialog("Load mask file", "");
      if (od.getPath() != null) { // not canceled
        maskFile = IJ.openImage(od.getPath()); // try open image
        selectedMask = od.getFileName();
      }
    }
    // here verify whether mask is ok
    if (maskFile == null) { // not loaded
      JOptionPane.showMessageDialog(pluginWnd,
              "Provided mask file: " + selectedMask + " could not be opened", "Mask loading error",
              JOptionPane.ERROR_MESSAGE);
      maskFilename = "";
    } else {
      maskFilename = selectedMask; // Remember full patch for configuration
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
   * Pass ViewUpdater to plugin.
   */
  @Override
  public void attachContext(ViewUpdater b) {
    vu = b;
  }

  /**
   * Transfer plugin configuration to QuimP.
   * 
   * <p>Only parameters mapped to UI by QWindowBuilder are supported directly by getValues() Any
   * other parameters created outside QWindowBuilder should be added here manually.
   */
  public ParamList getPluginConfig() {
    return params; // return ready list. To avoid problems with fields that are not got from
    // QWindowBuilder, the list is created always on plugin run, not here
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin#setup()
   */
  @Override
  public int setup() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin#setPluginConfig(uk.ac.warwick.wsbc.quimp.
   * plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin#showUI(boolean)
   */
  @Override
  public int showUi(boolean val) {
    return toggleWindow(val) ? 1 : 0;

  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin#getVersion()
   */
  @Override
  public String getVersion() {
    return "Not versioned separately";
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin#about()
   */
  @Override
  public String about() {
    return "Convert binary masks into Snakes\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }

  /**
   * Perform segmentation and modify Nest reference passed to this object.
   * 
   * @see uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi
   * @see <a href=
   *      "link">uk.ac.warwick.wsbc.quimp.BinarySegmentation.BinarySegmentation(ImagePlus)</a>
   */
  @Override
  public void runPlugin() throws QuimpPluginException {
    if (nest == null) {
      return;
    }
    if (maskFile == null) { // failed load
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
      for (ArrayList<SegmentedShapeRoi> asS : ret) {
        for (SegmentedShapeRoi ss : asS) {
          ss.setInterpolationParameters(step, smoothing);
        }
      }
      nest.cleanNest(); // remove old stuff
      nest.addHandlers(ret); // convert from array of SegmentedShapeRoi to SnakeHandlers
      vu.updateView(); // update view
    } catch (IllegalArgumentException e) { // thrown by BinarySegmentation
      JOptionPane.showMessageDialog(pluginWnd, "Error during execution: " + e.getMessage(),
              "Processing error", JOptionPane.ERROR_MESSAGE);
      LOGGER.error(e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpNestPlugin#attachData(uk.ac.warwick.wsbc.quimp.Nest)
   */
  @Override
  public void attachData(Nest data) {
    this.nest = data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    String selectedMask;
    if (e.getItemSelectable() == ui.get("get opened")) { // import opened image
      selectedMask = ((Choice) ui.get("get opened")).getSelectedItem(); // selected item
      if (!selectedMask.equals(BOA_.NONE)) {
        maskFile = WindowManager.getImage(selectedMask);
      } else {
        maskFile = null;
      }
      LOGGER.debug("Choice action - mask: " + maskFile != null ? maskFile.toString() : "null");
    }
  }

}
