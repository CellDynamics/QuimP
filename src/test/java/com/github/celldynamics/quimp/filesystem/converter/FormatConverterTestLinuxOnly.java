package com.github.celldynamics.quimp.filesystem.converter;

import static com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers.containsExactText;
import static com.github.baniuk.ImageJTestSuite.tools.files.FileModifiers.getLine;
import static com.github.baniuk.ImageJTestSuite.tools.files.FileModifiers.replaceLine;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
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

import com.github.celldynamics.quimp.Shape;

/**
 * FormatConverterTestLinuxOnly.
 * 
 * <p>This tests always return true under Windows due to different paths in template files.
 * 
 * @author p.baniukiewicz
 * 
 */
public class FormatConverterTestLinuxOnly {
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(FormatConverterTestLinuxOnly.class.getName());

  /**
   * setUp.
   *
   * @throws Exception the exception
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
   * Assume positive test on Windows.
   */
  @Before
  public void windowsOnly() {
    org.junit.Assume.assumeTrue(!isWindows());
  }

  /**
   * tearDown.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.5);
  }

  /**
   * Checks if is windows.
   *
   * @return true, if is windows
   */
  private boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
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
