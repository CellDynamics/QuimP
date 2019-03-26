package com.github.celldynamics.quimp.omero;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ExperimenterData;
import omero.log.LogMessage;

/**
 * @author p.baniukiewicz
 *
 */
public class OmeroClient {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  static final Logger LOGGER = LoggerFactory.getLogger(OmeroClient.class.getName());
  public static final int DEF_PORT = 4064;

  private Gateway gateway = null;
  private SecurityContext ctx;

  public OmeroClient(String user, String pass, String host, int port)
          throws DSOutOfServiceException {
    LOGGER.debug("Opening connection:" + user + ", " + pass + ", " + host);
    LoginCredentials cred = new LoginCredentials(user, pass, host, port);
    Gateway gateway = new Gateway(new LoggerWrapper());
    ExperimenterData experimenter = gateway.connect(cred);
    ctx = new SecurityContext(experimenter.getGroupId());

  }

  public void close() {
    if (gateway != null) {
      gateway.disconnect();
      LOGGER.debug("Omero disconnected");
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    String user;
    String pass;
    String host;
    Properties prop = new Properties();
    OmeroClient client = null;
    try (FileInputStream input = new FileInputStream(
            Paths.get(System.getProperty("user.home"), "omero.properties").toFile())) {
      prop.load(input);
      user = prop.getProperty("user");
      pass = prop.getProperty("pass");
      host = prop.getProperty("host");
      client = new OmeroClient(user, pass, host, DEF_PORT);

    } catch (IOException | DSOutOfServiceException ex) {
      ex.printStackTrace();
    } finally {
      if (client != null) {
        client.close();
      }
    }

    // https://github.com/ome/minimal-omero-client/blob/master/src/main/java/com/example/SimpleConnection.java
    // https://docs.openmicroscopy.org/omero/5.4.10/developers/Java.html

  }

}

class LoggerWrapper implements omero.log.Logger {
  static final Logger LOGGER = LoggerFactory.getLogger(LoggerWrapper.class.getName());

  @Override
  public void debug(Object originator, String logMsg) {
    LOGGER.debug("[" + originator.getClass().getName() + "] " + logMsg);

  }

  @Override
  public void debug(Object originator, LogMessage msg) {
    debug(originator, msg.toString());

  }

  @Override
  public void info(Object originator, String logMsg) {
    LOGGER.info("[" + originator.getClass().getName() + "] " + logMsg);

  }

  @Override
  public void info(Object originator, LogMessage msg) {
    info(originator, msg.toString());
  }

  @Override
  public void warn(Object originator, String logMsg) {
    LOGGER.warn("[" + originator.getClass().getName() + "] " + logMsg);
  }

  @Override
  public void warn(Object originator, LogMessage msg) {
    warn(originator, msg.toString());
  }

  @Override
  public void error(Object originator, String logMsg) {
    LOGGER.error("[" + originator.getClass().getName() + "] " + logMsg);
  }

  @Override
  public void error(Object originator, LogMessage msg) {
    error(originator, msg.toString());
  }

  @Override
  public void fatal(Object originator, String logMsg) {
    LOGGER.error("[" + originator.getClass().getName() + "] " + logMsg);
  }

  @Override
  public void fatal(Object originator, LogMessage msg) {
    fatal(originator, msg.toString());
  }

  @Override
  public String getLogFile() {
    // TODO Auto-generated method stub
    return null;
  }

}
