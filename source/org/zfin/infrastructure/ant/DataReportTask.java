package org.zfin.infrastructure.ant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    private String delimiter = "__";

    private String variableNames;
    private String valueNames;
    private boolean useParameterMap;

    protected DataReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public int execute() {
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
        return 0;
    }

    private void initLogger() {
        clearReportDirectory();
        setLoggerFile();
        LOG.getRootLogger().setLevel(Level.WARN);
    }

    @Override
    protected void addCustomVariables(Map<String, Object> map) {
        map.putAll(dataMap);
    }

    private void runQueryFile(File dbQueryFile) {
        copyFileToReportDirectory(dbQueryFile);
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile, null, dataMap);
            Map<String, List<List<String>>> resultMap = service.getResultMap();
            generateReports(errorMessages, resultMap);
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
    public static final Option dataDirOpt = OptionBuilder.withArgName("dataDir").hasArg().withDescription("Base Data directory").create("dataDir");
    public static final Option propertyDirOpt = OptionBuilder.withArgName("propertyDir").hasArg().withDescription("Property File directory").create("propertyDir");
    public static final Option useParametersOpt = OptionBuilder.withArgName("useParameters").hasArg().withDescription("Boolean that indicates if the command line options contain a parameter map").create("useParameters");


    static {
        jobNameOpt.setRequired(true);
        options.addOption(jobNameOpt);
        options.addOption(parameterVariablesOpt);
        options.addOption(parameterValuesOpt);
        options.addOption(delimiterOpt);
        options.addOption(dataDirOpt);
        options.addOption(propertyDirOpt);
        options.addOption(useParametersOpt);
    }

    public static void main(String[] args) {
        CommandLine commandLine = parseArguments(args, "???");
        String jobName = commandLine.getOptionValue(jobNameOpt.getOpt());
        String useParameterMap = commandLine.getOptionValue(useParametersOpt.getOpt());
        DataReportTask task = new DataReportTask(jobName, commandLine.getOptionValue(propertyDirOpt.getOpt()),
                commandLine.getOptionValue(dataDirOpt.getOpt()));
        if (StringUtils.isNotEmpty(useParameterMap) && useParameterMap.equals("true")) {
            task.useParameterMap = true;
            handleParameterMap(commandLine, task);
        }
        task.initLogger();
        task.initDatabase();
        LOG.getRootLogger().setLevel(Level.INFO);
        System.exit(task.execute());
    }

    private static void handleParameterMap(CommandLine commandLine, DataReportTask task) {
        task.variableNames = commandLine.getOptionValue(parameterVariablesOpt.getOpt());
        String values = commandLine.getOptionValue(parameterValuesOpt.getOpt());
        String delimiter = commandLine.getOptionValue(delimiterOpt.getOpt());
        if (StringUtils.isNotEmpty(delimiter)) {
            task.delimiter = delimiter;
        }
        if (StringUtils.isBlank(values)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy__MM__dd");
            values = df.format(new Date());
        }
        task.valueNames = values;
    }

}
