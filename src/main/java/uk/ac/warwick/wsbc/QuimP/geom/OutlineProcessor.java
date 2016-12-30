package uk.ac.warwick.wsbc.QuimP.geom;

import uk.ac.warwick.wsbc.QuimP.Outline;
import uk.ac.warwick.wsbc.QuimP.Vert;

/**
 * Support processing outlines.
 * 
 * @author p.baniukiewicz
 *
 */
public class OutlineProcessor {
    private Outline o;

    /**
     * 
     * @param o Reference to Outline to be processed
     */
    public OutlineProcessor(Outline o) {
        this.o = o;
    }

    /**
     * Shrink outline.
     * 
     * @param steps Number of steps
     * @param delta shift done in one step
     * @param angleTh
     * @param freezeTh
     */
    public void shrink(double steps, double stepRes, double angleTh, double freezeTh) {
        // System.out.println("steps: " + steps + ", step size: " +
        // ANAp.stepRes);
        Vert n;
        int j;
        int max = 10000;
        for (j = 0; j < steps; j++) {
            if (o.getNumVerts() <= 3) {
                break;
            }
            n = o.getHead();
            do {
                if (!n.frozen) {
                    n.setX(n.getX() - stepRes * n.getNormal().getX());
                    n.setY(n.getY() - stepRes * n.getNormal().getY());
                }
                n = n.getNext();
            } while (!n.isHead());
            o.updateNormales(true);
            removeProx();
            freezeProx(angleTh, freezeTh);
            if (j > max) {
                System.out.println("shrink (336) hit max iterations!");
                break;
            }
        }

        if (o.getNumVerts() < 3) {
            System.out.println("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
        }

        if (o.cutSelfIntersects()) {
            System.out.println("ANA_(382)...fixed ana intersects");
        }

        if (o.getNumVerts() < 3) {
            System.out.println("ANA 377_NODES LESS THAN 3");
        }
    }

    private void removeProx() {
        if (o.getNumVerts() <= 3) {
            return;
        }
        Vert v, vl, vr;
        double d1, d2;
        v = o.getHead();
        vl = v.getPrev();
        vr = v.getNext();
        do {
            d1 = ExtendedVector2d.lengthP2P(v.getPoint(), vl.getPoint());
            d2 = ExtendedVector2d.lengthP2P(v.getPoint(), vr.getPoint());

            if ((d1 < 1.5 || d2 < 1.5) && !v.frozen) { // don't remove frozen.
                                                       // May alter angles
                o.removeVert(v);
            }
            v = v.getNext().getNext();
            vl = v.getPrev();
            vr = v.getNext();
        } while (!v.isHead() && !vl.isHead());

    }

    private void freezeProx(double angleTh, double freezeTh) {
        // freeze a node and corresponding edge if its to close && close to
        // paralel
        Vert v, vT;
        ExtendedVector2d closest, edge, link;
        double dis, angle;

        v = o.getHead();
        do {
            // if (!v.frozen) {
            vT = o.getHead();
            do {
                if (vT.getTrackNum() == v.getTrackNum()
                        || vT.getNext().getTrackNum() == v.getTrackNum()) {
                    vT = vT.getNext();
                    continue;
                }
                closest = ExtendedVector2d.PointToSegment(v.getPoint(), vT.getPoint(),
                        vT.getNext().getPoint());
                dis = ExtendedVector2d.lengthP2P(v.getPoint(), closest);
                // System.out.println("dis: " + dis);
                // dis=1;
                if (dis < freezeTh) {
                    edge = ExtendedVector2d.unitVector(vT.getPoint(), vT.getNext().getPoint());
                    link = ExtendedVector2d.unitVector(v.getPoint(), closest);
                    angle = Math.abs(ExtendedVector2d.angle(edge, link));
                    if (angle > Math.PI)
                        angle = angle - Math.PI; // if > 180, shift back around
                                                 // 180
                    angle = angle - 1.5708; // 90 degree shift to centre around
                                            // zero
                    // System.out.println("angle:" + angle);

                    if (angle < angleTh && angle > -angleTh) {
                        v.frozen = true;
                        vT.frozen = true;
                        vT.getNext().frozen = true;
                    }

                }
                vT = vT.getNext();
            } while (!vT.isHead());
            // }
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * @return the o
     */
    public Outline getO() {
        return o;
    }

}
