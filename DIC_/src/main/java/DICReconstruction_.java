

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import uk.warwick.dic.lid.DICReconstruction;
import uk.warwick.dic.lid.DicException;

/**
 * Main implementation of ImageJ plugin
 * TODO Add conditions for images etc.
 * @author p.baniukiewicz
 * @date 14 Dec 2015 
 *
 */
public class DICReconstruction_ implements PlugInFilter {

	private DICReconstruction dic;
	private ImagePlus imp;
	private ImageProcessor ip;
	
	public DICReconstruction_() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		this.imp = imp;
		return DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImageProcessor ret;
		try {
			dic = new DICReconstruction(ip, 0.04, 135);
//			ret = dic.reconstructionDicLid();
			ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
		} catch (DicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			imp.updateAndDraw();
		}

	}
	
	void showAbout() {
		IJ.showMessage("About ",
		"Line...\n"
		);
	}

}
