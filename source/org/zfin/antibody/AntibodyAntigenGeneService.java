package org.zfin.antibody;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.marker.service.MarkerService;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Class that contains various methods retrieving aggregated info from
 * antibodies.
 */
@Service
@Log4j2
public class AntibodyAntigenGeneService {

    /**
     * Get the list of antigen genes for an antibody. Returns as MarkerRelationshipPresentation for serialization
     * to the browser
     * @param antibodyZdbId the ID of the antibody
     * @return MarkerRelationshipPresentation list
     */
    public List<MarkerRelationshipPresentation> getAntigenGenes(String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);

        // get antigen genes
        Set<MarkerRelationship> markerRelationships = antibody.getSecondMarkerRelationships();
        List<MarkerRelationshipPresentation> relatedGenes = new ArrayList<>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            if (
                    markerRelationship.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG)
                            // todo: should use a different type
                            &&
                            markerRelationship.getType().equals(MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY)
            ) {
                Marker gene = markerRelationship.getFirstMarker();
                MarkerRelationshipPresentation markerRelationshipPresentation = new MarkerRelationshipPresentation();
                markerRelationshipPresentation.setAbbreviation(gene.getAbbreviation());
                markerRelationshipPresentation.setZdbID(gene.getZdbID());
                markerRelationshipPresentation.setSinglePublication(markerRelationship.getSinglePublication());
                markerRelationshipPresentation.setRelatedMarker(gene);
                markerRelationshipPresentation.setMarkerRelationshipZdbId(markerRelationship.getZdbID());
                markerRelationshipPresentation.setNumberOfPublications(markerRelationship.getPublicationCount());
                markerRelationshipPresentation.setAttributionZdbIDs(
                        markerRelationship
                                .getPublications()
                                .stream()
                                .map(RecordAttribution::getSourceZdbID)
                                .collect(Collectors.toSet())
                );
                relatedGenes.add(markerRelationshipPresentation);
            }
        }

        relatedGenes.sort(Comparator.comparing(MarkerRelationshipPresentation::getAbbreviation));
        return relatedGenes;
    }

    /**
     * Given an ID for an antibody, an abbreviation for an antigen gene, and a set of supporting publication IDs,
     * create the MarkerRelationship and attributions
     *
     * @param antibodyZdbId the ID of the antibody
     * @param antigenGeneAbbreviation the abbreviation of the antigen gene
     * @param publicationIDs list of attribution IDs
     * @return the newly created marker relationship (or existing one if found)
     */
    public MarkerRelationship addAntigenGeneForAntibody(String antibodyZdbId, String antigenGeneAbbreviation, Set<String> publicationIDs) {

        Marker firstMarker = getMarkerRepository().getMarkerByAbbreviation(antigenGeneAbbreviation);
        Marker antibody = getMarkerRepository().getMarkerByID(antibodyZdbId);

        if (!antibody.isInTypeGroup(Marker.TypeGroup.ATB)) {
            log.error("NOT ANTIBODY " + antibodyZdbId);
            return null;
        }

        List<String> pubList = new ArrayList<>(publicationIDs);

        MarkerRelationship markerRelationship = getMarkerRepository().getMarkerRelationship(firstMarker, antibody, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        if (markerRelationship == null) {
            String publicationID = pubList.remove(0);
            markerRelationship = MarkerService.addMarkerRelationship(firstMarker, antibody, publicationID, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        }

        List<String> existingPublicationIDs = markerRelationship.getPublications().stream().map(PublicationAttribution::getSourceZdbID).toList();
        Collection<String> remainingPublications = CollectionUtils.subtract(pubList, existingPublicationIDs);
        for(String publicationID : remainingPublications) {
            MarkerService.addMarkerRelationshipAttribution(firstMarker, antibody, publicationID, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        }

        return markerRelationship;
    }

    /**
     * Delete relationship for antibody and antigen gene
     * @param antibodyZdbId the ID of the antibody
     * @param antibodyAntigenGeneRelationshipID MarkerRelationship ZDB ID for relationship for antibody and antigen gene
     */
    public void deleteAntigenGeneForAntibody(String antibodyZdbId, String antibodyAntigenGeneRelationshipID ) throws Exception {
        Marker antibody = getMarkerRepository().getMarkerByID(antibodyZdbId);
        MarkerRelationship mrel = getMarkerRepository().getMarkerRelationshipByID(antibodyAntigenGeneRelationshipID);
        Marker secondMarker = mrel.getSecondMarker();

        if (!secondMarker.equals(antibody)) {
            throw new Exception("Trying to delete marker relationship for antibody without matching antibody");
        }

        MarkerService.deleteMarkerRelationship(mrel);
    }

    /**
     * Get a marker relationship for antibody and antigen gene
     * @param antibodyZdbID the ID of the antibody
     * @param antigenGeneAbbreviation the abbreviation of the antigen gene
     */
    public MarkerRelationship getAntibodyAntigenGeneMarkerRelationship(String antibodyZdbID, String antigenGeneAbbreviation) {
        Marker antibody = getMarkerRepository().getMarker(antibodyZdbID);
        Marker firstMarker = getMarkerRepository().getMarkerByAbbreviation(antigenGeneAbbreviation);
        return getMarkerRepository().getMarkerRelationship(firstMarker, antibody, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
    }

    /**
     * Given existing relationship by antibody ZDB-ID and an associated antigen gene, and a set of supporting publicationIDs,
     * this will update the existing list of references for that relationship to match the given list of publications.
     *
     * @param antibodyZdbID the ID of the antibody
     * @param antigenGeneAbbreviation the abbreviation of the antigen gene
     * @param publicationIDs list of attribution IDs
     */
    public void updateAntigenGenePublicationsForAntibody(String antibodyZdbID, String antigenGeneAbbreviation, Set<String> publicationIDs) throws Exception {
        MarkerRelationship mrel = getAntibodyAntigenGeneMarkerRelationship(antibodyZdbID, antigenGeneAbbreviation);

        //does the mrel exist?
        if (null == mrel) {
            throw new Exception("No such marker relationship for: " + antibodyZdbID + " and " + antigenGeneAbbreviation);
        }

        updateAntigenGenePublicationsForAntibodyRelationship(mrel, publicationIDs);
    }

    /**
     * Returns the list of publication IDs that support a relationship between antibody and antigen gene
     * @param antibodyZdbID the ID of the antibody
     * @param antigenGeneAbbreviation the abbreviation of the antigen gene
     */
    public List<String> getPublicationAttributionsForAntibodyAntigenGeneRelationship(String antibodyZdbID, String antigenGeneAbbreviation) throws Exception {
        Marker antibody = getMarkerRepository().getMarker(antibodyZdbID);
        Marker antigenGene = getMarkerRepository().getMarkerByAbbreviation(antigenGeneAbbreviation);
        MarkerRelationship markerRelationship = getMarkerRepository().getMarkerRelationship(antigenGene, antibody, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);

        if (null == markerRelationship) {
            throw new Exception("No such antibody/antigen gene marker relationship for " + antibodyZdbID + ", " + antigenGeneAbbreviation);
        }

        List<RecordAttribution> attributionList = getInfrastructureRepository().getRecordAttributions(getInfrastructureRepository().getActiveData(markerRelationship.getZdbID()));
        return attributionList.stream().map(RecordAttribution::getSourceZdbID).toList();
    }

    /**
     * update relationship between antibody and antigen gene
     * can be used to change the antigen gene (which is the same as a delete and add), or to update publication attributions
     * @param antibodyAntigenGeneRelationshipID MarkerRelationship ZDB ID for relationship for antibody and antigen gene
     * @param antigenGeneAbbreviation the abbreviation of the antigen gene
     * @param publicationIDs list of attribution IDs
     */
    public MarkerRelationship updateAntigenGeneForAntibody(String antibodyAntigenGeneRelationshipID, String antigenGeneAbbreviation, Set<String> publicationIDs) {
        MarkerRelationship antibodyAntigenGeneRelationship = getMarkerRepository().getMarkerRelationshipByID(antibodyAntigenGeneRelationshipID);
        if (!antibodyAntigenGeneRelationship.getFirstMarker().getAbbreviation().equals(antigenGeneAbbreviation)) {
            //update relationship with new gene for first marker
            //in this case, update is the same as delete and add.
            String antibodyZdbID = antibodyAntigenGeneRelationship.getSecondMarker().getZdbID();

            MarkerService.deleteMarkerRelationship(antibodyAntigenGeneRelationship);
            return addAntigenGeneForAntibody(antibodyZdbID, antigenGeneAbbreviation, publicationIDs);
        } else {
            return updateAntigenGenePublicationsForAntibodyRelationship(antibodyAntigenGeneRelationship, publicationIDs);
        }
    }

    /**
     * Helper method for updating the attributions for relationship between antibody and antigen gene
     * @param antibodyAntigenGeneRelationship  MarkerRelationship for relationship for antibody and antigen gene
     * @param publicationIDs list of attribution IDs
     */
    private MarkerRelationship updateAntigenGenePublicationsForAntibodyRelationship(MarkerRelationship antibodyAntigenGeneRelationship, Set<String> publicationIDs) {
        List<String> existingPublicationIDs = antibodyAntigenGeneRelationship.getPublications().stream().map(PublicationAttribution::getSourceZdbID).toList();

        Collection<String> idsToAdd = CollectionUtils.subtract(publicationIDs, existingPublicationIDs);
        for(String id : idsToAdd) {
            MarkerService.addMarkerRelationshipAttribution(antibodyAntigenGeneRelationship.getFirstMarker(), antibodyAntigenGeneRelationship.getSecondMarker(), id, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        }

        Collection<String> idsToDelete = CollectionUtils.subtract(existingPublicationIDs, publicationIDs);
        for(String id : idsToDelete) {
            MarkerService.deleteMarkerRelationshipAttribution(antibodyAntigenGeneRelationship.getFirstMarker(), antibodyAntigenGeneRelationship.getSecondMarker(), id, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        }

        return getMarkerRepository().getMarkerRelationshipByID(antibodyAntigenGeneRelationship.getZdbID());
    }


}
