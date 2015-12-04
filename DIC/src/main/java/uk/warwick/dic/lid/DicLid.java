package uk.warwick.dic.lid;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.XMLFormatter;
import ij.*;
import ij.process.*;

public class DicLid {
//	protected static final Logger log = Logger.getLogger( LIDTest.class.getName() );
	
	private int cols;		// size of the image
	private int rows;
	protected int[] inImagePixels;	// array of raw pixels		
	private int[][] diagonals;	// array of diagonals
	private float[] outImagePixels;		// output image
	private float[][] Ig;			// table of line integrals for [diagonal][point_on_diagonal]
	
	private void loggerInit() throws SecurityException, IOException { 
		//TODO add logging config (http://www.javapractices.com/topic/TopicAction.do?Id=143) and use it to switch off logging on release
//		FileHandler fh = null;
//		fh = new FileHandler("DICLID.log", false);
//		fh.setFormatter(new XMLFormatter());
//		log.addHandler(fh);
//		log.setLevel(Level.ALL);
	}
	/**
	 * @param image
	 */
	public DicLid(ImagePlus image) {
		// setup logger and block all his exceptions
		try {
			loggerInit();
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get ImageProcessor necessary for obtaining raw pixel array
		ByteProcessor tmpProcessor = (ByteProcessor) image.getProcessor();
		cols = image.getWidth();
		rows = image.getHeight();
		// casting from byte to int (see ImageJ plugin Writer pdf)
		byte[] tmpPixels;	// temporary array for getPixels
		tmpPixels = (byte[]) tmpProcessor.getPixels();
		inImagePixels = new int[rows*cols]; // allocation pixel data
		for(int i = 0; i < cols*rows; ++i) // converting byte pixels to ints
			inImagePixels[i] = tmpPixels[i] & 0xff;
		// creating data structures
		diagonals = new int[cols+rows-1][];	// number of diagonals (for every pixel on left and bottom edge), -1 because of main diagonal which counted once
		Ig = new float[cols+rows-1][];		// number of line integrals
		// create output image
		outImagePixels = new float[rows*cols];
		Arrays.fill(outImagePixels, 0xffff); // fill with bright values
//		log.log(Level.FINE, "Image size ["+rows+","+cols+"]");
//		log.log(Level.FINE, "Image type: "+image.getType());
	}
	/**
	 * Extract all diagonals from inImagePixels[] and fills diagonals[][]
	 * Starts form top left corner of image and follow to right bottom. Main diagonal is in middle of diagonals[] 
	 */
	protected void fillDiagonals() {
		
		int l;	// local counter for array with profiles
		int diag = 0;	// local counter for diagonal
		int pixelsInDiagonal;	// number of pixels in diagonal
		int p ; // counters for pixels on diagonals
		for(int r
				= rows-1; r>=0; --r)	// all rows from last to first (0) for most left column
		{
			l = 0;
			// how many pixels will be in diagonal
			pixelsInDiagonal = rows - r;
			// allocate space for diagonal
			diagonals[diag] = new int[pixelsInDiagonal];
			// taking diagonal
			p = r;
			do {
				diagonals[diag][l] = inImagePixels[index2Lin(p, l)]; 
				p++;			
				l++;
			} while(p<rows);
			diag++;				
		}
		for(int c = 1; c<cols; c++) {
			l = 0;
			// how many pixels will be in diagonal
			pixelsInDiagonal = cols - c;
			// allocate space for diagonal
			diagonals[diag] = new int[pixelsInDiagonal]; // next slot after previous loop
			// taking diagonal
			p = c;
			do {
				diagonals[diag][l] = inImagePixels[index2Lin(l, p)]; 
				p++;
				l++;
			} while(p<cols);
			diag++;			
		}
		
	}
	
	/**
	 * @param B
	 * @param s0
	 * @param d
	 * @bug May not work now for s0!=0
	 */
	protected void lineInetgration(float B, double s0, int d) {
		
		float tmpSumUp, tmpSumRev;
		
		float[] preExp = null; // precomputed values of decay (if s0 != 0)
		// precomputing exp(ii.s0)
		if(s0 != 0.0) {
			int maxlength = diagonals[rows-1].length; //FIXME It is not true, longest diag will be in the middle (for square images)
			preExp = new float[maxlength];
//			log.log(Level.FINE, "Max diagonal: "+maxlength);
			for(int i=0; i<maxlength; ++i)
				preExp[i] = (float) Math.exp(i/s0);
		}
		// evaluating lineIntegrals
		int[] tmpProf;	// temporary diagonal
//		log.log(Level.FINE,"Number of diagonals to iterate: "+diagonals.length);
		for(int r=0; r<diagonals.length; ++r) { // for every diagonal perform integrations
			tmpProf = diagonals[r];
			Ig[r] = new float[tmpProf.length];	// integration results for r-th diagonal
			for(int i=0; i<tmpProf.length; ++i) { // for every point in diagonal
				for(int ii=0; ii<tmpProf.length; ++ii) { // for every i we move forward and sum all points
					for(int dd=-d; dd<=d; ++dd)	{ // for adjacent lines - averaging
						if(r - dd < 0 || r - dd >= diagonals.length) { // if there is no adjacent diagonal to diagonal r
//							log.log(Level.SEVERE, "dd skipped: r= "+r+" dd= "+dd+" tmpProf.length= "+tmpProf.length);
							continue;
		                }
						// collecting adjacent diagonal data
						//TODO move declaration outside
						int[] localDiagonal = diagonals[r-dd];
						int localii = ii - Math.abs(d);
						int locali = i - Math.abs(d);
			            if(locali + localii < localDiagonal.length && locali + localii >= 0) //TODO Buffor length value
			            	tmpSumUp = localDiagonal[locali+localii];
			            else
			                tmpSumUp = Math.abs(B);
			            if(locali - localii >= 0 && locali - localii < localDiagonal.length)
		                   tmpSumRev = localDiagonal[locali-localii];
		                else
		                   tmpSumRev = Math.abs(B);
			            if(s0 == 0.0)
		                   Ig[r][i] += (tmpSumUp - tmpSumRev);
		               else
		                   Ig[r][i] += ( (tmpSumUp - tmpSumRev)*Math.exp((double)ii/s0) ); //TODO add table preExp here to speed up
					} // dd
				} // ii
			} // i
		} // r
	}
	
	/**
	 * Performs DIC reconstruction using LID method
	 * @param B 
	 * @param s0 
	 * @param d
	 * @return Reconstructed image of size of original image
	 * @warning Performs scaling of output array to ImagePlus formats
	 */
	public ImagePlus DICReconstructionLID(float B, double s0, int d)
	{
		// create ImagePlus object
		ImageProcessor tmpNew = new FloatProcessor(cols,rows);
		
		// evaluation of function
		fillDiagonals();
		lineInetgration(B, s0, d);
		// reconstructing from IG
		int l;
		for(int r=0; r<rows; ++r) {
			l = 0;
			for(int p=r; p<rows; ++p) {
				outImagePixels[index2Lin(p,l)] = Ig[rows-r-1][l]; 
				l++;
			}
		}
		for(int c=1; c<cols; ++c) {
			l = 0;
			for(int p=c; p<cols; ++p) {
				outImagePixels[index2Lin(l,p)] = Ig[c+rows-1][l]; 
				l++;
			}
		}
		
		tmpNew.setPixels(outImagePixels);
		ImagePlus outImage = new ImagePlus("Out Image", tmpNew.convertToByteProcessor(true));
		return outImage; 
		
	}
	
	/**
	 * Internal method for converting from (x,y) space to linear representation of image. Assumes row-ordered images.
	 * @param r Row number counted from 0
	 * @param c Column number counted from 0
	 * @return Index of pixel on Image(r,c)
	 */
 	private int index2Lin(int r, int c)	{
		return(r*cols + c);		
	}
	
	/**
	 * Method for accessing diagonals for tests
	 * @return Reference to diagonals
	 */
	protected int[][] getDiagonals() {
		return diagonals;
	}
	
	/**
	 * Method for accessing outImage for tests
	 * @return Reference to outImage
	 */
	protected float[] getoutImage() {
		return outImagePixels;
	}
}