package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.query.NativeQuery;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
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
        log.debug("creating bulk load temp table");
        String sql = "create temp table bulk_marker_go_term_evidence as select * from marker_go_term_evidence where false";
        currentSession().createSQLQuery(sql).executeUpdate();

        //load the data into the bulk table
        log.debug("loading data into bulk load temp table");
        List<List<SecondaryTermLoadAction>> batchedActions = ListUtils.partition(secondaryTermLoadActions, 100);

        for(List<SecondaryTermLoadAction> actions : batchedActions) {
            loadSingleBatchOfMarkerGoTermEvidenceIntoBulkTempTable(actions);
        }

        //set the zdb_id field
        log.debug("generating ZDB IDs");
        sql = "update bulk_marker_go_term_evidence set mrkrgoev_zdb_id = get_id('MRKRGOEV')";
        currentSession().createSQLQuery(sql).executeUpdate();

        //insert the active data
        log.debug("inserting ZDB IDs to active data");
        sql = "insert into zdb_active_data select mrkrgoev_zdb_id from bulk_marker_go_term_evidence";
        currentSession().createSQLQuery(sql).executeUpdate();

        //insert from the bulk table
        log.debug("inserting data from bulk load temp table");
        sql = "insert into marker_go_term_evidence select * from bulk_marker_go_term_evidence";
        currentSession().createSQLQuery(sql).executeUpdate();

        //drop the bulk table
        log.debug("dropping bulk load temp table");
        sql = "drop table bulk_marker_go_term_evidence";
        currentSession().createSQLQuery(sql).executeUpdate();
    }

    /**
     * Builds up a single batch insert query like so:
     *        insert into bulk_marker_go_term_evidence
     *         (
     *             mrkrgoev_mrkr_zdb_id,
     *             mrkrgoev_source_zdb_id,
     *             mrkrgoev_evidence_code,
     *             mrkrgoev_notes,
     *             mrkrgoev_term_zdb_id,
     *             mrkrgoev_annotation_organization,
     *             mrkrgoev_annotation_organization_created_by
     *         ) VALUES
     *         (?, ?, 'IEA', ?, ?, 5, 'ZFIN'),
     *         (?, ?, 'IEA', ?, ?, 5, 'ZFIN'),
     *         (?, ?, 'IEA', ?, ?, 5, 'ZFIN'),
     *         (?, ?, 'IEA', ?, ?, 5, 'ZFIN'),
     *         (?, ?, 'IEA', ?, ?, 5, 'ZFIN')
     *         ...
     * @param actions
     */
    private void loadSingleBatchOfMarkerGoTermEvidenceIntoBulkTempTable(List<SecondaryTermLoadAction> actions) {
        String sqlOuterTemplate = """
                insert into bulk_marker_go_term_evidence 
                (
                    mrkrgoev_mrkr_zdb_id, 
                    mrkrgoev_source_zdb_id,	
                    mrkrgoev_evidence_code,	
                    mrkrgoev_notes,	
                    mrkrgoev_term_zdb_id,
                    mrkrgoev_annotation_organization,
                    mrkrgoev_annotation_organization_created_by
                ) VALUES
                """;

        List<String> sqlInnerTemplates = new ArrayList<>();
        actions.forEach(a -> sqlInnerTemplates.add("(?, ?, 'IEA', ?, ?, 5, 'ZFIN')"));

        String sql = sqlOuterTemplate + String.join(", ", sqlInnerTemplates);
        NativeQuery query = currentSession().createSQLQuery(sql);

        int i = 1;
        for(SecondaryTermLoadAction action : actions) {
            if (action.getRelatedEntityFields() == null) {
                log.error("Related entity fields should not be null for MarkerGoTermEvidenceActionProcessor");
                log.error("Action: " + action);
                throw new RuntimeException("Related entity fields should not be null for MarkerGoTermEvidenceActionProcessor");
            }
            MarkerGoTermEvidenceSlimDTO dto = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());

            query.setParameter(i++, dto.getMarkerZdbID());
            query.setParameter(i++, dto.getPublicationID());
            query.setParameter(i++, getNotesForDBName(action.getDbName()));
            query.setParameter(i++, dto.getGoTermZdbID());

            if (!dto.getPublicationID().equals(getPubIDForDBName(action.getDbName()))) {
                throw new RuntimeException("publication IDs don't match for " + action.getDbName() + " " + dto.getPublicationID() + " " + getPubIDForDBName(action.getDbName()));
            }
        }
        query.executeUpdate();
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
