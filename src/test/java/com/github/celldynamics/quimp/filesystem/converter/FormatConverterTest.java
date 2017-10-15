package com.github.celldynamics.quimp.filesystem.converter;

import static com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers.containsExactText;
import static com.github.baniuk.ImageJTestSuite.tools.files.FileModifiers.getLine;
import static com.github.baniuk.ImageJTestSuite.tools.files.FileModifiers.replaceLine;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 * 
 */
public class FormatConverterTest {
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(FormatConverterTest.class.getName());

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
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 1.0);
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.5);
  }

  /**
   * Temporary folder.
   * 
   * @see TemporaryFolder
   * @see MyTemporaryFolder
   */
  @Rule
  public MyTemporaryFolder folder = new MyTemporaryFolder();

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
  public void testGenerateNewDataFiles() throws Exception {
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
  public void testGenerateOldDataFiles() throws Exception {
    QconfLoader qc = new QconfLoader(
            new File("src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
    FormatConverter fc = new FormatConverter(qc);
    accessPrivate("generateOldDataFiles", fc, new Object[] {}, new Class<?>[] {});
  }

  /**
   * Start conversion from not 0 paQP. Expected exception.
   * 
   * @throws Exception on error
   */
  @Test(expected = QuimpException.class)
  public void testDoConversionNotFirstFile() throws Exception {
    // copy and rename to _1 any valid paQP we have
    Path input = Paths.get(
            "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0_expected.paQP");
    Path target = folder.getRoot().toPath();
    Files.copy(input, target.resolve("test_1.paQP"), StandardCopyOption.REPLACE_EXISTING);
    FormatConverter fc = new FormatConverter(target.resolve("test_1.paQP").toFile());
    fc.doConversion();
  }

  /**
   * Compare conversion results to ground truth.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversionCompareTemplate() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-Q to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.doConversion();
    // do compare
    Path outputFileName;
    outputFileName = Paths.get("test_0.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_convexityMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_coordMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_fluoCh1.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_motilityMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_originMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_xMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0_yMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));

    outputFileName = Paths.get("test_1_convexityMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_coordMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_fluoCh2.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_motilityMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_originMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_xMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1_yMap.maQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));

  }

  /**
   * Compare conversion results to ground truth.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversionCompareTemplate_1() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-ECMM to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.doConversion();
    // do compare
    Path outputFileName;
    outputFileName = Paths.get("test_0.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
  }

  /**
   * Compare conversion results to ground truth.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversionCompareTemplate_2() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-BOA to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.doConversion();
    // do compare
    Path outputFileName;
    outputFileName = Paths.get("test_0.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
  }

  /**
   * Compare conversion results to ground truth.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConversionCompareTemplate_3() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-ANA to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.doConversion();
    // do compare
    Path outputFileName;
    outputFileName = Paths.get("test_0.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.paQP");
    replaceLine(target.resolve(outputFileName), 0, getLine(inputPath.resolve(outputFileName), 0));
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.snQP");
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_0.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
    outputFileName = Paths.get("test_1.stQP.csv");
    replaceLine(target.resolve(outputFileName), 1, getLine(inputPath.resolve(outputFileName), 1));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));
  }

  /**
   * Compare conversion results to ground truth.
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore("Due to fluctuating heads")
  public void testConversionCompareTemplate_4() throws Exception {
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.0);
    Path inputFileName = Paths.get("test_0.paQP"); // template should be saved with fixed heads
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Paqp-Q to QCONF");
    Path target = folder.getRoot().toPath();
    Path outputFileName = Paths.get("test.QCONF");
    // copy stuff to tmp
    FileUtils.copyDirectory(inputPath.toFile(), target.toFile());
    Files.delete(target.resolve(outputFileName)); // remove copied template result
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.doConversion();

    replaceLine(target.resolve(outputFileName), 7, getLine(inputPath.resolve(outputFileName), 7));
    assertThat(target.resolve(outputFileName).toFile(),
            is(containsExactText(inputPath.resolve(outputFileName).toFile())));

  }

  /**
   * Massive conversion test. To make folder persistent look at {@link MyTemporaryFolder}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQconf2paQp2Qconf() throws Exception {
    Path input = Paths.get("src/test/Resources-static/FormatConverter/QCONF");
    Path target = folder.getRoot().toPath();
    LOGGER.debug("Working dir: " + target.toString());
    // copy stuff to tmp
    Files.copy(input.resolve("test.QCONF"), target.resolve("test.QCONF"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(input.resolve("test.tif"), target.resolve("test.tif"),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve("test.QCONF").toFile());
    fc.doConversion();
    // copy converted paQP to other folder and convert them back to QCONF
    Path targetConv = folder.newFolder("convpaQP2QCONF").toPath();
    Files.copy(input.resolve("test.tif"), targetConv.resolve("test.tif"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_0.paQP"), targetConv.resolve("test_0.paQP"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_0.snQP"), targetConv.resolve("test_0.snQP"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_1.paQP"), targetConv.resolve("test_1.paQP"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_1.snQP"), targetConv.resolve("test_1.snQP"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_2.paQP"), targetConv.resolve("test_2.paQP"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_2.snQP"), targetConv.resolve("test_2.snQP"),
            StandardCopyOption.REPLACE_EXISTING);
    // copy also other files required for paQP->QCONF
    Files.copy(target.resolve("test_0.stQP.csv"), targetConv.resolve("test_0.stQP.csv"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_1.stQP.csv"), targetConv.resolve("test_1.stQP.csv"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(target.resolve("test_2.stQP.csv"), targetConv.resolve("test_2.stQP.csv"),
            StandardCopyOption.REPLACE_EXISTING);
    DirectoryStream<Path> dirStream = Files.newDirectoryStream(input, "*.maQP"); // all maps
    dirStream.forEach(path -> {
      try {
        Files.copy(path, targetConv.resolve(path.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // convert back to QCONF
    fc = new FormatConverter(targetConv.resolve("test_0.paQP").toFile());
    fc.doConversion();

    // This QCONF could be compared with that from target but roundings causes problems. Original
    // QCONF was generated with random head that matters here.

    // then convert this QCONF to paQP back
    Path targetConv2 = folder.newFolder("convQCONF2paQP").toPath();
    Files.copy(targetConv.resolve("test.QCONF"), targetConv2.resolve("test.QCONF"),
            StandardCopyOption.REPLACE_EXISTING);
    Files.copy(input.resolve("test.tif"), targetConv2.resolve("test.tif"),
            StandardCopyOption.REPLACE_EXISTING);
    fc = new FormatConverter(targetConv2.resolve("test.QCONF").toFile());
    fc.doConversion();
    // here snQP,stQP from target folder can be compared with those from targetConv2.
    // there are differences after rounding so automatic tests will fail

  }

  /**
   * Verify saving maps.
   * 
   * @throws Exception Exception
   */
  @Test()
  public void testSaveMaps_1() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-Q to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.saveMaps(STmap.ALLMAPS);
    assertThat(target.resolve("test_0_convexityMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_coordMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_fluoCh1.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_motilityMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_originMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_xMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_0_yMap.maQP").toFile().exists(), is(true));

    assertThat(target.resolve("test_1_convexityMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_coordMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_fluoCh2.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_motilityMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_originMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_xMap.maQP").toFile().exists(), is(true));
    assertThat(target.resolve("test_1_yMap.maQP").toFile().exists(), is(true));

    // verify random size
    double[][] map =
            QuimPArrayUtils.file2Array(",", target.resolve("test_0_convexityMap.maQP").toFile());
    assertThat(map.length, is(15));
    assertThat(map[0].length, is(400));

  }

  /**
   * Verify saving maps.
   * 
   * @throws Exception Exception
   */
  @Test()
  public void testSaveStats_1() throws Exception {
    Path inputFileName = Paths.get("test.QCONF");
    Path inputPath =
            Paths.get("src/test/Resources-static/FormatConverter/templates/Qconf-Q to Paqp");
    Path target = folder.getRoot().toPath();
    // copy stuff to tmp
    Files.copy(inputPath.resolve(inputFileName), target.resolve(inputFileName),
            StandardCopyOption.REPLACE_EXISTING);
    // perform conversion
    FormatConverter fc = new FormatConverter(target.resolve(inputFileName).toFile());
    fc.saveStats();
    assertThat(target.resolve("test_0.stQP.csv").toFile().exists(), is(true));
    assertThat(target.resolve("test_1.stQP.csv").toFile().exists(), is(true));
  }

  /**
   * TemporaryFolder from junit with blocked deletion of tmp folder.
   * 
   * @author p.baniukiewicz
   *
   */
  class MyTemporaryFolder extends TemporaryFolder {

    /*
     * (non-Javadoc)
     * 
     * @see org.junit.rules.TemporaryFolder#after()
     */
    @Override
    protected void after() {
      // comment to block deleting - for testing
      super.after();
    }

  }
}
