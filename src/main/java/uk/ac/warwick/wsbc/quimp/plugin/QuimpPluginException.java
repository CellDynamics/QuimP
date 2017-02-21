package uk.ac.warwick.wsbc.quimp.plugin;

import uk.ac.warwick.wsbc.quimp.QuimpException;

// TODO: Auto-generated Javadoc
/**
 * Basic class derived from Exception for purposes of QuimP plugins
 * 
 * @author p.baniukiewicz
 * 
 */
@SuppressWarnings("serial")
public class QuimpPluginException extends QuimpException {

    /**
     * @param type
     */
    public QuimpPluginException(MessageSinkTypes type) {
        super(type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param type
     */
    public QuimpPluginException(String message, MessageSinkTypes type) {
        super(message, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param type
     */
    public QuimpPluginException(String message, Throwable cause, MessageSinkTypes type) {
        super(message, cause, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     * @param type
     */
    public QuimpPluginException(Throwable cause, MessageSinkTypes type) {
        super(cause, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    public QuimpPluginException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public QuimpPluginException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public QuimpPluginException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public QuimpPluginException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public QuimpPluginException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
