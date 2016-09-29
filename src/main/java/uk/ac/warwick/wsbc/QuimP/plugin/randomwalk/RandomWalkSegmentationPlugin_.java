/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URI;
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
import ij.plugin.Converter;
import ij.plugin.PlugIn;
import ij.plugin.tool.BrushTool;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.PropertyReader;

/**
 * Run RandomWalkSegmentation in IJ environment.
 * 
 * Implements common PlugIn interface as both images are provided after run.
 * The seed can be one image - in this case seed propagation is used to generate seed for
 * subsequent frames, or it can be stack of the same size as image. In latter case every slice
 * from seed is used for seeding related slice from image.
 *  
 * @author p.baniukiewicz
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
    private BrushTool br = new BrushTool();
    private String lastTool; // tool selected in IJ
    
    public JFrame wnd;
    
    /**
     * Default constructor
     */
    public RandomWalkSegmentationPlugin_() {
        lastTool = IJ.getToolName(); // remember selected tool
    }

    /**
     * Build main dialog
     * 
     * @startuml
     * salt
     *   {+
     *   Random Walk segmentation
     *   ~~
     *   {+
     *   Define images
     *   Open image:  |  ^Original image ^
     *   Open seed:   |  ^Seed image     ^
     *   }
     *   {+
     *   or create it:
     *   [  Clone  ] | [   FG   ] | [   BG   ]
     *   }
     *   ==
     *   {+
     *   Segmentation parameters
     *   alpha: | "400.0  "
     *   beta: | "50.0   "
     *   gamma: | "100.0  "
     *   iterations: | "80     "
     *   erode power: | "5      "
     *   }
     *   ~~
     *   {
     *   [     OK     ] | [   Cancel   ]
     *   }
     *   }
     * @enduml
     *   
     * State diagram
     *
     * @startuml
     *   [*] --> Default
     *   Default : selectors empty
     *   Default : **Clone**, **BG** and **FG** //inactive//
     *   Default --> ImageSelected : when **Image** selector not empty
     *   ImageSelected : **Clone** //active//
     *   Default --> SeedSelected : when **Seed** selector not empty and image tthere is valid
     *   SeedSelected : **Clone**, **BG** and **FG** //active//
     *   SeedSelected --> SeedCreation
     *   ImageSelected --> SeedCreation : Clicked **Clone**
     *   SeedCreation : Original image cloned and converted to RGB
     *   SeedCreation : **BG** and **FG** //active//
     *   SeedCreation : **SeedImage** selector filled with name of cloned image
     *   SeedCreation --> Sketch : **BG** or **FG** clicked
     *   Sketch : Draw tool selected in IJ
     *   Sketch : **BG** or **FG** changed to notify
     *   Default -> Run
     *   Run : Verify all fields
     *   Run : Run algorithm
     *   Sketch --> Run
     *   Sketch --> [*]
     *   SeedCreation --> Run
     *   SeedCreation --> [*]
     *   ImageSelected --> Run
     *   ImageSelected --> [*]
     *   Run --> [*]
     *   Default --> [*]
     * @enduml
     * 
     */
    public void showDialog() {
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

        // reaction on focus = all choices are rebuilt
        wnd.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowLostFocus(WindowEvent e) {
            }

            /**
             * Updates selector if user deleted the window
             */
            @Override
            public void windowGainedFocus(WindowEvent e) {
                Object sel = cSeed.getSelectedItem();
                cSeed.removeAllItems();
                for (String s : WindowManager.getImageTitles())
                    cSeed.addItem(s);
                cSeed.setSelectedItem(sel);
                sel = cImage.getSelectedItem();
                cImage.removeAllItems();
                for (String s : WindowManager.getImageTitles())
                    cImage.addItem(s);
                cImage.setSelectedItem(sel);
                selectorLogic();
            }
        });
        wnd.pack();
        wnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        wnd.setVisible(true);
    }

    /**
     * Control status of FG, BG and Clone buttons
     */
    private void selectorLogic() {
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
    }

    /**
     * Plugin runner. 
     * 
     * Shows UI and perform segmentation after validating UI
     */
    @Override
    public void run(String arg) {
        showDialog();
    }

    /**
     * Run segmentation - fired from UI
     */
    private void runPlugin() {
        ImageStack ret; // all images treated as stacks
        Map<Integer, List<Point>> seeds;
        try {
            ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
            ImageStack is = image.getStack(); // get current stack (size 1 for one image)
            // segment first slice (or image if it is not stack)
            RandomWalkSegmentation obj = new RandomWalkSegmentation(is.getProcessor(1), params);
            seeds = obj.decodeSeeds(seedImage.getStack().getProcessor(1), Color.RED, Color.GREEN); // generate
                                                                                                   // seeds
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

    /**
     * Verify all entries in window and set them as final assigning to object variables.
     * All operations are here. Performs also some window logic
     * 
     * @param e Component
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        // enable disable controls depending on selectors (see diagram)
        if (b == cImage || b == cSeed) {
            selectorLogic();
        }
        // Start data verification, show message on problem and exit method setting FGBG unselected
        // 0. check if we can paint on selected image if user try
        if (b == bFore || b == bBack) {
            ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem());
            if (tmpSeed == null)
                return;
            if (tmpSeed.getBitDepth() != 24) {
                JOptionPane.showMessageDialog(wnd, "Seed image must be 24 bit RGB type", "Error",
                        JOptionPane.ERROR_MESSAGE);
                bFore.setSelected(false);
                bBack.setSelected(false);
                return; // we cant - return
            }
        }
        if (b == bFore) { // foreground pressed
            if (((JToggleButton) b).isSelected()) { // if selected
                bBack.setSelected(false); // unselect background
                IJ.setForegroundColor(Color.RED.getRed(), Color.RED.getGreen(),
                        Color.RED.getBlue()); // set pen color
                BrushTool.setBrushWidth(10); // set brush width
                br.run(""); // tun macro
            } else {
                IJ.setTool(lastTool); // if unselected just switch off BrushTool selecting other
                                      // tool
            }
        }
        if (b == bBack) { // see bFore comments
            if (((JToggleButton) b).isSelected()) {
                bFore.setSelected(false);
                IJ.setForegroundColor(Color.GREEN.getRed(), Color.GREEN.getGreen(),
                        Color.GREEN.getBlue());
                BrushTool.setBrushWidth(10);
                br.run("");
            } else {
                IJ.setTool(lastTool);
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
            runPlugin(); // run process - it gives new image
            // decelect seeds buttons
            bBack.setSelected(false);
            bFore.setSelected(false);
        } // end Apply
        if (b == bClone) {
            // clone seed image, convert it to RGB, add to list and select it on it
            ImagePlus tmpImage = WindowManager.getImage((String) cImage.getSelectedItem());
            ImagePlus duplicatedImage = tmpImage.duplicate();
            duplicatedImage.show();
            new Converter().run("RGB Color");
            duplicatedImage.setTitle("SEED_" + tmpImage.getTitle());

            cSeed.addItem(new String(duplicatedImage.getTitle()));
            cSeed.setSelectedItem(duplicatedImage.getTitle());
        }
        if (b == bHelp) {
            String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
            try {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e1) {
                LOGGER.error("Could not open help: " + e1.getMessage(), e1);
            }
        }
        if (b == bCancel) {
            wnd.dispose();
        }

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        LOGGER.debug("State changed");
    }

}