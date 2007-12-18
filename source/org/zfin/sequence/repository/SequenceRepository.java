/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository ;

import org.zfin.sequence.*;
import org.zfin.orthology.Species;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;

public interface SequenceRepository {

    ReferenceDatabase getReferenceDatabaseByAlternateKey(ForeignDB foreignDB,
                                                      ReferenceDatabase.Type type,
                                                      ReferenceDatabase.SuperType superType,
                                                      Species organism) ;
    ForeignDB getForeignDBByName(String dbName);

    ReferenceDatabase getReferenceDatabase(String foreignDBName,
                                                      ReferenceDatabase.Type type,
                                                      ReferenceDatabase.SuperType superType,
                                                      Species organism) ;


    Accession getAccessionByAlternateKey(String number, ReferenceDatabase referenceDatabase);

    Map<String, MarkerDBLink > getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases) throws Exception ;
    Map<String, Set<MarkerDBLink> > getMarkerDBLinks(ReferenceDatabase... referenceDatabases) throws Exception ;
    void addDBLinks(Collection<MarkerDBLink> dbLinksToAdd, Publication attributionPub, int commitChunk);
    public void removeDBLinks(Set<MarkerDBLink> dbLinksToRemove) ;
}



