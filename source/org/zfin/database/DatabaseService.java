package org.zfin.database;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.database.presentation.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DbScriptFileParser;

import java.io.File;
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
                if (StringUtils.isEmpty(entityValue))
                    continue;
                entityNameBuilder.append(entityValue);
                // add delimiter
                entityNameBuilder.append(DELIMITER);
            }
        }
        if (entityNameBuilder.indexOf(DELIMITER) > -1)
            entityNameBuilder.delete(entityNameBuilder.length() - DELIMITER.length(), entityNameBuilder.length());
        return entityNameBuilder.toString();
    }

    private static String getEntityColumnValue(Object ID, Table table) {
        DatabaseJdbcStatement statement = createJdbcStatement(ID, table);
        List<List<String>> list = getInfrastructureRepository().executeNativeQuery(statement);
        if (list == null)
            return null;
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
        if (ID instanceof String)
            queryBuilder.append("'");
        queryBuilder.append(ID);
        if (ID instanceof String)
            queryBuilder.append("'");
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
        if (list == null)
            return null;
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
        StringBuilder builder = new StringBuilder(SELECT);
        builder.append(" ");
        builder.append(columnName);
        builder.append(" ");
        builder.append(FROM);
        builder.append(" ");
        builder.append(table.getTableName());
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(builder.toString());
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
     * @param lookup
     * @param foreignKeys
     * @param entityTable
     * @return
     */
    public static DatabaseJdbcStatement createJoinJdbcStatement(TableValueLookup lookup, List<ForeignKey> foreignKeys, Table entityTable) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        QueryBuilder queryBuilder = new QueryBuilder(entityTable);
        Table rootTable = lookup.getTable();
        queryBuilder.addTable(rootTable);
        if (!CollectionUtils.isEmpty(lookup.getColumnValues())) {
            for (ColumnValue colValue : lookup.getColumnValues())
                queryBuilder.addPKWhereClause(colValue);
        }
        if (foreignKeys != null) {
            String joinColumn = rootTable.getPkName();
            for (ForeignKey foreignKey : foreignKeys) {
                Table foreignKeyTable = foreignKey.getForeignKeyTable();
                if (foreignKey.isManyToManyRelationship()) {
                    queryBuilder.addManyToManyJoinClause(rootTable, foreignKey);
                } else {
                    queryBuilder.addTableWhereJoinedClause(rootTable, foreignKey);
                    joinColumn = foreignKeyTable.getPkName();
                    rootTable = foreignKey.getForeignKeyTable();
                }
            }
        }
        statement.addQueryPart(queryBuilder.getSqlQuery());
        return statement;
    }


    public static int getNumberOfRecords(Table table) {
        StringBuilder builder = new StringBuilder(SELECT);
        builder.append(" ");
        builder.append(COUNT_START);
        builder.append(" ");
        builder.append(FROM);
        builder.append(" ");
        builder.append(table.getTableName());
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(builder.toString());
        List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
        return Integer.parseInt(result.get(0).get(0));
    }

    public static List<ForeignKeyResult> createFKResultList(Table rootTable, String id) {
        List<ForeignKeyResult> foreignKeyResultList = new ArrayList<ForeignKeyResult>(4);
        List<Table> tables = new ArrayList<Table>(5);
        tables.add(rootTable);
        TableValueLookup lookup = new TableValueLookup(rootTable);
        ColumnValue columnValue = new ColumnValue(rootTable.getPkName(), id);
        lookup.addColumnValue(columnValue);
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>(5);

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
                    if (!foreignKey.isManyToManyRelationship())
                        addChildTableResults(foreignKeyTable, foreignKeyResult, lookup, foreignKeyList);
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
            if (foreignKeyHierarchy.contains(foreignKey))
                continue;
            foreignKeyHierarchy.add(foreignKey);
            DatabaseJdbcStatement statement = DatabaseService.createJoinJdbcStatement(lookup, foreignKeyHierarchy, true);
            List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
            ForeignKeyResult foreignKeyResult = new ForeignKeyResult(foreignKey, Integer.parseInt(result.get(0).get(0)));
            parentFKResult.add(foreignKeyResult);
            // has more children
            if (foreignKey.getForeignKeyTable().hasForeignKeys()) {
                if (!foreignKey.isManyToManyRelationship())
                    addChildTableResults(foreignKey.getForeignKeyTable(), foreignKeyResult, lookup, foreignKeyHierarchy);
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
        if (foreignKeyName == null)
            return null;
        String foreignKeyNameList = ForeignKey.getForeignKeyHierarchyName(foreignKeyName);
        ForeignKey foreignKey = ForeignKey.getForeignKeyByColumnName(foreignKeyName.split(ForeignKey.DELIMITER)[0]);
        Table rootTable = foreignKey.getEntityTable();
        List<ForeignKeyResult> fullList = createFKResultList(rootTable, parentPkValue);
        ForeignKeyResult subTree = getSubTree(fullList, foreignKeyNameList);
        if (subTree == null)
            return null;
        return subTree.getChildren();
    }

    private static ForeignKeyResult getSubTree(List<ForeignKeyResult> fullList, String foreignKeyList) {
        for (ForeignKeyResult keyResult : fullList) {
            if (keyResult.hasChildNode(foreignKeyList))
                return keyResult.getChildKeyResult(foreignKeyList);
        }
        return null;
    }

    public static DatabaseJdbcStatement createQueryFromFullForeignKeyHierarchy(String fullNodeName, String pkValue) {
        return createQueryFromFullForeignKeyHierarchy(fullNodeName, pkValue, null);
    }

    public static DatabaseJdbcStatement createQueryFromFullForeignKeyHierarchy(String fullNodeName, String pkValue, Table entityTable) {
        Table rootTable = ForeignKey.getRootTableFromNodeName(fullNodeName);
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
        for (String columnName : result) {
            if (index > 1) {
                builder.append(",");
                builder.append(index);
            }
            index++;
        }
        statement.addQueryPart(builder.toString());
        return statement;
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

    public List<String> runDbScriptFile(File dbScriptFile, Map<String, List<List<String>>> dataMap) {
        List<String> errorMessage = new ArrayList<String>(2);
        if (!dbScriptFile.exists()) {
            String message = "Could not find script file " + dbScriptFile.getAbsolutePath();
            LOG.error(message);
            errorMessage.add(message);
            return errorMessage;
        }

        if (dataMap == null)
            dataMap = new HashMap<String, List<List<String>>>(5);
        DbScriptFileParser parser = new DbScriptFileParser(dbScriptFile);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        InfrastructureRepository infrastructureRep = RepositoryFactory.getInfrastructureRepository();
        if (!LOG.isDebugEnabled())
            LOG.info("No Debugging enabled: To see more debug data enable the logger to leg level debug.");
        for (DatabaseJdbcStatement statement : queries) {
            LOG.info("Statement " + statement.getLocationInfo() + ": " + statement.getHumanReadableQueryString());
            if (statement.isInformixWorkStatement())
                continue;
            if (statement.isLoadStatement()) {
                List<List<String>> data = dataMap.get(statement.getDataKey());
                if (data == null) {
                    LOG.info("No data found for key: " + statement.getDataKey());
                    continue;
                }
                statement.updateInsertStatement(data.get(0).size());
                infrastructureRep.executeJdbcStatement(statement, data);
                LOG.info(data.size() + " records inserted");
            } else if (statement.isDebug()) {
                List<List<String>> dataReturn = null;
                if (LOG.isDebugEnabled()) {
                    dataReturn = infrastructureRep.executeNativeQuery(statement);
                    if (dataReturn == null)
                        LOG.debug("  Debug data: No records found.");
                    else {
                        LOG.debug("  Debug data: " + dataReturn.size() + " records.");
                        for (List<String> row : dataReturn)
                            LOG.debug("  " + row);
                    }
                }
            } else if (statement.isUnloadStatement() || statement.isReadOnlyStatement()) {
                List<List<String>> dataReturn = null;
                dataReturn = infrastructureRep.executeNativeQuery(statement);
                if (dataReturn == null) {
                    LOG.info("  Debug data: No records found.");
                } else if (statement.getDataKey() == null) {
                    LOG.info("  Data: " + dataReturn.size() + " records.");
                } else if (statement.getDataKey().toUpperCase().equals(DatabaseJdbcStatement.DEBUG)) {
                    LOG.info("  Debug data: " + dataReturn.size() + " records.");
                    for (List<String> row : dataReturn)
                        LOG.info("  " + row);
                } else
                    dataMap.put(statement.getDataKey(), dataReturn);
            } else if (statement.isEcho()) {
                LOG.info(statement.getQuery());
            } else if (statement.isTest()) {
                LOG.info(statement.getQuery());
                String key = statement.getDataKey();
                int value = Integer.valueOf(dataMap.get(key).get(0).get(0));
                if (statement.isTestTrue(value))
                    errorMessage.add(statement.getErrorMessage(value));
            } else {
                infrastructureRep.executeJdbcStatement(statement);
            }
            DbSystemUtil.logLockInfo();
        }
        return errorMessage;
    }

    private final Logger LOG = Logger.getLogger(DatabaseService.class);

}
