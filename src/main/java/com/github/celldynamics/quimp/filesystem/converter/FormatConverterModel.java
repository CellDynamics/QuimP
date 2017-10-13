package com.github.celldynamics.quimp.filesystem.converter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keep status of {@link FormatConverterUi} checkboxes in {@link List}. Only active included.
 * 
 * <p>Name of the control is coded as Group:name stored as string.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverterModel {

  /**
   * Keep status of checkboxes in {@link FormatConverterUi}. Only active are included, other are
   * assumed to be unselected.
   */
  private List<String> status;

  /**
   * Path to QCONF/paQP being processed.
   */
  public Path convertedFile;

  /**
   * Return status map.
   * 
   * @return the status
   */
  public List<String> getStatus() {
    return status;
  }

  /**
   * Default constructor.
   */
  public FormatConverterModel() {
    status = Collections.synchronizedList(new ArrayList<String>());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FormatConverterModel [status=" + status + ", qconfFile=" + convertedFile + "]";
  }

}
