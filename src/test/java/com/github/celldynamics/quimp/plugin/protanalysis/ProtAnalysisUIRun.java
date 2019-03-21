package com.github.celldynamics.quimp.plugin.protanalysis;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ij.IJ;
import ij.ImageJ;

/**
 * The Class ProtAnalysisUIRun.
 *
 * @author p.baniukiewicz
 */
public class ProtAnalysisUIRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  Prot_Analysis mockProtAnalysis() {
    Prot_Analysis mockPa = Mockito.mock(Prot_Analysis.class);
    Mockito.when(mockPa.getImage()).thenReturn(IJ.createImage("Test", 512, 512, 5, 8));
    return mockPa;
  }

  /**
   * Runner.
   * 
   * @param args args
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {
    ProtAnalysisUIRun obj = new ProtAnalysisUIRun();
    MockitoAnnotations.initMocks(obj);
    ImageJ ij = new ImageJ();
    // ProtAnalysisUI ui = new ProtAnalysisUI(obj.mockProtAnalysis());

    // run like API
    // Prot_Analysis pa = new Prot_Analysis(
    // "{paramFile:src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF}");
    // pa.showUi(true);

    // run like IJ macro
    // Prot_Analysis pa = new Prot_Analysis();
    // pa.run("");

    // IJ from script
    Prot_Analysis pa = new Prot_Analysis();
    pa.run("{paramFile:src/test/Resources-static/FullAnalysis/Stack.QCONF}");
  }

}