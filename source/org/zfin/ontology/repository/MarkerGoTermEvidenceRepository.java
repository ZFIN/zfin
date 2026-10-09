package org.zfin.ontology.repository;

import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.marker.Marker;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.NoctuaModel;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import java.util.List;
import java.util.SortedSet;

/**
 */
public interface MarkerGoTermEvidenceRepository {

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation);

    MarkerGoTermEvidence getMarkerGoTermEvidenceByZdbID(String zdbID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbID(String zdbID);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbID(String publicationID);

    @SuppressWarnings("unchecked")
    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbIDs(List<String> publicationIDs);

    List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbIDOrdered(String markerID);

    GoEvidenceCode getGoEvidenceCode(String evidenceCode);

    MarkerGoTermEvidence getNdExistsForGoGeneEvidenceCode(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    /**
     * Every currently-stored root-term (GO:0003674 / GO:0008150 / GO:0005575) annotation, across
     * all markers. Small (a few thousand rows at most -- ND is a placeholder, not a common
     * annotation), so a full fetch is cheap and lets a caller build an in-memory lookup once per
     * run instead of querying per row.
     */
    List<MarkerGoTermEvidence> getAllRootTermAnnotations();

    GafOrganization getGafOrganization(GafOrganization.OrganizationEnum organizationEnum);

    /**
     * This methods assumes that the inferences have not been added either.
     *
     * @param markerGoTermEvidenceToAdd
     * @param isInternalLoad
     */
    void addEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd, boolean isInternalLoad);

    String isValidMarkerGoTerm(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    void removeEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    void updateEvidence(MarkerGoTermEvidence markerGoTermEvidence);

    List<String> getEvidencesForGafOrganization(GafOrganization gafOrganization);

    List<MarkerGoTermEvidence> getLikeMarkerGoTermEvidencesButGo(MarkerGoTermEvidence markerGoTermEvidenceToAdd);

    int deleteMarkerGoTermEvidenceByZdbIDs(List<String> zdbIDs);

    int getEvidenceForMarkerCount(Marker m);

    MarkerGoTermEvidence getFirstEvidenceForMarkerOntology(Marker m,Ontology ontology);

    SortedSet<GenericTerm> getGOtermsInferedFromZDBid(String zdbID);

    NoctuaModel getNoctuaModel(String modelID);

    void saveNoctualModel(NoctuaModel noctuaModel);
}
