package org.zfin.framework.presentation;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Form bean being used whenever a property file is being viewed.
 */
public class Log4JConfigurationFormBean  {

    private List<Logger> allLoggers;
    private String LoggerLevel;
    private boolean showAllLevels;
    private String type;
    private String newLoggerName;
    private String newLoggerLevel;
    private String[] level;
    private String[] loggerName;
    private List<Appender> appenders;

    public List<Logger> getAllLoggers() {
        return allLoggers;
    }

    public void setAllLoggers(List<Logger> allLoggers) {
        this.allLoggers = allLoggers;
    }

    public String getLoggerLevel() {
        return LoggerLevel;
    }

    public void setLoggerLevel(String loggerLevel) {
        LoggerLevel = loggerLevel;
    }

    public boolean isShowAllLevels() {
        return showAllLevels;
    }

    public void setShowAllLevels(boolean showAllLevels) {
        this.showAllLevels = showAllLevels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getLevel() {
        return level;
    }

    public void setLevel(String[] level) {
        this.level = level;
    }

    public String getNewLoggerName() {
        return newLoggerName;
    }

    public void setNewLoggerName(String newLoggerName) {
        this.newLoggerName = newLoggerName;
    }

    public String getNewLoggerLevel() {
        return newLoggerLevel;
    }

    public void setNewLoggerLevel(String newLoggerLevel) {
        this.newLoggerLevel = newLoggerLevel;
    }

    public String[] getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String[] loggerName) {
        this.loggerName = loggerName;
    }

    boolean isUpdateExistingLogger() {
        return type != null && type.equals("update");
    }

    boolean isCreateNewLogger() {
        return type != null && type.equals("create");
    }

    public Map<String,String> getLoggerValues() {
        Map<String,String> entries = new LinkedHashMap<String, String>();
        entries.put("FATAL", "FATAL");
        entries.put("ERROR", "ERROR");
        entries.put("WARN", "WARN");
        entries.put("INFO", "INFO");
        entries.put("DEBUG", "DEBUG");
        entries.put("DEFAULT", "DEFAULT");
        return entries;
    }

    public void setAppenders(List<Appender> appenders) {
        this.appenders = appenders;
    }

    public List<Appender> getAppenders() {
        return appenders;
    }
}
