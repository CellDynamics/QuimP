package uk.ac.warwick.wsbc.quimp;

/**
 * Verify keys in JSon for tested class (field naming).
 * 
 * @author p.baniukiewicz
 *
 */
public class NestTest extends JsonKeyMatchTemplate<Nest> {

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.JsonKeyMatchTemplate#setUp()
   */
  @Override
  public void setUp() throws Exception {
    obj = new Nest();
    indir = "uk.ac.warwick.wsbc.quimp.Nest";
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
