package org.zfin.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Maps to all database records.
 */
@Entity
@Table(name = "PG_STAT_ACTIVITY")
public class PostgresSession implements Comparable<PostgresSession> {

    @Id
    @Column(name = "pid")
    private long pid;
    @Column(name = "datname")
    private String dbname;
    @Column(name = "usename")
    private String owner;
    @Column(name = "query")
    private String query;
    @Column(name = "state")
    private String state;
    @Column(name = "backend_start")
    private Date dateCreated;
    @Column(name = "state_change")
    private Date dateLastUsed;

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getDateLastUsed() {
        return dateLastUsed;
    }

    public void setDateLastUsed(Date dateLastUsed) {
        this.dateLastUsed = dateLastUsed;
    }

    @Override
    public int compareTo(PostgresSession o) {
        return dbname.compareTo(o.getDbname());
    }
}
