package uk.ac.warwick.wsbc.quimp;

/**
 * Exception thrown by BOA class. Uses {@link QuimpException.MessageSinkTypes#CONSOLE} as sink.
 * 
 * <p>Contains additional information on frame and type.
 * 
 * @author rtyson
 *
 */
public class BoaException extends QuimpException {

  private static final long serialVersionUID = 1L;
  private int frame;
  private int type;

  /**
   * Create exception object for given frame and type.
   * 
   * @param msg message
   * @param f frame
   * @param t type
   */
  public BoaException(String msg, int f, int t) {
    super(msg);
    frame = f;
    type = t;
  }

  /**
   * Create exception object.
   * 
   * @param string message
   */
  public BoaException(String string) {
    super(string);
  }

  /**
   * Get frame.
   * 
   * @return frame
   */
  public int getFrame() {
    return frame;
  }

  /**
   * Get type.
   * 
   * @return type
   */
  public int getType() {
    return type;
  }

  /**
   * BoaException.
   * 
   */
  public BoaException() {
    super();
  }

  /**
   * BoaException.
   * 
   * @param message message
   * @param cause cause
   * @param enableSuppression enableSuppression
   * @param writableStackTrace writableStackTrace
   */
  public BoaException(String message, Throwable cause, boolean enableSuppression,
          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * BoaException.
   * 
   * @param message message
   * @param cause cause
   */
  public BoaException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * BoaException.
   * 
   * @param cause cause
   */
  public BoaException(Throwable cause) {
    super(cause);
  }
}