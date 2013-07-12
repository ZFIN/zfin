package org.zfin.util;

import net.logstash.log4j.JSONEventLayout;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;
import org.zfin.framework.filter.AddRequestInfoToLog4j;
import org.zfin.profile.Person;

import java.util.Map;

/**
 * Extend the JSONEventLayout to include Person.
 */
public class ZfinJsonEventLayout extends JSONEventLayout {

    @Override
    public String format(LoggingEvent loggingEvent) {
        try {
            Person person = Person.getCurrentSecurityUser();
            if (person != null && person.getFullName() != null) {
                Map map = (Map) MDC.get(AddRequestInfoToLog4j.REQUEST_MAP);
                map.put("user", person.getFullName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ignore error to make sure request does not fail..
        }

        return super.format(loggingEvent);
    }
}
