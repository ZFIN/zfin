package org.zfin.database.presentation;


import org.zfin.framework.presentation.PaginationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Form bean used for the antibody search page.
 */
public class DatabaseQueryFormBean extends PaginationBean {

    private List<String> columnName;
    private List<String> columnValue;
    private String foreignKeyName;
    private String parentPkValue;
    private String fullNodeName;
    private String baseTable;

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = new ArrayList<String>(1);
        this.columnName.add(columnName);
    }

    public List<String> getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(List<String> columnValue) {
        this.columnValue = columnValue;
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = new ArrayList<String>(1);
        this.columnValue.add(columnValue);
    }

    public String getParentPkValue() {
        return parentPkValue;
    }

    public void setParentPkValue(String parentPkValue) {
        this.parentPkValue = parentPkValue;
    }

    public String getBaseTable() {
        return baseTable;
    }

    public void setBaseTable(String baseTable) {
        this.baseTable = baseTable;
    }

    public String getFullNodeName() {
        return fullNodeName;
    }

    public void setFullNodeName(String fullNodeName) {
        this.fullNodeName = fullNodeName;
    }

    public String getFullNodeNameForNextForeignKey() {
        if (fullNodeName == null)
            return "";
        return fullNodeName + ",";
    }
}



