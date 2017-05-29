package com.github.celldynamics.quimp.plugin.generatemask;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.SnakeHandler;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.PluginTemplate;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
 * Convert QCONF files to BW masks.
 * 
 * <p>Use Snake data produced by BOA and stored in QCONF file.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMask_ extends PluginTemplate {

  /**
   * Resulting image.
   */
  private ImagePlus res;
  /**
   * default configuration parameters, for future using.
   */
  private ParamList paramList = new ParamList();

  /**
   * Do nothing here. For compatibility with IJ
   */
  public GenerateMask_() {
    super();
  }

  /**
   * Constructor that allows to provide own file.
   * 
   * @param paramFile it can be null to ask user for file or it can be parameters string like that
   *        passed in macro.
   * @throws QuimpPluginException on any error
   * @see #about()
   */
  public GenerateMask_(String paramFile) throws QuimpPluginException {
    super(paramFile);
  }

  @Override
  public int setup() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#setPluginConfig(com.github.celldynamics.quimp.
   * plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    paramList = new ParamList(par);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#getPluginConfig()
   */
  @Override
  public ParamList getPluginConfig() {
    return paramList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#showUI(boolean)
   */
  @Override
  public int showUi(boolean val) {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#getVersion()
   */
  @Override
  public String getVersion() {
    return "See QuimP version";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#about()
   */
  @Override
  public String about() {
    return "Generate mask plugin.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk\n" + "This plugin supports macro parameters\n"
            + "\tfilenam=path_to_QCONF";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#parseOptions(java.lang.String)
   */
  @Override
  protected void parseOptions(String options) {
    if (options == null || options.isEmpty()) {
      return; // just use paramFile=null
    }
    // go through params
    String val = Macro.getValue(options, "filename", null);
    if (val == null) { // this is allowed, just ask user
      paramFile = null; // do not do anything, paramFile isnull by default
    } else {
      paramFile = new File(val); // set file from options
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#runFromQCONF()
   */
  @Override
  protected void runFromQconf() throws QuimpException {
    IJ.showStatus("Generate mask");
    BOAState bs = qconfLoader.getBOA();
    Nest nest = bs.nest;
    // create output image
    res = NewImage.createByteImage("test", bs.boap.getWidth(), bs.boap.getHeight(),
            bs.boap.getFrames(), NewImage.FILL_BLACK);
    // get stacks reference
    ImageStack contourStack = res.getStack();
    res.setSlice(1); // set for first
    int frame; // frames counter (from 1)
    Snake snake;
    ImageProcessor contourIp; // processor taken from stack (ref)
    for (frame = 1; frame <= bs.boap.getFrames(); frame++) { // iterate over frames
      List<Integer> snakes = nest.getSnakesforFrame(frame); // find all SnakeHandlers on frame
      contourIp = contourStack.getProcessor(frame); // get processor from stack for frame
      contourIp.setColor(Color.WHITE); // set plotting color
      for (Integer snakeID : snakes) { // iterate over SnakeHandlers
        SnakeHandler snakeH = nest.getHandlerofId(snakeID); // get SH of snakeID
        if (snakeH != null) {
          snake = snakeH.getStoredSnake(frame); // get snake from this handler and current frame
          Roi roi = snake.asFloatRoi(); // convert to ROI
          roi.setFillColor(Color.WHITE);
          contourIp.fill(roi); // plot on current slice
        }
      }
    }
    if (!apiCall) { // do not show nor save if called from api
      res.show();
      // save in QCONF folder
      QParamsQconf qp = (QParamsQconf) qconfLoader.getQp();
      Path filename = Paths.get(qp.getPath(), qp.getFileName() + FileExtensions.generateMaskSuffix);
      IJ.saveAsTiff(res, filename.toString());
      IJ.log("Saved in: " + filename.toString());
      if (runAsMacro == MessageSinkTypes.GUI) { // run form IJ not from macro
        JOptionPane.showMessageDialog(
                IJ.getInstance(), QuimpToolsCollection
                        .stringWrap("Image saved! (see log to find path)", QuimP.LINE_WRAP),
                "Saved!", JOptionPane.INFORMATION_MESSAGE);
      } else {
        IJ.log("Mask generated!");
      }
      IJ.showStatus("Finished");
    }

  }

  /**
   * Return image generated by plugin. Use with API calls.
   * 
   * @return the res Mask image generated from loaded Qconf
   */
  public ImagePlus getRes() {
    return res;
  }
}
