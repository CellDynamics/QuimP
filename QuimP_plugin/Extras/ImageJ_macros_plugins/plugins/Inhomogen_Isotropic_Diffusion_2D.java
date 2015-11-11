/******************************************************************************\
* 2D Inhomogen Isotropic Diffusion Perona-Malik Filtering Plug-in for ImageJ   *
* version 1.0en, 2006/09/12                                                    *
* written by Jan-Friedrich Ehlenbröker, Alexander Maier, Uwe Mönks, Stefan     *
* Schwalowsky and Matthias Tobergte                                            *
* This plug-in is designed to perform Filtering on an 8-bit image. Output is   *
* generated as a stack to view every single result of each iteration.          *
* Implemented is the Perona-Mailk Function for diffusion filtering with two    *
* different functions to calculate the diffusion coefficients with.            *
\******************************************************************************/


// Importing standard Java API Files and ImageJ packages
import java.lang.Math;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import ij.plugin.frame.*;
import javax.swing.*;
import java.awt.event.*;

public class Inhomogen_Isotropic_Diffusion_2D implements PlugInFilter {
        // Input parameters
        static int runs = 20; // Number of iterations
        static double lambda = 10.0; // Parameter used to manipulate calculation of the diffusion coefficient
        static double dt = 0.1; // Used to define the weight of every iteration
        static boolean imageStack = true; // Show each result of every iteration
        static boolean eightPixel = false; // Use 8 neighbour pixels to calculate the new value of the pixel
        
        // Temporary variables for the GUI
        static boolean d1 = false;
        static boolean d2 = true;
        static double wert = (1.0/7);
    
        ImageProcessor copy;
	ImagePlus imp;
        ImageStack ims;
        
        StringBuffer debug = new StringBuffer();
        
	public int setup(String arg, ImagePlus imp) {
            this.imp = imp;
            return DOES_ALL;
	}
        
        // Generate GUI and process image
        public void run(ImageProcessor ip) {
            gui(ip);
        }
        
	private void filter(ImageProcessor ip) {
            IJ.showProgress(0, runs + 1);
            
            int w = ip.getWidth();
            int h = ip.getHeight();
            
            ims = new ImageStack(w, h);
            
            // Read each pixel's valus and keep it
            double[][] pic = new double[w][h];
            
            for (int u = 0; u < w; u++) {
                   for (int v = 0; v < h; v++) {
                        pic[u][v] = ip.getPixel(u, v);
                   }
            }
            
            // Add the original picture to the images stack
            if (imageStack) {
                copy = ip.duplicate();
                ims.addSlice("Original", copy);
            }
            
            // Filter the saved pixel values as often as defined by runs
            double [][] picNew = pic;
            for (int r = 0; r < this.runs; r++) {
                for (int u = 0; u < w; u++) {
                    for (int v = 0; v < h; v++) {
                        double grayThis = pic[u][v];
                        
                        // Read the neighbour pixels' values
                        double grayEast;
                        double grayWest;
                        
                        double grayNorth;
                        double graySouth;
                        
                        double grayNorthEast;
                        double graySouthWest;
                        double grayNorthWest;
                        double graySouthEast;
                        
                        if (u == 0) {
                            grayWest = grayThis;
                        }
                        else {
                            grayWest = pic[u - 1][v];
                        }
                        if (u == w - 1) {
                            grayEast = grayThis;
                        }
                        else grayEast = pic[u + 1][v];
                        
                        if (v == h - 1) {
                            graySouth = grayThis;
                        }
                        else graySouth = pic[u][v + 1];
                        if (v == 0) {
                            grayNorth = grayThis;
                        }
                        else grayNorth = pic[u][v - 1];
                        
                        // Calculate every diffusion coefficient
                        double dEast = diffusionCoefficient(grayThis, grayEast);
                        double dWest = diffusionCoefficient(grayThis, grayWest);
                        
                        double dNorth = diffusionCoefficient(grayThis, grayNorth);
                        double dSouth = diffusionCoefficient(grayThis, graySouth);
                        
                        // Calculate the difference between the actual and the
                        // new value of the calculated pixel
                        double grayDelta = (dEast * (grayEast - grayThis))-
                                (dWest * (grayThis - grayWest)) -
                                (dNorth * (grayThis - grayNorth)) +
                                (dSouth * (graySouth - grayThis));
                        
                        // Do the same, when chosen to use 8 neighbours
                        if (eightPixel) {
                            if ((u == 0) && (v == 0)) {
                                graySouthWest = graySouth;
                            }
                            else if ((u == 0) && (v == h - 1)) {
                                graySouthWest = grayThis;
                            }
                            else if ((u == w - 1) && (v == h - 1)) {
                                graySouthWest = grayWest;
                            }
                            else if (u == 0) {
                                graySouthWest = graySouth;
                            }
                            else if (v == h - 1) {
                                graySouthWest = grayWest;
                            }
                            else graySouthWest = pic[u - 1][v + 1];


                            if ((u == 0) && (v == 0)) {
                                grayNorthEast = grayEast;
                            }
                            else if ((u == w - 1) && (v == 0)) {
                                grayNorthEast = grayThis;
                            }
                            else if ((u == w - 1) && (v == h - 1)) {
                                grayNorthEast = grayNorth;
                            }
                            else if (u == w - 1) {
                                grayNorthEast = grayNorth;
                            }
                            else if (v == 0) {
                                grayNorthEast = grayEast;
                            }
                            else grayNorthEast = pic[u + 1][v - 1];


                            if ((u == 0) && (v == 0)) {
                                grayNorthWest = grayThis;
                            }
                            else if ((u == w - 1) && (v == 0)) {
                                grayNorthWest = grayWest;
                            }
                            else if ((u == 0) && (v == h - 1)) {
                                grayNorthWest = grayNorth;
                            }
                            else if (u == 0) {
                                grayNorthWest = grayNorth;
                            }
                            else if (v == 0) {
                                grayNorthWest = grayWest;
                            }
                            else grayNorthWest = pic[u - 1][v - 1];


                            if ((u == w - 1) && (v == 0)) {
                                graySouthEast = graySouth;
                            }
                            else if ((u == w - 1) && (v == h - 1)) {
                                graySouthEast = grayThis;
                            }
                            else if ((u == 0) && (v == h - 1)) {
                                graySouthEast = grayEast;
                            }
                            else if (u == w - 1) {
                                graySouthEast = graySouth;
                            }
                            else if (v == h - 1) {
                                graySouthEast = grayEast;
                            }
                            else graySouthEast = pic[u + 1][v + 1];


                            double dNorthEast = diffusionCoefficient(grayThis, grayNorthEast);
                            double dSouthWest = diffusionCoefficient(grayThis, graySouthWest);

                            double dNorthWest = diffusionCoefficient(grayThis, grayNorthEast);
                            double dSouthEast = diffusionCoefficient(grayThis, graySouthWest);
                       
                            grayDelta += -(1.0/2) * (dNorthEast * (grayThis - grayNorthEast)) +
                                    (1.0/2) * (dSouthWest * (graySouthWest - grayThis)) +
                                    (1.0/2) * (dSouthEast * (graySouthEast - grayThis)) -
                                    (1.0/2) * (dNorthWest * (grayThis - grayNorthWest));
                        }
                        
                        // Save new pixel
                        picNew[u][v] += dt * grayDelta;
                    }
                }
                
                // Save the new image to the stack
                if (imageStack) {
                    copy = ip.duplicate();
                    for (int u = 0; u < w; u++) {
                        for (int v = 0; v < h; v++) {                           
                            copy.putPixel(u, v, (int) picNew[u][v]);
                        }
                    }
                    ims.addSlice("Iteration " + (r + 1), copy);
                }
                
                pic = picNew;
                
                IJ.showProgress(r, runs + 1);
            }
            
            // Overwrite the original picture with the new one
            for (int u = 0; u < w; u++) {
                   for (int v = 0; v < h; v++) {
                        ip.putPixel(u, v, (int) pic[u][v]);
                   }
            }
            
            if (imageStack) {
                WindowManager.setCurrentWindow(new StackWindow(new ImagePlus("Iteration results", ims)));
            }
            
            IJ.showProgress(1.0);
	}
        
        // Calculate diffusion coefficients
        private double diffusionCoefficient(double grayFirstPix, double graySecondPix) {
            double diffCoeff;
            
            // d2 is the 1/x^2 function for the difusion coefficient
            if (d2) {
                diffCoeff = (lambda * lambda) /
                        ((lambda * lambda) + Math.pow(Math.abs(grayFirstPix - graySecondPix),2));
            }
            // d1 is the exp() function
            else if (d1) {
                diffCoeff = Math.exp(-1 * Math.pow((Math.abs(grayFirstPix - graySecondPix) / lambda),2));
            }
            else {
                diffCoeff = 0;
            }
            
            return diffCoeff;
        }
        
        // Show GUI and process Image after clicking OK
        private void gui(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Inhomogen Isotropic Diffusion 2D");
        gd.addMessage("                                               " +
                "Version 1.0en      2006/09/12");
        gd.addMessage("");
        gd.addNumericField("Lambda:", lambda, 1);
        gd.addNumericField("Iterations:", runs, 0);
        gd.addNumericField("dt:", dt, 2);
        gd.addCheckbox("Show result of each iteration" , imageStack);
        gd.addCheckbox("Use 8 neighbour pixels instead of 4 for filtering",
                eightPixel);
        gd.addMessage("");
        gd.addMessage("Function used to calculate the diffusion coefficient:");

        Panel bild = new Panel(new BorderLayout());
        Panel bild1=new Panel(new BorderLayout());
        Panel bild2=new Panel();

        final Checkbox D1 = new Checkbox("Gauss");
        final Checkbox D2 = new Checkbox("Alternative (recommended)");
        bild.add(bild1, BorderLayout.EAST);
        bild.add(bild2, BorderLayout.WEST);
        bild1.add(D1, BorderLayout.NORTH);
        bild1.add(D2, BorderLayout.SOUTH);
        gd.addPanel(bild);
        gd.addMessage("Copyright (c) 2006\nJan-Friedrich Ehlenbröker, Alexander " +
                "Maier,\nUwe Mönks, Stefan Schwalowsky, Matthias Tobergte");

        D1.setState(d1);
        D2.setState(d2);

        D1.addItemListener(new ItemListener()
        {
           public void itemStateChanged(ItemEvent e)
           {
            d1=true;
            d2=false;
            D1.setState(true);
            D2.setState(false);
           }
        });

        D2.addItemListener(new ItemListener()
        {
          public void itemStateChanged(ItemEvent e)
          {
            d1=false;                               
            d2=true;
            D1.setState(false);
            D2.setState(true);   
          }
        }); 


        gd.showDialog();      
        if (gd.wasCanceled()) 
        {
        return;
        }

        // Read user inputs
        lambda = (double) gd.getNextNumber();
        runs = (int) gd.getNextNumber();
        dt = (double) gd.getNextNumber();
        imageStack = (boolean) gd.getNextBoolean();
        eightPixel = (boolean) gd.getNextBoolean();

        if(wert < dt)
        {
          try{
              JOptionPane abfrage = new JOptionPane();
              abfrage.showMessageDialog(null, "dt has to be less than 1/7.\n" +
                      "Please restart the Plugin.",
                      "Warning",JOptionPane.ERROR_MESSAGE);      
              }
              catch ( HeadlessException b)
              {
               System.out.println("Error " + b);
               return;
               }
               }
               else
               {
                filter(ip);
               }
        }

}
