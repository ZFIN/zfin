package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.uniprot.secondary.AddNewSecondaryTermToGoHandler.*;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSecondaryTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSecondaryTermToGoHandler implements SecondaryLoadHandler {
    private final ForeignDB.AvailableName dbName;
    protected final List<SecondaryTerm2GoTerm> translationRecords;

    public RemoveSecondaryTermToGoHandler() {
        this.dbName = null;
        this.translationRecords = null;
    }

    public RemoveSecondaryTermToGoHandler(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> deletes = actions.stream()
                .filter(action -> dbName.equals(action.getDbName())
                        && action.getType().equals(SecondaryTermLoadAction.Type.DELETE)
                        && action.getSubType().equals(SecondaryTermLoadAction.SubType.DB_LINK))
                .toList();

        log.debug("Joining " + deletes.size()  + " SecondaryLoadAction against " + translationRecords.size() + " " + dbName + " translation records ");

        log.debug("DELETING marker_go_term_evidence");
        //join the load actions to the interpro translation records
        List<Tuple2<SecondaryTermLoadAction, SecondaryTerm2GoTerm>> joined = Seq.seq(deletes)
                .innerJoin(translationRecords,
                        (action, item2go) -> action.getAccession().equals(item2go.dbAccession()))
                .toList();

        for(var joinedRecord : joined) {
            SecondaryTermLoadAction action = joinedRecord.v1();
            SecondaryTerm2GoTerm item2go = joinedRecord.v2();
            SecondaryTermLoadAction newAction = SecondaryTermLoadAction.builder()
                    .accession(action.getAccession())
                    .dbName(dbName)
                    .type(SecondaryTermLoadAction.Type.DELETE)
                    .subType(isSubTypeHandlerFor())
                    .geneZdbID(action.getGeneZdbID())
                    .goID(item2go.goID())
                    .goTermZdbID(item2go.termZdbID())
                    .handlerClass(this.getClass().getName())
                    .build();
            actions.add(newAction);
        }

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

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
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
