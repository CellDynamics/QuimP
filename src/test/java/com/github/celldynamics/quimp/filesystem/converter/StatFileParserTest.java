package com.github.celldynamics.quimp.filesystem.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers;
import com.github.celldynamics.quimp.FrameStatistics;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.filesystem.StatsCollection;
import com.github.celldynamics.quimp.plugin.ana.ANAp;

/**
 * @author p.baniukiewicz
 *
 */
public class StatFileParserTest {

  ArrayList<String> expFiles;
  StatFileParser obj;

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

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.filesystem.converter.StatFileParser#getAllFiles()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetAllFiles() throws Exception {
    String folder = "src/test/Resources-static/com.github.celldynamics.quimp."
            + "filesystem.converter.StatFileParser/";
    obj = new StatFileParser(folder + "test");
    List<Path> ret = obj.getAllFiles();
    List<Path> exp = Arrays.asList(new Path[] { Paths.get(folder, "test_0.stQP.csv"),
        Paths.get(folder, "test_1.stQP.csv") });
    assertThat(ret, is(exp));
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.filesystem.converter.StatFileParser#importStQp()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testImportStQp() throws Exception {
    String folder = "src/test/Resources-static/com.github.celldynamics.quimp."
            + "filesystem.converter.StatFileParser/";
    obj = new StatFileParser(folder + "test");
    obj.importStQp(); // do import
    List<Path> files = obj.getAllFiles(); // only for matcher - get list of stQP files

    int i = 0; // _XX number in stQP
    for (Path p : files) {
      // compare with QCONF by saving QCONF data to stQP file (easier comparison because of
      // rounding)
      QconfLoader qcl = new QconfLoader(Paths.get(folder, "test.QCONF").toFile()); // load QCONF
      StatsCollection stats = qcl.getStats(); // get stats
      File tmp = File.createTempFile("importBOA", ""); // tmp output
      ANAp anap = new ANAp(); // needed for saving stQP
      // copy params from BOA to ANAp
      anap.scale = qcl.getBOA().boap.getImageScale();
      anap.frameInterval = qcl.getBOA().boap.getImageFrameInterval();
      // write stats (with rounding)
      FrameStatistics.write(stats.sHs.get(i++).framestat.toArray(new FrameStatistics[0]), tmp,
              anap);
      replaceLine(tmp.toPath(), 1, getLine(p, 1)); // make sure that both files have same
      // header
      assertThat(tmp, is(FileMatchers.containsExactText(p.toFile())));
    }
  }

  /**
   * Replace line in file.
   * 
   * @param name file name and path
   * @param lineNumber line number from 0
   * @param data replace string
   * @throws IOException error
   */
  public void replaceLine(Path name, int lineNumber, String data) throws IOException {
    List<String> lines = Files.readAllLines(name, StandardCharsets.UTF_8);
    lines.set(lineNumber, data);
    Files.write(name, lines, StandardCharsets.UTF_8);
  }

  /**
   * Get line from file.
   * 
   * @param name file name and path
   * @param lineNumber line number from 0
   * @return Line at position lineNumber
   * @throws IOException error
   */
  public String getLine(Path name, int lineNumber) throws IOException {
    List<String> lines = Files.readAllLines(name, StandardCharsets.UTF_8);
    return lines.get(lineNumber);
  }

}
