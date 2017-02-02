package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * Static definitions.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimP {
    /**
     * Suffix added to every preference entry.
     */
    static public final String QUIMP_PREFS_SUFFIX = "QUIMP";
    /**
     * Desired length of line in message box.
     */
    static public final int LINE_WRAP = 60;

    /**
     * Quimp package version taken from jar.
     */
    static public final QuimpVersion TOOL_VERSION = new QuimpToolsCollection().getQuimPBuildInfo();

}
