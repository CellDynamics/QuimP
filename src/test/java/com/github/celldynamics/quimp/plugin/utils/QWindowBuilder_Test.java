package com.github.celldynamics.quimp.plugin.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

// TODO: Auto-generated Javadoc
/**
 * Test class for QWindowBuilder
 * 
 * @author p.baniukiewicz
 *
 */
public class QWindowBuilder_Test {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QWindowBuilder_Test.class.getName());
  private ParamList def1;

  /**
   * The inst.
   */
  QWindowBuilderInst inst;

  /**
   * Instance private class for tested QWindowBuilder
   * 
   * @author p.baniukiewicz
   *
   */
  class QWindowBuilderInst extends QWindowBuilder {
  }

  /**
   * The name.
   */
  @Rule
  public TestName name = new TestName(); // !< Allow to get tested method name (called at setUp())

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    def1 = new ParamList(); // setup window params
    def1.put("Name", "test");
    def1.put("wIndow", "spinner, -0.5, 0.5, 0.1, 0");
    def1.put("smootH", "spinner, -1, 10, 1, -1");
    def1.put("Selector", "choice, option_1, option_2, option_3");
    def1.put("help",
            "FlowLayout is the default layout manager for every JPanel."
                    + " It simply lays out components in a single row, starting a"
                    + " new row if its container is not sufficiently wide. Both "
                    + "panels in CardLayoutDemo, shown previously, use FlowLayout."
                    + " For further details, see How to Use FlowLayout.");
    inst = new QWindowBuilderInst(); // create window object
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    def1.clear();
    def1 = null;
    inst = null;
  }

  /**
   * getValues Get default values from window.
   * 
   * <p>pre: def1 config string
   * 
   * <p>post: default values are lower bounds for defined ui controls
   */
  @Test
  public void test_getValues() {
    ParamList ret;
    inst.buildWindow(def1);
    ret = inst.getValues();
    assertEquals(0, ret.getDoubleValue("window"), 1e-4);
    assertEquals(-1, ret.getDoubleValue("smooth"), 1e-4);
    assertEquals("option_1", ret.getStringValue("selector"));
  }

  /**
   * setgetValues Get previously set values from window.
   * 
   * <p>pre: values for two ui are set
   * 
   * <p>post: set values are received
   */
  @Test
  public void test_setgetValues() {
    ParamList ret;
    ParamList set = new ParamList();
    set.put("windOw", String.valueOf(0.32));
    set.put("sMooth", String.valueOf(7.0));
    set.put("selector", "option_3"); // must exist in list
    inst.buildWindow(def1);
    inst.setValues(set);
    ret = inst.getValues();
    assertEquals(0.32, ret.getDoubleValue("window"), 1e-4);
    assertEquals(7, ret.getDoubleValue("smooth"), 1e-4);
    assertEquals("option_3", ret.getStringValue("selector"));
  }

}
