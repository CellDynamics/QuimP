/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;

// TODO: Auto-generated Javadoc
/**
 * Exception class for Random Walker plugin. Redirect all exceptions to GUI.
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkException extends QuimpPluginException {

    /**
     * @param type
     */
    public RandomWalkException(MessageSinkTypes type) {
        super(type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param type
     */
    public RandomWalkException(String message, MessageSinkTypes type) {
        super(message, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param type
     */
    public RandomWalkException(String message, Throwable cause, MessageSinkTypes type) {
        super(message, cause, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     * @param type
     */
    public RandomWalkException(Throwable cause, MessageSinkTypes type) {
        super(cause, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    private static final long serialVersionUID = -29506627099108519L;

    /**
     * 
     */
    public RandomWalkException() {
        super(MessageSinkTypes.GUI);
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
        super(arg0, cause, MessageSinkTypes.GUI);
    }

    /**
     * @param arg0
     */
    public RandomWalkException(String arg0) {
        super(arg0, MessageSinkTypes.GUI);
    }

    /**
     * @param cause
     */
    public RandomWalkException(Throwable cause) {
        super(cause, MessageSinkTypes.GUI);
    }

}
