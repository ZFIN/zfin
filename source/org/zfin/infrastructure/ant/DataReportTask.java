package org.zfin.infrastructure.ant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    protected String delimiter = "__";
    protected String variableNames;
    protected String valueNames;
    private boolean useParameterMap;

    public DataReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public int execute() {
        LOG.info("Job Name: " + jobName);
        if (useParameterMap) {
            String[] variables = variableNames.split(delimiter);
            String[] values = valueNames.split(delimiter);
            if (variables.length != values.length) {
                throw new RuntimeException("The number of variables need to match the number of values" + variableNames + valueNames);
            }
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
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            LOG.error(e);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static final Option jobNameOpt = OptionBuilder.withArgName("jobName").hasArg().withDescription("job name").create("jobName");
    public static final Option parameterVariablesOpt = OptionBuilder.withArgName("parameterVariables").hasArg().withDescription("List of parameter variable names").create("parameterVariables");
    public static final Option parameterValuesOpt = OptionBuilder.withArgName("parameterValues").hasArg().withDescription("List of parameter values").create("parameterValues");
    public static final Option delimiterOpt = OptionBuilder.withArgName("delimiter").hasArg().withDescription("delimiter used to separate individual parameters").create("delimiter");
    public static final Option taskClassNameOpt = OptionBuilder.withArgName("taskClassName").hasArg().withDescription("Name of the class (inheriting from DataReportTask) to instantiate").create("taskClassName");
    public static final Option dataDirOpt = OptionBuilder.withArgName("dataDir").hasArg().withDescription("Base Data directory").create("dataDir");
    public static final Option propertyDirOpt = OptionBuilder.withArgName("propertyDir").hasArg().withDescription("Property File directory").create("propertyDir");
    public static final Option useParametersOpt = OptionBuilder.withArgName("useParameters").hasArg().withDescription("Boolean that indicates if the command line options contain a parameter map").create("useParameters");


    static {
        jobNameOpt.setRequired(true);
        options.addOption(jobNameOpt);
        options.addOption(parameterVariablesOpt);
        options.addOption(parameterValuesOpt);
        options.addOption(delimiterOpt);
        options.addOption(taskClassNameOpt);
        options.addOption(dataDirOpt);
        options.addOption(propertyDirOpt);
        options.addOption(useParametersOpt);
    }

    public static void main(String[] args) {
        LOG.getRootLogger().setLevel(Level.INFO);
        CommandLine commandLine = parseArguments(args, "???");
        String taskClassName = commandLine.getOptionValue(taskClassNameOpt.getOpt());
        String jobName = commandLine.getOptionValue(jobNameOpt.getOpt());
        String useParameterMap = commandLine.getOptionValue(useParametersOpt.getOpt());
        String propertyDir = commandLine.getOptionValue(propertyDirOpt.getOpt());
        String dataDir = commandLine.getOptionValue(dataDirOpt.getOpt());
        DataReportTask task = null;
        try {
            task = (DataReportTask) (Class.forName(taskClassName)
                    .getConstructor(String.class, String.class, String.class)
                    .newInstance(jobName, propertyDir, dataDir));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
            LOG.error("Could not create instance of class " + taskClassName + ".");
            System.exit(1);
        }
        if (StringUtils.isNotEmpty(useParameterMap) && useParameterMap.equals("true")) {
            task.useParameterMap = true;
            task.handleParameterMap(commandLine);
        }
        task.initLogger();
        task.initDatabase();
        System.exit(task.execute());
    }

    protected void handleParameterMap(CommandLine commandLine) {
        variableNames = commandLine.getOptionValue(parameterVariablesOpt.getOpt());
        String values = commandLine.getOptionValue(parameterValuesOpt.getOpt());
        String delimiter = commandLine.getOptionValue(delimiterOpt.getOpt());

        if (StringUtils.isNotEmpty(delimiter)) {
            this.delimiter = delimiter;
        }
        if (StringUtils.isBlank(values)) {
            values = handleBlankParameterValue();
        }
        valueNames = values;
    }

    protected String handleBlankParameterValue() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy__MM__dd");
        return df.format(new Date());
    }

}


