package uk.ac.warwick.wsbc.QuimP.plugin.ana;

import java.io.File;

import uk.ac.warwick.wsbc.QuimP.FormatConverter;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * Container class for parameters concerned with ANA analysis.
 * 
 * This class is serialized through {@link uk.ac.warwick.wsbc.QuimP.filesystem.ANAParamCollection}.
 * The structure of transient and non-transient fields must be reflected in FormatConverter.
 * 
 * @author rtyson
 * @see FormatConverter#doConversion()
 */
public class ANAp {

    transient public File INFILE;
    transient public File OUTFILE;
    transient public File STATSFILE;
    transient public File FLUOFILE;
    private double cortexWidthPixel; // in pixels
    private double cortexWidthScale; // at scale
    public File[] fluTiffs;
    transient public double stepRes = 0.04; // step size in pixels
    transient public double freezeTh = 1;
    transient public double angleTh = 0.1;
    transient public double oneFrameRes = 1;
    transient public double scale = 1.0;
    transient public double frameInterval;
    transient public int startFrame, endFrame;
    transient boolean normalise = true;
    transient boolean sampleAtSame = false;
    transient int[] presentData;
    transient boolean cleared;
    transient boolean noData;
    transient int channel = 0;
    transient int useLocFromCh;
    transient boolean plotOutlines = false; // plot outlines on new image

    public ANAp() {
        fluTiffs = new File[3];
        fluTiffs[0] = new File("/");
        fluTiffs[1] = new File("/");
        fluTiffs[2] = new File("/");
        presentData = new int[3];
        setCortextWidthScale(0.7); // default value
    }

    /**
     * Copy constructor.
     * 
     * @param src
     */
    public ANAp(ANAp src) {
        this.INFILE = new File(src.INFILE.getAbsolutePath());
        this.OUTFILE = new File(src.OUTFILE.getAbsolutePath());
        this.STATSFILE = new File(src.STATSFILE.getAbsolutePath());
        this.FLUOFILE = new File(src.FLUOFILE.getAbsolutePath());
        this.cortexWidthPixel = src.cortexWidthPixel;
        this.cortexWidthScale = src.cortexWidthScale;
        this.stepRes = src.stepRes;
        this.freezeTh = src.freezeTh;
        this.angleTh = src.angleTh;
        this.oneFrameRes = src.oneFrameRes;
        this.scale = src.scale;
        this.frameInterval = src.frameInterval;
        this.startFrame = src.startFrame;
        this.endFrame = src.endFrame;
        this.normalise = src.normalise;
        this.sampleAtSame = src.sampleAtSame;
        this.presentData = new int[src.presentData.length];
        System.arraycopy(src.presentData, 0, this.presentData, 0, src.presentData.length);
        this.cleared = src.cleared;
        this.noData = src.noData;
        this.channel = src.channel;
        this.useLocFromCh = src.useLocFromCh;
        this.plotOutlines = src.plotOutlines;

        this.fluTiffs = new File[src.fluTiffs.length];
        for (int i = 0; i < fluTiffs.length; i++)
            fluTiffs[i] = new File(src.fluTiffs[i].getPath());

    }

    /**
     * Initiates ANAp class with parameters copied from BOA analysis
     * 
     * @param qp reference to QParams container (master file and BOA params)
     */
    void setup(QParams qp) {
        channel = 0;
        INFILE = qp.getSnakeQP();
        OUTFILE = new File(INFILE.getAbsolutePath()); // output file (.snQP) file
        STATSFILE = new File(qp.getStatsQP().getAbsolutePath()); // output file
        // (.stQP.csv) file
        scale = qp.getImageScale();
        frameInterval = qp.getFrameInterval();
        setCortextWidthScale(qp.cortexWidth);
        startFrame = qp.getStartFrame();
        endFrame = qp.getEndFrame();
        cleared = false;
        noData = true;
    }

    public void setCortextWidthScale(double c) {
        cortexWidthScale = c;
        cortexWidthPixel = QuimpToolsCollection.distanceFromScale(cortexWidthScale, scale);
    }

    /**
     * @return the cortexWidthPixel
     */
    public double getCortexWidthPixel() {
        return cortexWidthPixel;
    }

    public double getCortexWidthScale() {
        return cortexWidthScale;
    }

    /**
     * @param cortexWidthPixel the cortexWidthPixel to set
     */
    public void setCortexWidthPixel(double cortexWidthPixel) {
        this.cortexWidthPixel = cortexWidthPixel;
    }
}