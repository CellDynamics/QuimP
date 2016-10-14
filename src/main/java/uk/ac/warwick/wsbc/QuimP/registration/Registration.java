package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * @author p.baniukiewicz
 *
 */
public class Registration extends JDialog implements ActionListener {

    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Registration.class.getName());

    private JButton bOk, bCancel;
    private boolean waited = false; // flag that indicates that user has waited already.
    private JTextField tEmail, tKey;
    private Window owner;

    private JPopupMenu popup;
    private JEditorPane helpArea;
    /**
     * 
     */
    private static final long serialVersionUID = -3889439366816085913L;

    /**
     * Create and display registration window.
     * 
     * @param owner
     * @param title
     */
    public Registration(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        buildMenu();
        buildWindow();

    }

    /**
     * Build popup menu.
     */
    private void buildMenu() {
        popup = new JPopupMenu();
        JMenuItem selectall = new JMenuItem("Select All"); // changing the name must follow with
                                                           // actionPerformed
        JMenuItem copy = new JMenuItem("Copy"); // changing the name must follow with
                                                // actionPerformed
        selectall.addActionListener(this);
        copy.addActionListener(this);
        popup.add(selectall);
        popup.add(copy);

    }

    /**
     * Build window.
     */
    private void buildWindow() {
        JPanel wndpanel = new JPanel();
        wndpanel.setLayout(new BorderLayout());

        // ok, cancel row
        JPanel caButtons = new JPanel();
        caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        bOk = new JButton("Apply");
        bOk.addActionListener(this);
        bCancel = new JButton("Cancel");
        bCancel.addActionListener(this);
        caButtons.add(bOk);
        caButtons.add(bCancel);
        wndpanel.add(caButtons, BorderLayout.SOUTH);
        // registration area and key and email
        JPanel centerarea = new JPanel();
        centerarea.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        wndpanel.add(centerarea, BorderLayout.CENTER);
        // html info
        try {
            helpArea = new JEditorPane(getClass().getResource("reg.html").toURI().toURL());

            helpArea.setContentType("text/html");
            helpArea.setEditable(false);
            JScrollPane helpPanel = new JScrollPane(helpArea);
            helpPanel.setPreferredSize(new Dimension(400, 500));
            c.gridx = 0;
            c.gridy = 0;
            centerarea.add(helpPanel, c);
            // add mouse listeners
            helpArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            // add link listener
            helpArea.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception e1) {
                            LOGGER.error("Can not start browser: " + e1.getMessage());
                            LOGGER.debug(e1.getMessage(), e1);
                        }
                    }
                }
            });
        } catch (IOException | URISyntaxException e2) {
            LOGGER.error("Can not read resource registration page: " + e2.getMessage());
            LOGGER.debug(e2.getMessage(), e2);
        }
        // email and key fields
        JPanel regarea = new JPanel();
        // regarea.setBackground(Color.YELLOW);
        regarea.setLayout(new GridLayout(3, 2));
        ((GridLayout) regarea.getLayout()).setHgap(20);
        ((GridLayout) regarea.getLayout()).setVgap(2);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        centerarea.add(regarea, c);
        tEmail = new JTextField(16);
        tKey = new JTextField(16);
        regarea.add(new JLabel(""));
        regarea.add(new JLabel(""));
        regarea.add(tEmail);
        regarea.add(new JLabel("Registration email"));
        regarea.add(tKey);
        regarea.add(new JLabel("Your key"));

        add(wndpanel);
        if (owner != null)
            setLocation(owner.getLocation());
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // clicked Cancel and user has not not waited yet
        if (e.getSource() == bCancel && waited == false) {
            bOk.setEnabled(false);
            bCancel.setEnabled(false);
            Worker w = new Worker(5);
            w.execute();
        }
        // Cancel and waited already - quit
        if (e.getSource() == bCancel && waited == true) {
            dispose();
        }
        // apply - get code
        if (e.getSource() == bOk) {
            boolean ret = validateRegInfo(tEmail.getText(), tKey.getText());
            if (ret) {
                waited = true;
                registerUser();
            } else
                JOptionPane.showMessageDialog(this,
                        "The key you provided does not match to the email.", "Error",
                        JOptionPane.WARNING_MESSAGE);
        }
        // support for action events
        switch (e.getActionCommand()) {
            case "Copy":
                helpArea.copy();
                break;
            case "Select All":
                helpArea.selectAll();
                break;
        }

    }

    /**
     * Add registration info to the IJ configuration.
     */
    private void registerUser() {
        // TODO Auto-generated method stub

    }

    /**
     * Validate whether key matches to email.
     * 
     * @param email
     * @param key
     * @return <tt>true</tt> is key matches to email.
     */
    private boolean validateRegInfo(final String email, final String key) {
        if (email == null || key == null)
            return false;
        String emails = email.toLowerCase();
        String keys = key.toLowerCase();
        // remove all white chars
        emails = emails.replaceAll("\\s+", "");
        keys = keys.replaceAll("\\s+", "");
        if (email.isEmpty() || key.isEmpty())
            return false;

        String salt = "secret"; // :)
        // add secret word
        emails = emails + salt;
        // compute hash
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(emails.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) { // convert to char
                sb.append(Integer.toHexString(b & 0xff));
            }
            String digest = sb.toString().toLowerCase();
            LOGGER.trace("email: " + emails + " Digest: " + digest);
            if (digest.equals(keys)) // compare both
                return true;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Do counting and waiting in background updating UI in the same time.
     * @author p.baniukiewicz
     * @see http://stackoverflow.com/questions/11817688/how-to-update-swing-gui-from-inside-a-long-method
     */
    class Worker extends SwingWorker<String, String> {
        private int wait;
        private Dimension dc;

        public Worker(int wait) {
            this.wait = wait;
        }

        @Override
        protected String doInBackground() throws Exception {
            dc = bCancel.getSize(); // remember size of button
            // This is what's called in the .execute method
            for (int i = 0; i < wait; i++) {
                // This sends the results to the .process method
                publish(String.valueOf(wait - i));
                Thread.sleep(1000);
            }
            // at the end of job reenable everything
            waited = true; // set flag on the end of wait
            bOk.setEnabled(true);
            bCancel.setEnabled(true);
            bCancel.setText("Cancel");
            return null;
        }

        protected void process(List<String> item) {
            // This updates the UI
            bCancel.setText(item.get(0));
            bCancel.setPreferredSize(dc); // keep size as original
        }
    }

    /**
     * Support for popupmenu.
     * 
     * @author p.baniukiewicz
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

}
