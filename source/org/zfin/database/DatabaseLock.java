package org.zfin.database;

import java.io.Serializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class DatabaseLock implements Serializable {

    private String dbsName;
    private String tableName;
    private String rowId;
    private String type;
    private long keyNum;
    private String waiter;
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

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getType() {
        return type;
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

    public long getKeyNum() {
        return keyNum;
    }

    public void setKeyNum(long keyNum) {
        this.keyNum = keyNum;
    }

    public String getWaiter() {
        return waiter;
    }

    public void setWaiter(String waiter) {
        this.waiter = waiter;
    }
    
    public Type getLockType(){
        return Type.getType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseLock syslock = (DatabaseLock) o;

        if (dbsName != null ? !dbsName.equals(syslock.dbsName) : syslock.dbsName != null) return false;
        if (rowId != null ? !rowId.equals(syslock.rowId) : syslock.rowId != null) return false;
        if (session != null ? !session.equals(syslock.session) : syslock.session != null) return false;
        if (tableName != null ? !tableName.equals(syslock.tableName) : syslock.tableName != null) return false;
        if (type != null ? !type.equals(syslock.type) : syslock.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dbsName != null ? dbsName.hashCode() : 0;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (rowId != null ? rowId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    public enum Type {
        B("B", "Byte lock"),
        IS("IS", "Intent shared lock"),
        S("S", "Shared lock"),
        XS("XS", "Shared key value held by a repeatable reader"),
        U("U", "Update lock"),
        IX("IX", "Intent exclusive lock"),
        SIX("SIX", "Shared intent exclusive lock"),
        X("X", "Exclusive lock"),
        XR("XR", "Exclusive key value held by a repeatable reader"),;

        private Type(String id, String name) {
            this.name = name;
            this.id = id;
        }

        private String name;
        private String id;

        public static Type getType(String name) {
            for (Type type : values())
                if (type.id.equals(name))
                    return type;
            throw new RuntimeException("No Lock type with id " + name + " found");
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }
}
