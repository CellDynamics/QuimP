package com.github.celldynamics.quimp.omero;

import java.util.ArrayList;
import java.util.List;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/**
 * Abstract dataset with active element.
 * 
 * @author p.baniukiewicz
 * @param <E> Type of dataset ({@link ImageData}, {@link DatasetData})
 *
 */
class AbstractDataSet<E> {

  /**
   * Opened datasets.
   */
  public List<E> ds; // opened datasets
  /**
   * Index of active dataset from {@link #ds}.
   */
  public int currentEl;

  public AbstractDataSet() {
    ds = new ArrayList<>();
    currentEl = -1;
  }

  public void clear() {
    ds.clear();
    currentEl = -1;
  }

  /**
   * Validate if dataset and current element are valid.
   * 
   * @return true if dataset contains data and current element >= 0
   */
  public boolean validate() {
    if (ds.isEmpty() || currentEl < 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Get current element.
   * 
   * @return curent element
   * @see AbstractDataSet#validate()
   */
  public E getCurrent() {
    return ds.get(currentEl);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (validate()) {
      return "Image [ds=" + ds.get(currentEl) + "]";
    } else {
      return "not valid image";
    }
  }

}
