package com.github.celldynamics.quimp.utils;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Vert;

/**
 * Collection of tools used across QuimP.
 * 
 * @author Richard
 * @author p.baniukiewicz
 */
public class QuimpToolsCollection {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpToolsCollection.class.getName());
  /**
   * Message returned by {@link #getQuimPBuildInfo()} if info is not found in jar.
   */
  public static final String defNote = "0.0.0";

  /**
   * Prepare info plate for QuimP.
   * 
   * @return QuimP version
   * 
   * @see #getFormattedQuimPversion(QuimpVersion)
   */
  public String getQuimPversion() {
    QuimpVersion quimpBuildInfo = getQuimPBuildInfo();
    return getFormattedQuimPversion(quimpBuildInfo);
  }

  /**
   * Prepare info plate for QuimP.
   * 
   * <p>It contains version, names, etc. By general QuimpToolsCollection class is static. These
   * methods can not be so they must be called:
   * 
   * <pre>
   * <code>LOGGER.debug(new Tool().getQuimPversion());</code>
   * </pre>
   * 
   * @param quimpBuildInfo info read from jar
   * @return Formatted string with QuimP version and authors
   * @see #getQuimPBuildInfo()
   */
  public static String getFormattedQuimPversion(QuimpVersion quimpBuildInfo) {
    //!>
    String infoPlate = "---------------------------------------------------------\n"
            + "| QuimP, by                                             |\n"
            + "| Richard Tyson (richard.tyson@warwick.ac.uk)           |\n"
            + "| Till Bretschneider (Till.Bretschneider@warwick.ac.uk) |\n"
            + "| Piotr Baniukiewicz (P.Baniukiewicz@warwick.ac.uk)     |\n"
            + "| Web page: "
            + new PropertyReader().readProperty("quimpconfig.properties", "webPage")
            + "                     |\n"
            + "---------------------------------------------------------\n";
    //!<
    infoPlate = infoPlate.concat("\n");
    infoPlate = infoPlate.concat("QuimP version: " + quimpBuildInfo.getVersion());
    infoPlate = infoPlate.concat("\n");
    infoPlate = infoPlate.concat("Build by: " + quimpBuildInfo.getBuildstamp());
    infoPlate = infoPlate.concat("\n");
    infoPlate = infoPlate.concat("Internal name: " + quimpBuildInfo.getName());
    infoPlate = infoPlate.concat("\n");
    return infoPlate;
  }

  /**
   * Get build info read from jar file.
   * 
   * @return Formatted strings with build info and version.
   */
  public QuimpVersion getQuimPBuildInfo() {
    String[] ret = new String[3];
    try {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
      // get internal name - jar name
      String iname = new PropertyReader().readProperty("quimpconfig.properties", "internalName");
      while (resources.hasMoreElements()) {
        URL reselement = resources.nextElement();
        if (!reselement.toString().contains("/" + iname)) {
          continue;
        }
        Manifest manifest = new Manifest(reselement.openStream());
        Attributes attributes = manifest.getMainAttributes();
        try {
          String date = attributes.getValue("Implementation-Date");

          ret[1] = attributes.getValue("Built-By") + " on: " + implementationDateConverter(date);
          ret[0] = attributes.getValue("Implementation-Version");
          ret[2] = attributes.getValue("Implementation-Title");
          LOGGER.trace(Arrays.toString(ret));
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
    // prepare output
    QuimpVersion retmap = new QuimpVersion(ret[0], ret[1], ret[2]);
    return retmap;
  }

  /**
   * Reformat date from jar (put there by Maven).
   * 
   * @param dateString string in format "2017-02-24T08:55:44+0000"
   * @return String in format "yyyy-MM-dd hh:mm:ss"
   */
  public static String implementationDateConverter(String dateString) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    TemporalAccessor accessor = timeFormatter.parse(dateString);
    Date date = Date.from(Instant.from(accessor));
    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dt.format(date);
  }

  /**
   * S 2 d.
   *
   * @param s the s
   * @return the double
   */
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

  /**
   * Get file name without extension.
   * 
   * @param filename name of the file
   * @return file (with path) without extension
   */
  public static String removeExtension(String filename) {
    // extract fileName without extension

    int dotI = filename.lastIndexOf(".");
    if (dotI > 0 && filename.length() - dotI <= 6) { // assume extension of length max 5+dot
      filename = filename.substring(0, dotI);
    }
    return filename;
  }

  /**
   * Get file extension.
   * 
   * @param filename Name of file
   * @return extension without dot
   */
  public static String getFileExtension(String filename) {
    // extract fileName without extension

    int dotI = filename.lastIndexOf(".");
    if (dotI > 0) {
      filename = filename.substring(dotI + 1, filename.length());
    }
    return filename;
  }

  /**
   * Closest floor.
   *
   * @param o the o
   * @param target the target
   * @return the vert
   */
  public static Vert closestFloor(Outline o, double target) {
    // find the vert with coor closest (floored) to target coordinate

    Vert v = o.getHead();
    double coordA;
    double coordB;

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

  /**
   * Distance to scale.
   *
   * @param value the value
   * @param scale the scale
   * @return the double
   */
  public static double distanceToScale(double value, double scale) {
    // assums pixelwidth is in micro meters
    return value * scale;
  }

  /**
   * Distance to scale.
   *
   * @param value the value
   * @param scale the scale
   * @return the double
   */
  public static double distanceToScale(int value, double scale) {
    return value * scale;
  }

  /**
   * Area to scale.
   *
   * @param value the value
   * @param scale the scale
   * @return the double
   */
  public static double areaToScale(double value, double scale) {
    // assums pixelwidth is in micro meters
    return value * (scale * scale);
  }

  /**
   * Speed to scale.
   *
   * @param value the value
   * @param scale the scale
   * @param frameInterval the frame interval
   * @return the double
   */
  public static double speedToScale(double value, double scale, double frameInterval) {
    return (value * scale) / frameInterval;
  }

  /**
   * Speed to scale.
   *
   * @param value the value
   * @param scale the scale
   * @param frameInterval the frame interval
   * @return the double
   */
  public static double speedToScale(int value, double scale, double frameInterval) {
    return (value * scale) / frameInterval;
  }

  /**
   * Distance from scale.
   *
   * @param value the value
   * @param scale the scale
   * @return the double
   */
  public static double distanceFromScale(double value, double scale) {
    return value / scale;
  }

  /**
   * Date as string.
   *
   * @return the string
   */
  public static String dateAsString() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    return formatter.format(date);
  }

  /**
   * Sets the limits equal.
   *
   * @param migLimits the mig limits
   * @return the double[]
   */
  public static double[] setLimitsEqual(double[] migLimits) { // min and max
    if (migLimits.length < 2) {
      LOGGER.warn("Array to short. Needs a min and max");
      return migLimits;
    }
    // Set limits to equal positive and negative
    if (migLimits[1] < 0) {
      migLimits[1] = -migLimits[0];
    }
    if (migLimits[0] > 0) {
      migLimits[0] = -migLimits[1];
    }

    // Make min and max equal for mig and conv
    if (migLimits[0] < -migLimits[1]) {
      migLimits[1] = -migLimits[0];
    } else {
      migLimits[0] = -migLimits[1];
    }

    return migLimits;
  }

  /**
   * Insert \n character after given number of chars trying to not break words.
   * 
   * @param in Input string
   * @param len line length
   * @return Wrapped string
   * @see <a href=
   *      "link">http://stackoverflow.com/questions/8314566/splitting-a-string-on-to-several-different-lines-in-java</a>
   */
  public static String stringWrap(String in, int len) {
    return stringWrap(in, len, "\n");
  }

  /**
   * Insert \n character after default number of chars trying to not break words.
   * 
   * @param in Input string
   * @return Wrapped string
   * @see <a href=
   *      "link">http://stackoverflow.com/questions/8314566/splitting-a-string-on-to-several-different-lines-in-java</a>
   */
  public static String stringWrap(String in) {
    return stringWrap(in, QuimP.LINE_WRAP, "\n");
  }

  /**
   * Insert any symbol after given number of chars trying to not break words.
   * 
   * <p>It preserve position of new line symbol if present in text.
   * 
   * @param in Input string
   * @param len line length
   * @param brek symbol to insert on line break
   * @return Wrapped string
   * @see <a href=
   *      "link">http://stackoverflow.com/questions/8314566/splitting-a-string-on-to-several-different-lines-in-java</a>
   */
  public static String stringWrap(String in, int len, String brek) {
    String[] sp = in.split("\n");
    String str = "";
    for (String s : sp) {
      s = s.concat("\n");
      str = str.concat(WordUtils.wrap(s, len, brek, false, "( |/|\\\\)"));
    }
    str = str.trim();
    // String str = WordUtils.wrap(in, len, brek, false, "( |/|\\\\)");
    return str;
  }

}
