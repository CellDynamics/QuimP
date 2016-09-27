package uk.ac.warwick.wsbc.QuimP;

import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.YesNoCancelDialog;
import ij.io.OpenDialog;
import ij.measure.Measurements;
import ij.plugin.Converter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import uk.ac.warwick.wsbc.QuimP.filesystem.ANAParamCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer;
import uk.ac.warwick.wsbc.QuimP.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Main ANA class implementing IJ PlugInFilter.
 * 
 * @author tyson
 */
public class ANA_ implements PlugInFilter, DialogListener {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(ANA_.class.getName());

    private QconfLoader qconfLoader;

    private OutlineHandler oH, outputH, ecmH;
    private OutlinesCollection outputOutlineHandlers; // output for new data file
    private Outline frameOneClone;
    private ECMM_Mapping ecmMapping;
    private ImagePlus orgIpl;
    private ImageProcessor orgIpr;
    private Overlay overlay;
    // outlines can be plotted separately. They are generated by Ana() and stored here
    private ArrayList<Roi> storedOuterROI;
    private ArrayList<Roi> storedInnerROI;

    private FrameStatistics[] fluoStats;
    private ANAp anap;
    private static final int m =
            Measurements.AREA + Measurements.INTEGRATED_DENSITY + Measurements.MEAN;

    /**
     * Default constructor called always.
     */
    public ANA_() {
        storedOuterROI = new ArrayList<>();
        storedInnerROI = new ArrayList<>();
        anap = new ANAp();
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        about();
        if (imp == null) {
            IJ.error("Image required to take fluoresence measurments.");
        }

        // System.out.println("flouIm dir: " +
        // imp.getOriginalFileInfo().directory);
        if (imp.getOriginalFileInfo().directory.matches("")) {
            IJ.log("Error: Fluorescence file needs to be saved to disk");
            IJ.error("Please save your fluorescence image to file.");
            return DOES_8G + DOES_16 + NO_CHANGES;
        }

        // change to 8-bit. Nooooooo
        /*
         * IJ.log("Warning: Image was reduced to 8-bit"); if (imp.getStackSize()
         * == 1) { ImageConverter imConv = new ImageConverter(imp);
         * imConv.convertToGray8(); } else { StackConverter stackconverter = new
         * StackConverter(imp); //convert to 8bit
         * stackconverter.convertToGray8(); }
         */
        IJ.run("Appearance...", " menu=0"); // switch off interpolation of
                                            // zoomed images

        orgIpl = imp;
        overlay = new Overlay();
        orgIpl.setOverlay(overlay);

        return DOES_8G + DOES_16 + NO_CHANGES;

    }

    @Override
    public void run(ImageProcessor Ip) {
        IJ.showStatus("ANA Analysis");
        orgIpr = orgIpl.getProcessor();
        ECMp.plot = false;
        ecmMapping = new ECMM_Mapping(1);

        try {
            QuimpConfigFilefilter fileFilter = new QuimpConfigFilefilter(); // use default
            // engine for
            // finding extension
            FileDialog od = new FileDialog(IJ.getInstance(),
                    "Open paramater file " + fileFilter.toString());
            od.setFilenameFilter(fileFilter);
            od.setDirectory(OpenDialog.getLastDirectory());
            od.setMultipleMode(false);
            od.setMode(FileDialog.LOAD);
            od.setVisible(true);
            if (od.getFile() == null) {
                IJ.log("Cancelled - exiting...");
                return;
            }
            File paramFile = new File(od.getDirectory(), od.getFile());
            qconfLoader = new QconfLoader(paramFile.toPath()); // load file
            if (qconfLoader.getConfVersion() == QParams.QUIMP_11) { // old path
                runFromPAQP();
            } else if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
                qconfLoader.getBOA(); // verify whether boa has been run (throws if not)
                qconfLoader.getECMM(); // verify whether ecmm has been run (throws if not)
                qconfLoader.getStats(); // verify whether file contains stats
                if (qconfLoader.isANAPresent()) {
                    YesNoCancelDialog ync;
                    ync = new YesNoCancelDialog(IJ.getInstance(), "Overwrite",
                            "You are about to override previous ANA results. Is it ok?");
                    if (!ync.yesPressed()) // if no or cancel
                    {
                        IJ.log("No changes done in input file.");
                        return; // end}
                    }
                }
                runFromQCONF();
                IJ.log("The new data file " + paramFile.getName()
                        + " has been updated by results of ECMM analysis.");
            } else {
                throw new IllegalStateException("QconfLoader returned unknown version of QuimP");
            }
            // post-plotting
            overlay = new Overlay();
            orgIpl.setOverlay(overlay);
            for (int f = 1; f < orgIpl.getStackSize(); f++) {
                orgIpl.setSlice(f);
                for (OutlineHandler oH : outputOutlineHandlers.oHs) {
                    Outline o = oH.getOutline(f);
                    if (o == null)
                        continue;
                    drawSamplePointsFloat(o, f);
                    orgIpl.draw();
                }
            }
            // plotting outlines on separate image
            if (anap.plotOutlines) {
                ImagePlus orgIplclone = orgIpl.duplicate();
                orgIplclone.show();
                new Converter().run("RGB Color");
                Overlay overlay = new Overlay();
                orgIplclone.setOverlay(overlay);
                for (Roi r : storedOuterROI) {
                    overlay.add(r);
                }
                for (Roi r : storedInnerROI) {
                    overlay.add(r);
                }
                orgIplclone.draw();
            }

            IJ.log("ANA Analysis complete");
            IJ.showStatus("Finished");
            ecmMapping = null;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with run of ANA processing: " + e.getMessage());
        }
    }

    /**
     * Display standard QuimP about message.
     */
    private void about() {
        IJ.log(new Tool().getQuimPversion());
    }

    /**
     * Main executive for ANA processing for QParamsQconf (new file version).
     * 
     * @throws QuimpException when OutlineHandler can not be read
     * @throws IOException when configuration can not be saved on disk
     */
    private void runFromQCONF() throws IOException, QuimpException {
        LOGGER.debug("Processing from new file format");
        ANAParamCollection anaStates;
        OutlinesCollection ecmmState = qconfLoader.getQp().getLoadedDataContainer().ECMMState;
        outputOutlineHandlers = new OutlinesCollection(ecmmState.oHs.size());
        if (qconfLoader.getQp().getLoadedDataContainer().getANAState() == null)
            // create ANA slots for all outlines
            anaStates = new ANAParamCollection(ecmmState.oHs.size()); // store ANA options for every cell
        else
            anaStates = qconfLoader.getQp().getLoadedDataContainer().getANAState(); // update old
        for (int i = 0; i < ecmmState.oHs.size(); i++) { // go over all outlines
            ((QParamsQconf) qconfLoader.getQp()).setActiveHandler(i); // set current handler number.
                                                                      // For compatibility, all
                                                                      // methods have the same
                                                                      // syntax (assumes that there
                                                                      // is only one handler)
            oH = ecmmState.oHs.get(i); // restore handler from ecmm
            anap = anaStates.aS.get(i); // get i-th ana parameters
            anap.setup(qconfLoader.getQp());

            // fluoStats = FrameStat.read(anap.STATSFILE); // read stat file (it is outside QCONF!!)
            // get stats stored in QCONF
            fluoStats = qconfLoader.getStats().sHs.get(i).framestat.toArray(new FrameStatistics[0]);

            investigateChannels(oH.indexGetOutline(0));// find first empty channel
            if (anap.noData && oH.getSize() == 1) {
                // only one frame, so no ECMM. set outline res to 2
                System.out.println("Only one frame. set marker res to 2");
                oH.indexGetOutline(0).setResolution(anap.oneFrameRes); // should be 2!!!
            }
            setImageScale();
            orgIpl.setSlice(qconfLoader.getQp().getStartFrame());
            if (!anaDialog()) {
                IJ.log("ANA cancelled");
                return;
            }
            anap.fluTiffs[anap.channel] = new File(orgIpl.getOriginalFileInfo().directory,
                    orgIpl.getOriginalFileInfo().fileName);
            outputH = new OutlineHandler(oH); // copy input to output (ana will add fields to it)
            Ana(); // fills outputH
            FrameStatistics.write(fluoStats, anap.STATSFILE, anap); // save fluoro to statFile for comp.
            CellStats statH = qconfLoader.getStats().sHs.get(i); // store fluoro in QCONF
            statH.framestat = new ArrayList<FrameStatistics>(Arrays.asList(fluoStats)); // store stats
            outputOutlineHandlers.oHs.add(i, new OutlineHandler(outputH)); // store actual result in
                                                                           // container

        }

        DataContainer dc = qconfLoader.getQp().getLoadedDataContainer();
        dc.ECMMState = outputOutlineHandlers; // assign ECMM container to global output
        dc.ANAState = anaStates;
        qconfLoader.getQp().writeParams(); // save global container
        // generate additional OLD files (stQP is generated in loop already)
        FormatConverter fC = new FormatConverter(qconfLoader,
                ((QParamsQconf) qconfLoader.getQp()).getParamFile().toPath());
        fC.generateOldDataFiles();
    }

    /**
     * Main executive for ANA processing for QParams (old file version).
     * 
     * @throws QuimpException when OutlineHandler can not be read
     * @throws IOException when configuration can not be saved on disk
     */
    private void runFromPAQP() throws QuimpException, IOException {
        outputOutlineHandlers = new OutlinesCollection(1);
        oH = new OutlineHandler(qconfLoader.getQp());

        anap.setup(qconfLoader.getQp());
        fluoStats = FrameStatistics.read(anap.STATSFILE);
        investigateChannels(oH.indexGetOutline(0));// find first empty channel

        if (anap.noData && oH.getSize() == 1) {
            // only one frame, so no ECMM. set outline res to 2
            System.out.println("Only one frame. set marker res to 2");
            oH.indexGetOutline(0).setResolution(anap.oneFrameRes); // should be 2!!!
        }

        setImageScale();
        orgIpl.setSlice(qconfLoader.getQp().getStartFrame());
        if (!oH.readSuccess) {
            throw new QuimpException("Could not read OutlineHandler");
        }
        if (!anaDialog()) {
            IJ.log("ANA cancelled");
            return;
        }
        System.out.println("CHannel: " + (anap.channel + 1));
        // qp.cortexWidth = ANAp.cortexWidthScale;
        anap.fluTiffs[anap.channel] = new File(orgIpl.getOriginalFileInfo().directory,
                orgIpl.getOriginalFileInfo().fileName);

        outputH = new OutlineHandler(oH.getStartFrame(), oH.getEndFrame());
        Ana();

        anap.INFILE.delete();
        anap.STATSFILE.delete();
        outputH.writeOutlines(anap.OUTFILE, qconfLoader.getQp().ecmmHasRun);
        FrameStatistics.write(fluoStats, anap.STATSFILE, anap);

        // ----Write temp files-------
        // File tempFile = new File(ANAp.OUTFILE.getAbsolutePath() +
        // ".tempANA.txt");
        // outputH.writeOutlines(tempFile);
        // File tempStats = new File(ANAp.STATSFILE.getAbsolutePath() +
        // ".tempStats.csv");
        // FluoStats.write(fluoStats, tempStats);
        // IJ.log("ECMM:137, saving to a temp file instead");
        // --------------------------

        IJ.showStatus("ANA Complete");
        IJ.log("ANA Complete");

        qconfLoader.getQp().cortexWidth = anap.getCortexWidthScale();
        qconfLoader.getQp().fluTiffs = anap.fluTiffs;
        qconfLoader.getQp().writeParams();
        outputOutlineHandlers.oHs.add(0, new OutlineHandler(outputH)); // for plotting purposes

    }

    private boolean anaDialog() {
        GenericDialog pd = new GenericDialog("ANA Dialog", IJ.getInstance());
        pd.addNumericField("Cortex width (\u00B5m)", anap.getCortexWidthScale(), 2);

        String[] channelC = { "1", "2", "3" };
        pd.addChoice("Save in channel", channelC, channelC[anap.channel]);
        pd.addCheckbox("Normalise to interior", anap.normalise);
        pd.addCheckbox("Sample at Ch" + (anap.useLocFromCh + 1) + " locations", anap.sampleAtSame);
        pd.addCheckbox("Clear stored measurements", false);
        pd.addCheckbox("New image with outlines? ", anap.plotOutlines);
        pd.addDialogListener(this);

        frameOneClone = (Outline) oH.indexGetOutline(0).clone();
        drawOutlineAsOverlay(frameOneClone, Color.RED);
        shrink(frameOneClone);
        drawOutlineAsOverlay(frameOneClone, Color.RED);
        pd.showDialog();

        // ANAp.cortexWidth = pd.getNextNumber();

        return pd.wasOKed();

    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {

        if (gd.wasOKed()) {
            return true;
        }

        Checkbox cb = (Checkbox) gd.getCheckboxes().elementAt(2); // clear
                                                                  // measurements
        Choice iob = (Choice) gd.getChoices().elementAt(0);

        if (cb.getState() && !anap.cleared) { // reset if clear measurments
                                              // checked
            System.out.println("reset fluo");
            resetFluo();
            cb.setLabel("Measurments Cleared");
            IJ.log("All fluorescence measurements have been cleared");
            anap.channel = 0;
            iob.select(0);
            anap.cleared = true;
            return true;
        }

        double scale = gd.getNextNumber();
        anap.channel = gd.getNextChoiceIndex();
        anap.normalise = gd.getNextBoolean();
        anap.sampleAtSame = gd.getNextBoolean();
        anap.plotOutlines = ((Checkbox) gd.getCheckboxes().elementAt(3)).getState();
        anap.setCortextWidthScale(scale);
        if (anap.cleared) { // can't deselect
            cb.setState(true);
        }

        frameOneClone = (Outline) oH.indexGetOutline(0).clone();
        overlay.clear();
        drawOutlineAsOverlay(frameOneClone, Color.RED);
        shrink(frameOneClone);
        drawOutlineAsOverlay(frameOneClone, Color.RED);
        return true;// gd.invalidNumber();
    }

    void resetFluo() {
        // reset all fluo back to -2 and st res to 2 if only one frame
        Outline o;
        for (int i = 0; i < oH.getSize(); i++) {
            o = oH.indexGetOutline(i);
            o.clearFluores();
            fluoStats[i].clearFluo();
        }

        if (oH.getSize() == 1) {
            // only one frame, so no ECMM. set outline res to 2
            System.out.println("Only one frame. set marker res to 2");
            oH.indexGetOutline(0).setResolution(anap.oneFrameRes);
        }

        // clear frame stats
        anap.noData = true;
        anap.channel = 0;
        anap.useLocFromCh = -1;
        anap.presentData[1] = 0;
        anap.presentData[2] = 0;
        anap.presentData[0] = 0;
        anap.fluTiffs[0] = new File("/");
        anap.fluTiffs[1] = new File("/");
        anap.fluTiffs[2] = new File("/");
    }

    void setImageScale() {
        orgIpl.getCalibration().frameInterval = anap.frameInterval;
        orgIpl.getCalibration().pixelHeight = anap.scale;
        orgIpl.getCalibration().pixelWidth = anap.scale;
    }

    /**
     * Main method for fluorescence measurements analysis.
     */
    private void Ana() {
        Roi outerROI, innerROI;
        Outline o1, s1, s2;

        IJ.showStatus("Running ANA (" + oH.getSize() + " frames)");
        for (int f = oH.getStartFrame(); f <= oH.getEndFrame(); f++) { // change i to frames
            IJ.log("Frame " + f);
            IJ.showProgress(f, oH.getEndFrame());

            orgIpl.setSlice(f);
            o1 = oH.getOutline(f);

            s1 = new Outline(o1);
            s2 = new Outline(o1);
            shrink(s2);

            // HACK for Du's embryoImage
            // shrink(s1);
            // s1.scale(14, 0.2);
            // ***

            overlay = new Overlay();
            orgIpl.setOverlay(overlay);
            outerROI = o1.asFloatRoi();
            innerROI = s2.asFloatRoi();
            outerROI.setPosition(f);
            outerROI.setStrokeColor(Color.BLUE);
            innerROI.setPosition(f);
            innerROI.setStrokeColor(Color.RED);

            storedInnerROI.add(innerROI);
            storedOuterROI.add(outerROI);
            // overlay.setStrokeColor(Color.BLUE);
            overlay.add(outerROI);
            // overlay.setStrokeColor(Color.RED);
            overlay.add(innerROI);

            Polygon polyS2 = s2.asPolygon();
            setFluoStats(s1.asPolygon(), polyS2, f);

            // use sample points already there
            if (anap.sampleAtSame && anap.useLocFromCh != -1) {
                useGivenSamplepoints(o1);
            } else {

                ecmH = new OutlineHandler(1, 2);
                ecmH.setOutline(1, s1);
                ecmH.setOutline(2, s2);

                ecmH = ecmMapping.runByANA(ecmH, orgIpr, anap.getCortexWidthPixel());

                // copy flur data to o1 and save
                // some nodes may fail to migrate properly so need to check
                // tracknumbers match
                Vert v = o1.getHead();
                Vert v2 = ecmH.getOutline(2).getHead();

                while (v2.getTrackNum() != v.getTrackNum()) { // check id's match
                    v = v.getNext();
                    if (v.isHead()) {
                        IJ.error("ANA fail");
                        break;
                        // return;
                    }
                }

                int vStart;
                do {
                    v.setFluoresChannel(v2.fluores[0], anap.channel);
                    v2 = v2.getNext();
                    if (v2.isHead()) {
                        break;
                    }
                    vStart = v.getTrackNum();
                    // find next vert in o1 that matches v2
                    do {
                        v = v.getNext();
                        v.setFluoresChannel((int) Math.round(v.getX()), (int) Math.round(v.getY()),
                                -1, anap.channel); // map fail if -1. fix by interpolation
                        if (vStart == v.getTrackNum()) {
                            System.out.println("ANA fail");
                            return;
                        }
                    } while (v2.getTrackNum() != v.getTrackNum());
                } while (!v2.isHead());

                interpolateFailures(o1);
            }

            if (anap.normalise) {
                normalise2Interior(o1, f);
            }
            outputH.save(o1, f);
        }
    }

    private void shrink(Outline o) {
        double steps = anap.getCortexWidthPixel() / anap.stepRes;

        // System.out.println("steps: " + steps + ", step size: " +
        // ANAp.stepRes);
        Vert n;
        int j;
        int max = 10000;
        for (j = 0; j < steps; j++) {
            if (o.getNumVerts() <= 3) {
                break;
            }
            n = o.getHead();
            do {
                if (!n.frozen) {
                    n.setX(n.getX() - anap.stepRes * n.getNormal().getX());
                    n.setY(n.getY() - anap.stepRes * n.getNormal().getY());
                }
                n = n.getNext();
            } while (!n.isHead());
            o.updateNormales(true);
            removeProx(o);
            freezeProx(o);
            if (j > max) {
                System.out.println("shrink (336) hit max iterations!");
                break;
            }
        }

        if (o.getNumVerts() < 3) {
            System.out.println("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
        }

        if (o.cutSelfIntersects()) {
            System.out.println("ANA_(382)...fixed ana intersects");
        }

        if (o.getNumVerts() < 3) {
            System.out.println("ANA 377_NODES LESS THAN 3");
        }

        this.markFrozenNodesNormal(frameOneClone);
        orgIpl.draw();
        o.unfreezeAll();
    }

    private void removeProx(Outline o) {
        if (o.getNumVerts() <= 3) {
            return;
        }
        Vert v, vl, vr;
        double d1, d2;
        v = o.getHead();
        vl = v.getPrev();
        vr = v.getNext();
        do {
            d1 = ExtendedVector2d.lengthP2P(v.getPoint(), vl.getPoint());
            d2 = ExtendedVector2d.lengthP2P(v.getPoint(), vr.getPoint());

            if ((d1 < 1.5 || d2 < 1.5) && !v.frozen) { // don't remove frozen.
                                                       // May alter angles
                o.removeVert(v);
            }
            v = v.getNext().getNext();
            vl = v.getPrev();
            vr = v.getNext();
        } while (!v.isHead() && !vl.isHead());

    }

    private void freezeProx(Outline o) {
        // freeze a node and corresponding edge if its to close && close to
        // paralel
        Vert v, vT;
        ExtendedVector2d closest, edge, link;
        double dis, angle;

        v = o.getHead();
        do {
            // if (!v.frozen) {
            vT = o.getHead();
            do {
                if (vT.getTrackNum() == v.getTrackNum()
                        || vT.getNext().getTrackNum() == v.getTrackNum()) {
                    vT = vT.getNext();
                    continue;
                }
                closest = ExtendedVector2d.PointToSegment(v.getPoint(), vT.getPoint(),
                        vT.getNext().getPoint());
                dis = ExtendedVector2d.lengthP2P(v.getPoint(), closest);
                // System.out.println("dis: " + dis);
                // dis=1;
                if (dis < anap.freezeTh) {
                    edge = ExtendedVector2d.unitVector(vT.getPoint(), vT.getNext().getPoint());
                    link = ExtendedVector2d.unitVector(v.getPoint(), closest);
                    angle = Math.abs(ExtendedVector2d.angle(edge, link));
                    if (angle > Math.PI)
                        angle = angle - Math.PI; // if > 180, shift back around
                                                 // 180
                    angle = angle - 1.5708; // 90 degree shift to centre around
                                            // zero
                    // System.out.println("angle:" + angle);

                    if (angle < anap.angleTh && angle > -anap.angleTh) {
                        v.frozen = true;
                        vT.frozen = true;
                        vT.getNext().frozen = true;
                    }

                }
                vT = vT.getNext();
            } while (!vT.isHead());
            // }
            v = v.getNext();
        } while (!v.isHead());
    }

    private void markFrozenNodesNormal(Outline o) {
        float[] x;
        float[] y;
        ExtendedVector2d norm;
        PolygonRoi pr;
        Vert v = o.getHead();
        do {
            if (v.frozen) {
                overlay.setStrokeColor(Color.RED);
                norm = new ExtendedVector2d(v.getX(), v.getY());
                norm.addVec(v.getNormal());
                // norm.addVec(new Vect2d(1,1));

                x = new float[2];
                y = new float[2];

                x[0] = (float) v.getX();
                x[1] = (float) norm.getX();
                y[0] = (float) v.getY();
                y[1] = (float) norm.getY();
                pr = new PolygonRoi(x, y, 2, Roi.POLYGON);
                overlay.add(pr);
            }

            v = v.getNext();
        } while (!v.isHead());
    }

    private void setFluoStats(Polygon outerPoly, Polygon innerPoly, int f) {

        int store = f - anap.startFrame; // frame to index
        // System.out.println("store: " + store);
        fluoStats[store].frame = f;

        // orgIpl.setRoi(outerRoi);
        orgIpr.setRoi(outerPoly);
        ImageStatistics is = ImageStatistics.getStatistics(orgIpr, m, null); // this does NOT scale
                                                                             // to image
        double outerAreaRaw = is.area;
        fluoStats[store].channels[anap.channel].totalFluor = is.mean * is.area;
        fluoStats[store].channels[anap.channel].meanFluor = is.mean; // fluoStats[store].channels[ANAp.channel].totalFluor
                                                                     // /
                                                                     // fluoStats[store].area;

        orgIpr.setRoi(innerPoly);
        is = ImageStatistics.getStatistics(orgIpr, m, null);

        fluoStats[store].channels[anap.channel].innerArea = Tool.areaToScale(is.area, anap.scale);
        fluoStats[store].channels[anap.channel].totalInnerFluor = is.mean * is.area;
        fluoStats[store].channels[anap.channel].meanInnerFluor = is.mean; // fluoStats[store].channels[ANAp.channel].totalInnerFluor
                                                                          // /
                                                                          // fluoStats[store].channels[ANAp.channel].innerArea;

        fluoStats[store].channels[anap.channel].cortexArea =
                fluoStats[store].area - fluoStats[store].channels[anap.channel].innerArea; // scaled
        fluoStats[store].channels[anap.channel].totalCorFluo =
                fluoStats[store].channels[anap.channel].totalFluor
                        - fluoStats[store].channels[anap.channel].totalInnerFluor;
        fluoStats[store].channels[anap.channel].meanCorFluo =
                fluoStats[store].channels[anap.channel].totalCorFluo / (outerAreaRaw - is.area); // not
                                                                                                 // scaled

        fluoStats[store].channels[anap.channel].percCortexFluo =
                (fluoStats[store].channels[anap.channel].totalCorFluo
                        / fluoStats[store].channels[anap.channel].totalFluor) * 100;
        fluoStats[store].channels[anap.channel].cortexWidth = anap.getCortexWidthScale();
    }

    private void normalise2Interior(Outline o, int f) {
        // interior mean fluorescence is used to normalse membrane measurments
        int store = f - anap.startFrame; // frame to index
        Vert v = o.getHead();
        do {
            v.fluores[anap.channel].intensity = v.fluores[anap.channel].intensity
                    / fluoStats[store].channels[anap.channel].meanInnerFluor;
            v = v.getNext();
        } while (!v.isHead());

    }

    private void drawOutlineAsOverlay(Outline o, Color c) {
        Roi r = o.asFloatRoi();
        if (r.subPixelResolution())
            System.out.println("is sub pixel");
        else
            System.out.println("is not sub pixel");
        overlay.setStrokeColor(c);
        overlay.add(r);
        orgIpl.updateAndDraw();
    }

    private void investigateChannels(Outline o) {
        // flu maps
        int firstEmptyCh = -1;
        int firstFullCh = -1;

        anap.presentData = new int[3];
        anap.noData = true;

        Vert v = o.getHead();
        for (int i = 0; i < 3; i++) {
            if (v.fluores[i].intensity == -2) { // no data
                anap.presentData[i] = 0;
                if (firstEmptyCh == -1) {
                    firstEmptyCh = i;
                }
            } else {
                anap.presentData[i] = 1;
                IJ.log("Data exists in channel " + (i + 1));
                anap.noData = false;
                if (firstFullCh == -1) {
                    firstFullCh = i;
                }
                // anap.setCortextWidthScale(fluoStats[0].channels[i].cortexWidth);
            }
        }

        if (QuimPArrayUtils.sumArray(anap.presentData) == 3)
            firstEmptyCh = 0;

        if (anap.noData) {
            anap.channel = 0;
            IJ.log("No previous sample points available.");
            anap.useLocFromCh = -1;
        } else {
            anap.channel = firstEmptyCh;
            IJ.log("Sample points from channel " + (firstFullCh + 1) + " available.");
            anap.useLocFromCh = firstFullCh;
        }

        v = o.getHead();
        for (int i = 0; i < 3; i++)
            if (v.fluores[i].intensity != -2)
                anap.setCortextWidthScale(fluoStats[0].channels[i].cortexWidth);
    }

    private void interpolateFailures(Outline o) {
        Vert v = o.getHead();
        Vert last, nex;
        double disLtoN; // distance last to nex
        double disLtoV; // distance last to V
        double ratio, intensityDiff;
        boolean fail;
        int firstID;
        do {
            fail = false;
            if (v.fluores[anap.channel].intensity == -1) {
                IJ.log("\tInterpolated failed node intensity (position: " + v.coord + ")");
                // failed to map - interpolate with last/next successful

                last = v.getPrev();
                firstID = last.getTrackNum();
                while (last.fluores[anap.channel].intensity == -1) {
                    last = last.getPrev();
                    if (last.getTrackNum() == firstID) {
                        IJ.log("Could not interpolate as all nodes failed");
                        v.fluores[anap.channel].intensity = 0;
                        fail = true;
                    }
                }

                nex = v.getNext();
                firstID = nex.getTrackNum();
                while (nex.fluores[anap.channel].intensity == -1) {
                    nex = nex.getNext();
                    if (nex.getTrackNum() == firstID) {
                        IJ.log("Could not interpolate as all nodes failed");
                        v.fluores[anap.channel].intensity = 0;
                        fail = true;
                    }
                }

                if (fail) {
                    v = v.getNext();
                    continue;
                }

                disLtoN = ExtendedVector2d.lengthP2P(last.getPoint(), nex.getPoint());
                disLtoV = ExtendedVector2d.lengthP2P(last.getPoint(), v.getPoint());
                ratio = disLtoV / disLtoN;
                if (ratio > 1) {
                    ratio = 1;
                }
                if (ratio < 0) {
                    ratio = 0;
                }
                intensityDiff =
                        (nex.fluores[anap.channel].intensity - last.fluores[anap.channel].intensity)
                                * ratio;
                v.fluores[anap.channel].intensity =
                        last.fluores[anap.channel].intensity + intensityDiff;
                if (v.fluores[anap.channel].intensity < 0
                        || v.fluores[anap.channel].intensity > 255) {
                    IJ.log("Error. Interpolated intensity out of range. Set to zero.");
                    v.fluores[anap.channel].intensity = 0;
                }
            }

            v = v.getNext();
        } while (!v.isHead());
    }

    /*
     * private void drawSamplePoints(Outline o) { int x, y; PointRoi pr; Vert v
     * = o.getHead(); do { x = (int) v.fluores[ANAp.channel].x; y = (int)
     * v.fluores[ANAp.channel].y; pr = new PointRoi(x, y); overlay.add(pr); v =
     * v.getNext(); } while (!v.isHead()); }
     */

    private void drawSamplePointsFloat(Outline o, int frame) {
        float x, y;
        PointRoi pr;
        Vert v = o.getHead();
        do {
            x = (float) v.fluores[anap.channel].x;
            y = (float) v.fluores[anap.channel].y;
            pr = new PointRoi(x + 0.5, y + 0.5);
            pr.setPosition(frame);
            overlay.add(pr);
            v = v.getNext();
        } while (!v.isHead());
    }

    private void useGivenSamplepoints(Outline o1) {
        int x, y;

        Vert v = o1.getHead();
        do {
            x = (int) v.fluores[anap.useLocFromCh].x;
            y = (int) v.fluores[anap.useLocFromCh].y;

            v.fluores[anap.channel].intensity = sampleFluo(x, y);
            v.fluores[anap.channel].x = x;
            v.fluores[anap.channel].y = y;
            v = v.getNext();
        } while (!v.isHead());

    }

    private double sampleFluo(int x, int y) {
        double tempFlu = orgIpr.getPixelValue(x, y) + orgIpr.getPixelValue(x - 1, y)
                + orgIpr.getPixelValue(x + 1, y) + orgIpr.getPixelValue(x, y - 1)
                + orgIpr.getPixelValue(x, y + 1) + orgIpr.getPixelValue(x - 1, y - 1)
                + orgIpr.getPixelValue(x + 1, y + 1) + orgIpr.getPixelValue(x + 1, y - 1)
                + orgIpr.getPixelValue(x - 1, y + 1);
        tempFlu = tempFlu / 9d;
        return tempFlu;
    }
}

class ChannelStat {

    double innerArea = 0;
    double totalFluor = 0;
    double cortexWidth = 0;
    double meanFluor = 0;
    double meanInnerFluor = 0;
    double totalInnerFluor = 0;
    double cortexArea = 0;
    double totalCorFluo = 0;
    double meanCorFluo = 0;
    double percCortexFluo = 0;

    ChannelStat() {
    }
}
