package uk.ac.warwick.wsbc.tools.images;

/**
 * Basic class derived from Exception for purposes of Interpolate module 
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 */
@SuppressWarnings("serial")
public class InterpolateException extends Exception {

	/**
	 * Main constructor
	 * 
	 * @param arg0 Reason of exception
	 */
	public InterpolateException(String arg0) {
		super(arg0);
	}
	
	public InterpolateException(String arg0, Throwable cause) {
		super(arg0,cause);
	}
}
