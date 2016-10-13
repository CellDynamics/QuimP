package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * @author p.baniukiewicz
 *
 */
public class Registration extends JDialog implements ActionListener {

    private JButton bOk, bCancel;
    private boolean waited = false; // flag that indicates that user has waited already.
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
