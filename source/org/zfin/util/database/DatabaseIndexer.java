package org.zfin.util.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.zfin.database.presentation.Table;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.properties.ZfinProperties;

import java.io.*;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.log4jFileOption;
import static org.zfin.util.database.IndexerFields.*;


/**
 * Go through all unload files of a given database (typically production) and find a given string in one or more tables.
 * It will return the first appearance of a string and when it disappeared by chronologically inspecting the unload files.
 * It uses a class Grep which is marginally changed to our purposes as it uses the Java optimized nio library to speedy
 * reads of files.
 */
@Service
public class DatabaseIndexer extends AbstractScriptWrapper {

    private static Logger LOG = Logger.getLogger(DatabaseIndexer.class);

    public static final Option updateIndexOption = OptionBuilder.withArgName("updateIndexOption").hasArg().withDescription("Update an existing indexWriter").create("updateIndex");
    public static final Option tablesOption = OptionBuilder.withArgName("tablesOption").hasArg().withDescription("list of tables and their ZDB-ID table").create("tables");
    public static final Option indexLastUnloadsOption = OptionBuilder.withArgName("indexLastUnloadsOption").hasArg().withDescription("number of unload files to indexWriter: last N").create("indexLastUnloads");
    private IndexWriter indexWriter;

    static {
        options.addOption(updateIndexOption);
        options.addOption(tablesOption);
        options.addOption(indexLastUnloadsOption);
        options.addOption(log4jFileOption);
    }

    private List<Table> tableNames;
    private String beginDate;
    private String endDate = "2012";
    private Action action;
    private int numberOfUnloadFilesToIndex;
    @Autowired
    private UnloadIndexingService unloadIndexingService;
    @Autowired
    private UnloadService unloadService;
    private boolean indexWriterClosed = true;

    // Used from Controller within Tomcat
    public DatabaseIndexer() throws IOException {
    }

    @DependsOn({"unloadService", "unloadIndexingService"})
    private IndexWriter getIndexWriter() throws IOException {
        if (indexWriter == null || indexWriterClosed) {
            indexWriter = new IndexWriter(unloadService.getIndexerFile(), unloadService.getAnalyzer(), false);
            indexWriterClosed = false;
        }
        return indexWriter;
    }

    public DatabaseIndexer(boolean incremental, List<Table> tables) throws IOException {
        init(incremental);
        if (tables != null)
            tableNames = tables;
        else
            tableNames = Table.getAllTablesWithZdbPk();
    }

    private void init(boolean incremental) throws IOException {
        File configurationFile = new File("conf", "unload-indexer.xml");
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("file:" + configurationFile.getAbsolutePath());
        unloadIndexingService = (UnloadIndexingService) context.getBean("unloadIndexingService");
        unloadService = (UnloadService) context.getBean("unloadService");
        if (!unloadService.getIndexerFile().exists() || unloadService.getIndexerFile().list().length < 3)
            incremental = false;
        indexWriter = new IndexWriter(unloadService.getIndexerFile(), unloadService.getAnalyzer(), !incremental);
        indexWriterClosed = false;
    }


    public static void main(String[] arguments) throws IOException {
        LOG = Logger.getLogger("");
        LOG.setLevel(Level.INFO);
        LOG.info("Start Data History Inspector: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        String optionValue = commandLine.getOptionValue(log4jFileOption.getOpt());
        if (optionValue == null)
            optionValue = "test/log4j.xml";
        initializeLogger(optionValue);
        String tablesString = commandLine.getOptionValue(tablesOption.getOpt());
        List<Table> tableList = null;
        if (tablesString != null)
            tableList = getTableEntityPattern(tablesString);
        String updateIndexString = commandLine.getOptionValue(updateIndexOption.getOpt());
        boolean updateIndex = Boolean.parseBoolean(updateIndexString);
        String unloadNumberString = commandLine.getOptionValue(indexLastUnloadsOption.getOpt());
        ZfinProperties.init();
        DatabaseIndexer inspector = new DatabaseIndexer(updateIndex, tableList);
        if (StringUtils.isNotEmpty(unloadNumberString))
            inspector.setNumberOfUnloadFilesToIndex(Integer.valueOf(unloadNumberString));
        inspector.run();
    }

    private static List<Table> getTableEntityPattern(String tablesString) {
        String[] tableNames = tablesString.split(",");
        List<Table> tables = new ArrayList<Table>(tableNames.length);
        for (String tableName : tableNames)
            tables.add(Table.getEntityTableByTableName(tableName));
        return tables;
    }

    private void run() {
        CronJobReport report = new CronJobReport("Check Entity Occurrence: ", cronJobUtil);
        report.start();
        try {
            indexAllUnloadDirectories();
            getIndexWriter().optimize();
        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
        } finally {
            try {
                closeIndexWriter();
            } catch (IOException e) {
                LOG.error(e);
                e.printStackTrace();
            }
        }
        report.finish();
        createReport();
        LOG.info(report.getDuration());
    }

    private void closeIndexWriter() throws IOException {
        getIndexWriter().close();
        indexWriterClosed = true;
    }


    public void runUpdate(String tableName) {
        Table table = Table.getEntityTableByTableName(tableName);
        tableNames = new ArrayList<Table>(1);
        tableNames.add(table);
        try {
            retrieveUnindexedDates(table);
            getIndexWriter().optimize();
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            try {
                closeIndexWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // ensure that the new data is visible for the search
        unloadService.refreshDataMap();
    }

    private void retrieveUnindexedDates(Table table) {
        String dateOfLastIndex = unloadService.getLatestIndexedDate(table);
        if (dateOfLastIndex == null)
            return;

        List<File> newFiles = new ArrayList<File>();
        for (File unloadFile : unloadIndexingService.getUnloadFiles()) {
            if (unloadFile.getName().compareTo(dateOfLastIndex) >= 0)
                newFiles.add(unloadFile);
        }
        indexListOfUnloadFiles(newFiles);
    }

    // indexWriter from a given date on. The first date (first file) is already been indexed but
    // is needed to decided if a record had been changed.
    private void indexListOfUnloadFiles(List<File> unloadFiles) {
        for (File unloadFile : unloadFiles) {
            // do not indexWriter the first date
            boolean indexUnloadFile = !unloadFiles.get(0).equals(unloadFile);
            String date = unloadFile.getName();
            LOG.info("Indexing Date: " + date);
            for (Table table : tableNames) {
                indexSingleDateAndTable(unloadFile, date, table, indexUnloadFile);
            }
            previousDate = date;
        }
    }

    private Map<String, Set<String>> previousRecords = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> previousEntityIds = new HashMap<String, Set<String>>();
    private String previousDate;

    private void indexAllUnloadDirectories() {
        List<File> unloadFiles = unloadIndexingService.getUnloadFiles();
        // <tableName,set<record>>
        int indexCount = 0;
        int startingIndex = 0;
        if (numberOfUnloadFilesToIndex > 0)
            startingIndex = unloadFiles.size() - numberOfUnloadFilesToIndex;
        for (File unloadFile : unloadFiles) {
            // only indexWriter the last n unload files.
            if (indexCount++ < startingIndex)
                continue;
            String date = unloadFile.getName();
            LOG.info("Indexing Date: " + date);
            for (Table table : tableNames) {
                indexSingleDateAndTable(unloadFile, date, table);
            }
            previousDate = date;
        }
    }

    private void indexSingleDateAndTable(File unloadFile, String date, Table table) {
        indexSingleDateAndTable(unloadFile, date, table, true);
    }

    private void indexSingleDateAndTable(File unloadFile, String date, Table table, boolean indexDatedUnloaded) {
        File tableFile = getTableDirectory(table.getTableName(), unloadFile);
        String tableName = tableFile.getName();
        String patternExpression = table.getPkIdentifier();
        if (table == Table.MARKER)
            patternExpression = "GENE";
        indexDatedUnload(tableFile, date, patternExpression, indexDatedUnloaded);
        if (indexDatedUnloaded)
            addSingleFieldDocToIndexer(date, tableName);
    }

    private void addSingleFieldDocToIndexer(String date, String tableName) {
        Document doc = new Document();
        doc.add(new Field(UNLOAD_DATE, date, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(UNLOAD_TABLE_NAME, tableName, Field.Store.YES, Field.Index.NO_NORMS));
        try {
            getIndexWriter().addDocument(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void indexDatedUnload(File unloadFile, String date, String entityPattern, boolean indexUnloadFile) {
        String tableName = unloadFile.getName();
        Set<String> previousTableRecords = previousRecords.get(tableName);
        if (previousTableRecords == null)
            previousTableRecords = new HashSet<String>(0);
        Set<String> previousTableEntityIds = previousEntityIds.get(tableName);
        if (previousTableEntityIds == null)
            previousTableEntityIds = new HashSet<String>(0);
        Set<String> currentTableRecords = new HashSet<String>(previousTableRecords.size() + 20);
        Set<String> currentTableEntityIds = new HashSet<String>(previousTableEntityIds.size() + 20);
        DataInputStream in = null;
        String strLine;
        try {
            FileInputStream fstream = new FileInputStream(unloadFile);
            // Get the object of DataInputStream
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            //Read File Line By Line
            int line = 0;
            while ((strLine = br.readLine()) != null) {
                String entityId = InspectDataHistory.getEntityIdFromFullRow(entityPattern, strLine);
                if (entityId == null) {
                    LOG.debug("Not a valid entity line [" + line + "]: " + strLine);
                    continue;
                }
                boolean newRecord = isNewRecord(entityId, previousTableEntityIds);
                boolean updated = false;
                if (!newRecord)
                    updated = isModifiedRecord(previousTableRecords, strLine);
                if (indexUnloadFile)
                    addToIndexer(date, unloadFile.getName(), entityId, updated, newRecord);
                currentTableRecords.add(strLine);
                currentTableEntityIds.add(entityId);
                line++;
            }
            LOG.info("Indexing Table: " + tableName + " with " + line + " numbers of records");
            if (indexUnloadFile)
                addTableStatsToIndexer(date, tableName, line);
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // create indexWriter documents for deleted entities
        if (indexUnloadFile)
            addDeletedDocumentsToIndexer(currentTableEntityIds, tableName);
        previousEntityIds.put(tableName, currentTableEntityIds);
        previousRecords.put(tableName, currentTableRecords);
    }

    private void addTableStatsToIndexer(String date, String tableName, int line) {
        Document doc = new Document();
        doc.add(new Field(SUMMARY_DATE, date, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(SUMMARY_TABLE_NAME, tableName, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(SUMMARY_NUMBER_OF_RECORDS, line + "", Field.Store.YES, Field.Index.NO_NORMS));
        saveDocumentToIndexer(doc);
    }

    private boolean isModifiedRecord(Set<String> previousTableRecords, String strLine) {
        if (CollectionUtils.isEmpty(previousTableRecords))
            return false;
        return !previousTableRecords.contains(strLine);
    }

    private boolean isNewRecord(String entityId, Set<String> previousTableEntityIds) {
        if (CollectionUtils.isEmpty(previousTableEntityIds))
            return false;
        return !previousTableEntityIds.contains(entityId);
    }

    private void addDeletedDocumentsToIndexer(Set<String> currentTableEntityIds, String tableName) {
        if (CollectionUtils.isEmpty(previousEntityIds.get(tableName)))
            return;

        for (String previousEntityId : previousEntityIds.get(tableName)) {
            // if record not found in subsequent unload it has been removed. Mark the last occurrence as deleted.
            if (!currentTableEntityIds.contains(previousEntityId)) {
                Document doc = new Document();
                doc.add(new Field(ENTITY_DATE, previousDate, Field.Store.YES, Field.Index.NO_NORMS));
                doc.add(new Field(ENTITY_TABLE_NAME, tableName, Field.Store.YES, Field.Index.NO_NORMS));
                doc.add(new Field(ENTITY_ID, previousEntityId, Field.Store.YES, Field.Index.NO_NORMS));
                doc.add(new Field(ENTITY_IS_DELETED, "true", Field.Store.YES, Field.Index.NO_NORMS));
                saveDocumentToIndexer(doc);
            }
        }
    }

    // do not indexWriter records that have not changed
    private void addToIndexer(String date, String tableName, String row, Boolean changed, Boolean newRecord) {
        if (!(changed || newRecord))
            return;
        Document doc = new Document();
        doc.add(new Field(ENTITY_DATE, date, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(ENTITY_TABLE_NAME, tableName, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(ENTITY_ID, row, Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(ENTITY_HAS_CHANGED, changed.toString(), Field.Store.YES, Field.Index.NO_NORMS));
        doc.add(new Field(ENTITY_IS_NEW, newRecord.toString(), Field.Store.YES, Field.Index.NO_NORMS));
        saveDocumentToIndexer(doc);
    }

    private void saveDocumentToIndexer(Document doc) {
        synchronized (this) {
            try {
                getIndexWriter().addDocument(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNumberOfUnloadFilesToIndex(int numberOfUnloadFilesToIndex) {
        this.numberOfUnloadFilesToIndex = numberOfUnloadFilesToIndex;
    }

    private File getTableDirectory(String tableName, File unloadDirectory) {
        return new File(unloadDirectory, tableName.toLowerCase());
    }

    private void createReport() {
/*
        Table output = new Table(4);
        output.addCell("Occurrence");
        output.addCell("Date");
        output.addCell("Table");
        output.addCell("Match");

        int indexWriter = -1;
        LOG.info("Report:\r" + output.render());
*/
    }


    public void indexNewTableByDate(String tableName, String date) {
        Table table = Table.getEntityTableByTableName(tableName);
        tableNames = new ArrayList<Table>(1);
        tableNames.add(table);
        if (unloadService.isTableIndexed(table))
            return;
        try {
            createDatesToBeIndexed(date);
            getIndexWriter().optimize();
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            try {
                closeIndexWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // ensure that the new data is visible for the search
        unloadService.refreshDataMap();

    }

    private void createDatesToBeIndexed(String date) {
        List<File> newFiles = new ArrayList<File>();
        for (File unloadFile : unloadIndexingService.getUnloadFiles()) {
            if (date == null || unloadFile.getName().compareTo(date) >= 0)
                newFiles.add(unloadFile);
        }
        indexListOfUnloadFiles(newFiles);
    }
}

