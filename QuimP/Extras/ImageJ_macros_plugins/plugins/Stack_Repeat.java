import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.PlugIn;

/** Inserts an image or stack into a stack.
*/
public class Stack_Repeat implements PlugIn {

	private static int x = 10;
			
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}               

		GenericDialog gd = new GenericDialog("Stack Repeater");
		gd.addNumericField("Num of Repeats: ", x, 0);
                
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		x = (int)gd.getNextNumber();
                
                if(x==0) return;
                if (x<1)
		  {IJ.error("Can not negatively repeat"); return;}
                
                ImageStack stack = imp.getStack();
                int size = stack.getSize();
                
                if(size==1){
                    ImageStack stackNew = imp.createEmptyStack();
                    stackNew.addSlice("", stack.getProcessor(1));
                    String title = imp.getTitle() + "_repeated";
                    ImagePlus imp2 = new ImagePlus(title, stackNew);
                    
                    repeat(imp2, x);
                    imp2.show();
                }else{  
                    repeat(imp, x);
                }
		IJ.register(Stack_Repeat.class);
	}
	
	void repeat(ImagePlus imp, int x) {
		ImageStack stack = imp.getStack();
                int size = stack.getSize();
                
                ImageProcessor ip;
                for(int r=1; r<x; r++){
                    for(int i=1; i<=size; i++){
                        ip = stack.getProcessor(i);
                        stack.addSlice("", ip);
                    }
                }
	}
}