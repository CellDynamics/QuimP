package uk.ac.warwick.wsbc.quimp.plugin.qanalysis;

import uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class STmapTest extends JsonKeyMatchTemplate<STmap> {

  /**
   * Just call parametrised constructor from test default.
   */
  public STmapTest() {
    super(1, false); // go deeper than usually
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new STmap();
    indir = "uk.ac.warwick.wsbc.quimp.filesystem.STmap";
  }
}