package com.github.celldynamics.quimp.utils;

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
   * Length of tool tip text.
   */
  public static int toolTipLength = 40;

  /**
   * Set tooltip to component with line breaking.
   * 
   * @param c component
   * @param toolTip tooltip text
   */
  public static void setToolTip(JComponent c, String toolTip) {
    if (toolTip != null && !toolTip.isEmpty()) {
      c.setToolTipText(getToolTipString(toolTip));
    }
  }

  /**
   * Get tooltip string wrapped.
   * 
   * @param toolTip String to wrap
   * @return wrapped string
   */
  public static String getToolTipString(String toolTip) {
    return "<html>" + QuimpToolsCollection.stringWrap(toolTip, toolTipLength, "<br>") + "</html>";
  }

}
