package uk.ac.warwick.wsbc.QuimP;

import java.io.File;

import ij.ImageJ;

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
        FormatConverter fC = new FormatConverter(new File(
                "/home/baniuk/Desktop/Tests/formatconv/currenttest/fluoreszenz-test.QCONF"));
        fC.doConversion();
    }

}
