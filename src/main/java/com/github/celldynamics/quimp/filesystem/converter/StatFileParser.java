package com.github.celldynamics.quimp.filesystem.converter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.CellStats;
import com.github.celldynamics.quimp.FrameStatistics;
import com.github.celldynamics.quimp.filesystem.FileExtensions;

/**
 * Parse stQP files and convert it to StatsCollection.
 * 
 * @author p.baniukiewicz
 *
 */
public class StatFileParser {
  static final int MAX_CELLS = 65535;
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(StatFileParser.class.getName());

  private String name;
  private ArrayList<CellStats> stats;

  /**
   * CellStat getter.
   * 
   * @return the stats
   */
  public ArrayList<CellStats> getStats() {
    return stats;
  }

  /**
   * Construct object.
   * 
   * @param name corename of file with path. E.g path/to/file/test for files test_0.stQP.csv,...
   */
  public StatFileParser(String name) {
    stats = new ArrayList<>();
    this.name = name;
  }

  /**
   * Read all files along given path and prefix used for constructing the object and convert them to
   * CellStat objects.
   * 
   * @return Imported stats
   * 
   * @throws IOException on File read error
   */
  public ArrayList<CellStats> importStQp() throws IOException {
    List<Path> files = getAllFiles();
    for (Path p : files) {
      FrameStatistics[] fs = FrameStatistics.read(p.toFile());
      stats.add(new CellStats(fs));
    }
    return getStats();
  }

  /**
   * Scan for stQP.csv files numbered consequently from _0.
   * 
   * @return List of full paths (related to path given in constructor) of stQP files or empty list.
   */
  List<Path> getAllFiles() {
    ArrayList<Path> ret = new ArrayList<>();
    Path pa = Paths.get(name);
    Path filename = pa.getFileName(); // file name without extension
    Path folder = pa.getParent();
    int i = 0;
    while (i < MAX_CELLS) {
      Path testFile;
      if (folder != null) {
        testFile = folder
                .resolve(Paths.get(filename.toString() + "_" + i + FileExtensions.statsFileExt));
      } else {
        testFile = Paths.get(filename.toString() + "_" + i + FileExtensions.statsFileExt);
      }
      if (testFile.toFile().isFile()) { // check if _xx exists
        ret.add(testFile); // store
        i++; // go to next possible file
      } else {
        break;
      }
    }
    if (i >= MAX_CELLS) {
      LOGGER.warn("Reached maximal number of statistic files: " + MAX_CELLS);
    }
    return ret;

  }

}
