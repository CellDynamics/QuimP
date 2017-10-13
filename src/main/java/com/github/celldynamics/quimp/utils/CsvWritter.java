package com.github.celldynamics.quimp.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import ij.IJ;

/**
 * Write csv filed line by line.
 * 
 * @author p.baniukiewicz
 *
 */
public class CsvWritter {
  private PrintWriter pw = null;
  private Path path;
  /**
   * Column delimiter.
   */
  public String delimiter = "\t";
  /**
   * Decimal places.
   */
  public int decPlaces = 4;

  /**
   * Create empty file with random name in temporary folder.
   * 
   * @throws IOException on file error
   */
  public CsvWritter() throws IOException {
    path = File.createTempFile("csvwritter-", ".csv").toPath();
    pw = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile())), true);
  }

  /**
   * Create file and fill first line with header.
   * 
   * @param path path to file
   * @param header header, null/empty value will skip header. Delimiter added automatically
   * @throws IOException on file error
   * @see #close()
   */
  public CsvWritter(Path path, String... header) throws IOException {
    this.path = path;
    pw = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile())), true);
    String h = "";
    if (header == null || header.length == 0) {
      return;
    }
    int i;
    for (i = 0; i < header.length - 1; i++) {
      h = h.concat(header[i]).concat(delimiter);
    }
    h = h.concat(header[i]); // last without delimiter
    pw.write(h + "\n");
  }

  /**
   * Write line to file. Adds \n.
   * 
   * @param line line to write.
   * @return this instance
   */
  public CsvWritter writeLine(String line) {
    pw.write(line);
    appendLine("\n");
    return this;
  }

  /**
   * Write series of Doubles using defined precision and default delimiter. Adds \n.
   * 
   * @param doubles numbers to write.
   * @return this instance
   * @see #close()
   */
  public CsvWritter writeLine(Double... doubles) {
    appendLine(doubles);
    appendLine("\n");
    return this;
  }

  /**
   * Write line to file. Neither adds \n nor delimiter before.
   * 
   * @param line line to write.
   * @return this instance
   * @see #appendDelim()
   * @see #close()
   */
  public CsvWritter appendLine(String line) {
    pw.write(line);
    return this;
  }

  /**
   * Write series of Doubles using defined precision and default delimiter. Neither adds \n nor
   * delimiter before.
   * 
   * @param doubles numbers to write.
   * @return this instance
   * @see #close()
   * @see #appendDelim()
   */
  public CsvWritter appendLine(Double... doubles) {
    int i;
    String h = "";
    for (i = 0; i < doubles.length - 1; i++) {
      h = h.concat(IJ.d2s(doubles[i], decPlaces)).concat(delimiter);
    }
    h = h.concat(IJ.d2s(doubles[i], decPlaces)); // last without delimiter
    pw.write(h);
    return this;
  }

  /**
   * Add current delimiter to line.
   * 
   * @return instance
   */
  public CsvWritter appendDelim() {
    pw.write(delimiter);
    return this;
  }

  /**
   * Return PrintWritter object.
   * 
   * @return the pw
   * @see #close()
   */
  public PrintWriter getPw() {
    return pw;
  }

  /**
   * Return file path.
   * 
   * @return the path
   */
  public Path getPath() {
    return path;
  }

  /**
   * Close stream.
   */
  public void close() {
    if (pw != null) {
      pw.close();
    }
  }

}
