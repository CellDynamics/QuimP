/**
 * @file FakeSegmentationUI.java
 * @date 28 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Show UI for segmentation from masks and run it.
 * 
 * Modifies provided NEst reference
 * 
 * @author p.baniukiewicz
 * @date 28 Jun 2016
 *
 */
public class FakeSegmentationUI extends QWindowBuilder {
    private static final Logger LOGGER = LogManager.getLogger(FakeSegmentationUI.class.getName());

    private Nest nest; //!< reference to Nest object
    private JDialog dialog;
    private ParamList uiDefinition;

    /**
     * Construct object
     * 
     * @param nest Reference to NEst object (will be modified)
     */
    public FakeSegmentationUI(Nest nest) {
        this.nest = nest;
        uiDefinition = new ParamList(); // will hold ui definitions
        uiDefinition.put("name", "FakeSegmentationUI"); // name of window
        uiDefinition.put("Load Mask", "button, Load_mask");
        uiDefinition.put("Step", "spinner, 1, 51, 1," + Integer.toString(1));
        uiDefinition.put("Smooth", "checkbox, true, interpolation," + Boolean.toString(true));
        uiDefinition.put("help", "Window must be uneven. Set 1 to switch filter off.");
        buildWindow(uiDefinition);

    }

    private void run() {
        ImagePlus mask = IJ.openImage("src/test/resources/BW_seg_5_slices.tif");
        FakeSegmentation obj = new FakeSegmentation(mask);
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        // set interpolation params
        for (ArrayList<SegmentedShapeRoi> asS : ret)
            for (SegmentedShapeRoi sS : asS)
                sS.setInterpolationParameters(1, false);
        nest.cleanNest();
        nest.addHandlers(ret);
    }

    public void show() {
        toggleWindow(true);
    }

    /**
     * Destroy window on exit
     * 
     * @author p.baniukiewicz
     * @date 22 Apr 2016
     *
     */
    class myWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent we) {
            dialog.dispose();
        }
    }

}
