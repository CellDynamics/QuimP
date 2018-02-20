package com.github.celldynamics.quimp.plugin.qanalysis;

import com.github.celldynamics.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class STmapTest extends JsonKeyMatchTemplate<STmap> {

  /**
   * Just call parametrised constructor from test default.
   */
  public STmapTest() {
    super(1, false); // go deeper than usually
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new STmap();
    indir = "com.github.celldynamics.quimp.filesystem.STmap";
  }
}