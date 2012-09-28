package org.zfin.database;

import java.util.Date;

/**
 * Domain object mapping to the table that contains the database version info.
 */
public class UnloadInfo {

    private long id;
    private Date date;
    private String version;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
