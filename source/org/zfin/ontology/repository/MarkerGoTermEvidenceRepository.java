package org.zfin.ontology.repository;

import org.zfin.mutant.GafOrganization;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.util.List;

/**
 */
public interface MarkerGoTermEvidenceRepository {

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation);

    MarkerGoTermEvidence getMarkerGoTermEvidenceByZdbID(String zdbID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbID(String zdbID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbID(String publicationID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbIDOrdered(String markerID);

    GoEvidenceCode getGoEvidenceCode(String evidenceCode);

    MarkerGoTermEvidence getNdExistsForGoGeneEvidenceCode(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    GafOrganization getGafOrganization(GafOrganization.OrganizationEnum organizationEnum);

    /**
     * This methods assumes that the inferences have not been added either.
     *
     * @param markerGoTermEvidenceToAdd
     */
    MarkerGoTermEvidence addEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    void removeEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    List<String> getEvidencesForGafOrganization(GafOrganization gafOrganization);

    List<MarkerGoTermEvidence> getLikeMarkerGoTermEvidencesButGo(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    int deleteMarkerGoTermEvidenceByZdbIDs(List<String> zdbIDs);
}
