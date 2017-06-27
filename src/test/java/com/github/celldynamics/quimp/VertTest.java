package com.github.celldynamics.quimp;

import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

/**
 * @author p.baniukiewicz
 *
 */
public class VertTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(VertTest.class.getName());

  private Vert org;
  private Vert copy;

  /**
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    org = getRandomVert(1);
    copy = new Vert(org); // make copy
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.Vert#hashCode()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testHashCode() throws Exception {
    assertThat(copy.hashCode(), is(org.hashCode()));
  }

  /**
   * Test of clone constructor.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testVertVert() throws Exception {
    assertThat(copy, is(org));
    // copy constructor copies only current object. next and previous are transient so do not check
    // them
    assertThat(EqualsBuilder.reflectionEquals(org, copy, false), is(true)); // copy is same

    org.updateNormale(false); // get other normale
    assertThat(copy, is(not(org))); // can not be same
    assertThat(EqualsBuilder.reflectionEquals(org, copy, false), is(not(true)));

  }

  /**
   * Produces random vertex, all filed filled with random data. Contains next and previous
   * neighbours
   * but not looped.
   * 
   * @param id vertex id
   * 
   * @return random vertex
   */
  public static Vert getRandomVert(int id) {
    Vert org;
    EnhancedRandom eh = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
            .exclude(field().named("prev").get()).exclude(field().named("next").get())
            .exclude(field().named("fluores").get()).overrideDefaultInitialization(true).build();
    org = eh.nextObject(Vert.class);
    org.setFluores(new FluoMeasurement[] { random(FluoMeasurement.class),
        random(FluoMeasurement.class), random(FluoMeasurement.class) });
    // for local curvature, no looping
    org.setNext(eh.nextObject(Vert.class));
    org.setPrev(eh.nextObject(Vert.class));
    org.updateNormale(true); // update normale, in test will be set to false
    org.setCurvatureLocal(); // compute curvature
    org.setTrackNum(id);
    return org;
  }

  /**
   * Produces four element list of random vertexes. List is looped
   * 
   * @return random vertex (head) is first on list
   */
  public static List<Vert> getRandomVertPointList() {

    Vert head = getRandomVert(1);
    head.setHead(true);
    Vert v1 = getRandomVert(2);
    v1.setHead(false);
    Vert v2 = getRandomVert(3);
    v2.setHead(false);
    Vert v3 = getRandomVert(4);
    v3.setHead(false);

    head.setNext(v1);
    v1.setPrev(head);
    v1.setNext(v2);

    v2.setPrev(v1);
    v2.setNext(v3);

    v3.setPrev(v2);
    v3.setNext(head);
    head.setPrev(v3);

    ArrayList<Vert> ret = new ArrayList<>();
    ret.add(head);
    ret.add(v1);
    ret.add(v2);
    ret.add(v3);
    return ret;
  }

}
