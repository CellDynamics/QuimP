package uk.ac.warwick.wsbc.quimp.plugin.utils;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.plugin.ParamList;

/**
 * Simple window builder for QuimP plugins
 * 
 * <p>Allow user to construct simple window for his plugins just by passing textual description of
 * what
 * that window should contain.
 * 
 * <p>Main function (BuildWindow) accepts HashMap with pairs [name,params] where name is unique name
 * of
 * the parameter and params defines how this parameter will be displayed in UI (see
 * BuildWindow(final ParamList)). Using this mapping there is next list \c ui created that contains
 * the same names but now joined with UI components. This list is used for addressing these
 * component basing on theirs names. The UI controls are stored at \c ui which is \a protected and
 * may be used for influencing these controls by user. To identify certain UI control its name is
 * required which is the string passed as first dimension of \c def definition passed to to
 * BuildWindow method. Below code shows how to change property of control
 * 
 * <pre>
 * <code>
 * String key = "paramname"; // case insensitive JSpinner comp = (JSpinner)
 * ui.get(key); // get control using its name
 * comp.getEditor()).getTextField().setColumns(5);
 * </code>
 * </pre>
 * 
 * <p><b>Warning</b>
 * 
 * <p>UI type as JSpinner keeps data in double format even in values passed through by
 * setValues(ParamList) are integer (ParamList keeps data as String). Therefore getValues can return
 * this list with the same data but in double syntax (5 -> 5.0). Any try of convention of "5.0" to
 * integer value will cause NumberFormatException. To avoid this problem use
 * QuimP.plugin.ParamList.getIntValue(String) from ParamList of treat all strings in ParamList as
 * Double.
 * 
 * <p>Methods getValues() and setValues() should be used by class extending QWindowBuilder for
 * setting
 * and achieving parameters from GUI. Note that parameters in UIs are validated only when they
 * become out of focus. Until cursor is in UI its value is not updated internally, thus getValues()
 * returns its old snapshot.
 * 
 * <p>reservedKeys is list of reserved keys that are not UI elements. They are processed in
 * different
 * way. Other behaviour: By default on close or when user clicked Cancel window is hided only, not
 * destroyed. This is due to preservation of all settings. Lifetime of window depends on QuimP
 * 
 * <p>All parameters passed to and from QWindowBuilder as ParamList are encoded as {@link String}
 * 
 * @author p.baniukiewicz
 */
public abstract class QWindowBuilder {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QWindowBuilder.class.getName());

  /**
   * The plugin wnd.
   */
  protected JFrame pluginWnd; // main window object

  /**
   * The window state.
   */
  protected boolean windowState; // current window state \c true if visible

  /**
   * The plugin panel.
   */
  protected JPanel pluginPanel; // Main panel extended on whole \c pluginWnd

  /**
   * The ui.
   */
  protected ComponentList ui; // list of all UI elements
  private ParamList def; // definition of window and parameters

  private final HashSet<String> reservedKeys =
          new HashSet<String>(Arrays.asList(new String[] { "help", "name" })); // reserved keys

  // definition string - positions of configuration data in value string (see
  // BuildWindow)
  private final int uiType = 0; // type of UI control to create
  private final int srMin = 1; // spinner min value
  private final int srMax = 2; // spinner max value
  private final int srStep = 3; // spinner step value
  private final int srDefault = 4; // spinner default value

  /**
   * The apply B.
   */
  // definition of constant elements of UI
  protected JButton applyB; // Apply button (do nothing but may be overwritten)

  /**
   * The cancel B.
   */
  protected JButton cancelB; // Cancel button (hides it)

  /**
   * Default constructor.
   */
  public QWindowBuilder() {
    LOGGER.trace("Entering constructor");
    ui = new ComponentList();
    def = null;
  }

  /**
   * Main window builder.
   * 
   * <p>The window is constructed using configuration provided by def parameter which is Map of
   * [key,value]. The key is the name of the parameter that should be related to value held in it
   * (e.g window, smooth, step, etc.). The name is not case sensitive. Keys are strictly related
   * to UI elements that are created by this method (basing on configuration passed in value).
   * There are two special names that are not related to UI directly:
   * <ol>
   * <li>help - defines textual help provided as parameter. It supports HTML
   * <li>name - defines plugin name provided as parameter
   * </ol>
   * 
   * <p>The parameter list is defined as String and its content is depended on key. For help and
   * name it contains single string with help text and plugin name respectively.
   * 
   * <p>The UI elements are defined for all other cases in value filed of Map as comma separated
   * string. Known UI are as follows:
   * <ul>
   * <li>spinner - creates Spinner control. It requires 4 parameters (in order)
   * <ol>
   * <li>minimal range
   * <li>maximal range
   * <li>step
   * <li>default value
   * </ol>
   * <li>choice - creates Choice control. It requires 0 or more parameters - entries in list
   * <ol>
   * <li>first entry
   * <li>second entry
   * <li>...
   * </ol>
   * <li>button - creates JButton control. It requires 1 parameter:
   * <ol>
   * <li>Name on the button
   * </ol>
   * <li>checkbox - creates JCheckBox control. It requires 3 parameters (in order)
   * <ol>
   * <li>checkbox name
   * <li>checkbox state (boolean value, true or false)
   * <li>initial value (true or false)
   * </ol>
   * </ul>
   * For choice calling uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder.setValues(final
   * ParamList) has sense only if passed parameters will be present in list already (so it has
   * been used during creation of window, passed in constructor) In this case it causes selection
   * of this entry in list. Otherwise passed value will be ignored. setVales for Choice does
   * not add new entry to list.
   * 
   * <p>The type of required UI element associated with given parameter name (Key) is coded in
   * value of given Key in accordance with list above. The correct order of sub-parameters must be
   * preserved. By default window is not visible yet. User must call ShowWindow or ToggleWindow. The
   * Apply
   * button does nothing. It is only to refocus after change of values in spinners. They are not
   * updated until unfocused. User can overwrite this behaviour in his own class derived from
   * QWindowBuilder
   * 
   * <p>This method can be overriden in implementing class that allows for e.g. setting size of the
   * window or add listeners:
   * 
   * <pre>
   * <code>
   * public void buildWindow(ParamList def) {
   *     super.buildWindow(def); // add preferred size to this window
   *     pluginWnd.setPreferredSize(new Dimension(300, 450));
   *     pluginWnd.pack();
   *     pluginWnd.setVisible(true);
   *     pluginWnd.addWindowListener(new myWindowAdapter()); // close not hide ((JButton)
   *     ui.get("Load Mask")).addActionListener(this);
   *     applyB.addActionListener(this);
   * }
   * </code>
   * </pre>
   * 
   * <p>Throw IllegalArgumentException or other unchecked exceptions on wrong syntax of def
   * 
   * @param def Configuration as described
   */
  public void buildWindow(final ParamList def) {
    if (def.size() < 2) {
      throw new IllegalArgumentException("Window must contain title and" + " at least one control");
    }
    this.def = def; // remember parameters
    ui.clear(); // clear all ui stored on second call of third method

    pluginWnd = new JFrame(); // create frame with title given as first position in table
    pluginPanel = new JPanel(); // main panel on whole window
    // divide window on two zones
    // - upper for controls,
    // - middle for help
    pluginPanel.setLayout(new BorderLayout());

    Panel north = new Panel(); // panel for controls
    // get layout size
    int siz = def.size(); // total size of Map
    // but grid layout does not contain help and name or other reserved non
    // UI keys
    Set<String> s = def.keySet(); // get Set of keys
    for (String k : reservedKeys) { // and check if any of them is in s
      if (s.contains(k)) {
        siz--;
      }
    }
    GridLayout gridL = new GridLayout(siz, 2); // Nx2, by default in row we have control and its des
    north.setLayout(gridL);
    gridL.setVgap(5); // set bigger gaps
    gridL.setHgap(5);

    // iterate over def entries except first one which is always title
    // every decoded control is put into ordered hashmap together with its
    // descriptor (label)
    for (Map.Entry<String, String> e : def.entrySet()) {
      String key = e.getKey();
      if (reservedKeys.contains(key)) {
        continue;
      }
      String[] uiparams = StringParser.getParams(e.getValue());
      if (uiparams.length == 0) {
        throw new IllegalArgumentException("Probably wrong syntax in UI definition");
      }
      switch (uiparams[uiType].toLowerCase()) {
        case "spinner": // by default all spinners are double
          if (uiparams.length != 5) {
            throw new IllegalArgumentException(
                    "Probably wrong syntax in UI definition for " + uiparams[uiType]);
          }
          SpinnerNumberModel model = new SpinnerNumberModel(Double.parseDouble(uiparams[srDefault]),
                  Double.parseDouble(uiparams[srMin]), // min
                  Double.parseDouble(uiparams[srMax]), // max
                  Double.parseDouble(uiparams[srStep])); // step
          ui.put(key, new JSpinner(model));
          ui.put(key + "label", new Label(WordUtils.capitalize(WordUtils.capitalize(key)))); // des
          break;
        case "choice":
          if (uiparams.length < 2) {
            throw new IllegalArgumentException(
                    "Probably wrong syntax in UI definition for " + uiparams[uiType]);
          }
          Choice c = new Choice();
          for (int i = uiType + 1; i < uiparams.length; i++) {
            c.add(uiparams[i]);
          }
          c.select(0);
          ui.put(key, c);
          ui.put(key + "label", new Label(WordUtils.capitalize(key))); // add description
          break;
        case "button":
          if (uiparams.length != 2) {
            throw new IllegalArgumentException(
                    "Probably wrong syntax in UI definition for " + uiparams[uiType]);
          }
          JButton b = new JButton(uiparams[1]);
          ui.put(key, b);
          ui.put(key + "label", new Label(WordUtils.capitalize(key))); // add description
          break;
        case "checkbox":
          if (uiparams.length != 3) {
            throw new IllegalArgumentException(
                    "Probably wrong syntax in UI definition for " + uiparams[uiType]);
          }
          JCheckBox cb = new JCheckBox(WordUtils.capitalize(uiparams[1]),
                  Boolean.parseBoolean(uiparams[2]));
          ui.put(key, cb);
          ui.put(key + "label", new Label(WordUtils.capitalize(key))); // add description
          break;
        default:
          // wrong param syntax
          throw new IllegalArgumentException("Unknown ui type" + " provided: " + key);
      }
    }

    // iterate over all components and add them to grid layout
    for (Map.Entry<String, Component> me : ui.entrySet()) {
      north.add(me.getValue());
    }

    // add non ui elements
    if (def.containsKey("name")) {
      // border on whole window
      pluginPanel.setBorder(BorderFactory.createTitledBorder("Plugin " + def.get("name")));
    }
    if (def.containsKey("help")) {
      JTextPane helpArea = new JTextPane(); // default size of text area
      // helpArea.setLineWrap(true);
      // helpArea.setWrapStyleWord(true);
      helpArea.setPreferredSize(new Dimension(80, 200));
      helpArea.setContentType("text/html");
      JScrollPane helpPanel = new JScrollPane(helpArea);
      helpArea.setEditable(false);
      pluginPanel.add(helpPanel, BorderLayout.CENTER); // locate at center position
      helpArea.setText(def.get("help")); // set help text
    }

    // add Apply button on south
    Panel south = new Panel();
    south.setLayout(new FlowLayout());
    applyB = new JButton("Apply");
    south.add(applyB);
    cancelB = new JButton("Cancel");
    south.add(cancelB);
    // set action on Cancel - window is hided
    cancelB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        pluginWnd.setVisible(false);
        windowState = false;
      }
    });

    // build window
    pluginPanel.add(north, BorderLayout.NORTH);
    pluginPanel.add(south, BorderLayout.SOUTH);
    pluginWnd.add(pluginPanel);
    pluginWnd.pack();
    // add listener on close - window is hidden to preserve settings
    pluginWnd.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        LOGGER.trace("Window closing");
        windowState = false;
        pluginWnd.setVisible(false);
      }
    });
    windowState = false; // by default window is not visible. User must call ShowWindow or ToggleWi
  }

  /**
   * Show or hide window.
   * 
   * @param state State of the window true to show, false to hide
   */
  public void showWindow(boolean state) {
    pluginWnd.setVisible(state);
    windowState = state;
  }

  /**
   * Toggle window visibility if input is \c true. Close it immediately if input is \c false
   * 
   * @param val Demanded state of window. If \c true visibility of window is toggled, if \c false
   *        window is closing.
   * @return Current status of window \c true if visible, \c false if not
   */
  public boolean toggleWindow(boolean val) {
    if (!val) {
      windowState = false;
      showWindow(windowState);
    } else {
      windowState = !windowState;
      showWindow(windowState);
    }
    return windowState;
  }

  /**
   * Check if window is visible.
   * 
   * @return true if it is visible, false otherwise
   */
  public boolean isWindowVisible() {
    return windowState;
  }

  /**
   * Set plugin parameters.
   * 
   * <p>Use the same parameters names as in BuildWindow(Map[String, String[]]). The name of the
   * parameter is key in Map. Every parameter passed to this method must have its
   * representation in GUI and thus it must be present in def parameter of
   * BuildWindow(Map[String, String[]]) All values are passed as:
   * <ol>
   * <li>Double in case of spinners
   * </ol>
   * 
   * <p>User has to care for correct format passed to UI control. If input values are above range
   * defined in def, new range is set for UI control
   * 
   * @param vals [key,value] pairs to fill UI.
   */
  public void setValues(final ParamList vals) {
    // iterate over parameters and match names to UIs
    for (Map.Entry<String, String> e : vals.entrySet()) {
      String key = e.getKey();
      String val = e.getValue();
      // find key in def and get type of control and its instance
      switch (def.getParsed(key)[uiType]) { // first string in vals is type of
        // control, see BuildWindow
        case "spinner": {
          // get UI component of name key (keys in vals must match to keys in BuildWindow(def))
          JSpinner comp = (JSpinner) ui.get(key);
          comp.setValue(Double.parseDouble(val)); // set value from vals
          SpinnerNumberModel sm = (SpinnerNumberModel) comp.getModel();
          if (sm.getNextValue() == null) {
            sm.setMaximum(Double.parseDouble(val));
          } else if (sm.getPreviousValue() == null) {
            sm.setMinimum(Double.parseDouble(val));
          }
          break;
        }
        case "choice": {
          Choice comp = (Choice) ui.get(key);
          comp.select(val);
          break;
        }
        case "button": {
          break;
        }
        case "checkbox": {
          JCheckBox c = (JCheckBox) ui.get(key);
          c.setSelected(Boolean.parseBoolean(val));

          break;
        }
        default:
          throw new IllegalArgumentException("Unknown UI type in setValues");
      }
    }
  }

  /**
   * Receives parameters related to UI elements as Map.
   * 
   * <p>To get one particular parameter use getIntegerFromUI(String) or getDoubleFromUI(String)
   * 
   * <p>JSpinners are set to support double values and that values are returned here It means that
   * originally pushed to UI integers are changed to Double what can affect set/getpluginConfig
   * from filter interface as well
   * 
   * @return List of [key,param], where key is the name of parameter passed to QWindowBuilder
   *         class through BuildWindow method. The method remaps those keys to related UI controls
   *         and reads values associated to them.
   * @see #getDoubleFromUI(String)
   * @see #getIntegerFromUI(String)
   * 
   */
  public ParamList getValues() {
    ParamList ret = new ParamList();
    // iterate over all UI elements
    Iterator<Map.Entry<String, Component>> entryIterator = ui.entrySet().iterator();
    while (entryIterator.hasNext()) {
      Map.Entry<String, Component> m = entryIterator.next();
      String key = m.getKey();
      // check type of component
      switch (def.getParsed(key)[uiType]) {
        case "spinner": {
          JSpinner val = (JSpinner) m.getValue(); // get value
          ret.put(key, String.valueOf(val.getValue())); // store it in returned Map at
          // the same key
          break;
        }
        case "choice": {
          Choice val = (Choice) m.getValue();
          ret.put(key, val.getSelectedItem());
          break;
        }
        case "button": {
          break;
        }
        case "checkbox": {
          JCheckBox c = (JCheckBox) m.getValue();
          ret.put(key, String.valueOf(c.isSelected()));
          break;
        }
        default:
          throw new IllegalArgumentException("Unknown UI type in getValues");
      }
      entryIterator.next(); // skip label. ui Map has repeating entries UI,label,UI1,label1,..
    }
    return ret;
  }

  /**
   * Return value related to given key.
   * 
   * <p>Value is retrieved from ui element related to given \b key. Relation between keys and ui
   * elements is defined by user in configuration list provided to buildWindow(final ParamList).
   * 
   * <p>The key must be defined and exists in that list. In case of wrong conversion it may be
   * exception thrown. User is responsible to call this method for proper key.
   * 
   * @param key Key to be read from configuration list, case insensitive
   * @return integer representation of value under \c key
   * @see #buildWindow(ParamList)
   */
  public int getIntegerFromUI(final String key) {
    return (int) getDoubleFromUI(key);
  }

  /**
   * Return value related to given key.
   * 
   * @param key key
   * @return {@link #getIntegerFromUI(String)}
   * 
   * @see #getIntegerFromUI(String)
   */
  public double getDoubleFromUI(final String key) {
    // get list of all params from ui as <key,val> list
    ParamList uiParam = getValues();
    return uiParam.getDoubleValue(key);
  }

  /**
   * Return value related to given key.
   * 
   * @param key key
   * @return {@link #getIntegerFromUI(String)}
   * 
   * @see #getIntegerFromUI(String)
   */
  public boolean getBooleanFromUI(final String key) {
    // get list of all params from ui as <key,val> list
    ParamList uiParam = getValues();
    return uiParam.getBooleanValue(key);
  }

  /**
   * Return value related to given key. Added for convenience
   * 
   * @param key key
   * @return {@link #getIntegerFromUI(String)}
   * 
   * @see #getIntegerFromUI(String)
   */
  public String getStringFromUI(final String key) {
    // get list of all params from ui as <key,val> list
    ParamList uiParam = getValues();
    return uiParam.getStringValue(key);
  }

  /**
   * Stores components under Keys that are not case insensitive
   * 
   * @author p.baniukiewicz
   *
   */
  public class ComponentList extends LinkedStringMap<Component> {

    private static final long serialVersionUID = -5157229346595354602L;

  }
}
