/**
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 *
 */
public class QuimpException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7943488580659917234L;

    /**
     * 
     */
    public QuimpException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public QuimpException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public QuimpException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public QuimpException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public QuimpException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

}
