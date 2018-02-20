package com.github.celldynamics.quimp.plugin.ecmm;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

/**
 * Test of ECMMOptions.
 * 
 * @author p.baniukiewicz
 *
 */
public class EcmmOptionsTest {

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.ecmm.EcmmOptions#EcmmOptions(java.io.File)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testEcmmOptionsFile() throws Exception {
    EcmmOptions opts = new EcmmOptions(new File("/test/file/path.Qconf"));
    assertThat(opts.paramFile, is("/test/file/path.Qconf"));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.plugin.ecmm.EcmmOptions#clone()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testClone() throws Exception {
    EcmmOptions opts = new EcmmOptions(new File("/test/file/path.Qconf"));
    EcmmOptions cl = (EcmmOptions) opts.clone();

    assertThat(EqualsBuilder.reflectionEquals(opts, cl, false), is(true));
  }

}
