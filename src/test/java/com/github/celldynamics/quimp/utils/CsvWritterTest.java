package com.github.celldynamics.quimp.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CsvWritterTest.
 *
 * @author p.baniukiewicz
 */
public class CsvWritterTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(CsvWritterTest.class.getName());

  /**
   * Test method for {@link com.github.celldynamics.quimp.utils.CsvWritter#CsvWritter()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCsvWritter() throws Exception {
    CsvWritter csv = new CsvWritter();
    csv.close();
    assertThat(Files.exists(csv.getPath(), LinkOption.NOFOLLOW_LINKS), is(true));
  }

  /**
   * Test method for
   * {@link CsvWritter#CsvWritter(java.nio.file.Path, java.lang.String[])}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCsvWritterPathStringArray() throws Exception {
    CsvWritter csv = new CsvWritter(Paths.get(tmpdir, "test.csv")); // empty file
    csv.close();
    CsvWritter csv1 =
            new CsvWritter(Paths.get(tmpdir, "test1.csv"), "#Frame", "x-coord", "y-coord");
    csv1.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.CsvWritter#writeLine(java.lang.String)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testWriteLineString() throws Exception {
    CsvWritter csv2 =
            new CsvWritter(Paths.get(tmpdir, "test2.csv"), "#Frames", "x-coord", "y-coord");
    csv2.writeLine("Line to write");
    csv2.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.CsvWritter#writeLine(java.lang.Double[])}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testWriteLineDoubleArray() throws Exception {
    CsvWritter csv2 =
            new CsvWritter(Paths.get(tmpdir, "test3.csv"), "#Frames", "x-coord", "y-coord");
    csv2.writeLine(1.0, 3.14, 0.245345678);
    csv2.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.CsvWritter#appendLine(Double...)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testAppendLineDoubleArray() throws Exception {
    CsvWritter csv2 =
            new CsvWritter(Paths.get(tmpdir, "test4.csv"), "#Frames", "x-coord", "y-coord");
    csv2.appendLine(1.0, 3.14, 0.245345678).appendDelim().appendLine(1.1, 2.2).appendLine("\n");
    csv2.close();
  }

}
