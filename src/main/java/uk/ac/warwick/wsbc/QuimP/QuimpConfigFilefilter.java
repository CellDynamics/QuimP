/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Implement filter of FileDialog.
 * <p>
 * Define also default extensions for new and old file format.
 * @author baniuk
 *
 */
public class QuimpConfigFilefilter implements FilenameFilter {
    private String[] ext;
    /**
     * Extension for new file format.
     */
    public static final String newFileExt = ".QCONF";
    /**
     * Extension for old file format.
     */
    public static final String oldFileExt = ".paQP";

    /**
     * Allow to provide list of accepted extensions
     * @param ext
     */
    public QuimpConfigFilefilter(String... ext) {
        this.ext = ext;
    }

    /**
     * Default constructor.
     * <p>
     * Set active extension using choicebox in Quimp_Bar
     * 
     */
    public QuimpConfigFilefilter() {
        ext = new String[1];
        if (QuimP_Bar.newFileFormat == true)
            ext[0] = newFileExt;
        else
            ext[0] = oldFileExt;
    }

    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File dir, String name) {
        boolean ret = false;
        for (String e : ext)
            ret = ret || (name.endsWith(e.toLowerCase()) || name.endsWith(e)); // any true will set
                                                                               // flag
        return ret;
    }

    /**
     * @return the ext
     */
    public String[] getExt() {
        return ext;
    }

    /** 
     * Return defined extensions. 
     * 
     * Can be used for filling bar of file selectors.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(ext);
    }

}
