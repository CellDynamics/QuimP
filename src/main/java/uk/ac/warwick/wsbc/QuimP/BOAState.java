package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.OpenDialog;

/**
 * Hold current BOA state that can be serialized
 * 
 * This class is composed from two inner classes:
 * -# BOAp - holds internal state of BOA plugin, maintained mainly for compatibility reasons
 * -# SegParam - holds segmentation parameters, exposed to UI
 * 
 * Moreover there are several fields related to new features of QuimP like storing internal
 * state for every frame separately or SnakePlugins.
 * 
 * @startuml
 * BOAState *-- "1" BOAp
 * BOAState *-- "1" SegParam
 * BOAState <|.. IQuimpSerialize
 * 
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
 * SegParam : +f_image
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
 * 
 * @enduml
 * 
 * @author p.baniukiewicz
 * @date 30 Mar 2016
 * @see Serializer
 */
class BOAState implements IQuimpSerialize {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    static final Logger LOGGER = LogManager.getLogger(BOAState.class.getName());
    /**
     * Reference to segmentation parameters. Holds current parameters
     * 
     * On every change of BOA state it is stored as copy in segParamSnapshots for current
     * frame. This is why that field is \c transient
     * 
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
     * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ConfigurationHandling
     */
    public transient SegParam segParam;
    public BOAp boap; //!< Reference to old BOAp class, keeps internal state of BOA

    public String fileName; //!< Current data file name
    /**
     * Keep snapshots of SegParam objects for every frame separately
     */
    private ArrayList<SegParam> segParamSnapshots;
    /**
     * Keep snapshots of SnakePluginList objects for every frame separately. Plugin
     * configurations are stored as well (but without plugin references)
     */
    private ArrayList<SnakePluginList> snakePluginListSnapshots;
    /**
     * List of plugins selected in plugin stack and information if they are active or not
     * This field is serializable.
     * 
     * Holds current parameters as the main object not referenced in BOAp
     * On every change of BOA state it is stored as copy in snakePluginListSnapshots for current
     * frame. This is why that field is \c transient
     * 
     * @see SnakePluginList
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
     */
    public transient SnakePluginList snakePluginList;
    /**
     * Reference to Nest, which is serializable as well
     * 
     * This is main object not referenced in other parts of QuimP
     */
    public Nest nest;
    /**
     * Store information whether for current frame button \b Edit was used. 
     * 
     * Do not indicate that any of Snakes was edited.
     */
    public ArrayList<Boolean> isFrameEdited;

    /**
     * Hold user parameters for segmentation algorithm
     * 
     * This class supports cloning and comparing.
     * 
     * @author p.baniukiewicz
     * @date 30 Mar 2016
     * @see BOAState
     */
    class SegParam {
        private double nodeRes; //!< Number of nodes on ROI edge 
        int blowup; //!< distance to blow up chain 
        double vel_crit;
        double f_central;
        double f_image; //!< image force 
        int max_iterations; //!< max iterations per contraction 
        int sample_tan;
        int sample_norm;
        double f_contract;
        double finalShrink;
        // Switch Params
        boolean use_previous_snake;//!< next contraction begins with prev chain 
        boolean showPaths;
        boolean expandSnake; //!< whether to act as an expanding snake
        private double min_dist; //!< min distance between nodes 
        private double max_dist; //!< max distance between nodes 

        /**
         * Copy constructor
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
         * Sets default values of parameters
         */
        public SegParam() {
            setDefaults();
            // defaults for GUI settings
            showPaths = false;
            use_previous_snake = true; // next contraction begins with last chain
            expandSnake = false; // set true to act as an expanding snake

        }

        /**
         * (non-Javadoc)
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

        /**
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof SegParam))
                return false;
            SegParam other = (SegParam) obj;
            if (blowup != other.blowup)
                return false;
            if (expandSnake != other.expandSnake)
                return false;
            if (Double.doubleToLongBits(f_central) != Double.doubleToLongBits(other.f_central))
                return false;
            if (Double.doubleToLongBits(min_dist) != Double.doubleToLongBits(other.min_dist))
                return false;
            if (Double.doubleToLongBits(max_dist) != Double.doubleToLongBits(other.max_dist))
                return false;
            if (Double.doubleToLongBits(f_contract) != Double.doubleToLongBits(other.f_contract))
                return false;
            if (Double.doubleToLongBits(f_image) != Double.doubleToLongBits(other.f_image))
                return false;
            if (Double.doubleToLongBits(finalShrink) != Double.doubleToLongBits(other.finalShrink))
                return false;
            if (max_iterations != other.max_iterations)
                return false;
            if (Double.doubleToLongBits(nodeRes) != Double.doubleToLongBits(other.nodeRes))
                return false;
            if (sample_norm != other.sample_norm)
                return false;
            if (sample_tan != other.sample_tan)
                return false;
            if (showPaths != other.showPaths)
                return false;
            if (use_previous_snake != other.use_previous_snake)
                return false;
            if (Double.doubleToLongBits(vel_crit) != Double.doubleToLongBits(other.vel_crit))
                return false;
            return true;
        }

        /**
         * Return nodeRes
         * 
         * @return nodeRes field
         */
        public double getNodeRes() {
            return nodeRes;
        }

        /**
         * Set \c nodeRes field and calculate \c min_dist and \c max_dist
         * 
         * @param d
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
         * These parameters are external - available for user to set in GUI.
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

        public double getMax_dist() {
            return max_dist;
        }

        public double getMin_dist() {
            return min_dist;
        }
    } // end of SegParam

    /**
     * Holds parameters defining snake and controlling contour matching algorithm.
     * BOAp is static class contains internal as well as external parameters used to
     * define snake and to control contour matching algorithm. There are also
     * several basic get/set methods for accessing selected parameters, setting
     * default {@link uk.ac.warwick.wsbc.QuimP.BOAp.SegParam.setDefaults() values} 
     * and writing/reading these (external) parameters to/from disk. File format used for
     * storing data in files is defined at {@link QParams} class.
     * 
     * External parameters are those related to algorithm options whereas internal
     * are those related to internal settings of algorithm, GUI and whole plugin
     * 
     * This class is shared among different QuimP components
     * 
     * @author rtyson
     * @see QParams
     * @see Tool
     */
    class BOAp {

        File orgFile; //!< handle to original file obtained from IJ (usually image opened) 
        File outFile; //!< handle to \a snPQ filled in QuimP.SnakeHandler.writeSnakes() 
        String fileName; //!< loaded image file name only, no extension (\c orgFile)
        transient QParams readQp; //!< read in parameter file 
        // internal parameters
        int NMAX; //!< maximum number of nodes (% of starting nodes) 
        double delta_t;
        double sensitivity;
        double f_friction;
        int FRAMES; //!< Number of frames in stack 
        int WIDTH, HEIGHT;
        int cut_every; //!< cut loops in chain every X frames 
        boolean oldFormat; //!< output old QuimP format? 
        boolean saveSnake; //!< save snake data 

        double proximity; //!< distance between centroids at which contact is tested for 
        double proxFreeze; //!< proximity of nodes to freeze when blowing up 
        boolean savedOne;
        
        /**
         * Current frame, CustomStackWindow.updateSliceSelector()
         * Not stored due to archiving all parameters for every frame separately
         */
        public transient int frame;
        /**
         * Snake selected in zoom selector, negative value if 100% view
         */
        public transient int snakeToZoom = -1;

        boolean singleImage;
        String paramsExist; // on startup check if defaults are needed to set
        boolean zoom;
        boolean doDelete;
        boolean doDeleteSeg;
        boolean editMode; //!< is select a cell for editing active? 
        int editingID; //!< currently editing cell iD. -1 if not editing
        boolean useSubPixel = true;
        boolean supressStateChangeBOArun = false;
        int callCount; //<! use to test how many times a method is called
        boolean SEGrunning; //!< is segmentation running 
        private double imageScale; //!< scale of image read from ip
        private boolean scaleAdjusted = false; //!< \c true when adjusted in constructor
        /**
         * @return the imageScale
         */
        public double getImageScale() {
            return imageScale;
        }

        /**
         * @param imageScale the imageScale to set
         */
        public void setImageScale(double imageScale) {
            if (imageScale == 0) {
                this.imageScale = 1;
                this.scaleAdjusted = true;
            }
        }

        /**
         * @return the scaleAdjusted
         */
        public boolean isScaleAdjusted() {
            return scaleAdjusted;
        }

        private double imageFrameInterval;
        private boolean fIAdjusted = false;

        /**
         * @return the imageFrameInterval
         */
        public double getImageFrameInterval() {
            return imageFrameInterval;
        }

        /**
         * @param imageFrameInterval the imageFrameInterval to set
         */
        public void setImageFrameInterval(double imageFrameInterval) {
            if (imageFrameInterval == 0) {
                this.imageFrameInterval = 1;
                this.fIAdjusted = true;
            }
        }

        /**
         * @return the fIAdjusted
         */
        public boolean isfIAdjusted() {
            return fIAdjusted;
        }

        /**
         * Default constructor
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
            proximity = 150; // distance between centroids at
                             // which contact is tested for
            proxFreeze = 1; // proximity of nodes to freeze when blowing up
            f_friction = 0.6;
            doDelete = false;
            doDeleteSeg = false;
            zoom = false;
            editMode = false;
            editingID = -1;
            callCount = 0;
            SEGrunning = false;
        }

        /**
         * Plot or not snakes after processing by plugins. If \c yes both snakes, after 
         * segmentation and after filtering are plotted.
         */
        boolean isProcessedSnakePlotted = true;

        /**
         * Define if first node of Snake (head) is plotted or not
         */
        boolean isHeadPlotted = false;

        /**
         * When any plugin fails this field defines how QuimP should behave. When
         * it is \c true QuimP breaks process of segmentation and do not store
         * filtered snake in SnakeHandler
         * @warning Currently not used
         * @todo TODO Implement this feature
         * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/81
         */
        boolean stopOnPluginError = true;

        /**
         * Initialize internal parameters of BOA plugin from ImagePlus
         * 
         * Most of these parameters are related to state machine of BOA. There are
         * also parameters related to internal state of Active Contour algorithm.
         * Defaults for parameters available for user are set in
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp.SegParam.setDefaults()}
         * 
         * @param ip Reference to segmented image passed from IJ
         */
        public void setup(final ImagePlus ip) {
            FileInfo fileinfo = ip.getOriginalFileInfo();
            if (fileinfo == null) {
                orgFile = new File(File.separator, ip.getTitle());
            } else {
                orgFile = new File(fileinfo.directory, fileinfo.fileName);
            }
            fileName = Tool.removeExtension(orgFile.getName());

            FRAMES = ip.getStackSize(); // get number of frames

            WIDTH = ip.getWidth();
            HEIGHT = ip.getHeight();
            paramsExist = "YES";

        }

        /**
         * Write set of snake parameters to disk.
         * 
         * writeParams method creates \a paQP master file, referencing other
         * associated files and \a csv file with statistics.
         * 
         * @param sID ID of cell. If many cells segmented in one time, QuimP
         * produces separate parameter file for every of them
         * @param startF Start frame (typically beginning of stack)
         * @param endF End frame (typically end of stack)
         * @see QParams
         */
        public void writeParams(int sID, int startF, int endF) {
            try {
                if (saveSnake) {
                    File paramFile = new File(outFile.getParent(), fileName + "_" + sID + ".paQP");
                    File statsFile = new File(outFile.getParent() + File.separator + fileName + "_"
                            + sID + ".stQP.csv");

                    QParams qp = new QParams(paramFile);
                    qp.segImageFile = orgFile;
                    qp.snakeQP = outFile;
                    qp.statsQP = statsFile;
                    qp.setImageScale(BOA_.qState.boap.imageScale);
                    qp.setFrameInterval(BOA_.qState.boap.imageFrameInterval);
                    qp.setStartFrame(startF);
                    qp.setEndFrame(endF);
                    qp.NMAX = NMAX;
                    qp.setBlowup(segParam.blowup);
                    qp.max_iterations = segParam.max_iterations;
                    qp.sample_tan = segParam.sample_tan;
                    qp.sample_norm = segParam.sample_norm;
                    qp.delta_t = delta_t;
                    qp.setNodeRes(segParam.nodeRes);
                    qp.vel_crit = segParam.vel_crit;
                    qp.f_central = segParam.f_central;
                    qp.f_contract = segParam.f_contract;
                    qp.f_image = segParam.f_image;
                    qp.f_friction = f_friction;
                    qp.finalShrink = segParam.finalShrink;
                    qp.sensitivity = sensitivity;

                    qp.writeParams();
                }
            } catch (QuimpException e) {
                LOGGER.error("Could not write parameters to file", e);
            }
        }

        /**
         * Read set of snake parameters from disk.
         * 
         * readParams method reads \a paQP master file, referencing other associated
         * files.
         * 
         * @return Status of operation
         * @retval true when file has been loaded successfully
         * @retval false when file has not been opened correctly or
         * QParams.readParams() returned \c false
         * @see QParams
         */
        public boolean readParams() {
            OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", "");
            if (od.getFileName() == null) {
                return false;
            }
            readQp = new QParams(new File(od.getDirectory(), od.getFileName()));

            try {
                readQp.readParams();
            } catch (QuimpException e) {
                BOA_.log("Failed to read parameter file " + e.getMessage());
                return false;
            }
            NMAX = readQp.NMAX;
            segParam.blowup = readQp.getBlowup();
            segParam.max_iterations = readQp.max_iterations;
            segParam.sample_tan = readQp.sample_tan;
            segParam.sample_norm = readQp.sample_norm;
            delta_t = readQp.delta_t;
            segParam.nodeRes = readQp.getNodeRes();
            segParam.vel_crit = readQp.vel_crit;
            segParam.f_central = readQp.f_central;
            segParam.f_contract = readQp.f_contract;
            segParam.f_image = readQp.f_image;

            if (readQp.paramFormat == QParams.QUIMP_11) {
                segParam.finalShrink = readQp.finalShrink;
            }
            BOA_.log("Successfully read parameters");
            return true;
        }
    }

    /**
     * Construct QState object for given stack size
     * Initializes other internal fields
     * 
     * @param ip current image object, can be \c null. In latter case only subclasses are 
     * initialized 
     */
    public BOAState(final ImagePlus ip) {
        boap = new BOAp(); // build BOAp
        segParam = new SegParam(); // and SegParam
        if (ip == null)
            return;
        int numofframes = ip.getStackSize();
        segParamSnapshots = new ArrayList<SegParam>(Collections.nCopies(numofframes, null));
        snakePluginListSnapshots =
                new ArrayList<SnakePluginList>(Collections.nCopies(numofframes, null));
        isFrameEdited = new ArrayList<Boolean>(Collections.nCopies(numofframes, false));
        BOA_.LOGGER.debug("Initialize storage of size: " + numofframes + " size of segParams: "
                + segParamSnapshots.size());
        boap.setImageScale(ip.getCalibration().pixelWidth);
        boap.setImageFrameInterval(ip.getCalibration().frameInterval);
    }

    /**
     * Make snapshot of current objects state
     * 
     * @param frame actual frame numbered from 1
     */
    public void store(int frame) {
        BOA_.LOGGER.debug("Data stored at frame:" + frame + " size of segParams is "
                + segParamSnapshots.size());
        segParamSnapshots.set(frame - 1, new SegParam(segParam));
        snakePluginListSnapshots.set(frame - 1, snakePluginList.getShallowCopy()); // download
                                                                                   // Plugin
                                                                                   // config
                                                                                   // as well
    }

    /**
     * Store information whether frame was edited only
     * 
     * Can be called when global state does not change, e.g. user clicked \b Edit button so
     * parameters and plugins have not been modified
     * 
     * @param frame current frame numbered from 1
     */
    public void storeOnlyEdited(int frame) {
        isFrameEdited.set(frame - 1, true);
    }

    /**
     * Should be called before serialization. Fills extra fields from BOAp
     */
    @Override
    public void beforeSerialize() {
        fileName = boap.fileName; // copy filename from system wide boap
        snakePluginList.beforeSerialize(); // download plugins configurations
        nest.beforeSerialize(); // prepare snakes
        // snakePluginListSnapshots and segParamSnapshots do not need beforeSerialize()
    }

    @Override
    public void afterSerialize() throws Exception {
        throw new UnsupportedOperationException();
    }
}