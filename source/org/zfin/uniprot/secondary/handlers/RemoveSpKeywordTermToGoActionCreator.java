package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTerm2GoTerm;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;
import static org.zfin.uniprot.secondary.handlers.MarkerGoTermEvidenceActionCreator.filterTerms;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSpKeywordTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSpKeywordTermToGoActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }
    private final List<SecondaryTerm2GoTerm> translationRecords;

    private final ForeignDB.AvailableName dbName;

    public RemoveSpKeywordTermToGoActionCreator(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.info("Creating actions to remove SPKW terms");
        if (this.dbName != UNIPROTKB) {
            log.error("Not a SPKW term");
            throw new RuntimeException("Not a SPKW term");
        }

        //existing SPKW terms
        List<MarkerGoTermEvidenceSlimDTO> existingRecords = context.getExistingMarkerGoTermEvidenceRecords(this.dbName);
        //TODO: filter to SPKW????

        //these are all the marker_go_term_evidence entries that have the SPKW terms based on the parsed uniprot records
        List<SecondaryTermLoadAction> newMarkerGoTermEvidenceLoadActions = AddNewSpKeywordTermToGoActionCreator
                .createMarkerGoTermEvidenceLoadActionsFromUniprotKeywords(uniProtRecords, context, this.translationRecords);

        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences = filterTerms(newMarkerGoTermEvidenceLoadActions);

        //difference:
        //existingRecords - newMarkerGoTermEvidenceLoadActions
        //this is the list of marker_go_term_evidence entries that need to be removed
        List<MarkerGoTermEvidenceSlimDTO> recordsToRemove = existingRecords.stream().filter(
                record -> !recordsToPersistContainsExistingRecord(filteredMarkerGoTermEvidences, record)
        ).toList();

        //create actions to remove marker_go_term_evidence entries
        return createMarkerGoTermEvidenceLoadActionsToRemove(recordsToRemove);
    }

    private List<SecondaryTermLoadAction> createMarkerGoTermEvidenceLoadActionsToRemove(List<MarkerGoTermEvidenceSlimDTO> recordsToRemove) {
        return recordsToRemove.stream().map(
                record -> SecondaryTermLoadAction.builder()
                        .type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                        .dbName(UNIPROTKB)
                        .accession(record.getGoID())
                        .relatedEntityFields(record.toMap())
                        .geneZdbID(record.getMarkerZdbID())
                        .build()
        ).toList();
    }

    public static boolean recordsToPersistContainsExistingRecord(List<SecondaryTermLoadAction> recordsToPersist, MarkerGoTermEvidenceSlimDTO record) {
        return recordsToPersist.stream().anyMatch(action -> {
            String goID = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()).getGoID();
            String markerZdbID = action.getGeneZdbID();
            boolean match = record.getGoID().equals(goID) && record.getMarkerZdbID().equals(markerZdbID);
            return match;
        });
    }
}
