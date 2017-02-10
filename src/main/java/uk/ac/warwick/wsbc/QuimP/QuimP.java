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

    /**
     * Switch on/off additional debug.
     * 
     * This switch causes that additional debug information can be produced.
     */
    static public boolean SUPER_DEBUG =
            Boolean.parseBoolean(System.getProperty("quimpconfig.superDebug"));
    /**
     * This field keeps localisation of -quimp plugins.
     * 
     * By default it is Fiji.app/plugins folder but it can be overwritten by setting system
     * property.
     */
    static public String PLUGIN_DIR = System.getProperty("quimpconfig.pluginDirectory");
}
