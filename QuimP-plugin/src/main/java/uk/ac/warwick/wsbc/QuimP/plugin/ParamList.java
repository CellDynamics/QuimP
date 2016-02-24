/**
 * @file ParamList.java
 * @date 24 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import uk.ac.warwick.wsbc.QuimP.plugin.utils.StringParser;

/**
 * List of parameters in <key,value> HashList, where both \c key and \c value
 * are java.lang.String and key is always in lower case.
 * 
 * @author p.baniukiewicz
 * @date 24 Feb 2016
 *
 */
public class ParamList extends LinkedHashMap<String, String> {

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
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        return super.put(key.toLowerCase(), value);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        return super.get(((String) key).toLowerCase());
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
    public String[] getParsed(Object key) {
        String val = get(key);
        String[] ret;
        try {
            ret = StringParser.getParams(val);
        } catch (Exception e) {
            ret = new String[0];
        }
        return ret;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(((String) key).toLowerCase());
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (java.util.Map.Entry<? extends String, ? extends String> e : m
                .entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        return super.remove(((String) key).toLowerCase());
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#putIfAbsent(java.lang.Object, java.lang.Object)
     */
    @Override
    public String putIfAbsent(String key, String value) {
        return super.putIfAbsent(key.toLowerCase(), value);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(((String) key).toLowerCase(), value);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#replace(java.lang.Object, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean replace(String key, String oldValue, String newValue) {
        return super.replace(key.toLowerCase(), oldValue, newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#replace(java.lang.Object, java.lang.Object)
     */
    @Override
    public String replace(String key, String value) {
        return super.replace(key.toLowerCase(), value);
    }

    /**
     * This method is not supported
     * 
     * @see java.util.HashMap#replaceAll(java.util.function.BiFunction)
     */
    @Override
    public void replaceAll(
            BiFunction<? super String, ? super String, ? extends String> function) {
        throw new UnsupportedOperationException("not supported");
    }

}
