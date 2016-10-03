/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * @author p.baniukiewicz
 *
 */
public class RandomWalkException extends QuimpPluginException {

    /**
     * 
     */
    private static final long serialVersionUID = -29506627099108519L;

    /**
     * 
     */
    public RandomWalkException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public RandomWalkException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param arg0
     * @param cause
     */
    public RandomWalkException(String arg0, Throwable cause) {
        super(arg0, cause);
    }

    /**
     * @param arg0
     */
    public RandomWalkException(String arg0) {
        super(arg0);
    }

    /**
     * @param cause
     */
    public RandomWalkException(Throwable cause) {
        super(cause);
    }

}
