package org.zfin.util.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.nocrala.tools.texttablefmt.Table;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.uniquery.SiteSearchService;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.log4jFileOption;

/**
 * Go through all unload files of a given database (typically production) and find a given string in one or more tables.
 * It will return the first appearance of a string and when it disappeared by chronologically inspecting the unload files.
 * It uses a class Grep which is marginally changed to our purposes as it uses the Java optimized nio library to speedy
 * reads of files.
 */
public class InspectDataHistory extends AbstractScriptWrapper {

    private static Logger LOG;

    public static final Option entityIdOption = OptionBuilder.withArgName("entityIdOption").hasArg().withDescription("the zdb Id of a given entity").create("entityId");
    public static final Option tableNameOption = OptionBuilder.withArgName("tableNameOption").hasArg().withDescription("the table names to be checked").create("tableNames");
    public static final Option unloadDirectoryOption = OptionBuilder.withArgName("unloadDirectoryOption").hasArg().withDescription("unload directory").create("unloadDirectory");
    public static final Option beginOption = OptionBuilder.withArgName("beginOption").hasArg().withDescription("beginning date").create("beginDate");
    public static final Option endOption = OptionBuilder.withArgName("endOption").hasArg().withDescription("ending date").create("endDate");
    public static final Option actionOption = OptionBuilder.withArgName("actionOption").hasArg().withDescription("Action to perform").create("action");
    public static final Option indexerLocationOption = OptionBuilder.withArgName("indexerLocationOption").hasArg().withDescription("Location of the index directory").create("indexerLocation");

    static {
        options.addOption(entityIdOption);
        options.addOption(tableNameOption);
        options.addOption(unloadDirectoryOption);
        options.addOption(beginOption);
        options.addOption(endOption);
        options.addOption(log4jFileOption);
        options.addOption(actionOption);
        options.addOption(indexerLocationOption);
    }

    private String[] entityIds;
    private String[] tableNames;
    private String unloadDirectory = "/sresearch/zunloads/databases/production";
    private String beginDate;
    private String endDate = "2012";
    private Action action;
    private boolean useIndexFile = true;
    private static String indexDirectory;

    public InspectDataHistory(String entityIds, String tableNames, String unloadDirectory) {
        this.entityIds = entityIds.split(",");
        this.tableNames = tableNames.split(",");
        if (unloadDirectory != null)
            this.unloadDirectory = unloadDirectory;
        LOG.info("Checking Directory: " + unloadDirectory);
        BooleanQuery.setMaxClauseCount(20000);
    }

    public InspectDataHistory(String entityIds, String tableNames) {
        this(entityIds, tableNames, null);
    }

    public static void main(String[] arguments) {
        LOG = Logger.getLogger("");
        LOG.setLevel(Level.INFO);
        LOG.info("Start Data History Inspector: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        String optionValue = commandLine.getOptionValue(log4jFileOption.getOpt());
        if (optionValue == null)
            optionValue = "test/log4j.xml";
        initializeLogger(optionValue);
        String entityIDs = commandLine.getOptionValue(entityIdOption.getOpt());
        String tableNames = commandLine.getOptionValue(tableNameOption.getOpt());
        String unloadDir = commandLine.getOptionValue(unloadDirectoryOption.getOpt());
        String actionString = commandLine.getOptionValue(actionOption.getOpt());
        Action action = Action.getAction(actionString);
        indexDirectory = commandLine.getOptionValue(indexerLocationOption .getOpt());
        LOG.info("Checking: " + entityIDs + " in tables: " + tableNames.toUpperCase());

        InspectDataHistory inspector = new InspectDataHistory(entityIDs, tableNames, unloadDir);
        inspector.run(action);

    }

    private void run(Action action) {
        CronJobReport report = new CronJobReport("Check Entity Occurrence: ", cronJobUtil);
        report.start();
        checkUnloadDirectory();
        switch (action) {
            case TRACE_ENTITY:
                firstOccurrence(entityIds[0]);
                break;
            case FIND_DELETED_ENTITIES:
                findRemovedEntities();
                break;
        }
        report.finish();
        createReport();
        LOG.info(report.getDuration());
    }

    private void findRemovedEntities() {
        String entityId = entityIds[0];
        String tableName = tableNames[0];
        LOG.info("Checking for Deleted entities <" + entityId + "> in table " + tableName);
        TreeMap<String, List<String>> entityMap = new TreeMap<String, List<String>>();

        // loop over all unload directories
        for (File file : unloadFiles) {
            //LOG.info("Latest Unload Directory: " + file.getAbsolutePath());
            // extract entity ids
            List<String> allEntityIds = getAllEntityIds(entityId, tableName, file);
            if (allEntityIds == null) {
                LOG.info("Null");
                continue;
            }
            String date = file.getName();
            entityMap.put(date, allEntityIds);
            LOG.info(date + ": " + allEntityIds.size() + " entities  ");
        }

        List<EntityChange> deletedEntities = getDeletedEntities(entityMap);
    }

    private List<EntityChange> getDeletedEntities(TreeMap<String, List<String>> map) {
        List<EntityChange> deletedEntityList = new ArrayList<EntityChange>();
        Map<String, EntityChange> changeMap = new TreeMap<String, EntityChange>();
        String firstDate = map.firstKey();
        List<String> previousEntityIdList = map.get(firstDate);
        for (String date : map.keySet()) {
            List<String> entityIdList = map.get(date);
            List<String> deletedEntities = getDeletedEntities(previousEntityIdList, entityIdList);
            List<String> addedEntities = getAddedEntities(previousEntityIdList, entityIdList);
            if (CollectionUtils.isNotEmpty(deletedEntities) || CollectionUtils.isNotEmpty(addedEntities)) {
                EntityChange changedEntities = new EntityChange(deletedEntities, addedEntities, date);
                changeMap.put(date, changedEntities);
            }
            previousEntityIdList = entityIdList;
        }
        for (String date : changeMap.keySet())
            LOG.info(NEWLINE + date + ": " + changeMap.get(date));
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private List<String> getDeletedEntities(List<String> previousEntityIdList, List<String> entityIdList) {
        List<String> deletedIds = new ArrayList<String>();
        for (String previousEntityId : previousEntityIdList) {
            if (!entityIdList.contains(previousEntityId))
                deletedIds.add(previousEntityId);
        }
        return deletedIds;
    }

    private List<String> getAddedEntities(List<String> previousEntityIdList, List<String> entityIdList) {
        List<String> addedEntities = new ArrayList<String>();
        for (String entityId : entityIdList) {
            if (!previousEntityIdList.contains(entityId))
                addedEntities.add(entityId);
        }
        return addedEntities;
    }

    private List<String> getAllEntityIds(String entityId, String tableName, File unloadDirectory) {
        List<String> idList = null;
        File tableFile = getTableDirectory(tableName, unloadDirectory);
        LOG.debug(tableFile.getAbsolutePath());
        Grep grep = null;
        try {
            grep = new Grep(entityId, tableFile);
        } catch (IOException e) {
            // ignore.
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        if (grep.foundMatch()) {
            List<String> matchedLines = grep.getLinesMatched();
            idList = new ArrayList<String>(matchedLines.size());
            for (String concatenatedEntity : matchedLines)
                idList.add(getEntityIdFromFullRow(entityId, concatenatedEntity));
        }
        return idList;
    }

    protected static String getEntityIdFromFullRow(String entityId, String concatenatedEntity) {
        int startIndex = concatenatedEntity.indexOf(entityId);
        // no entity found in line (happens on bad unloads)
        if (startIndex == -1) {
            return null;
        }
        String truncatedString = concatenatedEntity.substring(startIndex);
        int indexOfDelimiter = truncatedString.indexOf("|");
        // if no pipe delimiter found at the end of the zdb id then this field is not the primary key (e.g. it could be used in hyperlinks)
        if(indexOfDelimiter == -1)
            return null;
        return truncatedString.substring(0, indexOfDelimiter);
    }

    private File getTableDirectory(String tableName, File unloadDirectory) {
        return new File(unloadDirectory, tableName);
    }

    private void createReport() {
        Table output = new Table(4);
        output.addCell("Occurrence");
        output.addCell("Date");
        output.addCell("Table");
        output.addCell("Match");

        int index = -1;
        for (Match match : occurrences) {
            index++;
            if (index % 2 == 0) {
                output.addCell("First");
            } else {
                output.addCell("Last");
            }
            output.addCell(match.getDate());
            output.addCell(match.getTableName());
            if (match.getMatchedLine().length() > 60)
                output.addCell(match.getMatchedLine().substring(0, 60) + "...");
            else
                output.addCell(match.getMatchedLine());
        }
        LOG.info("Report:\r" + output.render());
    }

    private List<Match> occurrences = new ArrayList<Match>(4);

    private void firstOccurrence(String entityId) {

        if (useIndexFile) {
            getEntityTraceFromIndexFile(entityId);
            return;
        }
    }

    private static Analyzer analyzer = new KeywordAnalyzer();


    private void getEntityTraceFromIndexFile(String entityId) {
        getIndexSummary();
        try {
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);
            //Query query = SiteSearchService.parseQuery(entityId, "row", analyzer);
            Query query = new TermQuery(new Term("row", entityId));

            // reformulate query into the fieldName-specific query, and rewrite the original query
            LOG.info("STill needs to be implemented!!!!!!");
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

    }

    private void createEntityTraceReport(Hits hits, String tableName) {
        boolean firstOccurrenceFound = false;
        Match lastMatch = null;
        for (String date : dateTableMap.keySet()) {
            if (foundDateHit(date, hits)) {
                if (!firstOccurrenceFound) {
                    Match match = new Match("", tableName, 0, date);
                    occurrences.add(match);
                    firstOccurrenceFound = true;
                } else {
                    lastMatch = new Match("", tableName, 0, date);
                }
            } else {
                if (firstOccurrenceFound) {
                    occurrences.add(lastMatch);
                    firstOccurrenceFound = false;
                }
            }
        }

    }

    private boolean foundDateHit(String date, Hits hits) {
        for (int index = 0; index < hits.length(); index++) {
            try {
                if (hits.doc(index).get("date").equals(date))
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Map<String, Map<String, Integer>> dateTableMap;

    private void getIndexSummary() {
        try {
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);
            Query query = SiteSearchService.parseQuery("20", "unloadDate", analyzer);
            Hits hits = searcher.search(query);
            LOG.debug(hits.length() + " unload directories found.");
            groupDatesAndTableNames(hits);
            populateNumbersOfRecords(searcher, reader);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    private void populateNumbersOfRecords(Searcher searcher, IndexReader indexReader) {
        for (String date : dateTableMap.keySet()) {
            Map<String, Integer> tableNames = dateTableMap.get(date);
            for (String tableName : tableNames.keySet()) {
                try {
                    Query query = SiteSearchService.parseQuery(date, "date", analyzer);
                    Query fullQuery = addCategoryPrefixToQuery("tableName", tableName, query);
                    //query = query.rewrite(indexReader);

                    Hits hits = searcher.search(fullQuery);
                    dateTableMap.get(date).put(tableName, hits.length());
                    LOG.debug(hits.length() + " unload directories found.");
                } catch (Exception e) {
                    LOG.error(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void groupDatesAndTableNames(Hits hits) throws IOException {
        dateTableMap = new TreeMap<String, Map<String, Integer>>();
        for (int index = 0; index < hits.length(); index++) {
            String date = hits.doc(index).get("unloadDate");
            String tableName = hits.doc(index).get("unloadTableName");
            Map<String, Integer> existingDate = dateTableMap.get(date);
            if (existingDate == null) {
                Map<String, Integer> tableNameSet = new TreeMap<String, Integer>();
                dateTableMap.put(date, tableNameSet);
                tableNameSet.put(tableName, 0);
            } else {
                existingDate.put(tableName, 0);
            }
            LOG.debug(hits.doc(index));
        }
    }

    public static Query addCategoryPrefixToQuery(String fieldName, String fieldValue, Query query) {

        BooleanQuery prefixQuery = new BooleanQuery();
        if (analyzer == null) {
            throw new RuntimeException("Analyzer is null");
        }
        if (query == null) {
            throw new RuntimeException("query is null");
        }

        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(fieldValue));

        if (tokenStream == null)
            throw new RuntimeException("tokenStream is null");
        Token token;
        try {
            token = tokenStream.next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (token != null) {
            TermQuery termQuery = new TermQuery(new Term(fieldName, new String(token.termBuffer(), 0, token.termLength())));
            prefixQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        BooleanQuery fullQuery = new BooleanQuery();
        fullQuery.add(prefixQuery, BooleanClause.Occur.MUST);
        fullQuery.add(query, BooleanClause.Occur.MUST);

        return fullQuery;
    }


    private List<File> getSearchFiles(File file) {
        List<File> files = new ArrayList<File>();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                File tableFile = getTableDirectory(tableName, file);
                if (!tableFile.exists())
                    LOG.info("Table " + tableName + " not found in unload directory " + file.getName());
                else
                    files.add(tableFile);
            }
        }
        return files;
    }

    private List<File> unloadFiles;

    private void checkUnloadDirectory() {
        File unloadDir = new File(unloadDirectory);
        if (!unloadDir.exists())
            throw new RuntimeException("Unload directory not found: " + unloadDir.getAbsolutePath());
        LOG.info("Found " + unloadDir.list().length + " backups.");
        File[] files = unloadDir.listFiles();
        unloadFiles = Arrays.asList(files);
        Collections.sort(unloadFiles);


        List<File> cleanedUpFiles = new ArrayList<File>();
        for (File file : unloadFiles) {
            if (file.getName().startsWith("20"))
                cleanedUpFiles.add(file);
        }
        unloadFiles = cleanedUpFiles;
        createLoadFilesOverview();

    }

    private void createLoadFilesOverview() {
        Table search = new Table(2);
        search.addCell("Entity");
        search.addCell("Table Names");
        int ind = 0;
        for (String table : tableNames) {
            if (ind == 0) {
                search.addCell(entityIds[0]);
                search.addCell(table);
            } else {
                search.addCell("");
                search.addCell(table);
            }
            ind++;
        }
        LOG.info(NEWLINE + search.render());

        Table output = new Table(2);
        output.addCell("Unload Directory Name");
        output.addCell("Number of Tables");
        int index = 0;
        for (File file : unloadFiles) {
            if (index < 3 || index > unloadFiles.size() - 4) {
                output.addCell(file.getName());
                output.addCell("" + file.list().length);
            }
            if (index == 3) {
                output.addCell("...");
                output.addCell("...");
            }
            index++;
        }
        LOG.debug(NEWLINE + output.render());
        output = new Table(2);
        output.addCell("Oldest Unload");
        output.addCell("Youngest Unload");
        output.addCell(unloadFiles.get(0).getName());
        output.addCell(unloadFiles.get(unloadFiles.size() - 1).getName());
        LOG.info(NEWLINE + output.render());
    }

}

class EntityChange {
    private List<String> deletedEntityIDs;
    private List<String> addedEntityIDs;
    private String date;

    EntityChange(List<String> deletedEntityIDs, List<String> addedEntityIDs, String date) {
        this.deletedEntityIDs = deletedEntityIDs;
        this.addedEntityIDs = addedEntityIDs;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public List<String> getDeletedEntityIDs() {
        return deletedEntityIDs;
    }

    public List<String> getAddedEntityIDs() {
        return addedEntityIDs;
    }

    @Override
    public String toString() {
        return "EntityChange{" +
                "deletedEntityIDs=" + deletedEntityIDs +
                ", addedEntityIDs=" + addedEntityIDs +
                '}';
    }
}

class Match {

    private String matchedLine;
    private String tableName;
    private int lineNumber;
    private String date;

    Match(String matchedLine, String tableName, int lineNumber, String date) {
        this.matchedLine = matchedLine;
        this.tableName = tableName;
        this.lineNumber = lineNumber;
        this.date = date;
    }

    public String getMatchedLine() {
        return matchedLine;
    }

    public String getTableName() {
        return tableName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDate() {
        return date;
    }
}

enum Action {
    TRACE_ENTITY, FIND_DELETED_ENTITIES;

    public static Action getAction(String name) {
        for (Action action : values())
            if (action.toString().equals(name))
                return action;
        throw new RuntimeException("NO action found with name " + name);
    }
}
