package uk.ac.warwick.wsbc.quimp;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class OutlineHandlerTest extends JsonKeyMatchTemplate<OutlineHandler> {

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new OutlineHandler();
    indir = "uk.ac.warwick.wsbc.quimp.OutlineHandler";
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#prepare()
   */
  @Override
  protected void prepare() throws Exception {
    ser.doBeforeSerialize = false;
    super.prepare();
  }

}
