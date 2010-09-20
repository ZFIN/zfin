package org.zfin.ontology.repository;

import org.zfin.mutant.MarkerGoTermEvidence;

import java.util.List;

/**
 */
public interface MarkerGoTermEvidenceRepository {

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation) ;

    MarkerGoTermEvidence getMarkerGoTermEvidenceByZdbID(String zdbID) ;

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbID(String zdbID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbID(String publicationID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbIDOrdered(String markerID);
}
