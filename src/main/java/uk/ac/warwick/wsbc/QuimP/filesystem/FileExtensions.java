package uk.ac.warwick.wsbc.QuimP.filesystem;

/**
 * Contain list of all extensions supported by QuimP.
 * 
 * @author p.baniukiewicz
 *
 */
public class FileExtensions {

    /**
     * Extension for new file format.
     */
    public static final String newConfigFileExt = ".QCONF";
    /**
     * Extension for old file format.
     */
    public static final String configFileExt = ".paQP";
    /**
     * Extension for file produced by BOA containing snake nodes.
     */
    public static final String snakeFileExt = ".snQP";
    /**
     * Extension for file produced by BOA containing plugin configuration.
     */
    public static final String pluginFileExt = ".pgQP";
    /**
     * Extension for file containing excel stats.
     */
    public static final String statsFileExt = ".stQP.csv";
    /**
     * Extension for file containing convexity map.
     */
    public static final String convmapFileExt = "_convexityMap.maQP";
    /**
     * Extension for file containing motility map.
     */
    public static final String motmapFileExt = "_motilityMap.maQP";
    /**
     * Extension for file containing coordinates map.
     */
    public static final String coordmapFileExt = "_coordMap.maQP";
    /**
     * Extension for file containing origins map.
     */
    public static final String originmapFileExt = "_originMap.maQP";
    /**
     * Extension for file containing x map.
     */
    public static final String xmapFileExt = "_xMap.maQP";
    /**
     * Extension for file containing y map.
     */
    public static final String ymapFileExt = "_yMap.maQP";
    /**
     * Extension for file containing fluoromap map.
     * 
     * The % should be replaced by map number.
     */
    public static final String fluomapFileExt = "_fluoCh%.maQP";
    /**
     * Extension for file containing motility image map.
     */
    public static final String motimageFileExt = "_motility.tiff";
    /**
     * Extension for file containing convexity image map.
     */
    public static final String convimageFileExt = "_convexity.tiff";
    /**
     * Extension for file containing motility vector plot.
     */
    public static final String motvecimageFileExt = "_motility.svg";
    /**
     * Extension for file containing track vector plot.
     */
    public static final String trackvecimageFileExt = "_track.svg";
    /**
     * Suffix for cell stats given by ProtAnalyis.
     */
    public static final String cellStatSuffix = "_cellstat.csv";
    /**
     * Protrusion statistics file suffix given by ProtAnalyis.
     */
    public static final String protStatSuffix = "_protstat.csv";
    /**
     * Polar plot suffix given by ProtAnalyis.
     */
    public static final String polarPlotSuffix = "_polar.svg";

}
