package com.github.celldynamics.quimp.utils.test.matchers.file;

import static com.github.celldynamics.quimp.utils.test.matchers.file.QuimpFileMatchers.givesSameJson;
import static com.github.celldynamics.quimp.utils.test.matchers.file.QuimpFileMatchers.haveSameKeys;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;

// TODO: Auto-generated Javadoc
/**
 * Test class for QuimpFileMatcher.
 * 
 * @author p.baniukiewicz
 *
 */
public class FileMatcherTest {

  // ------------------------- GivesSameJson

  /**
   * Test of json matching, same objects.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testGivesSameJson_same() throws Exception {
    DataContainer dt = new DataContainer();
    DataContainer dt1 = new DataContainer();

    assertThat(dt, givesSameJson(dt1));
  }

  /**
   * Test of json matching, other objects.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testGivesSameJson_other() throws Exception {
    DataContainer dt = new DataContainer();
    dt.ECMMState = new OutlinesCollection();
    DataContainer dt1 = new DataContainer();

    assertThat(dt, is(not(givesSameJson(dt1))));
  }

  // ------------------------- HaveSameKeys

  /**
   * Test of keys matching.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testHaveSameKeys_same() throws Exception {
    TestClass test = new TestClass();
    TestClass ref = new TestClass();
    ref.alpha = 13; // val difference
    assertThat(test, haveSameKeys(ref)); // but keys remain the same
    // but different json
    assertThat(test, is(not(givesSameJson(ref))));
    ref.alpha = 0; // set the same value
    assertThat(test, is(givesSameJson(ref))); // and have the same json
  }

  /**
   * Test of keys matching.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testHaveSameKeys_different() throws Exception {
    TestClass test = new TestClass();
    TestClass1 ref = new TestClass1(); // one letter different
    ref.alpha = 13; // val difference
    assertThat(test, is(not(haveSameKeys(ref))));
  }

  /**
   * The Class TestClass.
   */
  class TestClass implements IQuimpSerialize {

    /** The alpha. */
    int alpha;
    
    /** The beta. */
    int beta = 10;
    
    /** The nest. */
    Nested nest;

    /**
     * Instantiates a new test class.
     */
    public TestClass() {
      nest = new Nested();
    }

    /* (non-Javadoc)
     * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
    }

    /* (non-Javadoc)
     * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {
    }

    /**
     * The Class Nested.
     */
    class Nested {
      
      /** The list. */
      List<String> list = new ArrayList<>();
      
      /** The bravo. */
      double bravo = 3.14;
    }
  }

  /**
   * The Class TestClass1.
   */
  class TestClass1 implements IQuimpSerialize {

    /** The alpha. */
    int alpha;
    
    /** The beta. */
    int beta = 10;
    
    /** The nest. */
    Nested nest;

    /**
     * Instantiates a new test class 1.
     */
    public TestClass1() {
      nest = new Nested();
    }

    /* (non-Javadoc)
     * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
    }

    /* (non-Javadoc)
     * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {
    }

    /**
     * The Class Nested.
     */
    class Nested {
      
      /** The list. */
      List<String> list = new ArrayList<>();
      
      /** The brav O. */
      double bravO = 3.14; // here
    }
  }
}
