package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.utils.UiTools;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

/**
 * Simple seed picker by means of ROIs. Allow to select multiple foreground objects and one
 * background. Produces {@link Seeds} structure at output.
 * 
 * <p>Add current ROI to ROI list, if nothing selected look through ROI manager and renames
 * un-renamed ROIs according to requested type (FG or BG).
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class SeedPicker extends JFrame {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SeedPicker.class.getName());

  /**
   * Prefix for foreground ROIs.
   */
  public final String fgName = "fg";
  /**
   * Prefix for background ROI.
   */
  public final String bgName = "bg";

  /** The content pane. */
  private JPanel contentPane;
  /**
   * Image used for seeding. It is not modified, only its dimensions are used.
   */
  public ImagePlus image;

  /** The rm. */
  RoiManager rm;

  /** The last tool. */
  private String lastTool;

  /** The last line width. */
  private int lastLineWidth;

  /** The last fg num. */
  private int lastFgNum = 0; // cell number

  /** The last bg num. */
  private int lastBgNum = 0; // but this is index, cell is always 0 for BG

  /** The bn finish listeners. */
  // For Finish button we need specific order of ActionListeners
  private ActionListener[] bnFinishListeners = new ActionListener[2];
  /**
   * Converted seeds available after Finish.
   */
  public List<Seeds> seedsRoi = new ArrayList<>();

  /** The btn finish. */
  private JButton btnFinish;

  /**
   * Default constructor. Does not show window. Require {@link #image} to be set.
   */
  public SeedPicker() {
    this(false);

  }

  /**
   * Allow to provide image to process and disable UI. Recommended to API calls.
   * 
   * @param image image to seed, actually only size of it is required
   * @param show true to show the UI
   */
  public SeedPicker(ImagePlus image, boolean show) {
    this(show);
    this.image = image;
  }

  /**
   * Allow to provide image to work with.
   * 
   * @param image image to seed, actually only size of it is required
   */
  public SeedPicker(ImagePlus image) {
    this(true);
    this.image = image;
  }

  /**
   * Create the frame.
   * 
   * @param show true to show the UI
   */
  public SeedPicker(boolean show) {
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setResizable(false);
    setAlwaysOnTop(true);
    setType(Type.NORMAL);
    setTitle("Select seeds");
    setVisible(show);
    // setPreferredSize(new Dimension(300, 100));
    // setBounds(100, 100, 295, 66);
    contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

    JButton tglbtnNewFg = new JButton("New FG");
    UiTools.setToolTip(tglbtnNewFg,
            "Create next FG label from current selection or from unassigned ROIs from Roi Manager"
                    + ". For creating one label from separated ROIs,"
                    + " add them first to Roi Manager and then use this button.");
    tglbtnNewFg.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!increaseRois(fgName)) {
          // nothing added - add current selection
          Roi current = image.getRoi();
          if (current != null) {
            rm.add(image, current, 0);
            increaseRois(fgName);
          }
        }
      }
    });
    contentPane.add(tglbtnNewFg);

    JButton btnBackground = new JButton("New BG");
    UiTools.setToolTip(btnBackground,
            "Create next BG label from current selection or from unassigned ROIs from Roi Manager"
                    + ". For creating one label from separated ROIs,"
                    + " add them first to Roi Manager and then use this button.");
    btnBackground.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!increaseRois(bgName)) {
          // nothing added - add current selection
          Roi current = image.getRoi();
          if (current != null) {
            rm.add(image, current, 0);
            increaseRois(bgName);
          }
        }
      }
    });
    contentPane.add(btnBackground);

    btnFinish = new JButton("Finish");
    // this listener should go first, before that from RandomWalkView
    bnFinishListeners[0] = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          if (image == null) {
            throw new RandomWalkException("No image opened with SeedPicker.");
          }
          seedsRoi = SeedProcessor.decodeSeedsfromRoiStack(Arrays.asList(rm.getRoisAsArray()),
                  fgName, bgName, image.getWidth(), image.getHeight(), image.getImageStackSize());
          dispose();
        } catch (RandomWalkException ex) {
          ex.setMessageSinkType(MessageSinkTypes.GUI);
          ex.handleException((JFrame) SwingUtilities.getRoot(contentPane),
                  "Problem with converting seeds from ROI");
        }

      }
    };
    btnFinish.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        bnFinishListeners[0].actionPerformed(e);
        bnFinishListeners[1].actionPerformed(e);
      }
    });

    contentPane.add(btnFinish);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent arg0) {
        openRoiManager();
        selectTools();
      }

      @Override
      public void componentHidden(ComponentEvent e) {
        IJ.setTool(lastTool);
        Line.setWidth(lastLineWidth);
      }
    });
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent arg0) {
        IJ.setTool(lastTool);
        Line.setWidth(lastLineWidth);
      }

      @Override
      public void windowClosed(WindowEvent e) {
        IJ.setTool(lastTool);
        Line.setWidth(lastLineWidth);
      }
    });
    pack();
  }

  /**
   * Add Listener to Finish button that is executed as second.
   * 
   * <p>First listener prepares {@link SeedPicker#seedsRoi} structure, so second one can use it.
   * 
   * @param list listener to add as second
   */
  void addFinishController(ActionListener list) {
    bnFinishListeners[1] = list;
  }

  /**
   * Rename ROIs in RoiManager and increases counters.
   * 
   * @param coreName ROI to rename
   * @return true if something added
   */
  private boolean increaseRois(String coreName) {
    boolean ret = false;
    if (coreName.equals(fgName)) {
      ret = renameRois(fgName);
      if (ret) { // rename FG
        lastFgNum++; // and increase counter if anything renamed
      }
    }
    if (coreName.equals(bgName)) {
      ret = renameRois(bgName);
      if (ret) {
        lastBgNum++;
      }
    }
    return ret;
  }

  /**
   * Open roi manager.
   */
  private void openRoiManager() {
    rm = RoiManager.getRoiManager();
    // rm.reset();
  }

  /**
   * Select tools.
   */
  private void selectTools() {
    lastTool = IJ.getToolName();
    lastLineWidth = Line.getWidth();
    IJ.setTool("freeline");
    Line.setWidth(10);

  }

  /**
   * Rename ROIs in RoiManager.
   * 
   * <p>Any nonrenamed ROIS will be renamed to format corenameCell_no, where Cell is cell number and
   * no
   * is ROI number for the cell. There can be many ROIs for one cell. For background there is only
   * one object but again it cn be scribbled by many ROIs.
   * 
   * @param coreName corename ROI will be renamed to
   * @return true if anything was added.
   */
  private boolean renameRois(String coreName) {
    boolean added = false; // nothing renamed in manager
    int localIndex; // for FG will progress and match global Index, for BG always 0
    int localNum;
    if (rm == null) {
      return false;
    }
    if (coreName.equals(bgName)) {
      localIndex = 0; // do not count backgrounds ROIs
      localNum = lastBgNum;
    } else {
      localIndex = lastFgNum; // but count foreground
      localNum = 0;
    }
    List<Roi> rois = Arrays.asList(rm.getRoisAsArray());
    for (Roi roi : rois) {
      String n = roi.getName();
      if (n.startsWith(fgName) || n.startsWith(bgName)) {
        continue;
      }
      int roiIndex = rm.getRoiIndex(roi);
      rm.rename(roiIndex, coreName + localIndex + "_" + localNum);
      roi.setName(coreName + localIndex + "_" + localNum);
      localNum++;
      added = true;
    }
    if (coreName.equals(bgName)) {
      lastBgNum = localNum;
    }
    return added;
  }

  /**
   * Prepare for new ROI selection. Clears all counters, roi manager and Seed list.
   */
  public void reset() {
    lastFgNum = 0;
    lastBgNum = 0;
    seedsRoi.clear();
    if (rm != null) {
      if (rm.getRoisAsArray().length != 0) {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult =
                JOptionPane.showConfirmDialog(null, "Clear ROI manager??", "Warning", dialogButton);
        if (dialogResult == JOptionPane.YES_OPTION) {
          rm.reset();
        }
      }
    }
  }

}
