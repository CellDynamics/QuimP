package uk.ac.warwick.wsbc.quimp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader;

// TODO Run all tests on directories in /tmp after copying relevant files
/**
 * @author p.baniukiewicz
 * 
 */
public class FormatConverterTest {

  /**
   * Access private.
   *
   * @param name the name
   * @param obj the obj
   * @param param the param
   * @param paramtype the paramtype
   * @return the object
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  static Object accessPrivate(String name, FormatConverter obj, Object[] param,
          Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    Files.deleteIfExists(Paths.get(Paths.get(".").toAbsolutePath().normalize().toString()
            + "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.snQP"));
    Files.deleteIfExists(Paths.get(Paths.get(".").toAbsolutePath().normalize().toString()
            + "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for FormatConverter#generatepaQP().
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testGeneratepaQP() throws Exception {
    FormatConverter fc = new FormatConverter(
            new File("src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));

    accessPrivate("generatepaQP", fc, new Object[] {}, new Class<?>[] {});

    Thread.sleep(1000);
    // compare paQP
    // manualy generated one
    BufferedReader readerexpected =
            new BufferedReader(new FileReader("src/test/Resources-static/FormatConverter/"
                    + "fluoreszenz-test_eq_smooth_0_expected.paQP"));
    // expected
    BufferedReader readertest = new BufferedReader(new FileReader(
            "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));

    readerexpected.readLine(); // skip header with random controlsum
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    char[] expected = new char[800];
    char[] test = new char[800];
    readerexpected.read(expected, 0, expected.length);
    readerexpected.close();
    readertest.read(test, 0, test.length);
    readertest.close();

    assertThat(test, is(expected));
  }

  /**
   * Test method for private FormatConverter#generatesnQP.
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testGeneratesnQP() throws Exception {
    FormatConverter fc = new FormatConverter(
            new File("src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
    accessPrivate("generatesnQP", fc, new Object[] {}, new Class<?>[] {});
    Thread.sleep(1000);
    // compare paQP
    // manualy generated one
    BufferedReader readerexpected =
            new BufferedReader(new FileReader("src/test/Resources-static/FormatConverter/"
                    + "fluoreszenz-test_eq_smooth_0_expected.paQP"));
    // expected
    BufferedReader readertest = new BufferedReader(new FileReader(
            "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));

    readerexpected.readLine(); // skip header with random controlsum
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    char[] expected = new char[800];
    char[] test = new char[800];
    readerexpected.read(expected, 0, expected.length);
    readerexpected.close();
    readertest.read(test, 0, test.length);
    readertest.close();

    assertThat(test, is(expected));
  }

  /**
   * Test method for private FormatConverter#generateOldDataFile.
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testFormatConverterQParamsQconfPath() throws Exception {
    QconfLoader qc = new QconfLoader(
            Paths.get("src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth.QCONF")
                    .toFile());
    FormatConverter fc = new FormatConverter(qc);
    accessPrivate("generateOldDataFile", fc, new Object[] {}, new Class<?>[] {});
    Thread.sleep(1000);
    // compare paQP
    // manualy generated one
    BufferedReader readerexpected =
            new BufferedReader(new FileReader("src/test/Resources-static/FormatConverter/"
                    + "fluoreszenz-test_eq_smooth_0_expected.paQP"));
    // expected
    BufferedReader readertest = new BufferedReader(new FileReader(
            "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));

    readerexpected.readLine(); // skip header with random controlsum
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    readerexpected.readLine();
    readertest.readLine();
    char[] expected = new char[800];
    char[] test = new char[800];
    readerexpected.read(expected, 0, expected.length);
    readerexpected.close();
    readertest.read(test, 0, test.length);
    readertest.close();

    assertThat(test, is(expected));
  }

  /**
   * Test method for private FormatConverter#generateNewDataFile().
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testGenerateNewDataFile() throws Exception {
    QconfLoader qc = new QconfLoader(new File("src/test/Resources-static/FormatConverter/res/"
            + "fluoreszenz-test_eq_smooth_0_expected.paQP"));
    FormatConverter fc = new FormatConverter(qc);
    accessPrivate("generateNewDataFile", fc, new Object[] {}, new Class<?>[] {});
  }

  /**
   * Test method for private FormatConverter#generateOldDataFile().
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGenerateOldDataFile() throws Exception {
    QconfLoader qc = new QconfLoader(
            new File("src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
    FormatConverter fc = new FormatConverter(qc);
    accessPrivate("generateOldDataFile", fc, new Object[] {}, new Class<?>[] {});
  }

}
