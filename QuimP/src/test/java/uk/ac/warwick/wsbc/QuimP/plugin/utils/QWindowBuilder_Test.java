/**
 * @file QWindowBuilder_Test.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;

/**
 * Test class for QWindowBuilder
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class QWindowBuilder_Test {
    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(QWindowBuilder_Test.class.getName());
    private ParamList def1;
    QWindowBuilderInst inst;

    /**
     * Instance private class for tested QWindowBuilder
     * 
     * @author p.baniukiewicz
     * @date 2 Feb 2016
     *
     */
    class QWindowBuilderInst extends QWindowBuilder {
    }

    @Rule
    public TestName name = new TestName(); /// < Allow to get tested method name
                                           /// (called at setUp())

    @Before
    public void setUp() throws Exception {
        def1 = new ParamList(); // setup window params
        def1.put("Name", "test");
        def1.put("wIndow", "spinner, -0.5, 0.5, 0.1, 0");
        def1.put("smootH", "spinner, -1, 10, 1, -1");
        def1.put("help",
                "FlowLayout is the default layout manager for every JPanel."
                        + " It simply lays out components in a single row, starting a"
                        + " new row if its container is not sufficiently wide. Both "
                        + "panels in CardLayoutDemo, shown previously, use FlowLayout."
                        + " For further details, see How to Use FlowLayout.");
        inst = new QWindowBuilderInst(); // create window object
    }

    @After
    public void tearDown() throws Exception {
        def1.clear();
        def1 = null;
        inst = null;
    }

    /**
     * @test getValues Get default values from window
     * @pre \c def1 config string
     * @post default values are lower bounds for defined ui controls
     */
    @Test
    public void test_getValues() {
        ParamList ret;
        inst.buildWindow(def1);
        ret = inst.getValues();
        assertEquals(0, ret.getDoubleValue("window"), 1e-4);
        assertEquals(-1, ret.getDoubleValue("smooth"), 1e-4);
    }

    /**
     * @test setgetValues Get previously set values from window
     * @pre \c values for two ui are set
     * @post set values are received
     */
    @Test
    public void test_setgetValues() {
        ParamList ret;
        ParamList set = new ParamList();
        set.put("windOw", String.valueOf(0.32));
        set.put("sMooth", String.valueOf(7.0));
        inst.buildWindow(def1);
        inst.setValues(set);
        ret = inst.getValues();
        assertEquals(0.32, ret.getDoubleValue("window"), 1e-4);
        assertEquals(7, ret.getDoubleValue("smooth"), 1e-4);
    }

}
