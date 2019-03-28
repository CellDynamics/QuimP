package com.github.celldynamics.quimp.omero;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.io.DirectoryChooser;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/**
 * Dialog by WindowsBuilder.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class OmeroLoginDialog extends JDialog {
  static final Logger LOGGER = LoggerFactory.getLogger(OmeroLoginDialog.class.getName());

  private static final String DISCONNECTED = "<b>Disconnected</b>";
  private static final String CONNECTED = "<b>Connected</b>";

  private final JPanel contentPanel = new JPanel();
  private final ImageTable imageTableModel = new ImageTable();
  private JTextField tfHost;
  private JTextField tfUser;
  private JPasswordField tfPass;
  private final Action testAction = new TestAction();
  private OmeroClient_ omc;
  private final Action downloadAction = new DownloadAction();
  private final Action uploadAction = new UploadAction();
  private JTabbedPane tabbedPane;
  private JPanel panelDownload;
  private JPanel panelUpload;
  private final Action storeAction = new StoreAction();
  private JTextField tfPort;
  private JCheckBox chckbxStoreCred;
  private JTextPane tpInfo;
  private JPanel panelSetup;
  private JList<String> listDatasetsDownload;
  private JTable tableImagesDownload;
  private JTable tableImagesUpload;
  private JList<String> listDatasetsUpload;
  private final Action uploadExperimentAction = new UploadExperimentAction();
  private final Action downloadExperimentAction = new DownloadExperimentAction();

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
   * Set {@link OmeroBrowser} login fields.
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
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent arg0) {
        omc.disconnect();
      }
    });
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
      tabbedPane.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            if (pane.getSelectedComponent() == getPanelDownload()) {
              LOGGER.trace("Selected download pane");

            }
            if (pane.getSelectedComponent() == getPanelUpload()) {
              LOGGER.trace("Selected upload pane");

            }
            if (pane.getSelectedComponent() == getPanelSetup()) {
              LOGGER.trace("Selected setup pane");
            }
          }
        }
      });
      contentPanel.add(tabbedPane);
      {
        panelSetup = new JPanel();
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
        panelDownload.setLayout(new BorderLayout(2, 2));
        {
          JButton btnDownloadExp = new JButton("");
          btnDownloadExp.setAction(downloadExperimentAction);
          btnDownloadExp.setToolTipText("");
          panelDownload.add(btnDownloadExp, BorderLayout.SOUTH);
        }
        {
          JPanel panel = new JPanel();
          panelDownload.add(panel, BorderLayout.CENTER);
          panel.setLayout(new GridLayout(1, 2, 0, 2));
          {
            JSplitPane splitPane = new JSplitPane();
            panel.add(splitPane);
            splitPane.setContinuousLayout(true);
            splitPane.setResizeWeight(0.5);
            splitPane.setOneTouchExpandable(true);
            {
              listDatasetsDownload = new JList<String>();
              listDatasetsDownload.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                  if (!e.getValueIsAdjusting()) {
                    int index = listDatasetsDownload.getSelectedIndex();
                    if (index >= 0) {
                      String name = listDatasetsDownload.getModel().getElementAt(index);
                      LOGGER.trace("Selected: " + name);
                      omc.setCurrentDs(index);
                      imageTableModel.update(omc.getImages(index));
                    }
                  }
                }
              });
              listDatasetsDownload.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
              JScrollPane scrollPane = new JScrollPane(listDatasetsDownload);
              splitPane.setLeftComponent(scrollPane);
            }
            {
              tableImagesDownload = new JTable(imageTableModel);
              tableImagesDownload.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
              tableImagesDownload.getSelectionModel()
                      .addListSelectionListener(new ListSelectionListener() {

                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                          if (!e.getValueIsAdjusting()) {
                            int index = tableImagesDownload.getSelectedRow();
                            if (index >= 0) {
                              String name = tableImagesDownload.getValueAt(index, 0).toString();
                              LOGGER.trace("Selected: " + name);
                              omc.setCurrentIm(index);
                            }
                          }

                        }
                      });
              JScrollPane scrollPane = new JScrollPane(tableImagesDownload);
              splitPane.setRightComponent(scrollPane);
            }
          }
        }
        tabbedPane.setEnabledAt(1, true);
      }
      {
        panelUpload = new JPanel();
        tabbedPane.addTab("Upload", null, panelUpload, "Upload experiment settings");
        panelUpload.setLayout(new BorderLayout(0, 0));
        {
          JButton btnUploadExp = new JButton("");
          btnUploadExp.setAction(uploadExperimentAction);
          btnUploadExp.setToolTipText("");
          panelUpload.add(btnUploadExp, BorderLayout.SOUTH);
        }
        {
          JPanel panel = new JPanel();
          panelUpload.add(panel, BorderLayout.CENTER);
          panel.setLayout(new GridLayout(1, 0, 0, 0));
          {
            JSplitPane splitPane = new JSplitPane();
            splitPane.setResizeWeight(0.5);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(true);
            panel.add(splitPane);
            {
              JScrollPane scrollPane = new JScrollPane();
              splitPane.setLeftComponent(scrollPane);
              {
                listDatasetsUpload = new JList<String>();
                listDatasetsUpload.addListSelectionListener(new ListSelectionListener() {
                  public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                      int index = listDatasetsUpload.getSelectedIndex();
                      if (index >= 0) {
                        String name = listDatasetsUpload.getModel().getElementAt(index);
                        LOGGER.trace("Selected: " + name);
                        omc.setCurrentDs(index);
                        imageTableModel.update(omc.getImages(index));
                      }
                    }
                  }
                });
                listDatasetsUpload.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                scrollPane.setViewportView(listDatasetsUpload);
              }
            }
            {
              JScrollPane scrollPane = new JScrollPane();
              splitPane.setRightComponent(scrollPane);
              {
                tableImagesUpload = new JTable(imageTableModel);
                tableImagesUpload.setRowSelectionAllowed(false);
                scrollPane.setViewportView(tableImagesUpload);
              }
            }
          }
        }
        tabbedPane.setEnabledAt(2, true);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setBorder(new EmptyBorder(0, 4, 4, 4));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        GridBagLayout gbl_buttonPane = new GridBagLayout();
        gbl_buttonPane.columnWidths = new int[] { 210, 110 };
        gbl_buttonPane.rowHeights = new int[] { 25 };
        gbl_buttonPane.columnWeights = new double[] { 1.0, 0.0, 0.0 };
        gbl_buttonPane.rowWeights = new double[] { 1.0 };
        buttonPane.setLayout(gbl_buttonPane);
        {
          tpInfo = new JTextPane();
          tpInfo.setContentType("text/html");
          tpInfo.setText(DISCONNECTED);
          tpInfo.setBackground(Color.ORANGE);
          tpInfo.setEditable(false);
          GridBagConstraints gbc_tpInfo = new GridBagConstraints();
          gbc_tpInfo.insets = new Insets(0, 0, 0, 5);
          gbc_tpInfo.fill = GridBagConstraints.BOTH;
          gbc_tpInfo.gridx = 0;
          gbc_tpInfo.gridy = 0;
          buttonPane.add(tpInfo, gbc_tpInfo);
        }
      }
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          omc.disconnect();
          getTpInfo().setText(DISCONNECTED);
          getTpInfo().setBackground(Color.ORANGE);
        }
      });
      cancelButton.setActionCommand("Cancel");
      GridBagConstraints gbc_cancelButton = new GridBagConstraints();
      gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
      gbc_cancelButton.gridx = 2;
      gbc_cancelButton.gridy = 0;
      buttonPane.add(cancelButton, gbc_cancelButton);
    }
  }

  /**
   * Action on Test button.
   * 
   * <p>Try to connect to Omero using credentials stored and downloads datasets from Omero and
   * populates them to Download tab.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  private class TestAction extends AbstractAction {
    public TestAction() {
      putValue(NAME, "Test");
      putValue(SHORT_DESCRIPTION, "Test connection");
    }

    public void actionPerformed(ActionEvent e) {
      if (omc.connect()) {
        getTpInfo().setText(CONNECTED);
        getTpInfo().setBackground(Color.GREEN);
        JList<String> lid = getListDatasetsDownload();
        JList<String> liu = getListDatasetsUpload();
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (DatasetData e1 : omc.getDatasets()) {
          model.addElement(e1.getName() + " (" + e1.getId() + ")");
        }
        lid.setModel(model);
        liu.setModel(model);

      } else {
        getTpInfo().setText(DISCONNECTED);
        getTpInfo().setBackground(Color.ORANGE);
      }
    }
  }

  /**
   * Action on Download button.
   * 
   * <p>switch tab to download.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  private class DownloadAction extends AbstractAction {
    public DownloadAction() {
      putValue(NAME, "Download");
      putValue(SHORT_DESCRIPTION, "Download experiment");
    }

    public void actionPerformed(ActionEvent e) {
      getTabbedPane().setSelectedComponent(getPanelDownload());
    }
  }

  /**
   * Action on Upload button.
   * 
   * <p>switch tab to upload.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
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

  /**
   * Action executed on Store checkbox.
   * 
   * <p>Copies current prefs to IJ prefs or clean those stored already.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
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

  protected JTextPane getTpInfo() {
    return tpInfo;
  }

  protected JPanel getPanelSetup() {
    return panelSetup;
  }

  protected JList<String> getListDatasetsDownload() {
    return listDatasetsDownload;
  }

  protected JTable getTableImagesDownload() {
    return tableImagesDownload;
  }

  protected JList<String> getListDatasetsUpload() {
    return listDatasetsUpload;
  }

  protected JTable getTableImagesUpload() {
    return tableImagesUpload;
  }

  /**
   * Action executed on uploading experiment button.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  private class UploadExperimentAction extends AbstractAction {
    public UploadExperimentAction() {
      putValue(NAME, "Select experiment and upload");
      putValue(SHORT_DESCRIPTION, "Upload QCONF to Omero");
    }

    public void actionPerformed(ActionEvent e) {
      setPanelEnabled(getContentPane(), false);
      omc.upload();
      imageTableModel.update(omc.getImages(omc.getCurrentDs()));
      setPanelEnabled(getContentPane(), true);
    }
  }

  private void setPanelEnabled(Container container, Boolean isEnabled) {
    container.setEnabled(isEnabled);

    Component[] components = container.getComponents();

    for (Component component : components) {
      if (component instanceof Container) {
        setPanelEnabled((Container) component, isEnabled);
      }
      component.setEnabled(isEnabled);
    }
  }

  /**
   * Action executed on download experiment button.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  private class DownloadExperimentAction extends AbstractAction {
    public DownloadExperimentAction() {
      putValue(NAME, "Select folder and download");
      putValue(SHORT_DESCRIPTION, "Download selected dataset");
    }

    public void actionPerformed(ActionEvent e) {
      String folder = new DirectoryChooser("Select destination folder").getDirectory();
      if (folder != null && !folder.isEmpty()) {
        setPanelEnabled(getContentPane(), false);
        omc.download(folder);
        setPanelEnabled(getContentPane(), true);
      }
    }
  }

  protected JPanel getContentPanel() {
    return contentPanel;
  }
}

class ImageTable extends AbstractTableModel {

  public String[] columnNames = { "Image", "Creation time", "ID" };

  List<String[]> rows;

  public ImageTable() {
    rows = new ArrayList<>();
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return rows.get(rowIndex)[columnIndex];
  }

  public void update(List<ImageData> up) {
    rows.clear();
    for (ImageData im : up) {
      rows.add(new String[] { im.getName(),
          new SimpleDateFormat("yyyy-MM-dd-HH mm ss").format(im.getCreated()),
          Long.toString(im.getId()) });
    }
    fireTableDataChanged();
  }

}
