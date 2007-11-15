/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository ;

import org.zfin.sequence.*;
import org.zfin.orthology.Species;
import org.zfin.marker.Marker;

import java.util.List;

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

    public Accession getAccessionByPrimaryKey(Long id);
    
    public AccessionRelationship getAccessionRelationshipByPrimaryKey(String zdbID);
//    public List<MarkerDBLink> getMarkerDBLinksByAccession(Accession accession) ;
}



