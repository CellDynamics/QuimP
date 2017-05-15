package uk.ac.warwick.wsbc.quimp;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class BOAStateTest extends JsonKeyMatchTemplate<BOAState> {

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new BOAState();
    indir = "uk.ac.warwick.wsbc.quimp.BOAState";
  }
}
