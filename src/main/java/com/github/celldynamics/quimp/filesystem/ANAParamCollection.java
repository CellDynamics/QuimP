package com.github.celldynamics.quimp.filesystem;

import java.util.ArrayList;

import com.github.celldynamics.quimp.plugin.ana.ANAp;

/**
 * Serialization container for {@link com.github.celldynamics.quimp.plugin.ana.ANAp}.
 * 
 * @author p.baniukiewicz
 *
 */
public class ANAParamCollection implements IQuimpSerialize {
  /**
   * Array of configuration options for every cell present in the image.
   */
  public ArrayList<ANAp> aS;

  /**
   * Default constructor.
   * 
   * <p>Create empty store for {@link ANAp} configurations.
   */
  public ANAParamCollection() {
    aS = new ArrayList<>();
  }

  /**
   * Create <tt>size</tt> elements in store for {@link ANAp} configurations.
   * 
   * <p>Size of the store usually equals to the number of cells in the image.
   * 
   * @param size size of the collection
   */
  public ANAParamCollection(int size) {
    aS = new ArrayList<ANAp>(size);
    for (int i = 0; i < size; i++) {
      aS.add(new ANAp());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {

  }

}
