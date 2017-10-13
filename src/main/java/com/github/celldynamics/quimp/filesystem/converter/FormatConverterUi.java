package com.github.celldynamics.quimp.filesystem.converter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * Build format converter view.
 * 
 * <p>Generally, {@link JCheckBox} controls are coded as group:name, where name is exactly name
 * displayed in UI. These strings are stored in {@link FormatConverterModel} on each action on
 * {@link JCheckBox}. {@link FormatConverterController} consumes such coded configuration performs
 * requested actions. The rationale is that these strings can ba also provided from command line if
 * headless mode is used.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverterUi extends JDialog {

  private static final String FILE_PER_FRAME = "File per frame";
  private static final String FILE_PER_CELL = "File per cell";
  // Strings displayed at controls and also unique code stored in model (like setActionCommand)
  // all lower case, name ,changing can break compatibility as these strings are supposed to be
  // parameters to Formatconverter run in headless mode
  static final String STATS_FLUORES = "stats:fluores";
  static final String STATS_GEOMETRIC = "stats:geometric";
  static final String ECMM_CENTROID = "ecmm:centroid";
  static final String ECCM_OUTLINES = "ecmm:outlines";
  static final String BOA_CENTROID = "boa:centroid";
  static final String BOA_SNAKES = "boa:snakes";
  static final String MAP_FLUORES = "map:fluores";
  static final String MAP_CONVEXITY = "map:convexity";
  static final String MAP_MOTILITY = "map:motility";
  static final String MAP_Y_COORDS = "map:x coords";
  static final String MAP_X_COORDS = "map:y coords";
  static final String MAP_ORIGIN = "map:origin";
  static final String MAP_COORD = "map:coord";
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 4700215468821831701L;
  private JButton generateButton;
  private JCheckBox chckbxCentroid;
  private JCheckBox chckbxSnakes;
  private JCheckBox chckbxCentroidEcmm;
  private JCheckBox chckbxGeometric;
  private JCheckBox chckbxFluoresStat;
  private JCheckBox chckbxX;
  private JCheckBox chckbxOrigin;
  private JCheckBox chckbxCoord;
  private JCheckBox chckbxFluo;
  private JCheckBox chckbxY;
  private JCheckBox chckbxMotility;
  private JCheckBox chckbxConvexity;
  private JToggleButton btnEcmm;
  private JToggleButton btnBoa;
  private JToggleButton btnStats;
  private JToggleButton btnMaps;
  private JButton cancelButton;
  private JPanel okcancelPanel;
  private JPanel loadPanel;
  private JButton btnLoad;
  private JButton btnConvertAny;
  private JTabbedPane tabbedPane;
  private JTextPane infoText;
  private JScrollPane scrollPane;
  private JCheckBox chckbxOutlinesEcmm;
  private JPanel multifilePanel;
  private JCheckBox chckbxMultiFileOutput;
  private JPanel panel_1;

  /**
   * Build checkbox with link to model.
   * 
   * @param text text of checkbox
   * @param model reference to {@link FormatConverterModel}
   * @return checkbox object
   * @wbp.factory
   * @wbp.factory.parameter.source text "fluores"
   * @wbp.factory.parameter.source model model
   */
  public static JCheckBox createJCheckBox(String text, FormatConverterModel model) {
    String[] names = text.split(":");
    JCheckBox checkBox = new JCheckBox(names[1]);
    checkBox.setSelected(false);
    checkBox.addItemListener(new ItemListener() { // itemlistener react on setSelected(true)

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (checkBox.isSelected()) { // add to list
          model.getStatus().add(text);
        } else {
          model.getStatus().remove(text); // remove from list
        }
      }
    });
    return checkBox;
  }

  /**
   * Default constructor - for tests and WindowCreator. Object should be initialized with
   * {@link #FormatConverterUi(FormatConverterModel)}.
   */
  public FormatConverterUi() {
    this(new FormatConverterModel());
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  /**
   * Create the dialog. Proer constructor for normal use.
   * 
   * @param model model class for this view
   */
  public FormatConverterUi(FormatConverterModel model) {
    setTitle("Format Converter");
    setBounds(100, 100, 568, 307);
    getContentPane().setLayout(new BorderLayout());
    {
      tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      tabbedPane.setBorder(null);
      tabbedPane.setBackground(UIManager.getColor("Panel.background"));
      getContentPane().add(tabbedPane, BorderLayout.CENTER);
      {
        JPanel panel = new JPanel();
        panel.setBorder(null);
        tabbedPane.addTab("Conversions", null, panel, null);
        tabbedPane.setBackgroundAt(0, UIManager.getColor("Panel.background"));
        GridBagLayout gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[] { 0, 0, 0, 0, 86, 0 };
        gblPanel.rowHeights = new int[] { 0 };
        gblPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        gblPanel.rowWeights = new double[] { 1.0 };
        panel.setLayout(gblPanel);
        {
          JPanel panel1 = new JPanel();
          GridBagConstraints gbcPanel1 = new GridBagConstraints();
          gbcPanel1.fill = GridBagConstraints.BOTH;
          gbcPanel1.insets = new Insets(0, 0, 0, 5);
          gbcPanel1.gridx = 0;
          gbcPanel1.gridy = 0;
          panel.add(panel1, gbcPanel1);
          panel1.setLayout(new GridLayout(0, 1, 0, 0));
          {
            JPanel boaPanel = new JPanel();
            boaPanel.setBorder(new TitledBorder(null, "Boa", TitledBorder.LEADING, TitledBorder.TOP,
                    null, null));
            panel1.add(boaPanel);
            boaPanel.setLayout(new BoxLayout(boaPanel, BoxLayout.Y_AXIS));
            {
              chckbxCentroid = FormatConverterUi.createJCheckBox(BOA_CENTROID, model);
              boaPanel.add(chckbxCentroid);
            }
            {
              chckbxSnakes = FormatConverterUi.createJCheckBox(BOA_SNAKES, model);
              boaPanel.add(chckbxSnakes);
            }
          }
        }
        {
          JPanel panel1 = new JPanel();
          GridBagConstraints gbcPanel1 = new GridBagConstraints();
          gbcPanel1.insets = new Insets(0, 0, 0, 5);
          gbcPanel1.fill = GridBagConstraints.BOTH;
          gbcPanel1.gridx = 1;
          gbcPanel1.gridy = 0;
          panel.add(panel1, gbcPanel1);
          panel1.setLayout(new GridLayout(0, 1, 0, 0));
          {
            JPanel ecmmPanel = new JPanel();
            panel1.add(ecmmPanel);
            ecmmPanel.setBorder(new TitledBorder(null, "Ecmm", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
            ecmmPanel.setLayout(new BoxLayout(ecmmPanel, BoxLayout.Y_AXIS));
            {
              chckbxCentroidEcmm = FormatConverterUi.createJCheckBox(ECMM_CENTROID, model);
              ecmmPanel.add(chckbxCentroidEcmm);
            }
            {
              chckbxOutlinesEcmm = FormatConverterUi.createJCheckBox(ECCM_OUTLINES, model);
              ecmmPanel.add(chckbxOutlinesEcmm);
            }
          }
        }
        {
          JPanel panel1 = new JPanel();
          GridBagConstraints gbcPanel1 = new GridBagConstraints();
          gbcPanel1.insets = new Insets(0, 0, 0, 5);
          gbcPanel1.fill = GridBagConstraints.BOTH;
          gbcPanel1.gridx = 2;
          gbcPanel1.gridy = 0;
          panel.add(panel1, gbcPanel1);
          panel1.setLayout(new GridLayout(0, 1, 0, 0));
          {
            JPanel statPanel = new JPanel();
            statPanel.setBorder(new TitledBorder(null, "Stats", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
            panel1.add(statPanel);
            statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS));
            {
              chckbxGeometric = FormatConverterUi.createJCheckBox(STATS_GEOMETRIC, model);
              statPanel.add(chckbxGeometric);
            }
            {
              chckbxFluoresStat = FormatConverterUi.createJCheckBox(STATS_FLUORES, model);
              statPanel.add(chckbxFluoresStat);
            }
          }
        }
        {
          JPanel panel1 = new JPanel();
          GridBagConstraints gbcPanel1 = new GridBagConstraints();
          gbcPanel1.insets = new Insets(0, 0, 0, 5);
          gbcPanel1.fill = GridBagConstraints.BOTH;
          gbcPanel1.gridx = 3;
          gbcPanel1.gridy = 0;
          panel.add(panel1, gbcPanel1);
          panel1.setLayout(new GridLayout(0, 1, 0, 0));
          {
            JPanel mapsPanel = new JPanel();
            mapsPanel.setBorder(new TitledBorder(null, "Maps", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
            panel1.add(mapsPanel);
            mapsPanel.setLayout(new BoxLayout(mapsPanel, BoxLayout.Y_AXIS));
            {
              chckbxCoord = FormatConverterUi.createJCheckBox(MAP_COORD, model);
              mapsPanel.add(chckbxCoord);
            }
            {
              chckbxOrigin = FormatConverterUi.createJCheckBox(MAP_ORIGIN, model);
              mapsPanel.add(chckbxOrigin);
            }
            {
              chckbxX = FormatConverterUi.createJCheckBox(MAP_X_COORDS, model);
              mapsPanel.add(chckbxX);
            }
            {
              chckbxY = FormatConverterUi.createJCheckBox(MAP_Y_COORDS, model);
              mapsPanel.add(chckbxY);
            }
            {
              chckbxMotility = FormatConverterUi.createJCheckBox(MAP_MOTILITY, model);
              mapsPanel.add(chckbxMotility);
            }
            {
              chckbxConvexity = FormatConverterUi.createJCheckBox(MAP_CONVEXITY, model);
              mapsPanel.add(chckbxConvexity);
            }
            {
              chckbxFluo = FormatConverterUi.createJCheckBox(MAP_FLUORES, model);
              mapsPanel.add(chckbxFluo);
            }
          }
        }
        {
          JPanel presetPanel = new JPanel();
          presetPanel.setBorder(new TitledBorder(null, "Presets", TitledBorder.LEADING,
                  TitledBorder.TOP, null, null));
          GridBagConstraints gbcPresetPanel = new GridBagConstraints();
          gbcPresetPanel.fill = GridBagConstraints.HORIZONTAL;
          gbcPresetPanel.anchor = GridBagConstraints.NORTH;
          gbcPresetPanel.gridx = 4;
          gbcPresetPanel.gridy = 0;
          panel.add(presetPanel, gbcPresetPanel);
          presetPanel.setLayout(new GridLayout(0, 1, 0, 2));
          {
            btnBoa = new JToggleButton("Boa");
            btnBoa.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent arg0) {
                boolean status = ((JToggleButton) arg0.getSource()).isSelected();
                setBoa(status);
              }
            });
            presetPanel.add(btnBoa);
          }
          {
            btnEcmm = new JToggleButton("Ecmm");
            btnEcmm.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent arg0) {
                boolean status = ((JToggleButton) arg0.getSource()).isSelected();
                setEcmm(status);
              }
            });
            presetPanel.add(btnEcmm);
          }
          {
            btnStats = new JToggleButton("Stats");
            btnStats.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                boolean status = ((JToggleButton) e.getSource()).isSelected();
                setStats(status);
              }
            });
            presetPanel.add(btnStats);
          }
          {
            btnMaps = new JToggleButton("Maps");
            btnMaps.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                boolean status = ((JToggleButton) e.getSource()).isSelected();
                setMaps(status);
              }
            });
            presetPanel.add(btnMaps);
          }
          {
            JSeparator separator = new JSeparator();
            presetPanel.add(separator);
          }
          {
            JButton btnAll = new JButton("All");
            btnAll.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                boolean status = true;
                setUiElements(status);
              }

            });
            btnAll.setBackground(Color.ORANGE);
            presetPanel.add(btnAll);
          }
          {
            JButton btnNone = new JButton("None");
            btnNone.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                boolean status = false;
                setUiElements(status);
              }
            });
            presetPanel.add(btnNone);
          }
        }
      }
      {
        JPanel panel = new JPanel();
        panel.setBorder(null);
        tabbedPane.addTab("Info", null, panel, null);
        panel.setLayout(new BorderLayout(0, 0));
        {
          panel_1 = new JPanel();
          panel_1.setLayout(new BorderLayout(0, 0));
          {
            infoText = new JTextPane();
            panel_1.add(infoText);
            infoText.setEditable(false);
          }
        }
        {
          scrollPane = new JScrollPane(panel_1);
          panel.add(scrollPane);
        }
      }
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new BorderLayout(0, 0));
      {
        okcancelPanel = new JPanel();
        FlowLayout flOkcancelPanel = (FlowLayout) okcancelPanel.getLayout();
        flOkcancelPanel.setHgap(1);
        flOkcancelPanel.setAlignment(FlowLayout.RIGHT);
        buttonPane.add(okcancelPanel, BorderLayout.EAST);
        {
          generateButton = new JButton("Generate");
          generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
              tabbedPane.setSelectedIndex(1);
            }
          });
          generateButton.setToolTipText("Save selected datasets into separate files");
          okcancelPanel.add(generateButton);
          generateButton.setActionCommand("");
          getRootPane().setDefaultButton(generateButton);
        }
        {
          cancelButton = new JButton("Quit");
          okcancelPanel.add(cancelButton);
          cancelButton.setActionCommand("");
        }
      }
      {
        loadPanel = new JPanel();
        FlowLayout flLoadPanel = (FlowLayout) loadPanel.getLayout();
        flLoadPanel.setHgap(1);
        flLoadPanel.setAlignment(FlowLayout.LEFT);
        buttonPane.add(loadPanel, BorderLayout.WEST);
        {
          btnLoad = new JButton("Load QCONF");
          btnLoad.setToolTipText("Load QCONF and allow further selections");
          btnLoad.setFont(new Font("Dialog", Font.BOLD, 12));
          btnLoad.setEnabled(true);
          loadPanel.add(btnLoad);
        }
        {
          btnConvertAny = new JButton("Convert any");
          btnConvertAny.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              tabbedPane.setSelectedIndex(1);
            }
          });
          btnConvertAny.setFont(new Font("Dialog", Font.BOLD, 12));
          btnConvertAny.setToolTipText("Load paQP/QCONF and convert");
          loadPanel.add(btnConvertAny);
        }
      }
      {
        multifilePanel = new JPanel();
        buttonPane.add(multifilePanel, BorderLayout.CENTER);
        {
          chckbxMultiFileOutput = new JCheckBox(FILE_PER_FRAME);
          chckbxMultiFileOutput.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
              if (chckbxMultiFileOutput.isSelected()) {
                chckbxMultiFileOutput.setText(FILE_PER_FRAME);
              } else {
                chckbxMultiFileOutput.setText(FILE_PER_CELL);
              }
            }
          });
          chckbxMultiFileOutput.setSelected(true);
          chckbxMultiFileOutput.setToolTipText(
                  "Save one file per cell or one file per frame (snakes and outlines only)");
          multifilePanel.add(chckbxMultiFileOutput);
        }
      }
    }
  }

  /**
   * Get reference to OK button.
   * 
   * @return Reference to OK button to Controller.
   */
  public JButton getOkButton() {
    return generateButton;
  }

  protected JCheckBox getChckbxCentroid() {
    return chckbxCentroid;
  }

  protected JCheckBox getChckbxSnakes() {
    return chckbxSnakes;
  }

  protected boolean getChckbxMultiFileOutput() {
    return chckbxMultiFileOutput.isSelected();
  }

  protected JCheckBox getChckbxCentroidEcmm() {
    return chckbxCentroidEcmm;
  }

  protected JCheckBox getChckbxOutlinesEcmm() {
    return chckbxOutlinesEcmm;
  }

  protected JCheckBox getChckbxGeometric() {
    return chckbxGeometric;
  }

  protected JCheckBox getChckbxFluoresStat() {
    return chckbxFluoresStat;
  }

  protected JCheckBox getChckbxX() {
    return chckbxX;
  }

  protected JCheckBox getChckbxOrigin() {
    return chckbxOrigin;
  }

  protected JCheckBox getChckbxCoord() {
    return chckbxCoord;
  }

  protected JCheckBox getChckbxFluo() {
    return chckbxFluo;
  }

  protected JCheckBox getChckbxY() {
    return chckbxY;
  }

  protected JCheckBox getChckbxMotility() {
    return chckbxMotility;
  }

  protected JCheckBox getChckbxConvexity() {
    return chckbxConvexity;
  }

  private void setBoa(boolean status) {
    getChckbxCentroid().setSelected(status);
    getChckbxSnakes().setSelected(status);
  }

  private void setEcmm(boolean status) {
    getChckbxCentroidEcmm().setSelected(status);
    getChckbxOutlinesEcmm().setSelected(status);
  }

  private void setStats(boolean status) {
    getChckbxGeometric().setSelected(status);
    getChckbxFluoresStat().setSelected(status);
  }

  private void setMaps(boolean status) {
    getChckbxCoord().setSelected(status);
    getChckbxOrigin().setSelected(status);
    getChckbxX().setSelected(status);
    getChckbxY().setSelected(status);
    getChckbxMotility().setSelected(status);
    getChckbxConvexity().setSelected(status);
    getChckbxFluo().setSelected(status);
  }

  protected JToggleButton getBtnEcmm() {
    return btnEcmm;
  }

  protected JToggleButton getBtnBoa() {
    return btnBoa;
  }

  protected JToggleButton getBtnStats() {
    return btnStats;
  }

  protected JToggleButton getBtnMaps() {
    return btnMaps;
  }

  private void setUiElements(boolean status) {
    setBoa(status);
    setEcmm(status);
    setStats(status);
    setMaps(status);

    getBtnBoa().setSelected(status);
    getBtnEcmm().setSelected(status);
    getBtnMaps().setSelected(status);
    getBtnStats().setSelected(status);
  }

  protected JButton getCancelButton() {
    return cancelButton;
  }

  protected JButton getLoadButton() {
    return btnLoad;
  }

  protected JButton getConvertButton() {
    return btnConvertAny;
  }

  protected JTextPane getInfoText() {
    return infoText;
  }
}
