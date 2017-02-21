package uk.ac.warwick.wsbc.quimp;

import java.io.File;

import ij.ImageJ;
import uk.ac.warwick.wsbc.quimp.FormatConverter;
import uk.ac.warwick.wsbc.quimp.QuimpException;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class FormatConverter_run {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * @param args
   * @throws QuimpException
   */
  public static void main(String[] args) throws QuimpException {
    ImageJ ij = new ImageJ();
    FormatConverter fC = new FormatConverter(
            new File("src/test/resources/formatconv/currenttest/fluoreszenz-test.QCONF"));
    fC.showConversionCapabilities(ij);
    fC.doConversion();
  }

}
