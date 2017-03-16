package uk.ac.warwick.wsbc.quimp.utils;

// TODO: Auto-generated Javadoc
/**
 * Basic exception class for ConfigReader
 * 
 * @author p.baniukiewicz
 *
 */
public class ConfigReaderException extends Exception {

  private static final long serialVersionUID = 6465559206168254036L;

  /**
   * 
   */
  public ConfigReaderException() {
  }

  /**
   * @param message
   */
  public ConfigReaderException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ConfigReaderException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ConfigReaderException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public ConfigReaderException(String message, Throwable cause, boolean enableSuppression,
          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

}
