package org.zfin.database;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.*;
import org.zfin.database.presentation.*;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DbScriptFileParser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.util.SqlQueryKeywords.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class DatabaseService {


    public static final String DELIMITER = " || ";

    /**
     * If the entity name is provided by one or more FK keys concatenate the entity names of those tables.
     *
     * @param ID    entity primary key value
     * @param table entity table
     * @return entity name for display
     */
    public static String getEntityName(Object ID, Table table) {
        String[] entityKeys = table.getEntityNameColumn().split(",");
        StringBuilder entityNameBuilder = new StringBuilder();
        for (String entityKey : entityKeys) {
            ForeignKey foreignKey = ForeignKey.getForeignKeyByColumnName(entityKey);
            if (foreignKey != null) {
                Table foreignKeyTable = foreignKey.getEntityTable();
                if (foreignKeyTable != table) {
                    String entityValue = getSingleColumnValue(foreignKey.getForeignKey(), (String) ID, table);
                    entityNameBuilder.append(getEntityName(entityValue, foreignKeyTable));
                    // add delimiter
                    entityNameBuilder.append(DELIMITER);
                }
            } else {
                String entityValue = getEntityColumnValue(ID, table);
                if (StringUtils.isEmpty(entityValue)) {
                    continue;
                }
                entityNameBuilder.append(entityValue);
                // add delimiter
                entityNameBuilder.append(DELIMITER);
            }
        }
        if (entityNameBuilder.indexOf(DELIMITER) > -1) {
            entityNameBuilder.delete(entityNameBuilder.length() - DELIMITER.length(), entityNameBuilder.length());
        }
        return entityNameBuilder.toString();
    }

    private static String getEntityColumnValue(Object ID, Table table) {
        DatabaseJdbcStatement statement = createJdbcStatement(ID, table);
        List<List<String>> list = getInfrastructureRepository().executeNativeQuery(statement);
        if (list == null) {
            return null;
        }
        return list.get(0).get(0);
    }

    /**
     * Create SELECT statement for a given table and primary key value (ID)
     *
     * @param ID    PK value
     * @param table Table
     * @return statement
     */
    public static DatabaseJdbcStatement createJdbcStatement(Object ID, Table table) {
        String entityNameColumn = table.getEntityNameColumn();
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select ");
        queryBuilder.append(entityNameColumn);
        queryBuilder.append(" from ");
        queryBuilder.append(table.getTableName());
        queryBuilder.append(" where ");
        queryBuilder.append(table.getPkName());
        queryBuilder.append(" = ");
        if (ID instanceof String) {
            queryBuilder.append("'");
        }
        queryBuilder.append(ID);
        if (ID instanceof String) {
            queryBuilder.append("'");
        }
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    /**
     * Retrieve column value for a given key=value pair on given table.
     *
     * @param table Table
     * @return statement
     */
    public static String getSingleColumnValue(String columnName, String pkValue, Table table) {
        QueryBuilder queryBuilder = new QueryBuilder(table);
        queryBuilder.addSelectColumn(columnName);
        queryBuilder.addTable(table);
        queryBuilder.addWhereClause(table.getPkName(), pkValue);
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(queryBuilder.toString());
        List<List<String>> list = getInfrastructureRepository().executeNativeQuery(statement);
        if (list == null) {
            return null;
        }
        return list.get(0).get(0);
    }

    /**
     * Retrieve all records for a given table and given column name.
     *
     * @param table      Table
     * @param columnName column name
     * @return Jdbc Statement
     */
    public static DatabaseJdbcStatement createJdbcStatement(Table table, String columnName) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(SELECT + " " + columnName + " " + FROM + " " + table.getTableName());
        return statement;
    }

    /**
     * Create query for foreign key
     *
     * @param lookup      Contains info on original Table lookup
     * @param foreignKeys foreign keys that connect the lookup table
     * @param count       query to count number of records
     * @return query
     */
    public static DatabaseJdbcStatement createJoinJdbcStatement(TableValueLookup lookup, List<ForeignKey> foreignKeys, boolean count) {
        return createJoinJdbcStatement(lookup, foreignKeys, null);
    }

    /**
     * Ensure that only the entity table columns are retrieved in the query if it is not a count(*) query.
     *
     * @param lookup      Lookup value
     * @param foreignKeys list of foreign Keys
     * @param entityTable Entity Table
     * @return DatabaseJdbcStatement
     */
    public static DatabaseJdbcStatement createJoinJdbcStatement(TableValueLookup lookup, List<ForeignKey> foreignKeys, Table entityTable) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        QueryBuilder queryBuilder = new QueryBuilder(entityTable);
        Table rootTable = lookup.getTable();
        queryBuilder.addTable(rootTable);
        if (!CollectionUtils.isEmpty(lookup.getColumnValues())) {
            for (ColumnValue colValue : lookup.getColumnValues()) {
                queryBuilder.addPKWhereClause(colValue);
            }
        }
        if (foreignKeys != null) {
            for (ForeignKey foreignKey : foreignKeys) {
                Table foreignKeyTable = foreignKey.getForeignKeyTable();
                if (foreignKey.isManyToManyRelationship()) {
                    queryBuilder.addManyToManyJoinClause(rootTable, foreignKey);
                } else {
                    queryBuilder.addTableWhereJoinedClause(rootTable, foreignKey);
                    rootTable = foreignKey.getForeignKeyTable();
                }
            }
        }
        statement.addQueryPart(queryBuilder.getSqlQuery());
        return statement;
    }


    public static int getNumberOfRecords(Table table) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(SELECT + " " + COUNT_START + " " + FROM + " " + table.getTableName());
        List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
        return Integer.parseInt(result.get(0).get(0));
    }

    public static List<ForeignKeyResult> createFKResultList(Table rootTable, String id) {
        List<ForeignKeyResult> foreignKeyResultList = new ArrayList<>(4);
        List<Table> tables = new ArrayList<>(5);
        tables.add(rootTable);
        TableValueLookup lookup = new TableValueLookup(rootTable);
        ColumnValue columnValue = new ColumnValue(rootTable.getPkName(), id);
        lookup.addColumnValue(columnValue);
        List<ForeignKey> foreignKeyList = new ArrayList<>(5);

        for (Table table : tables) {
            List<ForeignKey> foreignKeys = ForeignKey.getForeignKeys(table);
            for (ForeignKey foreignKey : foreignKeys) {
                Table foreignKeyTable = foreignKey.getForeignKeyTable();
                foreignKeyList.add(foreignKey);
                DatabaseJdbcStatement statement = DatabaseService.createJoinJdbcStatement(lookup, foreignKeyList, true);
                List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
                ForeignKeyResult foreignKeyResult = new ForeignKeyResult(foreignKey, Integer.parseInt(result.get(0).get(0)));
                foreignKeyResultList.add(foreignKeyResult);
                // has more children
                if (foreignKeyTable.hasForeignKeys()) {
                    if (!foreignKey.isManyToManyRelationship()) {
                        addChildTableResults(foreignKeyTable, foreignKeyResult, lookup, foreignKeyList);
                    }
                }
                foreignKeyList.remove(foreignKeyList.size() - 1);
            }
        }
        Collections.sort(foreignKeyResultList, new ForeignKeyResultSort());
        return foreignKeyResultList;
    }

    private static void addChildTableResults(Table table, ForeignKeyResult parentFKResult, TableValueLookup lookup, List<ForeignKey> foreignKeyHierarchy) {
        List<ForeignKey> foreignKeys = ForeignKey.getForeignKeys(table);
        for (ForeignKey foreignKey : foreignKeys) {
            if (foreignKeyHierarchy.contains(foreignKey)) {
                continue;
            }
            foreignKeyHierarchy.add(foreignKey);
            DatabaseJdbcStatement statement = DatabaseService.createJoinJdbcStatement(lookup, foreignKeyHierarchy, true);
            List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
            ForeignKeyResult foreignKeyResult = new ForeignKeyResult(foreignKey, Integer.parseInt(result.get(0).get(0)));
            parentFKResult.add(foreignKeyResult);
            // has more children
            if (foreignKey.getForeignKeyTable().hasForeignKeys()) {
                if (!foreignKey.isManyToManyRelationship()) {
                    addChildTableResults(foreignKey.getForeignKeyTable(), foreignKeyResult, lookup, foreignKeyHierarchy);
                }
            }
            foreignKeyHierarchy.remove(foreignKeyHierarchy.size() - 1);
        }
    }

    /**
     * Retrieve Foreign Key results for given foreign key name and Id for parent table.
     *
     * @param foreignKeyName FK name
     * @param parentPkValue  value for parent table   PK
     * @return list of foreign key id
     */
    public static List<ForeignKeyResult> createFKResultList(String foreignKeyName, String parentPkValue) {
        if (foreignKeyName == null) {
            return null;
        }
        String foreignKeyNameList = ForeignKey.getForeignKeyHierarchyName(foreignKeyName);
        ForeignKey foreignKey = ForeignKey.getForeignKeyByColumnName(foreignKeyName.split(ForeignKey.DELIMITER)[0]);
        Table rootTable = foreignKey.getEntityTable();
        List<ForeignKeyResult> fullList = createFKResultList(rootTable, parentPkValue);
        ForeignKeyResult subTree = getSubTree(fullList, foreignKeyNameList);
        if (subTree == null) {
            return null;
        }
        return subTree.getChildren();
    }

    private static ForeignKeyResult getSubTree(List<ForeignKeyResult> fullList, String foreignKeyList) {
        for (ForeignKeyResult keyResult : fullList) {
            if (keyResult.hasChildNode(foreignKeyList)) {
                return keyResult.getChildKeyResult(foreignKeyList);
            }
        }
        return null;
    }

    public static DatabaseJdbcStatement createQueryFromFullForeignKeyHierarchy(String fullNodeName, String pkValue, Table entityTable, Table baseTable) {
        Table rootTable = ForeignKey.getRootTableFromNodeName(fullNodeName, baseTable);
        TableValueLookup lookup = new TableValueLookup(rootTable);
        ColumnValue columnValue = new ColumnValue(rootTable, pkValue);
        lookup.addColumnValue(columnValue);

        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeyHierarchy(fullNodeName);
        return createJoinJdbcStatement(lookup, foreignKeyList, entityTable);
    }

    public static DatabaseJdbcStatement createJdbcStatementAllSortedRecords(String tableName) {
        List<String> result = getInfrastructureRepository().retrieveMetaData(tableName);
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder builder = new StringBuilder();
        builder.append("Select * from ");
        builder.append(tableName);
        builder.append(" order by 1");
        int index = 1;
        for (String ignored : result) {
            if (index > 1) {
                builder.append(",");
                builder.append(index);
            }
            index++;
        }
        statement.addQueryPart(builder.toString());
        return statement;
    }

    public void setConsoleAppender() {
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%r %-5p: %m%n"));
        Logger.getRootLogger().addAppender(ca);
    }

    public Map<String, List<List<String>>> getResultMap() {
        return resultMap;
    }

    public static class ForeignKeyResultSort implements Comparator<ForeignKeyResult> {
        public int compare(ForeignKeyResult o1, ForeignKeyResult o2) {
            return o1.getForeignKey().getForeignKeyTable().getTableName().compareToIgnoreCase(o2.getForeignKey().getForeignKeyTable().getTableName());
        }
    }


    public List<String> runDbScriptFile(String dbScriptFile) {
        File file = new File(dbScriptFile);
        return runDbScriptFile(file, null);
    }

    public List<String> runDbScriptFile(File dbScriptFile) {
        return runDbScriptFile(dbScriptFile, null);
    }

    public List<String> runDbScriptFile(String dbScriptFile, Map<String, List<List<String>>> dataMap) {
        File file = new File(dbScriptFile);
        return runDbScriptFile(file, dataMap);
    }

    private List<List<List<String>>> listOfResultRecords = new ArrayList<>(5);
    private Map<String, List<List<String>>> resultMap = new HashMap<>();

    public List<List<List<String>>> getListOfResultRecords() {
        return listOfResultRecords;
    }

    public List<String> runDbScriptFile(File dbScriptFile, Map<String, List<List<String>>> dataMap) {
        return runDbScriptFile(dbScriptFile, dataMap, null);
    }

    private TraceFileService trace;

    public List<String> runDbScriptFile(File dbScriptFile, Map<String, List<List<String>>> dataMap, Map<String, String> variableMap) {
        trace = new TraceFileService("load");
        List<String> errorMessage = new ArrayList<>(2);
        if (!dbScriptFile.exists()) {
            String message = "Could not find script file " + dbScriptFile.getAbsolutePath();
            LOG.error(message);
            errorMessage.add(message);
            return errorMessage;
        }

        if (dataMap == null) {
            dataMap = new HashMap<>(5);
        }
        DbScriptFileParser parser = new DbScriptFileParser(dbScriptFile);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        if (!LOG.isDebugEnabled()) {
            LOG.info("No Debugging enabled: To see more debug data enable the logger to leg level debug.");
        }
        for (DatabaseJdbcStatement statement : queries) {
            statement.setDataMap(variableMap);
            LOG.info("Statement " + statement.getLocationInfo() + "\n" + statement.getHumanReadableQueryString());
            if (statement.isInformixWorkStatement()) {
                continue;
            }
            if (statement.isLoadStatement()) {
                if (statement.isSelectIntoStatement()) {
                    getInfrastructureRepository().executeJdbcStatement(statement);
                } else {
                    List<List<String>> data = dataMap.get(statement.getDataKey());
                    if (data == null) {
                        LOG.info("No data found for key: " + statement.getDataKey());
                        continue;
                    }
                    statement = statement.completeInsertStatement(data.get(0).size());
                    getInfrastructureRepository().executeJdbcStatement(statement, data);
                    LOG.info(data.size() + " records inserted");
                }
            } else if (statement.isDebug()) {
                List<List<String>> dataReturn;
                if (LOG.isDebugEnabled()) {
                    dataReturn = getInfrastructureRepository().executeNativeQuery(statement);
                    listOfResultRecords.add(dataReturn);
                    if (dataReturn == null) {
                        LOG.debug("  Debug data: No records found.");
                    } else {
                        LOG.debug("  Debug data:\n " + dataReturn.size() + " records.");
                        for (List<String> row : dataReturn) {
                            LOG.debug("\n  " + row);
                        }
                    }
                }
            } else if (statement.isUnloadStatement() || statement.isReadOnlyStatement()) {
                List<List<String>> dataReturn;
                dataReturn = getInfrastructureRepository().executeNativeDynamicQuery(statement);
                listOfResultRecords.add(dataReturn);
                if (statement.getDataKey() != null) {
                    resultMap.put(statement.getDataKey(), dataReturn);
                } else {
                    resultMap.put("_NO-KEY", dataReturn);
                }
                if (dataReturn == null) {
                    LOG.info("  Debug data: No records found.");
                } else if (statement.getDataKey() == null) {
                    LOG.info("\n" + dataReturn.size() + " records retrieved");
                } else if (statement.getDataKey().toUpperCase().equals(DatabaseJdbcStatement.DEBUG)) {
                    LOG.info("  Debug data:\n " + dataReturn.size() + " records.");
                    for (List<String> row : dataReturn) {
                        LOG.info("\n  " + row);
                    }
                } else {
                    dataMap.put(statement.getDataKey(), dataReturn);
                }
            } else if (statement.isEcho()) {
                LOG.info("\n  " + statement.getQuery());
            } else if (statement.isTest()) {
                LOG.info("\n  " + statement.getQuery());
                String key = statement.getDataKey();
                int value = Integer.valueOf(dataMap.get(key).get(0).get(0));
                if (statement.isTestTrue(value)) {
                    errorMessage.add(statement.getErrorMessage(value));
                }
            } else {
                if (statement.isInsertStatement() || statement.isDeleteStatement()) {
                    runDebugStatement(statement);
                }
                int affectedRows = getInfrastructureRepository().executeJdbcStatement(statement);
                if (statement.isDeleteStatement()) {
                    trace.writeToTraceFile("Deleted Rows: " + affectedRows);
                    trace.writeToTraceFile("After Delete Statement: ");
                    runDebugStatementAfterDelete(statement);
                    LOG.info("  Deleted data:\n " + affectedRows);
                }
            }
            DbSystemUtil.logLockInfo();
        }
        trace.closeTraceFile();
        return errorMessage;
    }

    public static String bindVariables(String query, List<String> resultRecord) {
        int index = 0;
        for (String value : resultRecord) {
            query = query.replace("$" + index, value);
            index++;
        }
        return query;
    }

    public void setLoggerLevelInfo() {
        LOG.setLevel(Level.INFO);
    }

    public void setLoggerFile(File file) {
        FileAppender appender;
        try {
            String logFilePattern = "%r %-5p %m%n";
            appender = new FileAppender(new PatternLayout(logFilePattern), file.getAbsolutePath());
            appender.setAppend(true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        LOG.getRootLogger().addAppender(appender);
    }

    private boolean debugMode = true;

    private void runDebugStatement(DatabaseJdbcStatement statement) {
        if (debugMode == false) {
            return;
        }
        try {
            DatabaseJdbcStatement debugStatement = statement.getDebugStatement();
            List<List<String>> dataReturn = getInfrastructureRepository().executeNativeQuery(debugStatement);
            trace.writeToTraceFile(debugStatement, dataReturn);
        } catch (Exception e) {
            LOG.error(e);
        }
    }


    private void runDebugStatementAfterDelete(DatabaseJdbcStatement statement) {
        if (debugMode == false) {
            return;
        }
        try {
            DatabaseJdbcStatement debugStatement = statement.getDebugDeleteStatement();
            List<List<String>> dataReturn = getInfrastructureRepository().executeNativeQuery(debugStatement);
            trace.writeToTraceFile(debugStatement, dataReturn, false);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private final Logger LOG = Logger.getLogger(DatabaseService.class);

}
