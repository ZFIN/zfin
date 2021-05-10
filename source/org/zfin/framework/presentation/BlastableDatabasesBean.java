package org.zfin.framework.presentation;

import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import java.util.List;

/**
 */
public class BlastableDatabasesBean {

    private String selectedReferenceDatabaseZdbID;
    private String databaseToSetAsPrimaryZdbID;
    private String databaseToAddZdbID;
    private String databaseToRemoveZdbID;
    private List<ReferenceDatabase> referenceDatabases ;
    private List<Database> databases ;

    public String getSelectedReferenceDatabaseZdbID() {
        return selectedReferenceDatabaseZdbID;
    }

    public void setSelectedReferenceDatabaseZdbID(String selectedReferenceDatabaseZdbID) {
        this.selectedReferenceDatabaseZdbID = selectedReferenceDatabaseZdbID;
    }

    public String getDatabaseToSetAsPrimaryZdbID() {
        return databaseToSetAsPrimaryZdbID;
    }

    public void setDatabaseToSetAsPrimaryZdbID(String databaseToSetAsPrimaryZdbID) {
        this.databaseToSetAsPrimaryZdbID = databaseToSetAsPrimaryZdbID;
    }

    public String getDatabaseToAddZdbID() {
        return databaseToAddZdbID;
    }

    public void setDatabaseToAddZdbID(String databaseToAddZdbID) {
        this.databaseToAddZdbID = databaseToAddZdbID;
    }

    public String getDatabaseToRemoveZdbID() {
        return databaseToRemoveZdbID;
    }

    public void setDatabaseToRemoveZdbID(String databaseToRemoveZdbID) {
        this.databaseToRemoveZdbID = databaseToRemoveZdbID;
    }

    public List<ReferenceDatabase> getReferenceDatabases() {
        return referenceDatabases;
    }

    public void setReferenceDatabases(List<ReferenceDatabase> referenceDatabases) {
        this.referenceDatabases = referenceDatabases;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }
}
