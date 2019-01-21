package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

@Deprecated
public class ActionNotSupported extends ProtAnalysisAbstractAction {

  private String name;

  /**
   * Create action not related to UI element.
   * 
   * @param ui window reference
   */
  public ActionNotSupported(CustomStackWindow ui) {
    super(ui);
  }

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionNotSupported(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
    this.name = name;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    logger.warn(name + " Not implemented yet");
    // throw new NotImplementedException("Not implemented yet");

  }

}
