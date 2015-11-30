package uk.warwick.quimp;

import ij.*;
import ij.plugin.*;
import ij.macro.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;


public class QuimP_Bar implements PlugIn, ActionListener {
    String name, title;
    String path;
    String separator = System.getProperty("file.separator");
    JFrame frame = new JFrame();
    Frame frontframe;
    int xfw = 0;
    int yfw = 0;
    int wfw = 0;
    int hfw = 0;
    JToolBar toolBar = null;
    JButton button = null;

    public void run(String s) {
        title = "QuimP11 bar";
        name = "quimpBar";

        frame.setTitle(title);

        //if already open, bring to front
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

        frame.getContentPane().setLayout(new GridLayout(0, 1));
        buildPanel(); // build the QuimP bar

        // captures the ImageJ KeyListener
        frame.setFocusable(true);
        frame.addKeyListener(IJ.getInstance());


        frame.setResizable(false);
        //frame.setAlwaysOnTop(true);

        frame.setLocation((int) Prefs.get("actionbar" + title + ".xloc", 10), (int) Prefs.get(
                "actionbar" + title + ".yloc", 10));
        WindowManager.addWindow(frame);


        frame.pack();
        frame.setVisible(true);
        WindowManager.setWindow(frontframe);

    }

    private void buildPanel() {
        toolBar = new JToolBar();

        button = makeNavigationButton("x.jpg", "open()", "Open a file", "OPEN IMAGE");
        toolBar.add(button);

        button = makeNavigationButton("x.jpg", "run(\"ROI Manager...\");", "Open the ROI manager", "ROI");
        toolBar.add(button);

        toolBar.addSeparator();

        button = makeNavigationButton("boa.jpg", "run(\"BOA\")", "Cell segmentation", "BOA");
        toolBar.add(button);

        //button = makeNavigationButton("binary.jpg", "run(\"Binary Seg\")", "Binary Segmentation", "Binary Seg");
        //toolBar.add(button);

        button = makeNavigationButton("ecmm.jpg", "run(\"ECMM Mapping\")", "Cell membrane tracking", "ECMM");
        toolBar.add(button);
       
        button = makeNavigationButton("ana.jpg", "run(\"ANA\")", "Measure fluorescence", "ANA");
        toolBar.add(button);
       
        toolBar.addSeparator();

        button = makeNavigationButton("qanalysis.jpg", "run(\"QuimP Analysis\")", "Q Analysis of data", "Q Analysis");
        toolBar.add(button);
   
        toolBar.setFloatable(false);
        frame.getContentPane().add(toolBar);
    }

    protected JButton makeNavigationButton(String imageName,
            String actionCommand, String toolTipText, String altText) {

        String imgLocation = "icons/" + imageName;
        URL imageURL = QuimP_Bar.class.getResource(imgLocation);
        JButton newbutton = new JButton();
        newbutton.setActionCommand(actionCommand);
        newbutton.setMargin(new Insets(2, 2, 2, 2));
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
        try {
            new MacroRunner(e.getActionCommand() + "\n");
        } catch (Exception ex) {
            IJ.error("Error in QuimP plugin");
        }
        frame.repaint();
    }

    protected void storeLocation() {
        Prefs.set("actionbar" + title + ".xloc", frame.getLocation().x);
        Prefs.set("actionbar" + title + ".yloc", frame.getLocation().y);
    }

}
