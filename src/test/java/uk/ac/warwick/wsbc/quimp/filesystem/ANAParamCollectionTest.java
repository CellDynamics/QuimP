package uk.ac.warwick.wsbc.quimp.filesystem;

import uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class ANAParamCollectionTest extends JsonKeyMatchTemplate<ANAParamCollection> {

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new ANAParamCollection();
    indir = "uk.ac.warwick.wsbc.quimp.filesystem.ANAParamCollection";
  }
}
