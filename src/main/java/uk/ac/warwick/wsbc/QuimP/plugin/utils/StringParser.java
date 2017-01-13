/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.utils;

/**
 * Class for parsing parameter strings.
 * 
 * Assumes that string contains list of parameters separated by commas. Any white character are
 * removed during parsing.
 * 
 * @author p.baniukiewicz
 *
 */
public class StringParser {

    /**
     * Get number of parameters in input string.
     * 
     * Simply return number of commas in input string. Does not check other conditions.
     * 
     * @param s string to be parsed
     * @return Number of commas or 0 when empty string
     * @see <a href=
     *      "Solution">http://stackoverflow.com/questions/15625629/regex-expressions-in-java-s-vs-s</a>
     */
    static public int getNumofParam(final String s) {
        if (s.isEmpty())
            return 0;
        else {
            String l = s.replaceAll("\\s+", ""); // get rid with white spaces
            return l.length() - l.replace(",", "").length() + 1;
        }
    }

    /**
     * Split input string into separate substrings using comma separator.
     * 
     * All white characters are removed from string. Output array may contain empty fields if
     * incorrect string is given
     * 
     * @param s string to be parsed
     * @return Array of substrings or empty array if empty string given
     * 
     */
    static public String[] getParams(final String s) {
        if (s.isEmpty())
            return new String[0];
        String l = s.replaceAll("\\s+", ""); // get rid with white spaces
        return l.split(",");
    }
}
