package com.github.celldynamics.quimp.filesystem.converter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import com.github.celldynamics.quimp.plugin.AbstractPluginTemplate;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
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
public class FormatConverterController extends AbstractPluginTemplate {

  private FormatConverterUi view;
  private FormatConverter fc;

  /**
   * Default constructor.
   */
  public FormatConverterController() {
    super(new FormatConverterModel());
    fc = new FormatConverter();

    FormatConverterModel model = (FormatConverterModel) options;
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
   * @see FormatConverter#doConversion()
   * @see #getModel()
   */
  public FormatConverterController(Path fileToLoad) {
    this();
    FormatConverterModel model = (FormatConverterModel) options;
    model.paramFile = fileToLoad.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractPluginTemplate#executer(java.lang.String)
   */
  @Override
  protected void executer() throws QuimpException {
    super.executer();
    FormatConverterModel model = (FormatConverterModel) options;
    if (model.getStatus().isEmpty()) {
      runPluginConversion();
    } else {
      runPluginExtraction();
    }
  }

  /**
   * Run in convert mode.
   * 
   * @throws QuimpException QuimpException
   */
  private void runPluginConversion() throws QuimpException {
    FormatConverterModel model = (FormatConverterModel) options;
    fc.attachFile(new File(model.paramFile));
    fc.doConversion();
    publishMacroString();
  }

  /**
   * Run in QCONF extraction mode.
   * 
   * @throws QuimpException on file load error
   */
  private void runPluginExtraction() throws QuimpException {
    FormatConverterModel model = (FormatConverterModel) options;
    fc.attachFile(new File(model.paramFile));
    saveDataFiles();
    publishMacroString();
  }

  /**
   * Get model and allow to set own configuration in no-gui mode.
   * 
   * @return the model
   */
  public FormatConverterModel getModel() {
    FormatConverterModel model = (FormatConverterModel) options;
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
    LoggerContext lc = FormatConverter.logger.getLoggerContext();
    MyAppender myAppender = new MyAppender();
    myAppender.setContext(lc);
    myAppender.setName("internalr");
    ThresholdFilter th = new ThresholdFilter();
    th.setLevel(Level.INFO.toString()); // above info pass, below hide
    th.start();
    myAppender.addFilter(th);
    myAppender.start();
    FormatConverter.logger.addAppender(myAppender);
    // trick to prevent doubling logs in windows and console (for normal debug state)
    if (FormatConverter.logger.getEffectiveLevel().isGreaterOrEqual(Level.INFO)) {
      FormatConverter.logger.setAdditive(false);
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
    FormatConverterModel model = (FormatConverterModel) options;

    @Override
    public void actionPerformed(ActionEvent e) {
      FileDialogEx od = new FileDialogEx(IJ.getInstance());
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setExtension(FileExtensions.newConfigFileExt);
      if (od.showOpenDialog() == null) {
        return;
      }
      try {
        FormatConverter.logger
                .info("-------------------------------------------------------------------------");
        model.paramFile = od.getPath().toString();
        fc.attachFile(od.getPath().toFile());
        FormatConverter.logger.info(fc.toString());
      } catch (QuimpException e1) {
        e1.logger.addAppender(FormatConverter.logger.getAppender("internalr"));
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
    FormatConverterModel model = (FormatConverterModel) options;

    @Override
    public void actionPerformed(ActionEvent e) {
      FileDialogEx od = new FileDialogEx(IJ.getInstance());
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setExtension(FileExtensions.newConfigFileExt, FileExtensions.configFileExt);
      if (od.showOpenDialog() == null) {
        return;
      }
      try {
        FormatConverter.logger
                .info("-------------------------------------------------------------------------");
        // this will clear UI and clear status list as well. Empty status list stands for conversion
        view.setUiElements(false);
        model.paramFile = od.getPath().toString();
        runPluginConversion(); // run in convert mode - status clear
      } catch (QuimpException e1) {
        e1.logger.addAppender(FormatConverter.logger.getAppender("internalr"));
        e1.logger.setAdditive(false); // show only in appender
        e1.setMessageSinkType(MessageSinkTypes.CONSOLE);
        e1.handleException(null, "File could not be converted.");
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
      if (fc.isFileLoaded() == QconfLoader.QCONF_INVALID) {
        FormatConverter.logger.error("Load valid QCONF file first");
        return;
      }
      try {
        FormatConverter.logger.info("Generating data...");
        runPluginExtraction();
      } catch (QuimpException e1) {
        e1.logger.addAppender(FormatConverter.logger.getAppender("internalr"));
        e1.logger.setAdditive(false); // show only in appender
        e1.setMessageSinkType(MessageSinkTypes.CONSOLE);
        e1.handleException(null, "File could not be converted.");
      }
    }
  }

  /**
   * Save data selected in {@link FormatConverterModel}.
   * 
   * @throws QuimpException
   * 
   * @see FormatConverterModel#getStatus()
   * @see FormatConverterUi
   */
  public void saveDataFiles() throws QuimpException {
    FormatConverterModel model = (FormatConverterModel) options;
    List<String> status = model.getStatus();
    FormatConverter.logger.debug(status.toString());
    for (String s : status) {
      FormatConverter.logger.info("Saving " + s);
      switch (s.toLowerCase()) { // if user provide in other case
        case FormatConverterUi.MAP_MOTILITY: // this is also String displayed on control
          fc.saveMaps(STmap.MOTILITY);
          break;
        case FormatConverterUi.MAP_CONVEXITY:
          fc.saveMaps(STmap.CONVEXITY);
          break;
        case FormatConverterUi.MAP_COORD:
          fc.saveMaps(STmap.COORD);
          break;
        case FormatConverterUi.MAP_FLUORES:
          fc.saveMaps(STmap.ALLFLU);
          break;
        case FormatConverterUi.MAP_ORIGIN:
          fc.saveMaps(STmap.ORIGIN);
          break;
        case FormatConverterUi.MAP_X_COORDS:
          fc.saveMaps(STmap.XMAP);
          break;
        case FormatConverterUi.MAP_Y_COORDS:
          fc.saveMaps(STmap.YMAP);
          break;
        case FormatConverterUi.BOA_CENTROID:
          fc.saveBoaCentroids();
          break;
        case FormatConverterUi.BOA_SNAKES:
          fc.saveBoaSnakes(view.getChckbxMultiFileOutput());
          break;
        case FormatConverterUi.ECMM_CENTROID:
          fc.saveEcmmCentroids();
          break;
        case FormatConverterUi.ECCM_OUTLINES:
          fc.saveEcmmOutlines(view.getChckbxMultiFileOutput());
          break;
        case FormatConverterUi.STATS_FLUORES:
          fc.saveStatFluores();
          break;
        case FormatConverterUi.STATS_GEOMETRIC:
          fc.saveStatGeom();
          break;
        case FormatConverterUi.STATS_Q11:
          fc.saveStats();
          break;
        default:
          FormatConverter.logger.warn("Parameter " + s + " is inproper");
      }
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

  @Override
  public void showUi(boolean val) throws Exception {
    initializeLogger(); // redirect log only for UI
    view.setVisible(val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpPlugin#about()
   */
  @Override
  public String about() {
    return "Format conversion Plugin.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }

  @Override
  protected void runPlugin() throws QuimpPluginException {
    // TODO Auto-generated method stub

  }

}
