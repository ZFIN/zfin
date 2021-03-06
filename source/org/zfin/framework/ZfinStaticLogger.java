package org.zfin.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.util.ArrayList;

/**
 * Logger that keeps track and logs session events from ZfinSessionListener.
 */
public class ZfinStaticLogger {
    /**
     * The set of messages that have been logged.
     */
    protected static ArrayList messages = new ArrayList();

    private static final Logger logger = LogManager.getLogger(ZfinStaticLogger.class);

    static {

        String logFileName = ZfinProperties.getSessionLogName();
        //TODO: make this access more generic
        // independent of the servlet container.
        String absoluteFilePath = getFullLogFileName(logFileName);
/*
        RollingFileAppender appender = null;
        try {
            String logFilePattern = ZfinPropertiesEnum.LOG_FILE_PATTERN.value();
            appender = new RollingFileAppender(new PatternLayout(logFilePattern), absoluteFilePath);
            appender.setMaximumFileSize(ZfinProperties.getLogFileSize());
            appender.setAppend(true);
            appender.setMaxBackupIndex(ZfinProperties.getMaxlogFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addAppender(appender);
*/
    }

    /**
     * The index of the next message that will be retrieved by a read() call.
     */
    protected static int position = 0;

    // --------------------------------------------------------- Public Methods


    /**
     * Return the next message that has been logged, or <code>null</code>
     * if there are no more messages.
     */
    public static String read() {

        synchronized (messages) {
            if (position < messages.size())
                return ((String) messages.get(position++));
            else
                return (null);
        }

    }


    /**
     * Reset the messages buffer and position.
     */
    public static void reset() {

        synchronized (messages) {
            messages.clear();
            position = 0;
        }

    }


    /**
     * Write a new message to the end of the messages buffer.
     *
     * @param message The message to be added
     */
    public static void write(String message) {

        synchronized (messages) {
            messages.add(message);
        }
        logger.debug(message);

    }

    private static String getFullLogFileName(String logFileName) {

        String absoluteFilePath = FileUtil.createAbsolutePath(ZfinPropertiesEnum.CATALINA_BASE.value(), "logs", logFileName);
        return absoluteFilePath;
    }


}
