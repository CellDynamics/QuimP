package com.github.celldynamics.quimp.plugin.randomwalk;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;

// TODO: Auto-generated Javadoc
/**
 * Exception class for Random Walker plugin. Redirect all exceptions to GUI.
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkException extends QuimpPluginException {

  /**
   * Construct exception with given sink type.
   * 
   * @param type exception sink.
   */
  public RandomWalkException(MessageSinkTypes type) {
    super(type);
  }

  /**
   * Construct exception with given sink type and message.
   * 
   * @param message message
   * @param type sink type
   */
  public RandomWalkException(String message, MessageSinkTypes type) {
    super(message, type);
  }

  /**
   * Construct exception with given sink type, message and cause.
   * 
   * @param message message
   * @param cause cause
   * @param type sink type
   */
  public RandomWalkException(String message, Throwable cause, MessageSinkTypes type) {
    super(message, cause, type);
  }

  /**
   * Construct exception with given sink type and cause.
   * 
   * @param cause cause
   * @param type sink type
   */
  public RandomWalkException(Throwable cause, MessageSinkTypes type) {
    super(cause, type);
  }

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -29506627099108519L;

  /**
   * Default exception with sink set to GUI.
   */
  public RandomWalkException() {
    super(MessageSinkTypes.GUI);
  }

  /**
   * Construct exception.
   * 
   * @param message message
   * @param cause cause
   * @param enableSuppression enableSuppression
   * @param writableStackTrace writableStackTrace
   */
  public RandomWalkException(String message, Throwable cause, boolean enableSuppression,
          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * Construct exception with default sin set to GUI.
   * 
   * @param message message
   * @param cause cause
   */
  public RandomWalkException(String message, Throwable cause) {
    super(message, cause, MessageSinkTypes.GUI);
  }

  /**
   * Construct exception with default sin set to GUI.
   * 
   * @param message message
   */
  public RandomWalkException(String message) {
    super(message, MessageSinkTypes.GUI);
  }

  /**
   * Construct exception with default sin set to GUI.
   * 
   * @param cause cause
   */
  public RandomWalkException(Throwable cause) {
    super(cause, MessageSinkTypes.GUI);
  }

}
