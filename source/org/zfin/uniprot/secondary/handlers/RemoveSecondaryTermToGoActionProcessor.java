package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerGoTermEvidenceRepository;
import static org.zfin.uniprot.secondary.handlers.AddNewSecondaryTermToGoActionCreator.*;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSecondaryTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSecondaryTermToGoActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            switch (action.getDbName()) {
                case INTERPRO -> deleteMarkerGoTermEvidence(action, IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case EC -> deleteMarkerGoTermEvidence(action, EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case UNIPROTKB -> deleteMarkerGoTermEvidence(action, SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                default -> log.error("Unknown marker_go_term_evidence dbname to delete " + action.getDbName());
            }
        }
    }

    private static void deleteMarkerGoTermEvidence(SecondaryTermLoadAction action, String pubID) {
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
