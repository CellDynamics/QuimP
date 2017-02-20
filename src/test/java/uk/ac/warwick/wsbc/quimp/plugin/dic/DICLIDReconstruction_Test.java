package uk.ac.warwick.wsbc.quimp.plugin.dic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class DICLIDReconstruction_Test {
    private DICLIDReconstruction_ inst;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        inst = new DICLIDReconstruction_();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.dic.DICLIDReconstruction_#showUI(boolean)}.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testShowDialog() throws Exception {
        new DICLIDReconstruction_().showUI(true);
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.dic.DICLIDReconstruction_#roundtofull(double)}.
     * 
     * @throws Exception
     */
    @Test
    public void testRoundtofull() throws Exception {
        String ret = inst.roundtofull(0);
        assertThat(ret, is("0"));

        ret = inst.roundtofull(90);
        assertThat(ret, is("90"));

        ret = inst.roundtofull(45);
        assertThat(ret, is("45"));

        ret = inst.roundtofull(135);
        assertThat(ret, is("135"));

        ret = inst.roundtofull(180);
        assertThat(ret, is("0"));

        ret = inst.roundtofull(225);
        assertThat(ret, is("45"));

        ret = inst.roundtofull(270);
        assertThat(ret, is("90"));

        ret = inst.roundtofull(20);
        assertThat(ret, is("0"));

        ret = inst.roundtofull(50);
        assertThat(ret, is("45"));

        ret = inst.roundtofull(80);
        assertThat(ret, is("90"));

        ret = inst.roundtofull(100);
        assertThat(ret, is("90"));

        ret = inst.roundtofull(125);
        assertThat(ret, is("135"));

        ret = inst.roundtofull(141);
        assertThat(ret, is("135"));

        ret = inst.roundtofull(160);
        assertThat(ret, is("0"));

    }

}
