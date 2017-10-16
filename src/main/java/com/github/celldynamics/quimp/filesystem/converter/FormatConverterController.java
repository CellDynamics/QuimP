package com.github.celldynamics.quimp.filesystem.converter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileDialogEx;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ij.IJ;
import ij.io.OpenDialog;

/**
 * Performs conversion actions. UI interface to {@link FormatConverter}
 * 
 * @author p.baniukiewicz
 * @see FormatConverterUi
 */
public class FormatConverterController extends FormatConverter {

  private FormatConverterModel model;
  private FormatConverterUi view;

  /**
   * Default constructor.
   */
  public FormatConverterController() {
    super();
    initializeLogger();
    model = new FormatConverterModel();
    view = new FormatConverterUi(model);
    view.getOkButton().addActionListener(new GenerateActionListener());
    view.getLoadButton().addActionListener(new LoadActionListener());
    view.getConvertButton().addActionListener(new ConvertActionListener());
    view.getCancelButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        view.dispose();
      }
    });
  }

  /**
   * Constructor allowing passing file to process.
   * 
   * @param fileToLoad to process
   * @see #doConversion()
   * @see #getModel()
   */
  public FormatConverterController(Path fileToLoad) {
    this();
    model.convertedFile = fileToLoad;
  }

  /**
   * Display UI.
   */
  public void showUi() {
    view.setVisible(true);
  }

  /**
   * Get model and allow to set own configuration in no-gui mode.
   * 
   * @return the model
   */
  public FormatConverterModel getModel() {
    return model;
  }

  /**
   * Re-initialises logger to log event>=INFO to internal console hiding them from stdout.
   * 
   * <p>If logging level is less than INFO, all messages end in console as well. By default Fiji
   * sets level to INFO, any external configuration (e.g. quimp-logback.xml) will produce logs in
   * stdout
   */
  private void initializeLogger() {
    LoggerContext lc = logger.getLoggerContext();
    MyAppender myAppender = new MyAppender();
    myAppender.setContext(lc);
    myAppender.setName("internalr");
    ThresholdFilter th = new ThresholdFilter();
    th.setLevel(Level.INFO.toString()); // above info pass, below hide
    th.start();
    myAppender.addFilter(th);
    myAppender.start();
    logger.addAppender(myAppender);
    // trick to prevent doubling logs in windows and console (for normal debug state)
    if (logger.getEffectiveLevel().isGreaterOrEqual(Level.INFO)) {
      logger.setAdditive(false);
    } // otherwise if debug set below INFO, all will be doubled (but in this window only those
    // >=INFO, Console will capture everything)
  }

  /**
   * Handle Load button.
   * 
   * @author p.baniukiewicz
   *
   */
  private class LoadActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      FileDialogEx od = new FileDialogEx(IJ.getInstance());
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setExtension(FileExtensions.newConfigFileExt);
      if (od.showOpenDialog() == null) {
        return;
      }
      try {
        logger.info("-------------------------------------------------------------------------");
        model.convertedFile = od.getPath();
        attachFile(od.getPath().toFile());
        logger.info(FormatConverterController.this.toString());
      } catch (QuimpException e1) {
        e1.logger.addAppender(logger.getAppender("internalr"));
        e1.logger.setAdditive(false); // show only in appender
        e1.setMessageSinkType(MessageSinkTypes.CONSOLE);
        e1.handleException(null, "File could not be loaded.");
      }
    }

  }

  /**
   * Handle Convert button.
   * 
   * @author p.baniukiewicz
   *
   */
  private class ConvertActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      FileDialogEx od = new FileDialogEx(IJ.getInstance());
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setExtension(FileExtensions.newConfigFileExt, FileExtensions.configFileExt);
      if (od.showOpenDialog() == null) {
        return;
      }
      try {
        logger.info("-------------------------------------------------------------------------");
        attachFile(od.getPath().toFile());
        doConversion();
      } catch (QuimpException e1) {
        e1.logger.addAppender(logger.getAppender("internalr"));
        e1.logger.setAdditive(false); // show only in appender
        e1.setMessageSinkType(MessageSinkTypes.CONSOLE);
        e1.handleException(null, "File could not be loaded.");
      }
    }
  }

  /**
   * Handle Generate button.
   * 
   * @author p.baniukiewicz
   *
   */
  private class GenerateActionListener implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (isFileLoaded() == QconfLoader.QCONF_INVALID) {
        logger.error("Load valid QCONF file first");
        return;
      }
      logger.info("Generating data...");
      saveDataFiles();
    }
  }

  /**
   * Save data selected in {@link FormatConverterModel}.
   * 
   * @see FormatConverterModel#getStatus()
   * @see FormatConverterUi
   */
  public void saveDataFiles() {
    List<String> status = model.getStatus();
    logger.debug(status.toString());
    try {
      for (String s : status) {
        logger.info("Saving " + s);
        switch (s.toLowerCase()) { // if user provide in other case
          case FormatConverterUi.MAP_MOTILITY: // this is also String displayed on control
            saveMaps(STmap.MOTILITY);
            break;
          case FormatConverterUi.MAP_CONVEXITY:
            saveMaps(STmap.CONVEXITY);
            break;
          case FormatConverterUi.MAP_COORD:
            saveMaps(STmap.COORD);
            break;
          case FormatConverterUi.MAP_FLUORES:
            saveMaps(STmap.ALLFLU);
            break;
          case FormatConverterUi.MAP_ORIGIN:
            saveMaps(STmap.ORIGIN);
            break;
          case FormatConverterUi.MAP_X_COORDS:
            saveMaps(STmap.XMAP);
            break;
          case FormatConverterUi.MAP_Y_COORDS:
            saveMaps(STmap.YMAP);
            break;
          case FormatConverterUi.BOA_CENTROID:
            saveBoaCentroids();
            break;
          case FormatConverterUi.BOA_SNAKES:
            saveBoaSnakes(view.getChckbxMultiFileOutput());
            break;
          case FormatConverterUi.ECMM_CENTROID:
            saveEcmmCentroids();
            break;
          case FormatConverterUi.ECCM_OUTLINES:
            saveEcmmOutlines(view.getChckbxMultiFileOutput());
            break;
          case FormatConverterUi.STATS_FLUORES:
            saveStatFluores();
            break;
          case FormatConverterUi.STATS_GEOMETRIC:
            saveStatGeom();
            break;
          case FormatConverterUi.STATS_Q11:
            saveStats();
            break;
          default:
            logger.warn("Parameter " + s + " is inproper");
        }
      }
    } catch (QuimpException qe) {
      qe.logger.addAppender(logger.getAppender("internalr"));
      qe.logger.setAdditive(false); // show only in appender
      qe.setMessageSinkType(MessageSinkTypes.CONSOLE);
      qe.handleException(null, "Conversion stopped. Some data can not be accessed.");
    }

  }

  /**
   * Redirect log events to application window. Type errors and warns in red.
   * 
   * @author p.baniukiewicz
   *
   */
  private class MyAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent arg0) {
      StyleContext sc = StyleContext.getDefaultStyleContext();
      StyledDocument doc = view.getInfoText().getStyledDocument();
      Color textColor = Color.BLACK;
      if (arg0.getLevel().isGreaterOrEqual(Level.WARN)) {
        textColor = Color.RED;
      }
      AttributeSet aset =
              sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textColor);

      String message = arg0.getMessage() + '\n';
      try {
        // fake space simulation
        doc.insertString(doc.getLength(), message.replace("\t", "... "), aset);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }
}
