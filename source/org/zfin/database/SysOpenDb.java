package org.zfin.database;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class SysOpenDb implements Comparable<SysOpenDb>{

    private int sid;
    private String name;
    private short isolation;

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getIsolation() {
        return isolation;
    }

    public void setIsolation(short isolation) {
        this.isolation = isolation;
    }

    @Override
    public int compareTo(SysOpenDb o) {
        return name.compareTo(o.getName());
    }
}
