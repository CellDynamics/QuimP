/**
 * @file QWindowBuilder.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;

/**
 * Simple window builder for QuimP plugins
 * 
 * Allow user to construct simple window for his plugins just by passing textual
 * description of what that window should contain.
 * 
 * Main function (BuildWindow) accepts HashMap with pairs <name,params> where
 * name is unique name of the parameter and params defines how this parameter
 * will be displayed in UI (see BuildWindow(final ParamList)). Using this
 * mapping there is next list \c ui created that contains the same names but now
 * joined with UI components. This list is used for addressing these component
 * basing on theirs names. The UI controls are stored at \c ui which is \a
 * protected and may be used for influencing these controls by user. To identify
 * certain UI control its name is required which is the string passed as first
 * dimension of \c def definition passed to to BuildWindow method. Below code
 * shows how to change property of control
 * 
 * @code{.java}
 * String key = "paramname"; // case insensitive
 * JSpinner comp = (JSpinner) ui.get(key); // get control using its name 
 * comp.getEditor()).getTextField().setColumns(5);
 * @endcode
 * 
 * The basic usage pattern is as follows:
 * 
 * @msc
 * hscale="1";
 * Caller,QWindowBuilder,AWTWindow;
 * Caller=>QWindowBuilder [label="BuildWindow(..)"];
 * Caller=>QWindowBuilder [label="ShowWindow(true)"];
 * QWindowBuilder->AWTWindow [label="Show AWT window"];
 * --- [label="Window is displayed"];
 * Caller=>QWindowBuilder [label="setValues(..)"];
 * QWindowBuilder->AWTWindow [label="update UI"];
 * Caller=>QWindowBuilder [label="getValues()"];
 * QWindowBuilder->AWTWindow [label="ask for values"];
 * AWTWindow>>QWindowBuilder [label="ParamList"]; 
 * Caller<<QWindowBuilder [label="UI values"];
 * --- [label="Ask for one value associated with name"];
 * Caller=>QWindowBuilder [label="getDoublefromUI(name)"];
 * QWindowBuilder=>QWindowBuilder [label="getVales()"];
 * QWindowBuilder=>QWindowBuilder [label="find name in ParamList"];
 * Caller<<QWindowBuilder [label="value"];
 * @endmsc
 * 
 * Methods getValues() and setValues() should be used by class extending
 * QWindowBuilder for setting and achieving parameters from GUI. Note
 * that parameters in UIs are validated only when they become out of
 * focus. Until cursor is in UI its value is not updated internally,
 * thus getValues() returns its old snapshot.
 * 
 * \c RESERVED_KEYS is list of reserved keys that are not UI elements. They are
 * processed in different way.
 * 
 * @warning UI type as JSpinner keeps data in double format even in values passed through by
 * setValues(ParamList) are integer (ParamList keeps data as String). Therefore getValues can 
 * return this list with the same data but in double syntax (5 -> 5.0). Any try of convention of
 *  "5.0" to integer value will cause NumberFormatException. To avoid this problem use 
 *  QuimP.plugin.ParamList.getIntValue(String) from ParamList of treat all strings in ParamList as
 *  Double.
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 */
public abstract class QWindowBuilder {
    final protected static Logger LOGGER = LogManager.getLogger(QWindowBuilder.class.getName());
    protected JFrame pluginWnd; //!< main window object
    protected boolean windowState; //!< current window state \c true if visible
    protected JPanel pluginPanel; //!< Main panel extended on whole \c pluginWnd
    protected ComponentList ui; //!< list of all UI elements
    private ParamList def; //!< definition of window and parameters

    final private HashSet<String> RESERVED_KEYS = new HashSet<String>(
            Arrays.asList(new String[] { "help", "name" })); //!< reserved keys

    // definition string - positions of configuration data in value string (see
    // BuildWindow)
    final private int UITYPE = 0; //!< type of UI control to create
    final private int S_MIN = 1; //!< spinner min value
    final private int S_MAX = 2; //!< spinner max value
    final private int S_STEP = 3; //!< spinner step value
    final private int S_DEFAULT = 4; //!< spinner default value

    // definition of constant elements of UI
    protected JButton applyB;

    /**
     * Default constructor
     */
    public QWindowBuilder() {
        LOGGER.trace("Entering constructor");
        ui = new ComponentList();
        def = null;
    }

    /**
     * Main window builder
     * 
     * The window is constructed using configuration provided by \c def
     * parameter which is Map of <key,value>. The key is the name of the
     * parameter that should be related to value held in it (e.g window, smooth,
     * step, etc.). The name is not case sensitive. Keys are strictly
     * related to UI elements that are created by this method (basing on
     * configuration passed in \b value). There are two special names that are
     * not related to UI directly: 
     * -# help - defines textual help provided as parameter
     * -# name - defines plugin name provided as parameter 
     * 
     * The parameter list is defined as String and its content is depended on 
     * key.
     * For \b help and \b name it contains single string with help text and
     * plugin name respectively. 
     * 
     * The UI elements are defined for all other cases in \b value filed of Map
     * as comma separated string.
     * Known UI are as follows:
     * <ul>
     * <li>spinner - creates Spinner control. It requires 4 parameters (in
     * order)
     * <ol>
     * <li>minimal range
     * <li>maximal range
     * <li>step
     * <li>default value
     * </ol>
     * </ul>
     *
     * The type of required UI element associated with given parameter name (\a
     * Key) is coded in value of given Key in accordance with list above. The
     * correct order of sub-parameters must be preserved. Exemplary
     * configuration is as follows:
     * 
     * @code{.java}
     *  def1 = new ParamList();
     *  def1.put("Name", "test");
     *  def1.put("wIndow", "spinner, -0.5, 0.5, 0.1, 0");
     *  def1.put("smootH", "spinner, -1, 10, 1, -1");
     *  def1.put("help","FlowLayout"); 
     * @endcode
     * 
     * By default window is not visible yet. User must call ShowWindow
     * or ToggleWindow. The \b Apply button does nothing. It is only to
     * refocus after change of values in spinners. They are not updated
     * until unfocused.
     * 
     * @param def Configuration as described
     * @throw IllegalArgumentException or other unchecked exceptions on wrong
     * syntax of \c def
     */
    public void buildWindow(final ParamList def) {
        if (def.size() < 2)
            throw new IllegalArgumentException(
                    "Window must contain title and" + " at least one control");
        this.def = def; // remember parameters
        ui.clear(); // clear all ui stored on second call of third method

        pluginWnd = new JFrame(); // create frame with title given as first
                                  // position in table
        pluginPanel = new JPanel(); // main panel on whole window
        pluginPanel.setLayout(new BorderLayout()); // divide window on two zones
                                                   // - upper for controls,
                                                   // - middle for help

        Panel north = new Panel(); // panel for controls
        // get layout size
        int siz = def.size(); // total size of Map
        // but grid layout does not contain help and name or other reserved non
        // UI keys
        Set<String> s = def.keySet(); // get Set of keys
        for (String k : RESERVED_KEYS) // and check if any of them is in s
            if (s.contains(k))
                siz--;

        GridLayout gridL = new GridLayout(siz, 2); // Nx2, by default in row we
                                                   // have control and its
                                                   // description
        north.setLayout(gridL);
        gridL.setVgap(5); // set bigger gaps
        gridL.setHgap(5);

        // iterate over def entries except first one which is always title
        // every decoded control is put into ordered hashmap together with its
        // descriptor (label)
        for (Map.Entry<String, String> e : def.entrySet()) {
            String key = e.getKey();
            if (RESERVED_KEYS.contains(key))
                continue;
            String[] uiparams = StringParser.getParams(e.getValue());
            if (uiparams.length == 0)
                throw new IllegalArgumentException("Probably wrong syntax in UI definition");
            switch (uiparams[UITYPE].toLowerCase()) {
                case "spinner": // by default all spinners are double
                    if (uiparams.length != 5) // 4+uitype
                        throw new IllegalArgumentException(
                                "Probably wrong syntax in UI definition for " + uiparams[UITYPE]);
                    SpinnerNumberModel model =
                            new SpinnerNumberModel(Double.parseDouble(uiparams[S_DEFAULT]), // val
                                    Double.parseDouble(uiparams[S_MIN]), // min
                                    Double.parseDouble(uiparams[S_MAX]), // max
                                    Double.parseDouble(uiparams[S_STEP])); // step
                    ui.put(key, new JSpinner(model));
                    ui.put(key + "label", new Label(key)); // add description
                    break;
                default:
                    // wrong param syntax
                    throw new IllegalArgumentException("Unknown ui type" + " provided: " + key);
            }
        }

        // iterate over all components and add them to grid layout
        for (Map.Entry<String, Component> me : ui.entrySet())
            north.add(me.getValue());

        // add non ui elements
        if (def.containsKey("name")) {
            // border on whole window
            pluginPanel.setBorder(BorderFactory.createTitledBorder("Plugin " + def.get("name")));
        }
        if (def.containsKey("help")) {
            JTextArea helpArea = new JTextArea(10, 10); // default size of text
                                                        // area
            JScrollPane helpPanel = new JScrollPane(helpArea);
            helpArea.setEditable(false);
            pluginPanel.add(helpPanel, BorderLayout.CENTER); // locate at center
                                                             // position
            helpArea.setText(def.get("help")); // set help text
            helpArea.setLineWrap(true); // with wrapping
        }

        // add Apply button on south
        Panel south = new Panel();
        applyB = new JButton("Apply");
        south.add(applyB);

        // build window
        pluginPanel.add(north, BorderLayout.NORTH);
        pluginPanel.add(south, BorderLayout.SOUTH);
        pluginWnd.add(pluginPanel);
        pluginWnd.pack();
        // add listener on close
        pluginWnd.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                windowState = false;
                pluginWnd.setVisible(false);
            }
        });
        windowState = false; // by default window is not visible. User must call
                             // ShowWindow or ToggleWindow
    }

    /**
     * Show or hide window
     * 
     * @param state
     * State of the window \c true to show, \c false to hide
     */
    public void showWindow(boolean state) {
        pluginWnd.setVisible(state);
        windowState = state;
    }

    /**
     * Toggle window visibility
     * 
     * @return Current status of window \c true if visible, \c false if not
     */
    public boolean toggleWindow() {
        windowState = !windowState;
        showWindow(windowState);
        return windowState;
    }

    /**
     * Check if window is visible
     * 
     * @return \c true if it is visible, \c false otherwise
     */
    public boolean isWindowVisible() {
        return windowState;
    }

    /**
     * Set plugin parameters.
     * 
     * Use the same parameters names as in BuildWindow(Map<String, String[]>).
     * The name of the parameter is \a key in Map. Every parameter passed to
     * this method must have its representation in GUI and thus it must be
     * present in \c def parameter of BuildWindow(Map<String, String[]>) All
     * values are passed as:
     * <ol>
     * <li>\c Double in case of spinners
     * </ol>
     * 
     * User has to care for correct format passed to UI control. If input values
     * are above range defined in \c def, new range is set for UI control
     * 
     * @param vals <key,value> pairs to fill UI.
     */
    public void setValues(final ParamList vals) {
        // iterate over parameters and match names to UIs
        for (Map.Entry<String, String> e : vals.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            // find key in def and get type of control and its instance
            switch (def.getParsed(key)[UITYPE]) { // first string in vals is type of
                // control, see BuildWindow
                case "spinner":
                    JSpinner comp = (JSpinner) ui.get(key); // get UI component of name key (keys
                                                            // in vals must match to keys in
                                                            // BuildWindow(def))
                    comp.setValue(Double.parseDouble(val)); // set value from vals
                    SpinnerNumberModel sm = (SpinnerNumberModel) comp.getModel();
                    if (sm.getNextValue() == null)
                        sm.setMaximum(Double.parseDouble(val));
                    else if (sm.getPreviousValue() == null)
                        sm.setMinimum(Double.parseDouble(val));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown UI type in setValues");
            }
        }
    }

    /**
     * Receives parameters related to UI elements as Map.
     * 
     * To get one particular parameter use getIntegerFromUI(String) or
     * getDoubleFromUI(String)
     * 
     * @return List of <key,param>, where \b key is the name of parameter passed
     * to QWindowBuilder class through BuildWindow method. The method
     * remaps those keys to related UI controls and reads values
     * associated to them.
     * @see getDoubleFromUI(final String)
     * @see getIntegerFromUI(final String)
     * @warning JSpinners are set to support \b double values and that values are returned here
     * It means that originally pushed to UI integers are changed to Double what can affect 
     * set/getpluginConfig from filter interface as well
     */
    public ParamList getValues() {
        ParamList ret = new ParamList();
        // iterate over all UI elements
        Iterator<Map.Entry<String, Component>> entryIterator = ui.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Component> m = entryIterator.next();
            String key = m.getKey();
            // check type of component
            switch (def.getParsed(key)[UITYPE]) {
                case "spinner":
                    JSpinner val = (JSpinner) m.getValue(); // get value
                    ret.put(key, String.valueOf(val.getValue())); // store it in returned Map at
                    // the same key
                    break;
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
     * Value is retrieved from ui element related to given \b key. Relation
     * between keys and ui elements is defined by user in configuration list
     * provided to BuildWindow().
     * 
     * @remarks The key must be defined and exists in that list.
     * @remarks In case of wrong conversion it may be exception thrown. User is
     * responsible to call this method for proper key.
     * 
     * @param key Key to be read from configuration list, case insensitive
     * @return integer representation of value under \c key
     * @see BuildWindow(final Map<String, String[]>)
     */
    public int getIntegerFromUI(final String key) {
        return (int) getDoubleFromUI(key);
    }

    /**
     * Return value related to given key.
     * 
     * @copydoc getIntegerFromUI(final String)
     */
    public double getDoubleFromUI(final String key) {
        // get list of all params from ui as <key,val> list
        ParamList uiParam = getValues();
        return uiParam.getDoubleValue(key);
    }
}
