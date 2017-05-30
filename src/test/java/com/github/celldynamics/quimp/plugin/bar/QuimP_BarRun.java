package com.github.celldynamics.quimp.plugin.bar;

import com.github.celldynamics.quimp.plugin.bar.QuimP_Bar;

import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * Bar displayer
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimP_BarRun {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    QuimP_Bar bar = new QuimP_Bar();
    bar.run("");

  }

}