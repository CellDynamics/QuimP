/**
 * @file Prot_Analysis.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.nio.file.Path;

import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.process.FloatProcessor;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 * @date 15 Aug 2016
 *
 */
public class Prot_Analysis extends QuimpPluginCore {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }

    /**
     * Default constructor. 
     * <p>
     * Run parameterized constructor with <tt>null</tt> showing file selector.
     */
    public Prot_Analysis() {
        this(null);
    }

    /**
     * @param path
     */
    public Prot_Analysis(Path path) {
        super(path);
        IJ.showStatus("Protrusion Analysis");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#showDialog()
     */
    @Override
    public boolean showDialog() {
        // TODO Auto-generated method stub
        return super.showDialog();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#validateQconf()
     */
    @Override
    public boolean validateQconf() throws QuimpException {
        return super.validateQconf();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#runFromQCONF()
     */
    @Override
    public void runFromQCONF() {
        STmap[] stMap = qp.getLoadedDataContainer().QState;
        for (STmap mapCell : stMap) { // iterate through cells
            float[][] motMap = QuimPArrayUtils.double2float(mapCell.motMap);
            mapCell.map2ImagePlus("motility_map", new FloatProcessor(motMap)).show();
        }
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#runFromPAQP()
     */
    @Override
    public void runFromPAQP() {
        // TODO Auto-generated method stub
        super.runFromPAQP();
    }

}
