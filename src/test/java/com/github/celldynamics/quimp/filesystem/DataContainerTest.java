package com.github.celldynamics.quimp.filesystem;

import com.github.celldynamics.quimp.JsonKeyMatchTemplate;

// TODO: Auto-generated Javadoc
/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class DataContainerTest extends JsonKeyMatchTemplate<DataContainer> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new DataContainer();
    indir = "com.github.celldynamics.quimp.filesystem.DataContainer";
  }
}
