package com.github.celldynamics.quimp.plugin.protanalysis;

import com.github.celldynamics.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisOptionsTest extends JsonKeyMatchTemplate<ProtAnalysisOptions> {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new ProtAnalysisOptions();
    indir = "com.github.celldynamics.quimp.filesystem.ProtAnalysisConfig";
  }
}
