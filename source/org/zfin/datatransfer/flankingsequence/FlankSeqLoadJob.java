package org.zfin.datatransfer.flankingsequence;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.util.ReportGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class FlankSeqLoadJob extends AbstractValidateDataReportTask {

private static Logger logger = LogManager.getLogger(FlankSeqLoadJob.class);



public FlankSeqLoadJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
        }

@Override
public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();


        FlankSeqProcessor fsProcessor = new FlankSeqProcessor();
        fsProcessor.updateFlankingSequences();


        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        for (String message : fsProcessor.getMessages()) {
        rg.addIntroParagraph(message);
        }
        List<List<String>> updated = fsProcessor.getUpdated();
        rg.addDataTable(updated.size() + " Updated features", Arrays.asList("feature", "seq1", "seq2"), updated);
        for (String error : fsProcessor.getErrors()) {
        rg.addErrorMessage(error);
        }
        rg.writeFiles(new File(dataDirectory, jobName), jobName);

        return fsProcessor.getErrors().size();
        }

public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        String jobName = args[2];
        FlankSeqLoadJob job = new FlankSeqLoadJob(jobName, args[0], args[1]);

        job.initDatabase();
        System.exit(job.execute());
        }
        }









