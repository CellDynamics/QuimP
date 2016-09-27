package uk.ac.warwick.wsbc.QuimP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Hold statistic evaluated for one frame.
 * 
 * @author p.baniukiewicz
 * @see {@link uk.ac.warwick.wsbc.QuimP.CellStatsEval} 
 */
public class FrameStatistics {
    public int frame;
    public double area;
    // double totalFlour;
    // double meanFlour;
    public ExtendedVector2d centroid;
    public double elongation;
    public double circularity;
    public double perimiter;
    public double displacement;
    public double dist;
    public double persistance;
    public double speed; // over 1 frame
    public double persistanceToSource;
    public double dispersion;
    public double extension;
    ChannelStat[] channels;
    // int cellAge;

    public FrameStatistics() {
        centroid = new ExtendedVector2d();
        channels = new ChannelStat[3];
        channels[0] = new ChannelStat();
        channels[1] = new ChannelStat();
        channels[2] = new ChannelStat();
    }

    public void toScale(double scale, double frameInterval) {
        area = Tool.areaToScale(area, scale);
        perimiter = Tool.distanceToScale(perimiter, scale);
        displacement = Tool.distanceToScale(displacement, scale);
        dist = Tool.distanceToScale(dist, scale);
        speed = Tool.speedToScale(speed, scale, frameInterval); // over 1 frame
    }

    void centroidToPixels(double scale) {
        centroid.setXY(centroid.getX() / scale, centroid.getY() / scale);
    }

    public void clearFluo() {
        this.channels[0] = new ChannelStat();
        this.channels[1] = new ChannelStat();
        this.channels[2] = new ChannelStat();
    }

    public static void write(FrameStatistics[] s, File OUTFILE, ANAp anap) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(OUTFILE), true); // auto flush
        IJ.log("Writing to file");
        pw.print("#p2\n#QuimP ouput - " + OUTFILE.getAbsolutePath() + "\n");
        pw.print(
                "# Centroids are given in pixels.  Distance & speed & area measurements are scaled to micro meters\n");
        pw.print("# Scale: " + anap.scale + " micro meter per pixel | Frame interval: "
                + anap.frameInterval + " sec\n");
        pw.print("# Frame,X-Centroid,Y-Centroid,Displacement,Dist. Traveled,"
                + "Directionality,Speed,Perimeter,Elongation,Circularity,Area");

        for (int i = 0; i < s.length; i++) {
            pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].centroid.getX(), 2) + ","
                    + IJ.d2s(s[i].centroid.getY(), 2) + "," + IJ.d2s(s[i].displacement) + ","
                    + IJ.d2s(s[i].dist) + "," + IJ.d2s(s[i].persistance) + "," + IJ.d2s(s[i].speed)
                    + "," + IJ.d2s(s[i].perimiter) + "," + IJ.d2s(s[i].elongation) + ","
                    + IJ.d2s(s[i].circularity, 3) + "," + IJ.d2s(s[i].area));
        }
        pw.print("\n#\n# Fluorescence measurements");
        writeFluo(s, pw, 0);
        writeFluo(s, pw, 1);
        writeFluo(s, pw, 2);
        pw.close();
    }

    private static void writeFluo(FrameStatistics[] s, PrintWriter pw, int c) {
        pw.print("\n#\n# Channel " + (c + 1)
                + ";Frame, Total Fluo.,Mean Fluo.,Cortex Width, Cyto. Area,Total Cyto. Fluo., Mean Cyto. Fluo.,"
                + "Cortex Area,Total Cortex Fluo., Mean Cortex Fluo., %age Cortex Fluo.");
        for (int i = 0; i < s.length; i++) {
            pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].channels[c].totalFluor) + ","
                    + IJ.d2s(s[i].channels[c].meanFluor) + ","
                    + IJ.d2s(s[i].channels[c].cortexWidth));
            pw.print("," + IJ.d2s(s[i].channels[c].innerArea) + ","
                    + IJ.d2s(s[i].channels[c].totalInnerFluor) + ","
                    + IJ.d2s(s[i].channels[c].meanInnerFluor));
            pw.print("," + IJ.d2s(s[i].channels[c].cortexArea) + ","
                    + IJ.d2s(s[i].channels[c].totalCorFluo) + ","
                    + IJ.d2s(s[i].channels[c].meanCorFluo) + ","
                    + IJ.d2s(s[i].channels[c].percCortexFluo));
        }
    }

    public static FrameStatistics[] read(File INFILE) throws IOException {

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
        FrameStatistics[] stats = new FrameStatistics[i];

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

            stats[i] = new FrameStatistics();
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
    }

    private static void readChannel(int c, FrameStatistics[] stats, BufferedReader br)
            throws IOException {
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
}