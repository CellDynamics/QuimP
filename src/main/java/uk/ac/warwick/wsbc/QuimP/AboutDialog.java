/**
 * @file AboutDialog.java
 * @date 22 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Build About dialog with support of mouse operations
 * 
 * @author p.baniukiewicz
 * @date 22 Apr 2016
 *
 */
public class AboutDialog implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(AboutDialog.class.getName());
    public JDialog aboutWnd; //!< About window
    private JTextArea info; //!< text area field
    private JPopupMenu popup; //!< popup menu
    private JMenuBar mbar; //!< the same but in menu bar
    private final int ROWS = 30; //!< Number of rows in window
    private final int COLS = 60; //!< Number of columns in window
    private final String c = "-"; //!< Limiter char
    
    /**
     * Main constructor
     * 
     * Builds window and menus
     * 
     * @param owner Owner of Dialog
     */
    public AboutDialog(Window owner) {
        aboutWnd = new JDialog(owner, "Info", JDialog.ModalityType.DOCUMENT_MODAL);
        aboutWnd.addWindowListener(new myWindowAdapter());
        // located in middle of quimp qindow
        Rectangle orgBounds = owner.getBounds();
        aboutWnd.setBounds(orgBounds.x + orgBounds.width / 2, orgBounds.y + orgBounds.height / 2,
                500, 300);
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 1)); // main window panel
        JPanel tp = new JPanel(); // panel with text area
        tp.setLayout(new GridLayout(1, 1));
        info = new JTextArea(ROWS, COLS); // area to write
        info.setBackground(Color.WHITE);
        info.setEditable(false);
        tp.add(info); // add to panel
        JScrollPane infoPanel = new JScrollPane(tp);
        p.add(infoPanel);
        aboutWnd.add(p);
        aboutWnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // build menus
        buildMenu();
        aboutWnd.setJMenuBar(mbar);
        // add actions for mouse click
        MouseListener popupListener = new PopupListener();
        aboutWnd.addMouseListener(popupListener);
        info.addMouseListener(popupListener);
        aboutWnd.pack();
    }

    /**
     * Create simple menu bar with entries and popup
     * Fills private fields of this class
     */
    private void buildMenu() {
        mbar = new JMenuBar();
        popup = new JPopupMenu();
        JMenu medit = new JMenu("Edit");
        JMenuItem selectall = new JMenuItem("Select All"); // changing the name must follow with
                                                           // actionPerformed
        JMenuItem copy = new JMenuItem("Copy"); // changing the name must follow with
                                                // actionPerformed
        selectall.addActionListener(this);
        copy.addActionListener(this);
        mbar.add(medit);
        popup.add(selectall);
        popup.add(copy);
        medit.add(copyMenuItem(selectall)); // add copy of MenuItems to MenuBar
        medit.add(copyMenuItem(copy));

        selectall.addActionListener(this);
        copy.addActionListener(this);
    }

    /**
     * Make copy of MenuItem
     * 
     * @remarks Components can not be shared among containers. This method copies basic properties
     * of component to its new instance (shallow copy of selected properties)
     * @param src source MenuItem
     * @return Copy of \c src MenuItem
     */
    private JMenuItem copyMenuItem(JMenuItem src) {
        JMenuItem dst = new JMenuItem();
        dst.setText(src.getText());
        dst.setToolTipText(src.getToolTipText());
        dst.setMnemonic(src.getMnemonic());
        for (ActionListener a : src.getActionListeners())
            dst.addActionListener(a);
        return dst;
    }

    /**
     * Add line of text to window TextArea
     * 
     * @param t Text to add in subsequent lines
     */
    public void appendLine(final String t) {
        info.append(t + "\n");
    }

    /**
     * Add line of length of window
     */
    public void appendDistance() {
        String line;
        line = new String(new char[COLS]).replace("\0", c);
        appendLine(line);
    }

    /**
     * Show or hide main window
     * 
     * @param state \c true to show window
     * @warning When window is visible append(final String) does not work
     */
    public void setVisible(boolean state) {
        info.setCaretPosition(0); // causes that initially view is scrolled to up
        aboutWnd.setVisible(state);
    }

    /**
     * Destroy window on exit
     * 
     * @author p.baniukiewicz
     * @date 22 Apr 2016
     *
     */
    class myWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent we) {
            LOGGER.trace("windowClosing");
            aboutWnd.dispose();
        }
    }

    /**
     * Support for popupmenu
     * 
     * @author p.baniukiewicz
     * @date 22 Apr 2016
     *
     */
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * Menu actions support
     * @param e event - currently method uses names of events (names of menus)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Copy":
                info.copy();
                break;
            case "Select All":
                info.selectAll();
                break;
        }

    }
}
