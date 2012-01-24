package org.zfin.database.presentation;

/**
 * Created by IntelliJ IDEA.
 * User: christianpich
 * Date: 10/14/11
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnValue {

    private String columnName;
    private Table table;
    private Object value;

    public ColumnValue(Table table, Object value) {
        this.table = table;
        columnName = table.getPkName();
        this.value = value;
    }

    public ColumnValue(String columnName, Object value) {
        this.columnName = columnName;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getColumnName() {
        return columnName;
    }

    public Table getTable() {
        return table;
    }
}
