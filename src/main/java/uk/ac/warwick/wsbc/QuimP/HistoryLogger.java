/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

/**
 * Builds history logger window and logs
 * 
 * Logs are supposed to be JSon objects that hold current BOA state.
 * Logger is updated only when window is visible. Closing and then opening window causes erasing 
 * its content.
 * Method addEntry(String, SnakePluginList) should be used after every activity in QuimP, where
 * first parameter is description of this activity and next parameters define QuimP state.
 *  
 * @author p.baniukiewicz
 *
 */
public class HistoryLogger implements WindowListener {

    private static final Logger LOGGER = LogManager.getLogger(HistoryLogger.class.getName());
    private Frame historyWnd; //!< Window handler
    private ArrayList<String> history; //!< array with all entries
    private TextArea info;
    private int id; //!< message counter

    /**
     * Construct main window
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
     * Make window visible
     */
    public void openHistory() {
        historyWnd.setVisible(true);
    }

    /**
     * Close window and call windowClosing() and windowClosed() methods
     */
    public void closeHistory() {
        historyWnd.setVisible(false);
    }

    /**
     * Add entry to log.
     * 
     * Gather all BOA state and include in log. Uses \c Entry class to pack these information to
     * JSon object. 
     * Particular entries can be null if they may not be logged
     *  
     * @param m General message to be included in log
     * @param bs BOA state machine object 
     * @todo TODO This method should accept more detailed BOA state (e.g. all segm. params)
     */
    public void addEntry(String m, BOAState bs) {
        if (historyWnd.isVisible()) {
            if (bs == null)
                return;
            LogEntry en = new LogEntry(id++, m, bs);
            Serializer<LogEntry> s = new Serializer<>(en, BOA_.quimpInfo);

            String jsontmp = s.toString();
            history.add(jsontmp); // store in array
            info.append(jsontmp + '\n'); // add to log window
            LOGGER.debug(jsontmp);
            en = null;
        }

    }

    /**
     * Check if window is opened
     * 
     * @return \c true is window is visible
     */
    public boolean isOpened() {
        return historyWnd.isVisible();
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        LOGGER.debug("History windowClosing");
        historyWnd.setVisible(false);
        info.setText("");
        id = 1;
        history.clear();
        historyWnd.dispose();

    }

    @Override
    public void windowClosed(WindowEvent e) {
        LOGGER.debug("History windowClosed");
        historyWnd.setVisible(false);
        info.setText("");
        id = 1;
        history.clear();
        historyWnd.dispose();

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

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
     * Main constructor
     * 
     * Object of this class is created temporarily only for logging purposes.
     * 
     * @param counter number of log entry
     * @param action description of action
     * @param bs BOA state machine
     * @todo TODO replace with snakePluginLists (beforeSerialize will not be called then)
     */
    public LogEntry(int counter, String action, BOAState bs) {
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