package uk.ac.warwick.wsbc.quimp;

// TODO: Auto-generated Javadoc
/**
 * Hold fluorescence value for pixel.
 * 
 * @author rtyson
 */
public class FluoMeasurement {

    /**
     * 
     */
    public double x;
    /**
     * 
     */
    public double y;
    /**
     * 
     */
    public double intensity;

    /**
     * @param xx
     * @param yy
     * @param i
     */
    public FluoMeasurement(double xx, double yy, double i) {
        x = xx;
        y = yy;
        intensity = i;
    }

    /**
     * Copy constructor
     * 
     * @param src source object
     */
    public FluoMeasurement(FluoMeasurement src) {
        x = src.x;
        y = src.y;
        intensity = src.intensity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(intensity);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FluoMeasurement))
            return false;
        FluoMeasurement other = (FluoMeasurement) obj;
        if (Double.doubleToLongBits(intensity) != Double.doubleToLongBits(other.intensity))
            return false;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FluoMeasurement [x=" + x + ", y=" + y + ", intensity=" + intensity + "]";
    }

}
