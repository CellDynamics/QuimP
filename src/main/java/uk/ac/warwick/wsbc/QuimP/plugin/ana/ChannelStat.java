package uk.ac.warwick.wsbc.QuimP.plugin.ana;

/**
 * Hold statistic of fluorescence for one channel.
 * 
 * @author p.baniukiewicz
 *
 */
public class ChannelStat {

    public double innerArea = 0;
    public double totalFluor = 0;
    public double cortexWidth = 0;
    public double meanFluor = 0;
    public double meanInnerFluor = 0;
    public double totalInnerFluor = 0;
    public double cortexArea = 0;
    public double totalCorFluo = 0;
    public double meanCorFluo = 0;
    public double percCortexFluo = 0;

    public ChannelStat() {
    }
}