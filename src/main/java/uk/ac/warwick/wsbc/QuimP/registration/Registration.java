package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author p.baniukiewicz
 *
 */
public class Registration extends JDialog implements ActionListener, ChangeListener {

    private JButton bOk, bCancel;
    private JLabel lWait;
    private boolean waited = false;
    /**
     * 
     */
    private static final long serialVersionUID = -3889439366816085913L;

    /**
     * @param owner
     * @param title
     * @param modalityType
     */
    public Registration(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
        buildWindow();
        setVisible(true);
    }

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
        bCancel.addChangeListener(this);
        caButtons.add(bOk);
        caButtons.add(bCancel);
        wndpanel.add(caButtons, BorderLayout.SOUTH);

        lWait = new JLabel(" ");
        wndpanel.add(lWait, BorderLayout.NORTH);

        add(wndpanel);
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bCancel && waited == false) {
            bOk.setEnabled(false);
            bCancel.setEnabled(false);
            Worker w = new Worker(5);
            w.execute();
        }
        if (e.getSource() == bCancel && waited == true) {
            dispose();
        }

    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        if (arg0.getSource() == bCancel) {
        }

    }

    /**
     * 
     * @author p.baniukiewicz
     * @see http://stackoverflow.com/questions/11817688/how-to-update-swing-gui-from-inside-a-long-method
     */
    public class Worker extends SwingWorker<String, String> {
        private int wait;

        public Worker(int wait) {
            this.wait = wait;
        }

        @Override
        protected String doInBackground() throws Exception {
            // This is what's called in the .execute method
            for (int i = 0; i < wait; i++) {
                // This sends the results to the .process method
                publish(String.valueOf(wait - i));
                Thread.sleep(1000);
            }
            waited = true;
            bOk.setEnabled(true);
            bCancel.setEnabled(true);
            return null;
        }

        protected void process(List<String> item) {
            // This updates the UI
            lWait.setText(item.get(0));
        }
    }

}
