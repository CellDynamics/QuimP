/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Frame;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * @author p.baniukiewicz
 *
 */
public class QuimpException extends Exception {
    static final Logger LOGGER = LoggerFactory.getLogger(QuimpException.class.getName());

    /**
     * Define where the message should be displayed.
     * <ol>
     * <li>CONSOLE - default, message goes to console.
     * <li>GUI - message should be shown in GUI
     * </ol>
     * 
     * @author p.baniukiewicz
     *
     */
    public enum MessageSinkTypes {
        CONSOLE, GUI
    };

    /**
     * @see MessageSinkTypes
     */
    protected MessageSinkTypes messageSinkType;

    /**
     * @param messageSinkType the messageSinkType to set
     */
    public void setMessageSinkType(MessageSinkTypes messageSinkType) {
        this.messageSinkType = messageSinkType;
    }

    /**
     * @return the type
     */
    public MessageSinkTypes getMessageSinkType() {
        return messageSinkType;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7943488580659917234L;

    /**
     * Default constructor, set message sink to console.
     */
    public QuimpException() {
        messageSinkType = MessageSinkTypes.CONSOLE;
    }

    /**
     * Allow to set type of message, where it should be displayed.
     * 
     * @param type of message
     * @see MessageSinkTypes
     */
    public QuimpException(MessageSinkTypes type) {
        this.messageSinkType = type;
    }

    /**
     * @param message
     */
    public QuimpException(String message) {
        super(message);
        messageSinkType = MessageSinkTypes.CONSOLE;
    }

    /**
     * @param message
     */
    public QuimpException(String message, MessageSinkTypes type) {
        super(message);
        this.messageSinkType = type;
    }

    /**
     * @param cause
     */
    public QuimpException(Throwable cause) {
        super(cause);
        messageSinkType = MessageSinkTypes.CONSOLE;
    }

    /**
     * @param cause
     */
    public QuimpException(Throwable cause, MessageSinkTypes type) {
        super(cause);
        this.messageSinkType = type;
    }

    /**
     * @param message
     * @param cause
     */
    public QuimpException(String message, Throwable cause) {
        super(message, cause);
        messageSinkType = MessageSinkTypes.CONSOLE;
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
        messageSinkType = MessageSinkTypes.CONSOLE;
    }

    /**
     * 
     * @param message
     * @param cause
     * @param type
     */
    public QuimpException(String message, Throwable cause, MessageSinkTypes type) {
        super(message, cause);
        this.messageSinkType = type;
    }

    /**
     * Handle this exception displaying it and logging.
     * 
     * @param frame Swing frame to display message for user, can be null
     * @param appendMessage Message added to beginning of the exception message, can be ""
     */
    public void handleException(Frame frame, String appendMessage) {
        if (getMessageSinkType() == MessageSinkTypes.GUI) { // display message as GUI
            JOptionPane.showMessageDialog(frame, QuimpToolsCollection
                    .stringWrap(appendMessage + " " + getMessage(), QuimP.LINE_WRAP), "Error",
                    JOptionPane.ERROR_MESSAGE);
            LOGGER.debug(getMessage(), this);
            LOGGER.error(appendMessage + " " + getMessage());
        } else { // or text as usual
            LOGGER.debug(getMessage(), this);
            LOGGER.error(appendMessage + " " + getMessage());
        }
    }

}
