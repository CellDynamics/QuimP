package uk.ac.warwick.wsbc.QuimP;

public class HistoryLogger_run {

    HistoryLogger hs;

    public HistoryLogger_run() {
        hs = new HistoryLogger();
    }

    public static void main(String[] args) {
        HistoryLogger_run hsr = new HistoryLogger_run();
        hsr.hs.openHistory();

        hsr.hs.addEntry("Test 1", new SnakePluginList());
        hsr.hs.addEntry("Test 2", null);

    }

}
