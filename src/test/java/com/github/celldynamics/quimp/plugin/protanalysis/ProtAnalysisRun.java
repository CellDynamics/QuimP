package com.github.celldynamics.quimp.plugin.protanalysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import ij.ImageJ;

/**
 * Runner.
 * 
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
   * @throws IOException on error
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws IOException {

    String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

    Path target = Paths.get(tmpdir, "fluoreszenz-test.QCONF");
    FileUtils.copyFile(
            new File("src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF"),
            target.toFile());

    ImageJ ij = new ImageJ();
    // new Prot_Analysis(
    // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
    // Prot_Analysis pa =
    // new Prot_Analysis("src/test/Resources-static/280/July14ABD_GFP_actin_twoCells.QCONF");
    Prot_Analysis obj = new Prot_Analysis();
    obj.run("opts={noiseTolerance:1.5,dropValue:1.0,plotMotmap:false,plotMotmapmax:true,"
            + "plotConmap:false,plotOutline:false,plotStaticmax:false," + "plotDynamicmax:false,"
            + "outlinesToImage:{motColor:{value:-16776961,falpha:0.0},"
            + "convColor:{value:-65536,falpha:0.0}," + "defColor:{value:-1,falpha:0.0},"
            + "motThreshold:0.0,convThreshold:0.0,plotType:MOTILITY},"
            + "staticPlot:{plotmax:true,plottrack:true,averimage:false},"
            + "dynamicPlot:{plotmax:true,plottrack:true},"
            + "polarPlot:{useGradient:true,plotpolar:false,type:SCREENPOINT,"
            + "gradientPoint:{x:0.0,y:0.0},gradientOutline:0}," + "paramFile:(" + target.toString()
            + ")}");
    // obj.run(""); // shows ui

  }

}
