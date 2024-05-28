package org.zfin.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Convenience methods for data-related info.
 */
public class DateUtil {

    public static final int MILLISECONDS_PER_DAY = 3600000 * 24;
    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static String getTimeDuration(long start, long end) {
        return getTimeDuration(new Date(start), new Date(end));
    }

    public static String getTimeDuration(long start) {
        return getTimeDuration(new Date(start), new Date());
    }

    public static String getTimeDuration(Date start, Date end) {
        if (end == null)
            end = new Date();
        long timeDifference = end.getTime() - start.getTime();
        StringBuffer dateDisplay = new StringBuffer();
        if (timeDifference > MILLISECONDS_PER_DAY) {
            dateDisplay.append(timeDifference / MILLISECONDS_PER_DAY);
            dateDisplay.append(" days ");
        } else if (timeDifference > MILLISECONDS_PER_HOUR) {
            dateDisplay.append((timeDifference / MILLISECONDS_PER_HOUR) % 60);
            dateDisplay.append(" hours ");
        } else if (timeDifference > MILLISECONDS_PER_MINUTE) {
            dateDisplay.append((timeDifference / MILLISECONDS_PER_MINUTE) % 60);
            dateDisplay.append(" minutes ");
        } else if (timeDifference > MILLISECONDS_PER_SECOND) {
            dateDisplay.append((timeDifference / MILLISECONDS_PER_SECOND) % 60);
            dateDisplay.append(" seconds ");
        } else {
            dateDisplay.append(timeDifference);
            dateDisplay.append(" ms ");
        }
        return dateDisplay.toString();
    }

    /**
     * Return the current date in the format specified.
     * @param format (e.g. "yyyy-MM-dd", or "yyyy-MM-dd HH:mm:ss")
     * @return formatted date string (e.g. "2021-01-01", or "2021-01-01 12:34:56")
     */
    public static String nowToString(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

}
