package uk.ac.warwick.wsbc.QuimP.registration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author p.baniukiewicz
 *
 */
public class Registration extends JDialog implements ActionListener, ChangeListener {

    private JButton bOk, bCancel;
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

        add(wndpanel);
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bCancel) {
            delay(5);
            // dispose();
        }

    }

    /**
     * Wait <tt>i</tt> seconds before proceeding.
     * @param i
     */
    private void delay(int i) {
        int n = i;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            n--;
            bCancel.setText(Integer.toString(i - n) + " sec");
        } while (n >= 0);

    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        if (arg0.getSource() == bCancel) {
        }

    }

}
