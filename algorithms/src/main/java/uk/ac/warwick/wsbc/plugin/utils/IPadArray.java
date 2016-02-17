package uk.ac.warwick.wsbc.plugin.utils;

/**
 * Basic interface for all family of filters based on moving window.
 * 
 * Supports various methods of data padding
 * 
 * @author p.baniukiewicz
 *
 */
public interface IPadArray {
    int CIRCULARPAD = 1; ///< defines circular padding
    int SYMMETRICPAD = 2; ///< defines symmetric padding

    /**
     * Helper method to pick values from X, Y arrays.
     * 
     * It accepts negative indexes as well as larger than X.size() and does
     * in-place padding. Returns new proper index for array that accounts
     * padding e.g. for input = -2 it returns last+2 if padding is \b circular
     * 
     * @param dataLength Length of data
     * @param index Index of element to get
     * @param mode Method of padding. Available are: - \b CIRCULARPAD - as in
     * Matlab padarray - \b SYMMETRICPAD - as in Matlab padarray
     * @return Proper index. If \c index is negative or larger than X,Y size
     * returned value simulates padding.
     * @remarks Do no check relations of window (provided \c index) to whole
     * data size. May be unstable for certain cases.
     */
    static int getIndex(int dataLength, int index, int mode) {

        switch (mode) {
            case CIRCULARPAD:
                if (index < 0)
                    return (dataLength + index); // for -1 points last element
                if (index >= dataLength)
                    return (index - dataLength); // for after last points 
                                                 // to first
                break;
            case SYMMETRICPAD:
                if (index < 0)
                    return -index - 1;
                if (index >= dataLength)
                    return (dataLength - (index - dataLength) - 1);
                break;
            default:
                throw new IllegalArgumentException(
                        "Padding mode not supported");
        }

        return index; // for all remaining cases
    }

}
