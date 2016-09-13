package uk.ac.warwick.wsbc.QuimP;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.Prefs;
import ij.WindowManager;
import ij.macro.MacroRunner;
import ij.plugin.PlugIn;

/**
 * Create QuimP bar with icons to QuimP plugins
 * 
 * @author r.tyson
 * @author p.baniukiewicz
 */
public class QuimP_Bar implements PlugIn, ActionListener, ItemListener {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    static final Logger LOGGER = LogManager.getLogger(QuimP_Bar.class.getName());
    /**
     * name used for storing bar location in IJ prefs
     */
    String prefsfName = "quimp_bar";
    String path;
    String separator = System.getProperty("file.separator");
    JFrame frame = new JFrame();
    Frame frontframe;
    int xfw = 0;
    int yfw = 0;
    int wfw = 0;
    int hfw = 0;
    JToolBar toolBarUpper = null;
    JToolBar toolBarBottom = null;
    JButton button = null;
    JCheckBox cFileFormat;
    JTextPane toolBarTitle1 = null;
    JTextPane toolBarTitle2 = null;
    private final Color barColor = new Color(0xFB, 0xFF, 0x94); // Color of title bar
    private MenuBar menuBar;
    private Menu menuHelp;
    private MenuItem menuVersion;
    private MenuItem menuOpenHelp;
    private MenuItem menuOpenSite;

    public void run(String s) {
        String title;
        String quimpInfo[] = new Tool().getQuimPBuildInfo(); // get jar title
        title = quimpInfo[2] + " " + quimpInfo[0];

        frame.setTitle(title); // and set to window title

        // if already open, bring to front
        if (WindowManager.getFrame(title) != null) {
            WindowManager.getFrame(title).toFront();
            return;
        }

        // this listener will save the bar's position and close it.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                storeLocation();
                e.getWindow().dispose();
                WindowManager.removeWindow((Frame) frame);
            }
        });

        frontframe = WindowManager.getFrontWindow();
        if (frontframe != null) {
            xfw = frontframe.getLocation().x;
            yfw = frontframe.getLocation().y;
            wfw = frontframe.getWidth();
            hfw = frontframe.getHeight();
        }

        frame.getContentPane().setLayout(new GridBagLayout());
        buildPanel(); // build the QuimP bar
        // add menu
        menuBar = new MenuBar();
        menuHelp = new Menu("Quimp-Help");
        menuBar.add(menuHelp);
        menuVersion = new MenuItem("About");
        menuOpenHelp = new MenuItem("Help Contents");
        menuOpenSite = new MenuItem("History of changes");
        menuHelp.add(menuOpenHelp);
        menuHelp.add(menuOpenSite);
        menuHelp.add(menuVersion);
        menuVersion.addActionListener(this);
        menuOpenHelp.addActionListener(this);
        menuOpenSite.addActionListener(this);
        frame.setMenuBar(menuBar);

        // captures the ImageJ KeyListener
        frame.setFocusable(true);
        frame.addKeyListener(IJ.getInstance());

        frame.setResizable(false);
        // frame.setAlwaysOnTop(true);

        frame.setLocation((int) Prefs.get("actionbar" + title + ".xloc", 10),
                (int) Prefs.get("actionbar" + title + ".yloc", 10));
        WindowManager.addWindow(frame);

        frame.pack();
        frame.setVisible(true);
        WindowManager.setWindow(frontframe);

    }

    /**
     * Build QuimP panel and run macros. Macros are defined in plugins.conf file, where
     * the name of the macro is related to class name to run.
     */
    private void buildPanel() {
        toolBarTitle1 = new JTextPane(); // first title bar
        toolBarUpper = new JToolBar(); // icons below it
        toolBarTitle2 = new JTextPane(); // second title bar
        toolBarBottom = new JToolBar(); // icons below it

        GridBagConstraints c = new GridBagConstraints();

        // define atributes for title bars
        SimpleAttributeSet titlebaratr = new SimpleAttributeSet();
        StyleConstants.setAlignment(titlebaratr, StyleConstants.ALIGN_CENTER);
        titlebaratr.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        // first row title - centered text
        StyledDocument doc = toolBarTitle1.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), titlebaratr, false);
        toolBarTitle1.setText("QuimP workflow");
        toolBarTitle1.setBackground(barColor);

        // second row - buttons and quimp icons
        JPanel panelButtons = new JPanel();
        GridBagConstraints cons = new GridBagConstraints();
        panelButtons.setLayout(new GridBagLayout());
        JPanel firstRow = new JPanel();
        firstRow.setLayout(new FlowLayout(FlowLayout.LEADING));

        button = makeNavigationButton("x.jpg", "open()", "Open a file", "OPEN IMAGE");
        firstRow.add(button, c);

        button = makeNavigationButton("x.jpg", "run(\"ROI Manager...\");", "Open the ROI manager",
                "ROI");
        firstRow.add(button);

        cons.gridx = 0;
        cons.gridy = 0;
        cons.fill = GridBagConstraints.HORIZONTAL;
        panelButtons.add(firstRow, cons);
        cFileFormat = new JCheckBox("Use new file format");
        cFileFormat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cFileFormat.setToolTipText("Unselect to use old paQP files");
        cFileFormat.setSelected(true); // default selection
        cFileFormat.addItemListener(this);

        cons.gridx = 0;
        cons.gridy = 1;
        panelButtons.add(cFileFormat, cons);

        toolBarUpper.add(panelButtons);

        toolBarUpper.addSeparator();

        button = makeNavigationButton("boa.jpg", "run(\"BOA\")", "Cell segmentation", "BOA");
        toolBarUpper.add(button);

        button = makeNavigationButton("ecmm.jpg", "run(\"ECMM Mapping\")", "Cell membrane tracking",
                "ECMM");
        toolBarUpper.add(button);

        button = makeNavigationButton("ana.jpg", "run(\"ANA\")", "Measure fluorescence", "ANA");
        toolBarUpper.add(button);

        toolBarUpper.addSeparator();

        button = makeNavigationButton("qanalysis.jpg", "run(\"QuimP Analysis\")",
                "Q Analysis of data", "Q Analysis");
        toolBarUpper.add(button);

        button = makeNavigationButton("prot.jpg", "run(\"Protrusion Analysis\")",
                "Run protrusion analysis", "Protrusion Analysis");
        toolBarUpper.add(button);

        // third row title
        StyledDocument doc1 = toolBarTitle2.getStyledDocument();
        doc1.setParagraphAttributes(0, doc1.getLength(), titlebaratr, false);
        toolBarTitle2.setText("Preprocessing methods");
        toolBarTitle2.setBackground(barColor);

        // fourth - preprocessing tools
        button = makeNavigationButton("diclid.jpg", "run(\"DIC\")",
                "Reconstruction of DIC images by Line Integral Method", "DIC LID");
        toolBarBottom.add(button);
        button = makeNavigationButton("rw.jpg", "run(\"RandomWalk\")",
                "Run random walk segmentation", "Random Walk");
        toolBarBottom.add(button);

        toolBarUpper.setFloatable(false);
        toolBarBottom.setFloatable(false);
        // build window
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.getContentPane().add(toolBarTitle1, c);
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(toolBarUpper, c);
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.getContentPane().add(toolBarTitle2, c);
        c.gridx = 0;
        c.gridy = 3;
        frame.getContentPane().add(toolBarBottom, c);
    }

    protected JButton makeNavigationButton(String imageName, String actionCommand,
            String toolTipText, String altText) {

        String imgLocation = "icons/" + imageName;
        URL imageURL = QuimP_Bar.class.getResource(imgLocation);
        JButton newbutton = new JButton();
        newbutton.setActionCommand(actionCommand);
        // newbutton.setMargin(new Insets(2, 2, 2, 2));
        newbutton.setBorderPainted(true);
        newbutton.addActionListener(this);
        newbutton.setFocusable(true);
        newbutton.addKeyListener(IJ.getInstance());
        if (imageURL != null) {
            newbutton.setIcon(new ImageIcon(imageURL, altText));
            newbutton.setToolTipText(toolTipText);
        } else {
            newbutton.setText(altText);
            newbutton.setToolTipText(toolTipText);
        }
        return newbutton;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuVersion) { // menu version
            String quimpInfo = new Tool().getQuimPversion(); // prepare info plate
            AboutDialog ad = new AboutDialog(frame); // create about dialog with parent 'window'
            ad.appendLine(quimpInfo); // display template filled by quimpInfo
            ad.appendDistance();
            ad.appendLine("All plugins for QuimP are reported in modules that use them natively.");
            ad.appendLine(
                    "Web page:\nhttp://www2.warwick.ac.uk/fac/sci/systemsbiology/staff/baniukiewicz/quimp");
            ad.setVisible(true);
            return;
        }
        if (e.getSource() == menuOpenHelp) { // open help
            String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
            try {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e1) {
                LOGGER.error("Could not open help: " + e1.getMessage(), e1);
            }
            return;
        }
        if (e.getSource() == menuOpenSite) { // open help
            String url = new PropertyReader().readProperty("quimpconfig.properties", "siteURL");
            try {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e1) {
                LOGGER.error("Could not open help: " + e1.getMessage());
            }
            return;
        }
        try {
            new MacroRunner(e.getActionCommand() + "\n");
        } catch (Exception ex) {
            IJ.error("Error in QuimP plugin");
        }
        frame.repaint();
    }

    protected void storeLocation() {
        Prefs.set("actionbar" + prefsfName + ".xloc", frame.getLocation().x);
        Prefs.set("actionbar" + prefsfName + ".yloc", frame.getLocation().y);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == cFileFormat) {
            LOGGER.debug("Clicked file format");
        }

    }

}
