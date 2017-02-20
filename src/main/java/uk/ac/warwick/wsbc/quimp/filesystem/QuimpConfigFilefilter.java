package uk.ac.warwick.wsbc.quimp.filesystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import uk.ac.warwick.wsbc.quimp.plugin.bar.QuimP_Bar;

// TODO: Auto-generated Javadoc
/**
 * Implement filter of FileDialog.
 * <p>
 * Define also default extensions for new and old file format.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimpConfigFilefilter implements FilenameFilter {
    private String[] ext;

    /**
     * Allow to provide list of accepted extensions with dot.
     * 
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
            ext[0] = FileExtensions.newConfigFileExt;
        else
            ext[0] = FileExtensions.configFileExt;
    }

    /*
     * (non-Javadoc)
     * 
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
     * @return the extension
     */
    public String[] getExt() {
        return ext;
    }

    /**
     * Return defined extensions.
     * 
     * Can be used for filling bar of file selectors.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(ext);
    }

}
