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
import java.util.Map;
import java.util.stream.Collectors;

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
    public void processActions(List<SecondaryTermLoadAction> subTypeActions, SecondaryTermLoadAction.Type type) {

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
        for(SecondaryTermLoadAction action : secondaryTermLoadActions) {
            loadSingleMarkerGoTermEvidence(action);
        }
    }

    private void bulkProcessDeleteActions(List<SecondaryTermLoadAction> secondaryTermLoadActions) {
        for(SecondaryTermLoadAction action : secondaryTermLoadActions) {
            deleteSingleMarkerGoTermEvidence(action);
        }
    }

    private static void loadSingleMarkerGoTermEvidence(SecondaryTermLoadAction action)  {
        MarkerGoTermEvidenceSlimDTO dto = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());
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
        getMutantRepository().addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, dto.getInferredFrom());
    }

    private void deleteSingleMarkerGoTermEvidence(SecondaryTermLoadAction action) {
        MarkerGoTermEvidenceSlimDTO dto = MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields());
        String sql = """
                DELETE FROM marker_go_term_evidence
                USING inference_group_member
                WHERE mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
                AND infgrmem_inferred_from = :inferred_from
                AND mrkrgoev_mrkr_zdb_id = :mrkrgoev_mrkr_zdb_id
                AND mrkrgoev_term_zdb_id = :mrkrgoev_term_zdb_id
                AND mrkrgoev_source_zdb_id = :mrkrgoev_source_zdb_id
                """;

        currentSession().createNativeQuery(sql)
                .setParameter("inferred_from", dto.getInferredFrom())
                .setParameter("mrkrgoev_mrkr_zdb_id", dto.getMarkerZdbID())
                .setParameter("mrkrgoev_term_zdb_id", dto.getGoTermZdbID())
                .setParameter("mrkrgoev_source_zdb_id", dto.getPublicationID())
                .executeUpdate();
    }
}
