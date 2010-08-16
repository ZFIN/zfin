package org.zfin.util.log4j;

import org.apache.log4j.Logger;
import org.zfin.util.ZfinSMTPAppender;
import org.zfin.util.servlet.RequestBean;
import org.zfin.util.servlet.ServletService;

import java.util.Enumeration;

/**
 * Service class to provide in regards to log4j.
 */
public class Log4jService {

    public static ZfinSMTPAppender getSmtpAppender() {
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        if (allAppenders == null)
            return null;

        while (allAppenders.hasMoreElements()) {
            Object appender = allAppenders.nextElement();
            if (appender instanceof ZfinSMTPAppender) {
                return (ZfinSMTPAppender) appender;
            }
        }
        return null;
    }
}
