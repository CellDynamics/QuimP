package com.github.celldynamics.quimp.plugin.ecmm;

import java.util.ArrayList;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.IJ;
import ij.process.FloatPolygon;

/**
 * A mapping between nodes?.
 * 
 * @author rtyson
 *
 */
class Sector {

  private int id;
  private Vert startO1;
  private Vert startO2;
  public Outline migCharges;
  public Outline tarCharges;
  FloatPolygon chargesPoly;
  FloatPolygon innerPoly;
  FloatPolygon outerPoly; // if no intersects have to use these
  public double lengthO1;
  public double lengthO2;
  public int vertSo1;
  public int vertSo2; // num verts in 01 and o2
  public boolean forwardMap; // mapping forward or reverse?
  /**
   * the cell expanding here. is segment T to the left or right of segment T+1.
   */
  public boolean expansion;
  /**
   * the other direction of the normals of migration charges.
   */
  public double outerNormal;

  /**
   * Constructor of sector.
   * 
   * @param i id
   */
  public Sector(int i) {
    id = i;
  }

  /**
   * Print Sector.
   */
  public void print() {
    IJ.log("Sector " + id + "\nMig charges: ");
    migCharges.print();
    IJ.log("");

    IJ.log("tar charges: ");
    tarCharges.print();
  }

  /**
   * Construct.
   */
  public void construct() {
    // calc lengths, determin expansion, set charges
    calcLengths();
    // left or right? Use the "left" algorithm (sign of triangle area)
    double sectorTriArea = ExtendedVector2d.triangleArea(startO1.getPoint(),
            startO1.getNext().getPoint(), startO2.getNext().getPoint());

    if ((lengthO1 > lengthO2) || ECMp.forceForwardMapping) {
      forwardMap = true;
      migCharges = formCharges(startO1);
      tarCharges = formCharges(startO2);
      if (sectorTriArea > 0) {
        expansion = true; //
        outerNormal = -1.;
      } else {
        expansion = false; //
        outerNormal = 1.;
      }
    } else {
      forwardMap = false; // backward in time
      migCharges = formCharges(startO2);//
      tarCharges = formCharges(startO1);
      if (sectorTriArea > 0) {
        expansion = true; //
        outerNormal = 1.;

      } else {
        expansion = false; //
        outerNormal = -1.;
      }
    }

    Vert v = migCharges.getHead();
    ExtendedVector2d normal;
    do {
      normal = new ExtendedVector2d(v.getNormal().getX(), v.getNormal().getY());
      normal.multiply(outerNormal * ECMp.w);
      v.getPoint().addVec(normal);
      v = v.getNext();
    } while (!v.isHead());

    if (ECMp.chargeDensity != -1) {
      migCharges.setResolution(ECMp.chargeDensity);
      tarCharges.setResolution(ECMp.chargeDensity);
    }

    // create polygon off all charges for cal point inside/outside sector
    chargesPolygon();

  }

  /**
   * constructWhole.
   * 
   * @param area1 area1
   * @param area2 area2
   */
  public void constructWhole(double area1, double area2) {
    Outline innerCharges;
    Outline outerCharges;

    calcLengths();

    if (((lengthO1 > lengthO2) || ECMp.forceForwardMapping || ECMp.ANA)
            && !ECMp.forceBackwardMapping) {
      forwardMap = true;
      migCharges = formCharges(startO1);
      tarCharges = formCharges(startO2);
      if (area1 > area2) {
        expansion = false; // n is migrating from outside in
        outerNormal = 1.;
        innerCharges = tarCharges;
        outerCharges = migCharges;
      } else {
        expansion = true; // n is migrating from inside out
        outerNormal = -1.;
        innerCharges = migCharges;
        outerCharges = tarCharges;
      }
    } else {
      forwardMap = false; // backward in time
      migCharges = formCharges(startO2);//
      tarCharges = formCharges(startO1);
      if (area1 > area2) {
        expansion = true; // n+1 is migrating from inside out, expansion
        outerNormal = -1.;
        innerCharges = migCharges;
        outerCharges = tarCharges;
      } else {
        expansion = false; // n+1 is migrating from outside in, contraction
        outerNormal = 1.;
        innerCharges = tarCharges;
        outerCharges = migCharges;
      }
    }

    Vert v = migCharges.getHead();
    ExtendedVector2d normal;
    do {
      normal = new ExtendedVector2d(v.getNormal().getX(), v.getNormal().getY());
      normal.multiply(outerNormal * ECMp.w);
      v.getPoint().addVec(normal);
      v = v.getNext();
    } while (!v.isHead());

    if (ECMp.chargeDensity != -1) {
      migCharges.setResolution(ECMp.chargeDensity);
      tarCharges.setResolution(ECMp.chargeDensity);
    }

    outerPoly = ioPolygons(outerCharges);
    innerPoly = ioPolygons(innerCharges);

  }

  public void setStarts(Vert a, Vert b) {
    startO1 = a;
    startO2 = b;
  }

  private void calcLengths() {
    lengthO1 = 0.;
    vertSo1 = 0;
    Vert v = startO1;
    do {
      lengthO1 += ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      vertSo1++;
      v = v.getNext();
    } while (!v.isIntPoint());

    lengthO2 = 0.;
    vertSo2 = 0;
    v = startO2;
    do {
      lengthO2 += ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      vertSo2++;
      v = v.getNext();
    } while (!v.isIntPoint());

    // double t = lengthO1;
    // lengthO1 = lengthO2;
    // lengthO2 = t;
  }

  private Outline formCharges(Vert s) {
    // create a new outline from the sector starting at s
    Vert newV = new Vert(s.getX(), s.getY(), 1);
    newV.setNormal(s.getNormal().getX(), s.getNormal().getY());
    newV.setIntPoint(true, -1);
    Outline o = new Outline(newV);

    s = s.getNext();
    do {
      newV = o.insertVert(newV);
      newV.setX(s.getX());
      newV.setY(s.getY());
      newV.setNormal(s.getNormal().getX(), s.getNormal().getY());

      if (s.isIntPoint()) {
        newV.setIntPoint(true, -1);
      }

      s = s.getNext();
    } while (!s.getPrev().isIntPoint()); // copy the int point too

    return o;
  }

  public Vert getMigStart() { // NOT CHARGES!
    if (forwardMap) {
      return startO1;
    } else {
      return startO2;
    }
  }

  public Vert getTarStart() { // NOT CHARGES
    if (forwardMap) {
      return startO2;
    } else {
      return startO1;
    }
  }

  public Vert addTempCharge(Vert tv) {
    // inserts a temporary charge into the charged nodes to ensure a
    // migrating node
    // remains within the boubdary of the outline. Have to find where to
    // insert it though.
    Vert v = migCharges.getHead();
    double dis = 99999.;
    double cdis;
    Vert closest = v;
    do {
      cdis = ExtendedVector2d.distPointToSegment(tv.getPoint(), v.getPoint(),
              v.getNext().getPoint());
      if (cdis < dis) {
        closest = v;
        dis = cdis;
      }
      v = v.getNext();
    } while (!v.isHead());

    Vert newVert = migCharges.insertVert(closest);
    newVert.setTrackNum(-35);
    newVert.setX(tv.getX());
    newVert.setY(tv.getY());
    ExtendedVector2d normal = new ExtendedVector2d(tv.getNormal().getX(), tv.getNormal().getY());
    normal.multiply(outerNormal * ECMp.w);
    newVert.getPoint().addVec(normal);
    newVert.updateNormale(true);
    return newVert;
  }

  public void removeTempCharge(Vert v) {
    migCharges.removeVert(v);
  }

  private void chargesPolygon() {
    ArrayList<ExtendedVector2d> points = new ArrayList<ExtendedVector2d>();

    Vert v = migCharges.getHead(); // get charges from head to int point, forward
    do {

      points.add(v.getPoint());
      if (v.isIntPoint() && !v.isHead()) {
        break;
      }
      v = v.getNext();
    } while (!v.isHead());

    // find int point in tar
    v = tarCharges.getHead();
    do {
      v = v.getNext();
    } while (!v.isIntPoint());

    // get tar charges in reverse
    do {
      points.add(v.getPoint());
      v = v.getPrev();
    } while (!v.getNext().isHead());

    // create floats
    float[] x = new float[points.size()];
    float[] y = new float[points.size()];

    ExtendedVector2d p;
    for (int i = 0; i < points.size(); i++) {
      p = (ExtendedVector2d) points.get(i);
      x[i] = (float) p.getX();
      y[i] = (float) p.getY();
    }

    chargesPoly = new FloatPolygon(x, y, x.length);
  }

  private FloatPolygon ioPolygons(Outline charges) { // in and out polygons
    float[] x = new float[charges.getNumPoints()];
    float[] y = new float[charges.getNumPoints()];

    int i = 0;
    Vert v = charges.getHead();
    do {
      x[i] = (float) v.getX();
      y[i] = (float) v.getY();
      i++;
      v = v.getNext();
    } while (!v.isHead());

    return new FloatPolygon(x, y, x.length); // was this
  }

  public boolean insideCharges(ExtendedVector2d p) {
    if (ECMp.numINTS > 1) {
      return chargesPoly.contains((float) p.getX(), (float) p.getY());
    } else {
      if (outerPoly.contains((float) p.getX(), (float) p.getY())) {
        if (innerPoly.contains((float) p.getX(), (float) p.getY())) {
          return false;
        } else {
          return true;
        }
      } else {
        return false;
      }
    }

  }

  public void switchMigDirection() {
    Outline tempO = tarCharges; // swtich charges
    tarCharges = migCharges;
    migCharges = tempO;
    forwardMap = !forwardMap;
  }
}