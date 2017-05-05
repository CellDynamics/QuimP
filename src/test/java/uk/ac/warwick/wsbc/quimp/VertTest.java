package uk.ac.warwick.wsbc.quimp;

import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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
// @RunWith(MockitoJUnitRunner.class)
public class VertTest {
  static final Logger LOGGER = LoggerFactory.getLogger(VertTest.class.getName());

  private Vert org;
  private Vert copy;

  /**
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    org = getRandomVert();
    copy = new Vert(org); // make copy
  }

  /**
   * Test method for {@link uk.ac.warwick.wsbc.quimp.Vert#hashCode()}.
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
   * Produces random vertex, all filed filled with random data. Has previous and next but not
   * looped.
   * 
   * @return random vertex.
   */
  public static Vert getRandomVert() {
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
    org.updateNormale(true); // update notmale, in test will be set to false
    org.setCurvatureLocal(); // compute curvature

    return org;

  }

}
