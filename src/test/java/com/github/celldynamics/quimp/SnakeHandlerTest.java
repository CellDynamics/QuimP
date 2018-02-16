package com.github.celldynamics.quimp;

import com.github.celldynamics.quimp.SnakeHandler;

// TODO: Auto-generated Javadoc
/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class SnakeHandlerTest extends JsonKeyMatchTemplate<SnakeHandler> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new SnakeHandler();
    indir = "com.github.celldynamics.quimp.SnakeHandler";
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
