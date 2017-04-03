package uk.ac.warwick.wsbc.quimp.filesystem;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import uk.ac.warwick.wsbc.quimp.QuimP;

/**
 * Implements file dialog with file filtering.
 * 
 * <p>If not stated otherwise, all constructors use default file config extension given by state of
 * QuimP ToolBar checkbox (of {@link QuimP#newFileFormat}.
 * 
 * @author p.baniukiewicz
 *
 */
public class FileDialogEx extends FileDialog {

  private QuimpConfigFilefilter qcf = new QuimpConfigFilefilter();

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -8657174496110858503L;

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   * @param title title
   * @param mode mode
   */
  public FileDialogEx(Dialog parent, String title, int mode) {
    super(parent, title, mode);
  }

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   * @param title title
   */
  public FileDialogEx(Dialog parent, String title) {
    super(parent, title);
  }

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   */
  public FileDialogEx(Dialog parent) {
    super(parent);
  }

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   * @param title title
   * @param mode mode
   */
  public FileDialogEx(Frame parent, String title, int mode) {
    super(parent, title, mode);
  }

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   * @param title title
   */
  public FileDialogEx(Frame parent, String title) {
    super(parent, title);
  }

  /**
   * Construct object with default file filtering.
   * 
   * @param parent parent
   */
  public FileDialogEx(Frame parent) {
    super(parent);
  }

  /**
   * Construct object with given file extension filtering.
   * 
   * @param parent parent
   * @param ext list of extensions (.ext). Can be null to use default
   *        {@link FileDialogEx.QuimpConfigFilefilter}
   */
  public FileDialogEx(Frame parent, String... ext) {
    super(parent);
    if (ext != null) {
      setExtension(ext);
    }
  }

  /**
   * Set extensions to filter.
   * 
   * <p>Should be called before {@link #showOpenDialog()}.
   * 
   * @param ext list of extensions (.ext). Can be null to use default
   *        {@link FileDialogEx.QuimpConfigFilefilter}
   */
  public void setExtension(String... ext) {
    qcf = new QuimpConfigFilefilter(ext);
  }

  /**
   * Get path to selected file.
   * 
   * <p>Should be called after {@link #showOpenDialog()}.
   * 
   * @return path to selected file or null if dialog cancelled.
   */
  public Path getPath() {
    if (getFile() == null) {
      return null;
    } else {
      return Paths.get(getDirectory(), getFile());
    }
  }

  /**
   * Show dialog and wait for user action.
   * 
   * @return path to selected file or null if cancelled.
   */
  public Path showOpenDialog() {
    setMultipleMode(false);
    setMode(FileDialog.LOAD);
    setTitle("Open paramater file " + qcf.toString());
    setFilenameFilter(qcf);
    setVisible(true);
    return getPath();
  }

  /**
   * Implement filter of FileDialog.
   * 
   * <p>Define also default extensions for new and old file format.
   * 
   * @author p.baniukiewicz
   *
   */
  class QuimpConfigFilefilter implements FilenameFilter {
    private String[] ext;

    /**
     * Allow to provide list of accepted extensions with dot.
     * 
     * @param ext list of extensions .ext, .ext
     */
    public QuimpConfigFilefilter(String... ext) {
      this.ext = ext;
    }

    /**
     * Default constructor.
     * 
     * <p>Set active extension using choicebox in Quimp_Bar
     * 
     */
    public QuimpConfigFilefilter() {
      if (QuimP.newFileFormat.get() == true) {
        ext = new String[1];
        ext[0] = FileExtensions.newConfigFileExt;
      } else {
        ext = new String[2];
        ext[0] = FileExtensions.configFileExt;
        ext[1] = FileExtensions.newConfigFileExt;
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File dir, String name) {
      boolean ret = false;
      for (String e : ext) {
        ret = ret || (name.endsWith(e.toLowerCase()) || name.endsWith(e)); // any true will set flag
      }
      return ret;
    }

    /**
     * Get stored extension.
     * 
     * @return the extension
     */
    public String[] getExt() {
      return ext;
    }

    /**
     * Return defined extensions.
     * 
     * <p>Can be used for filling bar of file selectors.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return Arrays.toString(ext);
    }

  }

}
