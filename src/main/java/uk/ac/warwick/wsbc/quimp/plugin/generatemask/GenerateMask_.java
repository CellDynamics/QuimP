package uk.ac.warwick.wsbc.quimp.plugin.generatemask;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.BOAState;
import uk.ac.warwick.wsbc.quimp.Nest;
import uk.ac.warwick.wsbc.quimp.QParamsQconf;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.Snake;
import uk.ac.warwick.wsbc.quimp.SnakeHandler;
import uk.ac.warwick.wsbc.quimp.QuimpException.MessageSinkTypes;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/**
 * Convert QCONF files to BW masks.
 * 
 * Use Snake data produced by BOA and stored in QCONF file.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMask_ extends PluginTemplate {

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
   * @see #about()
   */
  public GenerateMask_(String paramFile) {
    run(paramFile);
  }

  @Override
  public int setup() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#setPluginConfig(uk.ac.warwick.wsbc.quimp.
   * plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    paramList = new ParamList(par);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#getPluginConfig()
   */
  @Override
  public ParamList getPluginConfig() {
    return paramList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#showUI(boolean)
   */
  @Override
  public void showUI(boolean val) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#getVersion()
   */
  @Override
  public String getVersion() {
    return "See QuimP version";
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#about()
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
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#parseOptions(java.lang.String)
   */
  @Override
  protected void parseOptions(String options) {
    if (options == null || options.isEmpty())
      return; // just use paramFile=null
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
   * @see uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate#runFromQCONF()
   */
  @Override
  protected void runFromQCONF() throws QuimpException {
    IJ.showStatus("Generate mask");
    BOAState bs = qconfLoader.getBOA();
    Nest nest = bs.nest;
    // create output image
    ImagePlus res = NewImage.createByteImage("test", bs.boap.getWIDTH(), bs.boap.getHEIGHT(),
            bs.boap.getFRAMES(), NewImage.FILL_BLACK);
    // get stacks reference
    ImageStack contourStack = res.getStack();
    res.setSlice(1); // set for first
    int frame; // frmaes counter (from 1)
    Snake snake;
    ImageProcessor contourIp; // processor taken from stack (ref)
    for (frame = 1; frame <= bs.boap.getFRAMES(); frame++) { // iterate over frames
      List<Integer> snakes = nest.getSnakesforFrame(frame); // find all SnakeHandlers on frame
      contourIp = contourStack.getProcessor(frame); // get processor from stack for frame
      contourIp.setColor(Color.WHITE); // set plotting color
      for (Integer snakeID : snakes) { // iterate over SnakeHandlers
        SnakeHandler sH = nest.getHandlerofId(snakeID); // get SH of snakeID
        if (sH != null) {
          snake = sH.getStoredSnake(frame); // get snake from this handler and current
                                            // frame
          Roi roi = snake.asFloatRoi(); // convert to ROI
          roi.setFillColor(Color.WHITE);
          contourIp.fill(roi); // plot on current slice
        }
      }
    }
    res.show();
    // save in QCONF folder
    QParamsQconf qp = (QParamsQconf) qconfLoader.getQp();
    Path filename = Paths.get(qp.getPath(), qp.getFileName() + FileExtensions.generateMaskSuffix);
    IJ.saveAsTiff(res, filename.toString());
    IJ.log("Saved in: " + filename.toString());
    if (runAsMacro == MessageSinkTypes.GUI) {
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
