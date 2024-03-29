package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.utils.UiTools;

/**
 * Abstract actions for Prot_Analysis UI.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public abstract class ProtAnalysisAbstractAction extends AbstractAction {
  protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  protected ProtAnalysisUi ui;
  // helpers retrieved from ui
  protected Prot_Analysis model;
  protected ProtAnalysisOptions options;

  /**
   * Constructor for calling actions without AbstractAction (as Listener but not Action).
   * 
   * <p>Allows to pass only reference to window and use it as in {@link ActionClearPoints}.
   * 
   * @param ui reference to window
   */
  public ProtAnalysisAbstractAction(ProtAnalysisUi ui) {
    this.ui = ui;
    this.model = ui.getModel();
    this.options = (ProtAnalysisOptions) model.getOptions();
  }

  /**
   * Default action.
   * 
   * @param name name of the action
   * @param desc description
   * @param ui reference to UI object
   */
  public ProtAnalysisAbstractAction(String name, String desc, ProtAnalysisUi ui) {
    super(name);
    putValue(SHORT_DESCRIPTION, UiTools.getToolTipString(desc));
    this.ui = ui;
    this.model = ui.getModel();
    this.options = (ProtAnalysisOptions) model.getOptions();
  }

  /**
   * Helper for updating current frame.
   */
  public void updateCurrentView() {
    ui.updateOverlay(ui.getImagePlus().getCurrentSlice());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public abstract void actionPerformed(ActionEvent e);

}
