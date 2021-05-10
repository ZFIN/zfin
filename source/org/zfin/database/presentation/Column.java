package org.zfin.database.presentation;

import java.util.*;

public class Column {

    private String name;
    private List<ForeignKey> foreignKeyRelations;
    private Table table;
    private String columnType;
    private int columnLength;
    private int nullable;
    private List<ReferenceTableRecord> referenceTableRecordList;

    public Column(String name, Table table) {
        this.name = name;
        this.table = table;
        foreignKeyRelations = ForeignKey.getForeignKeysByColumnName(name);
    }

    public String getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    public boolean isForeignKey() {
        return foreignKeyRelations != null && foreignKeyRelations.size() > 0;
    }

    public List<ForeignKey> getForeignKeyRelations() {
        return foreignKeyRelations;
    }

    public boolean isPrimaryKey() {
        return table.isPrimaryKey(name);
    }

    public boolean isEntityName() {
        if (foreignKeyRelations == null)
            return false;
        return foreignKeyRelations.get(0).getEntityTable().isEntityName();
    }

    public String getForeignKeyTableName() {
        if (foreignKeyRelations == null)
            return null;
        return foreignKeyRelations.get(0).getEntityTable().getTableName();
    }

    public ForeignKey getForeignKeyRelation() {
        return foreignKeyRelations.get(0);
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public int getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(int columnLength) {
        this.columnLength = columnLength;
    }

    public boolean isNullable() {
        return nullable == 1;
    }

    public void setIsNullable(int nullable) {
        this.nullable = nullable;
    }

    public void addRefTableRecord(ReferenceTableRecord tableRecord) {
        if (referenceTableRecordList == null)
            referenceTableRecordList = new ArrayList<ReferenceTableRecord>(5);
        referenceTableRecordList.add(tableRecord);
    }

    public List<ReferenceTableRecord> getReferenceTableRecordList() {
        return referenceTableRecordList;
    }
}
