package org.zfin.sequence.blast.repository;

import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.DatabaseRelationship;
import org.zfin.sequence.blast.Origination;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 */
public interface BlastRepository {
        // get blastDBS
    Database getDatabase(Database.AvailableAbbrev blastDatabaseAvailableAbbrev) ;
    Origination getOrigination(Origination.Type type) ; 
    List<Database> getDatabases(Database.Type type) ;
    List<Database> getDatabases(Database.Type type, boolean excludePrivate,boolean excludeExternal) ;
    List<Database> getDatabaseByOrigination(Origination.Type... originationType) ;
    List<DatabaseRelationship> getChildDatabaseRelationshipsByOrigination(Origination.Type originationType) ;
    Set<String> getAllValidAccessionNumbers(Database database) ;
    List<String> getPreviousAccessionsForDatabase(Database database) ;
    Integer getNumberValidAccessionNumbers(Database database) ;
    int setAllDatabaseLock(boolean isLocked) ;
    void addPreviousAccessions(Database database, Collection<String> accessionToAdd);
    void removePreviousAccessions(Database database, Collection<String> accessionToRemove);
}
