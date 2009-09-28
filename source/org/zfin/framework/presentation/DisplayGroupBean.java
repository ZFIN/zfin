package org.zfin.framework.presentation;

import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;
import java.util.Set;

public class DisplayGroupBean {

    private Long displayGroupToEditID;
    private String referenceDatabaseToAddZdbID ;
    private String referenceDatabaseToRemoveZdbID ;

    private Set<ReferenceDatabase> referenceDatabases;
    private Set<DisplayGroup> displayGroups;

    public Long getDisplayGroupToEditID() {
        return displayGroupToEditID;
    }

    public void setDisplayGroupToEditID(Long displayGroupToEditID) {
        this.displayGroupToEditID = displayGroupToEditID;
    }

    public String getReferenceDatabaseToAddZdbID() {
        return referenceDatabaseToAddZdbID;
    }

    public void setReferenceDatabaseToAddZdbID(String referenceDatabaseToAddZdbID) {
        this.referenceDatabaseToAddZdbID = referenceDatabaseToAddZdbID;
    }

    public String getReferenceDatabaseToRemoveZdbID() {
        return referenceDatabaseToRemoveZdbID;
    }

    public void setReferenceDatabaseToRemoveZdbID(String referenceDatabaseToRemoveZdbID) {
        this.referenceDatabaseToRemoveZdbID = referenceDatabaseToRemoveZdbID;
    }

    public Set<ReferenceDatabase> getReferenceDatabases() {
        return referenceDatabases;
    }

    public void setReferenceDatabases(Set<ReferenceDatabase> referenceDatabases) {
        this.referenceDatabases = referenceDatabases;
    }

    public Set<DisplayGroup> getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(Set<DisplayGroup> displayGroups) {
        this.displayGroups = displayGroups;
    }



    public void clear(){
        displayGroupToEditID = null ;
        referenceDatabaseToAddZdbID = null ;
        referenceDatabaseToRemoveZdbID = null ;
    }
}
