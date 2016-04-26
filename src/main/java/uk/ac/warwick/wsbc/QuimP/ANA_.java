/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.measure.Measurements;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

import java.awt.*;
import java.io.*;

/**
 * Main ANA class implementing IJ PlugInFilter
 * 
 * @author tyson
 */
public class ANA_ implements PlugInFilter, DialogListener {

    OutlineHandler oH, outputH, ecmH;
    Outline frameOneClone;
    ECMM_Mapping ecmMapping;
    ImagePlus orgIpl;
    ImageProcessor orgIpr;
    ImageStack orgStack;
    Overlay overlay;
    Roi outerROI, innerROI;
    FluoStats[] fluoStats;
    QParams qp;
    private static final int m = Measurements.AREA + Measurements.INTEGRATED_DENSITY + Measurements.MEAN;

    @Override
    public int setup(String arg, ImagePlus imp) {

        IJ.log("##############################################\n \n" + Tool.getQuimPversion()
                + " - ANA plugin,\nby Richard Tyson (R.A.Tyson@warwick.ac.uk)\n\n"
                + " & T. Bretschneider (T.Bretschneider@warwick.ac.uk)\n\n"
                + "##############################################\n \n");

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

        /*
         * //angle test Vect2d v1 = new Vect2d(-1,0); Vect2d v2 = new
         * Vect2d(0.5,1); v2.makeUnit(); double angle = Vect2d.angle(v1, v2);
         * System.out.println("angle " + angle); if(true)return;
         * 
         * 
         * ////////////
         */
        orgIpr = orgIpl.getProcessor();
        orgStack = orgIpl.getStack();
        ECMp.plot = false;
        ecmMapping = new ECMM_Mapping(1);

        try {
            do {
                OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", OpenDialog.getLastDirectory(),
                        ".paQP");
                if (od.getFileName() == null) {
                    return;
                }
                File paramFile = new File(od.getDirectory(), od.getFileName());

                qp = new QParams(paramFile);
                qp.readParams();// will NOT know startFrame or endFrame until
                                // outlines read if old format!!
                oH = new OutlineHandler(qp);

                ANAp.setup(qp);
                fluoStats = FluoStats.read(ANAp.STATSFILE);
                investigateChannels(oH.indexGetOutline(0));// find first empty
                                                           // channel

                if (ANAp.noData && oH.getSize() == 1) {
                    // only one frame, so no ECMM. set outline res to 2
                    System.out.println("Only one frame. set marker res to 2");
                    oH.indexGetOutline(0).setResolution(ANAp.oneFrameRes); // should
                                                                           // be
                                                                           // 2!!!
                }

                setImageScale();
                orgIpl.setSlice(qp.startFrame);
                if (!oH.readSuccess) {
                    return;
                }
                if (!anaDialog()) {
                    IJ.log("ANA cancelled");
                    return;
                }
                System.out.println("CHannel: " + (ANAp.channel + 1));
                // qp.cortexWidth = ANAp.cortexWidthScale;
                qp.fluTiffs[ANAp.channel] = new File(orgIpl.getOriginalFileInfo().directory,
                        orgIpl.getOriginalFileInfo().fileName);

                outputH = new OutlineHandler(oH.getStartFrame(), oH.getEndFrame());
                Ana();

                ANAp.INFILE.delete();
                ANAp.STATSFILE.delete();
                outputH.writeOutlines(ANAp.OUTFILE, qp.ecmmHasRun);
                FluoStats.write(fluoStats, ANAp.STATSFILE);

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

                qp.cortexWidth = ANAp.getCortexWidthScale();
                qp.writeParams();
                return;

            } while (true);

        } catch (Exception e) {
            e.printStackTrace();
            IJ.error("Unknown exception 170");
        }

        // ECMp.setDefault();
        ecmMapping = null;
        System.out.println("\nfinished");
    }

    private boolean anaDialog() {
        GenericDialog pd = new GenericDialog("ANA Dialog", IJ.getInstance());
        pd.addNumericField("Cortex width (\u00B5m)", ANAp.getCortexWidthScale(), 2);

        String[] channelC = { "1", "2", "3" };
        pd.addChoice("Save in channel", channelC, channelC[ANAp.channel]);
        pd.addCheckbox("Normalise to interior", ANAp.normalise);
        pd.addCheckbox("Sample at Ch" + (ANAp.useLocFromCh + 1) + " locations", ANAp.sampleAtSame);
        pd.addCheckbox("Clear stored measurements", false);
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

        if (cb.getState() && !ANAp.cleared) { // reset if clear measurments
                                              // checked
            System.out.println("reset fluo");
            resetFluo();
            cb.setLabel("Measurments Cleared");
            IJ.log("All fluorescence measurements have been cleared");
            ANAp.channel = 0;
            iob.select(0);
            ANAp.cleared = true;
            return true;
        }

        ANAp.setCortextWidthScale(gd.getNextNumber());
        ANAp.channel = gd.getNextChoiceIndex();
        ANAp.normalise = gd.getNextBoolean();
        ANAp.sampleAtSame = gd.getNextBoolean();

        if (ANAp.cleared) { // can't deselect
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
            oH.indexGetOutline(0).setResolution(ANAp.oneFrameRes);
        }

        // clear frame stats
        ANAp.noData = true;
        ANAp.channel = 0;
        ANAp.useLocFromCh = -1;
        ANAp.presentData[1] = 0;
        ANAp.presentData[2] = 0;
        ANAp.presentData[0] = 0;
    }

    void setImageScale() {
        orgIpl.getCalibration().frameInterval = ANAp.frameInterval;
        orgIpl.getCalibration().pixelHeight = ANAp.scale;
        orgIpl.getCalibration().pixelWidth = ANAp.scale;
    }

    /**
     * Main method for fluorescence measurements analysis
     */
    private void Ana() {

        Outline o1, s1, s2;

        IJ.showStatus("Running ANA (" + oH.getSize() + " frames)");
        // System.out.println("start frame: " + ANAp.startFrame + ". end frame:
        // ," +ANAp.endFrame);

        for (int f = oH.getStartFrame(); f <= oH.getEndFrame(); f++) { // change
                                                                       // i to
                                                                       // frames
            IJ.log("Frame " + f);
            IJ.showProgress(f, oH.getEndFrame());

            orgIpl.setSlice(f);
            // orgIpr= orgIpl.getProcessor(); // set slice sets the processor
            o1 = oH.getOutline(f);

            s1 = (Outline) o1.clone();
            s2 = (Outline) o1.clone();
            shrink(s2);

            // HACK for Du's embryoImage
            // shrink(s1);
            // s1.scale(14, 0.2);
            // ***

            overlay = new Overlay();
            orgIpl.setOverlay(overlay);
            outerROI = o1.asFloatRoi();
            innerROI = s2.asFloatRoi();
            overlay.setStrokeColor(Color.BLUE);
            overlay.add(outerROI);
            overlay.setStrokeColor(Color.RED);
            overlay.add(innerROI);

            // try{ Thread.sleep(50L);}catch(Exception e){}

            Polygon polyS2 = s2.asPolygon();
            // System.out.println("f: " + f);
            setFluoStats(s1.asPolygon(), polyS2, f);

            // use sample points already there
            if (ANAp.sampleAtSame && ANAp.useLocFromCh != -1) {
                useGivenSamplepoints(o1);
            } else {

                ecmH = new OutlineHandler(1, 2);
                ecmH.setOutline(1, s1);
                ecmH.setOutline(2, s2);

                ecmH = ecmMapping.runByANA(ecmH, orgIpr, ANAp.getCortexWidthPixel());

                // copy flur data to o1 and save
                // some nodes may fail to migrate properly so need to check
                // tracknumbers match
                Vert v = o1.getHead();
                Vert v2 = ecmH.getOutline(2).getHead();

                while (v2.getTrackNum() != v.getTrackNum()) { // check id's
                                                              // match
                    v = v.getNext();
                    if (v.isHead()) {
                        IJ.error("ANA fail");
                        break;
                        // return;
                    }
                }

                int vStart;
                do {
                    // v.fluores[ANAp.channel] = v2.fluores[0].copy(); //
                    // measurments are stored in channel 0 by ECMM
                    v.setFluoresChannel(v2.fluores[0], ANAp.channel);
                    v2 = v2.getNext();
                    if (v2.isHead()) {
                        break;
                    }
                    vStart = v.getTrackNum();
                    // find next vert in o1 that matches v2
                    do {
                        v = v.getNext();
                        v.setFluoresChannel((int) Math.round(v.getX()), (int) Math.round(v.getY()), -1, ANAp.channel); // map
                                                                                                                       // fail
                                                                                                                       // if
                                                                                                                       // -1.
                                                                                                                       // fix
                                                                                                                       // by
                                                                                                                       // interpolation
                        if (vStart == v.getTrackNum()) {
                            System.out.println("ANA fail");
                            return;
                        }
                    } while (v2.getTrackNum() != v.getTrackNum());
                } while (!v2.isHead());

                // o1.getHead().getNext().fluores[ANAp.channel].intensity = -1;
                // // test correction

                interpolateFailures(o1);
            }

            if (ANAp.normalise) {
                normalise2Interior(o1, f);
            }

            outputH.save(o1, f);

            if (f == oH.getEndFrame())
                drawSamplePointsFloat(o1);

            // System.out.println("Nb nodes: "+o1.getVerts() + ", overlay size:
            // "+overlay.size());
            orgIpl.draw();

            // if (i == 12) {
            // break;
            // }
        }

    }

    private void shrink(Outline o) {
        double steps = ANAp.getCortexWidthPixel() / ANAp.stepRes;

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
                    n.setX(n.getX() - ANAp.stepRes * n.getNormal().getX());
                    n.setY(n.getY() - ANAp.stepRes * n.getNormal().getY());
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
                if (vT.getTrackNum() == v.getTrackNum() || vT.getNext().getTrackNum() == v.getTrackNum()) {
                    vT = vT.getNext();
                    continue;
                }
                closest = ExtendedVector2d.PointToSegment(v.getPoint(), vT.getPoint(), vT.getNext().getPoint());
                dis = ExtendedVector2d.lengthP2P(v.getPoint(), closest);
                // System.out.println("dis: " + dis);
                // dis=1;
                if (dis < ANAp.freezeTh) {
                    edge = ExtendedVector2d.unitVector(vT.getPoint(), vT.getNext().getPoint());
                    link = ExtendedVector2d.unitVector(v.getPoint(), closest);
                    angle = Math.abs(ExtendedVector2d.angle(edge, link));
                    if (angle > Math.PI)
                        angle = angle - Math.PI; // if > 180, shift back around
                                                 // 180
                    angle = angle - 1.5708; // 90 degree shift to centre around
                                            // zero
                    // System.out.println("angle:" + angle);

                    if (angle < ANAp.angleTh && angle > -ANAp.angleTh) {
                        v.frozen = true;
                        // v.getNext().frozen = true;
                        // v.getPrev().frozen = true;
                        vT.frozen = true;
                        vT.getNext().frozen = true;
                        // System.out.println("Dis: "+dis+", angle:"+angle+",
                        // vNum:"+v.getTrackNum()+", vTNum:"+ vT.getTrackNum());
                        // System.out.println("v.x: " + v.getX() + " , v.y: " +
                        // v.getY());
                        // System.out.println("vt.x: "+vT.getX()+",
                        // vt.y:"+vT.getY()+", vt.n.x:"+vT.getNext().getX()+",
                        // vt.n.y:"+vT.getNext().getY());
                        // System.out.println("c.x: " + closest.getX()+", c.y:
                        // "+ closest.getY());
                        // break;
                    }

                }
                vT = vT.getNext();
            } while (!vT.isHead());
            // }
            v = v.getNext();
        } while (!v.isHead());
    }

    // private void markFrozenNodes(Outline o){
    // int x, y;
    // Vect2d norm;
    // PointRoi pr;
    // Vert v = o.getHead();
    // do {
    // if(v.frozen){
    // overlay.setStrokeColor(Color.RED);
    // norm = new Vect2d(v.getX(), v.getY());
    // norm.addVec(v.getNormal());
    //
    // x = (int) v.getX();
    // y = (int) v.getY();
    // pr = new PointRoi(x, y);
    // overlay.add(pr);
    // }
    //
    // v = v.getNext();
    // } while (!v.isHead());
    // }

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

    /*
     * private void drawOutlineAsSelection(Outline o) { orgIpl.setRoi(new
     * PolygonRoi(o.asPolygon(), Roi.POLYGON)); //orgIpr =
     * orgStack.getProcessor(1); //orgIpr.setRoi(poly);
     * //orgIpl.updateAndDraw(); }
     */

    // private void setFluoStatsScaled(Polygon outerPoly, Polygon innerPoly, int
    // f) {
    //
    // //PolygonRoi outerRoi = new PolygonRoi(outerPoly, Roi.POLYGON);
    // //PolygonRoi innerRoi = new PolygonRoi(innerPoly, Roi.POLYGON);
    //
    // int store = f - ANAp.startFrame; // frame to index
    // //System.out.println("store: " + store);
    // fluoStats[store].frame = f;
    //
    // //orgIpl.setRoi(outerRoi);
    // orgIpr.setRoi(outerPoly);
    // ImageStatistics is = ImageStatistics.getStatistics(orgIpr, m, null); //
    // this does NOT scale to image
    //
    // fluoStats[store].channels[ANAp.channel].totalFluor = is.mean * is.area;
    // fluoStats[store].channels[ANAp.channel].meanFluor =
    // fluoStats[store].channels[ANAp.channel].totalFluor /
    // fluoStats[store].area;
    //
    // orgIpr.setRoi(innerPoly);
    // is = ImageStatistics.getStatistics(orgIpr, m, null);
    // fluoStats[store].channels[ANAp.channel].innerArea =
    // Tool.areaToScale(is.area, ANAp.scale);
    // fluoStats[store].channels[ANAp.channel].totalInnerFluor = is.mean *
    // is.area;
    // fluoStats[store].channels[ANAp.channel].meanInnerFluor =
    // fluoStats[store].channels[ANAp.channel].totalInnerFluor /
    // fluoStats[store].channels[ANAp.channel].innerArea;
    //
    //
    // fluoStats[store].channels[ANAp.channel].cortexArea =
    // fluoStats[store].area -
    // fluoStats[store].channels[ANAp.channel].innerArea;
    // fluoStats[store].channels[ANAp.channel].totalCorFluo =
    // fluoStats[store].channels[ANAp.channel].totalFluor -
    // fluoStats[store].channels[ANAp.channel].totalInnerFluor;
    // fluoStats[store].channels[ANAp.channel].meanCorFluo =
    // fluoStats[store].channels[ANAp.channel].totalCorFluo /
    // fluoStats[store].channels[ANAp.channel].cortexArea;
    //
    // fluoStats[store].channels[ANAp.channel].percCortexFluo =
    // (fluoStats[store].channels[ANAp.channel].totalCorFluo /
    // fluoStats[store].channels[ANAp.channel].totalFluor) * 100;
    // fluoStats[store].channels[ANAp.channel].cortexWidth =
    // ANAp.getCortexWidthScale();
    // }

    private void setFluoStats(Polygon outerPoly, Polygon innerPoly, int f) {

        // PolygonRoi outerRoi = new PolygonRoi(outerPoly, Roi.POLYGON);
        // PolygonRoi innerRoi = new PolygonRoi(innerPoly, Roi.POLYGON);

        int store = f - ANAp.startFrame; // frame to index
        // System.out.println("store: " + store);
        fluoStats[store].frame = f;

        // orgIpl.setRoi(outerRoi);
        orgIpr.setRoi(outerPoly);
        ImageStatistics is = ImageStatistics.getStatistics(orgIpr, m, null); // this
                                                                             // does
                                                                             // NOT
                                                                             // scale
                                                                             // to
                                                                             // image

        double outerAreaRaw = is.area;
        fluoStats[store].channels[ANAp.channel].totalFluor = is.mean * is.area;
        fluoStats[store].channels[ANAp.channel].meanFluor = is.mean; // fluoStats[store].channels[ANAp.channel].totalFluor
                                                                     // /
                                                                     // fluoStats[store].area;

        orgIpr.setRoi(innerPoly);
        is = ImageStatistics.getStatistics(orgIpr, m, null);

        fluoStats[store].channels[ANAp.channel].innerArea = Tool.areaToScale(is.area, ANAp.scale);
        fluoStats[store].channels[ANAp.channel].totalInnerFluor = is.mean * is.area;
        fluoStats[store].channels[ANAp.channel].meanInnerFluor = is.mean; // fluoStats[store].channels[ANAp.channel].totalInnerFluor
                                                                          // /
                                                                          // fluoStats[store].channels[ANAp.channel].innerArea;

        fluoStats[store].channels[ANAp.channel].cortexArea = fluoStats[store].area
                - fluoStats[store].channels[ANAp.channel].innerArea; // scaled
        fluoStats[store].channels[ANAp.channel].totalCorFluo = fluoStats[store].channels[ANAp.channel].totalFluor
                - fluoStats[store].channels[ANAp.channel].totalInnerFluor;
        fluoStats[store].channels[ANAp.channel].meanCorFluo = fluoStats[store].channels[ANAp.channel].totalCorFluo
                / (outerAreaRaw - is.area); // not scaled

        fluoStats[store].channels[ANAp.channel].percCortexFluo = (fluoStats[store].channels[ANAp.channel].totalCorFluo
                / fluoStats[store].channels[ANAp.channel].totalFluor) * 100;
        fluoStats[store].channels[ANAp.channel].cortexWidth = ANAp.getCortexWidthScale();
    }

    private void normalise2Interior(Outline o, int f) {
        // interior mean fluorescence is used to normalse membrane measurments
        int store = f - ANAp.startFrame; // frame to index
        Vert v = o.getHead();
        do {
            // System.out.print("normalise: " +
            // v.fluores[ANAp.channel].intensity);
            v.fluores[ANAp.channel].intensity = v.fluores[ANAp.channel].intensity
                    / fluoStats[store].channels[ANAp.channel].meanInnerFluor;
            // System.out.print(" to " + v.fluores[ANAp.channel].intensity);
            // System.out.print(", by mean fluo: " +
            // fluoStats[store].channels[ANAp.channel].meanInnerFluor + "\n");
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

        ANAp.presentData = new int[3];
        ANAp.noData = true;

        Vert v = o.getHead();
        for (int i = 0; i < 3; i++) {
            if (v.fluores[i].intensity == -2) { // no data
                ANAp.presentData[i] = 0;
                if (firstEmptyCh == -1) {
                    firstEmptyCh = i;
                }
            } else {
                ANAp.presentData[i] = 1;
                IJ.log("Data exists in channel " + (i + 1));
                ANAp.noData = false;
                if (firstFullCh == -1) {
                    firstFullCh = i;
                }
                ANAp.setCortextWidthScale(fluoStats[0].channels[i].cortexWidth);
            }
        }

        if (Tool.sumArray(ANAp.presentData) == 3)
            firstEmptyCh = 0;

        if (ANAp.noData) {
            ANAp.channel = 0;
            IJ.log("No previous sample points available.");
            ANAp.useLocFromCh = -1;
        } else {
            ANAp.channel = firstEmptyCh;
            IJ.log("Sample points from channel " + (firstFullCh + 1) + " available.");
            ANAp.useLocFromCh = firstFullCh;
        }
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
            if (v.fluores[ANAp.channel].intensity == -1) {
                IJ.log("\tInterpolated failed node intensity (position: " + v.coord + ")");
                // failed to map - interpolate with last/next successful

                last = v.getPrev();
                firstID = last.getTrackNum();
                while (last.fluores[ANAp.channel].intensity == -1) {
                    last = last.getPrev();
                    if (last.getTrackNum() == firstID) {
                        IJ.log("Could not interpolate as all nodes failed");
                        v.fluores[ANAp.channel].intensity = 0;
                        fail = true;
                    }
                }

                nex = v.getNext();
                firstID = nex.getTrackNum();
                while (nex.fluores[ANAp.channel].intensity == -1) {
                    nex = nex.getNext();
                    if (nex.getTrackNum() == firstID) {
                        IJ.log("Could not interpolate as all nodes failed");
                        v.fluores[ANAp.channel].intensity = 0;
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
                intensityDiff = (nex.fluores[ANAp.channel].intensity - last.fluores[ANAp.channel].intensity) * ratio;
                v.fluores[ANAp.channel].intensity = last.fluores[ANAp.channel].intensity + intensityDiff;
                if (v.fluores[ANAp.channel].intensity < 0 || v.fluores[ANAp.channel].intensity > 255) {
                    IJ.log("Error. Interpolated intensity out of range. Set to zero.");
                    v.fluores[ANAp.channel].intensity = 0;
                }

                // IJ.log("n.prev: "+last.fluores[ANAp.channel].intensity +",
                // n.next:" + nex.fluores[ANAp.channel].intensity + ", new
                // inten: " + v.fluores[ANAp.channel].intensity);
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

    private void drawSamplePointsFloat(Outline o) {
        float x, y;
        PointRoi pr;
        Vert v = o.getHead();
        do {
            x = (float) v.fluores[ANAp.channel].x;
            y = (float) v.fluores[ANAp.channel].y;
            pr = new PointRoi(x + 0.5, y + 0.5);
            overlay.add(pr);
            v = v.getNext();
        } while (!v.isHead());
    }

    private void useGivenSamplepoints(Outline o1) {
        int x, y;

        Vert v = o1.getHead();
        do {
            x = (int) v.fluores[ANAp.useLocFromCh].x;
            y = (int) v.fluores[ANAp.useLocFromCh].y;

            v.fluores[ANAp.channel].intensity = sampleFluo(x, y);
            v.fluores[ANAp.channel].x = x;
            v.fluores[ANAp.channel].y = y;
            v = v.getNext();
        } while (!v.isHead());

    }

    private double sampleFluo(int x, int y) {
        double tempFlu = orgIpr.getPixelValue(x, y) + orgIpr.getPixelValue(x - 1, y) + orgIpr.getPixelValue(x + 1, y)
                + orgIpr.getPixelValue(x, y - 1) + orgIpr.getPixelValue(x, y + 1) + orgIpr.getPixelValue(x - 1, y - 1)
                + orgIpr.getPixelValue(x + 1, y + 1) + orgIpr.getPixelValue(x + 1, y - 1)
                + orgIpr.getPixelValue(x - 1, y + 1);
        tempFlu = tempFlu / 9d;
        return tempFlu;
    }
}

class FluoStats {

    int frame = -1;
    double area = -1;
    ExtendedVector2d centroid;
    double elongation = -1;
    double circularity = -1;
    double perimiter = -1;
    double displacement = -1;
    double dist = -1;
    double persistance = -1;
    double speed = -1; // over 1 frame
    double persistanceToSource = -1;
    double dispersion = -1;
    double extension = -1;
    ChannelStat[] channels;

    public FluoStats() {
        centroid = new ExtendedVector2d();
        channels = new ChannelStat[3];
        channels[0] = new ChannelStat();
        channels[1] = new ChannelStat();
        channels[2] = new ChannelStat();
    }

    public static void write(FluoStats[] s, File OUTFILE) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(OUTFILE), true); // auto
                                                                             // flush
            IJ.log("Writing to file");
            pw.print("#p2\n#QuimP ouput - " + OUTFILE.getAbsolutePath() + "\n");
            pw.print(
                    "# Centroids are given in pixels.  Distance & speed & area measurements are scaled to micro meters\n");
            pw.print("# Scale: " + ANAp.scale + " micro meter per pixel | Frame interval: " + ANAp.frameInterval
                    + " sec\n");
            pw.print("# Frame,X-Centroid,Y-Centroid,Displacement,Dist. Traveled,"
                    + "Directionality,Speed,Perimeter,Elongation,Circularity,Area");

            for (int i = 0; i < s.length; i++) {
                pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].centroid.getX(), 2) + ","
                        + IJ.d2s(s[i].centroid.getY(), 2) + "," + IJ.d2s(s[i].displacement) + "," + IJ.d2s(s[i].dist)
                        + "," + IJ.d2s(s[i].persistance) + "," + IJ.d2s(s[i].speed) + "," + IJ.d2s(s[i].perimiter) + ","
                        + IJ.d2s(s[i].elongation) + "," + IJ.d2s(s[i].circularity, 3) + "," + IJ.d2s(s[i].area));
            }
            pw.print("\n#\n# Fluorescence measurements");
            writeFluo(s, pw, 0);
            writeFluo(s, pw, 1);
            writeFluo(s, pw, 2);
            pw.close();
        } catch (Exception e) {
            IJ.log("Could not open out file");
            return;
        }
    }

    private static void writeFluo(FluoStats[] s, PrintWriter pw, int c) throws Exception {
        pw.print("\n#\n# Channel " + (c + 1)
                + ";Frame, Total Fluo.,Mean Fluo.,Cortex Width, Cyto. Area,Total Cyto. Fluo., Mean Cyto. Fluo.,"
                + "Cortex Area,Total Cortex Fluo., Mean Cortex Fluo., %age Cortex Fluo.");
        for (int i = 0; i < s.length; i++) {
            pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].channels[c].totalFluor) + ","
                    + IJ.d2s(s[i].channels[c].meanFluor) + "," + IJ.d2s(s[i].channels[c].cortexWidth));
            pw.print("," + IJ.d2s(s[i].channels[c].innerArea) + "," + IJ.d2s(s[i].channels[c].totalInnerFluor) + ","
                    + IJ.d2s(s[i].channels[c].meanInnerFluor));
            pw.print("," + IJ.d2s(s[i].channels[c].cortexArea) + "," + IJ.d2s(s[i].channels[c].totalCorFluo) + ","
                    + IJ.d2s(s[i].channels[c].meanCorFluo) + "," + IJ.d2s(s[i].channels[c].percCortexFluo));
        }
    }

    public static FluoStats[] read(File INFILE) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(INFILE));
            String thisLine;
            int i = 0;
            // count the number of frames in .scv file
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.startsWith("# Fluorescence measurements")) {
                    break;
                }
                if (thisLine.startsWith("#")) {
                    continue;
                }
                // System.out.println(thisLine);
                i++;
            }
            br.close();
            FluoStats[] stats = new FluoStats[i];

            i = 0;
            String[] split;
            br = new BufferedReader(new FileReader(INFILE)); // re-open and read
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.startsWith("# Channel")) { // reached fluo stats
                    break;
                }
                if (thisLine.startsWith("#")) {
                    continue;
                }
                // System.out.println(thisLine);

                split = thisLine.split(",");

                stats[i] = new FluoStats();
                stats[i].frame = (int) Tool.s2d(split[0]);
                stats[i].centroid.setXY(Tool.s2d(split[1]), Tool.s2d(split[2]));
                stats[i].displacement = Tool.s2d(split[3]);
                stats[i].dist = Tool.s2d(split[4]);
                stats[i].persistance = Tool.s2d(split[5]);
                stats[i].speed = Tool.s2d(split[6]);
                stats[i].perimiter = Tool.s2d(split[7]);
                stats[i].elongation = Tool.s2d(split[8]);
                stats[i].circularity = Tool.s2d(split[9]);
                stats[i].area = Tool.s2d(split[10]);

                i++;
            }

            readChannel(0, stats, br);
            readChannel(1, stats, br);
            readChannel(2, stats, br);

            br.close();
            return stats;

        } catch (IOException e) {
            System.err.println("Could not read file: " + e);
            IJ.error("Could not read file: " + e);
        }

        return new FluoStats[1];

    }

    private static void readChannel(int c, FluoStats[] stats, BufferedReader br) throws IOException {
        String thisLine;
        String[] split;
        int i = 0;
        while ((thisLine = br.readLine()) != null) {
            if (thisLine.startsWith("# Channel")) {
                break;
            }
            if (thisLine.startsWith("#")) {
                continue;
            }

            split = thisLine.split(",");
            // split[0] == frame
            stats[i].channels[c].totalFluor = Tool.s2d(split[1]);
            stats[i].channels[c].meanFluor = Tool.s2d(split[2]);
            stats[i].channels[c].cortexWidth = Tool.s2d(split[3]);
            stats[i].channels[c].innerArea = Tool.s2d(split[4]);
            stats[i].channels[c].totalInnerFluor = Tool.s2d(split[5]);
            stats[i].channels[c].meanInnerFluor = Tool.s2d(split[6]);
            stats[i].channels[c].cortexArea = Tool.s2d(split[7]);
            stats[i].channels[c].totalCorFluo = Tool.s2d(split[8]);
            stats[i].channels[c].meanCorFluo = Tool.s2d(split[9]);
            stats[i].channels[c].percCortexFluo = Tool.s2d(split[10]);

            i++;
        }
    }

    void clearFluo() {
        this.channels[0] = new ChannelStat();
        this.channels[1] = new ChannelStat();
        this.channels[2] = new ChannelStat();
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

/**
 * Container class for parameters concerned with ANA analysis
 * 
 * @author rtyson
 *
 */
class ANAp {

    static public File INFILE;
    static public File OUTFILE;
    static public File STATSFILE;
    static public File FLUOFILE;
    static private double cortexWidthPixel; // in pixels
    static private double cortexWidthScale; // at scale
    static public double stepRes = 0.04; // step size in pixels
    static public double freezeTh = 1;
    static public double angleTh = 0.1;
    static public double oneFrameRes = 1;
    static public double scale;
    static public double frameInterval;
    static public int startFrame, endFrame;
    static boolean normalise = true;
    static boolean sampleAtSame = false;
    static int[] presentData;
    static boolean cleared;
    static boolean noData;
    static int channel;
    static int useLocFromCh;

    public ANAp() {
    }

    /**
     * Initiates ANAp class with parameters copied from BOA analysis
     * 
     * @param qp
     *            reference to QParams container (master file and BOA params)
     */
    static void setup(QParams qp) {
        INFILE = qp.snakeQP;
        OUTFILE = new File(INFILE.getAbsolutePath()); // output file (.snQP)
                                                      // file
        STATSFILE = new File(qp.statsQP.getAbsolutePath()); // output file
                                                            // (.stQP.csv) file
        scale = qp.imageScale;
        frameInterval = qp.frameInterval;
        ANAp.setCortextWidthScale(qp.cortexWidth);
        startFrame = qp.startFrame;
        endFrame = qp.endFrame;
        channel = 0;
        cleared = false;
        noData = true;
    }

    static void setCortextWidthScale(double c) {
        cortexWidthScale = c;
        cortexWidthPixel = Tool.distanceFromScale(cortexWidthScale, scale);
    }

    static double getCortexWidthScale() {
        return cortexWidthScale;
    }

    static double getCortexWidthPixel() {
        return cortexWidthPixel;
    }
}
