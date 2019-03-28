package com.github.celldynamics.quimp.omero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import omero.log.LogMessage;

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