package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.ExternalNote;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DBLinkExternalNote;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkExternalNoteSlimDTO;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.uniprot.secondary.SecondaryTermLoadService.EXTNOTE_PUBLICATION_ATTRIBUTION_ID;
import static org.zfin.uniprot.secondary.SecondaryTermLoadService.EXTNOTE_REFERENCE_DATABASE_ID;
import static org.zfin.util.ZfinStringUtils.isEqualIgnoringWhiteSpace;

/**
 * Creates actions for new external notes and also for deleting external notes
 * UPDATE: 11/11/2023 - This is preserved from when we used to display this information. We will likely deprecate this
 * since it is not displayed anywhere.
 */
@Log4j2
public class ExternalNotesHandler implements SecondaryLoadHandler {

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.debug("disabling external notes for now");
        //use the "realHandle" method to re-enable this
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if(action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                loadOrUpdateExternalNote(action);
            } else if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                deleteExternalNoteForAction(action);
            }
        }
    }

    private void deleteExternalNoteForAction(SecondaryTermLoadAction action) {
        log.debug("Processing delete external note action: " + action);
        String externalNoteZdbID = action.getDetails();

        getInfrastructureRepository().deleteRecordAttributionsForData(externalNoteZdbID);
        getInfrastructureRepository().deleteDBLinkExternalNote(externalNoteZdbID);
    }

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.EXTERNAL_NOTE;
    }

    public void realHandle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> secondaryTermLoadActions = new ArrayList<>();
        Set<String> uniprotAccessions = uniProtRecords.keySet();
        List<String> unmatchedUniprots = new ArrayList<>();
        List<SecondaryTermLoadAction> calculatedNotesThatAlreadyExist = new ArrayList<>();

        for(String uniprot : uniprotAccessions) {
            RichSequenceAdapter record = uniProtRecords.get(uniprot);
            List<String> comments = record.getComments();
            if (comments == null || comments.isEmpty()) {
                continue;
            }
            String combinedComment = String.join("<br>", comments)
                                            .replaceAll("\\n +", " ");
            if (StringUtils.isEmpty(combinedComment)) {
                continue;
            }

            List<DBLinkSlimDTO> genesMatchingUniprot = context.getGeneByUniprot(uniprot);
            if(genesMatchingUniprot == null || genesMatchingUniprot.isEmpty()) {
                unmatchedUniprots.add(uniprot);
                continue;
            }
            String firstGeneZdbID = genesMatchingUniprot.get(0).getDataZdbID();

            //TODO: handle multiple gene matches?
            SecondaryTermLoadAction action = SecondaryTermLoadAction.builder()
                    .geneZdbID(firstGeneZdbID)
                    .accession(uniprot)
                    .details(combinedComment)
                    .type(SecondaryTermLoadAction.Type.LOAD)
                    .subType(SecondaryTermLoadAction.SubType.EXTERNAL_NOTE)
                    .handlerClass(this.getClass().getName())
                    .build();

            DBLinkExternalNoteSlimDTO existingNote = context.getExternalNoteByGeneAndAccession(firstGeneZdbID, uniprot);
            if(existingNote != null && existingNote.getNote().equals(combinedComment)) {
                calculatedNotesThatAlreadyExist.add(action);
            } else if (existingNote != null && isEqualIgnoringWhiteSpace(existingNote.getNote(), combinedComment)  ) {
                calculatedNotesThatAlreadyExist.add(action);
            } else if (existingNote != null) {
                log.debug("Updating external note for " + uniprot + "/" + firstGeneZdbID + " because it has changed (" + existingNote.getDblinkZdbID() + ")");
                secondaryTermLoadActions.add(action);
                calculatedNotesThatAlreadyExist.add(action);
            } else {
                secondaryTermLoadActions.add(action);
            }
        }

        //batch log unmatched uniprots
        if(!unmatchedUniprots.isEmpty()) {
            log.info("Unmatched uniprots (" + unmatchedUniprots.size() + "): " );

            //break up into chunks of 100
            ListUtils.partition(unmatchedUniprots, 20)
                    .forEach(chunk -> log.info(String.join(", ", chunk)));
        }

        actions.addAll(secondaryTermLoadActions);

        List<SecondaryTermLoadAction> deleteActions = calculateDeletedNotes(context, calculatedNotesThatAlreadyExist);
        actions.addAll(deleteActions);
    }

    private List<SecondaryTermLoadAction> calculateDeletedNotes(SecondaryLoadContext context, List<SecondaryTermLoadAction> calculatedNotesThatAlreadyExist) {
        Set<DBLinkExternalNoteSlimDTO> allExistingNotes = new HashSet<>(context.getAllExternalNotes());
        log.debug("Found " + allExistingNotes.size() + " existing external notes");

        //remove from the existing notes all the notes that we have calculated that should exist
        //any that remain are notes that should be deleted
        allExistingNotes.removeIf(note -> calculatedNotesThatAlreadyExist.stream()
                .anyMatch(calculatedNote -> calculatedNote.getAccession().equals(note.getAccession())
                        && calculatedNote.getGeneZdbID().equals(note.getGeneZdbID()))
        );

        log.debug("Number that should be deleted: " + allExistingNotes.size());

        return allExistingNotes.stream().map(note ->
            SecondaryTermLoadAction.builder()
                    .geneZdbID(note.getGeneZdbID())
                    .accession(note.getAccession())
                    .details(note.getZdbID())
                    .type(SecondaryTermLoadAction.Type.DELETE)
                    .subType(SecondaryTermLoadAction.SubType.EXTERNAL_NOTE)
                    .build()
        ).toList();
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

}
