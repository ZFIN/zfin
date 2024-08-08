package org.zfin.construct.presentation;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.antibody.AntibodyService;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.InvalidConstructNameException;
import org.zfin.construct.name.*;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.database.InformixUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.construct.name.Cassette.COMPONENT_SEPARATOR;
import static org.zfin.framework.HibernateUtil.currentSession;

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

