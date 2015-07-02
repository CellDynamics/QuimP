/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.warwick.quimp_11b;

/**
 *
 * @author rtyson
 */
public class NodeMigrator {
   public NodeMigrator() {
   }

   private Vect2d euler(Vert v, Sector s) {
      //Vect2d[] history =  new Vect2d[ECMp.maxIter];
      int x, y;
      double dist = 0; // distance migrated
      double tempFlu;
      Vert edge;
      Vect2d p, pp;

      v.snapped = false;

      if (ECMp.ANA) { // sample at boundary
         p = v.getPoint();
         x = (int) Math.round(p.getX());
         y = (int) Math.round(p.getY());
         tempFlu = this.sampleFluo(x, y);
         v.fluores[0].intensity = tempFlu;
         v.fluores[0].x = x;         // store in first slot
         v.fluores[0].y = y;
      }

      if (ECMp.plot) {
            ECMM_Mapping.plot.setColor(0, 0, 0);
      }

      p = new Vect2d(v.getX(), v.getY());
      pp = new Vect2d(v.getX(), v.getY()); //previouse position

      //history[0] = new Vect2d(p.getX(), p.getY());

      int i = 1;
      Vect2d k;

      for (; i < ECMp.maxIter - 1; i++) {
         //IJ.log("\tIt " + i); //debug
         if (ODEsolver.proximity(p, s) || (ECMp.ANA && dist>=(ECMp.anaMigDist))) {
            // stop when within d of the target segment or
            // if migrated more than the ana set cortex width (in pixels)
            pp.setX(p.getX());
            pp.setY(p.getY());

            if(!ECMp.ANA) {  // no need to snap ana result. landing coord not needed
               edge = this.snap(p, s);
               dist += Vect2d.lengthP2P(pp, p);
               v.distance = Tool.speedToScale(dist, ECMp.scale, ECMp.frameInterval);
               if (s.expansion && !ECMp.ANA) {
                  v.setLandingCoord(p, edge);
               }
            }

            if (ECMp.plot) {
               ECMM_Mapping.plot.setColor(1, 0, 0);
               ECMM_Mapping.plot.drawLine(pp, p);
            }

            v.snapped = true;
            break;
         }

         k = ODEsolver.dydt(p, s);
         k.multiply(ECMp.h);

         pp.setX(p.getX());
         pp.setY(p.getY());
         p.setX(p.getX() + k.getX());
         p.setY(p.getY() + k.getY());
         dist += Vect2d.lengthP2P(pp, p);

         if (ECMp.plot) {
            //ECMM_Mapping.plot.setColor(1, 0, 0);
            ECMM_Mapping.plot.drawLine(pp, p);
         }
         //history[i] = new Vect2d(p.getX(), p.getY());

         if (ECMp.ANA) {
            //v.floures += ECMp.image.getPixelValue((int)Math.round(p.getX()), (int)Math.round(p.getY())); // average

            // tempFlu = ECMp.image.getPixelValue(x, y) + ECMp.image.getPixelValue(x - 1, y)
            //          + ECMp.image.getPixelValue(x + 1, y) + ECMp.image.getPixelValue(x, y - 1)
            //         + ECMp.image.getPixelValue(x, y + 1);
            // tempFlu = tempFlu / 5;

            //tempFlu = ECMp.image.getPixelValue(x, y); // debug
            x = (int) Math.round(p.getX());
            y = (int) Math.round(p.getY());
            tempFlu = this.sampleFluo(x, y);

            if (tempFlu > v.fluores[0].intensity) { //store first one
               v.fluores[0].intensity = tempFlu;
               v.fluores[0].x = x;
               v.fluores[0].y = y;
            }
         }

         ECMp.its++;
      }

      if (ECMp.plot && !v.snapped) { //mark the start point of failed nodes
         ECMM_Mapping.plot.setColor(1, 1, 0);
         //p.print(v.getTrackNum() + "p: ");
         //pp.print(v.getTrackNum() + "pp: ");
         ECMM_Mapping.plot.drawCircle(v.getPoint(), 5);
      }

      return p;
   }

   private Vect2d dydt(Vect2d p, Sector s) {
      Vect2d result = fieldAt(p, s);
      result.multiply(ECMp.mobileQ);

      if (true) {//Math.abs(result.length()) > ECMp.maxVertF) {
         //IJ.log("!WARNING-max force exceeded: " + Math.abs(result.length()));
         result.makeUnit();
         result.multiply(ECMp.maxVertF);
      }
      return result;
   }

   private boolean proximity(Vect2d p, Sector s) {
      // could test against the chrages or the actual contour.
      // if using polar lines can use actual contour
      //Vert v = s.getTarStart();
      //if(true) return false;
      //Vert v = s.tarCharges.getHead();
      Vert v = s.getTarStart();
      do {
         double d = Vect2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
         //IJ.log("\t\tprox to: " + d); //debug
         if (d <= ECMp.d) {
            return true;
         }
         v = v.getNext();
      } while (!v.isIntPoint());
      return false;
   }

   private  Vert snap(Vect2d p, Sector s) {
      // snap p to the closest segment of target contour
      Vect2d closest, current;
      Vert closestEdge;
      double distance;// = ECMp.d + 1; // must be closer then d+1, good starting value
      double tempDis;

      Vert v = s.getTarStart().getPrev(); //include the edge to the starting intersect pount
      distance = Vect2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
      v = v.getNext();
      closest = v.getPoint();
      closestEdge = v;
      do {
         current = Vect2d.PointToSegment(p, v.getPoint(), v.getNext().getPoint());
         tempDis = Vect2d.lengthP2P(p, current);

         if (tempDis < distance) {
            closest = current;
            closestEdge = v;
            distance = tempDis;
         }
         v = v.getNext();
      } while (!v.isIntPoint());

      //p.setX(closest.getX());
      //p.setY(closest.getY());

      return closestEdge;
   }

   Vect2d fieldAt(Vect2d p, Sector s) {
      // override
      return new Vect2d(0,0);
   }

   private double sampleFluo(int x, int y) {
      double tempFlu = ECMp.image.getPixelValue(x, y) + ECMp.image.getPixelValue(x - 1, y)
              + ECMp.image.getPixelValue(x + 1, y) + ECMp.image.getPixelValue(x, y - 1)
              + ECMp.image.getPixelValue(x, y+1) + ECMp.image.getPixelValue(x-1, y - 1)
              + ECMp.image.getPixelValue(x + 1, y+1) + ECMp.image.getPixelValue(x+1, y - 1)
              + ECMp.image.getPixelValue(x-1, y + 1);
      tempFlu = tempFlu / 9d;
      return tempFlu;
   }
}
