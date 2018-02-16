package com.github.celldynamics.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsArrayWithSize.emptyArray;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.filesystem.FileExtensions;

// TODO: Auto-generated Javadoc
/**
 * The Class QParamsTest.
 *
 * @author p.baniukiewicz
 */
public class QParamsTest {

  /**
   * Test method for {@link com.github.celldynamics.quimp.QParams#findParamFiles()}.
   * 
   * <p>Input file does not have path nor extension
   * 
   * <p>Expected exception
   * 
   * @throws Exception on error
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFindParamFiles_nopath() throws Exception {
    QParams qp = new QParams(new File("dfs"));
    qp.findParamFiles();
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.QParams#findParamFiles()}.
   * 
   * <p>Input file does have path but is does not exist
   * 
   * <p>Empty array
   * 
   * @throws Exception on error
   */
  @Test()
  public void testFindParamFiles_pathnodir() throws Exception {
    QParams qp = new QParams(new File("./dfdee/dfs"));
    File[] ret = qp.findParamFiles();
    assertThat(ret, is(emptyArray()));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.QParams#findParamFiles()}.
   * 
   * <p>Input file does have path, do not exist but no files inside
   * 
   * <p>Empty array
   * 
   * @throws Exception on error
   */
  @Test()
  public void testFindParamFiles_nofiles() throws Exception {
    QParams qp = new QParams(new File("src/test/Resources-static/QParams/emptyfolder/file"));
    File[] ret = qp.findParamFiles();
    assertThat(ret, is(emptyArray()));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.QParams#findParamFiles()}.
   * 
   * <p>Input file does have path, exist and other files inside
   * 
   * <p>List of files except original
   * 
   * @throws Exception on error
   */
  @Test()
  public void testFindParamFiles_files() throws Exception {
    QParams qp = new QParams(new File(
            "src/test/Resources-static/QParams/folder1/file" + FileExtensions.configFileExt));
    File[] ret = qp.findParamFiles();
    Stream<File> f = Arrays.stream(ret);
    List<String> names = new ArrayList<String>();
    f.forEach(x -> names.add(x.getName()));

    assertThat(names, containsInAnyOrder("other.paQP", "another.paQP"));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.QParams#readParams()}.
   * 
   * <p>Empty file
   * 
   * @throws Exception on error
   */
  @Test(expected = IllegalStateException.class)
  public void testReadParams() throws Exception {
    QParams qp = new QParams(new File(
            "src/test/Resources-static/QParams/folder1/file" + FileExtensions.configFileExt));
    qp.readParams();
  }

}
