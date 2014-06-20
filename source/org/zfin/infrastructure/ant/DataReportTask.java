package org.zfin.infrastructure.ant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    private String delimiter = "__";
    private static final String REPORT_DIRECTORY = "server_apps/DB_maintenance/report_data";

    private String variableNames;
    private String valueNames;
    private boolean useParameterMap;

    public void execute() {
        LOG.getRootLogger().setLevel(Level.INFO);
        LOG.info("Job Name: " + jobName);
        if (useParameterMap) {
            String[] variables = variableNames.split(delimiter);
            String[] values = valueNames.split(delimiter);
            if (variables.length != values.length)
                throw new RuntimeException("The number of variables need to match the number of values");
            for (int index = 0; index < values.length; index++) {
                dataMap.put(variables[index], values[index]);
            }
        }
        queryFile = new File(dataDirectory, jobName + ".sql");
        if (!queryFile.exists()) {
            String message = "No file found: " + queryFile.getAbsolutePath();
            queryFile = new File(dataDirectory, jobName + ".sqlj");
            if (!queryFile.exists()) {
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
        LOG.info("Handling file : " + queryFile.getAbsolutePath());
        runQueryFile(queryFile);
    }

    @Override
    protected void addCustomVariables(Map<String, Object> map) {
        map.putAll(dataMap);
    }

    private void runQueryFile(File dbQueryFile) {
        setLoggerFile();
        clearReportDirectory();
        copyFileToReportDirectory(dbQueryFile);
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile, null, dataMap);
            List<List<List<String>>> resultList = service.getListOfResultRecords();
            if (resultList != null)
                createReportFile(dataDirectory, resultList.get(0), errorMessages);
            else
                createErrorReport(errorMessages, null, dataDirectory);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.rollbackTransaction();
            HibernateUtil.closeSession();
        }
    }

    public static final Option jobNameOpt = OptionBuilder.withArgName("jobName").hasArg().withDescription("job name").create("jobName");
    public static final Option parameterVariablesOpt = OptionBuilder.withArgName("parameterVariables").hasArg().withDescription("List of parameter variable names").create("parameterVariables");
    public static final Option parameterValuesOpt = OptionBuilder.withArgName("parameterValues").hasArg().withDescription("List of parameter variable names").create("parameterValues");
    public static final Option delimiterOpt = OptionBuilder.withArgName("delimiter").hasArg().withDescription("delimiter used to separate individual parameters").create("delimiter");
    public static final Option baseDirOpt = OptionBuilder.withArgName("baseDir").hasArg().withDescription("Base directory").create("baseDir");
    public static final Option useParametersOpt = OptionBuilder.withArgName("useParameters").hasArg().withDescription("Boolean that indicates if the command line options contain a parameter map").create("useParameters");


    static {
        jobNameOpt.setRequired(true);
        options.addOption(jobNameOpt);
        options.addOption(parameterVariablesOpt);
        options.addOption(parameterValuesOpt);
        options.addOption(delimiterOpt);
        options.addOption(baseDirOpt);
        options.addOption(useParametersOpt);
    }

    public static void main(String[] args) {
        CommandLine commandLine = parseArguments(args, "???");
        String jobName = commandLine.getOptionValue(jobNameOpt.getOpt());
        DataReportTask task = new DataReportTask();
        task.setJobName(jobName);
        String useParameterMap = commandLine.getOptionValue(useParametersOpt.getOpt());
        if (StringUtils.isNotEmpty(useParameterMap) && useParameterMap.equals("true")) {
            task.useParameterMap = true;
            handleParameterMap(commandLine, task);
        }
        task.propertiesFile = "report-data-email.properties";
        String baseDir = commandLine.getOptionValue(baseDirOpt.getOpt());
        String pathname = baseDir + "/" + REPORT_DIRECTORY;
        if (baseDir != null) {
            task.propertyFilePath = baseDir + "/" + task.propertyFilePath;
            task.init(pathname);
        }
        task.dataDirectory = new File(pathname);
        task.execute();
    }

    private static void handleParameterMap(CommandLine commandLine, DataReportTask task) {
        task.variableNames = commandLine.getOptionValue(parameterVariablesOpt.getOpt());
        String values = commandLine.getOptionValue(parameterValuesOpt.getOpt());
        String delimiter = commandLine.getOptionValue(delimiterOpt.getOpt());
        if (StringUtils.isNotEmpty(delimiter))
            task.delimiter = delimiter;
        if (values.equals("")) {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH) + 1;
            int day = now.get(Calendar.DAY_OF_MONTH);
            values = year + "__" + getPaddedNumber(month) + "__" + getPaddedNumber(day);
        }
        task.valueNames = values;
    }

    private static String getPaddedNumber(int month) {
        if (month > 9)
            return String.valueOf(month);
        else
            return "0" + String.valueOf(month);
    }

}
