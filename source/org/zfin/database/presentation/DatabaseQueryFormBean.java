package org.zfin.database.presentation;


import org.apache.commons.lang.StringUtils;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.SortBy;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.ontology.Term;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Form bean used for the antibody search page.
 */
public class DatabaseQueryFormBean extends PaginationBean {

    private List<String> columnName;
    private List<String> columnValue;
    private String foreignKeyName;
    private String parentPkValue;
    private String fullNodeName;

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



