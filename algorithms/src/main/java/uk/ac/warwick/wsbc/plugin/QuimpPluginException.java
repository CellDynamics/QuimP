package uk.ac.warwick.wsbc.plugin;

/**
 * Basic class derived from Exception for purposes of Interpolate module 
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 */
@SuppressWarnings("serial")
public class QuimpPluginException extends Exception {

	/**
	 * Main constructor
	 * 
	 * @param arg0 Reason of exception
	 */
	public QuimpPluginException(String arg0) {
		super(arg0);
	}
	
	public QuimpPluginException(String arg0, Throwable cause) {
		super(arg0,cause);
	}
}
