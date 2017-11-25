package com.github.celldynamics.quimp.plugin.protanalysis;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisRun {
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
    // new Prot_Analysis(
    // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
    // Prot_Analysis pa =
    // new Prot_Analysis("src/test/Resources-static/280/July14ABD_GFP_actin_twoCells.QCONF");
    Prot_Analysis obj = new Prot_Analysis();
    obj.run("{noiseTolerance:1.5,dropValue:1.0,plotMotmap:false,plotMotmapmax:true,"
            + "plotConmap:false,plotOutline:false,plotStaticmax:false," + "plotDynamicmax:false,"
            + "outlinesToImage:{motColor:{value:-16776961,falpha:0.0},"
            + "convColor:{value:-65536,falpha:0.0}," + "defColor:{value:-1,falpha:0.0},"
            + "motThreshold:0.0,convThreshold:0.0,plotType:MOTILITY},"
            + "staticPlot:{plotmax:true,plottrack:true,averimage:false},"
            + "dynamicPlot:{plotmax:true,plottrack:true},"
            + "polarPlot:{useGradient:true,plotpolar:false,type:SCREENPOINT,"
            + "gradientPoint:{x:0.0,y:0.0},gradientOutline:0},"
            + "paramFile:[/home/baniuk/Desktop/Tests/formatconv/currenttest/"
            + "fluoreszenz-test.QCONF]}");
    // obj.run(""); // shows ui

  }

}
