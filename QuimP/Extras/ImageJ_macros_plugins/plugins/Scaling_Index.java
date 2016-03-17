import apple.laf.CoreUIConstants.ShowArrows;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.Map;
import java.util.HashMap;

/**
*
* @author Mike
* @version 05-Feb-2010
*/
public class Scaling_Index implements PlugInFilter{

   private static int r1=3;
   private static int r2=9;
   private static Convolver runKernel = new Convolver();
   private static Map<Integer,float[]> kernelCache  = new HashMap<Integer,float[]>();

   public Scaling_Index(){
       runKernel.setNormalize(false);

   }

   public int setup(String arg0, ImagePlus arg1) {
       if(getParams())
           return DOES_32+DOES_16+DOES_8G+CONVERT_TO_FLOAT;
       else
           return DONE;
   }

   private boolean getParams(){
       GenericDialog gd = new GenericDialog("Scaling Index");
       gd.addNumericField("r1", r1, 0);
       gd.addNumericField("r2", r2, 0);
       gd.showDialog();
       if(gd.wasCanceled())
           return false;
       r1 = (int)gd.getNextNumber();
       r2 = (int)gd.getNextNumber();
       return true;
   }

   public void run(ImageProcessor image) {
       new ImagePlus("result", run(image,r1,r2)).show();
   }

   public static ImageProcessor run(ImageProcessor image, int r1, int r2){

       FloatProcessor convolved1 = convolveWith(image,r1);
       FloatProcessor convolved2 = convolveWith(image,r2);
       convolved2.copyBits(convolved1, 0, 0, Blitter.SUBTRACT);

       double value = 1/(Math.log(r2)-Math.log(r1));
       convolved2.multiply(value);

       return convolved2;
   }

   private static FloatProcessor convolveWith(ImageProcessor image, int radius){
       ImageProcessor convolved =  image.duplicate().convertToFloat();
       float[] kernel = getKernel(radius);
       //IJ.log("kernel size: "+kernel.length);
       runKernel.convolveFloat(convolved, kernel, 3+radius*2, 3+radius*2);
       convolved.log();
       //new ImagePlus("r"+radius,convolved).show();
       return (FloatProcessor)convolved;
   }

   private static float[] getKernel(int radius){
       if(kernelCache.containsKey(radius))
           return kernelCache.get(radius);

       FloatProcessor kernel = new FloatProcessor(3+radius*2,3+radius*2);
       kernel.setColor(1);
       kernel.fillOval(1,1, radius*2+1, radius*2+1);

       //ImageProcessor ikp = kernel.duplicate();
       //ImagePlus ik = new ImagePlus("kernel",ikp);

       new ImagePlus("kernel",kernel).show();
       
       // Some versions of ImageJ have BLUR_MORE as a protected field so this
       // line doesn't work...
       kernel.filter(ImageProcessor.BLUR_MORE);
       // If that is the case, try these lines instead:
       //GaussianBlur gb = new GaussianBlur();
       //gb.blurFloat(kernel, 0.5, 0.5, 0.001);


       float[] p = (float[])kernel.getPixels();
       kernelCache.put(radius, p);
       return p;
   }


}