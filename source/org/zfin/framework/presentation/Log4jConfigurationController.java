package org.zfin.framework.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller class to adjust exsisting logger levels or
 * create new appenders.
 */
public class Log4jConfigurationController extends AbstractCommandController {

    public Log4jConfigurationController() {
        setCommandClass(Log4JConfigurationFormBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
                                  BindException errors) throws Exception {

        Log4JConfigurationFormBean form = (Log4JConfigurationFormBean) command;

        if (form.isUpdateExistingLogger()) {
            adjustLoggers(form);
        } else if (form.isCreateNewLogger()) {
            createLogger(form);
        }

        Enumeration enumLoggers = LogManager.getCurrentLoggers();
        List<Logger> allLoggers = new ArrayList<Logger>();
        CollectionUtils.addAll(allLoggers, enumLoggers);
        Collections.sort(allLoggers, new Log4jComparator());
        form.setAllLoggers(allLoggers);
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        List<Appender> appenders = new ArrayList<Appender>(5);
        while( allAppenders.hasMoreElements()){
            appenders.add((Appender) allAppenders.nextElement());
        }
        form.setAppenders(appenders);
        return new ModelAndView("log4j-configuration", "loggerForm", form);
    }

    private void createLogger(Log4JConfigurationFormBean form) {
        String loggerName = form.getNewLoggerName();
        String loggerLevel = form.getNewLoggerLevel();
        Level newLevel = Level.toLevel(loggerLevel, null);
        Logger logger = LogManager.getLogger(loggerName);
        logger.setLevel(newLevel);
    }

    private void adjustLoggers(Log4JConfigurationFormBean form) {
        String[] names = form.getLoggerName();
        String[] levels = form.getLevel();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String level = levels[i];
            Logger logger = LogManager.getLogger(name);
            if (logger == null) {
                throw new RuntimeException("Logger Name '" + name + "' not found");
            } else {
                Level newLevel = Level.toLevel(level, null);
                logger.setLevel(newLevel);
            }
        }

    }


    /**
     * Sort loggers by their name.
     */
    static class Log4jComparator implements Comparator<Logger> {

        public int compare(Logger log1, Logger log2) {
            return log1.getName().compareTo(log2.getName());
        }
    }
}
