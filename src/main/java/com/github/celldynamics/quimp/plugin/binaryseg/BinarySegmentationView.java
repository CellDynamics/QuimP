package com.github.celldynamics.quimp.plugin.binaryseg;

import java.awt.Choice;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

import ij.WindowManager;

/**
 * Show UI for segmentation from masks and run it.
 * 
 * <p>Modifies provided Nest reference on Apply. Update BOA screen on Apply button.
 * 
 * <p>This is front end of {@link BinarySegmentation} used in BOA.
 * 
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.plugin.utils.QWindowBuilder
 * @see BinarySegmentation_
 */
public class BinarySegmentationView extends QWindowBuilder {

  // these fields code names of UI elements are are related to BinarySegmentationOptions#options
  static final String NAME = "name";
  static final String RESTORE_SNAKE = "Restore_Snake";
  static final String CLEAR_NEST = "Clear_nest";
  static final String SMOOTHING2 = "smoothing";
  static final String STEP2 = "step";
  static final String SELECT_IMAGE = "select_image";
  static final String LOAD_MASK = "load_mask";
  // this is not part of UI, just store name for BOA (only ParamList by getPluginConfig() is stored
  // in QCONF)
  static final String LOADED_FILE = "loaded_file";

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentationView.class.getName());

  // default parameters
  private int step = 1; // discretization step
  private boolean smoothing = false; // use smoothing?
  private boolean clearnest = true; // clear nest before adding next outline
  private boolean restoreFields = true; // if true internal Snake fields will be restored

  private ParamList uiDefinition; // window definition

  /**
   * Construct object.
   * 
   * @see com.github.celldynamics.quimp.plugin.utils.QWindowBuilder
   */
  public BinarySegmentationView() {
    // define window controls (selecter filled in buildWindow
    uiDefinition = new ParamList(); // will hold ui definitions
    uiDefinition.put(NAME, "BinarySegmentation"); // name of window
    uiDefinition.put(LOAD_MASK, "button: Load mask");
    uiDefinition.put(SELECT_IMAGE, "choice:" + BOA_.NONE);
    // start, end, step, default
    uiDefinition.put(STEP2, "spinner: 1: 10001: 1:" + Integer.toString(step));
    // name
    uiDefinition.put(SMOOTHING2, "checkbox: interpolation:" + Boolean.toString(smoothing));
    // clear nest
    uiDefinition.put(CLEAR_NEST, "checkbox: clear:" + Boolean.toString(clearnest));
    // restore
    uiDefinition.put(RESTORE_SNAKE, "checkbox: restore:" + Boolean.toString(restoreFields));
    // use http://www.freeformatter.com/java-dotnet-escape.html#ad-output for escaping
    //!>
    uiDefinition.put("help", "<font size=\"3\">If you use this plugin in standalone mode"
            + " (run from QuimP Toolbar, not from BOA), make sure that frame interval and pixel "
            + "scale are correct"
            + "<p><strong>Load Mask</strong> - Load mask file. "
            + "It should be 8-bit image of size of original stack with <span style=\"color:"
            + " #ffffff; background-color: #000000;\">black background</span> and"
            + " grayscale objects. If specified image is binary, cells will be tracked by testing "
            + "overlapping between frames. For grayscale images plugin will use gray levels to "
            + "assign cells to the same tracks."  
            + "</p>\r\n<p><strong>Select Image</strong> - Select mask already opened in"
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
  }

  /**
   * Add listener to apply button.
   * 
   * @param action listener
   * @see QWindowBuilder
   */
  void addApplyListener(ActionListener action) {
    applyB.addActionListener(action);
  }

  /**
   * Add listener to load mask button.
   * 
   * @param action listener
   */
  void addLoadMaskListener(ActionListener action) {
    ((JButton) ui.get(LOAD_MASK)).addActionListener(action);
  }

  /**
   * Add listener to image selector.
   * 
   * @param action listener
   */
  void addSelectImageListener(ItemListener action) {
    ((Choice) ui.get(SELECT_IMAGE)).addItemListener(action);
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
    pluginWnd.setVisible(false);
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
      private Choice getImage = (Choice) ui.get(SELECT_IMAGE);
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

  }
}
