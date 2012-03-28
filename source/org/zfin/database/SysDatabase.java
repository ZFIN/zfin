package org.zfin.database;

import java.util.Date;

/**
 * Maps to all database records.
 */
public class SysDatabase implements Comparable<SysDatabase> {

    private String name;
    private String owner;
    private Date dateCreated;
    private boolean logging;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    @Override
    public int compareTo(SysDatabase o) {
        return name.compareTo(o.getName());
    }
}
