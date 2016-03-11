import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DIC_LID_ReconstructionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * @test Test of GUI builder for DIC_LID_Reconstruction
     * @post Shows GUI and expect correct inputs (numbers) and OK
     */
    @Test
    public void TestBuildGUI() {
        DIC_LID_Reconstruction dic = new DIC_LID_Reconstruction();
        assertTrue(dic.showDialog());
    }

}
