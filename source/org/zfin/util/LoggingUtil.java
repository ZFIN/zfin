package org.zfin.util;

import org.apache.logging.log4j.Logger;

/**
 * Utility class to add a new appender for a specific purpose, such as ontology loading.
 */
public class LoggingUtil {

    private long startTime;
    private Logger log;

    public LoggingUtil(Logger log) {
        startTime = System.currentTimeMillis();
        this.log = log;
    }

    public LoggingUtil() {
        startTime = System.currentTimeMillis();
    }

    public void logDuration(String message) {
        log.info(message + ": " + DateUtil.getTimeDuration(startTime));
    }

}
