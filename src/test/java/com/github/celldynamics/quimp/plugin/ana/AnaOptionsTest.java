package com.github.celldynamics.quimp.plugin.ana;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

/**
 * Test of AnaOptions.
 * 
 * @author p.baniukiewicz
 *
 */
public class AnaOptionsTest {

  /**
   * Test method for {@link com.github.celldynamics.quimp.plugin.ana.AnaOptions#clone()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testClone() throws Exception {
    AnaOptions opts = new AnaOptions();
    opts.channel = 4;
    opts.clearFlu = true;
    opts.fluoResultTable = true;
    opts.fluoResultTableAppend = true;
    opts.normalise = false;
    opts.paramFile = "file.qconf";
    opts.plotOutlines = true;
    opts.sampleAtSame = true;
    AnaOptions cl = (AnaOptions) opts.clone();

    assertThat(EqualsBuilder.reflectionEquals(opts, cl, false), is(true));
  }
}
