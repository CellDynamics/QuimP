package uk.ac.warwick.wsbc.quimp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;
import ij.io.FileInfo;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentationPlugin;
import uk.ac.warwick.wsbc.quimp.plugin.engine.PluginFactory;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/*
 * //!>
 * @startuml doc-files/BOAState_UML.png
 * BOAState *-- "1" BOAp
 * BOAState *-- "1" SegParam
 * BOAState <|.. IQuimpSerialize
 * BOAState : +boap
 * BOAState : +SegParam
 * BOAState : -segParamSnapshots
 * BOAState : +SnakePluginList
 * BOAState : +Nest
 * BOAState : ... 
 * BOAState : +beforeSerialize()
 * BOAState : +afterSerialize()
 * BOAState : +store()
 * BOAState : +storeOnlyEdited()
 * SegParam : +nodeList
 * SegParam : +imageForce
 * SegParam : +equals()
 * SegParam : +hashCode()
 * SegParam : +setDefaults()
 * SegParam : ...()
 * SegParam : ...
 * IQuimpSerialize : +beforeSerialize()
 * IQuimpSerialize : +afterSerialize()
 * BOAp : -imageScale
 * BOAp : -scaleAdjusted
 * BOAp : +frame
 * BOAp : ~zoom
 * BOAp : ...
 * BOAp : ...()
 * @enduml
 * //!<
 */
/**
 * Hold current BOA state that can be serialized.
 * 
 * <p>This class is composed from two inner classes:
 * <ul>
 * <li>BOAp - holds internal state of BOA plugin, maintained mainly for compatibility reasons
 * <li>SegParam - holds segmentation parameters, exposed to UI
 * </ul>
 * 
 * <p>Moreover there are several fields related to new features of QuimP like storing internal state
 * for every frame separately or SnakePlugins.<br>
 * 
 * <img src="doc-files/BOAState_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 * @see Serializer
 */
public class BOAState implements IQuimpSerialize {

  private static final Logger LOGGER = LoggerFactory.getLogger(BOAState.class.getName());
  /**
   * Reference to segmentation parameters. Holds current parameters.
   * 
   * <p>On every change of BOA state it is stored as copy in segParamSnapshots for current frame.
   * This is why that field is transient
   * 
   * @see uk.ac.warwick.wsbc.quimp.BOA_#run(String)
   * @see <a href=
   *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ConfigurationHandling">ConfigurationHandling</a>
   */
  public transient SegParam segParam;
  /**
   * Reference to old BOAp class, keeps internal state of BOA.
   */
  public BOAp boap;
  /**
   * Instance of binary segmentation plugin that converts BW masks into snakes.
   * 
   * <p>This is regular plugin but handled separately from SnakePlugins and it is not provided as
   * external jar
   */
  public transient BinarySegmentationPlugin binarySegmentationPlugin;
  /**
   * Configuration of BinarySegmentation plugin if it was used. Used during saving boa state.
   */
  @SuppressWarnings("unused")
  private ParamList binarySegmentationParam;
  /**
   * Keep snapshots of SegParam objects for every frame separately.
   */
  private ArrayList<SegParam> segParamSnapshots;
  /**
   * Keep snapshots of SnakePluginList objects for every frame separately.
   * 
   * <p>Plugin configurations are stored as well (but without plugin references)
   */
  public ArrayList<SnakePluginList> snakePluginListSnapshots;
  /**
   * List of plugins selected in plugin stack and information if they are active or not. This field
   * is not serializable because snakePluginListSnapshots keeps configurations for
   * every frame.
   * 
   * <p>Holds current parameters as the main object not referenced in BOAp On every change of BOA
   * state it is stored as copy in snakePluginListSnapshots for current frame. This is why that
   * field is transient
   * 
   * @see uk.ac.warwick.wsbc.quimp.SnakePluginList
   * @see uk.ac.warwick.wsbc.quimp.BOA_#run(String)
   * @see uk.ac.warwick.wsbc.quimp.BOAState#store(int)
   */
  public transient SnakePluginList snakePluginList;
  /**
   * Reference to Nest, which is serializable as well. This is main object not referenced in other
   * parts of QuimP
   */
  public Nest nest;
  /**
   * Store information whether for current frame button <b>Edit</b> was used. Do not indicate that
   * any of Snakes was edited.
   */
  public ArrayList<Boolean> isFrameEdited;

  /**
   * Hold user parameters for segmentation algorithm.
   * 
   * <p>Most of those parameters are available from BOA user menu. This class supports cloning and
   * comparing.
   * 
   * @author p.baniukiewicz
   * @see uk.ac.warwick.wsbc.quimp.BOAState
   */
  class SegParam {
    /**
     * Number of nodes on ROI edge.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    private double nodeRes;
    /**
     * Distance to blow up chain.
     * 
     * <p>Check user manual or our publications for details.
     */
    public int blowup;
    /**
     * Critical velocity.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public double vel_crit;
    /**
     * Central force.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public double f_central;
    /**
     * Image force.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public double f_image;
    /**
     * Max iterations per contraction.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public int max_iterations;
    /**
     * Sample tan.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public int sample_tan;
    /**
     * Sample norm.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public int sample_norm;
    /**
     * Contraction force.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public double f_contract;
    /**
     * Final shrink.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public double finalShrink;
    // Switch Params
    /**
     * Next contraction begins with prev chain.
     */
    public boolean use_previous_snake;
    /**
     * Decide whether to show paths on screen.
     * 
     * <p>Cell segmentation parameter. Check user manual or our publications for details.
     */
    public boolean showPaths;
    /**
     * Whether to act as an expanding snake.
     * 
     * <p>Visualisation option parameter. Check user manual or our publications for details.
     */
    public boolean expandSnake;
    /**
     * Min distance between nodes.
     * 
     * <p>Cell segmentation parameter.
     */
    private double min_dist;
    /**
     * Max distance between nodes.
     * 
     * <p>Cell segmentation parameter.
     */
    private double max_dist;

    /**
     * Copy constructor.
     * 
     * @param src object to copy
     */
    public SegParam(final SegParam src) {
      this.nodeRes = src.nodeRes;
      this.blowup = src.blowup;
      this.vel_crit = src.vel_crit;
      this.f_central = src.f_central;
      this.f_image = src.f_image;
      this.max_iterations = src.max_iterations;
      this.sample_tan = src.sample_tan;
      this.sample_norm = src.sample_norm;
      this.f_contract = src.f_contract;
      this.finalShrink = src.finalShrink;
      this.use_previous_snake = src.use_previous_snake;
      this.showPaths = src.showPaths;
      this.expandSnake = src.expandSnake;
      this.min_dist = src.min_dist;
      this.max_dist = src.max_dist;
    }

    /**
     * Sets default values of parameters.
     */
    public SegParam() {
      setDefaults();
      // defaults for GUI settings
      showPaths = false;
      use_previous_snake = true; // next contraction begins with last chain
      expandSnake = false; // set true to act as an expanding snake

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + blowup;
      result = prime * result + (expandSnake ? 1231 : 1237);
      long temp;
      temp = Double.doubleToLongBits(f_central);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(f_contract);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(max_dist);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(min_dist);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(f_image);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(finalShrink);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + max_iterations;
      temp = Double.doubleToLongBits(nodeRes);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + sample_norm;
      result = prime * result + sample_tan;
      result = prime * result + (showPaths ? 1231 : 1237);
      result = prime * result + (use_previous_snake ? 1231 : 1237);
      temp = Double.doubleToLongBits(vel_crit);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof SegParam)) {
        return false;
      }
      SegParam other = (SegParam) obj;
      if (blowup != other.blowup) {
        return false;
      }
      if (expandSnake != other.expandSnake) {
        return false;
      }
      if (Double.doubleToLongBits(f_central) != Double.doubleToLongBits(other.f_central)) {
        return false;
      }
      if (Double.doubleToLongBits(min_dist) != Double.doubleToLongBits(other.min_dist)) {
        return false;
      }
      if (Double.doubleToLongBits(max_dist) != Double.doubleToLongBits(other.max_dist)) {
        return false;
      }
      if (Double.doubleToLongBits(f_contract) != Double.doubleToLongBits(other.f_contract)) {
        return false;
      }
      if (Double.doubleToLongBits(f_image) != Double.doubleToLongBits(other.f_image)) {
        return false;
      }
      if (Double.doubleToLongBits(finalShrink) != Double.doubleToLongBits(other.finalShrink)) {
        return false;
      }
      if (max_iterations != other.max_iterations) {
        return false;
      }
      if (Double.doubleToLongBits(nodeRes) != Double.doubleToLongBits(other.nodeRes)) {
        return false;
      }
      if (sample_norm != other.sample_norm) {
        return false;
      }
      if (sample_tan != other.sample_tan) {
        return false;
      }
      if (showPaths != other.showPaths) {
        return false;
      }
      if (use_previous_snake != other.use_previous_snake) {
        return false;
      }
      if (Double.doubleToLongBits(vel_crit) != Double.doubleToLongBits(other.vel_crit)) {
        return false;
      }
      return true;
    }

    /**
     * Return nodeRes.
     * 
     * @return nodeRes field
     */
    public double getNodeRes() {
      return nodeRes;
    }

    /**
     * Set nodeRes field and calculate <tt>min_dist</tt> and <tt>max_dist</tt>.
     * 
     * @param d resolution
     */
    public void setNodeRes(double d) {
      nodeRes = d;
      if (nodeRes < 1) {
        min_dist = 1; // min distance between nodes
        max_dist = 2.3; // max distance between nodes
        return;
      }
      min_dist = nodeRes; // min distance between nodes
      max_dist = nodeRes * 1.9; // max distance between nodes
    }

    /**
     * Set default parameters for contour matching algorithm.
     * 
     * <p>These parameters are external - available for user to set in GUI.
     */
    public void setDefaults() {
      setNodeRes(6.0);
      blowup = 20; // distance to blow up chain
      vel_crit = 0.005;
      f_central = 0.04;
      f_image = 0.2; // image force
      max_iterations = 4000; // max iterations per contraction
      sample_tan = 4;
      sample_norm = 12;
      f_contract = 0.04;
      finalShrink = 3d;
    }

    /**
     * Gets the max dist.
     *
     * @return the max dist
     */
    public double getMax_dist() {
      return max_dist;
    }

    /**
     * Gets the min dist.
     *
     * @return the min dist
     */
    public double getMin_dist() {
      return min_dist;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "SegParam [nodeRes=" + nodeRes + ", blowup=" + blowup + ", velCrit=" + vel_crit
              + ", centralForce=" + f_central + ", imageForce=" + f_image + ", maxIterations="
              + max_iterations + ", sampleTan=" + sample_tan + ", sampleNorm=" + sample_norm
              + ", contractForce=" + f_contract + ", finalShrink=" + finalShrink
              + ", use_previous_snake=" + use_previous_snake + ", showPaths=" + showPaths
              + ", expandSnake=" + expandSnake + ", min_dist=" + min_dist + ", max_dist=" + max_dist
              + "]";
    }

  } // end of SegParam

  /**
   * Holds parameters defining snake and controlling contour matching algorithm.
   * 
   * <p>BOAp is static class contains internal as well as external parameters used to define snake
   * and to control contour matching algorithm. There are also several basic get/set methods for
   * accessing selected parameters, setting default
   * {@link uk.ac.warwick.wsbc.quimp.BOAState.SegParam#setDefaults() values} and writing/reading
   * these (external) parameters to/from disk. File format used for storing data in files is
   * defined at {@link QParams} class.
   * 
   * <p>External parameters are those related to algorithm options whereas internal are those
   * related
   * to internal settings of algorithm, GUI and whole plugin
   * 
   * <p>This class is shared among different QuimP components.
   * 
   * @author rtyson
   * @see uk.ac.warwick.wsbc.quimp.QParams
   */
  public class BOAp {
    /**
     * handle to original file obtained from IJ (usually image opened).
     * 
     * <p>This filed is serialised for further verification of QCONF and image matching but not
     * restored on load to not overwrite currently opened image name.
     */
    private File orgFile;
    /**
     * Corename for output, initially contains path and name without extension from orgFile.
     * 
     * <p>Can be changed by user on save Change of this field causes change of the
     * <tt>fileName</tt>
     */
    private File outputFileCore;
    /**
     * <tt>outputFileCore</tt> but without path and extension.
     */
    private String fileName;
    /**
     * read in parameter file.
     */
    transient QParams readQp;
    // internal parameters
    /**
     * Maximum number of nodes (% of starting nodes).
     */
    int NMAX; // name related to QCONF file do not change

    /**
     * The delta t.
     */
    double delta_t;

    /**
     * The sensitivity.
     */
    double sensitivity;

    /**
     * The f friction.
     */
    double f_friction;
    /**
     * Number of frames in stack.
     */
    private int FRAMES; // name related to QCONF file do not change
    private int WIDTH, HEIGHT; // name related to QCONF file do not change
    /**
     * Cut loops in chain every X frames.
     */
    int cut_every;
    /**
     * output old QuimP format.
     */
    boolean oldFormat;
    /**
     * save snake data.
     */
    boolean saveSnake;

    /**
     * distance between centroids at which contact is tested for.
     */
    double proximity;
    /**
     * Proximity of nodes to freeze when blowing up.
     */
    double proxFreeze;

    /**
     * The saved one.
     */
    boolean savedOne;
    /**
     * Use json pretty format.
     */
    boolean savePretty = true;

    /**
     * Current frame, CustomStackWindow.updateSliceSelector().
     */
    public int frame;
    /**
     * Snake selected in zoom selector, negative value if 100% view.
     */
    public int snakeToZoom = -1;

    /**
     * The single image.
     */
    boolean singleImage;

    /**
     * The params exist.
     */
    String paramsExist; // on startup check if defaults are needed to set

    /**
     * The zoom.
     */
    boolean zoom;

    /**
     * The do delete.
     */
    boolean doDelete;

    /**
     * The do delete seg.
     */
    boolean doDeleteSeg;
    /**
     * is select a cell for editing active.
     */
    boolean editMode;
    /**
     * currently editing cell iD. -1 if not editing
     */
    int editingID;

    /**
     * The use sub pixel.
     */
    boolean useSubPixel = true;
    /**
     * Block rerun of runBoa() when spinners have been changed programmatically.
     * 
     * <p>Modification of spinners from code causes that stateChanged() event is called.
     */
    boolean supressStateChangeBOArun = false;
    /**
     * use to test how many times a method is called.
     */
    int callCount;
    /**
     * Indicate that {@link uk.ac.warwick.wsbc.quimp.BOA_#runBoa(int, int)} is active.
     * 
     * <p>This method calls {@link uk.ac.warwick.wsbc.quimp.ImageGroup#setIpSliceAll(int)} that
     * raises event
     * {@link uk.ac.warwick.wsbc.quimp.BOA_.CustomStackWindow#updateSliceSelector()} which then
     * fire other methods.
     */
    boolean SEGrunning;
    private double imageScale; // scale of image read from ip
    private boolean scaleAdjusted = false; // true when adjusted in constructor

    /**
     * Get scale of the image.
     * 
     * @return the imageScale
     */
    public double getImageScale() {
      return imageScale;
    }

    /**
     * Set scale of teh image.
     * 
     * @param imageScale the imageScale to set
     */
    public void setImageScale(double imageScale) {
      if (imageScale == 0) {
        this.imageScale = 1;
        this.scaleAdjusted = true;
      } else {
        this.imageScale = imageScale;
      }
    }

    /**
     * Get scaleAdjusted.
     * 
     * @return the scaleAdjusted
     */
    public boolean isScaleAdjusted() {
      return scaleAdjusted;
    }

    private double imageFrameInterval;
    private boolean fIAdjusted = false;

    /**
     * Get imageFrameInterval.
     * 
     * @return the imageFrameInterval
     */
    public double getImageFrameInterval() {
      return imageFrameInterval;
    }

    /**
     * Set imageFrameInterval.
     * 
     * @param imageFrameInterval the imageFrameInterval to set
     */
    public void setImageFrameInterval(double imageFrameInterval) {
      if (imageFrameInterval == 0) {
        this.imageFrameInterval = 1;
        this.fIAdjusted = true;
      } else {
        this.imageFrameInterval = imageFrameInterval;
      }
    }

    /**
     * Get fIAdjusted.
     * 
     * @return the fIAdjusted
     */
    public boolean isfIAdjusted() {
      return fIAdjusted;
    }

    /**
     * Default constructor.
     */
    public BOAp() {
      savedOne = false;
      // nestSize = 0;
      // internal parameters
      NMAX = 250; // maximum number of nodes (% of starting nodes)
      delta_t = 1.;
      sensitivity = 0.5;
      cut_every = 8; // cut loops in chain every X interations
      oldFormat = false; // output old QuimP format?
      saveSnake = true; // save snake data
      proximity = 150; // distance between centroids at which contact is tested for
      proxFreeze = 1; // proximity of nodes to freeze when blowing up
      f_friction = 0.6;
      doDelete = false;
      doDeleteSeg = false;
      zoom = false;
      editMode = false;
      editingID = -1;
      callCount = 0;
      SEGrunning = false;
      frame = 1;
      savePretty = true;
    }

    /**
     * Plot or not snakes after processing by plugins.
     * 
     * <p>If true both snakes, after segmentation and after filtering are plotted.
     */
    boolean isProcessedSnakePlotted = true;

    /**
     * Define if first node of Snake (head) is plotted or not.
     */
    boolean isHeadPlotted = false;

    /**
     * When any plugin fails this field defines how QuimP should behave.
     * 
     * <p>When it is true QuimP breaks process of segmentation and do not store filtered snake in
     * SnakeHandler.
     * TODO Implement this feature
     * 
     * @see <a href="http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/81">ticket#81</a>
     */
    boolean stopOnPluginError = true;

    /**
     * Initialize internal parameters of BOA plugin from ImagePlus.
     * 
     * <p>Most of these parameters are related to state machine of BOA. There are also parameters
     * related to internal state of Active Contour algorithm. Defaults for parameters available
     * for user are set in {@link uk.ac.warwick.wsbc.quimp.BOAState.SegParam#setDefaults()}
     * 
     * @param ip Reference to segmented image passed from IJ
     */
    public void setup(final ImagePlus ip) {
      FileInfo fileinfo = ip.getOriginalFileInfo();
      if (fileinfo == null) {
        orgFile = new File(File.separator, ip.getTitle());
        setOutputFileCore(File.separator + ip.getTitle());
      } else {
        orgFile = new File(fileinfo.directory, fileinfo.fileName);
        setOutputFileCore(fileinfo.directory + orgFile.getName());
      }

      FRAMES = ip.getStackSize(); // get number of frames

      WIDTH = ip.getWidth();
      HEIGHT = ip.getHeight();
      paramsExist = "YES";

    }

    /**
     * Get FRAMES.
     * 
     * @return the fRAMES
     */
    public int getFrames() {
      return FRAMES;
    }

    /**
     * Get WIDTH.
     * 
     * @return the wIDTH
     */
    public int getWidth() {
      return WIDTH;
    }

    /**
     * Get HEIGHT.
     * 
     * @return the hEIGHT
     */
    public int getHeight() {
      return HEIGHT;
    }

    /**
     * Get orgFile.
     * 
     * @return the orgFile
     */
    public File getOrgFile() {
      return orgFile;
    }

    /**
     * Set orgFile.
     * 
     * @param orgFile the orgFile to set
     */
    public void setOrgFile(File orgFile) {
      this.orgFile = orgFile;
    }

    /**
     * Get outputFileCore.
     * 
     * @return the outputFileCore
     */
    public File getOutputFileCore() {
      return outputFileCore;
    }

    /**
     * Set outputFileCore.
     * 
     * <p>From compatibility reasons outputFileCore is File type.
     * 
     * @param outputFileCore the outputFileCore to set. should not contain extension
     */
    private void setOutputFileCore(File outputFileCore) {
      this.outputFileCore = outputFileCore;
      fileName = outputFileCore.getName();
    }

    /**
     * Set setOutputFileCore.
     * 
     * @param outputFileCore the outputFileCore to set
     */
    public void setOutputFileCore(String outputFileCore) {
      setOutputFileCore(new File(QuimpToolsCollection.removeExtension(outputFileCore)));
    }

    /**
     * Get fileName.
     * 
     * @return the fileName
     */
    public String getFileName() {
      return fileName;
    }

    /**
     * Generate Snake file name basing on ID.
     * 
     * <p>Mainly to have this in one place. Use outputFileCore that is set by user choice of output
     * 
     * @param id of Snake
     * @return Full path to file with extension
     */
    public String deductSnakeFileName(int id) {
      LOGGER.trace(getOutputFileCore().getAbsoluteFile().toString());
      return getOutputFileCore().getAbsoluteFile() + "_" + id + FileExtensions.snakeFileExt;
    }

    /**
     * Generate stats file name basing on ID.
     * 
     * <p>Mainly to have this in one place. Use outputFileCore that is set by user choice of output
     * 
     * @param id of Snake
     * @return Full path to file with extension
     */
    public String deductStatsFileName(int id) {
      return getOutputFileCore().getAbsoluteFile() + "_" + id + FileExtensions.statsFileExt;
    }

    /**
     * Generate main param file (old) name basing on ID.
     * 
     * <p>Mainly to have this in one place. Use outputFileCore that is set by user choice of output
     * 
     * @param id of Snake
     * @return Full path to file with extension
     */
    public String deductParamFileName(int id) {
      return getOutputFileCore().getAbsoluteFile() + "_" + id + FileExtensions.configFileExt;
    }

    /**
     * Generate main filter config file name.
     * 
     * <p>Mainly to have this in one place. Use outputFileCore that is set by user choice of output
     * 
     * @return Full path to file with extension
     */
    public String deductFilterFileName() {
      return getOutputFileCore().getAbsoluteFile() + FileExtensions.pluginFileExt;
    }

    /**
     * Generate main param file (new) name.
     * 
     * <p>Mainly to have this in one place. Use outputFileCore that is set by user choice of output
     * 
     * @return Full path to file with extension
     */
    public String deductNewParamFileName() {
      return getOutputFileCore().getAbsoluteFile() + FileExtensions.newConfigFileExt;
    }

  }

  /**
   * Default constructor.
   */
  public BOAState() {
    boap = new BOAp(); // build BOAp
    segParam = new SegParam(); // and SegParam
    snakePluginList = new SnakePluginList();
    binarySegmentationParam = new ParamList(); // save empty list even if plugin not used
  }

  /**
   * Construct BOAState object for given stack size. Initializes other internal fields.
   * 
   * @param ip current image object, can be \c null. In latter case only subclasses are
   *        initialized
   */
  public BOAState(final ImagePlus ip) {
    this(ip, null, null);

  }

  /**
   * Construct full base object filling snapshots with default but valid objects.
   * 
   * @param ip current image object, can be \c null. In latter case only subclasses are
   *        initialized
   * @param pf PluginFactory used for creating plugins
   * @param vu ViewUpdater reference
   */
  public BOAState(final ImagePlus ip, final PluginFactory pf, final ViewUpdater vu) {
    this();
    snakePluginList = new SnakePluginList(BOA_.NUM_SNAKE_PLUGINS, pf, vu);
    if (ip == null) {
      return;
    }

    initializeSnapshots(ip, pf, vu);
    // set scale read from image, set also scaleAdjusted if scale from image is wrong
    boap.setImageScale(ip.getCalibration().pixelWidth);
    // set interval read from image, set also fIAdjusted if scale from image is wrong
    boap.setImageFrameInterval(ip.getCalibration().frameInterval);
    boap.setup(ip);
  }

  private void initializeSnapshots(final ImagePlus ip, final PluginFactory pf,
          final ViewUpdater vu) {
    int numofframes = ip.getStackSize();
    // fill snaphots with default values
    segParamSnapshots = new ArrayList<SegParam>(Collections.nCopies(numofframes, new SegParam()));
    snakePluginListSnapshots = new ArrayList<SnakePluginList>(
            Collections.nCopies(numofframes, new SnakePluginList(BOA_.NUM_SNAKE_PLUGINS, pf, vu)));
    isFrameEdited = new ArrayList<Boolean>(Collections.nCopies(numofframes, false));
    LOGGER.debug("Initialize storage of size: " + numofframes + " size of segParams: "
            + segParamSnapshots.size());
  }

  /**
   * Make snapshot of current objects state.
   * 
   * @param frame actual frame numbered from 1
   * @see uk.ac.warwick.wsbc.quimp.SnakePluginList
   */
  public void store(int frame) {
    LOGGER.debug(
            "Data stored at frame:" + frame + " size of segParams is " + segParamSnapshots.size());
    segParamSnapshots.set(frame - 1, new SegParam(segParam));
    // download Plugin config as well
    snakePluginListSnapshots.set(frame - 1, snakePluginList.getDeepCopy());
  }

  /**
   * Copy from snapshots data to current one.
   * 
   * @param frame current frame
   * @see uk.ac.warwick.wsbc.quimp.SnakePluginList
   */
  public void restore(int frame) {
    LOGGER.trace("Data restored from frame:" + frame);
    SegParam tmp = segParamSnapshots.get(frame - 1);
    if (tmp != null) {
      segParam = tmp;
    }
    snakePluginList = snakePluginListSnapshots.get(frame - 1);
  }

  /**
   * Store information whether frame was edited only.
   * 
   * <p>Can be called when global state does not change, e.g. user clicked \b Edit button so
   * parameters and plugins have not been modified.
   * 
   * @param frame current frame numbered from 1
   */
  public void storeOnlyEdited(int frame) {
    isFrameEdited.set(frame - 1, true);
  }

  /**
   * Reset BOAState class.
   * 
   * <p>This method does:
   * <ol>
   * <li>Closes all windows from plugins
   * <li>Cleans all snapshots
   * <li>Set default parameters
   * </ol>
   * 
   * @param ip current image object, can be \c null. In latter case only subclasses are
   *        initialized
   * @param pf PluginFactory used for creating plugins
   * @param vu ViewUpdater reference
   */
  public void reset(final ImagePlus ip, final PluginFactory pf, final ViewUpdater vu) {
    if (snakePluginList != null) {
      snakePluginList.clear();
    }
    if (snakePluginListSnapshots != null) {
      for (SnakePluginList sp : snakePluginListSnapshots) {
        if (sp != null) {
          sp.clear();
        }
      }
    }
    // boap = new BOAp(); // must be disabled becaue boap keeps some data related to loaded
    // image that never changes
    segParam = new SegParam(); // and SegParam
    initializeSnapshots(ip, pf, vu);
  }

  /**
   * Should be called before serialization.
   * 
   * <p>Creates ArrayLists from Shape.
   */
  @Override
  public void beforeSerialize() {
    nest.beforeSerialize(); // prepare snakes
    if (binarySegmentationPlugin != null) {
      binarySegmentationParam = binarySegmentationPlugin.getPluginConfig();
    } else {
      binarySegmentationParam = new ParamList();
    }

    // snakePluginListSnapshots and segParamSnapshots do not need beforeSerialize()
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    LOGGER.trace("After serialize called");
    nest.afterSerialize(); // rebuild Shape<T extends PointsList<T>> from ArrayList used for storing
    snakePluginList.afterSerialize(); // assumes that snakePluginList contains valid refs to Facory

    // recreate file objects. without these lines the File objects after serialization are
    // created but they do not keep information about root, e.g. getAbsolutePath() returns
    // program_path/stored_in_json_path
    boap.outputFileCore = new File(boap.outputFileCore.toString());
    boap.orgFile = new File(boap.orgFile.toString());
    // restore local segParam to be first from segParamSnapshots
    if (segParamSnapshots.size() > 0) {
      if (segParamSnapshots.get(0) != null) {
        segParam = new SegParam(segParamSnapshots.get(0));
        // otherwise segParam will be default owng to SegParam constructor
      }
    }
  }

  /**
   * Write set of snake parameters to disk.
   * 
   * <p>writeParams method creates <i>paQP</i> master file, referencing other associated files and
   * <i>csv</i> file with statistics.
   * 
   * <p>Compatibility layer with old QuimP
   * 
   * @param sid ID of cell. If many cells segmented in one time, QuimP produces separate parameter
   *        file for every of them
   * @param startF Start frame (typically beginning of stack)
   * @param endF End frame (typically end of stack)
   * @see uk.ac.warwick.wsbc.quimp.QParams
   * @see <a href=
   *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/176#comment:3">ticket/176</a>
   */
  public void writeParams(int sid, int startF, int endF) {
    try {
      if (boap.saveSnake) {
        File paramFile = new File(boap.deductParamFileName(sid));
        QParams qp = new QParams(paramFile);
        qp.setSegImageFile(boap.orgFile);
        qp.setSnakeQP(new File(boap.deductSnakeFileName(sid)));
        qp.setStatsQP(new File(boap.deductStatsFileName(sid)));
        qp.setImageScale(BOA_.qState.boap.imageScale);
        qp.setFrameInterval(BOA_.qState.boap.imageFrameInterval);
        qp.setStartFrame(startF);
        qp.setEndFrame(endF);
        qp.nmax = boap.NMAX;
        qp.setBlowup(segParam.blowup);
        qp.maxIterations = segParam.max_iterations;
        qp.sampleTan = segParam.sample_tan;
        qp.sampleNorm = segParam.sample_norm;
        qp.deltaT = boap.delta_t;
        qp.setNodeRes(segParam.nodeRes);
        qp.velCrit = segParam.vel_crit;
        qp.centralForce = segParam.f_central;
        qp.contractForce = segParam.f_contract;
        qp.imageForce = segParam.f_image;
        qp.frictionForce = boap.f_friction;
        qp.finalShrink = segParam.finalShrink;
        qp.sensitivity = boap.sensitivity;

        qp.writeParams();
      }
    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Could not write parameters to file", e.getMessage());
    }
  }

  /**
   * Read set of snake parameters from disk.
   * 
   * <p>readParams method reads <i>paQP</i> master file, referencing other associated files.
   * 
   * @param paramFile paQP configuration file
   * 
   * @return Status of operation, true when file has been loaded successfully, false when file has
   *         not been opened correctly or {@link uk.ac.warwick.wsbc.quimp.QParams#readParams()}
   *         returned false
   * @see uk.ac.warwick.wsbc.quimp.QParams
   * @see <a href=
   *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/176#comment:3">ticket/176</a>
   */
  public boolean readParams(File paramFile) {
    boap.readQp = new QParams(paramFile);

    try {
      boap.readQp.readParams();
    } catch (QuimpException e) {
      BOA_.log("Failed to read parameter file " + e.getMessage());
      return false;
    }
    loadParams(boap.readQp);
    BOA_.log("Successfully read parameters");
    return true;
  }

  /**
   * Build internal boa state from QParams object.
   * 
   * <p><b>Warning</b>
   * 
   * <p>frame interval and image scale are not loaded by this function due to compatibility with BOA
   * workflow - user set scale on beginning and then use this method to load paQP files.
   * 
   * @param readQp QParams object
   */
  public void loadParams(QParams readQp) {

    boap.NMAX = readQp.nmax;
    segParam.blowup = readQp.getBlowup();
    segParam.max_iterations = readQp.maxIterations;
    segParam.sample_tan = readQp.sampleTan;
    segParam.sample_norm = readQp.sampleNorm;
    boap.delta_t = readQp.deltaT;
    segParam.nodeRes = readQp.getNodeRes();
    segParam.vel_crit = readQp.velCrit;
    segParam.f_central = readQp.centralForce;
    segParam.f_contract = readQp.contractForce;
    segParam.f_image = readQp.imageForce;

    if (readQp.paramFormat == QParams.QUIMP_11) {
      segParam.finalShrink = readQp.finalShrink;
    }
    boap.readQp = readQp;
    // copy loaded data to snapshots
    for (int f = readQp.getStartFrame(); f <= readQp.getEndFrame(); f++) {
      store(f);
    }
  }
}