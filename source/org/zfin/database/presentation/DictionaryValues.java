package org.zfin.database.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class DictionaryValues {

    private String columnName;
    private List<DictionaryValue> values;
    private String query;
    private Column column;

    public DictionaryValues(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public List<DictionaryValue> getValues() {
        return values;
    }

    public void setValues(List<DictionaryValue> values) {
        this.values = values;
    }

    public void addValue(DictionaryValue value) {
        if (values == null)
            values = new ArrayList<DictionaryValue>();
        values.add(value);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNumberOfRecords() {
        if (values == null)
            return 0;
        int sum = 0;
        for (DictionaryValue val : values)
            sum += val.getNumberOfValues();
        return sum;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }
}
