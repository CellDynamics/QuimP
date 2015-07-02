/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.warwick.quimp_11b;

import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import java.io.File;

/**
 *
 * @author rtyson
 */
public class Scale_Correction implements PlugIn {

   @Override
   public void run(String arg) {

      try {

         OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", OpenDialog.getLastDirectory(), "");
         if (od.getFileName() == null) {
            return;
         }
         File paramFile = new File(od.getDirectory(), od.getFileName());
         //qp = new QParams(paramFile);
         //qp.readParams();


      }catch (Exception e){
         
      }
   }

   }
