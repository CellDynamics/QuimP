package com.github.celldynamics.quimp.plugin.randomwalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

/**
 * Store objects (maps) under specified key. There are many maps allowed under one key.
 * 
 * <p>For particular key, maps are stored in ArrayList structure.
 * 
 * @author p.baniukiewicz
 * @param <T> type of map stored
 *
 */
@SuppressWarnings("serial")
public class ListMap<T> extends HashMap<SeedTypes, List<T>> {

  /**
   * Default constructor.
   */
  public ListMap() {
    super();
  }

  /**
   * Constructor with initial capacity.
   * 
   * @param initialCapacity initial capacity
   */
  public ListMap(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Store specified map in this structure.
   * 
   * <p>This method does not verify if this particular map is already present.
   * 
   * @param key key to store map under, will be created if necessary
   * @param val map to store under specified key
   * @return List of maps where specified seed map has been stored
   */
  public List<T> put(SeedTypes key, T val) {
    List<T> loc = get(key);
    if (loc != null) {
      loc.add(val);
    } else {
      put(key, new ArrayList<>());
      loc = get(key);
      loc.add(val);
    }
    return loc;
  }
}
