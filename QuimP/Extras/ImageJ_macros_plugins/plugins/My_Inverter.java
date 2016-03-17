import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class My_Inverter implements PlugInFilter {
	
	public int setup (String arg, ImagePlus im) {
		return DOES_8G;	// accepts 8 bit greyscale
	}
	
	public void run (ImageProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		// iterate over all pixels
		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				int p = ip.getPixel(u,v);
				ip.putPixel(u,v,255-p); // invert
			}
		}
	}
	
} // end of class My_Inverter

