package com.github.celldynamics.quimp.filesystem;

import com.github.celldynamics.quimp.JsonKeyMatchTemplate;

// TODO: Auto-generated Javadoc
/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class OutlinesCollectionTest extends JsonKeyMatchTemplate<OutlinesCollection> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new OutlinesCollection();
    indir = "com.github.celldynamics.quimp.filesystem.OutlinesCollection";
  }
}