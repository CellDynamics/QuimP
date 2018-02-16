package com.github.celldynamics.quimp.plugin.ecmm;

import static com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers.containsExactText;
import static com.github.celldynamics.quimp.utils.test.matchers.file.QuimpFileMatchers.givesSameJson;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;

import ij.ImageJ;

/**
 * EcmmMappingTest.
 * 
 * <p>Requires registered Quimp in IJ_Prefs.txt.
 * 
 * @author p.baniukiewicz
 *
 */
public class EcmmMappingTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(EcmmMappingTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  static Path tmp = Paths.get(tmpdir);

  private ImageJ ij;

  /**
   * SetUp.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    ij = new ImageJ(); // for prefs and registration
  }

  /**
   * clean.
   * 
   * @throws Exception Exception
   */
  @After
  public void clean() throws Exception {
    ij.exitWhenQuitting(true); // for prefs and registration
    ij.dispose();
    ij = null;

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
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // copy segmentation to tmp
    Files.copy(boan, tmp.resolve(boan.getFileName()), REPLACE_EXISTING);
    // this is file to process - full path
    File boa = tmp.resolve(boan.getFileName()).toFile();
    ECMM_Mapping ob = new ECMM_Mapping(boa); // it will be updated
    ob.run("");
    // rename it to ref_xxx
    boa.renameTo(tmp.resolve(Paths.get("ref_" + boan.getFileName())).toFile());
    LOGGER.info("Copy file " + boa.getAbsolutePath() + " to /src/test/Resources-static/"
            + "com.github.celldynamics.quimp.plugin.ecmm.EcmmMappingTest/ in resources repo"
            + " and update submodule");

    // only segmentation from BOA
    Path boapa = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.paQP");
    Path boasn = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.snQP");
    // copy segmentation to tmp
    Files.copy(boapa, tmp.resolve(boapa.getFileName()), REPLACE_EXISTING);
    // copy segmentation to tmp
    Files.copy(boasn, tmp.resolve(boasn.getFileName()), REPLACE_EXISTING);
    // this is file to process - full path
    File ps = tmp.resolve(boapa.getFileName()).toFile();
    File sn = tmp.resolve(boasn.getFileName()).toFile();
    new ECMM_Mapping(ps); // it will be updated
    // rename it to ref_xxx
    ps.renameTo(tmp.resolve(Paths.get("ref_" + boapa.getFileName())).toFile());
    sn.renameTo(tmp.resolve(Paths.get("ref_" + boasn.getFileName())).toFile());
    LOGGER.info("Copy file " + sn.getAbsolutePath() + " and " + ps.getAbsolutePath()
            + " to /src/test/Resources-static/"
            + "com.github.celldynamics.quimp.plugin.ecmm.EcmmMappingTest/ in resources repo"
            + " and update submodule");

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.ecmm.ECMM_Mapping#ECMM_Mapping(java.io.File)}.
   * 
   * <p>Compare ECMMState from QCONF with reference. ECMM is run in new path.
   * 
   * <p>API call example.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testECMM_MappingFile() throws Exception {
    // only segmentation
    Path boan = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // reference file
    Path ecmmn = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "ref_fluoreszenz-test_eq_smooth_frames_1-5_BOA.QCONF");
    // copy segmentation to tmp
    Files.copy(boan, tmp.resolve(boan.getFileName()), REPLACE_EXISTING);
    // this is file to process
    File boa = tmp.resolve(boan.getFileName()).toFile();
    // process it
    ECMM_Mapping ob = new ECMM_Mapping(boa); // it will be updated
    ob.run("");

    // load reference and updated and compare

    OutlinesCollection ecmmTest = new QconfLoader(boa).getEcmm();
    // ecmmTest.oHs.get(0).indexGetOutline(0).freezeAll(); // test of test
    OutlinesCollection ecmmRef = new QconfLoader(ecmmn.toFile()).getEcmm();

    assertThat(ecmmTest, givesSameJson(ecmmRef));

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.ecmm.ECMM_Mapping#ECMM_Mapping(java.io.File)}.
   * 
   * <p>Compare snQP after ECMM with reference. ECMM is run in old path.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testECMM_MappingFile_paQP() throws Exception {
    // only segmentation
    Path boapa = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.paQP");
    Path boasn = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.snQP");
    // reference file
    Path boasnRef = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "ref_fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.snQP");
    Path boapaRef = Paths.get(
            "src/test/Resources-static/com.github.celldynamics.quimp.plugin.ecmm.EcmmMapping",
            "ref_fluoreszenz-test_eq_smooth_frames_1-5_BOA_0.paQP");
    // copy segmentation to tmp
    // copy to random dir to prevent doing other paQP files in folder
    Path tmpRandom = Files.createTempDirectory(tmp, "quimp", new FileAttribute<?>[] {});
    Files.copy(boapa, tmpRandom.resolve(boapa.getFileName()), REPLACE_EXISTING);
    Files.copy(boasn, tmpRandom.resolve(boasn.getFileName()), REPLACE_EXISTING);
    // this is file to process (in tmp)
    File ps = tmpRandom.resolve(boapa.getFileName()).toFile();
    // output file snQP, modified by ECMM
    File sn = tmpRandom.resolve(boasn.getFileName()).toFile();
    // process it
    ECMM_Mapping ob = new ECMM_Mapping(ps); // it will be updated
    ob.run("");
    // compare files
    assertThat(ps, containsExactText(boapaRef.toFile()));
    assertThat(sn, containsExactText(boasnRef.toFile()));
  }

}
