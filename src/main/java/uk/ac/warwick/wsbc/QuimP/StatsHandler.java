package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 *
 */
public class StatsHandler {

    private int frames;
    public final int statsElements = 11;
    public final int fluoElements = 11;

    double[][] stats;
    double[][] fluoroCh1;
    double[][] fluoroCh2;
    double[][] fluoroCh3;

    public StatsHandler(int frames) {
        this.frames = frames;
        stats = QuimPArrayUtils.initDoubleArray(frames, statsElements);
        fluoroCh1 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        fluoroCh2 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        fluoroCh3 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        QuimPArrayUtils.fill2Darray(fluoroCh1, -1.0);
        QuimPArrayUtils.fill2Darray(fluoroCh2, -1.0);
        QuimPArrayUtils.fill2Darray(fluoroCh3, -1.0);
    }
}
