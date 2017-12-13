package com.github.celldynamics.quimp.filesystem.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;

/**
 * Keep status of {@link FormatConverterUi} checkboxes in {@link List}. Only active included.
 * 
 * <p>Name of the control is coded as Group:name stored as string.
 * 
 * @author p.baniukiewicz
 *
 */
public class FormatConverterModel extends AbstractPluginOptions {

  /**
   * Keep status of checkboxes in {@link FormatConverterUi}. Only active are included, other are
   * assumed to be unselected.
   */
  private List<String> status;

  /**
   * Save one file with all snakes or many files with one snake.
   */
  public boolean areMultipleFiles = true;

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
    return "FormatConverterModel [status=" + status + ", qconfFile=" + paramFile + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    FormatConverterModel cp = new FormatConverterModel();
    cp.status = new ArrayList<>(this.status);
    cp.paramFile = this.paramFile;
    cp.areMultipleFiles = this.areMultipleFiles;
    return cp;
  }

}
