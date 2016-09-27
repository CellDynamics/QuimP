package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

/**
 * Keep statistics (outline and fluoro) for one cell along frames.
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsHandler {

    private int frames;
    private int statsElements;
    private int fluoElements;

    /**
     * List of statistic calculated for subsequent frames for the same object.
     */
    public ArrayList<FrameStat> framestats;

    public StatsHandler() {
        framestats = new ArrayList<>();
        frames = 0;
        statsElements = 0;
        fluoElements = 0;
    }

    public StatsHandler(int frames, int statsElements, int fluoElements) {
        this.frames = frames;
        this.statsElements = statsElements;
        this.fluoElements = fluoElements;
        framestats = new ArrayList<>(frames);
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
