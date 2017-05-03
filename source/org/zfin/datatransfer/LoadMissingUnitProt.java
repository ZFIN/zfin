package org.zfin.datatransfer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.DataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.util.DateUtil;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;

public class LoadMissingUnitProt extends AbstractValidateDataReportTask {

    private static final String UNIPROT_INFO_FILE = "uniprot-all.txt";
    private static final String ENSDARG_UNIPROT_FILE = "mart_export.txt";
    private static final String UNIPROT_INFO_UNL = "uniprot-info.unl";
    private static final String ENSEMBL_UNIPROT_UNL = "ensembl-uniprot.unl";
    private static final String LOAD_MISSING_UNIPROT_RECORDS_SQL = "load-missing-uniprot-records.sql";
    private static final String NEW_UNIPROT_IDS_REPORT_NAME = "new_uniprot_ids";

    private File loadDirectory;

    static {
        options.addOption(loadDir);
        options.addOption(log4jFileOption);
        options.addOption(dbScriptFileOption);
        options.addOption(webrootDirectory);
        options.addOption(productionModeOption);
        options.addOption(debugModeOption);
        options.addOption(DataReportTask.jobNameOpt);
    }

    private LoadMissingUnitProt(String jobName, String propertyFilePath, String dataDirectoryString, String... scriptFiles) throws IOException {
        super(jobName, propertyFilePath, dataDirectoryString);
        initializeLoad(scriptFiles);
    }

    private void initializeLoad(String[] scriptFiles) throws IOException {
        if (scriptFiles != null) {
            for (String dbScriptFile : scriptFiles) {
                File file = new File(dbScriptFile);
                loadDirectory = new File(FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath()));
                if (!FileUtil.checkFileExists(dbScriptFile))
                    throw new IOException("No DB script file <" + dbScriptFile + "> found!");
            }
        }
        clearReportDirectory();
        setReportProperties();
    }

    public static void main(String[] arguments) throws IOException {
        initLogging();
        CommandLine commandLine = parseArguments(arguments, "load <>");
        String dbScriptFilesNames = commandLine.getOptionValue(dbScriptFileOption.getOpt());
        String webrootDir = commandLine.getOptionValue(webrootDirectory.getOpt());
        String jobName = commandLine.getOptionValue(DataReportTask.jobNameOpt.getOpt());
        String loadingDir = commandLine.getOptionValue(loadDir.getOpt());
        String[] dbScriptFiles = dbScriptFilesNames.split(",");
        String propertyFileName = getPropertyFileFromWebroot(webrootDir);

        LoadMissingUnitProt load = new LoadMissingUnitProt(jobName, propertyFileName, loadingDir, dbScriptFiles);
        load.execute();
        System.exit(0);
    }


    public int execute() {
        try {
            parseEnsdargUniprotFile();
            parseUniprotInfoFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initAll(propertyFilePath);
        runLoad();
        return 0;
    }

    private void parseUniprotInfoFile() throws IOException {
        Reader unitprot = new FileReader(UNIPROT_INFO_FILE);
        Iterable<CSVRecord> uniprotRecords = CSVFormat.TDF.withFirstRecordAsHeader().parse(unitprot);
        List<UnitProtInfo> unitProtList = new ArrayList<>();
        for (CSVRecord record : uniprotRecords) {
            unitProtList.add(new UnitProtInfo(record.get(0), record.get(1), record.get(2)));
        }
        List<List<String>> uniprotIDs = unitProtList.stream()
                .map(unitproRecord -> {
                    List<String> ids = new ArrayList<>(3);
                    ids.add(unitproRecord.getUniprotID());
                    ids.add(unitproRecord.getLength().toString());
                    ids.add(unitproRecord.getGeneName());
                    return ids;
                })
                .collect(Collectors.toList());
        dataMap.put(UNIPROT_INFO_UNL, uniprotIDs);
        LOG.info("Number of UnitProt Records from Uniprot: " + unitProtList.size());
    }

    private void parseEnsdargUniprotFile() throws IOException {
        Reader in = new FileReader(ENSDARG_UNIPROT_FILE);
        Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in);
        List<EnsdargUniprotMapping> idList = new ArrayList<>();
        for (CSVRecord record : records) {
            idList.addAll(getRecords(record.get(0), record.get(1), record.get(2)));
        }
        List<List<String>> ensemblIDs = idList.stream()
                .map(ensdargRecord -> {
                    List<String> ids = new ArrayList<>(5);
                    ids.add(ensdargRecord.getEnsdargID());
                    ids.add(ensdargRecord.getUnitprotID());
                    return ids;
                })
                .collect(Collectors.toList());
        dataMap.put(ENSEMBL_UNIPROT_UNL, ensemblIDs);

        Map<String, Set<String>> ensdargIdMap = idList.stream()
                .collect(Collectors.groupingBy(EnsdargUniprotMapping::getEnsdargID,
                        Collectors.mapping(EnsdargUniprotMapping::getUnitprotID, Collectors.toSet())));

        LOG.info("ENSDARG / Uniprot Records: " + idList.size());
        LOG.info("Distinct ENSDARG IDS: " + ensdargIdMap.keySet().size());
    }

    private static Map<String, List<List<String>>> dataMap = new HashMap<>(20);

    private void runLoad() {
        try {
            HibernateUtil.createTransaction();
            String[] dbScriptFiles = {LOAD_MISSING_UNIPROT_RECORDS_SQL};
            DatabaseService service = new DatabaseService();
            for (String dbScriptFile : dbScriptFiles) {
                LOG.info("Running Db script: " + dbScriptFile);
                long startTimeLong = System.currentTimeMillis();
                service.runDbScriptFile(dbScriptFile, dataMap);
                LOG.info("Duration of Script Execution: " + DateUtil.getTimeDuration(startTimeLong));
            }
            //HibernateUtil.flushAndCommitCurrentSession();
            LOG.info("Committed load...");
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
        createReport();
    }

    private void createReport() {
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, loadDirectory, jobName, true);
        createErrorReport(null, dataMap.get(NEW_UNIPROT_IDS_REPORT_NAME), reportConfiguration);
    }

    private static List<EnsdargUniprotMapping> getRecords(String ensdarg, String swiss, String trembl) {
        List<EnsdargUniprotMapping> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(swiss)) {
            list.add(new EnsdargUniprotMapping(ensdarg, swiss));
        }
        if (StringUtils.isNotEmpty(trembl)) {
            list.add(new EnsdargUniprotMapping(ensdarg, trembl));
        }
        return list;
    }

    private static void initLogging() {
        initLog4J();
        Logger.getLogger(DatabaseService.class).setLevel(Level.INFO);
        Logger.getLogger(AbstractScriptWrapper.class).setLevel(Level.INFO);
        Logger.getLogger(LoadMissingUnitProt.class).setLevel(Level.INFO);
    }
}


class EnsdargUniprotMapping {
    private String ensdargID;
    private String unitprotID;

    EnsdargUniprotMapping(String ensdargID, String unitprotID) {
        this.ensdargID = ensdargID;
        this.unitprotID = unitprotID;
    }

    String getEnsdargID() {
        return ensdargID;
    }

    String getUnitprotID() {
        return unitprotID;
    }
}

class UnitProtInfo {
    private String uniprotID;
    private String geneName;
    private Integer length;

    UnitProtInfo(String unitprotID, String length, String geneName) {
        this.uniprotID = unitprotID;
        this.geneName = geneName;
        this.length = Integer.parseInt(length);
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    String getUniprotID() {
        return uniprotID;
    }

    public String getGeneName() {
        return geneName;
    }

    public Integer getLength() {
        return length;
    }
}

