package uk.ac.warwick.wsbc.quimp.utils;

import javax.swing.JComponent;

/**
 * Contain UI related tools.
 * 
 * @author p.baniukiewicz
 *
 */
public class UiTools {

  /**
   * Tootltip delay for this window in ms.
   */
  public static final int TOOLTIPDELAY = 3000;

  /**
   * Set tooltip to component with line breaking.
   * 
   * @param c component
   * @param toolTip tooltip text
   */
  public static void setToolTip(JComponent c, String toolTip) {
    if (toolTip != null && !toolTip.isEmpty()) {
      String text = "<html>" + QuimpToolsCollection.stringWrap(toolTip, 40, "<br>") + "</html>";
      c.setToolTipText(text);
    }
  }

}
