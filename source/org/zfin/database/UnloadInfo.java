package org.zfin.database;

import javax.persistence.*;
import java.util.Date;

/**
 * Domain object mapping to the table that contains the database version info.
 */
@Entity
@Table(name = "database_info")
public class UnloadInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "di_pk_id")
    private long id;
    @Column(name = "di_date_unloaded")
    private Date date;
    @Column(name = "di_code_version")
    private String version;
    @Column(name = "di_database_unloaded")
    private String databaseName;

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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
