package com.github.celldynamics.quimp;

import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
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

// TODO: Auto-generated Javadoc
/**
 * Test of Node.
 * 
 * @author p.baniukiewicz
 *
 */
public class NodeTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(NodeTest.class.getName());
  
  /** The org. */
  private Node org;
  
  /** The copy. */
  private Node copy;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    org = getRandomNode(1);
    copy = new Node(org); // make copy
  }

  /**
   * Test method for com.github.celldynamics.quimp.Node.Node(final Node). Test copy constructor and
   * equals methods.
   * 
   * @throws Exception on error
   */
  @Test
  public void testNodeNode() throws Exception {
    assertThat(copy, is(org));
    // copy constructor copies only current object. next and previous are transient so do not check
    // them
    assertThat(EqualsBuilder.reflectionEquals(org, copy, false), is(true)); // copy is same

    org.updateNormale(false); // get other normale
    assertThat(copy, is(not(org))); // can not be same
    assertThat(EqualsBuilder.reflectionEquals(org, copy, false), is(not(true)));

  }

  /**
   * Test conversion constructor between Node and Vert.
   */
  @Test
  public void testNode2Vert() {
    Node def = new Node(1, 2, 3); // default node, initialises superclass and child class
    Vert cmp = new Vert(def); // superclass should be the same as def, child class (extended to
    // Node) set to defaults.
    Vert v = new Vert(1, 2, 3); // should be same as cmp

    assertThat(v, is(cmp));
  }

  /**
   * Produces random node, all filed filled with random data. Has previous and next but not
   * looped.
   * 
   * @param id Node id
   * 
   * @return random node.
   */
  public static Node getRandomNode(int id) {
    Node org;
    EnhancedRandom eh = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
            .exclude(field().named("prev").get()).exclude(field().named("next").get())
            .overrideDefaultInitialization(true).build();
    org = eh.nextObject(Node.class);
    // for local curvature, no looping
    org.setNext(eh.nextObject(Node.class));
    org.setPrev(eh.nextObject(Node.class));
    org.updateNormale(true); // update normale, in test will be set to false
    org.update();
    org.setTrackNum(id);

    return org;
  }

  /**
   * Produces four element list of random nodes.
   * 
   * @return random node (head) is first on list
   */
  public static List<Node> getRandomNodePointList() {

    Node head = getRandomNode(1);
    head.setHead(true);
    Node v1 = getRandomNode(2);
    v1.setHead(false);
    Node v2 = getRandomNode(3);
    v2.setHead(false);
    Node v3 = getRandomNode(4);
    v3.setHead(false);

    head.setNext(v1);
    v1.setPrev(head);
    v1.setNext(v2);

    v2.setPrev(v1);
    v2.setNext(v3);

    v3.setPrev(v2);
    v3.setNext(head);
    head.setPrev(v3);

    ArrayList<Node> ret = new ArrayList<>();
    ret.add(head);
    ret.add(v1);
    ret.add(v2);
    ret.add(v3);
    return ret;
  }

}
