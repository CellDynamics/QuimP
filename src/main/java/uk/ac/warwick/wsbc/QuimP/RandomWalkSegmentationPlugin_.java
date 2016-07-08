/**
 * @file RandomWalkSegmentationPlugin.java
 * @date 4 Jul 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Params;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Point;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.PropagateSeeds;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkException;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentation;

/**
 * Run RandomWalkSegmentation in IJ environment.
 * 
 * Implements common PlugIn interface as both images are provided after run.
 * The seed can be one image - in this case seed propagation is used to generate seed for
 * subsequent frames, or it can be stack of the same size as image. In latter case every slice
 * from seed is used for seeding related slice from image.
 *  
 * @author p.baniukiewicz
 * @date 4 Jul 2016
 *
 */
public class RandomWalkSegmentationPlugin_ implements PlugIn, ActionListener, ChangeListener {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER =
            LogManager.getLogger(RandomWalkSegmentationPlugin_.class.getName());

    private ImagePlus image; //!< stack or image to segment
    private ImagePlus seedImage; //!< RGB seed image
    private Params params; // parameters
    private int erodeIter; //!< number of erosions for generating next seed from previous
    private boolean useSeedStack; //!< \a true if seed has the same size as image, slices are seeds 

    private JComboBox<String> cImage,cSeed;
    private JButton bClone;
    private JToggleButton bBack,bFore;
    private JSpinner sAlpha,sBeta,sGamma,sIter,sErode;
    private JButton bCancel,bApply,bHelp;
    
    JFrame wnd;
    
    /**
     * 
     * @return
     */
    public boolean showNewDialog() {
        wnd = new JFrame("Random Walker Segmentation");
        wnd.setResizable(false);
        JPanel panel = new JPanel(new BorderLayout());

        // Choices zone (upper)
        JPanel comboPanel = new JPanel();
        comboPanel.setBorder(BorderFactory.createTitledBorder("Image selection"));
        comboPanel.setLayout(new GridLayout(4, 1, 2, 2));
        cImage = new JComboBox<String>(WindowManager.getImageTitles());
        cImage.addActionListener(this);
        cSeed = new JComboBox<String>(WindowManager.getImageTitles());
        cSeed.addActionListener(this);
        comboPanel.add(new JLabel("Original image"));
        comboPanel.add(cImage);
        comboPanel.add(new JLabel("Seed image"));
        comboPanel.add(cSeed);

        // Seed build zone (middle)
        JPanel seedBuildPanel = new JPanel();
        seedBuildPanel.setBorder(BorderFactory.createTitledBorder("Seed build"));
        seedBuildPanel.setLayout(new GridLayout(1, 3, 2, 2));
        bClone = new JButton("Clone");
        bClone.setToolTipText("Clone selected original image and allow to seed it manually");
        bClone.addActionListener(this);
        bFore = new JToggleButton("FG");
        bFore.setToolTipText("Select Foreground pen");
        bFore.setBackground(Color.ORANGE);
        bFore.addActionListener(this);
        bBack = new JToggleButton("BG");
        bBack.setToolTipText("Select Background pen");
        bBack.setBackground(Color.GREEN);
        bBack.addActionListener(this);
        seedBuildPanel.add(bClone);
        seedBuildPanel.add(bFore);
        seedBuildPanel.add(bBack);

        // Options zone (middle)
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Segmentation options"));
        optionsPanel.setLayout(new GridLayout(5, 2, 2, 2));
        sAlpha = new JSpinner(new SpinnerNumberModel(400, 1, 100000, 1));
        sAlpha.addChangeListener(this);
        optionsPanel.add(new JLabel("Alpha"));
        optionsPanel.add(sAlpha);
        sBeta = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
        sBeta.addChangeListener(this);
        optionsPanel.add(new JLabel("Beta"));
        optionsPanel.add(sBeta);
        sGamma = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
        sGamma.addChangeListener(this);
        optionsPanel.add(new JLabel("Gamma"));
        optionsPanel.add(sGamma);
        sIter = new JSpinner(new SpinnerNumberModel(80, 1, 1000, 1));
        sIter.addChangeListener(this);
        optionsPanel.add(new JLabel("Iterations"));
        optionsPanel.add(sIter);
        sErode = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        sErode.addChangeListener(this);
        optionsPanel.add(new JLabel("Erode power"));
        optionsPanel.add(sErode);

        // integrate middle panels into one
        JPanel seedoptionsPanel = new JPanel();
        seedoptionsPanel.setLayout(new GridBagLayout()); // prevent equal sizes
        GridBagConstraints c = new GridBagConstraints();
        // c.gridheight = 1;
        // c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 50;
        c.fill = GridBagConstraints.HORIZONTAL;
        seedoptionsPanel.add(seedBuildPanel, c);
        // c.gridheight = 5;
        // c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        seedoptionsPanel.add(optionsPanel, c);

        // cancel apply row
        JPanel caButtons = new JPanel();
        caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        bApply = new JButton("Apply");
        bApply.addActionListener(this);
        bCancel = new JButton("Cancel");
        bCancel.addActionListener(this);
        bHelp = new JButton("Help");
        bHelp.addActionListener(this);
        caButtons.add(bApply);
        caButtons.add(bCancel);
        caButtons.add(bHelp);

        // build window
        panel.add(comboPanel, BorderLayout.NORTH);
        panel.add(seedoptionsPanel, BorderLayout.CENTER);
        // panel.add(optionsPanel,BorderLayout.SOUTH);
        panel.add(caButtons, BorderLayout.SOUTH);
        wnd.add(panel);
        wnd.pack();
        wnd.setVisible(true);

        // disable on start only if there is no image
        if (cImage.getSelectedItem() == null) // if not null it must be string
            bClone.setEnabled(false);
        else
            bClone.setEnabled(true);
        if (cSeed.getSelectedItem() == null) {
            bFore.setEnabled(false);
            bBack.setEnabled(false);
        } else {
            bFore.setEnabled(true);
            bBack.setEnabled(true);
        }

        return true;
    }

    /**
     * Shows user dialog and check conditions.
     * 
     * @return \c true if user clicked \b OK and input data are correct or 
     * return \c false otherwise
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("Random Walk segmentation");
        gd.addChoice("Image", WindowManager.getImageTitles(), ""); // image to be segmented
        gd.addChoice("Seed", WindowManager.getImageTitles(), ""); // seed image
        gd.addNumericField("alpha", 400, 0, 6, ""); // alpha
        gd.addNumericField("beta", 50, 2, 6, ""); // beta
        gd.addNumericField("gamma", 100, 2, 6, ""); // gamma
        gd.addNumericField("Iterations", 80, 3);
        gd.addNumericField("erode iterations", 5, 0, 2, "");

        //!<
        gd.addMessage(
                "The erode iterations depend\n"
              + "on how fast cells move or how\n"
              + "big are differences betweenn\n"
              + "succeeding frames.");
        /**/
        gd.setResizable(false);
        gd.showDialog();
        // user response, return false on any error
        if (gd.wasCanceled()) // check if user clicked OK or CANCEL
            return false;
        image = WindowManager.getImage(gd.getNextChoice());
        seedImage = WindowManager.getImage(gd.getNextChoice());
        if (image.getBitDepth() != 8 && image.getBitDepth() != 16) {
            IJ.showMessage("Error", "Image must be 8 or 16 bit");
            return false; // wrong image type
        }
        if (seedImage.getBitDepth() != 24) {
            IJ.showMessage("Error", "Seed image must be 24 bit");
            return false; // wrong seed
        }
        if (seedImage.getStackSize() == 1)
            useSeedStack = false; // use propagateSeed for generating next frame seed from previous
        else if (seedImage.getStackSize() == image.getStackSize())
            useSeedStack = true; // use slices as seeds
        else {
            IJ.showMessage("Error", "Seed must be image or stack of the same size as image");
            return false; // wrong seed size
        }

        // read GUI elements and store results in private fields order as these
        // methods are called should match to GUI build order
        //!<
        params = new Params(gd.getNextNumber(), // alpha
                gd.getNextNumber(), // beta
                gd.getNextNumber(), // gamma1
                0, // not used gamma 2
                (int) gd.getNextNumber(), // iterations
                0.1, // dt
                8e-3 // error
                );
        /**/
        erodeIter = (int) Math.round(gd.getNextNumber()); // erosions
        if (gd.invalidNumber()) { // check if numbers in fields were correct
            IJ.error("Not valid number");
            LOGGER.error("One of the numbers in dialog box is not valid");
            return false;
        }
        return true; // all correct
    }

    /**
     * Plugin runner. 
     * 
     * Shows UI and perform segmentation after validating UI
     */
    @Override
    public void run(String arg) {
        ImageStack ret; // all images treated as stacks
        Map<Integer, List<Point>> seeds;
        if (showDialog()) { // returned true - all fields ok and initialized correctly
            if (image == null || seedImage == null) {
                IJ.showMessage("Error", "Select both images first");
            }
            try {
                ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
                ImageStack is = image.getStack(); // get current stack (size 1 for one image)
                // segment first slice (or image if it is not stack)
                RandomWalkSegmentation obj = new RandomWalkSegmentation(is.getProcessor(1), params);
                seeds = obj.decodeSeeds(seedImage.getStack().getProcessor(1), Color.RED,
                        Color.GREEN); // generate seeds
                ImageProcessor retIp = obj.run(seeds); // segmentation
                ret.addSlice(retIp.convertToByte(true)); // store output in new stack
                // iterate over all slices after first (may not run for one image)
                for (int s = 2; s <= is.getSize(); s++) {
                    Map<Integer, List<Point>> nextseed;
                    obj = new RandomWalkSegmentation(is.getProcessor(s), params);
                    // get seeds from previous result
                    if (useSeedStack) { // true - use slices
                        nextseed = obj.decodeSeeds(seedImage.getStack().getProcessor(s), Color.RED,
                                Color.GREEN);
                    } else // false - use previous frame
                        nextseed = PropagateSeeds.propagateSeed(retIp, erodeIter);
                    retIp = obj.run(nextseed); // segmentation and results stored for next seeding
                    ret.addSlice(retIp); // add next slice
                    IJ.showProgress(s - 1, is.getSize());
                }
                // convert to ImagePlus and show
                ImagePlus segmented = new ImagePlus("Segmented_" + image.getTitle(), ret);
                segmented.show();
                segmented.updateAndDraw();
            } catch (RandomWalkException e) {
                LOGGER.error("Segmentation failed because: " + e.getMessage());
            }

        }

    }

    /**
     * Verify all entries in window and set them as final assigning to object variables.
     * All operations are here
     * 
     * @param e Component
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        // Start data verification, show message on problem and exit method setting FGBG unselected
        // 0. check if we can paint on selected image if user try
        if (b == bFore || b == bBack) {
            ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem());
            if (tmpSeed.getBitDepth() != 24) {
                JOptionPane.showMessageDialog(wnd, "Seed image must be 24 bit RGB type", "Error",
                        JOptionPane.ERROR_MESSAGE);
                bFore.setSelected(false);
                bBack.setSelected(false);
                return; // we cant - return
            }
        }
        if (b == bFore) {
            if (((JToggleButton) b).isSelected()) {
                IJ.run("brush");
            } else {
                // unselect
            }
        }
        if (b == bApply) {
            // verify data before - store data in object after verification
            ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem()); // tmp var
            ImagePlus tmpImage = WindowManager.getImage((String) cImage.getSelectedItem());
            // 1. Verify sizes - must be the same
            if (tmpSeed.getWidth() != tmpImage.getWidth()
                    && tmpSeed.getHeight() != tmpImage.getHeight()) {
                JOptionPane.showMessageDialog(wnd, "Images are incompatibile in size", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // when wrong sizes
            }
            // 2. Check seed bitDepth
            if (tmpSeed.getBitDepth() != 24) {
                JOptionPane.showMessageDialog(wnd, "Seed image must be 24 bit RGB type", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // when no 24 bit depth
            }
            // 3. Check stack size compatibility
            if (tmpSeed.getStackSize() == 1)
                useSeedStack = false; // use propagateSeed for generating next frame seed from prev
            else if (tmpSeed.getStackSize() == tmpImage.getStackSize())
                useSeedStack = true; // use slices as seeds
            else {
                JOptionPane.showMessageDialog(wnd,
                        "Seed must be image or stack of the same size as image", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // wrong seed size
            }
            // 4. Read numeric data
            //!<
            params = new Params((Integer)sAlpha.getValue(), // alpha
                    (Integer)sBeta.getValue(), // beta
                    (Integer)sGamma.getValue(), // gamma1
                    0, // not used gamma 2
                    (Integer)sIter.getValue(), // iterations
                    0.1, // dt
                    8e-3 // error
                    );
            /**/
            erodeIter = (Integer) sErode.getValue(); // erosions
            // all ok - store images to later use
            image = tmpImage;
            seedImage = tmpSeed;
            LOGGER.debug("Window params: " + params.toString());
            // assign verified data to variables and run
        } // end Apply
        if (b == bClone) {
            // clone seed image, convert it to RGB, add to list and select it on it
            ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem()); // tmp var
            ImagePlus duplicatedSeed = tmpSeed.duplicate();
            duplicatedSeed.setProcessor(duplicatedSeed.getProcessor().convertToRGB()); // convert
            duplicatedSeed.setTitle("SEED_" + tmpSeed.getTitle());
            duplicatedSeed.show();
            cSeed.addItem(new String(duplicatedSeed.getTitle()));
            cSeed.setSelectedItem(duplicatedSeed.getTitle());
        }

    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        Object b = arg0.getSource();

    }

}
