package uk.ac.warwick.wsbc.tools.images;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.tools.images.Interpolate;

@RunWith(Parameterized.class)
public class Interpolate_Test {
	
	private List<Vector2d> testcase;
	private Double smooth;
	private Path testfileName;
	private static final Logger logger = LogManager.getLogger(Interpolate_Test.class.getName());

	/**
	 * Parameterized constructor.
	 * 
	 * Each parameter should be placed as an argument here
     * Every time runner triggers, it will pass the arguments
     * from parameters we defined in primeNumbers() method
     * 
	 * @param testFileName test file name
	 * @param smooth smoothing value
	 * @see dataLoader
	 */
	public Interpolate_Test(String testFileName, Double smooth) {
		this.testfileName = Paths.get(testFileName);
		this.smooth = smooth;
	}

	/**
	 * Called after construction but before tests
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		testcase = new dataLoader(testfileName.toString()).getData();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Set of parameters for tests.
	 * 
	 * @return List of strings with paths to testfiles and smooth parameter
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> testFiles() {
		return Arrays.asList(new Object[][] {
			{"src/test/resources/testData_75.dat",0.02},
			{"src/test/resources/testData_75.dat",0.04},
			{"src/test/resources/testData_75.dat",0.06},
			{"src/test/resources/testData_75.dat",0.08},
			{"src/test/resources/testData_75.dat",0.1},
			
			{"src/test/resources/testData_125.dat",0.02},
			{"src/test/resources/testData_125.dat",0.04},
			{"src/test/resources/testData_125.dat",0.06},
			{"src/test/resources/testData_125.dat",0.08},
			{"src/test/resources/testData_125.dat",0.1},
			
			{"src/test/resources/testData_137.dat",0.02},
			{"src/test/resources/testData_137.dat",0.04},
			{"src/test/resources/testData_137.dat",0.06},
			{"src/test/resources/testData_137.dat",0.08},
			{"src/test/resources/testData_137.dat",0.1},
			
			{"src/test/resources/testData_1.dat",0.02},
			{"src/test/resources/testData_1.dat",0.04},
			{"src/test/resources/testData_1.dat",0.06},
			{"src/test/resources/testData_1.dat",0.08},
			{"src/test/resources/testData_1.dat",0.1},
		});
	}
		
	/**
	 * @test Test of Interpolate method
	 * @post Save image testInterpolate_* in /tmp/
	 */
	@Test
	public void test_getInterpolationLoess() {
		ArrayList<Vector2d> out;
		Interpolate i = new Interpolate(testcase);
		out = (ArrayList<Vector2d>) i.getInterpolationLoess(smooth.doubleValue());
		roiSaver.saveROI("/tmp/test_getInterpolationLoess_"+testfileName.getFileName()+"_"+smooth.toString()+".tif", out);
		logger.debug("setUp: "+testcase.toString());
		if(out.size()<100)
			logger.debug("testInterpolate: "+out.toString());
	}
	
	/**
	 * @test Simple test of roiSaver class
	 * @post Save image /tmp/testroiSaver_*.tif
	 */
	@Test
	public void test_roiSaver() {
		roiSaver.saveROI("/tmp/test_roiSaver_"+testfileName.getFileName()+"_"+smooth.toString()+".tif", testcase);
	}

}

/**
 * Simple data loader for test
 * 
 * Load contours saved as one-column list with interleaving coordinates of 
 * vertices:
 * @code
 * X1
 * Y1
 * X2
 * Y2
 * ...
 * Xn
 * Yn
 * @endcode
 * The file must contain even number of data. Exemplary code in Matlab to create such file:
 * @code{.m}
 * addpath('/home/baniuk/Documents/QuimP11_MATLAB/')
 * qCells = readQanalysis('Resources/after-macro');
 * testFrames = [75 125 137 1];
 * clear coords;
 * for i=1:length(testFrames)
 *   coords{i} = qCells.outlines{testFrames(i)}(:,2:3);
 * end
 * for i=1:length(testFrames)
 *   fid = fopen(['testData_' num2str(testFrames(i)) '.dat'], 'w');
 *   xy = coords{i};
 *   xyr = reshape(xy',[],1); % x first
 *   fprintf(fid,'%.4f\n',xyr);
 *   fclose(fid);
 * end
 * @endcode
 * 
 * @author baniuk
 *
 */
class dataLoader {
	private static final Logger logger = LogManager.getLogger(dataLoader.class.getName());
	private List<Double> data; 
	public List<Vector2d> Vert;
	
	/**
	 * Construct dataLoader object.
	 * 
	 * Open and read datafile
	 * 
	 * @param fileName file with data (with path)
	 * @throws FileNotFoundException on bad file
	 * @throws IllegalArgumentException when the number of lines in \c fileName is not power of 2 
	 */
	dataLoader(String fileName) throws FileNotFoundException,IllegalArgumentException {
		data = new ArrayList<Double>();
		Vert = new ArrayList<Vector2d>();
		Scanner scanner = new Scanner(new File(fileName));
		while(scanner.hasNextDouble())
			data.add(scanner.nextDouble());
		scanner.close();
		convertToVector();
		logger.debug("File: "+fileName+" loaded");
	}
	
	/**
	 * Convert read List<Double> to List<Vector2d>
	 * 
	 * @throws IllegalArgumentException
	 */
	private void convertToVector() throws IllegalArgumentException{
		if(data.size()%2!=0)
			throw new IllegalArgumentException("Data must be multiply of 2");
		ListIterator<Double> it = data.listIterator();
		while(it.hasNext()) {
			Vert.add(new Vector2d(
					it.next().doubleValue(), // x coord
					it.next().doubleValue())); //y coord from input file
		}
	}
	
	/**
	 * Return loaded data
	 * 
	 * @return loaded polygon as List<Vector2d> 
	 * @retval List<Vector2d> 
	 */
	public List<Vector2d> getData() {
		return Vert;
	}
}

/**
 * Helper class to export shapes as \a *.tif images
 * 
 * @author baniuk
 *
 */
class roiSaver {
	private static final Logger logger = LogManager.getLogger(roiSaver.class.getName());
	
	/**
	 * Dummy constructor
	 */
	roiSaver() {		
	}
	
	/**
	 * Save ROI as image
	 * 
	 * Get ListArray with vertices and create \a fileName.tif image with ROI
	 * 
	 * @param fileName file to save image with path
	 * @param vert list of vertices
	 */
	public static void saveROI(String fileName, List<Vector2d> vert) {
		double[] bb;
		float[] x = new float[vert.size()];
		float[] y = new float[vert.size()];
		int l = 0;
		// copy to arrays
		for(Vector2d el : vert) {
			x[l] = (float)el.getX();
			y[l] = (float)el.getY();
			l++;
		}
		bb = getBoundingBox(vert); // get size of output image
		PolygonRoi pR = new PolygonRoi(x, y, Roi.POLYGON); // create polygon object
		logger.debug("Creating image of size ["+(int)Math.round(bb[0])+","+(int)Math.round(bb[1])+"]");
		ImagePlus outputImage = IJ.createImage("", (int)Math.round(bb[0]+0.2*bb[0]), (int)Math.round(bb[1]+0.2*bb[1]), 1, 8); // output image of size of polygon + margins
		ImageProcessor ip = outputImage.getProcessor(); // get processor required later
		ip.setColor(Color.WHITE); // set pen
		pR.setLocation(0.1*bb[0], 0.1*bb[1]); // move slightly ROI to center
		pR.drawPixels(ip); // draw roi
		IJ.saveAsTiff(outputImage, fileName); // save image
		logger.debug("Saved as: "+fileName);
	}
	
	/**
	 * Calculates \b width and \b height of bounding box for shape defined as List of Vector2d elements
	 * 
	 * @param vert List of vertexes of shape
	 * @return two elements array where [width height]
	 * @retval double[2]
	 * @todo move to RectangleBox class after rework of that class to accept ListArrays
	 */
	private static double[] getBoundingBox(List<Vector2d> vert) {
		double minx = vert.get(0).getX();
		double maxx = minx;
		double miny = vert.get(0).getY();
		double maxy = miny;
		double out[] = new double[2];
		for(Vector2d el : vert) {
			if(el.getX()>maxx) maxx = el.getX();
			if(el.getX()<minx) minx = el.getX();
			if(el.getY()>maxy) maxy = el.getY();
			if(el.getY()<miny) miny = el.getY();
		}
		out[0] = Math.abs(maxx-minx);
		out[1] = Math.abs(maxy-miny);
		return out;		
	}
}