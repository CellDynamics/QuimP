package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Scanner;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.process.FloatPolygon;

/**
 * Simple data loader for test
 * 
 * Load contours saved as one-column list with interleaving coordinates of vertices:
 * 
 * @code X1 Y1 X2 Y2 ... Xn Yn
 * @endcode The file must contain even number of data. Exemplary code in Matlab to create such file:
 * @code{.m} addpath('/home/p.baniukiewicz/Documents/QuimP11_MATLAB/') qCells =
 *           readQanalysis('Resources/after-macro'); testFrames = [75 125 137 1]; clear coords; for
 *           i=1:length(testFrames) coords{i} = qCells.outlines{testFrames(i)}(:,2:3); end for
 *           i=1:length(testFrames) fid = fopen(['testData_' num2str(testFrames(i)) '.dat'], 'w');
 *           xy = coords{i}; xyr = reshape(xy',[],1); % x first fprintf(fid,'%.4f\n',xyr);
 *           fclose(fid); end
 * @endcode
 * 
 * @author p.baniukiewicz
 *
 */
public class DataLoader {
    static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class.getName());
    private List<Double> data;
    public List<Point2d> Vert;

    /**
     * Construct dataLoader object.
     * 
     * Open and read datafile
     * 
     * @param fileName file with data (with path)
     * @throws FileNotFoundException on bad file
     * @throws IllegalArgumentException when the number of lines in \c fileName is not power of 2
     */
    public DataLoader(String fileName) throws FileNotFoundException, IllegalArgumentException {
        data = new ArrayList<Double>();
        Vert = new ArrayList<Point2d>();
        Scanner scanner = new Scanner(new File(fileName));
        scanner.useLocale(Locale.US);
        while (scanner.hasNextDouble())
            data.add(scanner.nextDouble());
        scanner.close();
        convertToPoint2d();
        LOGGER.debug("File: " + fileName + " loaded");
    }

    /**
     * Convert read List<Double> to List<Point2d>
     * 
     * @throws IllegalArgumentException
     */
    private void convertToPoint2d() throws IllegalArgumentException {
        if (data.size() % 2 != 0)
            throw new IllegalArgumentException("Data must be multiply of 2");
        ListIterator<Double> it = data.listIterator();
        while (it.hasNext()) {
            Vert.add(new Point2d(it.next().doubleValue(), // x coord
                    it.next().doubleValue())); // y coord from input file
        }
    }

    /**
     * Return loaded data
     * 
     * @return loaded polygon as List<Point2d>
     * @retval List<Vector2d>
     */
    public List<Point2d> getData() {
        return Vert;
    }

    /**
     * Return loaded data as FloatPolygon
     * 
     * @return Loaded polygon as FloatPolygon
     */
    public FloatPolygon getFloatPolygon() {
        QuimpDataConverter qd = new QuimpDataConverter(getData());
        return new FloatPolygon(qd.getFloatX(), qd.getFloatY());
    }

    /**
     * Convert loaded data to string
     * 
     * @return String representation of loaded data
     */
    public String toString() {
        return Vert.toString();
    }
}