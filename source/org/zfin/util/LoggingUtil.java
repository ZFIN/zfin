package org.zfin.util;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;

/**
 * Utility class to add a new appender for a specific purpose, such as ontology loading.
 */
public class LoggingUtil {

    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static void removeAppender(Logger log, Appender appender) {
        log.removeAppender(appender);
    }

    /**
     * Initialize a separate file appender with a name provided.
     * log files are created and stored in the temp directory specified in the
     * java.io.tmpdir location.
     */
    public static Appender addFileAppender(Logger log, String filename) {
        PatternLayout layout = new PatternLayout("%r - %p: %m%n");
        String fileName = filename + ".log";
        fileName = FileUtil.createAbsolutePath(TEMP_DIRECTORY, "cron-jobs-logs", FileUtil.addTimeStampToFileName(fileName));
        FileAppender appender = null;
        try {
            appender = new FileAppender(layout, fileName);
            appender.setName("ontology-logger");
            log.addAppender(appender);
        } catch (IOException e) {
            log.error("failed to initialize logger", e);
        }
        return appender;
    }

}
