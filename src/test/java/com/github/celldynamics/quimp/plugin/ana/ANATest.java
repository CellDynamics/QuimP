package com.github.celldynamics.quimp.plugin.ana;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.celldynamics.quimp.filesystem.ANAParamCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.utils.IJTools;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;

/**
 * Test of ANA high API.
 * 
 * @author p.baniukiewicz
 *
 */
public class ANATest {

  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static ImageJ ij;

  /**
   * SetUp ImageJ.
   * 
   * @throws Exception Exception
   */
  @BeforeClass
  public static void before() throws Exception {
    ij = new ImageJ();
  }

  /**
   * Exit ImageJ.
   * 
   * @throws Exception Exception
   */
  @AfterClass
  public static void after() throws Exception {
    IJTools.exitIj(ij);
    ij = null;
  }

  /**
   * setUp.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {

  }

  /**
   * tearDown.
   * 
   * @throws Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    IJTools.closeAllImages();
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.plugin.ana.ANA_#run(java.lang.String)}.
   * 
   * <p>Run ANA twice and check if data are properly incorporated.
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun() throws Exception {
    double setScalae = 3.1;
    Path destFile = Paths.get(temp.getRoot().getPath(), "test.QCONF");
    ImagePlus im = IJ.openImage("src/test/Resources-static/FormatConverter/QCONF/test.tif");
    im.show();
    FileUtils.copyFile(
            Paths.get("src/test/Resources-static/FormatConverter/QCONF/test.QCONF").toFile(),
            destFile.toFile());
    ANA_ ana = new ANA_();
    ana.run("opts={plotOutlines:true," + "fluoResultTable:false,fluoResultTableAppend:false,"
            + "channel:0,userScale:" + setScalae
            + ",normalise:true,sampleAtSame:false,clearFlu:true," + "paramFile:("
            + destFile.toString() + ")}");

    assertThat(WindowManager.getImageTitles(), hasItemInArray("DUP_test.tif"));

    QconfLoader qcl = new QconfLoader(destFile.toFile());

    ANAParamCollection anap = qcl.getANA();

    for (ANAp ap : anap.aS) {
      assertThat(ap.getCortexWidthScale(), is(setScalae));
      assertThat(ap.fluTiffs[0].getPath(),
              is("src/test/Resources-static/FormatConverter/QCONF/test.tif"));
      assertThat(ap.fluTiffs[1].getPath(), is("/"));
      assertThat(ap.fluTiffs[2].getPath(), is("/"));
    }
    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[0].innerArea, is(not(-1.0)));
    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[1].innerArea, is((-1.0)));
    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[2].innerArea, is((-1.0)));
    assertThat(anap.aS.size(), is(3));

    // run the same but without plotOutline and different channel and no clearing
    // close old result to check if new appear (expected not)
    ImagePlus image = WindowManager.getImage("DUP_test.tif");
    image.changes = false;
    image.close();

    ana = null;
    ana = new ANA_();
    ana.run("opts={plotOutlines:false," + "fluoResultTable:false,fluoResultTableAppend:false,"
            + "channel:1,userScale:" + setScalae
            + ",normalise:true,sampleAtSame:false,clearFlu:false," + "paramFile:("
            + destFile.toString() + ")}");
    assertThat(WindowManager.getImageTitles(), not(hasItemInArray("DUP_test.tif")));
    qcl = new QconfLoader(destFile.toFile());
    anap = qcl.getANA();
    // expected added chanel 1
    for (ANAp ap : anap.aS) {
      assertThat(ap.getCortexWidthScale(), is(setScalae));
      assertThat(ap.fluTiffs[0].getPath(),
              is("src/test/Resources-static/FormatConverter/QCONF/test.tif"));
      assertThat(ap.fluTiffs[1].getPath(),
              is("src/test/Resources-static/FormatConverter/QCONF/test.tif"));
      assertThat(ap.fluTiffs[2].getPath(), is("/"));
    }

    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[0].innerArea, is(not(-1.0)));
    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[1].innerArea, is(not(-1.0)));
    assertThat(qcl.getStats().sHs.get(0).framestat.get(0).channels[2].innerArea, is((-1.0)));
  }

  /**
   * About test.
   */
  @Test
  public void testAbout() {
    ANA_ mask;
    mask = new ANA_();
    assertThat(mask.about(), is(any(String.class)));
  }

}
