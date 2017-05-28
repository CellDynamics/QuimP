package com.github.celldynamics.quimp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

/**
 * Use cases for Serializer versioning with Since and Until tags
 * 
 * @author p.baniukiewicz
 *
 */
public class SerializerVersioning {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SerializerVersioning.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Load older QCONF to newer QuimP.
   * 
   * <p>Older version written in 0.8 without annotations. Loaded to 1.2.
   * 
   * <p>Pre: Old version contains all fields
   * 
   * <p>Post: Loaded class contains vales of old fields and new (not available in json) initialized
   * in cnew class constructor. If no initialisation given - empty or default value.
   * 
   * @throws Exception
   * @throws IOException
   * @throws JsonIOException
   * @throws JsonSyntaxException
   */
  @Test
  public void testScenario1() throws JsonSyntaxException, JsonIOException, IOException, Exception {
    // save old version
    TestClass_older to = new TestClass_older();
    Serializer<TestClass_older> s =
            new Serializer<>(to, new QuimpVersion("0.8.0", "stamp", "quimp"));
    s.setPretty();
    s.save(tmpdir + "older.josn");

    // load to new class
    Serializer<TestClass_newer> out;
    TestClass_newer obj;
    Serializer<TestClass_newer> sl =
            new Serializer<>(TestClass_newer.class, new QuimpVersion("1.2.0", "stamp", "quimp"));
    out = sl.load(tmpdir + "older.josn");
    obj = out.obj;
    LOGGER.debug(obj.toString());
    assertEquals(15, obj.a);
    assertEquals(20, obj.b);
    assertEquals(30, obj.c);
    assertEquals(40, obj.d);
    assertEquals(50, obj.e);
    assertEquals(600, obj.f);
  }

  /**
   * Save newer in 1.0 version. It contains annotations.
   * 
   * <p>Pre saved json does not contains f filed neither d field (due to since and until)
   * 
   * <p>Post Load 1.0 version to 1.2 version - d and f fields are default as the same tags are
   * active
   * because GSon version is read from json file.
   * 
   * @throws Exception
   * @throws IOException
   * @throws JsonIOException
   * @throws JsonSyntaxException
   */
  @Test
  public void testScenario2() throws JsonSyntaxException, JsonIOException, IOException, Exception {
    // save old version
    TestClass_newer to = new TestClass_newer(1, 2, 3, 4, 5, 6);
    // version here determines which annotaions will be activated (on save)
    Serializer<TestClass_newer> s =
            new Serializer<>(to, new QuimpVersion("1.0.0", "stamp", "quimp"));
    s.setPretty();
    s.save(tmpdir + "newer10.josn");

    // load to new class
    Serializer<TestClass_newer> out;
    TestClass_newer obj;
    // annotations are activated using version read from json. The value given to constructor
    // may be ussd to fire Converter
    Serializer<TestClass_newer> sl =
            new Serializer<>(TestClass_newer.class, new QuimpVersion("1.2.0", "stamp", "quimp"));
    out = sl.load(tmpdir + "newer10.josn");
    obj = out.obj;
    LOGGER.debug(obj.toString());
    assertEquals(1, obj.a);
    assertEquals(2, obj.b);
    assertEquals(3, obj.c);
    assertEquals(400, obj.d);
    assertEquals(5, obj.e);
    assertEquals(600, obj.f);
  }

  /**
   * Loaded class contains field ff not available in current one and not versioned and does not
   * contain c field.
   * 
   * <p>Pre saved json contains ff field and no c
   * 
   * <p>d field is default as it is not loaded due to Until tag, c is default because not available
   * in json as well as f
   * 
   * @throws Exception
   * @throws IOException
   * @throws JsonIOException
   * @throws JsonSyntaxException
   */
  @Test
  public void testScenario3() throws JsonSyntaxException, JsonIOException, IOException, Exception {
    // save old version
    TestClass_other to = new TestClass_other();
    Serializer<TestClass_other> s =
            new Serializer<>(to, new QuimpVersion("1.0.0", "stamp", "quimp"));
    s.setPretty();
    s.save(tmpdir + "other10.josn");

    // load to new class
    Serializer<TestClass_newer> out;
    TestClass_newer obj;
    Serializer<TestClass_newer> sl =
            new Serializer<>(TestClass_newer.class, new QuimpVersion("1.2.0", "stamp", "quimp"));
    out = sl.load(tmpdir + "other10.josn");
    obj = out.obj;
    LOGGER.debug(obj.toString());
    assertEquals(15, obj.a);
    assertEquals(20, obj.b);
    assertEquals(300, obj.c);
    assertEquals(400, obj.d);
    assertEquals(50, obj.e);
    assertEquals(600, obj.f);
  }

}

/**
 * Dummy test class with support GSon annotations.
 * 
 * <p>Contain five fields.
 * 
 * @author p.baniukiewicz
 *
 */
class TestClass_older implements IQuimpSerialize {
  int a;
  int b;
  int c;
  int d;
  int e;

  @Override
  public void beforeSerialize() {
    // TODO Auto-generated method stub

  }

  public TestClass_older() {
    a = 15;
    b = 20;
    c = 30;
    d = 40;
    e = 50;
  }

  @Override
  public void afterSerialize() throws Exception {
    // TODO Auto-generated method stub

  }

}

/**
 * Dummy test class with support GSon annotations.
 * 
 * <p>Contain five fields.
 * 
 * @author p.baniukiewicz
 *
 */
class TestClass_other implements IQuimpSerialize {
  int a;
  int b;
  int d;
  int e;
  int ff;

  @Override
  public void beforeSerialize() {
    // TODO Auto-generated method stub

  }

  public TestClass_other() {
    a = 15;
    b = 20;
    d = 40;
    e = 50;
    ff = 500;
  }

  @Override
  public void afterSerialize() throws Exception {
    // TODO Auto-generated method stub

  }

}

/**
 * Dummy test class with support GSon annotations.
 * 
 * <p>Newer to TestClass_older, one extra filed and one deprecated.
 * 
 * @author p.baniukiewicz
 *
 */
class TestClass_newer implements IQuimpSerialize {
  int a;
  int b;
  int c;
  @Until(0.9)
  int d;
  int e;
  @Since(1.1)
  int f;

  /**
   * @param a
   * @param b
   * @param c
   * @param d
   * @param e
   * @param f
   */
  public TestClass_newer(int a, int b, int c, int d, int e, int f) {
    super();
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
    this.e = e;
    this.f = f;
  }

  @Override
  public void beforeSerialize() {
    // TODO Auto-generated method stub

  }

  public TestClass_newer() {
    a = 150;
    b = 200;
    c = 300;
    d = 400;
    e = 500;
    f = 600;
  }

  @Override
  public void afterSerialize() throws Exception {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TestClass_newer [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f="
            + f + "]";
  }

}