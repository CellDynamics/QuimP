package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
        buildWindow();
        setVisible(true);
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

        try {
            helpArea = new JEditorPane(getClass().getResource("reg.html").toURI().toURL());

            helpArea.setContentType("text/html");
            helpArea.setEditable(false);
            JScrollPane helpPanel = new JScrollPane(helpArea);
            helpPanel.setPreferredSize(new Dimension(400, 500));
            wndpanel.add(helpPanel, BorderLayout.CENTER);
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
        } // default size of text area
        add(wndpanel);
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

    }

    /**
     * 
     * @author p.baniukiewicz
     * @see http://stackoverflow.com/questions/11817688/how-to-update-swing-gui-from-inside-a-long-method
     */
    public class Worker extends SwingWorker<String, String> {
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

}
