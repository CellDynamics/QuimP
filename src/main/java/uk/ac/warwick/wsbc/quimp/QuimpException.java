package uk.ac.warwick.wsbc.quimp;

import java.awt.Frame;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class QuimpException extends Exception {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpException.class.getName());

  /**
   * Define where the message should be displayed.
   * <ol>
   * <li>CONSOLE - default, message goes to console.
   * <li>GUI - message should be shown in GUI
   * <li>IJERROR - use IJ error handling
   * </ol>
   * 
   * @author p.baniukiewicz
   *
   */
  public enum MessageSinkTypes {

    /**
     * The console.
     */
    CONSOLE,
    /**
     * The gui.
     */
    GUI,
    /**
     * IJ error.
     */
    IJERROR
  }

  /**
   * Message sinks - where they will appear.
   * 
   * @see MessageSinkTypes
   */
  protected MessageSinkTypes messageSinkType;

  /**
   * Set message sink.
   * 
   * @param messageSinkType the messageSinkType to set
   */
  public void setMessageSinkType(MessageSinkTypes messageSinkType) {
    this.messageSinkType = messageSinkType;
  }

  /**
   * Get message sink.
   * 
   * @return the type
   */
  public MessageSinkTypes getMessageSinkType() {
    return messageSinkType;
  }

  /**
   * serialVersionUID.
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
   * Default exception with message. Will be shown in console.
   * 
   * @param message message
   */
  public QuimpException(String message) {
    super(message);
    messageSinkType = MessageSinkTypes.CONSOLE;
  }

  /**
   * Exception with message.
   * 
   * @param message message
   * @param type where to show message
   */
  public QuimpException(String message, MessageSinkTypes type) {
    super(message);
    this.messageSinkType = type;
  }

  /**
   * QuimpException.
   * 
   * @param cause cause
   */
  public QuimpException(Throwable cause) {
    super(cause);
    messageSinkType = MessageSinkTypes.CONSOLE;
  }

  /**
   * QuimpException.
   * 
   * @param cause cause
   * @param type type
   */
  public QuimpException(Throwable cause, MessageSinkTypes type) {
    super(cause);
    this.messageSinkType = type;
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   */
  public QuimpException(String message, Throwable cause) {
    super(message, cause);
    messageSinkType = MessageSinkTypes.CONSOLE;
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   * @param enableSuppression enableSuppression
   * @param writableStackTrace writableStackTrace
   */
  public QuimpException(String message, Throwable cause, boolean enableSuppression,
          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    messageSinkType = MessageSinkTypes.CONSOLE;
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   * @param type where to show message
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
    LOGGER.debug(getMessage(), this);
    switch (getMessageSinkType()) {
      case GUI:
        JOptionPane.showMessageDialog(frame, QuimpToolsCollection
                .stringWrap(appendMessage + " " + getMessage(), QuimP.LINE_WRAP), "Error",
                JOptionPane.ERROR_MESSAGE);
        break;
      case CONSOLE:
        LOGGER.debug(getMessage(), this);
        LOGGER.error(appendMessage + " " + getMessage());
        break;
      case IJERROR:
        IJ.error(appendMessage + " " + getMessage());
        break;
      default:
    }
  }

}
