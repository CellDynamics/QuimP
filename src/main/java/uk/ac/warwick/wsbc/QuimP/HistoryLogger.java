/**
 * @file HistoryLogger.java
 * @date 24 Mar 2016
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

/**
 * @author p.baniukiewicz
 * @date 24 Mar 2016
 *
 */
public class HistoryLogger implements WindowListener {

    private Frame historyWnd;
    private ArrayList<String> history;
    private TextArea info;

    /**
     * 
     */
    public HistoryLogger() {
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

        history = new ArrayList<>();

    }

    public void openHistory() {
        historyWnd.setVisible(true);
    }

    public void addEntry(String m) {
        if (historyWnd.isVisible()) {
            history.add(m);
            info.append(m + '\n');
        }

    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(WindowEvent e) {
        historyWnd.setVisible(false);
        history.clear();
        historyWnd.dispose();

    }

    @Override
    public void windowClosed(WindowEvent e) {
        historyWnd.setVisible(false);
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