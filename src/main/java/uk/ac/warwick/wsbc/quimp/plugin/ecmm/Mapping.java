package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import ij.IJ;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

/**
 * Create mapping between outlines.
 * 
 * @author rtyson
 *
 */
class Mapping {

  Outline o1;
  Outline o2;
  Sector[] sectors;

  public Mapping(Outline oo1, Outline oo2) {
    o1 = oo1;
    o2 = oo2;
    ECMp.numINTS = 0;

    if (ECMp.ANA || ECMp.forceNoSectors) { // for ANA force no intersection points
      insertFake();
      o1.updateNormales(true);
      o2.updateNormales(true);
      formSectors();
      return;
    }

    // shift them slightly
    ECMp.numINTS = calcIntersects(); // temp intersect points are inserted

    if (ECMp.numINTS == 0) {
      System.out.println("No intersects found");
      insertFake();
      o1.updateNormales(true);
      o2.updateNormales(true);
      formSectors();
    } else {
      if (ECMp.inspectSectors) {
        if (!inspectInts()) {
          IJ.log("    invalid outline intersections. Intersects corrected");
          if (ECMp.plot && ECMp.drawFails) {
            ECMM_Mapping.plot.writeText("Intersects corrected");
          }
          rebuildInts();

        }
      }
      if (ECMp.plot && ECMp.drawIntersects) {
        drawIntersects();
      }
      // System.out.println("Num intersects: " + INTS);

      o1.updateNormales(true);
      o2.updateNormales(true);
      formSectors();
    }
  }

  public void printSector(int i) {
    sectors[i].print();
  }

  private int calcIntersects() {
    // inserts intersect point (intPoints) into both outlines
    int ints = 0;

    Vert na;
    Vert nb;
    Vert temp; // node 1 of edges A and B
    double[] intersect = new double[2];
    int state;

    na = o1.getHead();
    do {
      // a different outline so no problem with adjacent edges being flagged as crossing
      nb = o2.getHead();
      // edgeBcount = 1;
      do {
        state = ExtendedVector2d.segmentIntersection(na.getX(), na.getY(), na.getNext().getX(),
                na.getNext().getY(), nb.getX(), nb.getY(), nb.getNext().getX(), nb.getNext().getY(),
                intersect);

        if (state == 1) {
          // result.print("intersect at : ");
          ints++;
          temp = o1.insertVert(na);
          temp.setX(intersect[0]);
          temp.setY(intersect[1]);
          temp.setIntPoint(true, ints);
          na = na.getNext();

          temp = o2.insertVert(nb);
          temp.setX(intersect[0]);
          temp.setY(intersect[1]);
          temp.setIntPoint(true, ints);
          nb = nb.getNext();
        }
        nb = nb.getNext();
      } while (!nb.isHead());
      na = na.getNext();
    } while (!na.isHead());

    return ints;
  }

  private void insertFake() {
    // insert one fake intersect point just after the heads done when no intersections exist
    ExtendedVector2d pos =
            ExtendedVector2d.vecP2P(o1.getHead().getPoint(), o1.getHead().getNext().getPoint());
    pos.multiply(0.5);
    pos.addVec(o1.getHead().getPoint()); // half way between head and next vert

    Vert temp = o1.insertVert(o1.getHead());
    temp.setX(pos.getX());
    temp.setY(pos.getY());
    temp.setIntPoint(true, 1);
    //
    pos = ExtendedVector2d.vecP2P(o2.getHead().getPoint(), o2.getHead().getNext().getPoint());
    pos.multiply(0.5);
    pos.addVec(o2.getHead().getPoint()); // half way between head and next vert

    temp = o2.insertVert(o2.getHead());
    temp.setX(pos.getX());
    temp.setY(pos.getY());
    temp.setIntPoint(true, 1);
    ECMp.numINTS++;
  }

  private boolean inspectInts() {
    // System.out.println("finding inverted intersects");
    // make sure the intersect points form proper sectors
    // by removing intersectiosn that form inverted sectors

    boolean valid = true; // made false if an inverse or loose sector is found

    Vert v1 = o1.getHead();
    Vert v2;
    Vert v1p;
    Vert v2p;
    Vert v2m;

    for (int j = 0; j < ECMp.numINTS; j++) {
      do {
        v1 = v1.getNext();
      } while (!v1.isIntPoint()); // find next int point

      v2 = o2.getHead();
      do {
        if (v2.isIntPoint()) {
          if (v2.intsectID == v1.intsectID) {
            break; // find matching in o2
          }
        }
        v2 = v2.getNext();
      } while (true);

      // System.out.println(j + " :looking at");
      // v1.print();
      // v2.print();

      v1p = v1;
      do {
        v1p = v1p.getNext();
      } while (!v1p.isIntPoint()); // find next intersect from v1

      v2p = v2;
      do {
        v2p = v2p.getNext();
      } while (!v2p.isIntPoint()); // find next intersect, same direction

      v2m = v2;
      do {
        v2m = v2m.getPrev();
      } while (!v2m.isIntPoint()); // find next intersect oposit direction from v2

      if (v1p.intsectID == v2p.intsectID) {
        // System.out.println("Found valid sector");
        if (v1.intState == 0) {
          v1.intState = 1; // green
        }
        if (v2.intState == 0) {
          v2.intState = 1;
        }

      } else if (v1p.intsectID == v2m.intsectID) {
        System.out.println("found inverse sector");
        v1.intState = 3;
        v2.intState = 3;

        // v1p.intState = 3;
        // v2m.intState = 3;
        valid = false;
      } else {
        // System.out.println("Found loose sector");
        valid = false;
        if (v1.intState == 0) {
          v1.intState = 2; // blue
        }
        if (v2.intState == 0) {
          v2.intState = 2;
        }
        if (v1.intState == 3) {
          v1.intState = 4; // blue
        }
        if (v2.intState == 3) {
          v2.intState = 4;
        }
      }
    }

    return valid;
  }

  /**
   * Attempts to remove the correct intersects to leave only valid intersections.
   * 
   * <p>Done by adding back in inverted ints found by findInvertedInts()
   */
  private void rebuildInts() {

    System.out.println("Rebuilding intersects");
    // find a good sector to start with (intState==1)
    Vert v1 = o1.getHead();
    boolean found = false;
    do {
      if (v1.isIntPoint() && v1.intState == 1) {
        found = true;
        break;
      }
      v1 = v1.getNext();
    } while (!v1.isHead());

    // if no valid sectors use a loose vert to start from (and cross
    // fingers)
    if (!found) {
      v1 = o1.getHead();
      do {
        if (v1.isIntPoint() && (v1.intState == 4 || v1.intState == 2)) {
          found = true;
          break;
        }
        v1 = v1.getNext();
      } while (!v1.isHead());
    }
    if (!found) {
      System.out.println("    ISSUE! ECMM.01 - NO valid sectors exist! (guessing correct sectors)");
      v1 = Outline.findIntersect(v1, 4);
    }

    // find matching intersection in o2
    Vert v2 = Outline.findIntersect(o2.getHead(), v1.intsectID);
    // System.out.println("done finding a start");

    // from v1, retain intersect points that allow building of good sectors, and delete the
    // others.
    int startingInt = v1.intsectID;
    Vert v1p;
    Vert v2p;
    Vert v1pp;
    Vert v2pp;
    double ratio1;
    double ratio2; // ratio of sector lengths for 2 possible solutions
    int d1;
    int d2;
    int d3;
    int d4;

    if (ECMp.plot && ECMp.drawFails) {
      ECMM_Mapping.plot.setColor(0, 0.8, 0); // deleted colour
    }

    do {
      // System.out.println("Iteration");
      v1p = v1;
      v2p = v2;
      v1p = Outline.getNextIntersect(v1);
      v2p = Outline.getNextIntersect(v2);
      if (v1p.intsectID == v2p.intsectID) {
        v1 = v1p;
        v2 = v2p;
      } else {
        v1pp = Outline.findIntersect(v1p, v2p.intsectID);
        v2pp = Outline.findIntersect(v2p, v1p.intsectID);
        // System.out.println("found vpp intersects");
        ratio1 = Outline.invertsBetween(v1, v1pp);
        ratio2 = Outline.invertsBetween(v2, v2pp);

        if (ratio1 == ratio2) {
          // System.out.println("using distance measure");
          // use Distance measure to choose
          d1 = Outline.distBetweenInts(v1, v1pp);
          d2 = Outline.distBetweenInts(v2, v2pp);
          d3 = Outline.distBetweenInts(v1, v1p);
          d4 = Outline.distBetweenInts(v2, v2p);
          ratio1 = (d1 > d3) ? d1 / d3 : d3 / d1;
          ratio2 = (d2 > d4) ? d2 / d4 : d4 / d2;
        }

        if (ratio1 < ratio2) { // delete ints on o1 should be < §
          do {
            v1 = v1.getNext();
            if (v1.intsectID == v2p.intsectID) {
              break;
            }
            if (v1.isIntPoint()) {
              if (v1.intsectID == startingInt) {
                // System.out.println("Removing starting INT!");
              }
              o1.removeVert(v1);
              // also delete in o1
              o2.removeVert(Outline.findIntersect(o2.getHead(), v1.intsectID));
              // System.out.println("removed o2 intersects");
              if (ECMp.plot && ECMp.drawFails) {
                ECMM_Mapping.plot.drawCross(v1.getPoint(), 5);
              }
            }
          } while (true);
          v2 = v2p;
        } else { // delete ints on o2
          do {
            v2 = v2.getNext();
            if (v2.intsectID == v1p.intsectID) {
              break;
            }
            if (v2.isIntPoint()) {
              if (v2.intsectID == startingInt) {
                // System.out.println("Removing starting INT!");
              }
              o2.removeVert(v2);
              // also delete in o2
              o1.removeVert(Outline.findIntersect(o1.getHead(), v2.intsectID));
              // System.out.println("removed o1 intersects");
              if (ECMp.plot && ECMp.drawFails) {
                ECMM_Mapping.plot.drawCross(v2.getPoint(), 5);
              }
            }
          } while (true);
          v1 = v1p;
        }
      }
    } while (v1.intsectID != startingInt);

    // count remaining intersects
    v1 = o1.getHead();
    int intersects = 0;
    do {
      if (v1.isIntPoint()) {
        intersects++;
      }
      v1 = v1.getNext();
    } while (!v1.isHead());

    ECMp.numINTS = intersects;
    System.out.println("finished rebuilding. INTS:" + ECMp.numINTS);
  }

  private void drawIntersects() {
    if (!ECMp.plot) {
      return;
    }

    ECMM_Mapping.plot.setColor(0, 0.8, 0);
    Vert v1 = o1.getHead();
    do {
      if (v1.isIntPoint()) {
        ECMM_Mapping.plot.drawCross(v1.getPoint(), 6);
        ECMM_Mapping.plot.drawCircle(v1.getPoint(), 12);
      }
      v1 = v1.getNext();
    } while (!v1.isHead());

  }

  /**
   * Forms sectors based on the intPoints inserted by 'calcIntersects'. A sector is simply a
   * pointer to the sectors starting intPoint checkValid();
   */
  private void formSectors() {

    if (ECMp.numINTS == 0) {
      // IJ.error("NO INTERSECTS");
      System.out.println("No Intersects"); // should never happen. fake ones insterted
    }
    sectors = new Sector[ECMp.numINTS];

    Vert vo1 = o1.getHead();
    Vert vo2 = o2.getHead();

    for (int i = 0; i < ECMp.numINTS; i++) {
      do {
        vo1 = vo1.getNext();
      } while (!vo1.isIntPoint());
      do {
        vo2 = vo2.getNext();
        if (vo2.isIntPoint()) {
          if (vo2.intsectID == vo1.intsectID) {
            break; // find matching intersect
          }
        }
      } while (true);

      if (ECMp.numINTS == 1) { // no intersects present, forced or otherwise
        sectors[0] = new Sector(0);
        sectors[0].setStarts(vo1, vo2);
        break;
      } else {
        if (i == 0) {
          sectors[i] = new Sector(i);
          sectors[i].setStarts(vo1, vo2);
          sectors[ECMp.numINTS - 1] = new Sector(ECMp.numINTS - 1); // set as ends for last sector
          // sectors[INTS - 1].setEnds(vo1, vo2);
        } else if (i == ECMp.numINTS - 1) {
          sectors[i].setStarts(vo1, vo2);
          // sectors[i - 1].setEnds(vo1, vo2);
        } else {
          sectors[i] = new Sector(i);
          sectors[i].setStarts(vo1, vo2);
          // sectors[i - 1].setEnds(vo1, vo2);
        }
      }
    }

    if (ECMp.numINTS == 1) { // no intersects present, forced or otherwise
      sectors[0].constructWhole(o1.calcArea(), o2.calcArea());
    } else {
      for (int i = 0; i < ECMp.numINTS; i++) {
        sectors[i].construct(); // calc lengths, determin exp or contr, make charges
        // sectors[i].showPlot();
      }
    }
  }

  /**
   * Migrate outlines.
   * 
   * @return mapped outline. Compute also intensities in {@link Vert#fluores}
   */
  public Outline migrate() {
    Vert newVert; // placed at the marker
    ExtendedVector2d newPos;
    Sector s;

    Vert mapHead = new Vert(-1);
    Outline mappedOutline = new Outline(mapHead);
    Vert currentMapVert = mapHead;
    for (int i = 0; i < sectors.length; i++) {
      s = sectors[i];
      Vert v = s.getMigStart().getNext(); // starting vert, don't migrate the intpoint

      do {
        // if (ECMp.chargeDensity != -1) { //nar. polar charges sort
        // this out
        // tempVert = s.addTempCharge(v);
        // }
        // IJ.log("migrating x:" + v.getX() + ", y:" + v.getY());
        // //debug
        newPos = ODEsolver.euler(v, s);
        if (!v.snapped) {
          ECMp.unSnapped++;
          IJ.log("    node failed to map (" + ECMp.unSnapped + ") - removed");
          if (!ECMp.ANA && ECMp.plot && ECMp.drawFails) {
            ECMM_Mapping.plot.writeText("FN(" + ECMp.unSnapped + ")");
          }
          v = v.getNext();

          // System.out.println("sector expand: " +s.expanding+",
          // trueExpand: "+s.trueExpand + ", outDirection: " +
          // s.outerDirection);
          // s.tarCharges.print();
          // s.migCharges.print();
          continue;
        }

        newVert = mappedOutline.insertVert(currentMapVert);
        newVert.tarLandingCoord = v.fLandCoord; // so we always have a reference to where we landed
        if (s.expansion) { // expanding or retracting based on area change (not length of sector)
          newVert.distance = -v.distance; // ?????????????? why neg
        } else {
          newVert.distance = v.distance;
        }

        if (!s.forwardMap) {
          newVert.setX(v.getX());
          newVert.setY(v.getY());
          newVert.gCoord = v.gLandCoord;// + 1;
          newVert.fCoord = v.fLandCoord;// + 1;
        } else {
          newVert.setX(newPos.getX());
          newVert.setY(newPos.getY());
          newVert.gCoord = v.gCoord;
          newVert.fCoord = v.coord;
        }

        if (ECMp.ANA) {
          // newVert.fluores = v.cloneFluo();
          newVert.setFluores(v.fluores);
          newVert.setTrackNum(v.getTrackNum());
        }
        currentMapVert = newVert;
        v = v.getNext();
      } while (!v.isIntPoint());

    }
    mappedOutline.removeVert(mapHead);

    return mappedOutline;
  }

  public Sector getSector(int i) {
    if (i < 0 || i > ECMp.numINTS) {
      IJ.error("sectors out of bounds - 250");
    }
    return sectors[i];
  }

}