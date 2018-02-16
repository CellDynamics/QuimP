package com.github.celldynamics.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

// TODO: Auto-generated Javadoc
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

  /**
   * Test method for {@link com.github.celldynamics.quimp.Nest#allFrozen()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testAllFrozen() throws Exception {
    assertThat(obj.allFrozen(), is(true));
  }

}
