package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.infrastructure.ActiveData;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.persistence.BatchProcessor;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;

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

        //group by subtype
        Map<SecondaryTermLoadAction.Type, List<SecondaryTermLoadAction>> groupedActions = subTypeActions.stream()
                .collect(Collectors.groupingBy(SecondaryTermLoadAction::getType));

        //assert that there are only 2 types max
        if (groupedActions.keySet().size() > 2) {
            throw new RuntimeException("There should only be 2 types of actions for MarkerGoTermEvidenceActionProcessor");
        }

        //process the delete actions first
        if (groupedActions.containsKey(SecondaryTermLoadAction.Type.DELETE)) {
            bulkProcessDeleteActions(groupedActions.get(SecondaryTermLoadAction.Type.DELETE));
        }

        //process the load actions
        if (groupedActions.containsKey(SecondaryTermLoadAction.Type.LOAD)) {
            bulkProcessLoadActions(groupedActions.get(SecondaryTermLoadAction.Type.LOAD));
        }

        currentSession().flush();
    }

    private void bulkProcessLoadActions(List<SecondaryTermLoadAction> secondaryTermLoadActions) {
        BatchProcessor batchProcessor = new BatchProcessor(
                "marker_go_term_evidence",
                "mrkrgoev_zdb_id",
                ActiveData.Type.MRKRGOEV.name(),
                secondaryTermLoadActions.stream().map( action -> {
                    MarkerGoTermEvidenceSlimDTO dto = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());

                    //assert dto.getPublicationID().equals ex
                    if (!dto.getPublicationID().equals(getPubIDForDBName(action.getDbName()))) {
                        throw new RuntimeException("Publication ID mismatch for " + action.getDbName() + " " + dto.getPublicationID() + " " + getPubIDForDBName(action.getDbName()));
                    }

                    Map<String, Object> rowValues = new HashMap<>();
                    rowValues.put("mrkrgoev_mrkr_zdb_id", dto.getMarkerZdbID());
                    rowValues.put("mrkrgoev_source_zdb_id", dto.getPublicationID());
                    rowValues.put("mrkrgoev_evidence_code", "IEA");
                    rowValues.put("mrkrgoev_notes", getNotesForDBName(action.getDbName()));
                    rowValues.put("mrkrgoev_term_zdb_id", dto.getGoTermZdbID());
                    rowValues.put("mrkrgoev_annotation_organization", 5);
                    rowValues.put("mrkrgoev_annotation_organization_created_by", "ZFIN");
                    return rowValues;
                }).toList()
            );
        batchProcessor.execute();
    }

    private void bulkProcessDeleteActions(List<SecondaryTermLoadAction> secondaryTermLoadActions) {
        for(SecondaryTermLoadAction action : secondaryTermLoadActions) {
            deleteSingleMarkerGoTermEvidence(action);
        }
    }

    private void deleteSingleMarkerGoTermEvidence(SecondaryTermLoadAction action) {
        MarkerGoTermEvidenceSlimDTO dto = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());
        String sql = """
                delete from marker_go_term_evidence
                where mrkrgoev_mrkr_zdb_id = :mrkrgoev_mrkr_zdb_id
                and mrkrgoev_term_zdb_id = :mrkrgoev_term_zdb_id
                and mrkrgoev_source_zdb_id = :mrkrgoev_source_zdb_id
                """;
        currentSession().createSQLQuery(sql)
                .setParameter("mrkrgoev_mrkr_zdb_id", dto.getMarkerZdbID())
                .setParameter("mrkrgoev_term_zdb_id", dto.getGoTermZdbID())
                .setParameter("mrkrgoev_source_zdb_id", dto.getPublicationID())
                .executeUpdate();
    }


    private String getNotesForDBName(ForeignDB.AvailableName dbName) {
        return switch(dbName) {
            case INTERPRO -> "ZFIN InterPro 2 GO";
            case EC -> "ZFIN EC acc 2 GO";
            case UNIPROTKB -> "ZFIN SP keyword 2 GO";
            default -> throw new IllegalStateException("Unexpected value: " + dbName);
        };
    }

    private String getPubIDForDBName(ForeignDB.AvailableName dbName) {
        return switch(dbName) {
            case INTERPRO -> IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            case EC -> EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            case UNIPROTKB -> SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            default -> throw new IllegalStateException("Unexpected value: " + dbName);
        };
    }
}
