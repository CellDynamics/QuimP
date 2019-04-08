package com.github.celldynamics.quimp;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ch.qos.logback.classic.Logger;
import ij.IJ;

/**
 * Template of exceptions thrown by QuimP API.
 * 
 * <p>Allow for handling them and present to user depending on {@link MessageSinkTypes}.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimpException extends Exception {

  /**
   * If true sink type will not be changed in {@link #setMessageSinkType(MessageSinkTypes...)} and
   * {@link #setMessageSinkType(Set)}.
   */
  protected boolean persistent = false;
  /**
   * The LOGGER.
   */
  public Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

  /**
   * Define where the message should be displayed. Anu combination of values is supported by
   * {@link QuimpException#handleException(Frame, String)}.
   * <ol>
   * <li>CONSOLE - default, message goes to console.
   * <li>GUI - message should be shown in GUI
   * <li>IJERROR - use IJ error handling
   * <li>MESSAGE - print message in console using INFO level
   * <li>NONE - {@link QuimpException#handleException(Frame, String)} will return just formatted
   * string without any action.
   * </ol>
   * 
   * @author p.baniukiewicz
   *
   */
  public enum MessageSinkTypes {

    /**
     * Console sink (stderr).
     */
    CONSOLE,
    /**
     * Display window with message.
     */
    GUI,
    /**
     * Use IJ.error for log.
     */
    IJERROR,
    /**
     * Print message in console.
     * 
     * <p>Similar to {@link #CONSOLE} but message has INFO level.
     */
    MESSAGE,
    /**
     * None of above, just return formatted exception string.
     */
    NONE;
  }

  /**
   * Message sinks - where they will appear.
   * 
   * @see MessageSinkTypes
   */
  protected EnumSet<MessageSinkTypes> messageSinkType;

  /**
   * Set message sink. It can be combination of {@link MessageSinkTypes} values.
   * 
   * @param messageSinkType the messageSinkType to set.
   */
  public void setMessageSinkType(MessageSinkTypes... messageSinkType) {
    if (persistent) {
      return;
    }
    this.messageSinkType = EnumSet.copyOf(Arrays.asList(messageSinkType));
  }

  /**
   * Set message sink. It can be combination of {@link MessageSinkTypes} values.
   * 
   * @param messageSinkType the messageSinkType to set.
   */
  public void setMessageSinkType(Set<MessageSinkTypes> messageSinkType) {
    if (persistent) {
      return;
    }
    this.messageSinkType = EnumSet.copyOf(messageSinkType);
  }

  /**
   * Get message sink.
   * 
   * @return the type
   */
  public Set<MessageSinkTypes> getMessageSinkType() {
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
    this.messageSinkType = EnumSet.of(MessageSinkTypes.CONSOLE);
  }

  /**
   * Allow to set type of message, where it should be displayed.
   * 
   * @param types of message (list)
   * @see MessageSinkTypes
   */
  public QuimpException(MessageSinkTypes... types) {
    setMessageSinkType(types);
  }

  /**
   * Allow to set type of message, where it should be displayed.
   * 
   * @param type of message (one)
   * @see MessageSinkTypes
   */
  public QuimpException(MessageSinkTypes type) {
    this.messageSinkType = EnumSet.of(type);
  }

  /**
   * Default exception with message. Will be shown in console.
   * 
   * @param message message
   */
  public QuimpException(String message) {
    super(message);
    this.messageSinkType = EnumSet.of(MessageSinkTypes.CONSOLE);
  }

  /**
   * Exception with message.
   * 
   * @param message message
   * @param type where to show message
   */
  public QuimpException(String message, MessageSinkTypes type) {
    super(message);
    this.messageSinkType = EnumSet.of(type);
  }

  /**
   * Exception with message.
   * 
   * @param message message
   * @param type where to show message
   * @param persistent if true the sink will not be overwritten
   */
  public QuimpException(String message, MessageSinkTypes type, boolean persistent) {
    super(message);
    this.messageSinkType = EnumSet.of(type);
    this.persistent = persistent;
  }

  /**
   * Exception with message.
   * 
   * @param message message
   * @param types where to show message
   */
  public QuimpException(String message, MessageSinkTypes... types) {
    super(message);
    setMessageSinkType(types);
  }

  /**
   * QuimpException.
   * 
   * @param cause cause
   */
  public QuimpException(Throwable cause) {
    super(cause);
    this.messageSinkType = EnumSet.of(MessageSinkTypes.CONSOLE);
  }

  /**
   * QuimpException.
   * 
   * @param cause cause
   * @param type type (one)
   */
  public QuimpException(Throwable cause, MessageSinkTypes type) {
    super(cause);
    this.messageSinkType = EnumSet.of(type);
  }

  /**
   * QuimpException.
   * 
   * @param cause cause
   * @param types type (list)
   */
  public QuimpException(Throwable cause, MessageSinkTypes... types) {
    super(cause);
    setMessageSinkType(types);
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   */
  public QuimpException(String message, Throwable cause) {
    super(message, cause);
    this.messageSinkType = EnumSet.of(MessageSinkTypes.CONSOLE);
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
    this.messageSinkType = EnumSet.of(MessageSinkTypes.CONSOLE);
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   * @param type where to show message (one)
   */
  public QuimpException(String message, Throwable cause, MessageSinkTypes type) {
    super(message, cause);
    this.messageSinkType = EnumSet.of(type);
  }

  /**
   * QuimpException.
   * 
   * @param message message
   * @param cause cause
   * @param types where to show message (list)
   */
  public QuimpException(String message, Throwable cause, MessageSinkTypes... types) {
    super(message, cause);
    setMessageSinkType(types);
  }

  /**
   * Handle this exception displaying it and logging depending on {@link #messageSinkType}.
   * 
   * @param frame Swing frame to display message for user, can be null
   * @param appendMessage Message added to beginning of the exception message, can be ""
   * @return Exception message
   */
  public String handleException(Frame frame, String appendMessage) {
    logger.debug(getMessage(), this);
    String message = prepareMessage(this, appendMessage);

    if (getMessageSinkType().contains(MessageSinkTypes.CONSOLE)) {
      logger.error(message);
    }
    if (getMessageSinkType().contains(MessageSinkTypes.MESSAGE)) {
      logger.info(message);
    }
    if (getMessageSinkType().contains(MessageSinkTypes.GUI)) {
      showGuiWithMessage(frame, message);
    }
    if (getMessageSinkType().contains(MessageSinkTypes.IJERROR)) {
      List<String> ex = getExceptionMessageChain(this);
      if (ex.size() > 1) {
        IJ.log("Error messages stack:");
        for (int i = 0; i < ex.size(); i++) {
          if (ex.get(i) != null) {
            if (i == 0) {
              IJ.log(" " + ex.get(i));
            } else {
              IJ.log("  ->" + ex.get(i));
            }
          }
        }
      }
      IJ.handleException(this);
      IJ.error(QuimpToolsCollection.stringWrap(message, QuimP.LINE_WRAP));
    }
    return message;
  }

  /**
   * Show UI with message.
   * 
   * @param frame Swing frame to display message for user, can be null
   * @param message message produced by {@link #prepareMessage(Exception, String)}
   */
  public static void showGuiWithMessage(Frame frame, String message) {
    JOptionPane.showMessageDialog(frame, QuimpToolsCollection.stringWrap(message, QuimP.LINE_WRAP),
            "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Prepare formatted message to show.
   * 
   * @param ex Exception to dig in.
   * @param appendMessage Message added to beginning of the exception message, can be ""
   * @return Exception message
   */
  public static String prepareMessage(Exception ex, String appendMessage) {
    String message = appendMessage;
    List<String> ch = getExceptionMessageChain(ex);
    int l = 0;
    for (String c : ch) {
      if (c != null) {
        if (l == 0) {
          message = message.concat(" (" + c);
        } else {
          message = message.concat(" -> " + c);
        }
        l++;
      }
    }
    if (l > 0) {
      message = message.concat(")");
    }
    return message;
  }

  /**
   * Get messages from exception stack.
   * 
   * <p>Taken from stackoverflow.com/questions/15987258/
   * 
   * @param throwable exception
   * @return list of messages from underlying exceptions
   */
  public static List<String> getExceptionMessageChain(Throwable throwable) {
    List<String> result = new ArrayList<String>();
    while (throwable != null) {
      result.add(throwable.getMessage());
      throwable = throwable.getCause();
    }
    return result;
  }

}
