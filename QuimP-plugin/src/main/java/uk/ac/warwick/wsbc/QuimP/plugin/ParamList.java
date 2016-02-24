/**
 * @file ParamList.java
 * @date 24 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import java.util.Map;

import uk.ac.warwick.wsbc.QuimP.plugin.utils.LinkedStringMap;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.StringParser;

/**
 * List of parameters in <key,value> HashList, where both \c key and \c value
 * are java.lang.String and key is case insensitive.
 * 
 * @author p.baniukiewicz
 * @date 24 Feb 2016
 *
 */
public class ParamList extends LinkedStringMap<String> {

    private static final long serialVersionUID = -8762132735734951785L;

    public ParamList() {
    }

    /**
     * @param initialCapacity
     * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html#put(K,%20V)
     */
    public ParamList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param m
     * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html#put(K,%20V)
     */
    public ParamList(Map<? extends String, ? extends String> m) {
        super(m);
    }

    /**
     * @param initialCapacity
     * @param loadFactor
     * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html#put
     * (K,%20V)
     */
    public ParamList(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Store Integer value in List. Key is not case sensitive
     * 
     * @param key name of key
     * @param value to store
     */
    public void setIntValue(String key, int value) {
        put(key, String.valueOf(value));
    }

    /**
     * Store Double value in List. Key is not case sensitive
     * 
     * @param key name of key
     * @param value to store
     */
    public void setDoubleValue(String key, double value) {
        put(key, String.valueOf(value));
    }

    /**
     * Store String value in List. Key is not case sensitive
     * 
     * @param key name of key
     * @param value to store
     */
    public void setStringValue(String key, String value) {
        put(key, value);
    }

    /**
     * Store Boolean value in List. Key is not case sensitive
     * 
     * @param key name of key
     * @param value to store
     */
    public void setBooleanValue(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    /**
     * Get Integer value from list associated with \c key. Key is not case
     * sensitive
     * 
     * @param key name of key
     * @return associated value
     */
    public int getIntValue(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Get Double value from list associated with \c key. Key is not case
     * sensitive
     * 
     * @param key name of key
     * @return associated value
     */
    public double getDoubleValue(String key) {
        return Double.parseDouble(get(key));
    }

    /**
     * Get String value from list associated with \c key. Key is not case
     * sensitive
     * 
     * @param key name of key
     * @return associated value
     */
    public String getStringValue(String key) {
        return get(key);
    }

    /**
     * Get Boolean value from list associated with \c key. Key is not case
     * sensitive
     * 
     * @param key name of key
     * @return associated value
     */
    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Get string associated with \c key and parse it to split according to 
     * delimiter
     * 
     * @param key to be read
     * @return Split substrings or empty array in case of any error
     * @see StringParser
     * @warning May be used only \c val under \c key can be parsed
     */
    public String[] getParsed(String key) {
        String val = get(key);
        String[] ret;
        try {
            ret = StringParser.getParams(val);
        } catch (Exception e) {
            ret = new String[0];
        }
        return ret;
    }

}
