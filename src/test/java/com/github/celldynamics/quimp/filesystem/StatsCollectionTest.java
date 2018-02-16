package com.github.celldynamics.quimp.filesystem;

import com.github.celldynamics.quimp.JsonKeyMatchTemplate;

// TODO: Auto-generated Javadoc
/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsCollectionTest extends JsonKeyMatchTemplate<StatsCollection> {

  /**
   * Just call parametrised constructor from test default.
   */
  public StatsCollectionTest() {
    super(2, false); // go deeper than usually
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new StatsCollection();
    indir = "com.github.celldynamics.quimp.filesystem.StatsCollection";
  }
}