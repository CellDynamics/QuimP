/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Extension of LinkedHashMap that assumes that Key is String are case insensitive.
 * 
 * All keys are converted to lower case.
 * 
 * @author p.baniukiewicz
 *
 */
public class LinkedStringMap<V> extends LinkedHashMap<String, V> {

    private static final long serialVersionUID = -8577387803055420569L;

    public LinkedStringMap() {
    }

    /**
     * @param initialCapacity
     */
    public LinkedStringMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param m
     */
    public LinkedStringMap(Map<? extends String, ? extends V> m) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * @param initialCapacity
     * @param loadFactor
     */
    public LinkedStringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @param initialCapacity
     * @param loadFactor
     * @param accessOrder
     */
    public LinkedStringMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#get(java.lang.Object)
     */
    public V get(String key) {
        return super.get(key.toLowerCase());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#containsKey(java.lang.Object)
     */
    public boolean containsKey(String key) {
        return super.containsKey(key.toLowerCase());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        for (Map.Entry<? extends String, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    public V remove(String key) {
        return super.remove(key.toLowerCase());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#putIfAbsent(java.lang.Object, java.lang.Object)
     */
    @Override
    public V putIfAbsent(String key, V value) {
        return super.putIfAbsent(key.toLowerCase(), value);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object, java.lang.Object)
     */
    public boolean remove(String key, Object value) {
        return super.remove(key.toLowerCase(), value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean replace(String key, V oldValue, V newValue) {
        return super.replace(key.toLowerCase(), oldValue, newValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#replace(java.lang.Object, java.lang.Object)
     */
    @Override
    public V replace(String key, V value) {
        return super.replace(key.toLowerCase(), value);
    }

    /**
     * This method is not supported.
     * 
     * @see java.util.HashMap#replaceAll(java.util.function.BiFunction)
     */
    @Override
    public void replaceAll(BiFunction<? super String, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException("not supported");
    }

}
