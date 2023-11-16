package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTerm2GoTerm;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;

import static org.zfin.uniprot.secondary.handlers.AddNewSecondaryTermToGoActionCreator.filterTerms;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSpKeywordTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSpKeywordTermToGoActionCreator extends RemoveSecondaryTermToGoActionCreator {
    private static final ForeignDB.AvailableName FOREIGN_DB_NAME = ForeignDB.AvailableName.UNIPROTKB;

    public RemoveSpKeywordTermToGoActionCreator(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        super(dbName, translationRecords);
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.debug("Creating actions to remove SPKW terms");

        //existing SPKW terms
        List<MarkerGoTermEvidence> existingRecords = context.getExistingMarkerGoTermEvidenceRecordsForSPKW();

        //these are all the marker_go_term_evidence entries that have the SPKW terms based on the parsed uniprot records
        List<SecondaryTermLoadAction> newMarkerGoTermEvidenceLoadActions = AddNewSpKeywordTermToGoActionCreator
                .createMarkerGoTermEvidenceLoadActionsFromUniprotKeywords(uniProtRecords, context, this.translationRecords);

        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences = filterTerms(newMarkerGoTermEvidenceLoadActions);

        //difference:
        //existingRecords - newMarkerGoTermEvidenceLoadActions
        //this is the list of marker_go_term_evidence entries that need to be removed
        List<MarkerGoTermEvidence> recordsToRemove = existingRecords.stream().filter(
                record -> !recordsToPersistContainsExistingRecord(filteredMarkerGoTermEvidences, record)
        ).toList();

        //create actions to remove marker_go_term_evidence entries
        return createMarkerGoTermEvidenceLoadActionsToRemove(recordsToRemove);
    }

    private List<SecondaryTermLoadAction> createMarkerGoTermEvidenceLoadActionsToRemove(List<MarkerGoTermEvidence> recordsToRemove) {
        return recordsToRemove.stream().map(
                record -> SecondaryTermLoadAction.builder()
                        .type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                        .dbName(FOREIGN_DB_NAME)
                        .accession(record.getGoTerm().getOboID())
                        .goID(record.getGoTerm().getOboID())
                        .goTermZdbID(record.getGoTerm().getZdbID())
                        .geneZdbID(record.getMarker().getZdbID())
                        .build()
        ).toList();
    }

    public static boolean recordsToPersistContainsExistingRecord(List<SecondaryTermLoadAction> recordsToPersist, MarkerGoTermEvidence record) {
        return recordsToPersist.stream().anyMatch(action -> {
            String goID = "GO:" + action.getGoID();
            String markerZdbID = action.getGeneZdbID();
            boolean match = record.getGoTerm().getOboID().equals(goID) && record.getMarker().getZdbID().equals(markerZdbID);
            return match;
        });
    }
}
