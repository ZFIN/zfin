package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkExternalNoteSlimDTO;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;

import static org.zfin.util.ZfinStringUtils.isEqualIgnoringWhiteSpace;

/**
 * Creates actions for new external notes and also for deleting external notes
 */
@Log4j2
public class ExternalNotesHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.debug("disabling external notes for now");
        //use the "realHandle" method to re-enable this
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

}
