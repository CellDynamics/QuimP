package com.github.celldynamics.quimp.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author p.baniukiewicz
 *
 */
public class AbstractPluginOptionsTest {

  /**
   * The Constant logger.
   */
  public final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * Test of serialise().
   * 
   * @throws Exception on error
   */
  @Test
  public void testSerialise() throws Exception {
    Options opt = new Options();
    String json = opt.serialize();
    logger.debug("Ser: " + json);
    logger.debug(opt.paramFile);
    assertThat(opt.otherPath, is("space space")); // not modified in main object
    assertThat(opt.paramFile, is("path/to/file with spaces.qconf"));
    assertThat(json, containsString("[space space]")); // escaped in json
    assertThat(json, containsString("[path/to/file with spaces.qconf]"));
    assertThat(StringUtils.countMatches(json, "["), is(2));
    assertThat(StringUtils.countMatches(json, "]"), is(2));
  }

  /**
   * Test of deserialise().
   * 
   * @throws Exception on error
   */
  @Test
  public void testDeserialise() throws Exception {
    String json = "{\"paramFile\":\"file/test.qconf\"}";
    Options opt;
    opt = Options.deserialize(json, new Options());
    assertThat(opt.paramFile, is("file/test.qconf"));

  }

  /**
   * Test of {@link AbstractPluginOptions#removeSpacesMacro(String)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testRemoveSpacesMacro() throws Exception {
    String test1 = "{param:test , param2 : test2, param3: [path to file],{ param5: [other path]} }";
    String ret = "{param:test,param2:test2,param3:[path to file],{param5:[other path]}}";
    assertThat(AbstractPluginOptions.removeSpacesMacro(test1), is(ret));
  }

  /**
   * Test of {@link AbstractPluginOptions#serialize2Macro()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testSerialize2Macro() throws Exception {
    Options opt = new Options();
    String json = opt.serialize2Macro();
    logger.debug(json);
    String ret = "{param2:10,param3:3.14,otherPath:[space space],param4:{internal1:20},paramFile:"
            + "[path/to/file with spaces.qconf]}";
    assertThat(json, is(ret));
  }

  /**
   * Test of {@link AbstractPluginOptions#deserialize2Macro(String, AbstractPluginOptions)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testDeserialize2Macro() throws Exception {
    Options opt = new Options();
    String ret = "{param2:10,param3:3.14,otherPath:[space space],param4:{internal1:20},paramFile:"
            + "[path/to/file with spaces.qconf]}";
    Options des = Options.deserialize2Macro(ret, new Options());
    assertThat(des.otherPath, is("space space")); // escaping chars removed
    assertThat(des.paramFile, is("path/to/file with spaces.qconf")); // escaping chars removed
  }

  /**
   * Test of serialization->deserialization for non processing serialzers.
   * 
   * @throws Exception on error
   */
  @Test
  public void testSerDeser_1() throws Exception {
    Options opt = new Options();
    String js = opt.serialize(); // change to json
    Options ret = AbstractPluginOptions.deserialize(js, new Options()); // back to object
    assertThat(ret.param2, is(opt.param2));
    assertThat(ret.paramFile, is(opt.paramFile));
    assertThat(ret.param3, is(opt.param3));
    assertThat(ret.otherPath, is(opt.otherPath));
    assertThat(ret.param4.internal1, is(opt.param4.internal1));
  }

  /**
   * Test of serialization->deserialization for macro processing serialzers.
   * 
   * @throws Exception on error
   */
  @Test
  public void testSerDeser_2() throws Exception {
    Options opt = new Options();
    String js = opt.serialize2Macro(); // change to json
    assertThat(js, containsString("[space space]")); // escaped in json
    assertThat(js, containsString("[path/to/file with spaces.qconf]"));
    assertThat(StringUtils.countMatches(js, "["), is(2));
    assertThat(StringUtils.countMatches(js, "]"), is(2));
    Options ret = AbstractPluginOptions.deserialize2Macro(js, new Options()); // back to object
    assertThat(ret.param2, is(opt.param2));
    assertThat(ret.paramFile, is(opt.paramFile));
    assertThat(ret.param3, is(opt.param3));
    assertThat(ret.otherPath, is(opt.otherPath)); // no escape chars
    assertThat(ret.param4.internal1, is(opt.param4.internal1));
  }

  /**
   * Test class.
   * 
   * @author p.baniukiewicz
   *
   */
  class Options extends AbstractPluginOptions {
    int param2 = 10;
    @EscapedPath // should be ignored
    double param3 = 3.14;

    @EscapedPath()
    String otherPath = "space space";

    Internal param4 = new Internal();

    class Internal {
      public int internal1 = 20;

    }

    public Options() {
      paramFile = "path/to/file with spaces.qconf";
    }
  }
}
