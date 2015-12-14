

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final Logger logger = LogManager.getLogger(DICReconstruction_.class.getName());
	private DICReconstruction dic;
	private ImagePlus imp;
	
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
			ret = dic.reconstructionDicLid();
			ip.setPixels(ret.getPixels());
		} catch (DicException e) {
			logger.error(e);
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
