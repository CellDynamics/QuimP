package com.github.celldynamics.quimp.filesystem.converter;

import java.io.File;

import com.github.celldynamics.quimp.QuimpException;

import ij.ImageJ;

/**
 * FormatConverterRun.
 * 
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
    @SuppressWarnings("unused")
    ImageJ ij = new ImageJ();
    // FormatConverter fc = new FormatConverter(
    // new File("src/test/Resources-static/formatconv/currenttest/fluoreszenz-test.QCONF"));
    FormatConverter fc =
            new FormatConverter(new File("/home/baniuk/Desktop/Tests/17.10/test.QCONF"));
    // fc.showConversionCapabilities(ij);
    // System.out.print(fc.toString());
    fc.doConversion();
    // {status:[stats:fluores,map:ycoords,stats:q11files],paramFile:
    // (/home/baniuk/Desktop/Tests/formatconv/currenttest/fluoreszenz-test.QCONF)}
  }

}
