package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.ExternalNote;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DBLinkExternalNote;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.uniprot.persistence.UniProtRelease;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

@Getter
@Setter
@Log4j2
public class SecondaryTermLoadService {


    public static final String DBLINK_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;
    public static final String EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-031118-3";
    public static final String IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020724-1";
    public static final String SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020723-1";
    public static final String EXTNOTE_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;


    public static final String EXTNOTE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-47";
    public static final String INTERPRO_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-48";
    public static final String EC_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-49";
    public static final String PFAM_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-50";
    public static final String PROSITE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-51";


    /**
     * Process the actions.
     * Some examples of actions from a run are below (count dbName type subType):
     *   33 InterPro DELETE DB_LINK
     *  851 InterPro LOAD DB_LINK
     *  306 EC LOAD DB_LINK
     *   15 Pfam DELETE DB_LINK
     *  332 Pfam LOAD DB_LINK
     *    9 PROSITE DELETE DB_LINK
     *  233 PROSITE LOAD DB_LINK
     *  635 InterPro LOAD MARKER_GO_TERM_EVIDENCE
     *   16 InterPro DELETE MARKER_GO_TERM_EVIDENCE
     *  400 EC LOAD MARKER_GO_TERM_EVIDENCE
     *  636 UniProtKB LOAD MARKER_GO_TERM_EVIDENCE
     *  124 UniProtKB DELETE MARKER_GO_TERM_EVIDENCE
     * 25983 null LOAD EXTERNAL_NOTE
     *
     * @param actions list of actions to perform
     */
    public static void processActions(List<SecondaryTermLoadAction> actions, UniProtRelease release) {
        //groupBy the actions
        Map<SecondaryTermLoadAction.Type, List<SecondaryTermLoadAction>> groupedByType = actions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getType));

        //process the actions
        currentSession().beginTransaction();
        for(SecondaryTermLoadAction.Type type : groupedByType.keySet()) {
            log.debug("Processing action types of " + type);
            List<SecondaryTermLoadAction> transactionActions = groupedByType.get(type);
            processActionsByType(type, transactionActions);
            log.debug("Finished action types of " + type);
        }
        if (release != null) {
            if (release.getProcessedDate() == null) {
                log.error("Release ID# " + release.getUpr_id() + " has not been processed yet. Must process before secondary terms.");
                currentSession().getTransaction().rollback();
                System.exit(5);
            }
            release.setSecondaryLoadDate(new Date());
            getInfrastructureRepository().updateUniProtRelease(release);
        }
        log.debug("Committing changes");
        currentSession().getTransaction().commit();
    }

    private static void processActionsByType(SecondaryTermLoadAction.Type type, List<SecondaryTermLoadAction> transactionActions) {
        switch (type) {
            case LOAD -> processLoadActions(transactionActions);
            case DELETE -> processDeleteActions(transactionActions);
            default -> log.error("Unknown action type: " + type);
        }
    }

    private static void processLoadActions(List<SecondaryTermLoadAction> transactionActions) {
        //groupBy the actions by subtype
        Map<SecondaryTermLoadAction.SubType, List<SecondaryTermLoadAction>> groupedBySubType =
                transactionActions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getSubType));

        for(SecondaryTermLoadAction.SubType subType : groupedBySubType.keySet()) {
            log.debug("Processing action subtypes of " + subType);
            List<SecondaryTermLoadAction> subTypeActions = groupedBySubType.get(subType);
            processLoadActionsBySubType(subType, subTypeActions);
            log.debug("Finished action subtypes of " + subType);
        }
    }

    private static void processLoadActionsBySubType(SecondaryTermLoadAction.SubType subType, List<SecondaryTermLoadAction> subTypeActions) {
        switch (subType) {
            case DB_LINK -> processDbLinkLoadActions(subTypeActions);
            case MARKER_GO_TERM_EVIDENCE -> processMarkerGoTermEvidenceLoadActions(subTypeActions);
            case EXTERNAL_NOTE -> processExternalNoteLoadActions(subTypeActions);
            default -> log.error("Unknown action subtype: " + subType);
        }
    }

    private static void processDbLinkLoadActions(List<SecondaryTermLoadAction> subTypeActions) {
        List<Marker> markers = getMarkerRepository().getMarkersByZdbIDs(subTypeActions.stream().map(SecondaryTermLoadAction::getGeneZdbID).toList());
        Map<String, Marker> markerMap = markers.stream().collect(Collectors.toMap(Marker::getZdbID, marker -> marker));
        List<MarkerDBLink> dblinks = new ArrayList<>();

        for(SecondaryTermLoadAction action : subTypeActions) {
            log.debug("Loading " + action.getDbName() + " dblink for " + action.getGeneZdbID() + " " + action.getAccession());

            Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
            MarkerDBLink newLink = new MarkerDBLink();
            newLink.setAccessionNumber(action.getAccession());
            newLink.setMarker(marker);
            newLink.setReferenceDatabase(getReferenceDatabaseForAction(action));
            newLink.setLength(action.getLength());
            newLink.setLinkInfo(getDBLinkInfo());
            dblinks.add(newLink);
        }
        Publication publication = getPublicationRepository().getPublication(DBLINK_PUBLICATION_ATTRIBUTION_ID);
        getSequenceRepository().addDBLinks(dblinks, publication, 50);
    }

    private static ReferenceDatabase getReferenceDatabaseForAction(SecondaryTermLoadAction action) {
        return getReferenceDatabase(getReferenceDatabaseIDForAction(action));
    }

    private static String getReferenceDatabaseIDForAction(SecondaryTermLoadAction action) {
        String referenceDatabaseID = null;
        switch (action.getDbName()) {
            case INTERPRO -> referenceDatabaseID = INTERPRO_REFERENCE_DATABASE_ID;
            case EC -> referenceDatabaseID = EC_REFERENCE_DATABASE_ID;
            case PFAM -> referenceDatabaseID = PFAM_REFERENCE_DATABASE_ID;
            case PROSITE -> referenceDatabaseID = PROSITE_REFERENCE_DATABASE_ID;
            default -> log.error("Unknown dblink dbname to load " + action.getDbName());
        }
        return referenceDatabaseID;
    }

    private static void processMarkerGoTermEvidenceLoadActions(List<SecondaryTermLoadAction> subTypeActions) {
        for(SecondaryTermLoadAction action : subTypeActions) {
            switch (action.getDbName()) {
                case INTERPRO -> loadMarkerGoTermEvidence(action, IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case EC -> loadMarkerGoTermEvidence(action, EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case UNIPROTKB -> loadMarkerGoTermEvidence(action, SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                default -> log.error("Unknown marker_go_term_evidence dbname to load " + action.getDbName());
            }
        }
    }

    private static void processExternalNoteLoadActions( List<SecondaryTermLoadAction> subTypeActions) {
        log.debug("disabling external note load actions");
//        for(SecondaryTermLoadAction action : subTypeActions) {
//            loadOrUpdateExternalNote(action);
//        }
    }

    private static void processDeleteActions( List<SecondaryTermLoadAction> actions) {
        //groupBy the actions by subtype
        Map<SecondaryTermLoadAction.SubType, List<SecondaryTermLoadAction>> groupedBySubType =
                actions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getSubType));

        for(SecondaryTermLoadAction.SubType subType : groupedBySubType.keySet()) {
            log.debug("Processing action subtypes of " + subType);
            List<SecondaryTermLoadAction> subTypeActions = groupedBySubType.get(subType);
            processDeleteActionsBySubType(subType, subTypeActions);
            log.debug("Finished action subtypes of " + subType);
        }
    }

    private static void processDeleteActionsBySubType(SecondaryTermLoadAction.SubType subType, List<SecondaryTermLoadAction> subTypeActions) {
        switch (subType) {
            case DB_LINK -> processDbLinkDeleteActions(subTypeActions);
            case MARKER_GO_TERM_EVIDENCE -> processMarkerGoTermEvidenceDeleteActions(subTypeActions);
            case EXTERNAL_NOTE -> processExternalNoteDeleteActions(subTypeActions);
            default -> log.error("Unknown action subtype: " + subType);
        }
    }

    private static void processDbLinkDeleteActions(List<SecondaryTermLoadAction> actions) {
        List<DBLink> dblinksToDelete = new ArrayList<>();
        for(SecondaryTermLoadAction action : actions) {
            DBLink dblink = getSequenceRepository().getDBLinkByReferenceDatabaseID(action.getGeneZdbID(), action.getAccession(), getReferenceDatabaseIDForAction(action));
            if (dblink == null) {
                log.error("Error deleting dblink (none found by attributes): " + action.getGeneZdbID() + " " + action.getAccession() + " " + action.getDbName() + " " + action.getSubType() + " " + getReferenceDatabaseIDForAction(action));
                continue;
            }
            System.err.println("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
            log.debug("Removing dblink: " + dblink.getZdbID());
            getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
            dblinksToDelete.add(dblink);
        }
        getSequenceRepository().removeDBLinks(dblinksToDelete);
    }

    private static void processMarkerGoTermEvidenceDeleteActions(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            switch (action.getDbName()) {
                case INTERPRO -> deleteMarkerGoTermEvidence(action, IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case EC -> deleteMarkerGoTermEvidence(action, EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                case UNIPROTKB -> deleteMarkerGoTermEvidence(action, SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID);
                default -> log.error("Unknown marker_go_term_evidence dbname to delete " + action.getDbName());
            }
        }
    }

    private static void processExternalNoteDeleteActions(List<SecondaryTermLoadAction> actions) {
        log.debug("disabling external note delete actions");
//        for(SecondaryTermLoadAction action : actions) {
//            deleteExternalNote (action);
//        }
    }

    private static void deleteExternalNote(SecondaryTermLoadAction action) {
        log.debug("Processing delete external note action: " + action);
        String externalNoteZdbID = action.getDetails();

        getInfrastructureRepository().deleteRecordAttributionsForData(externalNoteZdbID);
        getInfrastructureRepository().deleteDBLinkExternalNote(externalNoteZdbID);
    }

    private static void loadMarkerGoTermEvidence(SecondaryTermLoadAction action, String pubID)  {
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();
        markerGoTermEvidence.setExternalLoadDate(null);

        GafOrganization uniprotGafOrganization = getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.UNIPROT);
        markerGoTermEvidence.setGafOrganization(uniprotGafOrganization);

        markerGoTermEvidence.setOrganizationCreatedBy(GafOrganization.OrganizationEnum.ZFIN.name());

        Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
        markerGoTermEvidence.setMarker(marker);

        GenericTerm goTerm = HibernateUtil.currentSession().get(GenericTerm.class, action.getGoTermZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);

        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubID);
        markerGoTermEvidence.setSource(publication);


        GoEvidenceCode goEvidenceCode = getMarkerGoTermEvidenceRepository().getGoEvidenceCode(GoEvidenceCodeEnum.IEA.name());
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        switch(action.getDbName()) {
            case INTERPRO -> markerGoTermEvidence.setNote("ZFIN InterPro 2 GO");
            case EC -> markerGoTermEvidence.setNote("ZFIN EC acc 2 GO");
            case UNIPROTKB -> markerGoTermEvidence.setNote("ZFIN SP keyword 2 GO");
            default -> log.error("Unknown marker_go_term_evidence dbname to load " + action.getDbName());
        }

        Date rightNow = new Date();
        markerGoTermEvidence.setModifiedWhen(rightNow);
        markerGoTermEvidence.setCreatedWhen(rightNow);

        getMarkerGoTermEvidenceRepository().addEvidence(markerGoTermEvidence, false);

        getMutantRepository().addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, action.getPrefixedAccession());

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

    private static void loadOrUpdateExternalNote(SecondaryTermLoadAction action) {
        DBLink relatedDBLink = getSequenceRepository().getDBLinkByReferenceDatabaseID(action.getGeneZdbID(), action.getAccession(), EXTNOTE_REFERENCE_DATABASE_ID);
        if (relatedDBLink == null) {
            log.error("Could not find related dblink for " + action.getGeneZdbID() + " " + action.getAccession());
            return;
        }

        List<DBLinkExternalNote> existingNotes = getInfrastructureRepository()
                .getDBLinkExternalNoteByDataZdbIDAndPublicationID(relatedDBLink.getZdbID(), EXTNOTE_PUBLICATION_ATTRIBUTION_ID);

        if (existingNotes == null || existingNotes.size() == 0) {
            log.debug("Loading external note for " + action.getGeneZdbID() + " " + action.getAccession());
            loadExternalNote(action, relatedDBLink);
        } else if (existingNotes.size() == 1) {
            DBLinkExternalNote firstNote = existingNotes.get(0);
            if (firstNote.getNote().equals(action.getDetails())) {
                log.debug("Note already exists for " + action.getGeneZdbID() + " " + action.getAccession());
            } else {
                log.debug("Updating note for " + firstNote.getZdbID() + " " + firstNote.getExternalDataZdbID() + " " + action.getGeneZdbID() + " " + action.getAccession());
                updateExternalNote(firstNote, action.getDetails());
            }
        } else {
            log.error("More than one existing note for " + action.getGeneZdbID() + " " + action.getAccession());
            log.error("Cannot determine which note to update");
            System.exit(4);
        }
    }

    private static void updateExternalNote(ExternalNote firstNote, String details) {
        getInfrastructureRepository().updateExternalNoteWithoutUpdatesLog(firstNote, details);
    }

    private static void loadExternalNote(SecondaryTermLoadAction action, DBLink relatedDBLink) {
        getInfrastructureRepository().addDBLinkExternalNote(relatedDBLink, action.getDetails(), EXTNOTE_PUBLICATION_ATTRIBUTION_ID);
    }

    public static ReferenceDatabase getReferenceDatabase(String referenceDatabaseID) {
        return getSequenceRepository().getReferenceDatabaseByID(referenceDatabaseID);
    }

    private static String getDBLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }
}
