package com.github.celldynamics.quimp.registration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class RegistrationTest.
 *
 * @author p.baniukiewicz
 */
@SuppressWarnings("unused")
public class RegistrationTest {

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
  static Object accessPrivate(String name, Registration obj, Object[] param, Class<?>[] paramtype)
          throws NoSuchMethodException, SecurityException, IllegalAccessException,
          IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  /** The modality type. */
  private ModalityType modalityType;

  /** The owner. */
  private Window owner;
  
  /** The title. */
  private String title;

  /**
   * Sets the up before class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * Tear down after class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for private Registration#validateRegInfo(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateRegInfo() throws Exception {
    Registration obj = new Registration(null);
    String email = "";
    String key = "";
    boolean ret = (boolean) accessPrivate("validateRegInfo", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
    assertFalse(ret);
  }

  /**
   * Test method for private Registration#validateRegInfo(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateRegInfo_1() throws Exception {
    Registration obj = new Registration(null);
    String email = " ";
    String key = "";
    boolean ret = (boolean) accessPrivate("validateRegInfo", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
    assertFalse(ret);
  }

  /**
   * Test method for private Registration#validateRegInfo(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateRegInfo_2() throws Exception {
    Registration obj = new Registration(null);
    String email = "baniuk1@gmail.com";
    String key = "d2264e17765b74627e67e73dcad1d9d4";
    boolean ret = (boolean) accessPrivate("validateRegInfo", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
    assertTrue(ret);
  }

  /**
   * Test method for private Registration#validateRegInfo(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateRegInfo_3() throws Exception {
    Registration obj = new Registration(null);
    String email = " baniuk1@gmail.com";
    String key = "d2264e17765b74627e67e73dcad1d9d4 ";
    boolean ret = (boolean) accessPrivate("validateRegInfo", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
    assertTrue(ret);
  }

  /**
   * Test method for private Registration#validateRegInfo(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testValidateRegInfo_4() throws Exception {
    Registration obj = new Registration(null);
    String email = "test@wp.pl";
    String key = "8acb3a0f375dce7f40f7a0cbd294c74b";
    boolean ret = (boolean) accessPrivate("validateRegInfo", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
    assertTrue(ret);
  }

  /**
   * Test method for private Registration#registerUser(java.lang.String, java.lang.String).
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRegisterUser() throws Exception {
    Registration obj = new Registration(null);
    String email = " baniuk1@gmail.com";
    String key = "d2264e17765b74627e67e73dcad1d9d4 ";
    accessPrivate("registerUser", obj, new Object[] { email, key },
            new Class<?>[] { String.class, String.class });
  }

}
