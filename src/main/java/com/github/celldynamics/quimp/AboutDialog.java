package com.github.celldynamics.quimp;

import java.awt.Color;
import java.awt.Font;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build About dialog with support of mouse operations.
 * 
 * <p>It can be used as universal text displayer.
 * 
 * @author p.baniukiewicz
 *
 */
public class AboutDialog implements ActionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(AboutDialog.class.getName());
  /**
   * About window.
   */
  public JDialog aboutWnd;
  private JTextArea info; // text area field
  private JPopupMenu popup; // popup menu
  private JMenuBar mbar; // the same but in menu bar
  /**
   * Number of rows in window.
   */
  private int rows = 30;
  /**
   * Number of columns in window.
   */
  private int cols = 80;
  private final String limiter = "-"; // Limiter char

  /**
   * Build window and menus with given size.
   * 
   * @param owner Owner of Dialog.
   * @param rows Number of rows.
   * @param cols Number of columns.
   */
  public AboutDialog(Window owner, int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    buildWindow(owner);
  }

  /**
   * Main constructor.
   * 
   * <p>Builds window and menus.
   * 
   * @param owner Owner of Dialog
   */
  public AboutDialog(Window owner) {
    buildWindow(owner);
  }

  /**
   * Construct the window.
   * 
   * @param owner parent window
   */
  private void buildWindow(Window owner) {
    aboutWnd = new JDialog(owner, "Info", JDialog.ModalityType.DOCUMENT_MODAL);
    aboutWnd.addWindowListener(new MyWindowAdapter());
    // located in middle of quimp qindow
    Rectangle orgBounds = owner.getBounds();
    aboutWnd.setBounds(orgBounds.x + orgBounds.width / 2, orgBounds.y + orgBounds.height / 2, 600,
            400);
    JPanel p = new JPanel();
    p.setLayout(new GridLayout(1, 1)); // main window panel
    JPanel tp = new JPanel(); // panel with text area
    tp.setLayout(new GridLayout(1, 1));
    info = new JTextArea(rows, cols); // area to write
    info.setBackground(Color.WHITE);
    info.setEditable(false);
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    info.setFont(font);
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
   * Create simple menu bar with entries and popup.
   * 
   * <p>Fills private fields of this class.
   */
  private void buildMenu() {
    mbar = new JMenuBar();
    popup = new JPopupMenu();
    JMenu medit = new JMenu("Edit");
    JMenuItem selectall = new JMenuItem("Select All"); // name must follow actionPerformed
    JMenuItem copy = new JMenuItem("Copy"); // changing the name must follow actionPerformed
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
   * Make copy of MenuItem.
   * 
   * <p>Components can not be shared among containers. This method copies basic properties of
   * component to its new instance (shallow copy of selected properties)
   * 
   * @param src source MenuItem
   * @return Copy of \limiter src MenuItem
   */
  private JMenuItem copyMenuItem(JMenuItem src) {
    JMenuItem dst = new JMenuItem();
    dst.setText(src.getText());
    dst.setToolTipText(src.getToolTipText());
    dst.setMnemonic(src.getMnemonic());
    for (ActionListener a : src.getActionListeners()) {
      dst.addActionListener(a);
    }
    return dst;
  }

  /**
   * Add line of text to window TextArea.
   * 
   * @param t Text to add in subsequent lines.
   */
  public void appendLine(final String t) {
    info.append(t + "\n");
  }

  /**
   * Add line of length of window.
   */
  public void appendDistance() {
    String line;
    line = new String(new char[cols]).replace("\0", limiter);
    appendLine(line);
  }

  /**
   * Show or hide main window.
   * 
   * <p><b>Warning</b><br>
   * When window is visible append(final String) does not work.
   * 
   * @param state \limiter true to show window
   */
  public void setVisible(boolean state) {
    info.setCaretPosition(0); // causes that initially view is scrolled to up
    aboutWnd.setVisible(state);
  }

  /**
   * Destroy window on exit.
   * 
   * @author p.baniukiewicz
   *
   */
  class MyWindowAdapter extends WindowAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent we) {
      LOGGER.trace("windowClosing");
      aboutWnd.dispose();
    }
  }

  /**
   * Support for popupmenu.
   * 
   * @author p.baniukiewicz
   *
   */
  class PopupListener extends MouseAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
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
   * Menu actions support.
   * 
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
      default:
        break;
    }

  }
}
