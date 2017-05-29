package com.github.celldynamics.quimp;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

// TODO: Auto-generated Javadoc
/**
 * Builds history logger window and logs.
 * 
 * <p>Logs are supposed to be JSon objects that hold current BOA state. Logger is updated only when
 * window is visible. Closing and then opening window causes erasing its content. Method
 * addEntry(String, SnakePluginList) should be used after every activity in QuimP, where first
 * parameter is description of this activity and next parameters define QuimP state.
 * 
 * @author p.baniukiewicz
 *
 */
public class HistoryLogger implements WindowListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryLogger.class.getName());
  private Frame historyWnd; //!< Window handler
  private ArrayList<String> history; //!< array with all entries
  private TextArea info;
  private int id; //!< message counter

  /**
   * Construct main window.
   */
  public HistoryLogger() {
    id = 1;
    historyWnd = new Frame("History");
    Panel p = new Panel();
    p.setLayout(new GridLayout(1, 1)); // main window panel
    Panel tp = new Panel(); // panel with text area
    tp.setLayout(new GridLayout(1, 1));
    info = new TextArea(10, 60); // area to write
    info.setEditable(false);
    info.setBackground(Color.WHITE);
    tp.add(info); // add to panel

    JScrollPane infoPanel = new JScrollPane(tp);
    p.add(infoPanel);
    historyWnd.add(p);
    historyWnd.pack();
    historyWnd.addWindowListener(this);

    history = new ArrayList<String>();

  }

  /**
   * Make window visible.
   */
  public void openHistory() {
    historyWnd.setVisible(true);
  }

  /**
   * Close window and call windowClosing() and windowClosed() methods.
   */
  public void closeHistory() {
    historyWnd.setVisible(false);
  }

  /**
   * Add entry to log.
   * 
   * <p>Gather all BOA state and include in log. Uses \c Entry class to pack these information to
   * JSon object. Particular entries can be null if they may not be logged
   * 
   * @param m General message to be included in log
   * @param bs BOA state machine object
   */
  public void addEntry(String m, BOAState bs) {
    // TODO This method should accept more detailed BOA state (e.g. all segm. params)
    if (historyWnd.isVisible()) {
      if (bs == null) {
        return;
      }
      LogEntry en = new LogEntry(id++, m, bs);
      Serializer<LogEntry> s = new Serializer<>(en, QuimP.TOOL_VERSION);

      String jsontmp = s.toString();
      history.add(jsontmp); // store in array
      info.append(jsontmp + '\n'); // add to log window
      LOGGER.debug(jsontmp);
      en = null;
    }

  }

  /**
   * Check if window is opened.
   * 
   * @return true is window is visible
   */
  public boolean isOpened() {
    return historyWnd.isVisible();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
   */
  @Override
  public void windowOpened(WindowEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosing(WindowEvent e) {
    LOGGER.debug("History windowClosing");
    historyWnd.setVisible(false);
    info.setText("");
    id = 1;
    history.clear();
    historyWnd.dispose();

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosed(WindowEvent e) {
    LOGGER.debug("History windowClosed");
    historyWnd.setVisible(false);
    info.setText("");
    id = 1;
    history.clear();
    historyWnd.dispose();

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
   */
  @Override
  public void windowIconified(WindowEvent e) {

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
   */
  @Override
  public void windowDeiconified(WindowEvent e) {

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
   */
  @Override
  public void windowActivated(WindowEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
   */
  @Override
  public void windowDeactivated(WindowEvent e) {
  }

}

/**
 * Serialization class. Holds all data that should be included in log
 * 
 * @author p.baniukiewicz
 *
 */
class LogEntry implements IQuimpSerialize {
  public int id; //!< Number of entry
  public String action; //!< Textual description of taken action
  // selected fields to be logged (from BOAState)
  public int frame; //!< current frame, CustomStackWindow.updateSliceSelector()
  public BOAState.SegParam segParam; //!< Reference to segmentation parameters
  public String fileName; //!< Current data file name
  public SnakePluginList snakePluginList; //!< Plugin config

  /**
   * Main constructor.
   * 
   * <p>Object of this class is created temporarily only for logging purposes.
   * 
   * @param counter number of log entry
   * @param action description of action
   * @param bs BOA state machine
   */
  public LogEntry(int counter, String action, BOAState bs) {
    // TODO replace with snakePluginLists (beforeSerialize will not be called then)
    super();
    this.id = counter;
    this.action = action;
    this.frame = bs.boap.frame;
    this.segParam = bs.segParam;
    this.fileName = bs.boap.getFileName();
    this.snakePluginList = bs.snakePluginList;

  }

  @Override
  public void beforeSerialize() {
    snakePluginList.beforeSerialize();
  }

  @Override
  public void afterSerialize() throws Exception {
  }
}