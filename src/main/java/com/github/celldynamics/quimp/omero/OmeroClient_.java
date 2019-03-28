package com.github.celldynamics.quimp.omero;

import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.swing.JDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimP;

import ij.Prefs;

/**
 * @author p.baniukiewicz
 *
 */
public class OmeroClient_ {
  static final Logger LOGGER = LoggerFactory.getLogger(OmeroClient_.class.getName());
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

  /**
   * Create all instances of UI and API.
   */
  public OmeroClient_() {
    downloadPrefs();
    dialog = new OmeroLoginDialog(this);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setVisible(true);
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
   * Upload current preferences to IJ_prefs or clear uploaded keys.
   * 
   * @param clear clear if true, upload otherwise
   */
  void uploadPrefs(boolean clear) {
    if (clear) {
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_USER_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_HOST_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PASS_SUFFIX, "");
      Prefs.set(PREFS_PREFIX + QuimP.QUIMP_PREFS_SUFFIX + PREFS_PORT_SUFFIX,
              OmeroClientApi.DEF_PORT);
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
            OmeroClientApi.DEF_PORT);
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
}
