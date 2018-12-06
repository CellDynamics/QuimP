package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ij.ImagePlus;

/**
 * @author p.baniukiewicz
 *
 */
@Deprecated
public class PluginGui extends JFrame {
  private Prot_Analysis model; // main model with method to run on ui action
  private ImagePlus imp;

  public PluginGui(Prot_Analysis model, final ImagePlus imp) {
    super("GUI");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.model = model;
    if (model.frameGui == null) {
      throw new RuntimeException("No child window");
    }
    this.imp = imp;
    addListeners();
    buildWindow();
  }

  private void addListeners() {
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        // close (hide) frameGui
        Prot_Analysis.LOGGER.trace("Closing Plugin Gui");
        model.frameGui.setVisible(false);
        model.frameGui.dispose();
        super.windowClosing(e);
        dispose();
      }

    });

  }

  private void buildWindow() {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    JLabel label = new JLabel("This is a label!");
    panel.add(label);
    add(panel);
    pack();
    setVisible(false);

  }

  @Override
  public void setVisible(boolean b) {
    // stick toolbox to right side of the window
    // frameGui must be displayed already
    if (b && model.frameGui.isVisible()) {
      int w = imp.getWidth();
      Point p = model.frameGui.getLocationOnScreen();
      if (p.x + w > 0.98 * Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
        setLocationRelativeTo(null); // if close to right edge center toolbox
      } else {
        setLocation(p.x + w, p.y);
      }
    }
    super.setVisible(b);
  }

}
