

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
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
	private GenericDialog gd;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImageProcessor ret;
		double decay;
		double angle;
		buildGUI();
		if(gd.wasCanceled())
			return;
		angle = gd.getNextNumber();
		decay = gd.getNextNumber();
		try {
			dic = new DICReconstruction(ip, decay, angle);
			ret = dic.reconstructionDicLid();
			ip.setPixels(ret.getPixels());
		} catch (DicException e) {
			logger.error(e);
		}
		finally {
			imp.updateAndDraw();
		}

	}
	
	public void buildGUI() {
		gd = new GenericDialog("DIC reconstruction");
		gd.addMessage("Reconstruction of DIC image by Line Integrals\nShear angle is measured counterclockwise");
		gd.addNumericField("Shear angle", 45.0, 0);
		gd.addMessage("Decay factor is usually positive and smaller than 1");
		gd.addNumericField("Decay", 0.0, 2);
		gd.setResizable(false);
		gd.showDialog();
	}

}
