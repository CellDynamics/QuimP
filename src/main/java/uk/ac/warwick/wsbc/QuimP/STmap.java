package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Create spatial temporal maps from ECMM and ANA data
 * <p>
 * This class can be serialized but only as container of maps. Data required for creation
 * of those maps are not serialized, thus restored object is not fully functional. As this is 
 * last step in QuimP workflow it may not be necessary to load this json anymore.
 * 
 * @author rtyson
 */
public class STmap implements IQuimpSerialize {
    private static final Logger LOGGER = LogManager.getLogger(STmap.class.getName());
    /**
     * Coordinates map.
     */
    public double[][] coordMap;
    public double[][] originMap, xMap, yMap;
    /**
     * Motility map.
     */
    public double[][] motMap;
    transient int[] migColor;
    transient float[] migPixels;
    // double[][] fluMap;
    // byte[] fluColor;
    public FluoMap[] fluoMaps;
    /**
     * Convexity map.
     */
    public double[][] convMap;
    transient int[] convColor;
    transient ImagePlus migImP, fluImP, convImP;
    /**
     * Contain OutlineHandler used for generating maps
     * <b>warning</b><p>
     * It is not serialized
     */
    transient OutlineHandler oH;
    /**
     * Resolution of maps.
     * <p>
     * This field together with <tt>T</tt> stands for the dimensions of 2D arrays for storing maps.
     * For this reason they are serialized.
     */
    private int res;
    /**
     * Number of outlines.
     */
    private int T;
    private double mapPixelHeight = 1;
    private double mapPixelWidth = 1;

    /**
     * Default constructor to satisfy GSon builder. Should not be used for proper object 
     * initialization
     */
    public STmap() {

    }

    /**
     * Copy constructor. 
     * 
     * @param src source object
     * <b>warning</b><p>
     * Make a copy of serializable fields only
     */
    public STmap(final STmap src) {
        this.coordMap = QuimPArrayUtils.copy2darray(src.coordMap, null);
        this.originMap = QuimPArrayUtils.copy2darray(src.originMap, null);
        this.xMap = QuimPArrayUtils.copy2darray(src.xMap, null);
        this.yMap = QuimPArrayUtils.copy2darray(src.yMap, null);
        this.motMap = QuimPArrayUtils.copy2darray(src.motMap, null);
        this.convMap = QuimPArrayUtils.copy2darray(src.convMap, null);
        this.res = src.res;
        this.T = src.T;
        this.mapPixelHeight = src.mapPixelHeight;
        this.mapPixelWidth = src.mapPixelWidth;
        this.fluoMaps = new FluoMap[src.fluoMaps.length];
        for (int i = 0; i < src.fluoMaps.length; i++)
            this.fluoMaps[i] = new FluoMap(src.fluoMaps[i]);

    }

    /**
     * Build object for given:
     * 
     * @param o Outline from ECMM
     * @param r Map resolution in pixels
     * @see uk.ac.warwick.wsbc.QuimP.Qp
     */
    public STmap(OutlineHandler o, int r) {
        mapPixelHeight = 1;
        mapPixelWidth = 1.0d / r;
        res = r;
        oH = o;
        T = oH.getSize();

        coordMap = new double[T][res];
        originMap = new double[T][res];
        xMap = new double[T][res]; // interpolated pixel coordinates
        yMap = new double[T][res];

        motMap = new double[T][res];
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

    /**
     * Generate all maps saved by Q Analysis
     * Fill internal class fields
     */
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
            motMap[tt][0] = intMig;
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
                motMap[tt][p] = intMig;
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

        migImP = map2ImagePlus("motility_map", new ColorProcessor(res, T, migColor));
        convImP = map2ImagePlus("convexity_map", new ColorProcessor(res, T, convColor));
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
            map3d.write(new File("/tmp/cell_02.wrl"));

            // create 3D of curvature
            STMap3D map3dCur = new STMap3D(xMap, yMap, convColor);
            map3dCur.build();
            map3dCur.toOrigin(oH.indexGetOutline(0).getCentroid());
            map3dCur.scale(0.05f);
            map3dCur.write(new File("/tmp/cell_02_cur.wrl"));
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
            QuimPArrayUtils.arrayToFile(motMap, ",",
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
                    "ECMM data is missing (or corrupt), and is needed for building accurate maps.+"
                            + "\nPlease run ECMM (fluorescence data will be lost)");
        }
        // test making LUT images
        /*
         * ImagePlus migImPLut = IJ.createImage("mig_32", "32-bit", res, T,1);
         * ImageProcessor ipFloat = new FloatProcessor(res, T, migPixels, null);
         * LUT lut = new LUT(); ipFloat.setLut(lut)
         * migImPLut.setProcessor(ipFloat); resize(migImPLut); migImPLut.show();
         */
    }

    public ImagePlus map2ImagePlus(String name, ImageProcessor imp) {
        ImagePlus ret = new ImagePlus(name, imp);
        resize(ret);
        setCalibration(ret);
        return ret;
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
            LOGGER.warn("WARNING- frac corrected: " + frac);
        }
        if (frac > 1 || frac < 0) {
            LOGGER.warn("!WARNING, frac out of range:" + frac);
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
            LOGGER.warn("WARNING- frac corrected: " + frac);
        }
        if (frac > 1 || frac < 0) {
            LOGGER.warn("WARNING, frac out of range:" + frac);
        }

        if (Double.isNaN(frac) || Double.isNaN(frac)) {
            LOGGER.warn("WARNING, frac is nan:" + frac);
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
            LOGGER.error("ERROR: target less than zero");
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
            LOGGER.error("ERROR: target less than zero");
        }

        return targ;
    }

    private double interpolate(double v1, double v2, double frac) {
        return v1 + ((v2 - v1) * frac);
    }

    /**
     * Calculates convexity by smoothing or averaging across nodes
     */
    private void calcCurvature() {

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

    /**
     * Sum smoothed curavture over a region of the membrane
     * 
     * @param o the outline
     */
    private void sumCurvature(Outline o) {
        //

        Vert v, tmpV;
        double totalCur, distance;
        // avertage over curvatures
        if (Qp.sumCov > 0) {
            LOGGER.trace("summing curv");
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
        if (T >= res * 0.9) {
            return; // don't resize if its going to compress frames
        }
        ImageProcessor ip = ImP.getProcessor();

        // if (Qp.singleImage) {
        ip.setInterpolationMethod(ImageProcessor.NONE);
        // } else {
        // ip.setInterpolationMethod(ImageProcessor.BILINEAR);
        // }

        double vertRes = Math.ceil((double) res / (double) T);
        // System.out.println("OH s: " + oH.getSize() + ",vertres: "+ vertRes);
        mapPixelHeight = 1.0d / vertRes;
        vertRes = T * vertRes;

        // System.out.println("OH s: " + oH.getSize() + ",vertres: "+ vertRes);

        ip = ip.resize(res, (int) vertRes);
        ImP.setProcessor(ip);

    }

    private void setCalibration(ImagePlus ImP) {
        ImP.getCalibration().setUnit("frames");
        ImP.getCalibration().pixelHeight = mapPixelHeight;
        ImP.getCalibration().pixelWidth = mapPixelWidth;
    }

    @Override
    public void beforeSerialize() {
    }

    @Override
    public void afterSerialize() throws Exception {
        LOGGER.debug("This class can not be deserialzied without assgning OutlineHndler");
    }
}