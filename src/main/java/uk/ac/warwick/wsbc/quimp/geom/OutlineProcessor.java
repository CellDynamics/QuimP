package uk.ac.warwick.wsbc.quimp.geom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.plugin.ana.ANA_;

// TODO: Auto-generated Javadoc
/**
 * Support algorithms for processing outlines.
 * 
 * @author p.baniukiewicz
 * @see ANA_
 */
public class OutlineProcessor {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(OutlineProcessor.class.getName());

    private Outline o;

    /**
     * 
     * @param o Reference to Outline to be processed
     */
    public OutlineProcessor(Outline o) {
        this.o = o;
    }

    /**
     * Compute running mean on <tt>curvatureLocal</tt>.
     * 
     * FIXME There is no looping, first and last vertexes are skipped.
     * 
     * @param window Window size
     * @return array of filtered coefficients in order of vertexes.
     */
    public double[] runningmeanfilter(int window) {
        int half = window / 2;
        // copy to array
        double[] curv = new double[o.getNumVerts()];
        double[] curvf = new double[o.getNumVerts()];
        Vert n;
        int l = 0;
        n = o.getHead();
        do {
            curv[l] = n.curvatureLocal;
            n = n.getNext();
            l++;
        } while (!n.isHead());
        // LOGGER.debug(
        // "Min=" + QuimPArrayUtils.arrayMin(curv) + " Max=" + QuimPArrayUtils.arrayMax(curv));

        for (int i = half; i < curv.length - 1 - half; i++) {
            double min = 0;
            for (int inner = i - half; inner <= i + half; inner++) {
                // if (curv[inner] < min)
                min += curv[inner];
            }
            curvf[i] = min / window;
        }

        n = o.getHead();
        l = 0;
        do {
            n.curvatureLocal = curvf[l];
            n = n.getNext();
            l++;
        } while (!n.isHead());

        return curvf;

    }

    /**
     * Shrink the outline nonlinearly.
     * 
     * @param steps
     * @param stepRes
     * @param angleTh
     * @param freezeTh
     * @see #shrink(double, double, double, double)
     */
    public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh) {
        LOGGER.debug("Steps: " + steps);
        LOGGER.debug("Original res: " + o.getNumVerts());
        int meanmasksize = 5;
        // System.out.println("steps: " + steps + ", step size: " +
        // ANAp.stepRes);
        Vert n;
        int j;
        int max = 10000;
        double d = o.getLength() / o.getNumVerts();

        for (j = 0; j < steps; j++) {
            runningmeanfilter(meanmasksize);
            if (o.getNumVerts() <= 3) {
                break;
            }
            n = o.getHead();
            do {
                if (!n.frozen) {
                    n.setX(n.getX() - stepRes * 1.0 * n.getNormal().getX());
                    n.setY(n.getY() - stepRes * 1.0 * n.getNormal().getY());
                }
                n = n.getNext();
            } while (!n.isHead());

            removeProx();
            freezeProx(angleTh, freezeTh);
            // double d = o.getLength() / o.getNumVerts();
            o.correctDensity(d, d / 2);
            o.updateNormales(true);
            o.updateCurvature();

            // do not shrink if there are 4 nodes or less
            if (o.getNumPoints() <= 4) {
                LOGGER.debug("Stopped iterations");
                break;
            }

            if (j > max) {
                LOGGER.warn("shrink (336) hit max iterations!");
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

        // LOGGER.debug("Shrank Verts: " + o.getNumVerts());
        // LOGGER.debug("Verts after density correction: " + o.getNumVerts());
        // LOGGER.debug("Density " + d + " [" + d / 4 + "," + d / 2 + "]");
    }

    private double fcn(double curv) {
        double ret;
        if (curv >= 0)
            ret = 1.0;
        else
            ret = 1 + 10 * (Math.exp(-curv * 0.5) / Math.exp(0.5));
        return ret;
    }

    /**
     * Shrink the outline linearly.
     * 
     * @param steps Number of steps
     * @param stepRes shift done in one step
     * @param angleTh
     * @param freezeTh
     */
    public void shrink(double steps, double stepRes, double angleTh, double freezeTh) {
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
                LOGGER.warn("shrink (336) hit max iterations!");
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

    /**
     * Remove close vertexes.
     */
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

            if ((d1 < 1.5 || d2 < 1.5) && !v.frozen) { // don't remove frozen. May alter angles
                o.removeVert(v);
            }
            v = v.getNext().getNext();
            vl = v.getPrev();
            vr = v.getNext();
        } while (!v.isHead() && !vl.isHead());

    }

    /**
     * Freeze a node and corresponding edge if its to close && close to paralel.
     * 
     * @param angleTh
     * @param freezeTh
     */
    private void freezeProx(double angleTh, double freezeTh) {
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
