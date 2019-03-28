package com.github.celldynamics.quimp.omero;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmeroLoginDialog extends JDialog {
  static final Logger LOGGER = LoggerFactory.getLogger(OmeroLoginDialog.class.getName());

  private final JPanel contentPanel = new JPanel();
  private JTextField tfHost;
  private JTextField tfUser;
  private JPasswordField tfPass;
  private final Action testAction = new SwingAction();
  private OmeroClient_ omc;
  private final Action downloadAction = new DownloadAction();
  private final Action uploadAction = new UploadAction();
  private JTabbedPane tabbedPane;
  private JPanel panelDownload;
  private JPanel panelUpload;
  private final Action storeAction = new StoreAction();
  private JTextField tfPort;
  private JCheckBox chckbxStoreCred;

  /**
   * Editable fields in config pane.
   * 
   * @author p.baniukiewicz
   *
   */
  private enum Fields {
    HOST, USER, PASS, PORT
  }

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      OmeroLoginDialog dialog = new OmeroLoginDialog(null);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Set {@link OmeroClientApi} login fields.
   * 
   * @param obj component
   * @param name name
   */
  private void readTextField(Object obj, Fields name) {
    LOGGER.debug("Reading: " + name);
    if (obj instanceof JTextField) {
      String text = ((JTextField) obj).getText();
      switch (name) {
        case HOST:
          omc.setHost(text);
          break;
        case USER:
          omc.setUser(text);
          break;
        case PASS:
          omc.setPass(text);
          break;
        case PORT:
          omc.setPort(text);
          break;
        default:
          throw new IllegalArgumentException("Wrong input type.");
      }
    } else {
      throw new IllegalArgumentException("Wrong input type.");
    }

  }

  /**
   * Create the dialog and update fields.
   */
  public OmeroLoginDialog(OmeroClient_ omc) {
    this.omc = omc;
    setTitle("Omero login");
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 450, 443);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
    {
      tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      contentPanel.add(tabbedPane);
      {
        JPanel panelSetup = new JPanel();
        panelSetup.setBorder(new EmptyBorder(4, 4, 4, 4));
        tabbedPane.addTab("Setup", null, panelSetup, "Setup connection");
        tabbedPane.setEnabledAt(0, true);
        GridBagLayout gbl_panelSetup = new GridBagLayout();
        gbl_panelSetup.columnWidths = new int[] { 417 };
        gbl_panelSetup.rowHeights = new int[] { 26, 26, 26, 26, 26, 26, 26 };
        gbl_panelSetup.columnWeights = new double[] { 1.0 };
        gbl_panelSetup.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 };
        panelSetup.setLayout(gbl_panelSetup);
        {
          JPanel panel_1 = new JPanel();
          GridBagConstraints gbc_panel_1 = new GridBagConstraints();
          gbc_panel_1.fill = GridBagConstraints.BOTH;
          gbc_panel_1.insets = new Insets(0, 0, 5, 0);
          gbc_panel_1.gridx = 0;
          gbc_panel_1.gridy = 0;
          panelSetup.add(panel_1, gbc_panel_1);
          panel_1.setLayout(new GridLayout(0, 2, 0, 0));
          {
            JLabel lblNewLabel = new JLabel("Host address");
            panel_1.add(lblNewLabel);
          }
          {
            tfHost = new JTextField();
            tfHost.setText(omc.getHost());
            tfHost.setHorizontalAlignment(SwingConstants.RIGHT);
            tfHost.addFocusListener(new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent arg0) {
                readTextField(arg0.getSource(), Fields.HOST);
              }
            });
            tfHost.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent arg0) {
                readTextField(arg0.getSource(), Fields.HOST);
              }
            });
            tfHost.setToolTipText("Host name");
            panel_1.add(tfHost);
            tfHost.setColumns(10);
          }
        }
        {
          JPanel panel_1 = new JPanel();
          GridBagConstraints gbc_panel_1 = new GridBagConstraints();
          gbc_panel_1.fill = GridBagConstraints.BOTH;
          gbc_panel_1.insets = new Insets(0, 0, 5, 0);
          gbc_panel_1.gridx = 0;
          gbc_panel_1.gridy = 1;
          panelSetup.add(panel_1, gbc_panel_1);
          panel_1.setLayout(new GridLayout(0, 2, 0, 0));
          {
            JLabel lblNewLabel_1 = new JLabel("User name");
            panel_1.add(lblNewLabel_1);
          }
          {
            tfUser = new JTextField();
            tfUser.setText(omc.getUser());
            tfUser.setHorizontalAlignment(SwingConstants.RIGHT);
            tfUser.addFocusListener(new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent e) {
                readTextField(e.getSource(), Fields.USER);
              }
            });
            tfUser.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent arg0) {
                readTextField(arg0.getSource(), Fields.USER);
              }
            });
            tfUser.setToolTipText("User name");
            panel_1.add(tfUser);
            tfUser.setColumns(10);
          }
        }
        {
          JPanel panel_1 = new JPanel();
          GridBagConstraints gbc_panel_1 = new GridBagConstraints();
          gbc_panel_1.fill = GridBagConstraints.BOTH;
          gbc_panel_1.insets = new Insets(0, 0, 5, 0);
          gbc_panel_1.gridx = 0;
          gbc_panel_1.gridy = 2;
          panelSetup.add(panel_1, gbc_panel_1);
          panel_1.setLayout(new GridLayout(0, 2, 0, 0));
          {
            JLabel lblNewLabel_2 = new JLabel("Password");
            panel_1.add(lblNewLabel_2);
          }
          {
            tfPass = new JPasswordField();
            tfPass.setHorizontalAlignment(SwingConstants.RIGHT);
            tfPass.addFocusListener(new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent e) {
                readTextField(e.getSource(), Fields.PASS);
              }
            });
            tfPass.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                readTextField(e.getSource(), Fields.PASS);
              }
            });
            tfPass.setToolTipText("Password");
            tfPass.setText(omc.getPass());
            panel_1.add(tfPass);
            tfPass.setColumns(10);
          }
        }
        {
          JPanel panel = new JPanel();
          GridBagConstraints gbc_panel = new GridBagConstraints();
          gbc_panel.insets = new Insets(0, 0, 5, 0);
          gbc_panel.fill = GridBagConstraints.BOTH;
          gbc_panel.gridx = 0;
          gbc_panel.gridy = 3;
          panelSetup.add(panel, gbc_panel);
          panel.setLayout(new GridLayout(0, 2, 0, 0));
          {
            JLabel lblNewLabel_3 = new JLabel("Port");
            panel.add(lblNewLabel_3);
          }
          {
            tfPort = new JTextField();
            tfPort.setText(omc.getPort());
            tfPort.setToolTipText("Port number");
            tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
            tfPort.addFocusListener(new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent e) {
                readTextField(e.getSource(), Fields.PORT);
              }
            });
            tfPort.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                readTextField(e.getSource(), Fields.PORT);
              }
            });
            panel.add(tfPort);
            tfPort.setColumns(10);
          }
        }
        {
          JPanel panel_1 = new JPanel();
          GridBagConstraints gbc_panel_1 = new GridBagConstraints();
          gbc_panel_1.fill = GridBagConstraints.BOTH;
          gbc_panel_1.insets = new Insets(0, 0, 5, 0);
          gbc_panel_1.gridx = 0;
          gbc_panel_1.gridy = 4;
          panelSetup.add(panel_1, gbc_panel_1);
          panel_1.setLayout(new GridLayout(0, 1, 0, 0));
          {
            chckbxStoreCred = new JCheckBox("Store credentials");
            chckbxStoreCred.setSelected(true);
            chckbxStoreCred.setAction(storeAction);
            chckbxStoreCred.setToolTipText(
                    "Store login details in ImageJ preference file. Password will be obfuscated.");
            panel_1.add(chckbxStoreCred);
          }
        }
        {
          JEditorPane tpHelp = new JEditorPane();
          tpHelp.setContentType("text/html");
          tpHelp.setText("<strong>Load Mask</strong>");
          tpHelp.setEditable(false);
          GridBagConstraints gbc_tpHelp = new GridBagConstraints();
          gbc_tpHelp.insets = new Insets(0, 0, 5, 0);
          gbc_tpHelp.fill = GridBagConstraints.BOTH;
          gbc_tpHelp.gridx = 0;
          gbc_tpHelp.gridy = 5;
          panelSetup.add(tpHelp, gbc_tpHelp);
        }
        {
          JPanel buttonPane = new JPanel();
          FlowLayout flowLayout = (FlowLayout) buttonPane.getLayout();
          flowLayout.setAlignment(FlowLayout.RIGHT);
          GridBagConstraints gbc_buttonPane = new GridBagConstraints();
          gbc_buttonPane.fill = GridBagConstraints.BOTH;
          gbc_buttonPane.gridx = 0;
          gbc_buttonPane.gridy = 6;
          panelSetup.add(buttonPane, gbc_buttonPane);
          {
            JButton btnDownload = new JButton("Download");
            buttonPane.add(btnDownload);
            btnDownload.setToolTipText("Download experiment to local folder");
            btnDownload.setAction(downloadAction);
          }
          {
            JButton btnUpload = new JButton("Upload");
            buttonPane.add(btnUpload);
            btnUpload.setToolTipText("Upload experiment to Omero");
            btnUpload.setAction(uploadAction);
          }
          {
            JButton btnNewButton = new JButton("Test");
            buttonPane.add(btnNewButton);
            btnNewButton.setToolTipText("Test connection");
            btnNewButton.setAction(testAction);
          }
        }
      }
      {
        panelDownload = new JPanel();
        tabbedPane.addTab("Download", null, panelDownload, "Download experiment settings");
        tabbedPane.setEnabledAt(1, true);
      }
      {
        panelUpload = new JPanel();
        tabbedPane.addTab("Upload", null, panelUpload, "Upload experiment settings");
        tabbedPane.setEnabledAt(2, true);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
  }

  private class SwingAction extends AbstractAction {
    public SwingAction() {
      putValue(NAME, "Test");
      putValue(SHORT_DESCRIPTION, "Test connection");
    }

    public void actionPerformed(ActionEvent e) {
    }
  }

  private class DownloadAction extends AbstractAction {
    public DownloadAction() {
      putValue(NAME, "Download");
      putValue(SHORT_DESCRIPTION, "Download experiment");
    }

    public void actionPerformed(ActionEvent e) {
      getTabbedPane().setSelectedComponent(getPanelDownload());
    }
  }

  private class UploadAction extends AbstractAction {
    public UploadAction() {
      putValue(NAME, "Upload");
      putValue(SHORT_DESCRIPTION, "Upload folder");
    }

    public void actionPerformed(ActionEvent e) {
      getTabbedPane().setSelectedComponent(getPanelUpload());
    }
  }

  protected JTabbedPane getTabbedPane() {
    return tabbedPane;
  }

  protected JPanel getPanelDownload() {
    return panelDownload;
  }

  protected JPanel getPanelUpload() {
    return panelUpload;
  }

  private class StoreAction extends AbstractAction {
    public StoreAction() {
      putValue(NAME, "Store credentials");
      putValue(SHORT_DESCRIPTION, "Store login details in ImageJ preference file");
    }

    public void actionPerformed(ActionEvent e) {
      if (e.getSource() instanceof JCheckBox) {
        JCheckBox obj = (JCheckBox) e.getSource();
        LOGGER.debug("Store: " + obj.isSelected());
        if (obj.isSelected()) { // upload content to IJ
          omc.uploadPrefs(false);
        } else { // erase content
          omc.uploadPrefs(true);
        }
      }
    }
  }

  protected JCheckBox getChckbxStoreCred() {
    return chckbxStoreCred;
  }
}
