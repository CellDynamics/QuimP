package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 *
 */
public class StatsHandler {

    private int frames;
    private int statsElements;
    private int fluoElements;

    public ArrayList<FrameStat> stats;
    public double[][] fluoroCh1;
    public double[][] fluoroCh2;
    public double[][] fluoroCh3;

    public StatsHandler() {
        stats = new ArrayList<>();
        frames = 0;
        statsElements = 0;
        fluoElements = 0;
    }

    public StatsHandler(int frames, int statsElements, int fluoElements) {
        this.frames = frames;
        this.statsElements = statsElements;
        this.fluoElements = fluoElements;
        stats = new ArrayList<>(frames);
        fluoroCh1 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        fluoroCh2 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        fluoroCh3 = QuimPArrayUtils.initDoubleArray(frames, fluoElements);
        QuimPArrayUtils.fill2Darray(fluoroCh1, -1.0);
        QuimPArrayUtils.fill2Darray(fluoroCh2, -1.0);
        QuimPArrayUtils.fill2Darray(fluoroCh3, -1.0);
    }

    /**
     * @return the frames
     */
    public int getFrames() {
        return frames;
    }

    /**
     * @return the statsElements
     */
    public int getStatsElements() {
        return statsElements;
    }

    /**
     * @return the fluoElements
     */
    public int getFluoElements() {
        return fluoElements;
    }
}
