package uk.ac.warwick.wsbc.quimp;

import java.io.File;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class FormatConverterRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Test runner.
   * 
   * @param args args
   * @throws QuimpException QuimpException
   */
  public static void main(String[] args) throws QuimpException {
    ImageJ ij = new ImageJ();
    // FormatConverter fc = new FormatConverter(
    // new File("src/test/Resources-static/formatconv/currenttest/fluoreszenz-test.QCONF"));
    FormatConverter fc = new FormatConverter(new File("/home/baniuk/Desktop/Tests/146/"
            + "saveAsQCONF (copy)/July14ABD_GFP_actin_twoCells.QCONF"));
    fc.showConversionCapabilities(ij);
    fc.doConversion();
  }

}
