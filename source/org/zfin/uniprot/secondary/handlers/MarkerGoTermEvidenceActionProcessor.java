package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.Date;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Adds to marker_go_term_evidence table
 * It's based on the new entries that are being added as a result of the AddNewFromUniProtsHandler
 * New accessions that are being loaded as a result of that load will then get processed here
 * and new entries in the marker_go_term_evidence table will be created.
 * This uses *2go translation files for converting EC, InterPro, UniProt Keywords(SPKW) to GO terms.
 * (actually, since UniProt Keywords require special handling, that logic is in AddNewSpKeywordTermToGoHandler
 */
@Log4j2
public class MarkerGoTermEvidenceActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }
    public static final String EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-031118-3";
    public static final String IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020724-1";
    public static final String SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020723-1";

    @Override
    public void processActions(List<SecondaryTermLoadAction> subTypeActions) {
        int i = 0;
        for(SecondaryTermLoadAction action : subTypeActions) {
            i++;
            if (i == 1000) {
                log.info("processed " + i + " of " + subTypeActions.size());
                i = 0;
            }
            if (action.getType().equals(SecondaryTermLoadAction.Type.LOAD)) {
                loadMarkerGoTermEvidence(action);
            }
            else if (action.getType().equals(SecondaryTermLoadAction.Type.DELETE)) {
                deleteMarkerGoTermEvidence(action);
            }
        }
        currentSession().flush();
    }

    private static void loadMarkerGoTermEvidence(SecondaryTermLoadAction action)  {
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();
        markerGoTermEvidence.setExternalLoadDate(null);

        GafOrganization uniprotGafOrganization = getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.UNIPROT);
        markerGoTermEvidence.setGafOrganization(uniprotGafOrganization);

        markerGoTermEvidence.setOrganizationCreatedBy(GafOrganization.OrganizationEnum.ZFIN.name());

        Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
        markerGoTermEvidence.setMarker(marker);

        GenericTerm goTerm = HibernateUtil.currentSession().get(GenericTerm.class, action.getGoTermZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);

        GoEvidenceCode goEvidenceCode = getMarkerGoTermEvidenceRepository().getGoEvidenceCode(GoEvidenceCodeEnum.IEA.name());
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        String pubID = null;
        switch(action.getDbName()) {
            case INTERPRO -> {
                markerGoTermEvidence.setNote("ZFIN InterPro 2 GO");
                pubID = IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            case EC -> {
                markerGoTermEvidence.setNote("ZFIN EC acc 2 GO");
                pubID = EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            case UNIPROTKB -> {
                markerGoTermEvidence.setNote("ZFIN SP keyword 2 GO");
                pubID = SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            default -> log.error("Unknown marker_go_term_evidence dbname to load " + action.getDbName());
        }

        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubID);
        markerGoTermEvidence.setSource(publication);

        Date rightNow = new Date();
        markerGoTermEvidence.setModifiedWhen(rightNow);
        markerGoTermEvidence.setCreatedWhen(rightNow);
        getMarkerGoTermEvidenceRepository().addEvidence(markerGoTermEvidence, false);
        getMutantRepository().addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, action.getPrefixedAccession());
    }

    private static void deleteMarkerGoTermEvidence(SecondaryTermLoadAction action) {
        MarkerGoTermEvidenceSlimDTO markerGoTermEvidenceDTO = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());
        String pubID = markerGoTermEvidenceDTO.getPublicationID();
        log.debug("Removing " + action.getDbName() + " marker_go_term_evidence for " + action.getGeneZdbID() + " " + action.getGoTermZdbID() + " " + pubID );
        List<MarkerGoTermEvidence> markerGoTermEvidences = getMarkerGoTermEvidenceRepository().getMarkerGoTermEvidencesForMarkerZdbID(action.getGeneZdbID());
        if (markerGoTermEvidences.size() == 0) {
            log.debug("No marker_go_term_evidence found to delete");
            return;
        }

        List<MarkerGoTermEvidence> toDelete = markerGoTermEvidences.stream()
                .filter(markerGoTermEvidence -> pubID.equals(markerGoTermEvidence.getSource().getZdbID()))
                .filter(markerGoTermEvidence -> action.getGoTermZdbID().equals(markerGoTermEvidence.getGoTerm().getZdbID()))
                .toList();
        List<String> toDeleteIDs = toDelete.stream().map(MarkerGoTermEvidence::getZdbID).toList();

        if (toDeleteIDs.size() > 1) {
            log.info("Found more than one marker_go_term_evidence to delete: " + toDeleteIDs + ". Deleting all...");
        } else if (toDeleteIDs.size() == 0) {
            log.debug("No marker_go_term_evidence found to delete after filtering");
            return;
        }
        log.debug("Found the following marker_go_term_evidence to delete: " + toDeleteIDs);

        getMarkerGoTermEvidenceRepository().deleteMarkerGoTermEvidenceByZdbIDs(toDeleteIDs);
    }

}
