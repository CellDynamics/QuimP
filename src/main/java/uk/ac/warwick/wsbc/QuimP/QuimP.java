package uk.ac.warwick.wsbc.QuimP;

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
     * The debug state.
     */
    static public boolean debug = Boolean.parseBoolean(
            new PropertyReader().readProperty("quimpconfig.properties", "superDebug"));
}
