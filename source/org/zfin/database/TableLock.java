package org.zfin.database;

import java.io.Serializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TableLock implements Serializable, Comparable<TableLock> {

    private String dbsName;
    private String tableName;
    private String rowId;
    private String type;
    private long numOfLocks;
    private SysSession session;

    public String getDbsName() {
        return dbsName;
    }

    public void setDbsName(String dbsName) {
        this.dbsName = dbsName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRowId() {
        return rowId;
    }

    public String getRowLength() {
        int rowInteger = 0;
        try {
            rowInteger = Integer.parseInt(rowId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return rowId;
        }
        switch (rowInteger) {
            case 0:
                return "Table";
        }
        if (rowId.endsWith("00"))
            return "Page";
        if (rowId.length() < 7)
            return "Row";

        return "Key-Value";
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getType() {
        return Type.getType(type).getDisplayName();
    }

    public void setType(String type) {
        this.type = type;
    }

    public SysSession getSession() {
        return session;
    }

    public void setSession(SysSession session) {
        this.session = session;
    }

    public long getNumOfLocks() {
        return numOfLocks;
    }

    public void setNumOfLocks(long numOfLocks) {
        this.numOfLocks = numOfLocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableLock tableLock = (TableLock) o;

        if (dbsName != null ? !dbsName.equals(tableLock.dbsName) : tableLock.dbsName != null) return false;
        if (session != null ? !session.equals(tableLock.session) : tableLock.session != null) return false;
        if (tableName != null ? !tableName.equals(tableLock.tableName) : tableLock.tableName != null) return false;
        if (type != null ? !type.equals(tableLock.type) : tableLock.type != null) return false;
        long rowIdInt = Long.parseLong(rowId);
        long rowIdIntComp = Long.parseLong(tableLock.getRowId());
        if ((rowIdInt == 0 && rowIdIntComp != 0) || (rowIdInt != 0 && rowIdIntComp == 0))
            return false;
        if ((rowId.length() > 6 && tableLock.getRowId().length() < 7) || (rowId.length() < 7 && tableLock.getRowId().length() > 6))
            return false;
        if ((rowId.endsWith("00") && !tableLock.getRowId().endsWith("00")) || (!rowId.endsWith("00") && tableLock.getRowId().endsWith("00")))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dbsName != null ? dbsName.hashCode() : 0;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        // do not care about discriminating rowIDs. They just all go into the same bucket (maps)
        return result;
    }

    public void incrementLockCounter() {
        numOfLocks++;
    }

    @Override
    public int compareTo(TableLock tableLock) {
        if (tableLock == null)
            return +1;

        if (numOfLocks != tableLock.numOfLocks)
            return (int) (tableLock.numOfLocks - numOfLocks);
        return tableName.toLowerCase().compareTo(tableLock.getTableName().toLowerCase());
    }

    enum Type {
        B("Bytes"), X("Exclusive"), IX("Exclusive Int"), S("Shred"), U("Update"), IS("Shred Int"),
        SIX("Exclusive Shred Int"), BYTE("BYTE");

        private String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public static Type getType(String name) {
            for (Type type : values()) {
                if (type.toString().equals(name))
                    return type;
            }
            throw new RuntimeException("No type of name " + name + " found");
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
