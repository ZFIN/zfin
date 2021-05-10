package org.zfin.database.presentation;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: christianpich
 * Date: 10/14/11
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableValueLookup {

    private Table table;
    private Set<ColumnValue> columnValues;

    public TableValueLookup(Table table) {
        this.table = table;
    }

    public void addColumnValue(ColumnValue columnValue){
        if(columnValues == null)
            columnValues = new HashSet<ColumnValue>(2);
        columnValues.add(columnValue);
    }

    public Set<ColumnValue> getColumnValues() {
        return columnValues;
    }

    public Table getTable() {
        return table;
    }
}
