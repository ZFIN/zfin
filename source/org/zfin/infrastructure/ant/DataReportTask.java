package org.zfin.infrastructure.ant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    private String delimiter = "__";
    private String delimiter2 = "__";

    private String variableNames;
    private String valueNames;
    private String variableNames2;
    private String valueNames2;
    private boolean useParameterMap;

    protected DataReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public int execute() {

        if (useParameterMap) {
            String[] variables = variableNames.split(delimiter);
            if(variables.length!=1) {
                String[] values = valueNames.split(delimiter);
                LOG.info("Job Name: " + variables.length);
                if (variables.length != values.length)
                    throw new RuntimeException("The number of variables need to match the number of values" + variableNames + valueNames);
                for (int index = 0; index < values.length; index++) {
                    dataMap.put(variables[index], values[index]);
                }
            }
            else{
                LOG.info("Job Name: " + jobName);

                String[] variables2 = variableNames2.split(delimiter2);
                String[] values2 = valueNames2.split(delimiter2);
                LOG.info("Job Name: " + values2[0]);
                if (variables2.length != values2.length)
                    throw new RuntimeException("The number of variables need to match the number of values" + variableNames2 + valueNames2);
                for (int index = 0; index < values2.length; index++) {
                    dataMap.put(variables2[index], values2[index]);
                }
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
    public static final Option dateVariablesOpt = OptionBuilder.withArgName("dateVariables").hasArg().withDescription("List of parameter variable names").create("dateVariables");
    public static final Option dateValuesOpt = OptionBuilder.withArgName("dateValues").hasArg().withDescription("List of parameter values").create("dateValues");

    public static final Option dataDirOpt = OptionBuilder.withArgName("dataDir").hasArg().withDescription("Base Data directory").create("dataDir");
    public static final Option propertyDirOpt = OptionBuilder.withArgName("propertyDir").hasArg().withDescription("Property File directory").create("propertyDir");
    public static final Option useParametersOpt = OptionBuilder.withArgName("useParameters").hasArg().withDescription("Boolean that indicates if the command line options contain a parameter map").create("useParameters");


    static {
        jobNameOpt.setRequired(true);
        options.addOption(jobNameOpt);
        options.addOption(parameterVariablesOpt);
        options.addOption(parameterValuesOpt);
        options.addOption(dateVariablesOpt);
        options.addOption(dateValuesOpt);
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
        task.variableNames2 = commandLine.getOptionValue(dateVariablesOpt.getOpt());

            String values = commandLine.getOptionValue(parameterValuesOpt.getOpt());
        String values2 = commandLine.getOptionValue(dateValuesOpt.getOpt());

            String delimiter = commandLine.getOptionValue(delimiterOpt.getOpt());
            if (StringUtils.isNotEmpty(delimiter)) {
                task.delimiter = delimiter;
            }
            if (StringUtils.isBlank(values)) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy__MM__dd");
                values = df.format(new Date());
            }
            task.valueNames = values;




            String delimiter2 = commandLine.getOptionValue(delimiterOpt.getOpt());
            if (StringUtils.isNotEmpty(delimiter2)) {
                task.delimiter = delimiter2;
            }

            if (StringUtils.isBlank(values2)){
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.setTime(new Date()); // Now use today date.
                c.add(Calendar.DATE, -30); // Adding 5 days
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c1 = Calendar.getInstance();
                c1.setTime(new Date()); // Now use today date.

                values2 = sdf1.format(c.getTime())+"__"+sdf2.format(c1.getTime());

            }
            task.valueNames2 = values2;
        }

}


