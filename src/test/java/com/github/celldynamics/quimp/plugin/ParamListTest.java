package com.github.celldynamics.quimp.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ParamList.
 * 
 * @author p.baniukiewicz
 *
 */
public class ParamListTest {

  /** The list. */
  private ParamList list;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    list = new ParamList();
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    list = null;
  }

  /**
   * Test set int value.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetIntValue() throws Exception {
    list.setIntValue("KEY", 10);
    assertEquals(10, list.getIntValue("key"));
  }

  /**
   * Test set double value.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetDoubleValue() throws Exception {
    list.setDoubleValue("KEY", 10.1);
    assertEquals(10.1, list.getDoubleValue("key"), 1e-5);
  }

  /**
   * Test set string value.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetStringValue() throws Exception {
    list.setStringValue("key", "v");
    assertEquals("v", list.getStringValue("Key"));
  }

  /**
   * Test set boolean value.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetBooleanValue() throws Exception {
    list.setBooleanValue("key", true);
    assertTrue(list.getBooleanValue("KEY"));
  }

  /**
   * Test put.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPut() throws Exception {
    list.put("key", "v");
    assertEquals("v", list.get("Key"));
  }

  /**
   * Test contains key.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsKey() throws Exception {
    list.put("Key", "1.1");
    assertTrue(list.containsKey("KEY"));

  }

  /**
   * Test put all.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPutAll() throws Exception {
    HashMap<String, String> s = new HashMap<>();
    s.put("key1", "1");
    s.put("key2", "2");
    list.putAll(s);
    assertTrue(list.containsKey("KEY1") && list.containsKey("KEY2"));
  }

  /**
   * Test remove object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveObject() throws Exception {
    list.put("Key", "1.1");
    list.put("rem", "2");
    list.remove("REM");
    assertFalse(list.containsKey("rem"));
  }

  /**
   * Test remove object object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveObjectObject() throws Exception {
    list.put("Key", "1.1");
    list.put("rem", "2");
    list.remove("REM", "2");
    assertFalse(list.containsKey("rem"));
  }

  /**
   * Test shallow copy.
   */
  @Test
  public void testShallowCopy() {
    HashMap<String, String> s = new HashMap<>();
    s.put("key1", "df");
    s.put("key2", "BH");
    list.putAll(s);
    assertTrue(list.containsKey("KEY1") && list.containsKey("KEY2"));

    ParamList copy = new ParamList(list); // make shallow copy
    assertTrue(copy.containsKey("KEY1") && copy.containsKey("KEY2"));
    assertEquals("df", copy.get("KEY1"));
    assertEquals("BH", copy.get("KEY2"));

  }
}
