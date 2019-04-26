package org.zfin.framework.presentation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class to adjust existing logger levels or
 * create new appender.
 */
@Controller
@RequestMapping(value = "/devtool")
public class Log4jConfigurationController {

    @RequestMapping("/log4j-configuration")
    public String showClassPathInfo(@ModelAttribute("formBean") Log4JConfigurationFormBean form) throws ServletException {

        if (form.isUpdateExistingLogger()) {
            adjustLoggers(form);
        } else if (form.isCreateNewLogger()) {
            createLogger(form);
        }

        LoggerContext logContext = (LoggerContext) LogManager
                .getContext(false);
        Map<String, LoggerConfig> map = logContext.getConfiguration()
                .getLoggers();
        List<LoggerConfig> allLoggers = map.entrySet().stream()
                .map(Map.Entry::getValue)
                .sorted(new Log4jComparator())
                .collect(Collectors.toList());

        form.setAllLoggers(allLoggers);

        Set<Appender> appenderSet = allLoggers.stream()
                .map(loggerConfig -> loggerConfig.getAppenders().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        form.setAppenders(new ArrayList<>(appenderSet));
        return "log4j-configuration.page";
    }

    private void createLogger(Log4JConfigurationFormBean form) {
        String loggerName = form.getNewLoggerName();
        String loggerLevel = form.getNewLoggerLevel();
        Level newLevel = Level.toLevel(loggerLevel, null);
        Logger logger = LogManager.getLogger(loggerName);
        Configurator.setLevel(logger.getName(), newLevel);
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
                Configurator.setLevel(logger.getName(), newLevel);
            }
        }

    }


    /**
     * Sort loggers by their name.
     */
    static class Log4jComparator implements Comparator<LoggerConfig> {

        public int compare(LoggerConfig log1, LoggerConfig log2) {
            return log1.getName().compareTo(log2.getName());
        }
    }
}
