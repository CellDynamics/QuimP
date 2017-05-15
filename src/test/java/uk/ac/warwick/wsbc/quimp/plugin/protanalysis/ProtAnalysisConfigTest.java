package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisConfigTest extends JsonKeyMatchTemplate<ProtAnalysisConfig> {

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new ProtAnalysisConfig();
    indir = "uk.ac.warwick.wsbc.quimp.filesystem.ProtAnalysisConfig";
  }
}
