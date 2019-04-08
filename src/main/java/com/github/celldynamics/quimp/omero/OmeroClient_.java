package com.github.celldynamics.quimp.omero;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import ij.IJ;
import ij.Prefs;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/**
 * QuimP module for Omero integration.
 * 
 * @author p.baniukiewicz
 *
 */
public class OmeroClient_ {
  static final Logger LOGGER = LoggerFactory.getLogger(OmeroClient_.class.getName());
  static final MessageSinkTypes SOURCE = QuimpException.MessageSinkTypes.GUI;
  private static final String KEY = "sEcReT";
  private static final String PREFS_PREFIX = "omero";
  private static final String PREFS_USER_SUFFIX = ".user";
  private static final String PREFS_HOST_SUFFIX = ".host";
  private static final String PREFS_PASS_SUFFIX = ".pass";
  private static final String PREFS_PORT_SUFFIX = ".port";
  private String user;
  private String host;
  private String pass;
  private int port;
  private OmeroLoginDialog dialog;
  private AbstractDataSet<DatasetData> currentDatasets;
  private AbstractDataSet<ImageData> currentImages;
  OmeroBrowser omero;

  /**
   * Create all instances of UI and API.
   */
  public OmeroClient_() {
    downloadPrefs();
    dialog = new OmeroLoginDialog(this);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setVisible(true);
    currentDatasets = new AbstractDataSet<>();
    currentImages = new AbstractDataSet<>();
  }

  boolean connect() {
    LOGGER.trace("Connecting..");
    currentDatasets.clear();
    currentImages.clear();
    try {
      disconnect(); // close previous
      omero = new OmeroBrowser(user, pass, host, port);
      omero.connect();
      LOGGER.info("Connected!");
      return true;
    } catch (Exception e) {
      omero.silentClose();
      if (!(e instanceof QuimpException)) {
        LOGGER.debug(e.getMessage(), e);
        QuimpException.showGuiWithMessage(null, QuimpException.prepareMessage(e, "OmeroClient"));
      } else {
        QuimpException ex = (QuimpException) e;
        ex.setMessageSinkType(SOURCE);
        ex.handleException(IJ.getInstance(), "OmeroClient");
      }
    }
    return false;
  }

  void disconnect() {
    LOGGER.trace("Closing..");
    if (omero != null) {
      omero.silentClose();
    }
  }

  /**
   * Get datasets from Omero and prepare data to show in UI.
   * 
   * <p>Updates also internal list of current datasets.
   * 
   * @return List of datasets.
   */
  List<DatasetData> getDatasets() {
    if (omero != null) {
      try {
        currentDatasets.clear();
        currentDatasets.ds.addAll(omero.listDatasets());
      } catch (DSOutOfServiceException | DSAccessException | ExecutionException e) {
        LOGGER.debug(e.getMessage(), e);
        QuimpException.showGuiWithMessage(IJ.getInstance(),
                QuimpException.prepareMessage(e, "OmeroClient"));
      }
    }
    return currentDatasets.ds;
  }

  /**
   * Return images from datasetName in format [name,date].
   * 
   * <p>Output is used to fill {@link OmeroLoginDialog#getTableImagesDownload()}. Updates also
   * internal list
   * of images for current dataset.
   * 
   * @param datasetIndex index of dataset returned by {@link #getDatasets()}.
   * @return images from dataset as list
   * @see #getDatasets()
   */
  List<ImageData> getImages(int datasetIndex) {
    DatasetData dstmp = currentDatasets.ds.get(datasetIndex);
    try {
      currentImages.clear();
      currentImages.ds.addAll(omero.openDataset(dstmp));
    } catch (DSOutOfServiceException | DSAccessException | ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
      QuimpException.showGuiWithMessage(IJ.getInstance(),
              QuimpException.prepareMessage(e, "OmeroClient"));
    }
    return currentImages.ds;
  }

  /**
   * Upload current preferences to IJ_prefs or clear uploaded keys.
   * 
   * @param clear clear if true, upload otherwise
   */
  void uploadPrefs(boolean clear) {
    if (clear) {
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_USER_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_HOST_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PASS_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PORT_SUFFIX, OmeroBrowser.DEF_PORT);
      LOGGER.info("Prefs cleared");
    } else {
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_USER_SUFFIX, user);
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_HOST_SUFFIX, host);
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PASS_SUFFIX,
              Xor.encrypt(pass, KEY));
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PORT_SUFFIX, port);
      LOGGER.debug("Prefs uploaded");
    }

  }

  void downloadPrefs() {
    user = Prefs.get(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_USER_SUFFIX, "");
    host = Prefs.get(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_HOST_SUFFIX, "");
    pass = Xor.decrypt(Prefs.get(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PASS_SUFFIX, ""),
            KEY);
    port = (int) Prefs.get(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PORT_SUFFIX,
            OmeroBrowser.DEF_PORT);
  }

  /**
   * Set user.
   * 
   * @param user the user to set
   */
  void setUser(String user) {
    LOGGER.debug("Set user: " + user);
    this.user = user;
    if (dialog.getChckbxStoreCred().isSelected()) {
      uploadPrefs(false);
    }
  }

  /**
   * Set host.
   * 
   * @param host the host to set
   */
  void setHost(String host) {
    LOGGER.debug("Set host: " + host);
    this.host = host;
    if (dialog.getChckbxStoreCred().isSelected()) {
      uploadPrefs(false);
    }

  }

  /**
   * Set pass.
   * 
   * @param pass the pass to set
   */
  void setPass(String pass) {
    LOGGER.debug("Set pass: " + pass);
    this.pass = pass;
    if (dialog.getChckbxStoreCred().isSelected()) {
      uploadPrefs(false);
    }
  }

  /**
   * Set port.
   * 
   * @param port the port to set
   */
  void setPort(String port) {
    LOGGER.debug("Set port: " + port);
    if (!port.isEmpty()) {
      this.port = Integer.parseInt(port);
    }
    if (dialog.getChckbxStoreCred().isSelected()) {
      uploadPrefs(false);
    }
  }

  /**
   * Get user field.
   * 
   * @return the user
   */
  String getUser() {
    return user;
  }

  /**
   * Get host field.
   * 
   * @return the host
   */
  String getHost() {
    return host;
  }

  /**
   * Get pass field.
   * 
   * @return the pass
   */
  String getPass() {
    return pass;
  }

  /**
   * Get port field.
   * 
   * @return the port
   */
  String getPort() {
    return String.valueOf(port);
  }

  /**
   * Get id of selected dataset.
   * 
   * @return the currentDs
   */
  AbstractDataSet<DatasetData> getCurrentDatasets() {
    return currentDatasets;
  }

  /**
   * Set id of selected dataset.
   * 
   * <p>This is index of dataset returned by {@link #getDatasets()}.
   * 
   * @param currentDs the currentDs to set
   */
  void setCurrentDs(int currentDs) {
    this.currentDatasets.currentEl = currentDs;
  }

  /**
   * Get d of selected image.
   * 
   * @return the currentIm
   */
  AbstractDataSet<ImageData> getCurrentImages() {
    return currentImages;
  }

  /**
   * Set id of selected image.
   * 
   * <p>This is index of image returned by {@link #getCurrentImages()}.
   * 
   * @param currentIm the currentIm to set
   */
  void setCurrentIm(int currentIm) {
    this.currentImages.currentEl = currentIm;
  }

  /**
   * Simple XOR implementation to obfuscate password in IJ_Prefs.
   * 
   * @author p.baniukiewicz
   *
   */
  static class Xor {

    /**
     * Encrypt string.
     * 
     * @param text string to encrypt
     * @param key password
     * @return base64 encrypted string
     */
    public static String encrypt(String text, String key) {
      Encoder enc = java.util.Base64.getEncoder();
      return enc.encodeToString(xor(text.getBytes(), key.getBytes()));
    }

    /**
     * Decrypt string.
     * 
     * @param text string base64 to decrypt
     * @param key password
     * @return decrypted string
     */
    public static String decrypt(String text, String key) {
      Decoder d = java.util.Base64.getDecoder();
      return new String(xor(d.decode(text), key.getBytes()));
    }

    private static byte[] xor(byte[] text, byte[] key) {
      byte[] ret = new byte[text.length];
      for (int i = 0; i < text.length; i++) {
        ret[i] = (byte) (text[i] ^ key[i % key.length]);
      }
      return ret;
    }

  }

  /**
   * Downloads image.
   * 
   * <p>Image selected by {@link OmeroClient_#setCurrentDs(int)} and then
   * {@link OmeroClient_#setCurrentIm(int)}
   * 
   * @param destFolder destination folder
   */
  public void download(String destFolder) {
    if (currentDatasets.validate() && currentImages.validate()) {
      LOGGER.debug("Download: " + currentDatasets.toString() + ", " + currentImages.toString());
      try {
        omero.download(currentImages.getCurrent(), Paths.get(destFolder));
      } catch (ServerError | PermissionDeniedException | CannotCreateSessionException
              | DSOutOfServiceException | DSAccessException | ExecutionException | IOException
              | URISyntaxException e) {
        LOGGER.debug(e.getMessage(), e);
        QuimpException.showGuiWithMessage(null, QuimpException.prepareMessage(e, "OmeroClient"));
      }
    } else {

      QuimpException.showGuiWithMessage(null,
              "Connect to database first and then select dataset on left and image "
                      + "with attached QCONF on right panel.");
    }

  }

  /**
   * Upload qconf and related image to currently selected dataset.
   * 
   * @see #setCurrentDs(int)
   */
  public void upload() {
    if (currentDatasets.validate()) {
      try {
        QconfLoader qconfLoader = new QconfLoader(null, FileExtensions.newConfigFileExt);
        if (qconfLoader.isFileLoaded() == QconfLoader.QCONF_INVALID) {
          return;
        }
        Path qconfPath = qconfLoader.getQconfFile();
        qconfLoader.getImage(); // try to read image from qconf and ask to point if abs path wrong
        Path imagePath = qconfLoader.getBOA().boap.getOrgFile().toPath();
        LOGGER.debug("Upload " + qconfPath.toString() + ", " + imagePath.toString());
        if (omero != null && currentDatasets.validate() && imagePath.getFileName() != null) {
          omero.upload(new String[] { imagePath.toString() }, currentDatasets.getCurrent());
          omero.upload(imagePath.getFileName().toString(), qconfPath.toString(),
                  currentDatasets.getCurrent());
        }
      } catch (QuimpException e) {
        e.setMessageSinkType(SOURCE);
        e.handleException(IJ.getInstance(), "OmeroClient");
      } catch (Exception e) {
        LOGGER.debug(e.getMessage(), e);
        QuimpException.showGuiWithMessage(null, QuimpException.prepareMessage(e, "OmeroClient"));
      }
    } else {
      QuimpException.showGuiWithMessage(null,
              "Connect to database first and then select dataset on left panel.");
    }
  }
}
