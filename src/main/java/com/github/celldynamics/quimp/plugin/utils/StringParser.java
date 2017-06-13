package com.github.celldynamics.quimp.plugin.utils;

/**
 * Class for parsing parameter strings.
 * 
 * <p>Assumes that string contains list of parameters separated by given char. Any white character
 * are removed during parsing.
 * 
 * @author p.baniukiewicz
 *
 */
public class StringParser {

  /**
   * Get number of parameters in input string.
   * 
   * <p>Simply return number of given chars in input string. Does not check other conditions.
   * 
   * @param s string to be parsed
   * @param ch char to look for
   * @return Number of given chars or 0 when empty string
   * @see <a href=
   *      "Solution">http://stackoverflow.com/questions/15625629/regex-expressions-in-java-s-vs-s</a>
   */
  public static int getNumofParam(final String s, final char ch) {
    if (s.isEmpty()) {
      return 0;
    } else {
      String l = s.replaceAll("\\s+", ""); // get rid with white spaces
      return l.length() - l.replace(String.valueOf(ch), "").length() + 1;
    }
  }

  /**
   * Split input string into separate substrings using defined separator.
   * 
   * <p>All white characters are removed from string. Output array may contain empty fields if
   * incorrect string is given
   * 
   * @param s string to be parsed
   * @param ch char to look for
   * @return Array of substrings or empty array if empty string given
   * 
   */
  public static String[] getParams(final String s, final char ch) {
    if (s.isEmpty()) {
      return new String[0];
    }
    // String l = s.replaceAll("\\s+", ""); // get rid with white spaces
    return s.split(String.valueOf(ch));
  }

  /**
   * Remove spaces from string.
   * 
   * @param in input
   * @return input without spaces
   */
  public static String removeSpaces(String in) {
    return in.replaceAll("\\s+", "");
  }
}
