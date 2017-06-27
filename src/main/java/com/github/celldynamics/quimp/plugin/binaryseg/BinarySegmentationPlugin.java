package com.github.celldynamics.quimp.plugin.binaryseg;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.Constrictor;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.SnakeHandler;
import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.IQuimpNestPlugin;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;

/**
 * Show UI for segmentation from masks and run it.
 * 
 * <p>Modifies provided Nest reference on Apply. Update BOA screen on Apply button
 * 
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.plugin.utils.QWindowBuilder
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
  private boolean clearnest; // clear nest before adding next outline
  private boolean restoreFields; // if true internal Snake fields will be restored
  private ImagePlus maskFile; // mask file
  private String maskFilename; // mask file name
  private ViewUpdater vu; // BOA context for updating it
  private ParamList params; // holds current configuration of plugin. Updated on plugin run
  private ImagePlus ip;

  /**
   * Construct object
   * 
   * @see com.github.celldynamics.quimp.plugin.utils.QWindowBuilder
   */
  public BinarySegmentationPlugin() {
    // defaults
    step = 1;
    smoothing = false;
    clearnest = true;
    restoreFields = true;
    maskFilename = "";
    // define window controls (selecter filled in buildWindow
    uiDefinition = new ParamList(); // will hold ui definitions
    uiDefinition.put("name", "BinarySegmentation"); // name of window
    uiDefinition.put("load mask", "button: Load mask");
    uiDefinition.put("get opened", "choice:" + BOA_.NONE);
    // start, end, step, default
    uiDefinition.put("step", "spinner: 1: 10001: 1:" + Integer.toString(step));
    // name
    uiDefinition.put("smoothing", "checkbox: interpolation:" + Boolean.toString(smoothing));
    // clear nest
    uiDefinition.put("Clear nest", "checkbox: clear:" + Boolean.toString(clearnest));
    // restore
    uiDefinition.put("Restore Snake", "checkbox: restore:" + Boolean.toString(restoreFields));
    // use http://www.freeformatter.com/java-dotnet-escape.html#ad-output for escaping
    //!>
    uiDefinition.put("help", "<font size=\"3\"><p><strong>Load Mask</strong> - Load mask file. "
            + "It should be 8-bit image of size of original stack with <span style=\"color:"
            + " #ffffff; background-color: #000000;\">black background</span> and white"
            + " objects." + "</p>\r\n<p><strong>Get Opened</strong> - Select mask already opened in"
            + " ImageJ."
            + " Alternative to <em>Load Mask</em>, will override loaded file.</p>\r\n<p>"
            + "<strong>step</strong> - stand for discretisation density, 1.0 means that every"
            + " pixel of the outline will be mapped to Snake node.</p>"
            + "\r\n<p><strong>smoothing</strong>&nbsp;"
            + "- add extra Spline interpolation to the shape</p>"
            + "\r\n<p><strong>Clear nest</strong>&nbsp;"
            + "- Delete all other snakes from view. If disabled, each use of <i>Apply</i> "
            + "will create new snake "
            + "\r\n<p><strong>Restore Snake</strong>&nbsp;"
            + "- Try to compute some internal data stored in Snake which are ususally obtained"
            + " if regular Active Contour method is used. Current AC options are used."
            + "</p></font>");
    //!<
    buildWindow(uiDefinition);
    params = new ParamList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see QWindowBuilder#buildWindow(com.github.celldynamics.quimp.
   * plugin.ParamList)
   */
  @Override
  public void buildWindow(ParamList def) {
    super.buildWindow(def);
    // add preffered size to this window
    pluginWnd.setPreferredSize(new Dimension(450, 450));
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
        getImage.add(BOA_.NONE); // add default position
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
    if (b == ui.get("load mask")) { // read file with mask
      OpenDialog od = new OpenDialog("Load mask file", "");
      if (od.getPath() != null) { // not canceled
        maskFile = IJ.openImage(od.getPath()); // try open image
        maskFilename = od.getFileName();
      }
    }
    // maskFilename can be null but it is handled by BinarySegmentation
    if (b == applyB) { // on apply read config and run
      step = getIntegerFromUI("step");
      smoothing = getBooleanFromUI("smoothing");
      clearnest = getBooleanFromUI("clear nest");
      restoreFields = getBooleanFromUI("Restore Snake");
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
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#setup()
   */
  @Override
  public int setup() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IQuimpCorePlugin#setPluginConfig(com.github.celldynamics.quimp.plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#showUI(boolean)
   */
  @Override
  public int showUi(boolean val) {
    return toggleWindow(val) ? 1 : 0;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#getVersion()
   */
  @Override
  public String getVersion() {
    return "Not versioned separately";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#about()
   */
  @Override
  public String about() {
    return "Convert binary masks into Snakes\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }

  /**
   * Perform segmentation and modify Nest reference passed to this object.
   * 
   * @see com.github.celldynamics.quimp.geom.SegmentedShapeRoi
   * @see <a href=
   *      "link">com.github.celldynamics.quimp.BinarySegmentation.BinarySegmentation(ImagePlus)</a>
   */
  @Override
  public void runPlugin() throws QuimpPluginException {
    if (nest == null) {
      return;
    }
    try {
      LOGGER.info("Segmentation: " + (maskFile != null ? maskFile.toString() : "null") + " params: "
              + params.toString());
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
      if (clearnest) {
        nest.cleanNest(); // remove old stuff
      }
      nest.addHandlers(ret); // convert from array of SegmentedShapeRoi to SnakeHandlers

      if (restoreFields) {
        Constrictor constrictor = new Constrictor();
        for (SnakeHandler sh : nest.getHandlers()) {
          for (int f = sh.getStartFrame(); f <= sh.getEndFrame(); f++) {
            sh.getBackupSnake(f).calcCentroid(); // actually this is calculated in Snake constr.
            sh.getBackupSnake(f).setPositions(); // actually this is calculated in Snake constr.
            sh.getBackupSnake(f).updateNormales(true); // calculated in Snake constr. but for other
            sh.getBackupSnake(f).getBounds(); // actually this is calculated in Snake constr.

            sh.getStoredSnake(f).calcCentroid();
            sh.getStoredSnake(f).setPositions();
            sh.getStoredSnake(f).updateNormales(true);
            sh.getStoredSnake(f).getBounds();

            constrictor.constrict(sh.getStoredSnake(f), ip.getStack().getProcessor(f));
            constrictor.constrict(sh.getBackupSnake(f), ip.getStack().getProcessor(f));
          }
          sh.getLiveSnake().calcCentroid();
          sh.getLiveSnake().setPositions();
          sh.getLiveSnake().updateNormales(true);
          sh.getLiveSnake().getBounds();
          constrictor.constrict(sh.getLiveSnake(), ip.getStack().getProcessor(sh.getStartFrame()));
        }
      }
      vu.updateView(); // update view
    } catch (QuimpPluginException e) { // thrown by BinarySegmentation
      e.setMessageSinkType(MessageSinkTypes.GUI);
      e.handleException(pluginWnd, "Mask tracking problem:");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.IQuimpNestPlugin#attachData(com.github.celldynamics.quimp.
   * Nest)
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
        maskFilename = maskFile.getTitle();
      } else {
        maskFile = null;
      }
      LOGGER.debug("Choice action - mask: " + maskFile != null ? maskFile.toString() : "null");
    }
  }

  public void attachImage(ImagePlus img) {
    ip = img;

  }

}
