package uk.ac.warwick.wsbc.QuimP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author p.baniukiewicz
 *
 */
public class Loggers {

    static final Logger LOGGER = LoggerFactory.getLogger(Loggers.class.getName());

    void logg() {
        LOGGER.trace("trace");
        LOGGER.warn("warn");
        LOGGER.debug("Debug");
        LOGGER.info("info");
        // -Dlogback.configurationFile=quimp-logback.xml
    }
}
