package uk.ac.warwick.wsbc.QuimP;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author p.baniukiewicz
 *
 */
@Deprecated
public class Loggers {

    static final Logger LOGGER = LoggerFactory.getLogger(Loggers.class.getName());
    @Parameter
    private LogService logService;

    void logg() {
        LOGGER.trace("trace");
        LOGGER.warn("warn");
        LOGGER.debug("Debug");
        LOGGER.info("info");
        // -Dlogback.configurationFile=quimp-logback.xml
    }

    void scilog() {
        logService.debug("debug sci");
        logService.error("error sci");
        logService.trace("trace sci");

    }
}
