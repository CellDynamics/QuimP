package uk.ac.warwick.wsbc.quimp.plugin.bar;

import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.Prefs;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.macro.MacroRunner;
import ij.plugin.PlugIn;
import uk.ac.warwick.wsbc.quimp.AboutDialog;
import uk.ac.warwick.wsbc.quimp.FormatConverter;
import uk.ac.warwick.wsbc.quimp.PropertyReader;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.QuimpException.MessageSinkTypes;
import uk.ac.warwick.wsbc.quimp.QuimpVersion;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.filesystem.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.quimp.registration.Registration;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * Create QuimP bar with icons to QuimP plugins.
 * 
 * @author r.tyson
 * @author p.baniukiewicz
 */
public class QuimP_Bar implements PlugIn, ActionListener {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimP_Bar.class.getName());
  /**
   * This field is used for sharing information between bar and other plugins.
   * 
   * <<<<<<< HEAD
   * <p>It is read by {@link uk.ac.warwick.wsbc.quimp.filesystem.QuimpConfigFilefilter} which is
   * used
   * =======
   * It is read by {@link uk.ac.warwick.wsbc.quimp.filesystem.QuimpConfigFilefilter} which is used
   * >>>>>>> feature/task_220-GSon-versioning
   * by {@link uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader} for serving
   * {@link uk.ac.warwick.wsbc.quimp.QParams} object for client.
   */
  public static boolean newFileFormat = true;

  /**
   * The path.
   */
  String path;

  /**
   * The separator.
   */
  String separator = System.getProperty("file.separator");

  /**
   * The frame.
   */
  JFrame frame = new JFrame();

  /**
   * The frontframe.
   */
  Window frontframe;

  /**
   * The xfw.
   */
  int xfw = 0;

  /**
   * The yfw.
   */
  int yfw = 0;

  /**
   * The wfw.
   */
  int wfw = 0;

  /**
   * The hfw.
   */
  int hfw = 0;

  /**
   * The tool bar upper.
   */
  JToolBar toolBarUpper = null;

  /**
   * The tool bar bottom.
   */
  JToolBar toolBarBottom = null;

  /**
   * The button.
   */
  JButton button = null;

  /**
   * The c file format.
   */
  JCheckBox cbFileformat;

  /**
   * The tool bar title 1.
   */
  JTextPane toolBarTitle1 = null;

  /**
   * The tool bar title 2.
   */
  JTextPane toolBarTitle2 = null;
  private final Color barColor = new Color(0xFB, 0xFF, 0x94); // Color of title bar
  private MenuBar menuBar;
  private Menu menuTools;
  private Menu menuMisc;
  private MenuItem menuShowreg;
  private MenuItem menuFormatConverter;
  private Menu menuHelp;
  private MenuItem menuVersion;
  private MenuItem menuOpenHelp;
  private MenuItem menuOpenSite;
  private MenuItem menuLicense;

  /*
   * (non-Javadoc)
   * 
   * @see ij.plugin.PlugIn#run(java.lang.String)
   */
  public void run(String s) {
    String title;
    QuimpVersion quimpInfo = QuimP.TOOL_VERSION; // get jar title
    title = quimpInfo.getName() + " " + quimpInfo.getVersion();

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

    frontframe = WindowManager.getActiveWindow();
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
    menuTools = new Menu("Tools");
    menuMisc = new Menu("Misc");
    menuBar.add(menuHelp);
    menuBar.add(menuTools);
    menuBar.add(menuMisc);
    menuVersion = new MenuItem("About");
    menuOpenHelp = new MenuItem("Help Contents");
    menuOpenSite = new MenuItem("History of changes");
    menuLicense = new MenuItem("Show licence");
    menuFormatConverter = new MenuItem("Format converter");
    menuShowreg = new MenuItem("Show registration");
    menuHelp.add(menuOpenHelp);
    menuHelp.add(menuOpenSite);
    menuHelp.add(menuVersion);
    menuTools.add(menuFormatConverter);
    menuMisc.add(menuShowreg);
    menuMisc.add(menuLicense);
    menuVersion.addActionListener(this);
    menuOpenHelp.addActionListener(this);
    menuOpenSite.addActionListener(this);
    menuLicense.addActionListener(this);
    menuFormatConverter.addActionListener(this);
    menuShowreg.addActionListener(this);
    frame.setMenuBar(menuBar);

    // captures the ImageJ KeyListener
    frame.setFocusable(true);
    frame.addKeyListener(IJ.getInstance());

    frame.setResizable(false);
    // frame.setAlwaysOnTop(true);

    frame.setLocation((int) Prefs.get("actionbar" + QuimP.QUIMP_PREFS_SUFFIX + ".xloc", 10),
            (int) Prefs.get("actionbar" + QuimP.QUIMP_PREFS_SUFFIX + ".yloc", 10));
    WindowManager.addWindow(frame);

    frame.pack();

    frame.setVisible(true);

    // validate registered user
    new Registration(frame, "QuimP Registration");

    WindowManager.setWindow(frontframe);

  }

  /**
   * Build QuimP panel and run macros.
   * 
   * Macros are defined in plugins.conf file, where the name of the macro is related to class name
   * to run.
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

    button = makeNavigationButton("x.jpg", "open(\"\")", "Open a file", "OPEN IMAGE");
    firstRow.add(button, c);

    button = makeNavigationButton("x.jpg", "run(\"ROI Manager...\");", "Open the ROI manager",
            "ROI");
    firstRow.add(button);

    cons.gridx = 0;
    cons.gridy = 0;
    cons.fill = GridBagConstraints.HORIZONTAL;
    panelButtons.add(firstRow, cons);
    cbFileformat = new JCheckBox("Use new file format");
    cbFileformat.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbFileformat
            .setToolTipText("Unselect to use old " + FileExtensions.configFileExt + "paQP files");
    cbFileformat.setSelected(QuimP_Bar.newFileFormat); // default selection
    cbFileformat.addItemListener(new ItemListener() { // set static field

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED)
          QuimP_Bar.newFileFormat = true;
        else
          QuimP_Bar.newFileFormat = false;
      }
    });

    cons.gridx = 0;
    cons.gridy = 1;
    panelButtons.add(cbFileformat, cons);

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

    button = makeNavigationButton("qanalysis.jpg", "run(\"QuimP Analysis\")", "Q Analysis of data",
            "Q Analysis");
    toolBarUpper.add(button);

    button = makeNavigationButton("prot.jpg", "run(\"Protrusion Analysis\")",
            "Run protrusion analysis", "Protrusion Analysis");
    toolBarUpper.add(button);

    // third row title
    StyledDocument doc1 = toolBarTitle2.getStyledDocument();
    doc1.setParagraphAttributes(0, doc1.getLength(), titlebaratr, false);
    toolBarTitle2.setText("Pre- and post-processing methods");
    toolBarTitle2.setBackground(barColor);

    // fourth - preprocessing tools
    button = makeNavigationButton("diclid.jpg", "run(\"DIC\")",
            "Reconstruction of DIC images by Line Integral Method", "DIC LID");
    toolBarBottom.add(button);
    button = makeNavigationButton("rw.jpg", "run(\"RandomWalk\")", "Run random walk segmentation",
            "Random Walk");
    toolBarBottom.add(button);
    button = makeNavigationButton("generatemask.jpg", "run(\"Generate mask\")",
            "Convert Snakes to Masks", "Generate mask");
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

  /**
   * Make navigation button.
   *
   * @param imageName the image name
   * @param actionCommand the action command
   * @param toolTipText the tool tip text
   * @param altText the alt text
   * @return the j button
   */
  protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText,
          String altText) {

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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == menuVersion) { // menu version
      String quimpInfo = new QuimpToolsCollection().getQuimPversion(); // prepare info plate
      AboutDialog ad = new AboutDialog(frame); // create about dialog with parent 'window'
      ad.appendLine(quimpInfo); // display template filled by quimpInfo
      ad.appendDistance();
      ad.appendLine("All plugins for QuimP are reported in modules that use them natively.");
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
    if (e.getSource() == menuLicense) {
      AboutDialog ad = new AboutDialog(frame, 50, 130);
      BufferedReader in = null;
      try {
        // get internal name - jar name
        String iname = new PropertyReader().readProperty("quimpconfig.properties", "internalName");
        // read file from resources
        Enumeration<URL> resources = getClass().getClassLoader().getResources("LICENSE.txt");
        while (resources.hasMoreElements()) {
          URL reselement = resources.nextElement();
          if (reselement.toString().contains("/" + iname)) {
            in = new BufferedReader(new InputStreamReader(reselement.openStream()));
            String line = in.readLine();
            while (line != null) {
              ad.appendLine(line);
              line = in.readLine();
            }
          }
        }
        ad.setVisible(true);
      } catch (IOException e1) {
        LOGGER.debug(e1.getMessage(), e1);
        LOGGER.error("Can not find license file in jar: " + e1.getMessage());
      } finally {
        try {
          if (in != null)
            in.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
      return;
    }
    if (e.getSource() == menuShowreg) {
      // show window filled with reg data
      Registration regwindow = new Registration(frame); // use manual constructor
      regwindow.waited = true; // user waited already
      regwindow.build("QuimP Registration", false); // build UI
      regwindow.fillRegForm(); // fill reg form from IJ_Prefs data
      regwindow.setVisible(true); // show and wait for user action
      return;
    }
    if (e.getSource() == menuFormatConverter) { // convert between file formats
      QuimpConfigFilefilter fileFilter = new QuimpConfigFilefilter(FileExtensions.newConfigFileExt,
              FileExtensions.configFileExt);
      FileDialog od =
              new FileDialog(IJ.getInstance(), "Open paramater file " + fileFilter.toString());
      od.setFilenameFilter(fileFilter);
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setMultipleMode(false);
      od.setMode(FileDialog.LOAD);
      od.setVisible(true);
      if (od.getFile() == null) {
        IJ.log("Cancelled - exiting...");
        return;
      }
      try {
        // load config file but check if it is new format or old
        FormatConverter fC = new FormatConverter(new File(od.getDirectory(), od.getFile()));
        fC.showConversionCapabilities(frame);
        fC.doConversion();
      } catch (QuimpException qe) {
        qe.setMessageSinkType(MessageSinkTypes.GUI);
        qe.handleException(frame, "Error during conversion:");
      } catch (Exception e1) {
        LOGGER.debug(e1.getMessage(), e1);
        LOGGER.error("Problem with running FormatConverter: " + e1.getMessage());
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

  /**
   * Store location.
   */
  protected void storeLocation() {
    Prefs.set("actionbar" + QuimP.QUIMP_PREFS_SUFFIX + ".xloc", frame.getLocation().x);
    Prefs.set("actionbar" + QuimP.QUIMP_PREFS_SUFFIX + ".yloc", frame.getLocation().y);
  }

}