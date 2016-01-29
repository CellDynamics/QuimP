/**
 * @file QWindowBuilder.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public abstract class QWindowBuilder {

	protected Frame pluginWnd;
	
	public void BuildWindow(String[][] def) {
		pluginWnd = new Frame(def[0][0]);
		pluginWnd.setSize(400,400);
		pluginWnd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			{
				pluginWnd.setVisible(false);
			}
		});
		pluginWnd.setVisible(true);
	}
}
