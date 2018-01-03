package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

/**
 * Simple seed picker by means of ROIs. Allow to select multiple foreground objects and one
 * background. Produces {@link Seeds} structure at output.
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
  private JPanel contentPane;
  /**
   * Image used for seeding. It is not modified, only its dimensions are used.
   */
  public ImagePlus image;
  private RoiManager rm;
  private String lastTool;
  private int lastLineWidth;
  private int lastFgNum = 0; // cell number
  private int lastBgNum = 0; // but this is index, cell is always 0 for BG
  /**
   * Converted seeds available after Finish.
   */
  public List<Seeds> seeds = new ArrayList<>();

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
    setResizable(false);
    setAlwaysOnTop(true);
    setVisible(show);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 270, 40);
    contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

    JButton tglbtnNewFg = new JButton("New FG");
    tglbtnNewFg.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (renameRois(fgName)) {
          lastFgNum++;
        }
      }
    });
    contentPane.add(tglbtnNewFg);

    JButton btnBackground = new JButton("New BG");
    btnBackground.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (renameRois(bgName)) {
          lastBgNum++;
        }
      }
    });
    contentPane.add(btnBackground);

    JButton btnFinish = new JButton("Finish");
    btnFinish.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        seeds = convert(Arrays.asList(rm.getRoisAsArray()));
        IJ.setTool(lastTool);
        Line.setWidth(lastLineWidth);
        dispose();
      }
    });
    contentPane.add(btnFinish);
    if (show) {
      openRoiManager();
      selectTools();
    }
  }

  private void openRoiManager() {
    rm = RoiManager.getRoiManager();
    rm.reset();
  }

  private void selectTools() {
    lastTool = IJ.getToolName();
    lastLineWidth = Line.getWidth();
    IJ.setTool("freeline");
    Line.setWidth(10);

  }

  /**
   * Rename ROIs in RoiManager.
   * 
   * Any nonrenamed ROIS will be renamed to format corenameCell_no, where Cell is cell number and no
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
   * Convert list of ROIs to binary images separately for each ROI.
   * 
   * <p>Assumes that ROIs are named: fgNameID_NO, where ID belongs to the same object and NO are
   * different scribbles for it.
   * 
   * @param rois rois to process.
   * @return List of Seeds for each slice
   */
  public List<Seeds> convert(List<Roi> rois) {
    ArrayList<Seeds> ret = new ArrayList<>();
    if (image == null) {
      LOGGER.warn("Image does not exist!");
      return ret;
    }
    // find nonassigned ROIs
    List<Roi> col0 =
            rois.stream().filter(roi -> roi.getPosition() == 0).collect(Collectors.toList());
    // find ROIS on each slice
    for (int s = 1; s <= image.getStackSize(); s++) {
      final int w = s;
      List<Roi> col =
              rois.stream().filter(roi -> roi.getPosition() == w).collect(Collectors.toList());
      // merge those nonassigned and slice 1
      if (s == 1) {
        col.addAll(col0);
      }
      // produce Seeds
      Seeds tmpSeed = SeedProcessor.decodeSeedsRoi(col, fgName, bgName, image.getWidth(),
              image.getHeight());
      ret.add(tmpSeed);
    }

    // new ImagePlus("", ret.get(0).get(SeedTypes.BACKGROUND, 1)).show();
    // List<ImageProcessor> tmp = ret.get(0).get(SeedTypes.FOREGROUNDS);
    // for (ImageProcessor ip : tmp) {
    // new ImagePlus("", ip).show();
    // }

    return ret;

  }

}
