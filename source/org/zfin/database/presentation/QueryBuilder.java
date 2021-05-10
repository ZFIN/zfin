package org.zfin.database.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.util.SqlQueryKeywords.*;

/**
 *
 */
public class QueryBuilder {

    private Table entityTable;
    private List<String> selectColumnNames = new ArrayList<String>(5);
    private List<String> tableNamePlusAlias = new ArrayList<String>(5);
    private Map<String, Integer> aliasIndex = new HashMap<String, Integer>(5);
    private List<String> tableList = new ArrayList<String>(5);
    private List<String> whereClauseList = new ArrayList<String>(5);

    private String currentTable;
    private String currentTableAlias;

    public QueryBuilder(Table entityTable) {
        this.entityTable = entityTable;
        if (entityTable == null) {
            String countElement = COUNT;
            countElement += "(*)";
            selectColumnNames.add(countElement);
        }
    }

    public String getSqlQuery() {
        if (selectColumnNames.isEmpty())
            selectColumnNames.add(getTableAlias(entityTable.getTableName()) + ".*");
        StringBuilder builder = new StringBuilder(SELECT);
        builder.append(" ");
        addListElements(selectColumnNames, builder);
        builder.append(" ");
        builder.append(FROM);
        builder.append(" ");
        addListElements(tableNamePlusAlias, builder);
        builder.append(" ");
        builder.append(WHERE);
        builder.append(" ");
        addWhereClauseElements(whereClauseList, builder);
        return builder.toString();
    }

    private void addListElements(List<String> elements, StringBuilder builder) {
        for (String colName : elements) {
            builder.append(colName);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
    }

    private void addWhereClauseElements(List<String> elements, StringBuilder builder) {
        int index = 1;
        for (String colName : elements) {
            builder.append(colName);
            if (index < elements.size()) {
                builder.append(" ");
                builder.append(AND);
                builder.append(" ");

            }
            index++;
        }
    }

    public void addSelectColumn(String columnName) {
        selectColumnNames.add(columnName);
    }

    public void addWhereClause(ColumnValue columnValue) {
        addWhereClause(columnValue.getColumnName(), columnValue.getValue());
    }

    /**
     * Assumes the only table added yet is the Primary table
     *
     * @param columnValue
     */
    public void addPKWhereClause(ColumnValue columnValue) {
        String fullColumnName = getTableAlias(tableList.get(0));
        fullColumnName += "." + columnValue.getColumnName();
        addWhereClause(fullColumnName, columnValue.getValue());
    }

    /**
     * add a clause with a parentColumn and a value
     *
     * @param key
     * @param value
     */
    public void addWhereClause(String key, Object value) {
        StringBuilder clause = new StringBuilder(key);
        clause.append(" ");
        clause.append(EQUALS);
        clause.append(" ");
        if (value instanceof Integer) {
            clause.append(value.toString());
        }
        if (value instanceof String) {
            clause.append("'");
            clause.append(value.toString());
            clause.append("'");
        }
        whereClauseList.add(clause.toString());
    }

    public String addTable(String tableName) {
        return addTable(tableName, false);
    }

    public String addTable(String tableName, boolean independent) {
        currentTable = tableName;
        currentTableAlias = getTableAlias(tableName, independent);
        tableNamePlusAlias.add(tableName + " as " + currentTableAlias);
        int index = getIndex(tableName);
        if (independent)
            index++;
        aliasIndex.put(tableName, index);
        tableList.add(tableName);
        return currentTableAlias;
    }

    private String getTableAlias(String tableName) {
        return getTableAlias(tableName, false);
    }

    private String getTableAlias(String tableName, boolean independent) {
        if (independent)
            return tableName + "_" + (getIndex(tableName) + 1);
        return tableName + "_" + getIndex(tableName);
    }

    private String getTableAlias(Table table) {
        return getTableAlias(table, false);
    }

    private String getTableAlias(Table table, boolean independent) {
        return getTableAlias(table.getTableName(), independent);
    }

    private int getIndex(String tableName) {
        if (aliasIndex.get(tableName) == null)
            return 1;
        return aliasIndex.get(tableName);
    }

    @Override
    public String toString() {
        return getSqlQuery();
    }

    public String addTable(Table foreignKeyTable) {
        return addTable(foreignKeyTable.getTableName());
    }

    public String addTable(Table foreignKeyTable, boolean independent) {
        return addTable(foreignKeyTable.getTableName(), independent);
    }

    /**
     * add a clause with a pair of join keys
     *
     * @param leftJoinTable
     * @param leftJoinColumn
     */
    public void addWhereJoinedClause(Table leftJoinTable, String leftJoinColumn, Table rightJoinTable, boolean firstJoin) {
        String leftJoinTableAlias;
        String rightJoinTableAlias;
        if (firstJoin) {
            leftJoinTableAlias = getTableAlias(leftJoinTable);
            rightJoinTableAlias = addTable(rightJoinTable);
        } else {
            rightJoinTableAlias = getTableAlias(rightJoinTable);
            leftJoinTableAlias = addTable(leftJoinTable, true);
        }
        String rightJoinColumn = rightJoinTable.getForeignKey(leftJoinTable);
        if (rightJoinColumn.contains(":"))
            rightJoinColumn = rightJoinColumn.split("\\:")[1];
        StringBuilder clause = new StringBuilder(leftJoinTableAlias);
        clause.append(".");
        clause.append(leftJoinColumn);
        clause.append(" ");
        clause.append(EQUALS);
        clause.append(" ");
        clause.append(rightJoinTableAlias);
        clause.append(".");
        clause.append(rightJoinColumn);
        whereClauseList.add(clause.toString());
    }

    public void addTableWhereJoinedClause(Table parentTable, ForeignKey foreignKey) {
        String parentTableAlias = getTableAlias(parentTable.getTableName());
        Table foreignKeyTable = foreignKey.getForeignKeyTable();
        addTable(foreignKeyTable);
        String joinTableAlias = getTableAlias(foreignKeyTable.getTableName());
        StringBuilder clause = new StringBuilder(parentTableAlias);
        clause.append(".");
        clause.append(parentTable.getPkName());
        clause.append(" ");
        clause.append(EQUALS);
        clause.append(" ");
        clause.append(joinTableAlias);
        clause.append(".");
        String foreignKeyColumnName = foreignKey.getForeignKey();
        if (foreignKeyColumnName.contains(":"))
            foreignKeyColumnName = foreignKeyColumnName.split(":")[1];
        clause.append(foreignKeyColumnName);
        whereClauseList.add(clause.toString());
    }

    public void addManyToManyJoinClause(Table rootTable, ForeignKey foreignKey) {
        Table manyToManyTable = foreignKey.getManyToManyTable();
        String joinColumn = foreignKey.getEntityTable().getPkName();
        addWhereJoinedClause(rootTable, joinColumn, manyToManyTable, true);
        Table joinTable = foreignKey.getForeignKeyTable();
        joinColumn = joinTable.getPkName();
        addWhereJoinedClause(joinTable, joinColumn, manyToManyTable, false);

    }
}
