package org.zfin.database.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.database.DatabaseService;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Controller that displays individual rows for a given db table.
 */
@Controller
public class DatabaseColumnViewController {

    private static final Logger LOG = Logger.getLogger(DatabaseColumnViewController.class);
    private static final String DELIMITER = "|";

    @ModelAttribute("formBean")
    private DatabaseQueryFormBean getDefaultSearchForm() {
        return new DatabaseQueryFormBean();
    }

    @RequestMapping(value = "/browse-database")
    protected String browseDatabaseTables() {
        return "database/browse-database-tables.page";
    }

    @RequestMapping(value = "/view-record/{ID}")
    protected String showRecordsByGeneralPkValue(Model model,
                                                 @PathVariable("ID") String id) {
        Table entityTable = Table.getEntityTable(id);
        id = extractPkId(id);
        DatabaseJdbcStatement statement = createJdbcStatement(id, entityTable.getTableName(), entityTable.getPkName());
        runQuery(model, entityTable, statement);
        List<ForeignKeyResult> foreignKeyResultList = DatabaseService.createFKResultList(entityTable, id);
        model.addAttribute("foreignKeyResultList", foreignKeyResultList);
        model.addAttribute("flattenedForeignKeyResultList", getFlattenedView(foreignKeyResultList));
        model.addAttribute("ID", id);
        return "database/table-record-view.page";
    }

    @RequestMapping(value = "/view-records/{table}")
    protected String showRecordsByGeneralNonPkValues(Model model,
                                                     @PathVariable("table") String table,
                                                     @ModelAttribute("formBean") DatabaseQueryFormBean formBean,
                                                     BindingResult bindingResult) {
        Table entityTable = Table.getEntityTableByTableName(table);
        DatabaseJdbcStatement statement = DatabaseService.createQueryFromFullForeignKeyHierarchy(formBean.getFullNodeName(), formBean.getColumnValue().get(0), entityTable);
        runQuery(model, entityTable, statement);
        List<ForeignKeyResult> foreignKeyResultList = DatabaseService.createFKResultList(formBean.getFullNodeName(), formBean.getColumnValue().get(0));
        model.addAttribute("foreignKeyResultList", foreignKeyResultList);
        model.addAttribute("flattenedForeignKeyResultList", getFlattenedView(foreignKeyResultList));

        return "database/table-record-view.page";
    }

    @RequestMapping(value = "/fetch-entity-name/{ID}")
    protected String fetchEntityName(Model model,
                                     @PathVariable("ID") String id) {
        Table entityTable = Table.getEntityTable(id);
        id = extractPkId(id);
        String name = "not found";
        if (entityTable.getEntityName(id) != null)
            name = entityTable.getEntityName(id);
        model.addAttribute("entityName", name);
        return "database/entity-name.popup";
    }

    @RequestMapping(value = "/view-table-statistics/{tableName}")
    protected String viewTableStatistics(Model model,
                                         @PathVariable("tableName") String tableName) {
        Table entityTable = Table.getEntityTableByTableName(tableName);
        List<Column> columnMetaData = getInfrastructureRepository().retrieveColumnMetaData(entityTable);
        addNumberOfRecordsForRefTables(columnMetaData);
        model.addAttribute("numberOfRecords", DatabaseService.getNumberOfRecords(entityTable));
        model.addAttribute("table", entityTable);
        model.addAttribute("columnMetaData", columnMetaData);
        model.addAttribute("dictionaryValuesList", getDictionaryValues(entityTable));
        model.addAttribute("foreignHistograms", getForeignKeyHistogram(entityTable));
        DatabaseJdbcStatement firstTenRecords = new DatabaseJdbcStatement();
        firstTenRecords.addQueryPart("select first 15 * from " + entityTable.getTableName());
        runQuery(model, entityTable, firstTenRecords);
        return "database/view-table-stats.page";
    }

    private void addNumberOfRecordsForRefTables(List<Column> columnMetaData) {
        if (columnMetaData == null)
            return;
        for (Column column : columnMetaData) {
            for (ForeignKey foreignKey : column.getForeignKeyRelations()) {
                ReferenceTableRecord tableRecord = new ReferenceTableRecord();
                tableRecord.setTable(foreignKey.getEntityTable());
                int numOfRecords = DatabaseService.getNumberOfRecords(foreignKey.getEntityTable());
                tableRecord.setNumberOfRecords(numOfRecords);
                column.addRefTableRecord(tableRecord);
            }
        }
    }

    private List<DictionaryValues> getDictionaryValues(Table table) {
        if (table == null)
            return null;
        if (!table.hasDictionaryColumns() && !table.hasZdbDictionaryColumns())
            return null;

        List<DictionaryValues> list = new ArrayList<DictionaryValues>(2);
        if (table.hasDictionaryColumns()) {
            addDictionaryValues(table, list, false);
        }
        if (table.hasZdbDictionaryColumns()) {
            addDictionaryValues(table, list, true);
        }
        return list;
    }

    private List<DictionaryValues> getForeignKeyHistogram(Table table) {
        if (table == null)
            return null;
        if (!table.hasForeignKeys())
            return null;

        List<DictionaryValues> list = new ArrayList<DictionaryValues>(2);
        for (ForeignKey foreignKey : ForeignKey.getForeignTrueKeys(table)) {
            if (!foreignKey.isManyToManyRelationship())
                addHistogram(foreignKey.getForeignKeyTable(), list, foreignKey.getForeignKey(), false, false);
        }
        return list;
    }

    private void addDictionaryValues(Table table, List<DictionaryValues> list, boolean isZdbDictionary) {
        List<String> dictionaryColumns = null;
        if (isZdbDictionary)
            dictionaryColumns = table.getZdbDictionaryColumns();
        else
            dictionaryColumns = table.getDictionaryColumns();
        for (String dictionaryColName : dictionaryColumns) {
            addHistogram(table, list, dictionaryColName, isZdbDictionary, true);
        }
    }

    private void addHistogram(Table table, List<DictionaryValues> list, String dictionaryColName, boolean isZdbDictionary, boolean isDictionary) {
        DictionaryValues values = new DictionaryValues(dictionaryColName);
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        String sql = "select ";
        if (!isDictionary)
            sql += "first 15 ";
        if (isZdbDictionary)
            sql += "get_obj_type(" + dictionaryColName + ")";
        else
            sql += dictionaryColName;
        sql += " as type, count(*) as ct ";
        sql += " from " + table.getTableName();
        sql += " group by type order by ct desc";
        statement.addQueryPart(sql);
        values.setQuery(sql);
        List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
        if (result != null) {
            for (List<String> singleValueList : result) {
                DictionaryValue val = new DictionaryValue();
                val.setValue(singleValueList.get(0));
                val.setNumberOfValues(Integer.parseInt(singleValueList.get(1)));
                values.addValue(val);
            }
        }
        Column column = new Column(dictionaryColName, table);
        values.setColumn(column);
        list.add(values);
    }

    private void runQuery(Model model, Table entityTable, DatabaseJdbcStatement statement) {
        List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
        List<String> metaData = getInfrastructureRepository().retrieveMetaData(entityTable.getTableName());
        if (result != null) {
            Map<Column, List<String>> dataMap = createDataMap(metaData, result, entityTable);
            model.addAttribute("table", entityTable);
            model.addAttribute("dataMap", dataMap);
            model.addAttribute("allPkIds", createListOfPkIds(dataMap));
            model.addAttribute("query", statement);
            model.addAttribute("totalRecords", result.size());
        } else
            model.addAttribute("totalRecords", 0);
        model.addAttribute("table", entityTable);
        model.addAttribute("query", statement);
    }

    public List<ForeignKeyResultFlattened> getFlattenedView(List<ForeignKeyResult> foreignKeyResults) {
        if (foreignKeyResults == null)
            return null;
        List<ForeignKeyResultFlattened> flattenedList = new ArrayList<ForeignKeyResultFlattened>(foreignKeyResults.size());
        addFlattenedRecords(foreignKeyResults, 0, flattenedList, null, null);
        return flattenedList;
    }

    /**
     * Turn the list of ForeignKeyResult objects into a flattened version of ForeignKeyResultFlattened objects.
     * ForeignKeyResult have children defined while for presentational purposes it is more convenient to have a
     * flat list with an indentation specifying the level of child relationship
     *
     * @param foreignKeyResults
     * @param level             level in hierarchy
     * @param flattenedList     flattened list
     * @param root              root FK
     */
    private void addFlattenedRecords(List<ForeignKeyResult> foreignKeyResults, int level, List<ForeignKeyResultFlattened> flattenedList, ForeignKey root, String prefix) {
        if (foreignKeyResults == null)
            return;
        if (prefix == null)
            prefix = "";
        for (ForeignKeyResult foreignKeyResult : foreignKeyResults) {
            String nodeName = prefix;
            if (StringUtils.isNotEmpty(nodeName)) {
                nodeName += DELIMITER;
            }
            nodeName += foreignKeyResult.getNodeName();
            if (root == null)
                root = foreignKeyResult.getForeignKey();
            ForeignKeyResultFlattened resultFlattened = new ForeignKeyResultFlattened(foreignKeyResult.getForeignKey(), foreignKeyResult.getNumberOfResults());
            resultFlattened.setLevel(level);
            resultFlattened.setRootForeignKey(root);
            resultFlattened.setFullNodeName(nodeName);
            flattenedList.add(resultFlattened);
            if (foreignKeyResult.hasChildren()) {
                addFlattenedRecords(foreignKeyResult.getChildren(), level + 1, flattenedList, root, nodeName);
            }
        }
    }

    /**
     * If it is an id with ZDB-xxx we assume the id is the full id
     * Otherwise we expect <tablename>-id
     *
     * @param id pk id containing string
     * @return pure id
     */
    private String extractPkId(String id) {
        if (id == null)
            return null;
        if (id.startsWith("ZDB-"))
            return id;
        int indexOfFirstDash = id.indexOf("-");
        return id.substring(indexOfFirstDash + 1);
    }

    private DatabaseJdbcStatement createJdbcStatement(Object ID, String table, String pkColumnName) {
        return createJdbcStatement(ID, table, pkColumnName, false);
    }

    protected DatabaseJdbcStatement createJdbcStatement(Object ID, ForeignKey foreignKey, boolean countOnly) {
        if (foreignKey.isPKLookup())
            return createJdbcStatement(ID, foreignKey.getForeignKeyTable().getTableName(), foreignKey.getForeignKey(), countOnly);
        return createNonPkJdbcStatement(ID, foreignKey, countOnly);
    }

    private DatabaseJdbcStatement createNonPkJdbcStatement(Object ID, ForeignKey foreignKey, boolean countOnly) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select");
        if (countOnly)
            queryBuilder.append(" count(*)");
        else
            queryBuilder.append(" *");
        queryBuilder.append(" from ");
        queryBuilder.append(foreignKey.getForeignKeyTable().getTableName());
        queryBuilder.append(", ");
        queryBuilder.append(foreignKey.getEntityTable().getTableName());
        queryBuilder.append(" where ");
        queryBuilder.append(foreignKey.getEntityTable().getPkName());
        queryBuilder.append(" = ");
        if (ID instanceof String)
            queryBuilder.append("'");
        queryBuilder.append(ID);
        if (ID instanceof String)
            queryBuilder.append("'");

        queryBuilder.append(" and ");
        queryBuilder.append(foreignKey.getNonPkLookupKey());
        queryBuilder.append(" = ");
        queryBuilder.append(foreignKey.getForeignKey());
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    private DatabaseJdbcStatement createJdbcStatement(Object ID, String table, String pkColumnName, boolean countOnly) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select ");
        if (countOnly)
            queryBuilder.append(" count(*) ");
        else
            queryBuilder.append(" * ");
        queryBuilder.append(" from ");
        queryBuilder.append(table);
        queryBuilder.append(" where ");
        queryBuilder.append(pkColumnName);
        queryBuilder.append(" = ");
        if (ID instanceof String)
            queryBuilder.append("'");
        queryBuilder.append(ID);
        if (ID instanceof String)
            queryBuilder.append("'");
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    private Map<Column, List<String>> createDataMap(List<String> metaData, List<List<String>> result, Table table) {
        Map<Column, List<String>> dataMap = new TreeMap<Column, List<String>>(new Comparator<Column>() {
            public int compare(Column col1, Column col2) {
                if (col1.isPrimaryKey() && !col2.isPrimaryKey())
                    return -1;
                if (!col1.isPrimaryKey() && col2.isPrimaryKey())
                    return 1;
                if (col1.isForeignKey() && !col2.isForeignKey())
                    return -1;
                if (!col1.isForeignKey() && col2.isForeignKey())
                    return 1;
                return col1.getName().compareToIgnoreCase(col2.getName());
            }
        });
        int index = 0;
        for (String columnName : metaData) {
            List<String> columnData = new ArrayList<String>();
            if (result != null) {
                columnData = new ArrayList<String>(result.size());
                for (List<String> values : result) {
                    columnData.add(values.get(index));
                }
            }
            dataMap.put(getColumn(table, columnName), columnData);
            index++;
        }

        return dataMap;
    }

    private Column getColumn(Table table, String columnName) {
        Column column = new Column(columnName, table);
        return column;
    }

    private String createListOfPkIds(Map<Column, List<String>> dataMap) {
        if (dataMap == null)
            return null;
        StringBuilder builder = new StringBuilder(1);
        for (Column column : dataMap.keySet()) {
            if (!column.isPrimaryKey())
                continue;
            builder = new StringBuilder(dataMap.get(column).size());
            List<String> allPkIds = dataMap.get(column);
            int index = 0;
            for (String pkId : allPkIds) {
                builder.append(pkId);
                if (index < allPkIds.size() - 1)
                    builder.append("|");
                index++;
            }
            break;
        }
        return builder.toString();
    }

    private DatabaseJdbcStatement createJdbcStatement(DatabaseQueryFormBean formBean, String table, boolean countOnly) {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select ");
        if (countOnly)
            queryBuilder.append(" count(*) ");
        else
            queryBuilder.append(" * ");
        queryBuilder.append(" from ");
        queryBuilder.append(table);
        queryBuilder.append(" where ");
        queryBuilder.append(getWhereClause(formBean.getColumnName(), formBean.getColumnValue()));
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    protected DatabaseJdbcStatement createJoinedJdbcStatement(DatabaseQueryFormBean formBean, ForeignKey foreignKey, boolean countOnly) {
        if (foreignKey.isPKLookup())
            return createPKJoinedJdbcStatement(formBean, foreignKey, countOnly);
        return createNonPKJoinedJdbcStatement(formBean, foreignKey, countOnly);
    }

    protected DatabaseJdbcStatement createNonPKJoinedJdbcStatement(DatabaseQueryFormBean formBean, ForeignKey foreignKey, boolean countOnly) {
        String table = foreignKey.getForeignKeyTable().getTableName();
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select ");
        if (countOnly)
            queryBuilder.append("count(*) ");
        else
            queryBuilder.append("* ");

        List<ForeignKey> joinTables = new ArrayList<ForeignKey>(3);
        boolean independentJoin = false;
        for (String columnName : formBean.getColumnName()) {
            joinTables.addAll(ForeignKey.getJoinedForeignKeys(columnName, table));
            if (!joinTables.contains(foreignKey))
                independentJoin = true;
        }
        // move query table to the first position as this is the info to be displayed
        joinTables.remove(foreignKey);
        joinTables.add(0, foreignKey);
        queryBuilder.append("from ");
        createFromTableListNonPkQuery(queryBuilder, joinTables);
        if (independentJoin) {
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            queryBuilder.append(", ");
            queryBuilder.append(foreignKey.getEntityTable().getTableName());
            queryBuilder.append(" ");
        }
        queryBuilder.append("where ");
        getNonPkJoinedWhereClause(queryBuilder, formBean.getColumnName().get(0), formBean.getColumnValue().get(0), joinTables);
        if (independentJoin) {
            queryBuilder.append(" and ");
            queryBuilder.append(foreignKey.getForeignKey());
            queryBuilder.append(" = ");
            queryBuilder.append(foreignKey.getEntityTable().getPkName());
        }
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    protected DatabaseJdbcStatement createPKJoinedJdbcStatement(DatabaseQueryFormBean formBean, ForeignKey foreignKey, boolean countOnly) {
        String table = foreignKey.getForeignKeyTable().getTableName();
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select ");
        if (countOnly)
            queryBuilder.append("count(*) ");
        else
            queryBuilder.append("* ");

        List<ForeignKey> joinTables = new ArrayList<ForeignKey>(3);
        boolean independentJoin = false;
        for (String columnName : formBean.getColumnName()) {
            List<ForeignKey> joinedForeignKeys = ForeignKey.getJoinedForeignKeys(columnName, table);
            if (joinedForeignKeys != null)
                joinTables.addAll(joinedForeignKeys);
            if (!joinTables.contains(foreignKey))
                independentJoin = true;
        }
        // move query table to the first position as this is the info to be displayed
        joinTables.remove(foreignKey);
        joinTables.add(0, foreignKey);
        queryBuilder.append("from ");
        createFromTableList(queryBuilder, joinTables);
        if (independentJoin) {
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            queryBuilder.append(", ");
            queryBuilder.append(foreignKey.getEntityTable().getTableName());
            queryBuilder.append(" ");
        }
        queryBuilder.append("where ");
        getJoinedWhereClause(queryBuilder, formBean.getColumnName().get(0), formBean.getColumnValue().get(0), joinTables);
        if (independentJoin) {
            queryBuilder.append(" and ");
            queryBuilder.append(foreignKey.getForeignKey());
            queryBuilder.append(" = ");
            queryBuilder.append(foreignKey.getEntityTable().getPkName());
        }
        statement.addQueryPart(queryBuilder.toString());
        return statement;
    }

    private void getNonPkJoinedWhereClause(StringBuilder queryBuilder, String columnName, String columnValue, List<ForeignKey> joinTables) {
        if (joinTables == null || joinTables.size() == 0)
            return;
        ForeignKey nonPkJoin = joinTables.remove(0);
        queryBuilder.append(nonPkJoin.getEntityTable().getPkName());
        queryBuilder.append(" = ");
        queryBuilder.append("'");
        queryBuilder.append(columnValue);
        queryBuilder.append("'");
        queryBuilder.append(" and ");
        queryBuilder.append(nonPkJoin.getNonPkLookupKey());
        queryBuilder.append(" = ");
        queryBuilder.append(nonPkJoin.getForeignKey());

        if (joinTables.size() == 1)
            return;

        for (ForeignKey foreignKey : joinTables) {
            // leave out the already joined foreign key
            if (foreignKey.getForeignKey().equals(columnName))
                continue;
            queryBuilder.append(" and ");
            queryBuilder.append(foreignKey.getEntityTable().getPkName());
            queryBuilder.append(" = ");
            queryBuilder.append(foreignKey.getForeignKey());
        }
    }

    private void getJoinedWhereClause(StringBuilder queryBuilder, String columnName, String columnValue, List<ForeignKey> joinTables) {
        if (joinTables == null || joinTables.size() == 0)
            return;
        queryBuilder.append(columnName);
        queryBuilder.append(" = ");
        queryBuilder.append("'");
        queryBuilder.append(columnValue);
        queryBuilder.append("'");

        if (joinTables.size() == 1)
            return;

        for (ForeignKey foreignKey : joinTables) {
            // leave out the already joined foreign key
            if (foreignKey.getForeignKey().equals(columnName))
                continue;
            queryBuilder.append(" and ");
            queryBuilder.append(foreignKey.getEntityTable().getPkName());
            queryBuilder.append(" = ");
            queryBuilder.append(foreignKey.getForeignKey());
        }
    }

    private void createFromTableListNonPkQuery(StringBuilder queryBuilder, List<ForeignKey> joinTables) {
        if (joinTables == null || joinTables.size() == 0)
            return;
        Set<Table> usedTables = new HashSet<Table>(joinTables.size());
        for (ForeignKey foreignKey : joinTables) {
            Table foreignKeyTable = foreignKey.getForeignKeyTable();
            queryBuilder.append(foreignKeyTable.getTableName());
            if (!foreignKey.isPKLookup()) {
                queryBuilder.append(", ");
                queryBuilder.append(foreignKey.getEntityTable().getTableName());
            }
            boolean isNew = usedTables.add(foreignKeyTable);
            if (!isNew) {
                queryBuilder.append(" as ");
                queryBuilder.append(getTableNameAlias(foreignKeyTable, 1));
            }
            queryBuilder.append(", ");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 2);
    }

    private void createFromTableList(StringBuilder queryBuilder, List<ForeignKey> joinTables) {
        if (joinTables == null || joinTables.size() == 0)
            return;
        Set<Table> usedTables = new HashSet<Table>(joinTables.size());
        for (ForeignKey foreignKey : joinTables) {
            Table foreignKeyTable = foreignKey.getForeignKeyTable();
            queryBuilder.append(foreignKeyTable.getTableName());
            boolean isNew = usedTables.add(foreignKeyTable);
            if (!isNew) {
                queryBuilder.append(" as ");
                queryBuilder.append(getTableNameAlias(foreignKeyTable, 1));
            }
            queryBuilder.append(", ");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 2);
    }

    private String getTableNameAlias(Table foreignKeyTable, int index) {
        return foreignKeyTable.getTableName() + "_" + index;
    }

    private String getWhereClause(List<String> columnNames, List<String> columnValues) {
        StringBuilder queryBuilder = new StringBuilder();
        int index = 0;
        for (String columnName : columnNames) {
            if (columnValues.get(index).contains("|")) {
                queryBuilder.append(" ");
                queryBuilder.append(columnName);
                queryBuilder.append(" in ");
                queryBuilder.append(" ( ");
                String[] tokens = columnValues.get(index).split("\\|");
                int tokenIndex = 0;
                for (String val : tokens) {
                    queryBuilder.append(addValueToClause(val));
                    if (tokenIndex++ < tokens.length - 1)
                        queryBuilder.append(",");
                }
                queryBuilder.append(" ) ");
            } else {
                queryBuilder.append(" ");
                queryBuilder.append(columnName);
                queryBuilder.append(" = ");
                queryBuilder.append(addValueToClause(columnValues.get(index)));
            }
            if (index != columnNames.size() - 1)
                queryBuilder.append(" AND ");
            index++;
        }
        return queryBuilder.toString();
    }

    private String addValueToClause(String val) {
        StringBuilder queryBuilder = new StringBuilder();
        if (val.contains("ZDB-"))
            queryBuilder.append("'");
        queryBuilder.append(val);
        if (val.contains("ZDB-"))
            queryBuilder.append("'");
        return queryBuilder.toString();
    }

    public class ForeignKeyResultSort implements Comparator<ForeignKeyResult> {
        public int compare(ForeignKeyResult o1, ForeignKeyResult o2) {
            return o1.getForeignKey().getForeignKeyTable().getTableName().compareToIgnoreCase(o2.getForeignKey().getForeignKeyTable().getTableName());
        }
    }
}
