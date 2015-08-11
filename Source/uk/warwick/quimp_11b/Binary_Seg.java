/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.warwick.quimp_11b;

/**
 *
 * @author rtyson
 */
import ij.*;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.PlugIn;
import ij.process.ImageStatistics;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

/*
public class Binary_Seg implements PlugInFilter{

private int options = ParticleAnalyzer.SHOW_PROGRESS + ParticleAnalyzer.ADD_TO_MANAGER + ParticleAnalyzer.SHOW_ROI_MASKS;
private int measurements = 0;
private ResultsTable rt;
private double minSize = 50;
private double maxSize = Double.POSITIVE_INFINITY;

ParticleAnalyzer pa;

public void run(ImageProcessor orgIp){



pa.run(orgIp);
}

public int setup(String string, ImagePlus ip) {

IJ.log("##############################################\n \n" +
"QuimP10a - Binary Segmentation Plugin (10-10-2010),\nby Richard Tyson (R.A.Tyson@warwick.ac.uk)\n"
+ "& Till Bretschneider (T.Bretschneider@warwick.ac.uk)\n\n"
+ "##############################################\n \n");

if( IJ.versionLessThan("1.43") ) return 0;


if (ip == null) {
IJ.error("Image stack required..");
//capability flags of the filter (instance variables)
return DOES_8G + DOES_16 + STACK_REQUIRED + NO_CHANGES;
}

pa = new ParticleAnalyzer(options, measurements, rt, minSize,  maxSize);
pa.setup(string, ip);

return DOES_8G + DOES_16 + STACK_REQUIRED + NO_CHANGES;
}
}

 */
public class Binary_Seg implements PlugIn {

    static boolean use_previous_snake = true;  // next contraction begins with last chain
    static private double nodeRes = 4.;
    static int blowup = 25;            // distance to blow up chain
    static double vel_crit = 0.001;
    static double f_central = 0.04;
    static double f_image = 0.2;     // image force
    //
    //advanced parameters
    static int NMAX = 150;             // maximum number of nodes (% of starting nodes)
    static double delta_t = 1.;
    static int max_iterations = 2000;  // max iterations per contraction
    static int sample_tan = 4;
    static int sample_norm = 12;
    static double f_contract = 0.015;
    static double f_friction = 0.1;
    static double sensitivity = 0.5;

    ImagePlus orgIpl;
    OutlineHandler OH;
    File outFile, orgFile, paramFile;
    String fileName;
    double imageScale;
    double imageFrameInterval;

    public void run(String arg) {

        IJ.log("##############################################\n \n"
                + "QuimP10a - Binary Segmentation Plugin (10-10-2010),\nby Richard Tyson (R.A.Tyson@warwick.ac.uk)\n"
                + "& Till Bretschneider (T.Bretschneider@warwick.ac.uk)\n\n"
                + "##############################################\n \n");

        if (IJ.versionLessThan("1.43")) {
            return;
        }

        orgIpl = IJ.getImage();
        setup();

        analyzeStackParticles();
    }

    public void analyzeStackParticles() {
        if (orgIpl.getBitDepth() == 24) {
            IJ.error("Grayscale image required");
            return;
        }
        CustomParticleAnalyzer pa = new CustomParticleAnalyzer();
        int flags = pa.setup("", orgIpl);
        if (flags == PlugInFilter.DONE) {
            return;
        }
        if ((flags & PlugInFilter.DOES_STACKS) != 0) {
            for (int i = 1; i <= orgIpl.getStackSize(); i++) {
                orgIpl.setSlice(i);
                pa.run(orgIpl.getProcessor());
            }
        } else {
            pa.run(orgIpl.getProcessor());
        }

        OH = pa.getOutlineHandler();

        for (int j = 0; j < OH.getSize(); j++) {
            OH.getOutline(j).setResolution(nodeRes);
        }

        if(!setSaveLocations()) return;

        new CellStat(OH, orgIpl, new File(outFile.getParent(), fileName + "_0.stQP.csv"),
                imageScale, imageFrameInterval);


        try {
            OH.writeOutlines(outFile, false);
            writeParams(0);
        } catch (Exception e) {
            IJ.error("Could not save param file");
        }
    }

    private boolean setSaveLocations() {
        String saveIn = orgFile.getParent();
        //System.out.println(orgFile.getParent());
        if (!orgFile.exists()) {
            IJ.log("image is not saved to disk!");
            saveIn = OpenDialog.getLastDirectory();
        }


        SaveDialog sd = new SaveDialog("Save data...", saveIn, fileName, "");

        if (sd.getFileName() == null) {
            return false;
        }
        outFile = new File(sd.getDirectory(), sd.getFileName() + "_0.snQP");
        fileName = sd.getFileName();


        return true;
    }

    public void writeParams(int sID) throws Exception {
        // write paramters to .prQP file

        paramFile = new File(outFile.getParent(), fileName + "_" + sID + ".paQP");

        // overwite if already exists
        if (paramFile.exists()) {
            paramFile.delete();
        }

        Random generator = new Random();
        double d = generator.nextDouble() * 1000000;    // 6 digit key to ID job
        long key = Math.round(d);

        PrintWriter pPW = new PrintWriter(new FileWriter(paramFile), true); //auto flush

        pPW.print("#p - Boa parameter file (BINARY!)\n");
        pPW.print(IJ.d2s(key, 6) + "\n");
        pPW.print(orgFile.getAbsolutePath() + "\n");
        pPW.print("./" + outFile.getName() + "\n");
        //pPW.print(outFile.getAbsolutePath() + "\n");

        pPW.print("#Image calibration\n");
        pPW.print(IJ.d2s(imageScale, 6) + "\n");
        pPW.print(IJ.d2s(imageFrameInterval, 6) + "\n");

        pPW.print("#segmentation parameters\n");
        pPW.print(IJ.d2s(NMAX, 6) + "\n");
        pPW.print(IJ.d2s(delta_t, 6) + "\n");
        pPW.print(IJ.d2s(max_iterations, 6) + "\n");
        pPW.print(IJ.d2s(nodeRes, 6) + "\n");
        pPW.print(IJ.d2s(blowup, 6) + "\n");
        pPW.print(IJ.d2s(sample_tan, 6) + "\n");
        pPW.print(IJ.d2s(sample_norm, 6) + "\n");
        pPW.print(IJ.d2s(vel_crit, 6) + "\n");
        pPW.print(IJ.d2s(f_central, 6) + "\n");
        pPW.print(IJ.d2s(f_contract, 6) + "\n");
        pPW.print(IJ.d2s(f_friction, 6) + "\n");
        pPW.print(IJ.d2s(f_image, 6) + "\n");
        //pPW.print(IJ.d2s(p.cortex_width, 6) + "\n");
        pPW.print(IJ.d2s(sensitivity, 6) + "\n");
        //pPW.print(IJ.d2s(cut_every, 6) + "\n");


        pPW.close();

    }

    private void setup() {
        FileInfo fileinfo = orgIpl.getOriginalFileInfo();
        if (fileinfo == null) {
            //System.out.println("1671-No file Info, use " + orgIpl.getTitle());
            orgFile = new File(File.separator, orgIpl.getTitle());
        } else {
            //System.out.println("1671-file Info, filename: " + fileinfo.fileName);
            orgFile = new File(fileinfo.directory, fileinfo.fileName);
        }
        fileName = Tool.removeExtension(orgFile.getName());

        // extract fileName without extension

        //int dotI = fileName.lastIndexOf(".");
        //if (dotI > 0) {
        //fileName = fileName.substring(0, dotI);
        //}
        // System.out.println("fileName: " + fileName);

        //FRAMES = orgIpl.getStackSize(); // get number of frames
        imageFrameInterval = orgIpl.getCalibration().frameInterval;
        imageScale = orgIpl.getCalibration().pixelWidth;
        if (imageFrameInterval == 0) {
            imageFrameInterval = 1;
            IJ.log("Warning. Frame interval was 0 sec. Using 1 sec instead"
                    + "\n\t[set in 'image->Properties...']");
        }
        if (imageScale == 0) {
            imageScale = 1;
            IJ.log("Warning. Scale was 1 pixel == 0 \u00B5m. Using 1 \u00B5m instead"
                    + "\n\t(set in 'Analyze->Set Scale...')");
        }
    }
}

class CustomParticleAnalyzer extends ParticleAnalyzer {

    private OutlineHandler OH;

    // Overrides method with the same in AnalyzeParticles that's called once for each particle
    protected void saveResults(ImageStatistics stats, Roi roi) {
        //Outline o = new Outline(roi);
        //set res?
        OH.setOutline(imp.getCurrentSlice() - 1, new Outline(roi));
        super.saveResults(stats, roi);
    }

    public int setup(String arg, ImagePlus imp) {
        OH = new OutlineHandler(1,imp.getStackSize());
        return super.setup(arg, imp);
    }

    public OutlineHandler getOutlineHandler() {
        return OH;
    }
}
