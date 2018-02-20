package com.github.celldynamics.quimp.plugin.qanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

/**
 * Test of {@link Qp}.
 * 
 * @author p.baniukiewicz
 *
 */
public class QpTest {

  /**
   * Test method for {@link com.github.celldynamics.quimp.plugin.qanalysis.QanalysisOptions}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testClone() throws Exception {
    EnhancedRandom eh =
            EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomizationDepth(1).build();
    Qp opts = eh.nextObject(Qp.class);
    opts.stQPfile = new File("dfgsasw");
    opts.outFile = new File("dfgsasw");
    Qp cl = (Qp) opts.clone();

    assertThat(EqualsBuilder.reflectionEquals(opts, cl, false), is(true));
  }

  /**
   * Test method for {@link QanalysisOptions#QanalysisOptions(java.io.File)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testQanalysisOptionsFile() throws Exception {
    Qp opts = new Qp(new File("/test/file/path.Qconf"));
    assertThat(opts.paramFile, is("/test/file/path.Qconf"));
  }
}
