package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.Prefs;
import uk.ac.warwick.wsbc.QuimP.QuimP;

// TODO: Auto-generated Javadoc
/*
 * !>
 * @startuml doc-files/Registration_1_UML.png
 * actor User
 * activate Module
 * Module-->Registration : <<create>>
 * activate Registration
 * Registration->Registration : check if registered
 * Registration->UI : not registered, show UI
 * activate UI
 * == Cancelled ==
 * User -> UI : Cancel
 * UI->UI : wait
 * ... Wait ...
 * UI-->Registration
 * deactivate UI
 * Registration-->Module
 * destroy Registration
 * ... Continue with module...
 * Module->Module : Run()
 * deactivate Module
 * @enduml
 * 
 * @startuml doc-files/Registration_2_UML.png
 * actor User
 * activate Module
 * Module-->Registration : <<create>>
 * activate Registration
 * Registration->Registration : check if registered
 * Registration->UI : not registered, show UI
 * activate UI
 * == Apply ==
 * User -> UI : Apply
 * UI->UI : compare hashes
 * UI-->Registration
 * deactivate UI
 * Registration-->Module
 * destroy Registration
 * ... Continue with module...
 * Module->Module : Run()
 * deactivate Module
 * @enduml
 * 
 * @startuml doc-files/Registration_3_UML.png
 * start
 * :read reg details;
 * note right
 * From IJ_Prefs.txt file
 * end note
 * :compute hash from email;
 * note left
 * Validate whether hash and email
 * from prefs matches
 * end note
 * if(hash the same as in IJ_Prefs?) then (no)
 * 
 * :show registration UI;
 * if(Button) then (cancel)
 * :wait;
 * else (apply)
 * :Check registration;
 * if(registration correct) then (yes)
 * :Store in IJ_Prefs.txt;
 * else (no)
 * start
 * endif
 * endif
 * endif
 * stop
 * @enduml
 * 
 * @startuml doc-files/Registration_4_UML.png
 * [*] --> DisplayUI
 * DisplayUI : Cancel
 * DisplayUI : Apply
 * 
 * DisplayUI->Wait : Cancel
 * Wait -> [*]
 * 
 * DisplayUI->Register : Apply
 * Register : compute hashes\nand compare
 * Register-> DisplayUI : hash not valid
 * Register ->[*] : hash valid
 * @enduml
 * 
 * !<
 */
/**
 * Support registration checking.
 * 
 * Registration component is self running. It should be located at very beginning of Module code. It
 * blocks Module from execution until driving from Registration is returned to it.
 * 
 * Registration process follows the scheme for cancel action: <br>
 * <img src="doc-files/Registration_1_UML.png"/><br>
 * And for register action: <br>
 * <img src="doc-files/Registration_2_UML.png"/><br>
 * Tests on first run: <br>
 * <img src="doc-files/Registration_3_UML.png"/><br>
 * UI transactions: <br>
 * <img src="doc-files/Registration_4_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 *
 */
public class Registration extends JDialog implements ActionListener {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Registration.class.getName());

    private JButton bOk, bCancel;
    /**
     * Flag that indicates that user has waited already.
     */
    public boolean waited = false;
    /**
     * How long to wait in sec.
     */
    private final int secondsToWait = 5;
    private JTextField tEmail, tKey;
    private Window owner;

    private JPopupMenu popup;
    private JEditorPane helpArea;

    private static final long serialVersionUID = -3889439366816085913L;

    /**
     * Create and display registration window if necessary.
     * 
     * @param owner
     * @param title
     */
    public Registration(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        // display reg window if not registered
        if (checkRegistration() == false) {
            build(title, true);
        }
    }

    /**
     * Construct object but does not display window.
     * 
     * Allow to set some parameters before window construction.
     * 
     * @param owner
     */
    public Registration(Window owner) {
        this.owner = owner;
    }

    /**
     * Construct and display window.
     * 
     * @param title
     * @param show indicate whether show window on screen
     */
    public void build(String title, boolean show) {
        buildMenu();
        buildWindow();
        setVisible(show);
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
     * Build main window.
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
        centerarea.setLayout(new BorderLayout());
        wndpanel.add(centerarea, BorderLayout.CENTER);
        // html info
        try {
            helpArea = new JEditorPane(getClass().getResource("reg.html").toURI().toURL());

            helpArea.setContentType("text/html");
            helpArea.setEditable(false);
            JScrollPane helpPanel = new JScrollPane(helpArea);
            helpPanel.setPreferredSize(new Dimension(500, 600));
            centerarea.add(helpPanel, BorderLayout.CENTER);
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
        ((GridLayout) regarea.getLayout()).setHgap(10);
        ((GridLayout) regarea.getLayout()).setVgap(2);
        centerarea.add(regarea, BorderLayout.SOUTH);
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
        setAlwaysOnTop(true);
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // clicked Cancel and user has not not waited yet
        if (e.getSource() == bCancel && waited == false) {
            bOk.setEnabled(false);
            bCancel.setEnabled(false);
            Worker w = new Worker(secondsToWait); // will change button text
            w.execute();
        }
        // Cancel and waited already - quit
        if (e.getSource() == bCancel && waited == true) {
            dispose();
        }
        // apply - get code, say thank you and exit
        if (e.getSource() == bOk) {
            boolean ret = validateRegInfo(tEmail.getText(), tKey.getText());
            if (ret) {
                waited = true;
                registerUser(tEmail.getText(), tKey.getText());
                JOptionPane.showMessageDialog(this, "Thank you for registering our product.", "OK!",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else // not ok - message and do nothing
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
     * 
     * @param email
     * @param key
     */
    private void registerUser(final String email, final String key) {
        Prefs.set("registration" + QuimP.QUIMP_PREFS_SUFFIX + ".mail", email);
        Prefs.set("registration" + QuimP.QUIMP_PREFS_SUFFIX + ".key", key);

    }

    /**
     * Read info from IJ container
     * 
     * @return Array of [0] reg email, [1] key
     */
    public String[] readRegInfo() {
        String[] ret = new String[2];
        ret[0] = Prefs.get("registration" + QuimP.QUIMP_PREFS_SUFFIX + ".mail", "");
        ret[1] = Prefs.get("registration" + QuimP.QUIMP_PREFS_SUFFIX + ".key", "");
        return ret;
    }

    /**
     * Read registration details from IJ and fills the registration window.
     */
    public void fillRegForm() {
        String[] reg = readRegInfo();
        tEmail.setText(reg[0]);
        tKey.setText(reg[1]);
    }

    /**
     * Check if user is registered.
     * 
     * @return True if user registration info is available in IJ_Prefs.txt and data are valid.
     */
    private boolean checkRegistration() {
        String[] reginfo = readRegInfo();
        boolean ret = validateRegInfo(reginfo[0], reginfo[1]);
        return ret;
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
                // dealing with conversion to int from byte and leading 0 for values smaller than 16
                String ss = Integer.toHexString(0x100 | (b & 0xff)).substring(1);
                sb.append(ss);
            }
            String digest = sb.toString().toLowerCase();
            LOGGER.trace("email: " + email + " Digest: " + digest);
            if (digest.equals(keys)) // compare both
                return true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Do counting and waiting in background updating UI in the same time.
     * 
     * @author p.baniukiewicz
     * @see <a href=
     *      "link">http://stackoverflow.com/questions/11817688/how-to-update-swing-gui-from-inside-a-long-method</a>
     */
    class Worker extends SwingWorker<String, String> {
        private int wait;
        private Dimension dc;

        /**
         * Instantiates a new worker.
         *
         * @param wait the wait
         */
        public Worker(int wait) {
            this.wait = wait;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
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

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#process(java.util.List)
         */
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
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        /* (non-Javadoc)
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

}
