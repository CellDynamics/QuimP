package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.EventQueue;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * SeedPickerRun.
 * 
 * @author p.baniukiewicz
 *
 */
public class SeedPickerRun {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Launch the application.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          @SuppressWarnings("unused")
          ImageJ ij = new ImageJ();
          IJ.run("ROI Manager...");
          ImagePlus ip = IJ.openImage("src/test/Resources-static/Stack_cut.tif");
          ip.show();
          new SeedPicker(ip, true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
