package com.github.celldynamics.quimp;

import static com.github.baniuk.ImageJTestSuite.matchers.json.JsonMatchers.haveSameKeys;
import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.filesystem.StatsCollectionTest;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

// TODO: Auto-generated Javadoc
/**
 * Template test class for comparing json output from {@link Serializer} for any class extending
 * {@link IQuimpSerialize}.
 * 
 * <p>It fills given object of type T with random data, saves it in temporary folder and then
 * compares with reference data that should available in <tt>indir</tt> folder. Compared are only
 * serializable fields (names) on first depth level (default). Other level can be provided by
 * creating default constructor in child class and calling parametrised constructor.
 * 
 * @author p.baniukiewicz
 * @param <T> Type of object
 * @see OutlineTest
 * @see NestTest
 * @see StatsCollectionTest
 *
 */
public abstract class JsonKeyMatchTemplate<T extends IQuimpSerialize> {
  /**
   * Randomisation depth, 1 by default. Override default constructor to change.
   */
  private int depthLevel;
  /**
   * Serialization object. Can be accessed by {@link #prepare()}
   */
  protected Serializer<?> ser;
  /**
   * Must be overridden - instance of serialized object.
   */
  protected T obj = null;
  /**
   * Must be overridden - foled located in /src/test/Resources-static/ where reference json is.
   */
  protected String indir = null;

  /**
   * Skip randomization if true. Should be set in {@link #setUp()}.
   */
  private boolean skipRandomizer = false;

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(JsonKeyMatchTemplate.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  
  /** The tmp. */
  static Path tmp = Paths.get(tmpdir);

  /**
   * Default constructor. Initialises test object with default values.
   */
  public JsonKeyMatchTemplate() {
    depthLevel = 1;
    skipRandomizer = false;
  }

  /**
   * Constructor, configure object.
   * 
   * @param depthLevel Randomisation depth level.
   * @param skipRandomizer true if randomiser should be skipped. (User provides ready object as obj.
   */
  public JsonKeyMatchTemplate(int depthLevel, boolean skipRandomizer) {
    this.depthLevel = depthLevel;
    this.skipRandomizer = skipRandomizer;
  }

  /**
   * Must be overridden - Initialise <tt>obj</tt> and <tt>indir</tt> here.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    throw new RuntimeException("Must be overriden!");
  }

  /**
   * Generates reference JSon. Saves it in tmp dir with name pattern: ref_classname.QCONF
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetReference() throws Exception {
    if (obj == null || indir == null) {
      throw new RuntimeException("Override obj and indir in setUp()");
    }
    T data;
    if (skipRandomizer) {
      data = obj;
    } else {
      data = getRandomData(obj, depthLevel);
    }
    ser = new Serializer<>(data, new QuimpVersion());
    ser.setPretty();
    prepare();
    ser.save(tmpdir.toString() + "ref_" + obj.getClass().getSimpleName() + ".QCONF");
    LOGGER.info("Copy file " + tmpdir + "ref_" + obj.getClass().getSimpleName() + ".QCONF"
            + " to /src/test/Resources-static/" + indir + " in resources repo"
            + " and update submodule");
  }

  /**
   * Run after randomisation, allows to add extra preparatory step. For example
   * {@link Serializer#doBeforeSerialize} can be set here.
   * 
   * @throws Exception any error
   * 
   */
  protected void prepare() throws Exception {
  }

  /**
   * Compares json keys with saved reference. Detects any change of naming that can break data
   * restoration.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCompareJsonKeys() throws Exception {
    if (obj == null || indir == null) {
      throw new RuntimeException("Override obj and indir in setUp()");
    }
    Path ref = Paths.get("src/test/Resources-static", indir,
            "ref_" + obj.getClass().getSimpleName() + ".QCONF");
    String refJson = new String(Files.readAllBytes(ref));
    T data;
    if (skipRandomizer) {
      data = obj;
    } else {
      data = getRandomData(obj, depthLevel);
    }
    ser = new Serializer<>(data, new QuimpVersion());
    prepare();
    assertThat(ser.toString(), haveSameKeys(refJson));

  }

  /**
   * Produces random obj, all filed filled with random data.
   *
   * @param <T> the generic type
   * @param obj Object to randomise - only first level is randomised.
   * @param depthLevel randomisation depth
   * @return Ranodmised object.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IQuimpSerialize> T getRandomData(T obj, int depthLevel) {
    T dt;
    EnhancedRandom eh =
            EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomizationDepth(depthLevel)
                    .exclude(field().named("prev").get()).exclude(field().named("next").get())
                    .overrideDefaultInitialization(true).collectionSizeRange(2, 2).build();
    dt = (T) eh.nextObject(obj.getClass());
    return dt;
  }

}
