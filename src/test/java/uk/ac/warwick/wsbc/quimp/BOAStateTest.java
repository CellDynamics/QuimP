package uk.ac.warwick.wsbc.quimp;

import static org.junit.Assert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.haveSameKeys;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

/**
 * @author p.baniukiewicz
 *
 */
public class BOAStateTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BOAStateTest.class.getName());

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
  @Ignore
  public void testGetReference() throws Exception {
    BOAState data = getRandomBoaState();
    Serializer<BOAState> n;
    n = new Serializer<>(data, new QuimpVersion());
    n.setPretty();
    n.save(tmpdir.toString() + "ref.QCONF");
    LOGGER.info("Copy file " + tmpdir + "ref.QCONF" + " to /src/test/Resources-static/"
            + "uk.ac.warwick.wsbc.quimp.BOAStateTest/ in resources repo" + " and update submodule");
  }

  /**
   * Compares json keys with saved reference. Detects any change of naming that can break data
   * restoration.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCompareJsonKeys() throws Exception {
    Path ref = Paths.get("src/test/Resources-static", "uk.ac.warwick.wsbc.quimp.BOAStateTest",
            "ref.QCONF");
    String refJson = new String(Files.readAllBytes(ref));
    // get current object
    BOAState test = getRandomBoaState();
    Serializer<BOAState> n = new Serializer<>(test, new QuimpVersion());
    assertThat(n.toString(), haveSameKeys(refJson));

  }

  /**
   * Produces random DataContainer, all filed filled with random data.
   * 
   * @return random vertex.
   */
  public static BOAState getRandomBoaState() {
    BOAState dt;
    EnhancedRandom eh = EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomizationDepth(1)
            .overrideDefaultInitialization(true).collectionSizeRange(2, 2).build();
    dt = eh.nextObject(BOAState.class);
    return dt;
  }
}
