package org.zfin.util.database;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.zfin.database.presentation.Table;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.zfin.util.database.IndexerFields.*;

/**
 * Service to support querying the Lucene index for the unload files.
 */
@Service
public class UnloadService {

    private String indexDirectory;
    private Analyzer analyzer = new KeywordAnalyzer();
    //<date, <tableName, # of entries>>
    private TreeMap<String, Map<String, Integer>> dateTableMap;
    private List<TableSummary> tableSummaryList = new ArrayList<TableSummary>();

    private static Logger LOG = LogManager.getLogger(UnloadService.class);

    // Used from Controller
    public UnloadService() {
        this(ZfinPropertiesEnum.DATABASE_UNLOAD_DIRECTORY.value()+"/index");
    }

    public UnloadService(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        initSummary(indexDirectory);
    }

    private LuceneQueryService luceneQueryService;

    @DependsOn(value = {"luceneQueryService"})
    protected void initSummary(String indexDirectory) {
        try {
            groupDatesAndTableNames();
            createTableSummary();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void createTableSummary() {
        tableSummaryList = new ArrayList<TableSummary>();
        List<String> allTables = getAllIndexedTables();
        for (String tableName : allTables) {
            TableSummary tableSummary = new TableSummary(tableName);
            // retrieve first date of appearance
            for (String date : dateTableMap.keySet()) {
                if (dateTableMap.get(date).containsKey(tableName)) {
                    tableSummary.setDateFirstAppearance(date);
                    break;
                }
            }
            // retrieve last date of appearance
            for (String date : dateTableMap.descendingKeySet()) {
                if (dateTableMap.get(date).containsKey(tableName)) {
                    tableSummary.setDateLastAppearance(date);
                    break;
                }
            }
            tableSummaryList.add(tableSummary);
        }
        Collections.sort(tableSummaryList);
    }

    public void groupDatesAndTableNames() {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(SUMMARY_DATE, "20");
        if (luceneQueryService == null)
            luceneQueryService = new LuceneQueryService(indexDirectory);
        else
            luceneQueryService.reopenIndex();
        Hits hits = luceneQueryService.getHitsFromStartsWith(queryProperties);
        if (hits == null)
            return;

        LOG.debug(hits.length() + " unload directories found.");
        dateTableMap = new TreeMap<String, Map<String, Integer>>();
        try {
            for (int index = 0; index < hits.length(); index++) {
                String date = hits.doc(index).get(SUMMARY_DATE);
                String tableName = hits.doc(index).get(SUMMARY_TABLE_NAME);
                String numberOfRecordString = hits.doc(index).get(SUMMARY_NUMBER_OF_RECORDS);
                Integer numberOfRecords = 0;
                if (StringUtils.isNotEmpty(numberOfRecordString))
                    numberOfRecords = Integer.valueOf(numberOfRecordString);
                Map<String, Integer> existingDate = dateTableMap.get(date);
                if (existingDate == null) {
                    Map<String, Integer> tableNameSet = new TreeMap<String, Integer>();
                    dateTableMap.put(date, tableNameSet);
                    tableNameSet.put(tableName, numberOfRecords);
                } else {
                    existingDate.put(tableName, numberOfRecords);
                }
                LOG.debug(hits.doc(index));
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public TreeMap<String, Map<String, Integer>> getAllTablesIndexed() {
        return dateTableMap;
    }

    public List<TableHistogram> getTableHistogram(String tableName) {
        List<TableHistogram> histogram = new ArrayList<TableHistogram>();
        int previousNumber = 0;
        for (String date : dateTableMap.keySet()) {
            if (dateTableMap.get(date).containsKey(tableName)) {
                int numberOfRecords = dateTableMap.get(date).get(tableName);
                if (numberOfRecords != previousNumber) {
                    TableHistogram histoItem = new TableHistogram(tableName);
                    histoItem.setDate(date);
                    histoItem.setNumberOfRecords(numberOfRecords);
                    histoItem.setDelta(numberOfRecords - previousNumber);
                    histogram.add(histoItem);
                    previousNumber = numberOfRecords;
                }
            }
        }
        return histogram;
    }


    public List<TableSummary> getTableSummaryList() {
        return tableSummaryList;
    }

    private List<String> allIndexedTables;

    public String getStartDate() {
        return dateTableMap.firstKey();
    }

    public String getEndDate() {
        return dateTableMap.lastKey();
    }


    public List<String> getAllIndexedTables() {
        if (allIndexedTables != null)
            return allIndexedTables;

        Set<String> tableNames = new HashSet<String>();
        for (Map tableMap : dateTableMap.values()) {
            tableNames.addAll(tableMap.keySet());
        }
        allIndexedTables = new ArrayList<String>(tableNames);
        return allIndexedTables;
    }


    public Map<String, List<EntityMatch>> getDeletedEntityMap(String tableName) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_IS_DELETED, "true");
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public Map<String, List<EntityMatch>> getDeletedEntityMap(String tableName, String entityID) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_IS_DELETED, "true");
        queryProperties.put(ENTITY_ID, entityID);
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public Map<String, List<EntityMatch>> getAddedEntityMap(String tableName) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_IS_NEW, "true");
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public Map<String, List<EntityMatch>> getAddedEntityMap(String tableName, String entityID) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_IS_NEW, "true");
        queryProperties.put(ENTITY_ID, entityID);
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public int getNumberOfDocuments() {
        return luceneQueryService.getNumberOfDocuments();
    }


    // <lastDateBeforeDisappearance, List<Match>>
    private TreeMap<String, List<EntityMatch>> createEntityTraceReport(Hits hits, String tableName) {
        TreeMap<String, List<EntityMatch>> datedMap = new TreeMap<String, List<EntityMatch>>();

        for (int index = 0; index < hits.length(); index++) {
            try {
                Document document = hits.doc(index);
                String date = document.getField(ENTITY_DATE).stringValue();
                if (document.getField(ENTITY_ID) == null)
                    continue;
                String entityId = document.getField(ENTITY_ID).stringValue();
                tableName = document.getField(ENTITY_TABLE_NAME).stringValue();
                EntityMatch entityMatch = new EntityMatch(tableName, date, entityId);
                List<EntityMatch> occurrences = datedMap.get(date);
                if (occurrences == null) {
                    occurrences = new ArrayList<EntityMatch>();
                    datedMap.put(date, occurrences);
                }
                occurrences.add(entityMatch);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return datedMap;
    }

    public String getPreviousDate(String comparisonDate) {
        String previousDate = null;
        for (String date : dateTableMap.keySet()) {
            if (previousDate != null && date.equals(comparisonDate))
                return previousDate;
            previousDate = date;
        }
        return null;
    }

    public String getFollowingDate(String comparisonDate) {
        boolean foundDate = false;
        for (String date : dateTableMap.keySet()) {
            if (foundDate)
                return date;
            if (date.equals(comparisonDate))
                foundDate = true;
        }
        return null;
    }


    private boolean foundDateHit(String date, Hits hits) {
        for (int index = 0; index < hits.length(); index++) {
            try {
                if (hits.doc(index).get(ENTITY_DATE).equals(date))
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    public File getIndexerFile() {
        return new File(indexDirectory);
    }

    // for developmental purposes only
    public void setIndexDirectory(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        initSummary(indexDirectory);
    }


    public List<EntityTrace> getEntityHistory(String entityID, String tableName) {
        Map<String, List<EntityMatch>> newRecords = getAddedEntityMap(tableName, entityID);
        Map<String, List<EntityMatch>> modifiedRecords = getModifiedEntityMap(tableName, entityID);
        Map<String, List<EntityMatch>> deletedRecords = getDeletedEntityMap(tableName, entityID);
        List<EntityTrace> entityTraceList = getSortedHistoryTrace(newRecords, modifiedRecords, deletedRecords);
        return entityTraceList;
    }

    private List<EntityTrace> getSortedHistoryTrace(Map<String, List<EntityMatch>> newRecords, Map<String, List<EntityMatch>> modifiedRecords, Map<String, List<EntityMatch>> deletedRecords) {
        List<EntityTrace> entityTraceList = new ArrayList<EntityTrace>();
        addRecordsToTraceList(newRecords, entityTraceList, EntityAction.ADD);
        addRecordsToTraceList(modifiedRecords, entityTraceList, EntityAction.MODIFY);
        addRecordsToTraceList(deletedRecords, entityTraceList, EntityAction.DELETE);
        Collections.sort(entityTraceList);
        return entityTraceList;
    }

    private void addRecordsToTraceList(Map<String, List<EntityMatch>> newRecords, List<EntityTrace> entityTraceList, EntityAction action) {
        for (String date : newRecords.keySet()) {
            List<EntityMatch> matches = newRecords.get(date);
            if (matches.size() > 1)
                throw new RuntimeException("Found more than one addition record on date " + date + " for entity ");
            EntityTrace trace = new EntityTrace(date, action);
            entityTraceList.add(trace);
        }
    }

    public Map<String, List<EntityMatch>> getModifiedEntityMap(String tableName) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_HAS_CHANGED, "true");
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public Map<String, List<EntityMatch>> getModifiedEntityMap(String tableName, String entityID) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_TABLE_NAME, tableName);
        queryProperties.put(ENTITY_HAS_CHANGED, "true");
        queryProperties.put(ENTITY_ID, entityID);
        return getEntityMapFromQueryParameters(queryProperties);
    }

    private TreeMap<String, List<EntityMatch>> getEntityMapFromQueryParameters(Map<String, String> properties) {
        if (properties.isEmpty())
            return null;

        Hits hits = luceneQueryService.getHitsFromAndQuery(properties);
        return createEntityTraceReport(hits, null);
    }

    public String getLatestIndexedDate(Table table) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(SUMMARY_TABLE_NAME, table.getTableName().toLowerCase());

        Hits hits = luceneQueryService.getHitsFromAndQuery(queryProperties);
        int totalHits = hits.length();
        if (totalHits == 0)
            return null;

        List<String> dates = new ArrayList<String>(totalHits);
        for (int index = 0; index < totalHits; index++) {
            try {
                dates.add(hits.doc(index).get(SUMMARY_DATE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(dates);
        return dates.get(totalHits - 1);
    }

    public void reLoadIndex() throws IOException {
        luceneQueryService.reLoadIndex();
    }

    public List<String> getAllUnloadedDates() {
        List<String> dates = new ArrayList<String>(dateTableMap.size());
        for (String date : dateTableMap.keySet())
            dates.add(date);
        return dates;
    }

    public List<EntityTrace> getDateHistory(String date) {
        List<EntityTrace> entityTraceList = new ArrayList<EntityTrace>();

        addEntitiesToHistory(entityTraceList, date, EntityAction.MODIFY);
        addEntitiesToHistory(entityTraceList, date, EntityAction.ADD);
        addEntitiesToHistory(entityTraceList, date, EntityAction.DELETE);
        return entityTraceList;
    }

    private void addEntitiesToHistory(List<EntityTrace> entityTraceList, String date, EntityAction action) {
        Map<String, String> queryProperties = new HashMap<String, String>(2);
        queryProperties.put(ENTITY_DATE, date);
        switch (action) {
            case ADD:
                queryProperties.put(ENTITY_IS_NEW, "true");
                break;
            case MODIFY:
                queryProperties.put(ENTITY_HAS_CHANGED, "true");
                break;
            case DELETE:
                queryProperties.put(ENTITY_IS_DELETED, "true");
                break;
        }

        Map<String, List<EntityMatch>> records = getEntityMapFromQueryParameters(queryProperties);
        if (records.keySet().size() > 1)
            throw new RuntimeException("More than one date found:");
        if (records.size() == 0)
            return;
        String dateMatch = records.keySet().iterator().next();
        List<EntityMatch> matches = records.get(dateMatch);
        for (EntityMatch match : matches) {
            EntityTrace trace = new EntityTrace(date, match.entityId, action, match.getTableName());
            entityTraceList.add(trace);
        }
    }


    private Map<String, List<EntityMatch>> getAllEntitiesHistory(String date) {
        Map<String, String> queryProperties = new HashMap<String, String>(3);
        queryProperties.put(ENTITY_DATE, date);
        return getEntityMapFromQueryParameters(queryProperties);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * Re-load all tables and dates in the indexer, e.g. after running the indexer again.
     */
    public void refreshDataMap() {
        groupDatesAndTableNames();
        createTableSummary();

    }

    /**
     * Retrieve all table names that are not indexed yet (zdb-id tables).
     *
     * @return list of table names that are not yet indexed.
     */
    public List<String> getUnIndexedTables() {
        List<Table> tables = Table.getAllTablesWithZdbPk();
        List<String> tableNames = new ArrayList<String>();

        // remove indexed tables from list
        for (TableSummary summary : tableSummaryList) {
            Table existingTable = Table.getEntityTableByTableName(summary.getTableName());
            if (tables.contains(existingTable))
                tables.remove(existingTable);
        }

        for (Table table : tables)
            tableNames.add(table.getTableName());
        return tableNames;
    }

    public boolean isTableIndexed(Table table) {
        return getLatestIndexedDate(table) != null;
    }

    public class EntityTrace implements Comparable<EntityTrace> {
        private String date;
        private String entityId;
        private String tableName;
        private EntityAction action;

        public EntityTrace(String date, String entityId, EntityAction action, String tableName) {
            this.date = date;
            this.entityId = entityId;
            this.action = action;
            this.tableName = tableName;
        }

        public EntityTrace(String date, EntityAction action) {
            this.date = date;
            this.action = action;
        }

        public String getDate() {
            return date;
        }

        public EntityAction getAction() {
            return action;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getTableName() {
            return tableName;
        }

        @Override
        public int compareTo(EntityTrace entityTrace) {
            return date.compareTo(entityTrace.getDate());
        }
    }

    public enum EntityAction {
        ADD, MODIFY, DELETE
    }

    public class EntityMatch {

        private String tableName;
        private String lastDateBeforeDisappearance;
        private String firstDateAfterDisappearance;
        private String entityId;

        public EntityMatch(String tableName, String date, String entityId) {
            this.tableName = tableName;
            this.lastDateBeforeDisappearance = date;
            this.entityId = entityId;
            this.firstDateAfterDisappearance = getFollowingDate(lastDateBeforeDisappearance);
        }

        public String getTableName() {
            return tableName;
        }

        public String getLastDateBeforeDisappearance() {
            return lastDateBeforeDisappearance;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getFirstDateAfterDisappearance() {
            return firstDateAfterDisappearance;
        }
    }

    public class TableSummary implements Comparable<TableSummary> {
        private String tableName;
        private String dateFirstAppearance;
        private String dateLastAppearance;

        public TableSummary(String tableName) {
            this.tableName = tableName;
        }

        public void setDateFirstAppearance(String dateFirstAppearance) {
            this.dateFirstAppearance = dateFirstAppearance;
        }

        public void setDateLastAppearance(String dateLastAppearance) {
            this.dateLastAppearance = dateLastAppearance;
        }

        public String getTableName() {
            return tableName;
        }

        public String getDateFirstAppearance() {
            return dateFirstAppearance;
        }

        public String getDateLastAppearance() {
            return dateLastAppearance;
        }

        @Override
        public int compareTo(TableSummary o) {
            return tableName.compareTo(o.getTableName());
        }
    }

    public class TableHistogram implements Comparable<TableHistogram> {
        private String date;
        private String tableName;
        private int numberOfRecords;
        private int delta;

        public TableHistogram(String tableName) {
            this.tableName = tableName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getNumberOfRecords() {
            return numberOfRecords;
        }

        public void setNumberOfRecords(int numberOfRecords) {
            this.numberOfRecords = numberOfRecords;
        }

        public int getDelta() {
            return delta;
        }

        public void setDelta(int delta) {
            this.delta = delta;
        }

        @Override
        public int compareTo(TableHistogram o) {
            return date.compareTo(o.getDate());
        }
    }

}



