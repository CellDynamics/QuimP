package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.YesNoCancelDialog;
import ij.io.OpenDialog;
// import ij.process.ColorProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 *
 * @author rtyson
 */
public class Q_Analysis {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Q_Analysis.class.getName());
    GenericDialog gd;
    OutlineHandler oH;
    QParams qp;
    private final String[] headActions = { "Remain", "Use from BOA" }; //!< Define possible action strings 

    /**
     * Main constructor and runner - class entry point
     * 
     * Left in this form for backward compatibility
     */
    public Q_Analysis() {
        this(null);
    }

    /**
     * Parametrized constructor for tests
     * 
     * @param path Path to *.paQP file. If \c null user is asked for this file
     */
    public Q_Analysis(Path path) {
        try {
            IJ.showStatus("QuimP Analysis");
            IJ.log(new Tool().getQuimPversion());
            String directory; // directory with paQP
            String filename; // file name of paQP

            if (path == null) { // no file provided, ask user
                OpenDialog od = new OpenDialog("Open paramater file (.paQP)...",
                        OpenDialog.getLastDirectory(), "");
                if (od.getFileName() == null) {
                    IJ.log("Cancelled - exiting...");
                    return;
                }
                directory = od.getDirectory();
                filename = od.getFileName();
            } else // use name provided
            {
                // getParent can return null
                directory = path.getParent() == null ? "" : path.getParent().toString();
                filename = path.getFileName() == null ? "" : path.getFileName().toString();
                LOGGER.debug("Use provided file:" + directory + " " + filename);
            }
            File paramFile = new File(directory, filename); // paQP file
            qp = new QParams(paramFile); // initialize general param storage
            qp.readParams(); // create associated files included in paQP and read params
            Qp.setup(qp); // copy selected data from general QParams to local storage

            if (!run()) // run everything
            {
                LOGGER.warn("Q_Analysis stopped on error or it has been cancelled");
                return; // end on run fail
            }

            File[] otherPaFiles = qp.findParamFiles(); // check whether are other paQP files

            if (otherPaFiles.length > 0) { // and process them if they are (that pointed by
                                           // user is skipped)
                YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Batch Process?",
                        "\tBatch Process?\n\n"
                                + "Process other paQP files in the same folder with QAnalysis?"
                                + "\n[The same parameters will be used]");
                if (yncd.yesPressed()) {
                    ArrayList<String> runOn = new ArrayList<String>(otherPaFiles.length);
                    this.closeAllImages();

                    // if user agreed iterate over found files
                    // (except that loaded explicitly by user)
                    for (int j = 0; j < otherPaFiles.length; j++) {
                        IJ.log("Running on " + otherPaFiles[j].getAbsolutePath());
                        paramFile = otherPaFiles[j];
                        qp = new QParams(paramFile);
                        qp.readParams();
                        Qp.setup(qp);
                        Qp.useDialog = false;
                        if (!run()) {
                            LOGGER.warn("Q_Analysis stopped on error or it has been cancelled");
                            return;
                        }
                        runOn.add(otherPaFiles[j].getName());
                        this.closeAllImages();
                    }
                    IJ.log("\n\nBatch - Successfully ran QAnalysis on:");
                    for (int i = 0; i < runOn.size(); i++) {
                        IJ.log(runOn.get(i));
                    }
                } else {
                    return; // no batch processing
                }
            }

            IJ.log("QuimP Analysis complete");
            IJ.showStatus("Finished");
        } catch (QuimpException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Main runner - do all calculations
     * 
     * @return \c true when run or \c false when stopped by user (canceled) or on error
     */
    private boolean run() {

        oH = new OutlineHandler(qp);
        if (!oH.readSuccess) {
            return false;
        }

        if (oH.getSize() == 1) {
            Qp.singleImage = true;
            // only one frame - re lable node indices
            oH.getOutline(1).resetAllCoords();
        }
        // oH.writeOutlines(new File( qp.getParamFile().getAbsolutePath() +
        // ".tempSNQP"));

        if (Qp.useDialog) {
            if (!showDialog()) { // if user cancelled dialog
                return false; // do nothing
            }
        }

        Qp.convexityToPixels();

        Qp.mapPixelHeight = 1;
        Qp.mapPixelWidth = 1.0d / Qp.mapRes;

        new STmap(oH, Qp.mapRes);

        SVGplotter svgPlotter = new SVGplotter(oH, Qp.fps, Qp.scale, Qp.channel, Qp.outFile);
        svgPlotter.plotTrack(Qp.trackColor, Qp.increment);
        // svgPlotter.plotTrackAnim();
        svgPlotter.plotTrackER(Qp.outlinePlot);

        Qp.convexityToUnits(); // reset the covexity options to units (as they are static)
        return true;
    }

    private boolean showDialog() {
        gd = new GenericDialog("Q Analysis Options", IJ.getInstance());

        gd.setOKLabel("RUN");

        gd.addMessage("Pixel width: " + Qp.scale + " \u00B5m\nFrame Interval: " + Qp.frameInterval
                + " sec");

        gd.addMessage("******* Cell track options (svg) *******");
        gd.addNumericField("Frame increment", Qp.increment, 0);
        gd.addChoice("Colour Map", QColor.colourMaps, QColor.colourMaps[0]);

        gd.addMessage("***** Motility movie options (svg) *****");
        gd.addChoice("Colour using", Qp.outlinePlots, Qp.outlinePlots[0]);

        gd.addMessage("********** Convexity options **********");
        gd.addNumericField("Sum over (\u00B5m)", Qp.sumCov, 2);
        gd.addNumericField("Smooth over (\u00B5m)", Qp.avgCov, 2);

        gd.addMessage("************* Map options *************");
        gd.addNumericField("Map resolution", Qp.mapRes, 0);

        gd.addMessage("************* Head nodes **************");
        gd.addChoice("Heads", headActions, headActions[0]);

        gd.setResizable(false);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return false;
        }

        // Qp.scale = gd.getNextNumber();
        // Qp.setFPS(gd.getNextNumber());
        Qp.increment = (int) gd.getNextNumber();
        Qp.trackColor = gd.getNextChoice();
        Qp.outlinePlot = gd.getNextChoice();
        Qp.sumCov = gd.getNextNumber();
        Qp.avgCov = gd.getNextNumber();
        Qp.mapRes = (int) gd.getNextNumber();
        Qp.headProcessing = gd.getNextChoice();

        return true;
    }

    private void closeAllImages() {
        int[] ids = ij.WindowManager.getIDList();
        for (int i = 0; i < ids.length; i++) {
            ij.WindowManager.getImage(ids[i]).close();
        }
    }
}

/**
 * Create spatial temporal maps from ECMM and ANA data
 *
 * @author rtyson
 */
class STmap {

    double[][] coordMap;
    double[][] originMap, xMap, yMap;
    double[][] migMap;
    int[] migColor;
    float[] migPixels;
    // double[][] fluMap;
    // byte[] fluColor;
    FluoMap[] fluoMaps;
    double[][] convMap; // convexity map
    int[] convColor;
    ImagePlus migImP, fluImP, convImP;
    OutlineHandler oH;
    int res;
    int T;

    public STmap(OutlineHandler o, int r) {
        res = r;
        oH = o;
        T = oH.getSize();

        coordMap = new double[T][res];
        originMap = new double[T][res];
        xMap = new double[T][res]; // interpolated pixel coordinates
        yMap = new double[T][res];

        migMap = new double[T][res];
        migColor = new int[T * res];
        migPixels = new float[T * res];

        // fluMap = new double[T][res];
        // fluColor = new byte[T * res];

        convMap = new double[T][res];
        convColor = new int[T * res];

        // flu maps
        fluoMaps = new FluoMap[3];
        Vert v = oH.indexGetOutline(0).getHead();
        for (int i = 0; i < 3; i++) {
            fluoMaps[i] = new FluoMap(T, res, i + 1);
            System.out.println("flou in v: " + v.fluores[i].intensity);
            if (v.fluores[i].intensity == -2) { // disable if no data
                IJ.log("No fluorescence data for channel " + (i + 1));
                fluoMaps[i].setEnabled(false);
            }
        }

        generate();
        // testQColorBW();
    }

    private void generate() {

        this.calcCurvature();
        Vert zeroVert, v;
        String migColorMap = "rwb";

        double fraction, intMig, intFlu, intConv, target, actualTarget;
        QColor color;
        int pN;
        Vert fHead, cHead;

        double step = 1.0d / res;

        // ------debug----
        // System.out.println("210.in generate: min:"+ oH.migLimits[0]+",
        // max"+oH.migLimits[1]);
        // zeroVert = closestFloor(oH.getOutline(28), 0.9138373074502235, 'f');
        // fHead = findFirstNode(oH.getOutline(28),'f');
        // cHead = findFirstNode(oH.getOutline(28),'c');
        // if(true)return;
        // -------------------------

        double origin = 0; // co-ord for zeroVert to move to next
        int frame;

        for (int tt = 0; tt < T; tt++) {

            frame = tt + oH.getStartFrame();
            // System.out.println("frame " + t);
            pN = tt * res; // pixel index

            // find the first node in terms of coord and fcoord (not the head)
            fHead = oH.getOutline(frame).findFirstNode('f');
            cHead = oH.getOutline(frame).findFirstNode('c');
            // fHead.print();
            // cHead.print();

            if (tt == 0) {
                // for the first time point the head coord node is our starting point
                zeroVert = cHead;
                fraction = 0;
                origin = 0;
            } else {
                // vert closest below zero (zero being tracked over time from
                // frame 1!)
                zeroVert = closestFloor(oH.getOutline(frame), origin, 'f', fHead);
                // System.out.println("zerovert: " + zeroVert.fCoord +", origin:
                // " + origin + ", fHead: " + fHead.fCoord);
                fraction = ffraction(zeroVert, origin, fHead); // position of origin between
                                                               // zeroVert and zeroVert.getNext
                // System.out.println("resulting fraction: " + fraction);

                // System.out.print("\nzeroVert.fCoord:"+zeroVert.coord+",
                // fraction:"+fraction +"\n");
                origin = interpCoord(zeroVert, fraction, cHead); // the new
                                                                 // origin
                // System.out.println("new origin: " + origin);
            }
            target = origin; // coord to fill in map next

            intMig = interpolate(zeroVert.distance, zeroVert.getNext().distance, fraction);
            migMap[tt][0] = intMig;
            color = QColor.ERColorMap2(migColorMap, intMig, oH.migLimits[0], oH.migLimits[1]);
            migColor[pN] = color.getColorInt();
            migPixels[pN] = (float) intMig;

            // fill fluo maps
            for (int i = 0; i < 3; i++) {
                if (fluoMaps[i].isEnabled()) {
                    if (zeroVert.fluores[i].intensity == -2) {
                        IJ.log("ERROR: There are missing fluoresecne values! Run ANA");
                        return;
                    }
                    intFlu = interpolate(zeroVert.fluores[i].intensity,
                            zeroVert.getNext().fluores[i].intensity, fraction);
                    fluoMaps[i].fill(tt, 0, pN, intFlu, oH.fluLims[i][1]);
                }
            }

            /*
             * if (zeroVert.floures == -1) { fluMap[t][0] = 0; fluColor[pN] =
             * (byte) QColor.bwScale(0, 256, oH.maxFlu, 0); } else { intFlu =
             * interpolate(zeroVert.floures, zeroVert.getNext().floures,
             * fraction); fluMap[t][0] = intFlu; fluColor[pN] = (byte)
             * QColor.bwScale(intFlu, 256, oH.maxFlu, 0); }
             */

            intConv = interpolate(zeroVert.curvatureSum, zeroVert.getNext().curvatureSum, fraction);
            convMap[tt][0] = intConv;
            color = QColor.ERColorMap2("rbb", intConv, oH.curvLimits[0], oH.curvLimits[1]);
            convColor[pN] = color.getColorInt();

            coordMap[tt][0] = origin;
            originMap[tt][0] = interpFCoord(zeroVert, fraction, fHead);
            xMap[tt][0] = interpolate(zeroVert.getX(), zeroVert.getNext().getX(), fraction);
            yMap[tt][0] = interpolate(zeroVert.getY(), zeroVert.getNext().getY(), fraction);

            if (target >= 1 || target < 0) {
                System.out.println("target out of range: " + target);
            }

            for (int p = 1; p < res; p++) {
                pN = (tt * res) + p; // pixel index
                target += step;
                actualTarget = (target >= 1) ? target - 1 : target; // wraps
                                                                    // around to
                                                                    // zero
                // System.out.println("\tactualtarget:"+actualTarget);
                coordMap[tt][p] = actualTarget;

                v = closestFloor(oH.getOutline(frame), actualTarget, 'c', cHead); // should
                                                                                  // this
                                                                                  // be
                                                                                  // 'g'?
                fraction = cfraction(v, actualTarget, cHead);

                originMap[tt][p] = interpFCoord(v, fraction, fHead);
                xMap[tt][p] = interpolate(v.getX(), v.getNext().getX(), fraction);
                yMap[tt][p] = interpolate(v.getY(), v.getNext().getY(), fraction);

                intMig = interpolate(v.distance, v.getNext().distance, fraction);
                migMap[tt][p] = intMig;
                color = QColor.ERColorMap2(migColorMap, intMig, oH.migLimits[0], oH.migLimits[1]);
                migColor[pN] = color.getColorInt();
                migPixels[pN] = (float) intMig;

                for (int i = 0; i < 3; i++) {
                    if (fluoMaps[i].isEnabled()) {
                        intFlu = interpolate(v.fluores[i].intensity,
                                v.getNext().fluores[i].intensity, fraction);
                        fluoMaps[i].fill(tt, p, pN, intFlu, oH.fluLims[i][1]);
                    }
                }
                /*
                 * if (zeroVert.floures == -1) { fluMap[t][p] = 0; fluColor[pN]
                 * = (byte) QColor.bwScale(0, 256, oH.maxFlu, 0); } else {
                 * intFlu = interpolate(v.floures, v.getNext().floures,
                 * fraction); fluMap[t][p] = intFlu; fluColor[pN] = (byte)
                 * QColor.bwScale(intFlu, 256, oH.maxFlu, 0); }
                 */

                intConv = interpolate(v.curvatureSum, v.getNext().curvatureSum, fraction);
                convMap[tt][p] = intConv;
                color = QColor.ERColorMap2("rbb", intConv, oH.curvLimits[0], oH.curvLimits[1]);
                convColor[pN] = color.getColorInt();

            }

        }

        migImP = new ImagePlus("motility_map", new ColorProcessor(res, T, migColor));
        convImP = new ImagePlus("convexity_map", new ColorProcessor(res, T, convColor));
        resize(migImP);
        resize(convImP);
        setCalibration(migImP);
        setCalibration(convImP);
        migImP.show();
        convImP.show();
        // fluImP.show();
        // IJ.doCommand("Red");

        if (Qp.Build3D) {
            // create 3D of motility
            STMap3D map3d = new STMap3D(xMap, yMap, migColor);
            map3d.build();
            map3d.toOrigin(oH.indexGetOutline(0).getCentroid());
            map3d.scale(0.05f);
            map3d.write(new File("/temp/cell_02.wrl"));

            // create 3D of curvature
            STMap3D map3dCur = new STMap3D(xMap, yMap, convColor);
            map3dCur.build();
            map3dCur.toOrigin(oH.indexGetOutline(0).getCentroid());
            map3dCur.scale(0.05f);
            map3dCur.write(new File("/temp/cell_02_cur.wrl"));
        }

        // create fluo images

        for (int i = 0; i < 3; i++) {
            if (!fluoMaps[i].isEnabled()) {
                continue;
            }

            fluImP = IJ.createImage(Qp.filename + "_fluoCH" + fluoMaps[i].channel, "8-bit black",
                    res, T, 1);
            fluImP.getProcessor().setPixels(fluoMaps[i].getColours());
            resize(fluImP);
            setCalibration(fluImP);
            fluImP.show();

            try {
                Thread.sleep(500); // needed to let imageJ set the right colour
                                   // maps
            } catch (Exception e) {
            }

            IJ.doCommand("Red"); // this don't always work. dun know why

            IJ.saveAs(fluImP, "tiff", Qp.outFile.getParent() + File.separator + Qp.filename
                    + "_fluoCh" + fluoMaps[i].channel + ".tiff");
        }

        try {
            // save images
            IJ.saveAs(migImP, "tiff",
                    Qp.outFile.getParent() + File.separator + Qp.filename + "_motility.tiff");
            IJ.saveAs(convImP, "tiff",
                    Qp.outFile.getParent() + File.separator + Qp.filename + "_convexity.tiff");

            QuimPArrayUtils.arrayToFile(coordMap, ",",
                    new File(Qp.outFile.getPath() + "_coordMap.maQP"));
            QuimPArrayUtils.arrayToFile(originMap, ",",
                    new File(Qp.outFile.getPath() + "_originMap.maQP"));
            QuimPArrayUtils.arrayToFile(migMap, ",",
                    new File(Qp.outFile.getPath() + "_motilityMap.maQP"));
            QuimPArrayUtils.arrayToFile(convMap, ",",
                    new File(Qp.outFile.getPath() + "_convexityMap.maQP"));
            QuimPArrayUtils.arrayToFile(xMap, ",", new File(Qp.outFile.getPath() + "_xMap.maQP"));
            QuimPArrayUtils.arrayToFile(yMap, ",", new File(Qp.outFile.getPath() + "_yMap.maQP"));

            for (int i = 0; i < 3; i++) {
                if (!fluoMaps[i].isEnabled()) {
                    continue;
                }
                QuimPArrayUtils.arrayToFile(fluoMaps[i].getMap(), ",",
                        new File(Qp.outFile.getPath() + "_fluoCh" + fluoMaps[i].channel + ".maQP"));
            }

        } catch (IOException e) {
            IJ.error("Could not write Map file:\n " + e.getMessage());
        }

        if (QuimPArrayUtils.sumArray(migColor) == 0) {
            IJ.showMessage(
                    "ECMM data is missing (or corrupt), and is needed for building accurate maps.\nPlease run ECMM (fluorescence data will be lost)");
        }
        // test making LUT images
        /*
         * ImagePlus migImPLut = IJ.createImage("mig_32", "32-bit", res, T,1);
         * ImageProcessor ipFloat = new FloatProcessor(res, T, migPixels, null);
         * LUT lut = new LUT(); ipFloat.setLut(lut)
         * migImPLut.setProcessor(ipFloat); resize(migImPLut); migImPLut.show();
         */
    }

    private Vert closestFloor(Outline o, double target, char c, Vert head) {
        // find the vert with coor closest (floored) to target coordinate

        Vert v = head; // the fcoord or cCoord head
        double coord, coordNext;
        do {
            coord = (c == 'f') ? v.fCoord : v.coord;
            coordNext = (c == 'f') ? v.getNext().fCoord : v.getNext().coord;

            if (coord == target) {
                break;
            }
            if (coordNext > target && coord < target) {
                break;
            }

            v = v.getNext();
        } while (v.getNext().getTrackNum() != head.getTrackNum());

        // System.out.println("found fcoord " + v.fCoord);
        return v;
    }

    private double cfraction(Vert v, double target, Vert head) {
        // calc fraction for iterpolation
        double v2coord;
        if (v.getNext().getTrackNum() == head.getTrackNum()) { // passed zero
            v2coord = v.getNext().coord + 1;
            target = (target > v.coord) ? target : target + 1; // not passed
                                                               // zero as all
                                                               // values are
                                                               // passed zero!
        } else {
            v2coord = v.getNext().coord;
        }

        double frac = (target - v.coord) / (v2coord - v.coord);
        // System.out.println("\tffraction:
        // |v:"+v.coord+"|v2:"+v2coord+"|tar:"+target+"|frac:"+frac);
        if (frac >= 1) {
            frac = frac - 1;
            System.out.println("WARNING- frac corrected: " + frac);
        }
        if (frac > 1 || frac < 0) {
            System.out.println("!WARNING, frac out of range:" + frac);
        }
        return frac;
    }

    private double ffraction(Vert v, double target, Vert head) {
        // calc fraction for iterpolation
        double v2coord;
        if (v.getNext().getTrackNum() == head.getTrackNum()) { // passed zero
            // System.out.println("\tffraction: pass zero. wrap");
            v2coord = v.getNext().fCoord + 1;
            target = (target > v.fCoord) ? target : target + 1; // not passed zero as all values are
                                                                // passed zero!
        } else {
            v2coord = v.getNext().fCoord;
        }
        double frac = (target - v.fCoord) / (v2coord - v.fCoord);
        // System.out.println("\tffraction:
        // |v:"+v.fCoord+"|v2:"+v2coord+"|tar:"+target+"|frac:"+frac);

        if (frac >= 1) {
            frac = frac - 1;
            System.out.println("WARNING- frac corrected: " + frac);
        }
        if (frac > 1 || frac < 0) {
            System.out.println("!WARNING, frac out of range:" + frac);
        }

        if (Double.isNaN(frac) || Double.isNaN(frac)) {
            System.out.println("!WARNING, frac is nan:" + frac);
            System.out.println("\tffraction: |v:" + v.fCoord + "|v2:" + v2coord + "|tar:" + target
                    + "|frac:" + frac);
            frac = 0.5;
        }
        return frac;
    }

    private double interpCoord(Vert v, double frac, Vert head) {
        double v2Coord = (v.getNext().getTrackNum() == head.getTrackNum()) ? v.getNext().coord + 1
                : v.getNext().coord; // pass
                                     // zero
        double dis = v2Coord - v.coord;
        double targ = v.coord + (dis * frac);

        if (targ >= 1) {
            targ += -1; // passed zero
        }

        if (targ < 0) {
            System.out.println("ERROR: target less than zero (Q_Analysis:interpCoord)");
        }

        return targ;
    }

    private double interpFCoord(Vert v, double frac, Vert head) {
        double v2Coord = (v.getNext().getTrackNum() == head.getTrackNum()) ? v.getNext().fCoord + 1
                : v.getNext().fCoord;
        double dis = v2Coord - v.fCoord;
        double targ = v.fCoord + (dis * frac);

        if (targ >= 1) {
            targ += -1; // passed zero
        }

        if (targ < 0) {
            System.out.println("ERROR: target less than zero (Q_Analysis:interpFCoord)");
        }

        return targ;
    }

    private double interpolate(double v1, double v2, double frac) {
        return v1 + ((v2 - v1) * frac);
    }

    // private void testQColorBW() {
    // /*
    // * fluImP = IJ.createImage("Fluorescence Map", "8-bit white", 10,10,1);
    // * ImageProcessor ip = fluImP.getProcessor(); byte[] pixels =
    // * (byte[])ip.getPixels(); for(int j = 0; j < pixels.length ; j++){
    // * System.out.println("pixel " + j+": "+pixels[j]); }
    // */
    // double max = 10;
    // double step = max / 255;
    // byte b;
    // for (int i = 0; i < 255; i++) {
    // System.out.print("in val: " + i * step);
    // b = (byte) QColor.bwScale(i * step, 256, max, 0);
    // System.out.println(", b: " + b);
    // }
    //
    // }

    private void calcCurvature() {
        // returns the min and max values
        // calcualtes convexity by smoothing or averaging across nodes

        Outline o;
        Vert v;

        oH.curvLimits = new double[2];

        for (int f = oH.getStartFrame(); f <= oH.getEndFrame(); f++) {
            o = oH.getOutline(f);
            if (o == null) {
                IJ.log("ERROR: Outline at frame " + f + " is empty");
                continue;
            }

            // update local curvature just in case
            o.updateCurvature();

            // set default curvatures
            v = o.getHead();
            do {
                v.curvatureSmoothed = v.curvatureLocal;
                v.curvatureSum = v.curvatureLocal;
                v = v.getNext();
            } while (!v.isHead());

            averageCurvature(o);
            sumCurvature(o);

            // find min and max of sum curvature

            v = o.getHead();
            if (f == oH.getStartFrame()) {
                oH.curvLimits[1] = v.curvatureSum;
                oH.curvLimits[0] = v.curvatureSum;
            }
            do {
                if (v.curvatureSum > oH.curvLimits[1]) {
                    oH.curvLimits[1] = v.curvatureSum;
                }
                if (v.curvatureSum < oH.curvLimits[0]) {
                    oH.curvLimits[0] = v.curvatureSum;
                }
                v = v.getNext();
            } while (!v.isHead());
            // System.out.println("Min curv: " + oH.curvLimits[0] + ", max curv:
            // " + oH.curvLimits[1]);

        }

        // System.out.println("curve limits before: " + oH.curvLimits[0] + ", "
        // + oH.curvLimits[1]);
        oH.curvLimits = Tool.setLimitsEqual(oH.curvLimits);
        // System.out.println("curve limits after: " + oH.curvLimits[0] + ", " +
        // oH.curvLimits[1]);
    }

    private void averageCurvature(Outline o) {

        Vert v, tmpV;
        double totalCur, distance;
        int count;

        // avertage over curvatures
        if (Qp.avgCov > 0) {
            // System.out.println("new outline");
            v = o.getHead();
            do {
                // System.out.println("\tnew vert");
                totalCur = v.curvatureLocal; // reset
                count = 1;

                // add up curvatures of prev nodes
                // System.out.println("\t prev nodes");
                tmpV = v.getPrev();
                distance = 0;
                do {
                    distance +=
                            ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
                    totalCur += tmpV.curvatureLocal;
                    count++;
                    tmpV = tmpV.getPrev();
                } while (distance < Qp.avgCov / 2);

                // add up curvatures of next nodes
                distance = 0;
                tmpV = v.getNext();
                do {
                    distance +=
                            ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
                    totalCur += tmpV.curvatureLocal;
                    count++;
                    tmpV = tmpV.getNext();
                } while (distance < Qp.avgCov / 2);

                v.curvatureSmoothed = totalCur / count;

                v = v.getNext();
            } while (!v.isHead());
        }
    }

    private void sumCurvature(Outline o) {
        // sum smoothed curavture over a region of the membrane

        Vert v, tmpV;
        double totalCur, distance;
        // avertage over curvatures
        if (Qp.sumCov > 0) {
            System.out.println("summing curv");
            v = o.getHead();
            do {
                // System.out.println("\tnew vert");
                totalCur = v.curvatureSmoothed; // reset
                // add up curvatures of prev nodes
                // System.out.println("\t prev nodes");
                tmpV = v.getPrev();
                distance = 0;
                do {
                    distance +=
                            ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
                    totalCur += tmpV.curvatureSmoothed;
                    tmpV = tmpV.getPrev();
                } while (distance < Qp.sumCov / 2);

                // add up curvatures of next nodes
                distance = 0;
                tmpV = v.getNext();
                do {
                    distance +=
                            ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
                    totalCur += tmpV.curvatureSmoothed;
                    tmpV = tmpV.getNext();
                } while (distance < Qp.sumCov / 2);

                v.curvatureSum = totalCur;

                v = v.getNext();
            } while (!v.isHead());
        }

    }

    @SuppressWarnings("unused")
    @Deprecated
    private Vert findFirstNodeX(Outline o, char c) {
        // find the first node in terms of coord and fcoord
        // ie closest to zero

        Vert v = o.getHead();
        Vert vFirst = v;

        double coord, coordPrev;
        double dis, disFirst = 0;

        do {
            coord = (c == 'f') ? v.fCoord : v.coord;
            coordPrev = (c == 'f') ? v.getPrev().fCoord : v.getPrev().coord;

            dis = Math.abs(coord - coordPrev);
            // System.out.println("abs( " + coord + "-"+coordPrev+") = " + dis);

            if (dis > disFirst) {
                vFirst = v;
                disFirst = dis;
                // System.out.println("\tchoosen ");
                // vFirst.print();
            }

            v = v.getNext();
        } while (!v.isHead());

        // System.out.println("First "+c+"Coord vert: ");
        // vFirst.print();

        return vFirst;

    }

    private void resize(ImagePlus ImP) {
        if (oH.getSize() >= res * 0.9) {
            return; // don't resize if its going to compress frames
        }
        ImageProcessor ip = ImP.getProcessor();

        // if (Qp.singleImage) {
        ip.setInterpolationMethod(ImageProcessor.NONE);
        // } else {
        // ip.setInterpolationMethod(ImageProcessor.BILINEAR);
        // }

        double vertRes = Math.ceil((double) res / (double) oH.getSize());
        // System.out.println("OH s: " + oH.getSize() + ",vertres: "+ vertRes);
        Qp.mapPixelHeight = 1.0d / vertRes;
        vertRes = oH.getSize() * vertRes;

        // System.out.println("OH s: " + oH.getSize() + ",vertres: "+ vertRes);

        ip = ip.resize(res, (int) vertRes);
        ImP.setProcessor(ip);

    }

    private void setCalibration(ImagePlus ImP) {
        ImP.getCalibration().setUnit("frames");
        ImP.getCalibration().pixelHeight = Qp.mapPixelHeight;
        ImP.getCalibration().pixelWidth = Qp.mapPixelWidth;
    }
}

/**
 * Configuration class for Q_Analysis
 * 
 * @author rtyson
 *
 */
class Qp {

    static public File snQPfile;
    static public File stQPfile;
    static public File outFile;
    static public String filename;
    static public double scale = 1; // pixel size in microns
    static public double frameInterval = 1; // frames per second
    static int startFrame, endFrame;
    static public double fps = 1; // frames per second
    static public int increment = 1;
    static public String trackColor;
    static public String[] outlinePlots = { "Speed", "Fluorescence", "Convexity" };
    static public String outlinePlot;
    static public double sumCov = 1;
    static public double avgCov = 0;
    static public int mapRes = 400;
    static public int channel = 0;
    static boolean singleImage = false;
    static double mapPixelHeight = 1;
    static double mapPixelWidth = 1;
    static boolean useDialog = true;
    static public String headProcessing; //!< Head processing algorithm. Define how to treat head position */
    final static boolean Build3D = false;

    static void convexityToPixels() {
        avgCov /= scale; // convert to pixels
        sumCov /= scale;
    }

    static void convexityToUnits() {
        avgCov *= scale; // convert to pixels
        sumCov *= scale;
    }

    public Qp() {
    }

    /**
     * Copies selected data from QParams to this object
     * 
     * @param qp General QuimP parameters object
     */
    static void setup(QParams qp) {
        Qp.snQPfile = qp.snakeQP;
        Qp.scale = qp.getImageScale();
        Qp.frameInterval = qp.getFrameInterval();
        Qp.filename = Tool.removeExtension(Qp.snQPfile.getName());
        Qp.outFile = new File(Qp.snQPfile.getParent() + File.separator + Qp.filename);
        Qp.startFrame = qp.getStartFrame();
        Qp.endFrame = qp.getEndFrame();
        fps = 1d / frameInterval;
        singleImage = false;
        useDialog = true;
    }
}
