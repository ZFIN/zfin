package org.zfin.database;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Maps to all database records.
 */
@Entity
@Data
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

    @Override
    public int compareTo(PostgresSession o) {
        return dbname.compareTo(o.getDbname());
    }
}
