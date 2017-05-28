package com.github.celldynamics.quimp;

import java.awt.Polygon;
import java.util.List;

import org.scijava.vecmath.Tuple2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.IJ;
import ij.gui.Roi;

/**
 * Represent Outline object used as Snake representation after ECMM mapping.
 * 
 * <p>Outline can have the same Shape as Snake but distribution of Vert may be different than
 * distribution of Node in Snake. Outline is produced after ECMM and used in further analysis.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 */
public final class Outline extends Shape<Vert> implements Cloneable, IQuimpSerialize {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(Outline.class.getName());

  /**
   * Default constructor. Create empty outline.
   */
  public Outline() {
    super();
  }

  /**
   * Create a Outline from existing linked list
   * 
   * <p>Behaviour of this method was changed. Now it does not make copy of Vert. In old approach
   * there was dummy node deleted in this constructor.
   * 
   * <pre>
   * <code>
   * index = 0;
   * head = new Vert(index); // dummy head node head.setHead(true); 
   * prevn = head;
   * index++; // insert next nodes here
   * </code>
   * </pre>
   * 
   * @param h head node of linked list
   * @param nn number of nodes in list
   * 
   */
  public Outline(final Vert h, int nn) {
    super(h, nn);
    // removeVert(head);
    this.updateCurvature();
    calcCentroid();
  }

  /**
   * Blank outline.
   * 
   * @param h Initial Vert
   */
  public Outline(final Vert h) {
    super(h);
    this.updateCurvature();
    calcCentroid();
  }

  /**
   * Copy constructor. Copy properties of Outline. Previous or next points are not copied
   * 
   * @param src Source Outline
   */
  public Outline(final Outline src) {
    super(src);
  }

  /**
   * Conversion constructor.
   * 
   * <p>Convert only basic properties. Do not forget that many of Vert properties are set during
   * ECMM or Q Analysis.
   * 
   * <p>Set normales outwards. This can be changed by calling {@link #updateNormales(boolean)}
   * afterwards.
   * 
   * @param src Snake to be converted to Outline
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Outline(final Snake src) {
    super((Shape) src, new Vert());
    this.updateCurvature();
    this.updateNormales(false);
  }

  /**
   * Create an outline from an Roi.
   * 
   * @param roi Initial ROI
   */
  public Outline(final Roi roi) {
    head = new Vert(0); // make a dummy head node
    POINTS = 1;
    head.setPrev(head); // link head to itself
    head.setNext(head);
    head.setHead(true);

    Vert v = head;

    Polygon p = roi.getPolygon();
    for (int i = 0; i < p.npoints; i++) {
      v = insertVert(v);
      v.setX(p.xpoints[i]);
      v.setY(p.ypoints[i]);
    }
    removeVert(head); // remove dummy head node
    updateNormales(false);
    this.updateCurvature();
    calcCentroid();
  }

  /**
   * Construct Outline object from list of nodes. Head node is always first element from array.
   * 
   * @param list list of nodes as Vector2d
   */
  public Outline(final List<? extends Tuple2d> list) {
    super(list, new Vert(0), true);
  }

  /**
   * Construct Outline object from X and Y arrays. Head node is always first element from array.
   * 
   * @param x x coordinates of nodes
   * @param y y coordinates of nodes
   */
  public Outline(final double[] x, final double[] y) {
    super(x, y, new Vert(0), true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Outline [POINTS=" + POINTS + ", centroid=" + centroid + ", toString()="
            + super.toString() + "]";
  }

  /**
   * Old toString.
   */
  public void print() {
    IJ.log("Print verts (" + POINTS + ")");
    int i = 0;
    Vert v = head;
    do {
      // int x = (int) v.getPoint().getX();
      // int y = (int) v.getPoint().getY();
      double x = v.getPoint().getX();
      double y = v.getPoint().getY();
      double c = v.coord;
      double f = v.fCoord;

      String xx = IJ.d2s(x, 8);
      String yy = IJ.d2s(y, 8);
      String cc = IJ.d2s(c, 3);
      String ff = IJ.d2s(f, 3);

      String sh = "";
      String si = "\t";
      if (v.isHead()) {
        sh = " isHead";
      }
      if (v.isIntPoint()) {
        si = "\t isIntPoint(" + v.intsectID + ")";
      }

      IJ.log("Vert " + v.getTrackNum() + " (" + cc + ")(" + ff + "), x:" + xx + ", y:" + yy + si
              + sh);
      v = v.getNext();
      i++;
    } while (!v.isHead());
    if (i != POINTS) {
      IJ.log("VERTS and linked list dont tally!!");
    }
  }

  /**
   * Remove selected Vert from list.
   * 
   * <p>Perform check if removed Vert was head and if it was, the new head is randomly selected.
   * Neighbours are linked together
   * 
   * @param v Vert to remove
   */
  public void removeVert(final Vert v) {
    if (POINTS <= 3) {
      LOGGER.error("Outline. 175. Can't remove node. Less than 3 would remain");
      return;
    }
    super.removePoint(v, true);
    if (POINTS < 3) {
      IJ.error("Outline.199. WARNING! Nodes less then 3");
    }
  }

  /**
   * Update curvature for all Vert in Outline.
   */
  public void updateCurvature() {
    Vert v = head;
    do {
      v.setCurvatureLocal();
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Insert default Vert after Vert v.
   * 
   * @param v Vert to insert new Vert after
   * @return Inserted Vert
   */
  public Vert insertVert(final Vert v) {
    return insertPoint(v, new Vert());
  }

  /**
   * Insert a vertex in-between v and v.next with interpolated values
   * 
   * <p>Modify current Outline
   * 
   * @param v vertex to insert other vertex after it
   * @return Vertex created between \c v and \c v.next
   */
  public Vert insertInterpolatedVert(final Vert v) {
    Vert newVert = insertVert(v);
    newVert.setX((newVert.getPrev().getX() + newVert.getNext().getX()) / 2);
    newVert.setY((newVert.getPrev().getY() + newVert.getNext().getY()) / 2);

    newVert.updateNormale(true);
    newVert.distance = (newVert.getPrev().distance + newVert.getNext().distance) / 2;
    newVert.gCoord = interpolateCoord(newVert.getPrev().gCoord, newVert.getNext().gCoord);
    newVert.fCoord = interpolateCoord(newVert.getPrev().fCoord, newVert.getNext().fCoord);

    return newVert;
  }

  /**
   * interpolate between co-ordinates (that run 0-1).
   * 
   * @param a start coord
   * @param b end coord
   * @return interpolated value between a and b
   */
  public double interpolateCoord(double a, double b) {
    if (a > b) {
      b = b + 1;
    }
    double dis = a + ((b - a) / 2);

    if (dis >= 1) {
      dis += -1; // passed zero
    }
    return dis;
  }

  /**
   * Evenly spaces a new vertices around the outline by following vectors between verts.
   * 
   * @param density density
   */
  public void setResolution(double density) {
    double length = getLength();
    int numVerts = (int) Math.round((length / density)); // must be round number of verts
    density = length / numVerts; // re-cal the density

    double remaining = 0.;
    double lastPlacement;
    double currentDis;

    Vert oldHead = head;
    Vert v1 = oldHead;

    // new, higher res outline
    head = new Vert(v1.getX(), v1.getY(), v1.getTrackNum());
    head.setHead(true);
    head.setIntPoint(oldHead.isIntPoint(), oldHead.intsectID);
    head.setNext(head);
    head.setPrev(head);

    head.gCoord = 0.;
    head.coord = 0.;

    nextTrackNumber = oldHead.getTrackNum() + 1;
    POINTS = 1;

    double coorSpaceing = 1.0 / numVerts;
    // System.out.println("coord: " + CoorSpaceing);
    double currentCoor = coorSpaceing;

    Vert temp;
    Vert newV;
    newV = head;

    int numVertsInserted = 1; // the head
    ExtendedVector2d uedge;
    ExtendedVector2d edge;
    ExtendedVector2d placementVector;
    do {
      Vert v2 = v1.getNext();
      lastPlacement = 0.0;
      currentDis = 0.0;
      edge = ExtendedVector2d.vecP2P(v1.getPoint(), v2.getPoint());
      // edge.print("edge");
      uedge = ExtendedVector2d.unitVector(v1.getPoint(), v2.getPoint());
      // uEdge.print("uEdge");
      if (edge.length() == 0) { // points on top of one another, move on
        v1 = v1.getNext();
        continue;
      }

      while (true) {
        placementVector = new ExtendedVector2d(uedge.getX(), uedge.getY());
        // double mult = (density + currentDis) - remaining;
        // System.out.println("mult "+mult);
        // placementVector.print("before Mult");
        placementVector.multiply((density + currentDis) - remaining);

        // placementVector.print("\tafter Mult");

        if (placementVector.length() <= edge.length() && numVertsInserted < numVerts) { // if
          currentDis = placementVector.length();
          lastPlacement = currentDis;
          remaining = 0;

          placementVector.addVec(v1.getPoint());

          temp = insertVert(newV);
          temp.setX(placementVector.getX());
          temp.setY(placementVector.getY());
          temp.gCoord = currentCoor;
          temp.coord = currentCoor;

          newV = temp;

          numVertsInserted++;
          currentCoor += coorSpaceing;

        } else {
          if (v2.isHead()) {
            break; // stop it douplicating the head node if it also an intpoint
          }
          if (v2.isIntPoint()) {
            temp = insertVert(newV);
            temp.setX(v2.getX());
            temp.setY(v2.getY());
            temp.setIntPoint(true, v2.intsectID);
            newV = temp;
          }
          remaining = edge.length() - lastPlacement + remaining;
          break;
        }
      }

      v1 = v1.getNext();
    } while (!v1.isHead());
    oldHead = null;
    // LOGGER.trace("head =[" + getHead().getX() + "," + getHead().getY() + "]");
  }

  /**
   * Evenly spaces a new vertices around the outline by following vectors between verts.
   * 
   * @param numVerts numVerts
   */
  public void setResolutionN(double numVerts) {
    double length = getLength();
    // int numVerts = (int) Math.round((length / density)); // must be round
    // number of verts
    double density = length / numVerts; // re-cal the density

    double remaining = 0.0;
    double lastPlacement;
    double currentDis;

    Vert oldHead = head;
    Vert v1 = oldHead;

    // new, higher res outline
    head = new Vert(v1.getX(), v1.getY(), v1.getTrackNum());
    head.setHead(true);
    head.setIntPoint(oldHead.isIntPoint(), oldHead.intsectID);
    head.setNext(head);
    head.setPrev(head);

    head.gCoord = 0.;
    head.coord = 0.;

    nextTrackNumber = oldHead.getTrackNum() + 1;
    POINTS = 1;

    double coorSpaceing = 1.0 / numVerts;
    // System.out.println("coord: " + CoorSpaceing);
    double currentCoor = coorSpaceing;

    Vert temp;
    Vert newV;
    newV = head;

    int numVertsInserted = 1; // the head
    ExtendedVector2d uedge;
    ExtendedVector2d edge;
    ExtendedVector2d placementVector;
    do {
      Vert v2 = v1.getNext();
      lastPlacement = 0.0;
      currentDis = 0.0;
      edge = ExtendedVector2d.vecP2P(v1.getPoint(), v2.getPoint());
      // edge.print("edge");
      uedge = ExtendedVector2d.unitVector(v1.getPoint(), v2.getPoint());
      // uEdge.print("uEdge");
      if (edge.length() == 0) { // points on top of one another, move on
        v1 = v1.getNext();
        continue;
      }

      while (true) {
        placementVector = new ExtendedVector2d(uedge.getX(), uedge.getY());
        // double mult = (density + currentDis) - remaining;
        // System.out.println("mult "+mult);
        // placementVector.print("before Mult");
        placementVector.multiply((density + currentDis) - remaining);

        // placementVector.print("\tafter Mult");

        if (placementVector.length() <= edge.length() && numVertsInserted < numVerts) { // if
          currentDis = placementVector.length();
          lastPlacement = currentDis;
          remaining = 0;

          placementVector.addVec(v1.getPoint());

          temp = insertVert(newV);
          temp.setX(placementVector.getX());
          temp.setY(placementVector.getY());
          temp.gCoord = currentCoor;
          temp.coord = currentCoor;

          newV = temp;

          numVertsInserted++;
          currentCoor += coorSpaceing;

        } else {
          if (v2.isHead()) {
            break; // stop it douplicating the head node if it also an intpoint
          }
          if (v2.isIntPoint()) {
            temp = insertVert(newV);
            temp.setX(v2.getX());
            temp.setY(v2.getY());
            temp.setIntPoint(true, v2.intsectID);
            newV = temp;
          }
          remaining = edge.length() - lastPlacement + remaining;
          break;
        }
      }

      v1 = v1.getNext();
    } while (!v1.isHead());
    oldHead = null;
  }

  /**
   * calcVolume.
   * 
   * @return volume of outline
   */
  public double calcVolume() {
    double sum;
    sum = 0.0;
    Vert n = head;
    Vert np1 = n.getNext();
    do {
      sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
      n = n.getNext();
      np1 = n.getNext(); // note: n is reset on prev line

    } while (!n.isHead());
    return 0.5 * sum;
  }

  /**
   * calcArea.
   * 
   * @return area of outline
   */
  public double calcArea() {
    double area;
    double sum;
    sum = 0.0;
    Vert n = head;
    Vert np1 = n.getNext();
    do {
      sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
      n = n.getNext();
      np1 = n.getNext(); // note: n is reset on prev line

    } while (!n.isHead());
    area = 0.5 * sum;
    return area;
  }

  /**
   * getNextIntersect.
   * 
   * @param v v
   * @return next intersection
   */
  public static Vert getNextIntersect(Vert v) {
    do {
      v = v.getNext();
    } while (!v.isIntPoint()); // move to next int point
    return v;
  }

  /**
   * findIntersect.
   * 
   * @param v v
   * @param id id
   * @return intersection
   */
  public static Vert findIntersect(Vert v, int id) {
    int count = 0; // debug
    do {
      if (v.isIntPoint() && v.intsectID == id) {
        break;
      }
      v = v.getNext();
      if (count++ > 2000) {
        System.out.println("Outline->findIntersect - search exceeded 2000");
        break;
      }
    } while (true);

    return v;
  }

  /**
   * distBetweenInts.
   * 
   * @param intA intA
   * @param intB intB
   * @return distance between intersections
   */
  public static int distBetweenInts(Vert intA, Vert intB) {
    int d = 0;
    do {
      d++;
      intA = intA.getNext();
      if (d > 2000) { // debug
        System.out.println("Outline:distBetween->search exceeded 2000");
        break;
      }
    } while (intA.intsectID != intB.intsectID);
    return d;
  }

  /**
   * invertsBetween.
   * 
   * @param intA intA
   * @param intB intB
   * @return ?
   */
  public static int invertsBetween(Vert intA, Vert intB) {
    int i = 0;
    int count = 0;
    do {
      intA = intA.getNext();
      if (intA.isIntPoint() && intA.intState > 2) {
        i++;
      }
      if (intA.isIntPoint() && intA.intState == 1) {
        i--;
      }
      if (count++ > 2000) { // debug
        System.out.println("Outline:invertsBetween. search exceeded 2000");
        break;
      }
    } while (intA.intsectID != intB.intsectID);
    return i;
  }

  /**
   * Insert or remove nodes according to criteria.
   * 
   * <p>It does not redistribute nodes equally. Removing has priority over inserting, inserted
   * vertex can be next removed if it is too close. Does not work vice-versa as inserting does not
   * push current node forward (to next in list) but removing does. And removing condition is
   * checked first
   * 
   * @param max max allowed distance
   * @param min min allowed distance
   */
  public void correctDensity(double max, double min) {
    double dist;
    Vert v = head;
    boolean canEnd = true;
    do {
      dist = ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      canEnd = true;
      if (dist < min) {
        if (!v.getNext().isHead()) {
          removeVert(v.getNext()); // just remove
          v = v.getNext(); // and go to next
        } else {
          removeVert(v.getNext()); // remove, do not know where is new head, can be current or next
          break; // end loop - next was head so we circulated all
        }
      } else if (dist > max) {
        this.insertInterpolatedVert(v); // insert after v
        // if head do not end, this is linear approx, inserted can be still farer than max. We check
        // this in next iter (current node des not change)
        if (v.isHead()) {
          canEnd = false;
        }
      } else {
        v = v.getNext();
      }
    } while (!(v.isHead() && canEnd));
  }

  /**
   * Done once at the end of each frame to cut out any parts of the contour that self intersect.
   * 
   * <p>Similar to cutLoops, but check all edges (interval up to NODES/2) and cuts out the smallest
   * section
   * 
   * @return true if cut something
   * @see com.github.celldynamics.quimp.Snake#cutLoops()
   * @see com.github.celldynamics.quimp.Snake#cutIntersects()
   */
  public boolean cutSelfIntersects() {
    boolean icut = false;
    int interval;
    int state;

    Vert na;
    Vert nb;
    double[] intersect = new double[2];
    Vert newN;

    boolean cutHead;

    na = head;
    do {
      cutHead = (na.getNext().isHead()) ? true : false;
      nb = na.getNext().getNext(); // don't check the next one along! they touch, not overlap
      interval = (POINTS > 6) ? POINTS / 2 : 2; // always leave 3 nodes, at least. Check half way
      for (int i = 2; i < interval; i++) {
        if (nb.isHead()) {
          cutHead = true;
        }
        intersect = new double[2];
        state = ExtendedVector2d.segmentIntersection(na.getX(), na.getY(), na.getNext().getX(),
                na.getNext().getY(), nb.getX(), nb.getY(), nb.getNext().getX(), nb.getNext().getY(),
                intersect);

        if (state == 1) {
          icut = true;
          newN = this.insertInterpolatedVert(na);
          newN.setX(intersect[0]);
          newN.setY(intersect[1]);

          newN.setNext(nb.getNext());
          nb.getNext().setPrev(newN);

          newN.updateNormale(true);
          nb.getNext().updateNormale(true);

          if (cutHead) {
            // System.out.println("cut the head");
            newN.setHead(true); // put a new head in
            head = newN;
          }

          // newN.print("inserted node: ");
          // System.out.println("C - VERTS : " + VERTS);
          if (POINTS - (i) < 3) {
            LOGGER.warn("OUTLINE 594_VERTS WILL BE than 3. i = " + i + ", VERT=" + POINTS);
          }
          POINTS -= (i);
          break;
        }
        nb = nb.getNext();
      }
      na = na.getNext();
    } while (!na.isHead());

    return icut;
  }

  /**
   * Remove really small edges that cause numerical inaccuracy when checking for self intersects.
   * 
   * <p>Tends to happen when migrating edges get pushed together
   * 
   * @return \c true if deleted something
   */
  public boolean removeNanoEdges() {
    double nano = 0.1;
    double length;
    boolean deleted = false;

    Vert na;
    Vert nb;

    na = head;
    do {
      do {
        nb = na.getNext();
        length = ExtendedVector2d.lengthP2P(na.getPoint(), nb.getPoint());
        if (length < nano) {
          this.removeVert(nb);
          deleted = true;
        }
      } while (length < nano);

      na = na.getNext();
    } while (!na.isHead());

    return deleted;
  }

  /**
   * Set head node coord to zero. Make closest landing to zero the head node
   * 
   * <p>Prevents circulating of the zero coord
   */
  public void coordReset() {
    Vert vertFirst = findFirstNode('g'); // get first node in terms of fcoord (origin)
    head.setHead(false);
    head = vertFirst;
    head.setHead(true);

    double length = getLength();
    double d = 0.;

    Vert v = head;
    do {
      v.coord = d / length;
      d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Reset all linear coordinates related to node {@link Vert#coord}, {@link Vert#gCoord},
   * {@link Vert#fCoord}.
   */
  public void resetAllCoords() {
    double length = getLength();
    double d = 0.;

    Vert v = head;
    do {
      v.coord = d / length;
      v.gCoord = v.coord;
      v.fCoord = v.coord;
      d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      v = v.getNext();
    } while (!v.isHead());
    // LOGGER.trace("head =[" + getHead().getX() + "," + getHead().getY() + "]");
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   * @deprecated Change to copy constructor
   */
  public Object clone() {
    // clone the outline
    Vert ov = head;

    Vert nv = new Vert(ov.getX(), ov.getY(), ov.getTrackNum()); // head node
    nv.coord = ov.coord;
    nv.fCoord = ov.fCoord;
    nv.gCoord = ov.gCoord;
    nv.distance = ov.distance;
    // nV.fluores = oV.cloneFluo();
    nv.setFluores(ov.fluores);

    Outline n = new Outline(nv);

    ov = ov.getNext();
    do {
      nv = n.insertVert(nv);
      nv.setX(ov.getX());
      nv.setY(ov.getY());
      nv.coord = ov.coord;
      nv.fCoord = ov.fCoord;
      nv.gCoord = ov.gCoord;
      nv.distance = ov.distance;
      // nV.fluores = oV.cloneFluo();
      nv.setFluores(ov.fluores);
      nv.setTrackNum(ov.getTrackNum());

      ov = ov.getNext();
    } while (!ov.isHead());
    n.updateNormales(true);
    n.calcCentroid();
    n.updateCurvature();

    return n;
  }

  /**
   * checkCoordErrors.
   * 
   * @return true if error found
   */
  public boolean checkCoordErrors() {
    Vert v = head;

    do {
      // check for errors in gCoord and fCoord
      if (v.gCoord >= 1 || v.gCoord < 0 || v.fCoord >= 1 || v.fCoord < 0 || v.coord >= 1
              || v.coord < 0) {
        System.out.println("Outline587: Errors in tracking Coordinates\n\t" + "coord=" + v.coord
                + ", gCoord= " + v.gCoord + ", fCoord = " + v.fCoord);
        return true;
      }

      v = v.getNext();
    } while (!v.isHead());

    return false;

  }

  /**
   * Find the first node in terms of coord (c) or fcoord (f) ie closest to zero.
   * 
   * @param c coordinate code
   * @return First node according to given coordinate code.
   */
  public Vert findFirstNode(char c) {

    Vert v = head;
    Vert vertFirst = v;

    double coord;
    double coordPrev;
    double dis;
    double disFirst = 0;

    do {
      // coord = (c == 'f') ? v.fCoord : v.coord;
      // coordPrev = (c == 'f') ? v.getPrev().fCoord : v.getPrev().coord;
      if (c == 'f') {
        coord = v.fCoord;
        coordPrev = v.getPrev().fCoord;
      } else if (c == 'g') {
        coord = v.gCoord;
        coordPrev = v.getPrev().gCoord;
      } else {
        coord = v.coord;
        coordPrev = v.getPrev().coord;
      }

      dis = Math.abs(coord - coordPrev);
      // System.out.println("abs( " + coord + "-"+coordPrev+") = " + dis);

      if (dis > disFirst) {
        vertFirst = v;
        disFirst = dis;
        // System.out.println("\tchoosen ");
        // vFirst.print();
      }

      v = v.getNext();
    } while (!v.isHead());

    // System.out.println("First "+c+"Coord vert: ");
    // vFirst.print();

    return vertFirst;

  }

  /**
   * clearFluores.
   */
  public void clearFluores() {
    Vert v = head;
    do {
      v.setFluoresChannel(-2, -2, -2, 0);
      v.setFluoresChannel(-2, -2, -2, 1);
      v.setFluoresChannel(-2, -2, -2, 2);
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * findCoordEdge.
   * 
   * @param a a
   * @return ?
   */
  Vert findCoordEdge(double a) {
    Vert v = head;
    do {

      if (v.coord < a && v.coord > a) {
        return v;
      }

      if (v.coord > a && v.getPrev().coord > a && v.getNext().coord > a
              && v.getNext().getNext().coord > a) {
        return v;
      }

      if (v.coord < a && v.getPrev().coord < a && v.getNext().coord < a
              && v.getNext().getNext().coord < a) {
        return v;
      }

      v = v.getNext();
    } while (!v.isHead());
    return head;
  }

  /**
   * Scale the outline proportionally.
   * 
   * <p>Shape is constricted in given number of <tt>steps</tt>. Method updates shape normales
   * setting them in inner direction. Results can differ (slightly) on each run due to random
   * selection of head on point remove.
   * 
   * <p>Updates centroid and normalised <tt>position</tt>.
   * 
   * @param amount scale
   * @param stepRes shift done in one step
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   */
  public void scale(double amount, double stepRes, double angleTh, double freezeTh) {
    int j;
    updateNormales(true);
    double steps = Math.abs(amount / stepRes);
    for (j = 0; j < steps; j++) {
      if (getNumPoints() <= 3) {
        break;
      }
      scale(stepRes);
      updateNormales(true);
      removeProx(1.5, 1.5); // constants taken from old removeProx were they were hardcoded
      freezeProx(angleTh, freezeTh);
      if (j > MAX_NODES) {
        LOGGER.warn("shrink (336) hit max iterations!");
        break;
      }
    }

    if (getNumPoints() < 3) {
      LOGGER.info("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
    }

    if (cutSelfIntersects()) {
      LOGGER.debug("ANA_(382)...fixed ana intersects");
    }

    if (getNumPoints() < 3) {
      LOGGER.info("ANA 377_NODES LESS THAN 3");
    }
    calcCentroid();
    setPositions();
  }

  /**
   * Remove close vertexes.
   * 
   * <p>For each element distances are calculated to next and previous elements. If any of distances
   * is
   * smaller than given threshold, element is removed (if not frozen).
   * 
   * @param d1th distance threshold between previous and current
   * @param d2th distance threshold between current and next
   */
  public void removeProx(double d1th, double d2th) {
    if (getNumPoints() <= 3) {
      return;
    }
    Vert v;
    Vert vl;
    Vert vr;
    double d1;
    double d2;
    v = getHead();
    vl = v.getPrev();
    vr = v.getNext();
    do {
      d1 = ExtendedVector2d.lengthP2P(v.getPoint(), vl.getPoint());
      d2 = ExtendedVector2d.lengthP2P(v.getPoint(), vr.getPoint());

      if ((d1 < d1th || d2 < d2th) && !v.isFrozen()) { // don't remove frozen. May alter angles
        removeVert(v);
      }
      v = v.getNext().getNext();
      vl = v.getPrev();
      vr = v.getNext();
    } while (!v.isHead() && !vl.isHead());

  }

  /**
   * Freeze a node and corresponding edge if its to close && close to parallel.
   * 
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   */
  public void freezeProx(double angleTh, double freezeTh) {
    Vert v;
    Vert vtmp;
    ExtendedVector2d closest;
    ExtendedVector2d edge;
    ExtendedVector2d link;
    double dis;
    double angle;

    v = getHead();
    do {
      // if (!v.frozen) {
      vtmp = getHead();
      do {
        if (vtmp.getTrackNum() == v.getTrackNum()
                || vtmp.getNext().getTrackNum() == v.getTrackNum()) {
          vtmp = vtmp.getNext();
          continue;
        }
        closest = ExtendedVector2d.PointToSegment(v.getPoint(), vtmp.getPoint(),
                vtmp.getNext().getPoint());
        dis = ExtendedVector2d.lengthP2P(v.getPoint(), closest);
        // System.out.println("dis: " + dis);
        // dis=1;
        if (dis < freezeTh) {
          edge = ExtendedVector2d.unitVector(vtmp.getPoint(), vtmp.getNext().getPoint());
          link = ExtendedVector2d.unitVector(v.getPoint(), closest);
          angle = Math.abs(ExtendedVector2d.angle(edge, link));
          if (angle > Math.PI) {
            angle = angle - Math.PI; // if > 180, shift back around
          }
          // 180
          angle = angle - 1.5708; // 90 degree shift to centre around zero
          // System.out.println("angle:" + angle);

          if (angle < angleTh && angle > -angleTh) {
            v.freeze();
            vtmp.freeze();
            vtmp.getNext().freeze();
          }

        }
        vtmp = vtmp.getNext();
      } while (!vtmp.isHead());
      // }
      v = v.getNext();
    } while (!v.isHead());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.Shape#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
    super.beforeSerialize();
    this.updateCurvature();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.Shape#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    super.afterSerialize();
    this.updateCurvature(); // WARN This may be not good idea to override loaded data
  }

}
