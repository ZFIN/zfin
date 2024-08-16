package org.zfin.construct.presentation;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.InvalidConstructNameException;
import org.zfin.construct.name.ConstructName;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.construct.presentation.ConstructComponentService.getExistingConstructName;

@Service
@Log4j2
public class ConstructEditService {
    private static final ConstructRepository cr = RepositoryFactory.getConstructRepository();
    private static final MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static final PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static final InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static final SequenceRepository sr = RepositoryFactory.getSequenceRepository();


    List<MarkerNameAndZdbId> getExistingSynonymsAsMarkerNameAndZdbIds(String constructID) {
        List<MarkerNameAndZdbId> existingSynonyms = mr.getPreviousNamesLight(mr.getMarkerByID(constructID)).stream().map(
                previousNameLight -> {
                    MarkerNameAndZdbId mnzdb = new MarkerNameAndZdbId();
                    mnzdb.setZdbID(previousNameLight.getAliasZdbID());
                    mnzdb.setLabel(previousNameLight.getAlias());
                    return mnzdb;
                }).toList();
        return existingSynonyms;
    }

    public void removeSequenceFromConstruct(String constructID, String sequenceID) {
        Session session = HibernateUtil.currentSession();
        Marker m = mr.getMarkerByID(constructID);
        ir.deleteActiveDataByZdbID(sequenceID);
        String hql = "delete from MarkerDBLink dbl where dbl.id = :sequenceID";
        Query query = session.createQuery(hql);
        query.setParameter("sequenceID", sequenceID);
        query.executeUpdate();
        ir.insertUpdatesTable(m,"sequence","deleted sequence");
    }

    public List<MarkerNameAndZdbId> calculateAddedNotes(String constructID, List<MarkerNameAndZdbId> notes) {
        List<MarkerNameAndZdbId> existingNotes = getExistingNotesAsMarkerNameAndZdbIds(constructID);
        List<MarkerNameAndZdbId> addedNotes = notes.stream().filter(note -> note.getZdbID() == null).toList();
        List<MarkerNameAndZdbId> addedNotesWithoutDuplicatesOfExisting = addedNotes.stream().filter(
                addedNote -> existingNotes.stream().noneMatch(
                        existingNote -> existingNote.getLabel().equals(addedNote.getLabel())
                )
        ).toList();

        return addedNotesWithoutDuplicatesOfExisting;
    }

    public List<MarkerNameAndZdbId> calculateRemovedNotes(String constructID, List<MarkerNameAndZdbId> notes) {
        List<MarkerNameAndZdbId> existingNotes = getExistingNotesAsMarkerNameAndZdbIds(constructID);
        List<MarkerNameAndZdbId> removedNotes = new ArrayList<>(CollectionUtils.subtract(existingNotes, notes));
        return removedNotes;
    }

    private List<MarkerNameAndZdbId> getExistingNotesAsMarkerNameAndZdbIds(String constructID) {
        return DTOMarkerService.getCuratorNoteDTOs(mr.getMarkerByID(constructID)).stream().map(
                note -> {
                    MarkerNameAndZdbId mnzdb = new MarkerNameAndZdbId();
                    mnzdb.setZdbID(note.getZdbID());
                    mnzdb.setLabel(note.getNoteData());
                    return mnzdb;
                }).toList();
    }

    public List<MarkerNameAndZdbId> calculateAddedSequences(String constructID, List<MarkerNameAndZdbId> sequences) {
        List<MarkerNameAndZdbId> existingSequences = getExistingSequencesAsMarkerNameAndZdbIds(constructID);
        List<MarkerNameAndZdbId> addedSequences = sequences.stream().filter(sequence -> sequence.getZdbID() == null).toList();
        List<MarkerNameAndZdbId> addedSequencesWithoutDuplicatesOfExisting = addedSequences.stream().filter(
                addedSequence -> existingSequences.stream().noneMatch(
                        existingSequence -> existingSequence.getLabel().equals(addedSequence.getLabel())
                )
        ).toList();

        return addedSequencesWithoutDuplicatesOfExisting;
    }

    public List<MarkerNameAndZdbId> calculateRemovedSequences(String constructID, List<MarkerNameAndZdbId> sequences) {
        List<MarkerNameAndZdbId> existingSequences = getExistingSequencesAsMarkerNameAndZdbIds(constructID);
        List<MarkerNameAndZdbId> removedSequences = new ArrayList<>(CollectionUtils.subtract(existingSequences, sequences));
        return removedSequences;
    }

    private List<MarkerNameAndZdbId> getExistingSequencesAsMarkerNameAndZdbIds(String constructID) {
        return DTOMarkerService.getSupportingSequenceDTOs(mr.getMarkerByID(constructID)).stream().map(
                sequence -> {
                    MarkerNameAndZdbId mnzdb = new MarkerNameAndZdbId();
                    mnzdb.setZdbID(sequence.getZdbID());
                    mnzdb.setLabel(sequence.getView());
                    return mnzdb;
                }).toList();
    }

    public List<MarkerNameAndZdbId> calculateRemovedSynonyms(String constructID, List<MarkerNameAndZdbId> newSynonyms) {
        List<MarkerNameAndZdbId> existingSynonyms = getExistingSynonymsAsMarkerNameAndZdbIds(constructID);
        List<MarkerNameAndZdbId> removedSynonyms = new ArrayList<>(CollectionUtils.subtract(existingSynonyms, newSynonyms));

        return removedSynonyms;
    }

    public boolean updateConstruct(String constructID, EditConstructFormFields request) throws InvalidConstructNameException {
        boolean changesMade = false;

        ConstructName constructName = request.getConstructName();
        constructName.reinitialize();

        //figure out if constructName has changed
        ConstructName oldName = getExistingConstructName(constructID);
        if (!constructName.equals(oldName)) {
            changesMade = true;
        }

        //figure out if synonyms have changed ( could have new synonyms, or removed synonyms)
        List<MarkerNameAndZdbId> removedSynonyms = calculateRemovedSynonyms(constructID, request.getSynonyms());
        List<MarkerNameAndZdbId> addedSynonyms = calculateAddedSynonyms(constructID, request.getSynonyms());

        //figure out if sequences have changed
        List<MarkerNameAndZdbId> removedSequences = calculateRemovedSequences(constructID, request.getSequences());
        List<MarkerNameAndZdbId> addedSequences = calculateAddedSequences(constructID, request.getSequences());

        //figure out if notes have changed
        List<MarkerNameAndZdbId> removedNotes = calculateRemovedNotes(constructID, request.getNotes());
        List<MarkerNameAndZdbId> addedNotes = calculateAddedNotes(constructID, request.getNotes());

        if (!removedSynonyms.isEmpty() ||
                !addedSynonyms.isEmpty() ||
                !removedSequences.isEmpty() ||
                !addedSequences.isEmpty() ||
                !removedNotes.isEmpty() ||
                !addedNotes.isEmpty()) {
            changesMade = true;
        }

        //figure out if publicNote has changed
        String newPublicNote = request.getPublicNote();
        String oldPublicNote = mr.getMarkerByID(constructID).getPublicComments();
        if (!newPublicNote.equals(oldPublicNote)) {
            changesMade = true;
        }

        if (!changesMade) {
            return false;
        }

        String pubZdbID = request.getPublicationZdbID();

        Marker newMarker;
        if (constructName.equals(oldName)) {
            newMarker = mr.getMarkerByID(constructID);
        } else {
            newMarker = ConstructComponentService.updateConstructName(constructID, constructName, pubZdbID);
        }

        //synonyms
        for(MarkerNameAndZdbId addedSynonym : addedSynonyms) {
            mr.addMarkerAlias(newMarker, addedSynonym.getLabel(), pr.getPublication(pubZdbID));
            ir.insertUpdatesTable(newMarker,"alias","added data alias");
            log.debug("Added synonym: " + addedSynonym.getLabel());
        }
        for(MarkerNameAndZdbId removedSynonym : removedSynonyms) {
            mr.deleteMarkerAlias(newMarker, mr.getMarkerAlias(removedSynonym.getZdbID()));
            ir.insertUpdatesTable(newMarker,"alias","deleted data alias");
            log.debug("Removed synonym: " + removedSynonym.getLabel());
        }

        //sequences
        for(MarkerNameAndZdbId addedSequence : addedSequences) {
            addGenBankSequenceToConstruct(addedSequence.getLabel(), pubZdbID, newMarker);
            log.debug("Added sequence: " + addedSequence.getLabel());
        }
        for (MarkerNameAndZdbId removedSequence : removedSequences) {
            removeSequenceFromConstruct(constructID, removedSequence.getZdbID());
            log.debug("Removed sequence: " + removedSequence.getZdbID());
        }

        //notes
        for(MarkerNameAndZdbId addedNote : addedNotes) {
            mr.addMarkerDataNote(newMarker, addedNote.getLabel());
            ir.insertUpdatesTable(mr.getMarkerByID(constructID),"curator notes","added new curator note");
            log.debug("Added note: " + addedNote.getLabel());
        }
        for (MarkerNameAndZdbId removedNote : removedNotes) {
            DataNote curatorNote = ir.getDataNoteByID(removedNote.getZdbID());
            ir.insertUpdatesTable(mr.getMarkerByID(constructID), "curator notes", "deleted curator note");
            mr.removeCuratorNote(newMarker, curatorNote);
            log.debug("Removed note: " + removedNote.getLabel());
        }

        //public note
        if (!newPublicNote.equals(oldPublicNote)) {
            newMarker.setPublicComments(newPublicNote);
            ConstructCuration c = cr.getConstructByID(constructID);
            c.setPublicComments(newPublicNote);
            ir.insertUpdatesTable(newMarker, "comments", "updated public notes");
            log.debug("Updated public note: " + newPublicNote);
        }

        return changesMade;

    }

    public void addGenBankSequenceToConstruct(String constructSequence, String pubid, Marker m) {
        ReferenceDatabase genBankRefDB = sr.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        mr.addDBLink(m, constructSequence, genBankRefDB, pubid);
    }


    public List<MarkerNameAndZdbId> calculateAddedSynonyms(String constructID, List<MarkerNameAndZdbId> newSynonyms) {
        List<MarkerNameAndZdbId> existingSynonyms = getExistingSynonymsAsMarkerNameAndZdbIds(constructID);

        List<MarkerNameAndZdbId> addedSynonyms = newSynonyms.stream().filter(syn -> syn.getZdbID() == null).toList();
        List<MarkerNameAndZdbId> addedSynonymsWithoutDuplicatesOfExisting = addedSynonyms.stream().filter(
                addedSynonym -> existingSynonyms.stream().noneMatch(
                        existingSynonym -> existingSynonym.getLabel().equals(addedSynonym.getLabel())
                )
        ).toList();

        return addedSynonymsWithoutDuplicatesOfExisting;
    }

}

