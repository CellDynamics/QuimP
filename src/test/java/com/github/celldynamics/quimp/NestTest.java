package com.github.celldynamics.quimp;

import com.github.celldynamics.quimp.Nest;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class NestTest extends JsonKeyMatchTemplate<Nest> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new Nest();
    indir = "com.github.celldynamics.quimp.Nest";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#prepare()
   */
  @Override
  protected void prepare() throws Exception {
    ser.doBeforeSerialize = false;
    super.prepare();
  }

}
