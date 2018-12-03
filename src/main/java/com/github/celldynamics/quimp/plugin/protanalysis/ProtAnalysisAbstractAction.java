package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Abstract actions for Prot_Analysis UI.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public abstract class ProtAnalysisAbstractAction extends AbstractAction {

  protected CustomStackWindow ui;

  /**
   * Default action.
   * 
   * @param name name of the action
   * @param desc description
   * @param ui reference to UI object
   */
  public ProtAnalysisAbstractAction(String name, String desc, CustomStackWindow ui) {
    super(name);
    putValue(SHORT_DESCRIPTION, desc);
    this.ui = ui;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public abstract void actionPerformed(ActionEvent e);

}
