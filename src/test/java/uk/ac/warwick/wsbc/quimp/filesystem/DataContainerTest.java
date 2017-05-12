package uk.ac.warwick.wsbc.quimp.filesystem;

import static org.junit.Assert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.haveSameKeys;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import uk.ac.warwick.wsbc.quimp.QuimpVersion;
import uk.ac.warwick.wsbc.quimp.Serializer;

/**
 * @author p.baniukiewicz
 *
 */
public class DataContainerTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(DataContainerTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  static Path tmp = Paths.get(tmpdir);

  /**
   * Generates reference QCONF (empty and default). Should be enabled when needed.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetReference() throws Exception {
    DataContainer data = getRandomDataContainer();
    Serializer<DataContainer> n;
    n = new Serializer<>(data, new QuimpVersion());
    n.setPretty();
    n.save(tmpdir.toString() + "ref.QCONF");
    LOGGER.info("Copy file " + tmpdir + "ref.QCONF" + " to /src/test/Resources-static/"
            + "uk.ac.warwick.wsbc.quimp.filesystem.DataContainerTest/ in resources repo"
            + " and update submodule");
  }

  /**
   * Compares json keys with saved reference. Detects any change of naming that can break data
   * restoration.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCompareJsonKeys() throws Exception {
    Path ref = Paths.get("src/test/Resources-static",
            "uk.ac.warwick.wsbc.quimp.filesystem.DataContainerTest", "ref.QCONF");
    String refJson = new String(Files.readAllBytes(ref));
    // get current object
    DataContainer test = getRandomDataContainer();
    Serializer<DataContainer> n = new Serializer<>(test, new QuimpVersion());
    assertThat(n.toString(), haveSameKeys(refJson));

  }

  /**
   * Produces random DataContainer, all filed filled with random data.
   * 
   * @return random vertex.
   */
  public static DataContainer getRandomDataContainer() {
    DataContainer dt;
    EnhancedRandom eh = EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomizationDepth(1)
            .overrideDefaultInitialization(true).collectionSizeRange(2, 2).build();
    dt = eh.nextObject(DataContainer.class);
    return dt;
  }

}
