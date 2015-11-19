/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.warwick.quimp;

/**
 *
 * @author rtyson
 */
public class FluoMeasurement{

   double x, y, intensity;

   public FluoMeasurement(double xx, double yy , double i) {
      x = xx;
      y = yy;
      intensity = i;
   }

   //FluoMeasurement copy(){
      //return new FluoMeasurement( x,y,intensity);
   //}

}
