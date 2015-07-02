package uk.warwick.quimp_11b;

/**
 * Richard Tyson. 23/09/2009. ECM Mapping Systems Biology DTC, Warwick
 * University.
 */
import ij.*;
//import java.awt.*;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import java.io.*;
import java.lang.Math.*;
import java.util.ArrayList;
//import java.util.Vector;
import java.util.Random;

public class ECMM_Mapping {

    OutlineHandler oH, outputH;
    static ECMplot plot;
    QParams qp;

    public ECMM_Mapping(int frames) { // work around. b is nothing
        if (ECMp.plot) {
            plot = new ECMplot(frames);
        }
    }

    public ECMM_Mapping(String QPfile) {
        IJ.log("ECMM with param file name as string");
        try {
            qp = new QParams(new File(QPfile));
            qp.readParams();
            //ECMp.setup(qp);
            runFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ECMM_Mapping(File f) {
        IJ.log("ECCM with param file name as file");
        try {
            qp = new QParams(f);
            qp.readParams();
            //ECMp.setup(qp);
            runFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ECMM_Mapping() {
        about();
        try {
            do {

                OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", OpenDialog.getLastDirectory(), ".paQP");
                if (od.getFileName() == null) {
                    return;
                }
                File paramFile = new File(od.getDirectory(), od.getFileName());
                qp = new QParams(paramFile);
                qp.readParams();

                //ECMp.setup(qp);
                runFromFile();

                File[] otherPaFiles = qp.findParamFiles();

                if (otherPaFiles.length > 0) {
                    YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Batch Process?", "\tBatch Process?\n\n"
                            + "Process other paQP files in the same folder with ECMM?\n"
                            + "[Files already run through ECMM will be skipped!]");
                    if (yncd.yesPressed()) {
                        ArrayList<String> runOn = new ArrayList<String>(otherPaFiles.length);
                        ArrayList<String> skipped = new ArrayList<String>(otherPaFiles.length);

                        for (int j = 0; j < otherPaFiles.length; j++) {
                            plot.close();

                            paramFile = otherPaFiles[j];
                            qp = new QParams(paramFile);
                            qp.readParams();
                            if (!qp.ecmmHasRun) {
                                System.out.println("Running on " + otherPaFiles[j].getAbsolutePath());
                                runFromFile();
                                runOn.add(otherPaFiles[j].getName());
                            } else {
                                System.out.println("Skipped " + otherPaFiles[j].getAbsolutePath());
                                skipped.add(otherPaFiles[j].getName());
                            }

                        }
                        IJ.log("\n\nBatch - Successfully ran ECMM on:");
                        for (int i = 0; i < runOn.size(); i++) {
                            IJ.log(runOn.get(i));
                        }
                        IJ.log("\nSkipped:");
                        for (int i = 0; i < skipped.size(); i++) {
                            IJ.log(skipped.get(i));
                        }

                    } else {
                        return;
                    }
                }

                return;

            } while (true);

        } catch (Exception e) {
            //IJ.error("Unknown exception");
            e.printStackTrace();
        }
    }

    private void about() {
        IJ.log("##############################################\n \n"
                + Tool.getQuimPversion() + " - ECMM Mapping plugin,\nby Richard Tyson (R.A.Tyson@warwick.ac.uk),\n\n"
                + "David Epstein & T. Bretschneider, Version 3.0\n"
                + "T.Bretschneider@warwick.ac.uk\n\n" + "##############################################\n \n");
    }

    private void runFromFile() {
        oH = new OutlineHandler(qp);
        if (!oH.readSuccess) {
            return;
        }

        ECMp.setup(qp);
        //System.out.println("sf " + ECMp.startFrame + ", ef " + ECMp.endFrame);
        //System.out.println("outfile " + ECMp.OUTFILE.getAbsolutePath());
        ECMp.setParams(oH.maxLength); //base params on outline in middle of sequence
        if (ECMp.plot) {
            plot = new ECMplot(oH.getSize() - 1);
        }
        run();

        if(ECMp.saveTemp){
            //------ save a temporary version instead as to not over write the old version
            File tempFile = new File(ECMp.OUTFILE.getAbsolutePath() + ".temp.txt");
            outputH.writeOutlines(tempFile, true);
            IJ.log("ECMM:137, saving to a temp file instead");
        }else{
            ECMp.INFILE.delete();
            outputH.writeOutlines(ECMp.OUTFILE, true);
        }
  
    }

    public OutlineHandler runByANA(OutlineHandler m, ImageProcessor Ipr, double d) { // ana uses this
        //IJ.log("ECM Mapping (Memory) - R Tyson");
        oH = m;
        ECMp.image = Ipr;
        ECMp.setParams(oH.maxLength);
        ECMp.startFrame = oH.getStartFrame();
        ECMp.endFrame = oH.getEndFrame();
        ECMp.plot = false;
        ECMp.ANA = true;
        ECMp.anaMigDist = d;
        ECMp.migQ = 1.5E-5; //
        ECMp.tarQ = -1.5E-5; // use same charge

        if (ECMp.plot) {
            plot = new ECMplot(oH.getSize() - 1);
        }

        //ECMp.setParams(m.indexGetOutline(0));

        //*******adjust params for ana***********
        ECMp.h = 0.9;
        ECMp.chargeDensity = 4;
        ECMp.d = 0.4;
        ECMp.maxVertF = 0.7;
        //*************************
        run();
        //IJ.log("ECM Mapping FINISHED");
        return outputH;
    }

    private void run() {
        long time = System.currentTimeMillis();
        if (!ECMp.ANA) {
            IJ.log("ECMM resolution: " + ECMp.markerRes + "(av. spacing)\n");
        }

        outputH = new OutlineHandler(oH.getStartFrame(), oH.getEndFrame());
        ECMp.unSnapped = 0;
        //int skippedFrames = 0; // if a frame is skipped need to divide next time point migration by 2, etc...

        Mapping map1;
        int f = ECMp.startFrame;  // now in frames
        Outline o1 = oH.getOutline(f);
        // resolution is always as in segmentation - not now
        if (!ECMp.ANA) {
            if (Math.abs(ECMp.markerRes) > 0) {
                o1.setResolution(Math.abs(ECMp.markerRes));
            }
        }
        o1.resetAllCoords();
        o1.clearFluores();


        outputH.save(o1, f);
        Outline o2;

        int stopAt = -1; // debug break


        for (; f <= oH.getEndFrame() - 1; f++) {
            if (f == stopAt) {
                ECMp.plot = true;
            }
            if (o1.checkCoordErrors()) {
                IJ.error("There was an error in tracking due to a bug (frame " + (f) + ")"
                        + "\nPlease try again");
                break;
            }



            if (!ECMp.ANA) {
                IJ.showStatus("Running ECMM");
                IJ.showProgress(f, oH.getEndFrame());
                IJ.log("Mapping " + f + " to " + (f + 1));
            }

            o2 = oH.getOutline(f + 1);
            // o2 left as seen in the segmentation - i.e. marker res unchanged
            if (!ECMp.ANA && ECMp.markerRes > 0) {
                o2.setResolution(ECMp.markerRes);  // must be done b4 intersects are calculated
            }
            o2.resetAllCoords();
            o2.clearFluores();
            
            if(!ECMp.ANA) this.nudgeOverlaps(o1,o2); // ensure no points/edges lie directly on each other (to 1e-4)/ .

            if (ECMp.plot) {
                plot.setDrawingFrame(f);
                plot.centre = o1.getCentroid();
            
                if (ECMp.drawInitialOutlines) {
                    plot.setColor(0d, 0d, 1d);
                    plot.drawOutline(o1);
                    plot.setColor(0d, 1d, 0d);
                    plot.drawOutline(o2);
                    plot.setSlice(f);
                }
            }

            //OutlineHandler.writeSingle("o2.snQP", o2);
            //OutlineHandler.writeSingle("o1.snQP", o1);

            map1 = new Mapping(o1, o2);

            /*
             * if (map1.invalid) { //Use no sectors IJ.log(" invalid outline
             * intersection, attempting non-intersect mapping...");
             * plot.writeText("Non-intersect mapping"); ECMp.noSectors = true;
             * map1 = new Mapping(o1, o2); ECMp.noSectors = false; }
             */

            o1 = map1.migrate();
            //System.out.println("num nodes: "+o1.getVerts());

            if (!ECMp.ANA) {
                //System.out.println("\n check final intersects");
                if (!ECMp.disableDensityCorrections) {
                    if (o1.removeNanoEdges()) {
                        //IJ.log("    result had some v.small edges- removed");
                    }
                    if (o1.cutSelfIntersects()) {
                        IJ.log("    result self intersected - fixed");
                        if (ECMp.plot) {
                            plot.writeText("Fixed self intersection");
                        }
                    }

                    if (ECMp.markerRes == 0) {
                        o1.correctDensity(2 * 1.6, 2 / 1.6);
                    } else {
                        o1.correctDensity(ECMp.markerRes * 1.6, ECMp.markerRes / 1.6);
                    }
                }
                if (ECMp.plot && ECMp.drawSolutionOutlines) {
                    plot.setColor(1d, 0d, 0d);
                    plot.drawOutline(o1);
                }
            }

            if (ECMp.ANA && ECMp.plot) {
                plot.setColor(0d, 0.7d, 0.7d);
                plot.drawOutline(o1);
            }

            //OutlineHandler.writeSingle("o2.snQP", o2);
            //OutlineHandler.writeSingle("o1.snQP", o1);

            o1.coordReset(); //reset the frame Coordinate system

            outputH.save(o1, f + 1);
            if (f == stopAt) {
                break;
            }
        }

        //IJ.log("Total iterations = " + ECMp.its);
        if (ECMp.plot) {
            plot.repaint();
        }

        if (!ECMp.ANA) {
            double timeSec = (System.currentTimeMillis() - time) / 1000d;
            IJ.showStatus("ECMM finished");
            IJ.log("ECMM finished in " + timeSec + " seconds.");
        }
        return;
    }
    
    private void nudgeOverlaps(Outline o1, Outline o2) {

        int state;
        double[] intersect = new double[2];
        Random rg = new Random();
        
        Vert nA = o1.getHead();
        Vert nB;
        do {
            nB = o2.getHead();
            do {
                // check if points on top of each other
                if (nB.getX() == nA.getX() && nB.getY() == nA.getY()) {
                    //IJ.log("  outline points overlap-fixed");
                    nA.setX(nA.getX() + (rg.nextDouble()*0.5)+0.01); //use a minimum nudge of 0.01 (imageJ pixel accurracy
                    nA.setY(nA.getY() + (rg.nextDouble()*0.5)+0.01);
                }

                //check if lines are parallel
                state = Vect2d.segmentIntersection(nA.getX(), nA.getY(), nA.getNext().getX(), nA.getNext().getY(),
                        nB.getX(), nB.getY(), nB.getNext().getX(), nB.getNext().getY(), intersect);
                if (state == -1 || state == -2) {
                    //IJ.log("  outline parrallel -fixed");
                    nA.setX(nA.getX() + (rg.nextDouble()*0.5)+0.01);
                    nA.setY(nA.getY() + (rg.nextDouble()*0.5)+0.01);
                }

                nB = nB.getNext();
            } while (!nB.isHead());
            nA = nA.getNext();

        } while (!nA.isHead());   
        
    }
}

class Mapping {

    Outline o1, o2;
    Sector[] sectors;

    public Mapping(Outline oo1, Outline oo2) {
        o1 = oo1;
        o2 = oo2;
        ECMp.numINTS = 0;

        if (ECMp.ANA || ECMp.forceNoSectors) { // for ANA force no intersection points
            insertFake();
            o1.updateNormales(true);
            o2.updateNormales(true);
            formSectors();
            return;
        }
        
        // shift them slightly
        ECMp.numINTS = calcIntersects(); //temp intersect points are inserted

        if (ECMp.numINTS == 0) {
            System.out.println("No intersects found");
            insertFake();
            o1.updateNormales(true);
            o2.updateNormales(true);
            formSectors();
        } else {
            if(ECMp.inspectSectors){
            if (!inspectInts()) {
                IJ.log("    invalid outline intersections. Intersects corrected");
                if(ECMp.plot && ECMp.drawFails) ECMM_Mapping.plot.writeText("Intersects corrected");
                //drawGoodInts();
                //drawRawIntsStates();
                rebuildInts();

            }
            }
            if (ECMp.plot && ECMp.drawIntersects) {
                drawIntersects();
            }
            //System.out.println("Num intersects: " + INTS);

            o1.updateNormales(true);
            o2.updateNormales(true);
            formSectors();
        }
    }

    public void printSector(int i) {
        sectors[i].print();
    }

    private int calcIntersects() {
        // inserts intersect point (intPoints) into both outlines
        int INTS = 0;

        Vert nA, nB, temp; // node 1 of edges A and B
        double[] intersect = new double[2];
        int edgeAcount = 1;
        int edgeBcount = 1;
        int i = 0, state;

        nA = o1.getHead();
        do {
            nB = o2.getHead(); // a different outline so no problem with adjacent edges being flagged as crossing
            //edgeBcount = 1;
            do {
                state = Vect2d.segmentIntersection(nA.getX(), nA.getY(), nA.getNext().getX(), nA.getNext().getY(),
                        nB.getX(), nB.getY(), nB.getNext().getX(), nB.getNext().getY(), intersect);

                if (state == 1) {
                    //result.print("intersect at : ");
                    INTS++;
                    temp = o1.insertVert(nA);
                    temp.setX(intersect[0]);
                    temp.setY(intersect[1]);
                    temp.setIntPoint(true, INTS);
                    nA = nA.getNext();

                    temp = o2.insertVert(nB);
                    temp.setX(intersect[0]);
                    temp.setY(intersect[1]);
                    temp.setIntPoint(true, INTS);
                    nB = nB.getNext();

                    //result.print("intersect point: ");
                    i++;
                }
                nB = nB.getNext();
                edgeBcount++;
            } while (!nB.isHead());
            nA = nA.getNext();
            edgeAcount++;
        } while (!nA.isHead());

        return INTS;
    }

    private void removeIntersects() {
        //removes intersect points inserted by calcIntersects
        Vert v = o1.getHead();
        do {
            if (v.getPrev().isIntPoint()) {
                o1.removeVert(v.getPrev());
            }
            v = v.getNext();
        } while (!v.isHead());

        v = o2.getHead();
        do {
            if (v.getPrev().isIntPoint()) {
                o2.removeVert(v.getPrev());
            }
            v = v.getNext();
        } while (!v.isHead());
    }

    private void insertFake() {
        //insert one fake intersect point just after the heads
        // done when no intersections exist
        Vect2d pos = Vect2d.vecP2P(o1.getHead().getPoint(), o1.getHead().getNext().getPoint());
        pos.multiply(0.5);
        pos.addVec(o1.getHead().getPoint()); //half way between head and next vert

        Vert temp = o1.insertVert(o1.getHead());
        temp.setX(pos.getX());
        temp.setY(pos.getY());
        temp.setIntPoint(true, 1);
        //
        pos = Vect2d.vecP2P(o2.getHead().getPoint(), o2.getHead().getNext().getPoint());
        pos.multiply(0.5);
        pos.addVec(o2.getHead().getPoint()); //half way between head and next vert

        temp = o2.insertVert(o2.getHead());
        temp.setX(pos.getX());
        temp.setY(pos.getY());
        temp.setIntPoint(true, 1);
        ECMp.numINTS++;
    }

    private boolean inspectInts() {
        //System.out.println("finding inverted intersects");
        // make sure the intersect points form proper sectors
        // by removing intersectiosn that form inverted sectors

        boolean valid = true; // made false if an inverse or loose sector is found

        Vert v1 = o1.getHead();
        Vert v2, v1p, v2p, v2m;

        for (int j = 0; j < ECMp.numINTS; j++) {
            do {
                v1 = v1.getNext();
            } while (!v1.isIntPoint()); //find next int point

            v2 = o2.getHead();
            do {
                if (v2.isIntPoint()) {
                    if (v2.intsectID == v1.intsectID) {
                        break; //find matching in o2
                    }
                }
                v2 = v2.getNext();
            } while (true);


            //System.out.println(j + " :looking at");
            //v1.print();
            //v2.print();

            v1p = v1;
            do {
                v1p = v1p.getNext();
            } while (!v1p.isIntPoint()); //find next intersect from v1

            v2p = v2;
            do {
                v2p = v2p.getNext();
            } while (!v2p.isIntPoint()); //find next intersect, same direction

            v2m = v2;
            do {
                v2m = v2m.getPrev();
            } while (!v2m.isIntPoint()); //find next intersect oposit direction from v2

            if (v1p.intsectID == v2p.intsectID) {
                //System.out.println("Found valid sector");
                if (v1.intState == 0) {
                    v1.intState = 1; //green
                }
                if (v2.intState == 0) {
                    v2.intState = 1;
                }

            } else if (v1p.intsectID == v2m.intsectID) {
                System.out.println("found inverse sector");
                v1.intState = 3;
                v2.intState = 3;

                //v1p.intState = 3;
                //v2m.intState = 3;
                valid = false;
            } else {
                //System.out.println("Found loose sector");
                valid = false;
                if (v1.intState == 0) {
                    v1.intState = 2; // blue
                }
                if (v2.intState == 0) {
                    v2.intState = 2;
                }
                if (v1.intState == 3) {
                    v1.intState = 4; // blue
                }
                if (v2.intState == 3) {
                    v2.intState = 4;
                }
            }
        }

        return valid;
    }

    private void rebuildIntsOLD() {
        // attempts to remove the correct intersects to leave only valid
        // intersections.  Done by adding back in inverted ints found by
        // findInvertedInts()

        //find a good sector to start with (intState==1)
        Vert v1 = o1.getHead();
        boolean found = false;
        do {
            if (v1.isIntPoint() && v1.intState == 1) {
                found = true;
                break;
            }
            v1 = v1.getNext();
        } while (!v1.isHead());

        // if no valid sectors use a loose vert to start from (and cross fingers)
        if (!found) {
            v1 = o1.getHead();
            do {
                if (v1.isIntPoint() && (v1.intState == 4 || v1.intState == 2)) {
                    found = true;
                    break;
                }
                v1 = v1.getNext();
            } while (!v1.isHead());

            if (!found) {
                System.out.println("    COUDL NOT FIND A STATING INT POINT. Going random=BAD");
            }
        }

        // find matching intersection in o2
        Vert v2 = o2.getHead();
        do {
            if (v2.isIntPoint() && v2.intsectID == v1.intsectID) {
                break;
            }
            v2 = v2.getNext();
        } while (!v2.isHead());

        // from v1, retain intersect points that allow building of good sectors,
        // and delete the others.
        int startingInt = v1.intsectID;
        Vert v1remove;

        int o1count; // provide rough estimate to sector lengths
        int o2count; // allow us to pick the assumed best soloution
        do {
            o1count = 0;
            o2count = 0;
            do {
                v1 = v1.getNext();
                o1count++;
            } while (!v1.isIntPoint()); // move to next int point
            do {
                v2 = v2.getNext();
                o2count++;
            } while (!v2.isIntPoint()); // move to next int point

            if (v2.intsectID == v1.intsectID) {
                //System.out.println("good sector");
                continue;

            }     //good  sector

            do {

                if (v2.isIntPoint() && v2.intsectID != v1.intsectID) {
                    //System.out.println("deleting...");
                    v1remove = o1.getHead();
                    do {
                        // find matching intersect to also delete
                        if (v1remove.isIntPoint() && v1remove.intsectID == v2.intsectID) {
                            o1.removeVert(v1remove);
                            break;
                        }
                        v1remove = v1remove.getNext();
                    } while (!v1remove.isHead()); // move to next int point

                    o2.removeVert(v2); // delete troublesome intersect
                    ECMM_Mapping.plot.setColor(0, 0.8, 0);
                    ECMM_Mapping.plot.drawCross(v2.getPoint(), 5);

                } else if (v2.isIntPoint() && v2.intsectID == v1.intsectID) {
                    //System.out.println("New good sector!!");
                    break;
                }
                v2 = v2.getNext();
            } while (true);

        } while (v1.intsectID != startingInt);

        // count remaining intersects
        v1 = o1.getHead();
        int intersects = 0;
        do {
            if (v1.isIntPoint()) {
                intersects++;
            }
            v1 = v1.getNext();
        } while (!v1.isHead());

        ECMp.numINTS = intersects;

    }

    private void rebuildInts() {
        // attempts to remove the correct intersects to leave only valid
        // intersections.  Done by adding back in inverted ints found by
        // findInvertedInts()

        System.out.println("Rebuilding intersects");
        //find a good sector to start with (intState==1)
        Vert v1 = o1.getHead();
        boolean found = false;
        do {
            if (v1.isIntPoint() && v1.intState == 1) {
                found = true;
                break;
            }
            v1 = v1.getNext();
        } while (!v1.isHead());

        // if no valid sectors use a loose vert to start from (and cross fingers)
        if (!found) {
            v1 = o1.getHead();
            do {
                if (v1.isIntPoint() && (v1.intState == 4 || v1.intState == 2)) {
                    found = true;
                    break;
                }
                v1 = v1.getNext();
            } while (!v1.isHead());
        }
        if (!found) {
            System.out.println("    ISSUE! ECMM.01 - NO valid sectors exist! (guessing correct sectors)");
            v1 = Outline.findIntersect(v1, 4);
        }

        // find matching intersection in o2
        Vert v2 = Outline.findIntersect(o2.getHead(), v1.intsectID);
        //System.out.println("done finding a start");

        // from v1, retain intersect points that allow building of good sectors,
        // and delete the others.
        int startingInt = v1.intsectID;
        Vert v1p, v2p, v1pp, v2pp;
        double ratio1, ratio2; // ratio of sector lenghs for 2 possible solutions
        int d1, d2, d3, d4;

        if(ECMp.plot && ECMp.drawFails) ECMM_Mapping.plot.setColor(0, 0.8, 0); // deleted colour

        do {
            //System.out.println("Iteration");
            v1p = v1;
            v2p = v2;
            v1p = Outline.getNextIntersect(v1);
            v2p = Outline.getNextIntersect(v2);
            if (v1p.intsectID == v2p.intsectID) {
                v1 = v1p;
                v2 = v2p;
            } else {
                v1pp = Outline.findIntersect(v1p, v2p.intsectID);
                v2pp = Outline.findIntersect(v2p, v1p.intsectID);
                //System.out.println("found vpp intersects");
                ratio1 = Outline.invertsBetween(v1, v1pp);
                ratio2 = Outline.invertsBetween(v2, v2pp);

                if (ratio1 == ratio2) {
                    //System.out.println("using distance measure");
                    // use Distance measure to choose
                    d1 = Outline.distBetweenInts(v1, v1pp);
                    d2 = Outline.distBetweenInts(v2, v2pp);
                    d3 = Outline.distBetweenInts(v1, v1p);
                    d4 = Outline.distBetweenInts(v2, v2p);
                    ratio1 = (d1 > d3) ? d1 / d3 : d3 / d1;
                    ratio2 = (d2 > d4) ? d2 / d4 : d4 / d2;
                }

                if (ratio1 < ratio2) {  // delete ints on o1  should be <    §
                    do {
                        v1 = v1.getNext();
                        if (v1.intsectID == v2p.intsectID) {
                            break;
                        }
                        if (v1.isIntPoint()) {
                            if (v1.intsectID == startingInt) {
                                //System.out.println("Removing starting INT!");
                            }
                            o1.removeVert(v1);
                            o2.removeVert(Outline.findIntersect(o2.getHead(), v1.intsectID)); // also delete in o1
                            //System.out.println("removed o2 intersects");
                            if(ECMp.plot && ECMp.drawFails) ECMM_Mapping.plot.drawCross(v1.getPoint(), 5);
                        }
                    } while (true);
                    v2 = v2p;
                } else { // delete ints on o2
                    do {
                        v2 = v2.getNext();
                        if (v2.intsectID == v1p.intsectID) {
                            break;
                        }
                        if (v2.isIntPoint()) {
                            if (v2.intsectID == startingInt) {
                                //System.out.println("Removing starting INT!");
                            }
                            o2.removeVert(v2);
                            o1.removeVert(Outline.findIntersect(o1.getHead(), v2.intsectID)); // also delete in o2
                            //System.out.println("removed o1 intersects");
                            if(ECMp.plot && ECMp.drawFails) ECMM_Mapping.plot.drawCross(v2.getPoint(), 5);
                        }
                    } while (true);
                    v1 = v1p;
                }
            }
        } while (v1.intsectID != startingInt);

        // count remaining intersects
        v1 = o1.getHead();
        int intersects = 0;
        do {
            if (v1.isIntPoint()) {
                intersects++;
            }
            v1 = v1.getNext();
        } while (!v1.isHead());

        ECMp.numINTS = intersects;
        System.out.println("finished rebuilding. INTS:" + ECMp.numINTS);
    }

    private void drawRawIntsStates() {
        // test method that draws different labels of intersection

        Vert v1 = o1.getHead();
        do {
            if (v1.isIntPoint()) {
                if (v1.intState == 4) {
                    ECMM_Mapping.plot.setColor(0.8, 0.8, 0);
                }
                if (v1.intState == 1) {
                    ECMM_Mapping.plot.setColor(0, 0.8, 0);
                }
                if (v1.intState == 2) {
                    ECMM_Mapping.plot.setColor(0, 0, 0.8);
                }
                if (v1.intState == 3) {
                    ECMM_Mapping.plot.setColor(0.8, 0, 0);
                }

                ECMM_Mapping.plot.drawCross(v1.getPoint(), 6);

            }
            v1 = v1.getNext();
        } while (!v1.isHead());

        Vert v2 = o2.getHead();
        do {
            if (v2.isIntPoint()) {
                if (v2.intState == 4) {
                    ECMM_Mapping.plot.setColor(0.8, 0.8, 0);
                }
                if (v2.intState == 1) {
                    ECMM_Mapping.plot.setColor(0, 0.8, 0);
                }
                if (v2.intState == 2) {
                    ECMM_Mapping.plot.setColor(0, 0, 0.8);
                }
                if (v2.intState == 3) {
                    ECMM_Mapping.plot.setColor(0.8, 0, 0);
                }
                ECMM_Mapping.plot.drawCircle(v2.getPoint(), 12);
            }
            v2 = v2.getNext();
        } while (!v2.isHead());

    }

    private void drawIntersects() {
        if(!ECMp.plot) return;
        
        ECMM_Mapping.plot.setColor(0, 0.8, 0);
        Vert v1 = o1.getHead();
        do {
            if (v1.isIntPoint()) {
                ECMM_Mapping.plot.drawCross(v1.getPoint(), 6);
                ECMM_Mapping.plot.drawCircle(v1.getPoint(), 12);
            }
            v1 = v1.getNext();
        } while (!v1.isHead());

    }

    private void formSectors() {
        // forms sectors based on the intPoints inserted by 'calcIntersects'
        // a sector is simply a pointer to the sectors starting intPoint
        //checkValid();

     //   if (ECMp.plot) {
//            ECMM_Mapping.plot.setColor(0.5, 0.5, 0.5);
      //  }

        if (ECMp.numINTS == 0) {
            //IJ.error("NO INTERSECTS");
            System.out.println("No Intersects"); //should never happen. fake ones insterted
        }
        sectors = new Sector[ECMp.numINTS];

        Vert vo1 = o1.getHead();
        Vert vo2 = o2.getHead();

        for (int i = 0; i < ECMp.numINTS; i++) {
            do {
                vo1 = vo1.getNext();
            } while (!vo1.isIntPoint());
            do {
                vo2 = vo2.getNext();
                if (vo2.isIntPoint()) {
                    if (vo2.intsectID == vo1.intsectID) {
                        break; //find matching intersect
                    }
                }
            } while (true);

            if (ECMp.numINTS == 1) { // no intersects present, forced or otherwise
                sectors[0] = new Sector(0);
                sectors[0].setStarts(vo1, vo2);
                break;
            } else {
                if (i == 0) {
                    sectors[i] = new Sector(i);
                    sectors[i].setStarts(vo1, vo2);
                    sectors[ECMp.numINTS - 1] = new Sector(ECMp.numINTS - 1);     // set as ends for last sector
                    //sectors[INTS - 1].setEnds(vo1, vo2);
                } else if (i == ECMp.numINTS - 1) {
                    sectors[i].setStarts(vo1, vo2);
                    //sectors[i - 1].setEnds(vo1, vo2);
                } else {
                    sectors[i] = new Sector(i);
                    sectors[i].setStarts(vo1, vo2);
                    //sectors[i - 1].setEnds(vo1, vo2);
                }
            }
        }

        if (ECMp.numINTS == 1) { // no intersects present, forced or otherwise
            sectors[0].constructWhole(o1.calcArea(), o2.calcArea());
        } else {
            for (int i = 0; i < ECMp.numINTS; i++) {
                sectors[i].construct();     // calc lengths, determin exp or contr, make charges
                //sectors[i].showPlot();
            }
        }
    }

    public Outline migrate() {
        Vert tempVert = new Vert(-2); //only create to get rid of 'might not be initialized'
        Vert newVert; // placed at the marker
        Vect2d newPos;
        Sector s;

        Vert mapHead = new Vert(-1);
        Outline mappedOutline = new Outline(mapHead);
        Outline tempO; // for switching mig and tar contours
        Vert currentMapVert = mapHead;
        int nodeSucCount = 0;

        for (int i = 0; i < sectors.length; i++) {
            s = sectors[i];
            Vert v = s.getMigStart().getNext(); // starting vert, don't migrate the intpoint

            do {
                //if (ECMp.chargeDensity != -1) { //nar. polar charges sort this out
                //tempVert = s.addTempCharge(v);
                //}
                //IJ.log("migrating x:" + v.getX() + ", y:" + v.getY()); //debug
                newPos = ODEsolver.euler(v, s);
                if (!v.snapped) {
                    ECMp.unSnapped++;
                    IJ.log("    node failed to map (" + ECMp.unSnapped + ") - removed");
                    if (!ECMp.ANA && ECMp.plot && ECMp.drawFails) {
                        ECMM_Mapping.plot.writeText("FN(" + ECMp.unSnapped + ")");
                    }
                    v = v.getNext();

                    //System.out.println("sector expand: " +s.expanding+", trueExpand: "+s.trueExpand + ", outDirection: " + s.outerDirection);
                    //s.tarCharges.print();
                    //s.migCharges.print();
                    continue;
                }

                newVert = mappedOutline.insertVert(currentMapVert);
                newVert.tarLandingCoord = v.fLandCoord; // so we always have a reference to where we landed
                nodeSucCount++;

                if (s.expansion) { // expanding or retracting based on area change (not length of sector)
                    newVert.distance = -v.distance; //?????????????? why neg
                } else {
                    newVert.distance = v.distance;
                }

                if (!s.forwardMap) {

                    //if (s.expansion) { // expanding or retracting based on area change (not length of sector)
                    // newVert.distance = -v.distance;
                    //} else {
                    // newVert.distance = v.distance;
                    //}
                    // expanding, vert assigned coor according to where it lands on target
                    newVert.setX(v.getX());
                    newVert.setY(v.getY());
                    newVert.gCoord = v.gLandCoord;// + 1;
                    newVert.fCoord = v.fLandCoord;// + 1;
                } else {

                    //if (s.expansion) {
                    // newVert.distance = -v.distance;
                    // } else {
                    // newVert.distance = v.distance;
                    //}
                    // retracting, vert retains its coor
                    newVert.setX(newPos.getX());
                    newVert.setY(newPos.getY());
                    newVert.gCoord = v.gCoord;
                    newVert.fCoord = v.coord;
                }

                if (ECMp.ANA) {
                    //newVert.fluores = v.cloneFluo();
                    newVert.setFluores(v.fluores);
                    newVert.setTrackNum(v.getTrackNum());
                }
                currentMapVert = newVert;
                //if (ECMp.chargeDensity != -1) {
                //s.removeTempCharge(tempVert);
                //}
                v = v.getNext();
            } while (!v.isIntPoint());
            // finsihed sector

            //if (ECMp.ANA) { // if ana, and migrating forward in time
            //secondPass(s, mappedOutline,nodeSucCount);
            //}


        }
        mappedOutline.removeVert(mapHead);

        return mappedOutline;
    }

    public Sector getSector(int i) {
        if (i < 0 || i > ECMp.numINTS) {
            IJ.error("sectors out of bounds - 250");
        }
        return sectors[i];
    }


    

    private void secondPass(Sector s, Outline mappedOutline, int noNodesAdded) {
        // check for low density and add in markers
        s.switchMigDirection();
        double markerResTol = ECMp.markerRes * 1.5d;

        Vert v = mappedOutline.getHead();
        Vert vp, addV; // prev vert
        double distance, rSize, fillGap, addAt; // relative size of gap
        double fillWith;

        for (int i = noNodesAdded; i > 1; i--) { //cycle back through success mapped nodes
            v = v.getPrev();
            vp = v.getPrev();


            distance = Vect2d.lengthP2P(v.getPoint(), vp.getPoint());
            if (distance > markerResTol) {

                rSize = distance / markerResTol; // gap size relative to marker density
                fillWith = Math.ceil(distance / markerResTol); // number of verts to insert
                distance = Vert.disCoord2Coord(vp.tarLandingCoord, v.tarLandingCoord);
                fillGap = distance / (fillWith + 1); // coord gap to inster nodes

                for (int j = 0; j < fillWith; j++) {
                    addAt = Vert.addCoords(vp.tarLandingCoord, fillGap);
                    //addV = o2.findCoordPoint(addAt);
                }


                //ECMM_Mapping.plot.drawCircle(vp.getPoint(), 4);
            }
        }

        s.switchMigDirection();
    }
}

class Sector {

    private int ID;
    private Vert startO1, startO2;
    //private Vert endO1, endO2;
    public Outline migCharges, tarCharges;
    FloatPolygon chargesPoly;
    FloatPolygon innerPoly, outerPoly; // if no intersects have to use these
    public double lengthO1, lengthO2;
    public int VERTSo1, VERTSo2; //num verts in 01 and o2
    public boolean forwardMap; // mapping forward or reverse?
    public boolean expansion;  //is the cell expanding here. is segment T to the left or right of segment T+1
    public double outerNormal; // the oter direction of the normals of migration charges
    // determined by FRdirection and expansion

    //double xMax, yMax, xMin, yMin;
    public Sector(int i) {
        ID = i;
    }

    public void print() {
        IJ.log("Sector " + ID + "\nMig charges: ");
        migCharges.print();
        IJ.log("");

        IJ.log("tar charges: ");
        tarCharges.print();
    }

    public void construct() {
        // calc lengths, determin expansion, set charges
        calcLengths();
        double sectorTriArea = Vect2d.triangleArea(startO1.getPoint(),
                startO1.getNext().getPoint(), startO2.getNext().getPoint()); //left or right? Use the "left" algorithm (sign of triangle area)


        if ((lengthO1 > lengthO2) || ECMp.forceForwardMapping) {
            forwardMap = true;
            migCharges = formCharges(startO1);
            tarCharges = formCharges(startO2);
            if (sectorTriArea > 0) {
                expansion = true; //
                outerNormal = -1.;
            } else {
                expansion = false;  //
                outerNormal = 1.;
            }
        } else {
            forwardMap = false; // backward in time
            migCharges = formCharges(startO2);// 
            tarCharges = formCharges(startO1);
            if (sectorTriArea > 0) {
                expansion = true; //
                outerNormal = 1.;

            } else {
                expansion = false;  //
                outerNormal = -1.;
            }
        }

        Vert v = migCharges.getHead();
        Vect2d normal;
        do {
            normal = new Vect2d(v.getNormal().getX(), v.getNormal().getY());
            normal.multiply(outerNormal * ECMp.w);
            v.getPoint().addVec(normal);
            v = v.getNext();
        } while (!v.isHead());

        if (ECMp.chargeDensity != -1) {
            migCharges.setResolution(ECMp.chargeDensity);
            tarCharges.setResolution(ECMp.chargeDensity);
        }

        // create polygon off all charges for cal point inside/outside sector
        chargesPolygon();



        /*
         *
         * if (ECMp.ANA) { //always contracting expanding = false; migCharges =
         * formCharges(startO1); tarCharges = formCharges(startO2); } else {
         * //System.out.println("lengthO1: " + lengthO1+", lengthO2:
         * "+lengthO2); if (lengthO1 >= lengthO2) { //should be lengthO1 >=
         * lengthO2. Set to true to do forward mapping expanding = false;
         * migCharges = formCharges(startO1); tarCharges = formCharges(startO2);
         * } else { expanding = true; migCharges = formCharges(startO2);
         * tarCharges = formCharges(startO1); } }
         */
        //System.out.println("sector " + ID);
        //startO1.getPoint().print("a: ");
        //startO1.getNext().getPoint().print("b: ");
        //startO2.getNext().getPoint().print("c: ");

        // if (ECMp.numINTS == 1) { //no intersections, only one fake
        //    System.out.println("sdoing this here");
        //    trueExpand = false;
        //   outerDirection = -1.;
        //} else {
        // if area of the triangle formed by segments at the start of a sector
        // is negative then the sector is expanding.
        // BUT still migrate nodes in the direction of the shorter contour...
        /*
         * double area = Vect2d.triangleArea(startO1.getPoint(),
         * startO1.getNext().getPoint(), startO2.getNext().getPoint()); //use
         * next node along if area is zero if (area > 0) { //check orientation
         * of sector (truely expanding?) trueExpand = true; } else { trueExpand
         * = false; } if (area == 0) { IJ.log("!WARNING-left algorithm == 0");
         * this.print(); } //System.out.println("area = " + area + ", LEFT: " +
         * left);
         *
         * //move mig charges outwards by normal (b4 setting resolution!),
         * //dependant on left if (expanding) { if (trueExpand) {
         * //normal.multiply(-ECMp.w); outerDirection = -1.; } else {
         * //normal.multiply(ECMp.w); outerDirection = 1.; } } else { if
         * (trueExpand) { //normal.multiply(ECMp.w); outerDirection = 1.; } else
         * { //normal.multiply(-ECMp.w); outerDirection = -1.; } } // }
         *
         *
         *
         */




    }

    public void constructWhole(double area1, double area2) { // no intersects exist
        //
        Outline innerCharges, outerCharges;

        calcLengths();

        if (((lengthO1 > lengthO2) || ECMp.forceForwardMapping || ECMp.ANA) && !ECMp.forceBackwardMapping) {
            forwardMap = true;
            migCharges = formCharges(startO1);
            tarCharges = formCharges(startO2);
            if (area1 > area2) {
                expansion = false; //n is migrating from outside in
                outerNormal = 1.;
                innerCharges = tarCharges;
                outerCharges = migCharges;
            } else {
                expansion = true;  //n is migrating from inside out
                outerNormal = -1.;
                innerCharges = migCharges;
                outerCharges = tarCharges;
            }
        } else {
            forwardMap = false; // backward in time
            migCharges = formCharges(startO2);// 
            tarCharges = formCharges(startO1);
            if (area1 > area2) {
                expansion = true; //n+1 is migrating from inside out, expansion
                outerNormal = -1.;
                innerCharges = migCharges;
                outerCharges = tarCharges;
            } else {
                expansion = false;  //n+1 is migrating from outside in, contraction
                outerNormal = 1.;
                innerCharges = tarCharges;
                outerCharges = migCharges;
            }
        }


        Vert v = migCharges.getHead();
        Vect2d normal;
        do {
            normal = new Vect2d(v.getNormal().getX(), v.getNormal().getY());
            normal.multiply(outerNormal * ECMp.w);
            v.getPoint().addVec(normal);
            v = v.getNext();
        } while (!v.isHead());



        if (ECMp.chargeDensity != -1) {
            migCharges.setResolution(ECMp.chargeDensity);
            tarCharges.setResolution(ECMp.chargeDensity);
        }

        outerPoly = ioPolygons(outerCharges);
        innerPoly = ioPolygons(innerCharges);

    }

    public void setStarts(Vert a, Vert b) {
        startO1 = a;
        startO2 = b;
    }

    private void calcLengths() {
        lengthO1 = 0.;
        VERTSo1 = 0;
        Vert v = startO1;
        do {
            lengthO1 += Vect2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            VERTSo1++;
            v = v.getNext();
        } while (!v.isIntPoint());

        lengthO2 = 0.;
        VERTSo2 = 0;
        v = startO2;
        do {
            lengthO2 += Vect2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            VERTSo2++;
            v = v.getNext();
        } while (!v.isIntPoint());

        //double t = lengthO1;
        //lengthO1 = lengthO2;
        //lengthO2 = t;
    }

    private Outline formCharges(Vert s) {
        //create a new outline from the sector starting at s
        Vert newV = new Vert(s.getX(), s.getY(), 1);
        newV.setNormal(s.getNormal().getX(), s.getNormal().getY());
        newV.setIntPoint(true, -1);
        Outline o = new Outline(newV);

        s = s.getNext();
        do {
            newV = o.insertVert(newV);
            newV.setX(s.getX());
            newV.setY(s.getY());
            newV.setNormal(s.getNormal().getX(), s.getNormal().getY());

            if (s.isIntPoint()) {
                newV.setIntPoint(true, -1);
            }

            s = s.getNext();
        } while (!s.getPrev().isIntPoint()); //copy the int point too

        return o;
    }

    public Vert getMigStart() { // NOT CHARGES!
        if (forwardMap) {
            return startO1;
        } else {
            return startO2;
        }
    }

    public Vert getTarStart() { // NOT CHARGES
        if (forwardMap) {
            return startO2;
        } else {
            return startO1;
        }
    }

    public Vert addTempCharge(Vert tv) {
        // inserts a temporary charge into the charged nodes to ensure a migrating node
        // remains within the boubdary of the outline. Have to find where to insert it though.
        Vert v = migCharges.getHead();
        double dis = 99999.;
        double cDis;
        Vert closest = v;
        do {
            cDis = Vect2d.distPointToSegment(tv.getPoint(), v.getPoint(), v.getNext().getPoint());
            if (cDis < dis) {
                closest = v;
                dis = cDis;
            }
            v = v.getNext();
        } while (!v.isHead());

        Vert newVert = migCharges.insertVert(closest);
        newVert.setTrackNum(-35);
        newVert.setX(tv.getX());
        newVert.setY(tv.getY());
        Vect2d normal = new Vect2d(tv.getNormal().getX(), tv.getNormal().getY());
        normal.multiply(outerNormal * ECMp.w);
        newVert.getPoint().addVec(normal);
        newVert.updateNormale(true);
        return newVert;
    }

    public void removeTempCharge(Vert v) {
        migCharges.removeVert(v);
    }

    private void chargesPolygon() {
        ArrayList<Vect2d> points = new ArrayList<Vect2d>();

        Vert v = migCharges.getHead(); //get charges from head to int point, forward
        do {

            points.add(v.getPoint());
            if (v.isIntPoint() && !v.isHead()) {
                break;
            }
            v = v.getNext();
        } while (!v.isHead());

        // find int point in tar
        v = tarCharges.getHead();
        do {
            v = v.getNext();
        } while (!v.isIntPoint());

        // get tar charges in reverse
        do {
            points.add(v.getPoint());
            v = v.getPrev();
        } while (!v.getNext().isHead());

        // create floats
        float[] x = new float[points.size()];
        float[] y = new float[points.size()];

        Vect2d p;
        for (int i = 0; i < points.size(); i++) {
            p = (Vect2d) points.get(i);
            x[i] = (float) p.getX();
            y[i] = (float) p.getY();
        }

        chargesPoly = new FloatPolygon(x, y, x.length);

        /*
         * if(true){ ECMM_Mapping.plot.drawPolygon(chargesPoly);
         * System.out.println("x length: " + x.length);
         *
         * System.out.println("tar size: "+ tarCharges.getVerts() +", mig size:
         * "+migCharges.getVerts());
         *
         *
         * if(chargesPoly.contains(75f, 40f)) { System.out.println("TRUE"); }
         * else{ System.out.println("FALSE"); } }
         */

    }

    private void ioPolygonsOLD() { //in and out polygons
        float[] x = new float[migCharges.getVerts()];
        float[] y = new float[migCharges.getVerts()];

        int i = 0;
        Vert v = migCharges.getHead();
        do {
            x[i] = (float) v.getX();
            y[i] = (float) v.getY();
            i++;
            v = v.getNext();
        } while (!v.isHead());

        outerPoly = new FloatPolygon(x, y, x.length); //was this
        //innerPoly = new FloatPolygon(x, y, x.length);

        x = new float[tarCharges.getVerts()];
        y = new float[tarCharges.getVerts()];
        i = 0;
        v = tarCharges.getHead();
        do {
            x[i] = (float) v.getX();
            y[i] = (float) v.getY();
            i++;
            v = v.getNext();
        } while (!v.isHead());

        innerPoly = new FloatPolygon(x, y, x.length); //was this
        //outerPoly = new FloatPolygon(x, y, x.length);
        //System.out.println("Using ioPoly");

        if (ECMp.plot) {
            //ECMM_Mapping.plot.drawPolygon(outerPoly);
            //ECMM_Mapping.plot.drawPolygon(innerPoly);
        }
    }

    private FloatPolygon ioPolygons(Outline charges) { //in and out polygons
        float[] x = new float[charges.getVerts()];
        float[] y = new float[charges.getVerts()];

        int i = 0;
        Vert v = charges.getHead();
        do {
            x[i] = (float) v.getX();
            y[i] = (float) v.getY();
            i++;
            v = v.getNext();
        } while (!v.isHead());

        return new FloatPolygon(x, y, x.length); //was this
    }

    public boolean insideCharges(Vect2d p) {
        if (ECMp.numINTS > 1) {
            return chargesPoly.contains((float) p.getX(), (float) p.getY());
        } else {
            if (outerPoly.contains((float) p.getX(), (float) p.getY())) {
                if (innerPoly.contains((float) p.getX(), (float) p.getY())) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }



    }

    public void switchMigDirection() {
        Outline tempO = tarCharges; // swtich charges
        tarCharges = migCharges;
        migCharges = tempO;
        forwardMap = !forwardMap;
    }
}

class ODEsolver {

    private static boolean inside;

    public ODEsolver() {
    }

    public static Vect2d euler(Vert v, Sector s) {
        //Vect2d[] history =  new Vect2d[ECMp.maxIter];
        int x, y;
        int lastSampleX = -1;
        int lastSampleY = -1; //store where last sample was 
        double dist = 0; // distance migrated
        double tempFlu;
        Vert edge;
        Vect2d p, pp;

        v.snapped = false;

        if (ECMp.ANA) { // sample at boundary
            p = v.getPoint();
            x = (int) Math.round(p.getX());
            y = (int) Math.round(p.getY());
            lastSampleX = x;
            lastSampleY = y;
            tempFlu = ODEsolver.sampleFluo(x, y);
            v.fluores[0].intensity = tempFlu;
            v.fluores[0].x = x;         // store in first slot
            v.fluores[0].y = y;
        }

        if (ECMp.plot) {
            ECMM_Mapping.plot.setColor(0, 0, 0);
        }

        p = new Vect2d(v.getX(), v.getY());
        pp = new Vect2d(v.getX(), v.getY()); //previouse position

        //history[0] = new Vect2d(p.getX(), p.getY());

        boolean maxHit = false;
        int i = 1;
        Vect2d k;

        for (; i < ECMp.maxIter - 1; i++) {
            //IJ.log("\tIt " + i); //debug
            if (ODEsolver.proximity(p, s) || (ECMp.ANA && dist >= (ECMp.anaMigDist)) || maxHit) {
                // stop when within d of the target segment or
                // if migrated more than the ana set cortex width (in pixels)
                pp.setX(p.getX());
                pp.setY(p.getY());

                //if(!ECMp.ANA) {  // no need to snap ana result. landing coord not needed
                edge = ODEsolver.snap(p, s);
                dist += Vect2d.lengthP2P(pp, p);
                v.distance = Tool.speedToScale(dist, ECMp.scale, ECMp.frameInterval);
                //if (s.expanding && !ECMp.ANA) {
                v.setLandingCoord(p, edge);
                //}
                //}

                if (ECMp.plot && ECMp.drawPaths) {
                    ECMM_Mapping.plot.setColor(0, 0, 0);
                    ECMM_Mapping.plot.drawLine(pp, p);
                }

                v.snapped = true;
                //System.out.println("iterations: " + i);
                break;
            }

            k = ODEsolver.dydt(p, s);
            k.multiply(ECMp.h);

            pp.setX(p.getX());
            pp.setY(p.getY());
            p.setX(p.getX() + k.getX());
            p.setY(p.getY() + k.getY());
            dist += Vect2d.lengthP2P(pp, p);

            if (ECMp.plot && ECMp.drawPaths) {
                //ECMM_Mapping.plot.setColor(1, 0, 0);
                ECMM_Mapping.plot.drawLine(pp, p);
            }
            //history[i] = new Vect2d(p.getX(), p.getY());

            if (ECMp.ANA) { // sample
                x = (int) Math.round(p.getX());
                y = (int) Math.round(p.getY());
                if (!(x == lastSampleX && y == lastSampleY)) { // on sample new locations
                    lastSampleX = x;
                    lastSampleY = y;
                    tempFlu = ODEsolver.sampleFluo(x, y);

                    if (tempFlu > v.fluores[0].intensity) { //store first one
                        // if((tempFlu / v.fluores[0].intensity)<1.1){ 
                        //     maxHit = true;
                        // }
                        v.fluores[0].intensity = tempFlu;
                        v.fluores[0].x = x;
                        v.fluores[0].y = y;

                    }//else{
                    //   maxHit = true;
                    //}
                    //else if(v.fluores[0].intensity - tempFlu > 20){                    
                    //    if((tempFlu / v.fluores[0].intensity)<1){             
                    //        maxHit = true;
                    //    }
                    //}
                }
            }

            ECMp.its++;
        }

        if (ECMp.plot && !v.snapped && ECMp.drawFails) { //mark the start point of failed nodes
            ECMM_Mapping.plot.setColor(1, 0, 0);
            //p.print(v.getTrackNum() + "p: ");
            //pp.print(v.getTrackNum() + "pp: ");
            ECMM_Mapping.plot.drawCircle(v.getPoint(), 5);
        }

        return p;
    }

    public static Vect2d dydt(Vect2d p, Sector s) {
        Vect2d result = fieldAt(p, s);
        result.multiply(ECMp.mobileQ);

        if (true) {//Math.abs(result.length()) > ECMp.maxVertF) {
            //IJ.log("!WARNING-max force exceeded: " + Math.abs(result.length()));
            result.makeUnit();
            result.multiply(ECMp.maxVertF);
        }
        return result;
    }

    public static boolean proximity(Vect2d p, Sector s) {
        // could test against the chrages or the actual contour.
        // if using polar lines can use actual contour
        //Vert v = s.getTarStart();
        //if(true) return false;
        //Vert v = s.tarCharges.getHead();
        Vert v = s.getTarStart();
        do {
            double d = Vect2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
            //IJ.log("\t\tprox to: " + d); //debug
            if (d <= ECMp.d) {
                return true;
            }
            v = v.getNext();
        } while (!v.isIntPoint());
        return false;
    }

    private static Vert snap(Vect2d p, Sector s) {
        // snap p to the closest segment of target contour
        Vect2d closest, current;
        Vert closestEdge;
        double distance;// = ECMp.d + 1; // must be closer then d+1, good starting value
        double tempDis;

        Vert v = s.getTarStart().getPrev(); //include the edge to the starting intersect pount
        distance = Vect2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
        v = v.getNext();
        closest = v.getPoint();
        closestEdge = v;
        do {
            current = Vect2d.PointToSegment(p, v.getPoint(), v.getNext().getPoint());
            tempDis = Vect2d.lengthP2P(p, current);

            if (tempDis < distance) {
                closest = current;
                closestEdge = v;
                distance = tempDis;
            }
            v = v.getNext();
        } while (!v.isIntPoint());

        //p.setX(closest.getX());
        //p.setY(closest.getY());

        return closestEdge;
    }

    private static Vect2d fieldAt(Vect2d p, Sector s) {


        // Use line charges or point charges. remove if for speed
        //return fieldAtLines(p, s);
        if (ECMp.lineCharges) {
            return fieldAtLines(p, s);
        } else {
            return fieldAtPoints(p, s);
        }
    }

    private static Vect2d fieldAtPoints(Vect2d p, Sector s) {
        //calc the field size at p according to to migrating and target charges
        Vect2d field = new Vect2d(0, 0);
        Vect2d totalF = new Vect2d(0, 0);

        Vert v = s.migCharges.getHead();
        do {

            forceP(field, p, v.getPoint(), ECMp.migQ, ECMp.migPower);
            totalF.addVec(field);
            //totalF.print("\ttotlaF = ");
            v = v.getNext();
        } while (!v.getPrev().isIntPoint() || v.getPrev().isHead());

        v = s.tarCharges.getHead();
        do {
            forceP(field, p, v.getPoint(), ECMp.tarQ, ECMp.tarPower);
            totalF.addVec(field);
            v = v.getNext();
        } while (!v.getPrev().isIntPoint() || v.getPrev().isHead());

        return totalF;
    }

    private static void forceP(Vect2d force, Vect2d p, Vect2d pQ, double q, double power) {
        double r = Vect2d.lengthP2P(pQ, p);
        //System.out.println("\t r = " + r);
        if (r == 0) {
            force.setX(250);
            force.setY(250);
            IJ.log("!WARNING-FORCE INFINITE");
            return;
        }
        r = Math.abs(Math.pow(r, power));
        Vect2d unitV = Vect2d.unitVector(pQ, p);
        double multiplier = (ECMp.k * (q / r));
        force.setX(unitV.getX() * multiplier);
        force.setY(unitV.getY() * multiplier);
    }

    private static Vect2d fieldAtLines(Vect2d p, Sector s) {
        //calc the field size at p according to to migrating and target charges
        Vect2d field = new Vect2d(0, 0);
        Vect2d totalF = new Vect2d(0, 0);
        double polarDir;


        // inside or outside sector?
        inside = s.insideCharges(p);

        if (!inside) {
            polarDir = -1;
            //System.out.println("switched");
        } else {
            polarDir = 1;
        }

        Vert v = s.migCharges.getHead();
        do {

            //forceL(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.migQ);

            /*
             * //times by the outerDirection to make lines polar sideDis =
             * Vect2d.distPoinToInfLine(p, v.getPoint(),
             * v.getNext().getPoint()); if (sideDis < 0) { polarDir =
             * s.outerDirection * -1; } else { polarDir = s.outerDirection; }
             *
             */

            forceLpolar(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.migQ, ECMp.migPower, polarDir);

            totalF.addVec(field);
            v = v.getNext();
        } while (!v.isIntPoint() || v.isHead());

        v = s.tarCharges.getHead();
        do {
            //forceL(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.tarQ);

            /*
             * sideDis = Vect2d.distPoinToInfLine(p, v.getPoint(),
             * v.getNext().getPoint()); if (sideDis < 0) { polarDir =
             * s.outerDirection ; } else { polarDir = s.outerDirection * -1; }
             *
             */


            forceLpolar(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.tarQ, ECMp.tarPower, polarDir);

            totalF.addVec(field);
            v = v.getNext();
        } while (!v.isIntPoint() || v.isHead());

        return totalF;
    }

    private static void forceL(Vect2d force, Vect2d p, Vect2d s1, Vect2d s2, double q) {
        double L = Vect2d.lengthP2P(s1, s2);
        Vect2d rU = Vect2d.unitVector(s2, p);
        double r = Vect2d.lengthP2P(s2, p);
        Vect2d rpU = Vect2d.unitVector(s1, p);
        double rp = Vect2d.lengthP2P(s1, p);

        double d = (((rp + r) * (rp + r)) - (L * L)) / (2 * L);
        //double d = ( Math.pow((rp + r), power) - (L * L)) / (2 * L);
        double multiplier = ((ECMp.k * q) / d);
        rpU.addVec(rU);

        force.setX(rpU.getX() * multiplier);
        force.setY(rpU.getY() * multiplier);
    }

    private static void forceLpolar(Vect2d force, Vect2d p, Vect2d s1, Vect2d s2, double q, double power, double orientation) {
        double L = Vect2d.lengthP2P(s1, s2);
        Vect2d rU = Vect2d.unitVector(s2, p);
        double r = Vect2d.lengthP2P(s2, p);
        Vect2d rpU = Vect2d.unitVector(s1, p);
        double rp = Vect2d.lengthP2P(s1, p);

        double d = (((rp + r) * (rp + r)) - (L * L)) / (2 * L);
        //double d = ( Math.pow((rp + r), power) - (L * L)) / (2 * L);
        double multiplier = ((ECMp.k * q) / d);
        rpU.addVec(rU);


        force.setX(rpU.getX() * multiplier * orientation);
        force.setY(rpU.getY() * multiplier * orientation);
    }

    private static double sampleFluo(int x, int y) {
        double tempFlu = ECMp.image.getPixelValue(x, y) + ECMp.image.getPixelValue(x - 1, y)
                + ECMp.image.getPixelValue(x + 1, y) + ECMp.image.getPixelValue(x, y - 1)
                + ECMp.image.getPixelValue(x, y + 1) + ECMp.image.getPixelValue(x - 1, y - 1)
                + ECMp.image.getPixelValue(x + 1, y + 1) + ECMp.image.getPixelValue(x + 1, y - 1)
                + ECMp.image.getPixelValue(x - 1, y + 1);
        tempFlu = tempFlu / 9d;
        return tempFlu;
    }
}



class ECMp {

    static public File INFILE; // snQP file
    static public File OUTFILE;
    static public double scale;
    static public double frameInterval;
    static public int startFrame, endFrame;
    static public ImageProcessor image;
    static public int numINTS;
    static public boolean ANA;
    static public boolean plot;
    static public boolean lineCharges;
    static public double markerRes;  // resolution of outlines
    static public double chargeDensity;// field complexity (set to -1 to leave as marker density)
    static public double maxVertF;   // max force allowed on a marker (0.06)
    static public double migPower;
    static public double tarPower;
    static public double migQ;  // was 0.4E-6
    static public double tarQ;
    static public double mobileQ;
    static public double d;//threshold distance to stop
    static public double w; // size of displacment of mig edge charges
    static public double h; //Euler time step, was 0.6
    static public int maxIter;
    static public double k;
    static public double anaMigDist;
    static public boolean forceNoSectors;
    static public boolean forceForwardMapping;
    static public boolean forceBackwardMapping;
    static public boolean disableDensityCorrections;
    static public int its;  // total euler iterations
    static public int unSnapped; //number of nodes that failed to snap
    static public int visualRes;
    static public double maxCellSize;
    
    static boolean drawIntersects;
    static boolean drawInitialOutlines;
    static boolean drawSolutionOutlines;
    static boolean drawPaths;
    static boolean drawFails;
    static boolean saveTemp;
    static boolean inspectSectors;
    
    public ECMp() {
    }

    public static void setParams(double maxCellLength) {
        maxCellSize = maxCellLength / Math.PI; // guess cell diameter

        lineCharges = true;
        markerRes = 4;  // resolution of outlines (set to 0 to not alter density, set negative to only alter at first frame)
        chargeDensity = -1; // field complexity (set to -1 to leave as marker density)
        maxVertF = 0.1;     // max force allowed on a marker (0.06)
        migPower = 2;
        tarPower = 2;
        migQ = 0.5E-6;  // was 0.4E-6
        tarQ = -0.5E-6; // was -2.5E-5
        mobileQ = 0.1E-5;
        d = 0.2; //threshold distance to stop
        w = 0.01; // size of displacment of mig edge charges (0.01)
        h = 0.3;  //Euler time step, was 0.6
        maxIter = 4000;
        k = 8.987E9;
        //static public boolean plot = true;
        inspectSectors = true;
        forceNoSectors = false;
        forceForwardMapping = false;
        forceBackwardMapping = false; // takes priority
        disableDensityCorrections = false;
        its = 0;   // total euler iterations
        unSnapped = 0; //number of nodes that failed to snap
        visualRes = 300; // set to 200! $
        
        saveTemp = false; // set to false!! $
        drawIntersects = true; // set to true!! $
        drawInitialOutlines = true; // set to true!! $
        drawSolutionOutlines = true; // set to true!! $
        drawPaths = true;
        drawFails = true;

    }

    static void setup(QParams qp) {
        INFILE = qp.snakeQP;
        OUTFILE = new File(ECMp.INFILE.getAbsolutePath()); // output file (.snQP) file
        scale = qp.imageScale;
        frameInterval = qp.frameInterval;
        //markerRes = qp.nodeRes;
        startFrame = qp.startFrame;
        endFrame = qp.endFrame;
        ECMp.ANA = false;
        ECMp.plot = true;
    }
}

class ECMplot {

    public ImagePlus imPlus;
    public ImageStack imStack;
    public ImageProcessor imProc;
    public int drawFrame = 1;
    public Vect2d centre;
    public double scale;
    public QColor color;
    private int intersectSize = 6;
    private int textPos = 25;
    public int w, h, f;
    //private int percentScreen = 65; //make visual output x% of screen height

    ECMplot(int ff) {

        //Dimension screen = IJ.getScreenSize();
        //ECMp.visualRes = (int) Math.round((screen.height / 100d) * percentScreen);
        double fitTo = ECMp.visualRes * 0.7;
        scale = fitTo / ECMp.maxCellSize;

        w = ECMp.visualRes;
        h = ECMp.visualRes;
        
        f = ff;
        centre = new Vect2d(0, 0);
        color = new QColor(1, 1, 1);
        //imPlus = NewImage.createByteImage("ECMM mappings", w, h, f, NewImage.FILL_BLACK);
        imPlus = NewImage.createRGBImage("ECMM_mappings", w, h, f, NewImage.FILL_WHITE);
        imStack = imPlus.getStack();
        imPlus.show();
    }

    public void setDrawingFrame(int d) {
        drawFrame = d - ECMp.startFrame + 1;
        textPos = 25;
        imProc = imStack.getProcessor(drawFrame);
        this.writeText("Frame map " + d + " to " + (d + 1));
    }

    public void writeText(String text) {
        this.setColor(0, 0, 0);
        imProc.drawString(text, 10, textPos);
        textPos += 15;
    }

    public void drawOutline(Outline o) {
        Vert v = o.getHead();
        do {
            drawLine(v, v.getNext());
            v = v.getNext();
        } while (!v.isHead());
    }

    public void drawPolygon(FloatPolygon p) {
        Vert va, vb;
        for (int i = 0; (i + 1) < p.xpoints.length; i++) {
            va = new Vert(p.xpoints[i], p.ypoints[i], i);
            vb = new Vert(p.xpoints[i + 1], p.ypoints[i + 1], i);
            drawLine(va, vb);

        }
    }

    public void drawLine(Vert v1, Vert v2) {

        Vect2d a = new Vect2d(v1.getX(), v1.getY());
        Vect2d b = new Vect2d(v2.getX(), v2.getY());
        relocate(a);
        relocate(b);

        imProc.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
    }

    public void setSlice(int f) {
        this.repaint();
        imPlus.setSlice(f);
    }

    public void drawPath(Vect2d[] data) {
        relocate(data[0]);
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i + 1] == null) {
                break;
            }
            relocate(data[i + 1]);
            imProc.drawLine((int) data[i].getX(), (int) data[i].getY(), (int) data[i + 1].getX(), (int) data[i + 1].getY());
        }
    }

    public void drawLine(Vect2d aa, Vect2d bb) {

        Vect2d a = new Vect2d(aa.getX(), aa.getY());
        Vect2d b = new Vect2d(bb.getX(), bb.getY());
        relocate(a);
        relocate(b);

        imProc.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
    }

    private void relocate(Vect2d p) {
        // move a point to the centre
        p.addVec(new Vect2d(-centre.getX(), -centre.getY()));
        p.multiply(scale);
        p.addVec(new Vect2d(ECMp.visualRes / 2, ECMp.visualRes / 2));

    }

    public void setColor(double r, double g, double b) {
        color.setRGB(r, g, b);
        imProc.setColor(color.getColorInt());
    }

    public void drawIntersect(Vert i) {
        this.drawCircle(i.getPoint(), intersectSize);
    }

    public void drawCircle(Vect2d a, int s) {
        Vect2d v = new Vect2d(a.getX(), a.getY());
        relocate(v);
        imProc.drawOval((int) v.getX() - (s / 2), (int) v.getY() - (s / 2), s, s);
    }

    public void drawCross(Vect2d a, int s) {
        Vect2d p = new Vect2d(a.getX(), a.getY());
        relocate(p);
        imProc.drawLine((int) p.getX() - s, (int) p.getY() - s, (int) p.getX() + s, (int) p.getY() + s);
        imProc.drawLine((int) p.getX() + s, (int) p.getY() - s, (int) p.getX() - s, (int) p.getY() + s);
    }

    public void repaint() {
        imPlus.repaintWindow();
    }

    public void close() {
        try {
            imPlus.close();
        } catch (Exception e) {
            System.out.println("ECMM Plot could not be closed (prob not open)");
        }
    }
}
