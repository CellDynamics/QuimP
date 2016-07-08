package uk.ac.warwick.wsbc.QuimP;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of tools used across QuimP
 * 
 * @author Richard
 * @author p.baniukiewicz
 */
public class Tool {

    private static final Logger LOGGER = LogManager.getLogger(Tool.class.getName());
    public static final String defNote = "Not found"; //!< Default message if content not found in jar

    /**
     * Prepare info plate for QuimP. It contains version, names, etc
     * 
     * @return Formatted string with QuimP version and authors
     * @remarks By general Tool() class is static. These methods can not be so they must be called:
     * @code{java}
     *  LOGGER.debug(new Tool().getQuimPversion());
     * @endcode
     */
    public String getQuimPversion() {
        String[] quimpBuildInfo = getQuimPBuildInfo();
        return getQuimPversion(quimpBuildInfo);
    }

    /**
     * Prepare info plate for QuimP. It contains version, names, etc
     * 
     * @param quimpBuildInfo info read from jar
     * @return Formatted string with QuimP version and authors
     * @remarks By general Tool() class is static. These methods can not be so they must be called:
     * @code{java}
     *  LOGGER.debug(new Tool().getQuimPversion());
     * @endcode
     * @see getQuimPBuildInfo()
     */
    public static String getQuimPversion(String[] quimpBuildInfo) {
        //!<
        String infoPlate = 
                  "---------------------------------------------------------\n" 
                + "| QuimP, by                                             |\n"
                + "| Richard Tyson (richard.tyson@warwick.ac.uk)           |\n"
                + "| Till Bretschneider (Till.Bretschneider@warwick.ac.uk) |\n"
                + "| Piotr Baniukiewicz (P.Baniukiewicz@warwick.ac.uk)     |\n"
                + "---------------------------------------------------------\n";
        /**/
        infoPlate = infoPlate.concat("\n");
        infoPlate = infoPlate.concat("QuimP version: " + quimpBuildInfo[0]);
        infoPlate = infoPlate.concat("\n");
        infoPlate = infoPlate.concat("Build by: " + quimpBuildInfo[1]);
        infoPlate = infoPlate.concat("\n");
        infoPlate = infoPlate.concat("Internal name: " + quimpBuildInfo[2]);
        infoPlate = infoPlate.concat("\n");
        return infoPlate;
    }

    /**
     * Get build info read from jar file
     * 
     * @return Formatted strings with build info and version:
     * -# [0] - contains only version string read from \a MANIFEST.MF
     * -# [1] - contains formatted string with build time and name of builder read from \a MANIFEST.MF
     * -# [2] - contains software name read from \a MANIFEST.MF
     * If those information are not available in jar, the \a defNote string is returned
     * @warning This method is jar-name dependent - looks for manifest with \a Implementation-Title
     * that contains \c QuimP string.
     */
    public String[] getQuimPBuildInfo() {
        String[] ret = new String[3];
        try {
            Enumeration<URL> resources =
                    getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                Attributes attributes = manifest.getMainAttributes();
                try {
                    String val = attributes.getValue("Implementation-Title");
                    if (val == null)
                        continue;
                    // name dependent part
                    if (attributes.getValue("Implementation-Title").contains("QuimP")) {
                        ret[1] = attributes.getValue("Built-By") + " on: "
                                + attributes.getValue("Implementation-Build");
                        ret[0] = attributes.getValue("Implementation-Version");
                        ret[2] = attributes.getValue("Implementation-Title");
                        LOGGER.debug(ret);
                    }
                } catch (Exception e) {
                    ; // do not care about problems - just use defaults defined on beginning
                }
            }
        } catch (IOException e) {
            ; // do not care about problems - just use defaults defined on beginning
        }
        // replace possible nulls with default text
        ret[0] = ret[0] == null ? defNote : ret[0];
        ret[1] = ret[1] == null ? defNote : ret[1];
        ret[2] = ret[2] == null ? defNote : ret[2];
        return ret;
    }

    public static double s2d(String s) {
        Double d;
        try {
            d = new Double(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        if (d != null) {
            return (d.doubleValue());
        } else {
            return (0.0);
        }
    }

    public static String removeExtension(String filename) {
        // extract fileName without extension

        int dotI = filename.lastIndexOf(".");
        if (dotI > 0) {
            filename = filename.substring(0, dotI);
        }
        return filename;
    }

    public static String getFileExtension(String filename) {
        // extract fileName without extension

        int dotI = filename.lastIndexOf(".");
        if (dotI > 0) {
            filename = filename.substring(dotI + 1, filename.length());
        }
        return filename;
    }

    public static Vert closestFloor(Outline o, double target) {
        // find the vert with coor closest (floored) to target coordinate

        Vert v = o.getHead();
        double coordA, coordB;

        do {
            // coordA = v.fCoord;
            // coordB = v.getNext().fCoord;
            coordA = v.coord;
            coordB = v.getNext().coord;
            // System.out.println("A: " + coordA + ", B: "+ coordB);

            if ((coordA > coordB)) {
                if (coordA <= target && coordB + 1 > target) {
                    break;
                }

                if (coordA - 1 <= target && coordB > target) {
                    break;
                }

            } else {
                if (coordA <= target && coordB > target) {
                    break;
                }
            }
            v = v.getNext();
        } while (!v.isHead());

        return v;
    }

    public static double distanceToScale(double value, double scale) {
        // assums pixelwidth is in micro meters
        return value * scale;
    }

    public static double areaToScale(double value, double scale) {
        // assums pixelwidth is in micro meters
        return value * (scale * scale);
    }

    public static double speedToScale(double value, double scale, double frameInterval) {
        return (value * scale) / frameInterval;
    }

    public static double distanceToScale(int value, double scale) {
        return value * scale;
    }

    public static double distanceFromScale(double value, double scale) {
        return value / scale;
    }

    public static double speedToScale(int value, double scale, double frameInterval) {
        return (value * scale) / frameInterval;
    }

    public static String dateAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public static double[] setLimitsEqual(double[] migLimits) { // min and max
        if (migLimits.length < 2) {
            System.out.println("Tool.237-Array to short. Needs a min and max");
            return migLimits;
        }
        // Set limits to equal positive and negative
        if (migLimits[1] < 0)
            migLimits[1] = -migLimits[0];
        if (migLimits[0] > 0)
            migLimits[0] = -migLimits[1];

        // Make min and max equal for mig and conv
        if (migLimits[0] < -migLimits[1]) {
            migLimits[1] = -migLimits[0];
        } else {
            migLimits[0] = -migLimits[1];
        }

        return migLimits;
    }

}
