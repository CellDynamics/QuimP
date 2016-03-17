/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP.plugin.dic;

/**
 * Basic class derived from Exception for purposes of DICReconstruction module
 * 
 * @author p.baniukiewicz
 * @date 13 Dec 2015
 */
@SuppressWarnings("serial")
public class DicException extends Exception {

    /**
     * Main constructor
     * 
     * @param arg0 Reason of exception
     */
    public DicException(final String arg0) {
        super(arg0);
    }

}
