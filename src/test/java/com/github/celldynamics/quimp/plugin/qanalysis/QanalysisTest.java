package com.github.celldynamics.quimp.plugin.qanalysis;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ij.ImageJ;
import ij.WindowManager;

/**
 * Test of Q analysis high API.
 * 
 * @author p.baniukiewicz
 *
 */
public class QanalysisTest {

  private ImageJ ij;

  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * Before.
   */
  @Before
  public void setUp() {
    ij = new ImageJ();
  }

  /**
   * Finish.
   */
  @After
  public void goDown() {
    ij.quit();
    ij = null;
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.qanalysis.Q_Analysis#Q_Analysis(java.io.File)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testQ_AnalysisFile() throws Exception {
    Path destFile = Paths.get(temp.getRoot().getPath(), "test.QCONF");
    FileUtils.copyFile(
            Paths.get("src/test/Resources-static/FormatConverter/QCONF/test.QCONF").toFile(),
            destFile.toFile());

    Q_Analysis qa = new Q_Analysis();
    qa.run("opts={trackColor:Summer,outlinePlot:Speed,sumCov:1.0,avgCov:0.0,mapRes:400,paramFile:("
            + destFile.toString() + ")}");

    assertThat(WindowManager.getImageTitles().length, is(9));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_0_motility.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_0_convexity.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_0_fluoCh1.tif"));

    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_1_motility.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_1_convexity.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_1_fluoCh2.tif"));

    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_2_motility.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_2_convexity.tif"));
    assertThat(WindowManager.getImageTitles(), hasItemInArray("test_2_fluoCh3.tif"));

    assertThat(Paths.get(temp.getRoot().toString(), "test_1_track.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_1_motility.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_1_motility.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_1_fluoCh2.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_1_convexity.tif").toFile().exists(),
            is(true));

    assertThat(Paths.get(temp.getRoot().toString(), "test_0_track.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_0_motility.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_0_motility.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_0_convexity.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_0_fluoCh1.tif").toFile().exists(),
            is(true));

    assertThat(Paths.get(temp.getRoot().toString(), "test_2_track.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_2_motility.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_2_motility.svg").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_2_convexity.tif").toFile().exists(),
            is(true));
    assertThat(Paths.get(temp.getRoot().toString(), "test_2_fluoCh3.tif").toFile().exists(),
            is(true));

  }

  /**
   * About test.
   */
  @Test
  public void testAbout() {
    Q_Analysis mask;
    mask = new Q_Analysis();
    assertThat(mask.about(), is(any(String.class)));
  }
}
