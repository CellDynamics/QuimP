package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.plugin.utils.ImageProcessorPlus.GenerateKernel;

/**
 * @author p.baniukiewicz
 *
 */
public class ImageProcessorPlusTest {

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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_GenerateKernel() throws Exception {
        {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.2f, 0.2f, 0.2f, 0.2f, 0.2f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f
                    };
         //!<   
            GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
            assertThat(gk.generateKernel("0"), is(exp));
        }
        {//!>
            float[] exp = {
                    0.2f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.2f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.2f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.2f
                    };
         //!<   
            GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
            assertThat(gk.generateKernel("45"), is(exp));
        }
        {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f
                    };
         //!<   
            GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
            assertThat(gk.generateKernel("90"), is(exp));
        }
        {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.0f, 0.0f, 0.2f,
                    0.0f, 0.0f, 0.0f, 0.2f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.2f, 0.0f, 0.0f, 0.0f,
                    0.2f, 0.0f, 0.0f, 0.0f, 0.0f
                    };
         //!<   
            GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
            assertThat(gk.generateKernel("135"), is(exp));
        }
    }
}
