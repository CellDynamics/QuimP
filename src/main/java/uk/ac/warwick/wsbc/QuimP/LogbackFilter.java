package uk.ac.warwick.wsbc.QuimP;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filter that filters out packages that not belong to QuimP.
 * 
 * Used for limit log output when log is set globally for the whole IJ.
 * 
 * @author p.baniukiewicz
 *
 */
public class LogbackFilter extends Filter<ILoggingEvent> {

    @SuppressWarnings("unused")
    private String packageName;

    public LogbackFilter() {
        packageName = LogbackFilter.class.getPackage().getName().toLowerCase();
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLoggerName().toLowerCase().contains("quimp")) { // to catch plugins as well
            return FilterReply.NEUTRAL; // run next filters on QuimP
        } else {
            return FilterReply.DENY; // stop logging
        }
    }
}
