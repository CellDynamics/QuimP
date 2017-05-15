package uk.ac.warwick.wsbc.quimp.filesystem;

import uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsCollectionTest extends JsonKeyMatchTemplate<StatsCollection> {

  /**
   * Just call parametrised constructor from test default.
   */
  public StatsCollectionTest() {
    super(2, false); // go deeper than usually
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new StatsCollection();
    indir = "uk.ac.warwick.wsbc.quimp.filesystem.StatsCollection";
  }
}