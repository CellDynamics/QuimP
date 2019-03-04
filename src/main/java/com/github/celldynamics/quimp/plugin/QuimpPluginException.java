package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.QuimpException;

/**
 * Basic class derived from Exception for purposes of QuimP plugins.
 * 
 * @author p.baniukiewicz
 * @see QuimpException
 * 
 */
@SuppressWarnings("serial")
public class QuimpPluginException extends QuimpException {

  /**
   * Exception constructor for given sink type (GUI or Text).
   * 
   * @param type of message
   */
  public QuimpPluginException(MessageSinkTypes type) {
    super(type);
  }

  /**
   * Exception constructor for given sink type (GUI or Text).
   * 
   * @param message message
   * @param type of message
   */
  public QuimpPluginException(String message, MessageSinkTypes type) {
    super(message, type);
  }

  /**
   * Exception constructor for given sink type (GUI or Text).
   * 
   * @param message message
   * @param type of message
   * @param persistent if true sink can not be changed by {@link #setMessageSinkType(java.util.Set)}
   */
  public QuimpPluginException(String message, MessageSinkTypes type, boolean persistent) {
    super(message, type, persistent);
  }

  /**
   * Exception constructor for given sink type (GUI or Text).
   * 
   * @param message message
   * @param cause cause of exception
   * @param type of message
   */
  public QuimpPluginException(String message, Throwable cause, MessageSinkTypes type) {
    super(message, cause, type);
  }

  /**
   * Exception constructor for given sink type (GUI or Text).
   * 
   * @param cause cause of exception
   * @param type of message
   */
  public QuimpPluginException(Throwable cause, MessageSinkTypes type) {
    super(cause, type);
  }

  /**
   * Default constructor for text sink type.
   */
  public QuimpPluginException() {
    super();
  }

  /**
   * Constructor for text sink type.
   * 
   * @param message message
   * @param cause cause of exception
   * @param enableSuppression enableSuppression
   * @param writableStackTrace writableStackTrace
   */
  public QuimpPluginException(String message, Throwable cause, boolean enableSuppression,
          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * Constructor for text sink type.
   * 
   * @param message message
   * @param cause cause of exception
   */
  public QuimpPluginException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor for text sink type.
   * 
   * @param message message
   */
  public QuimpPluginException(String message) {
    super(message);
  }

  /**
   * Constructor for text sink type.
   * 
   * @param cause message
   */
  public QuimpPluginException(Throwable cause) {
    super(cause);
    if (cause instanceof QuimpException) {
      setMessageSinkType(((QuimpException) cause).getMessageSinkType());
    }
  }

}
