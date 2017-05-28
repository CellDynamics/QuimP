package com.github.celldynamics.quimp;

import com.github.celldynamics.quimp.BOAState;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class BOAStateTest extends JsonKeyMatchTemplate<BOAState> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new BOAState();
    indir = "com.github.celldynamics.quimp.BOAState";
  }
}
