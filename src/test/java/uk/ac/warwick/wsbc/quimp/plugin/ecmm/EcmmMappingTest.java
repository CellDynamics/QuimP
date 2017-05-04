package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.givesSameJson;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.quimp.utils.graphics.PolarPlotTest;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EcmmMappingTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(PolarPlotTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  static Path tmp = Paths.get(tmpdir);

  /**
   * SetUp.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Generate reference files from segmentation fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCON.
   * 
   * <p>Should be disabled until needed. After enabling copy output files to resource repository
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testGetReferences() throws Exception {
    // only segmentation from BOA
    Path boan = Paths.get(
            "src/test/Resources-static/uk.ac.warwick.wsbc.quimp.plugin.ecmm.EcmmMappingTest",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // copy segmentation to tmp
    Files.copy(boan, tmp.resolve(boan.getFileName()), REPLACE_EXISTING);
    // this is file to process - full path
    File boa = tmp.resolve(boan.getFileName()).toFile();
    new ECMM_Mapping(boa); // it will be updated
    // rename it to ref_xxx
    boa.renameTo(tmp.resolve(Paths.get("ref_" + boan.getFileName())).toFile());
    LOGGER.info("Copy file " + boa.getAbsolutePath() + " to /src/test/Resources-static/"
            + "uk.ac.warwick.wsbc.quimp.plugin.ecmm.EcmmMappingTest/ in resources repo"
            + " and update submodule");
  }

  /**
   * Test method for
   * {@link uk.ac.warwick.wsbc.quimp.plugin.ecmm.ECMM_Mapping#ECMM_Mapping(java.io.File)}.
   * 
   * <p>Compare ECMMState from QCONF with reference.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testECMM_MappingFile() throws Exception {
    // only segmentation
    Path boan = Paths.get(
            "src/test/Resources-static/uk.ac.warwick.wsbc.quimp.plugin.ecmm.EcmmMappingTest",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // reference file
    Path ecmmn = Paths.get(
            "src/test/Resources-static/uk.ac.warwick.wsbc.quimp.plugin.ecmm.EcmmMappingTest",
            "ref_fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // copy segmentation to tmp
    Files.copy(boan, tmp.resolve(boan.getFileName()), REPLACE_EXISTING);
    // this is file to process
    File boa = tmp.resolve(boan.getFileName()).toFile();
    // process it
    new ECMM_Mapping(boa); // it will be updated

    // load reference and updated and compare

    OutlinesCollection ecmmTest = new QconfLoader(boa).getECMM();
    // ecmmTest.oHs.get(0).indexGetOutline(0).freezeAll(); // test of test
    OutlinesCollection ecmmRef = new QconfLoader(ecmmn.toFile()).getECMM();

    assertThat(ecmmTest, givesSameJson(ecmmRef));

  }

}
