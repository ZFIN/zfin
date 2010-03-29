package org.zfin.gwt.marker.server;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.MarkerNoteBox;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GoTerm;
import org.zfin.orthology.Species;
import org.zfin.people.MarkerSupplier;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.presentation.DBLinkPresentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class DTOService {

    private static Logger logger = Logger.getLogger(DTOService.class);

    public static Set<RelatedEntityDTO> createRelatedEntitiesForPublications(String dataZdbID, String name, Set<PublicationAttribution> publications) {
        Set<RelatedEntityDTO> relatedEntityDTOs = new HashSet<RelatedEntityDTO>();
        if (publications == null || publications.size() == 0) {
            RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
            relatedEntityDTO.setDataZdbID(dataZdbID);
            relatedEntityDTO.setName(name);
            relatedEntityDTOs.add(relatedEntityDTO);
        } else {
            for (PublicationAttribution publication : publications) {
                RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
                relatedEntityDTO.setName(name);
                relatedEntityDTO.setDataZdbID(dataZdbID);
                relatedEntityDTO.setPublicationZdbID(publication.getPublication().getZdbID());
                relatedEntityDTOs.add(relatedEntityDTO);
            }
        }
        return relatedEntityDTOs;
    }

    public static Set<SequenceDTO> createSequenceDTOsForPublications(Sequence sequence, String markerName) {
        Set<SequenceDTO> sequenceDTOs = new HashSet<SequenceDTO>();
        Set<PublicationAttribution> publications = sequence.getDbLink().getPublications();
        if (publications == null || publications.size() == 0) {
            sequenceDTOs.add(createSequenceDTOFromSequence(sequence, markerName, null, null));
        } else {
            for (PublicationAttribution publication : publications) {
                sequenceDTOs.add(createSequenceDTOFromSequence(sequence, markerName, publication.getPublication().getZdbID(), publication.getSourceType().toString()));
            }
        }
        return sequenceDTOs;
    }

    public static SequenceDTO createSequenceDTOFromSequence(Sequence sequence, String markerName, String publicationZdbID, String attributionType) {

        SequenceDTO sequenceDTO = new SequenceDTO();
        DBLink dbLink = sequence.getDbLink();
        sequenceDTO.setDataZdbID(dbLink.getDataZdbID());
        sequenceDTO.setDataName(markerName);
        sequenceDTO.setZdbID(dbLink.getZdbID());
        if (dbLink.getLength() == null) {
            sequenceDTO.setLength(sequence.getData().length());
        } else {
            sequenceDTO.setLength(dbLink.getLength());
        }
        // don't set is editable here
        // don't set is link here
        sequenceDTO.setName(dbLink.getAccessionNumber());
        sequenceDTO.setPublicationZdbID(publicationZdbID);

        sequenceDTO.setSequence(sequence.getData());
        sequenceDTO.setDefLine(sequence.getDefLine().toString());
        sequenceDTO.setAttributionType(attributionType);

        ReferenceDatabase referenceDatabase = dbLink.getReferenceDatabase();
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        sequenceDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);

        return sequenceDTO;
    }

    public static <U extends HasLink> Set<U> createLinksForPublication(U linkableData, Set<PublicationAttribution> publications) {
        Set<U> attributeDTOs = new HashSet<U>();
        if (publications == null || publications.size() == 0) {
            attributeDTOs.add(linkableData);
        } else {
            for (PublicationAttribution publicationAttribution : publications) {
                U newLink = linkableData.<U>deepCopy();
                newLink.setPublicationZdbID(publicationAttribution.getPublication().getZdbID());
                attributeDTOs.add(newLink);
            }
        }
        return attributeDTOs;
    }


    public static List<DBLinkDTO> createDBLinkDTOsFromTranscriptDBLink(TranscriptDBLink transcriptDBLink) {
        return createDBLinkDTOsFromDBLink(transcriptDBLink, transcriptDBLink.getTranscript().getZdbID(), transcriptDBLink.getTranscript().getAbbreviation());
    }

    public static List<DBLinkDTO> createDBLinkDTOsFromMarkerDBLinks(List<MarkerDBLink> markerDBLinks) {
        List<DBLinkDTO> markerDBLinkDTOs = new ArrayList<DBLinkDTO>() ;
        for(MarkerDBLink markerDBLink: markerDBLinks){
            markerDBLinkDTOs.addAll(createDBLinkDTOsFromMarkerDBLink(markerDBLink)) ;
        }
        return markerDBLinkDTOs ;
    }

    public static List<DBLinkDTO> createDBLinkDTOsFromMarkerDBLink(MarkerDBLink markerDBLink) {
        return createDBLinkDTOsFromDBLink(markerDBLink, markerDBLink.getMarker().getZdbID(), markerDBLink.getMarker().getAbbreviation());
    }

    public static ReferenceDatabase getReferenceDatabase(ReferenceDatabaseDTO referenceDatabaseDTO) {
        if (referenceDatabaseDTO == null) return null;

        ReferenceDatabase referenceDatabase;
        if (referenceDatabaseDTO.getZdbID() == null
                && referenceDatabaseDTO.getName() != null) {
            referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.getType(referenceDatabaseDTO.getName()),
                    ForeignDBDataType.DataType.getType(referenceDatabaseDTO.getType()),
                    ForeignDBDataType.SuperType.getType(referenceDatabaseDTO.getSuperType()),
                    Species.ZEBRAFISH);
        } else {
            referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, referenceDatabaseDTO.getZdbID());
        }
        return referenceDatabase;
    }

    public static List<DBLinkDTO> createDBLinkDTOsFromDBLink(List<DBLink> dbLinks, String markerZdbID, String markerName) {
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>();

        for (DBLink dbLink : dbLinks) {
            dbLinkDTOs.addAll(createDBLinkDTOsFromDBLink(dbLink, markerZdbID, markerName));
        }
        return dbLinkDTOs;
    }

    public static Transcript getTranscriptFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        if (dbLinkDTO.getDataZdbID() != null) {
            return (Transcript) HibernateUtil.currentSession().get(Transcript.class, dbLinkDTO.getDataZdbID());
        } else if (dbLinkDTO.getDataName() != null) {
            return (Transcript) RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName());
        } else {
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null;
        }
    }

    public static Marker getMarkerFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        if (dbLinkDTO.getDataZdbID() != null) {
            return (Marker) HibernateUtil.currentSession().get(Marker.class, dbLinkDTO.getDataZdbID());
        } else if (dbLinkDTO.getDataName() != null) {
            return RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName());
        } else {
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null;
        }
    }

    public static DBLinkDTO createDBLinkDTOFromDBLinkForPub(DBLink dbLink, String markerZdbID, String markerName) {
        return createDBLinkDTOFromDBLinkForPub(dbLink, markerZdbID, markerName, null);
    }

    public static DBLinkDTO createDBLinkDTOFromDBLinkForPub(DBLink dbLink, String markerZdbID, String markerName, String publicationZdbID) {
        DBLinkDTO dbLinkDTO = new DBLinkDTO();
        dbLinkDTO.setZdbID(dbLink.getZdbID());
        dbLinkDTO.setDataZdbID(markerZdbID);
        dbLinkDTO.setDataName(markerName);
        dbLinkDTO.setName(dbLink.getAccessionNumber());
        dbLinkDTO.setLength(dbLink.getLength());

        dbLinkDTO.setLink(DBLinkPresentation.getLink(dbLink));


        ReferenceDatabase referenceDatabase = dbLink.getReferenceDatabase();
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
        dbLinkDTO.setPublicationZdbID(publicationZdbID);

        return dbLinkDTO;
    }

    /**
     * Craete a list of DBLinkDTO, one for each reference.
     *
     * @param dbLink      Template DBLink.
     * @param markerZdbID MarkerZdbID to mirror.
     * @param markerName  Marker name.
     * @return A list of DBLinkDTOs, one for each reference.
     */
    public static List<DBLinkDTO> createDBLinkDTOsFromDBLink(DBLink dbLink, String markerZdbID, String markerName) {
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>();

        Set<PublicationAttribution> publicationAttributions = dbLink.getPublications();
        if (publicationAttributions == null || publicationAttributions.size() == 0) {
            dbLinkDTOs.add(createDBLinkDTOFromDBLinkForPub(dbLink, markerZdbID, markerName));
        } else {
            for (PublicationAttribution publicationAttribution : dbLink.getPublications()) {
                dbLinkDTOs.add(createDBLinkDTOFromDBLinkForPub(dbLink, markerZdbID, markerName, publicationAttribution.getPublication().getZdbID()));
            }
        }
        return dbLinkDTOs;
    }

    public static List<ReferenceDatabaseDTO> convertReferenceDTOs(List<ReferenceDatabase> referenceDatabases) {
        List<ReferenceDatabaseDTO> referenceDatabaseDTOList = new ArrayList<ReferenceDatabaseDTO>();

        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            referenceDatabaseDTOList.add(convertReferenceDTO(referenceDatabase));
        }

        return referenceDatabaseDTOList;
    }

    public static ReferenceDatabaseDTO convertReferenceDTO(ReferenceDatabase referenceDatabase) {
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        if (referenceDatabase.getPrimaryBlastDatabase() != null) {
            referenceDatabaseDTO.setBlastName(referenceDatabase.getPrimaryBlastDatabase().getName());
        }
        return referenceDatabaseDTO;
    }

    public static TranscriptDBLink createTranscriptDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        TranscriptDBLink dbLink = new TranscriptDBLink();
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // set transcript
        Transcript transcript = getTranscriptFromDBLinkDTO(dbLinkDTO);
        dbLink.setTranscript(transcript);
        dbLink.setDataZdbID(transcript.getZdbID());

        // reference DBs
        ReferenceDatabase referenceDatabase = null;
        if (dbLinkDTO.getReferenceDatabaseDTO() != null) {
            referenceDatabase = DTOService.getReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());
        }

        logger.info("referenceDB: " + referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink;
    }

    public static MarkerDBLink createMarkerDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        MarkerDBLink dbLink = new MarkerDBLink();
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // though we can't save this into the database, we can set it here to make things easier
        dbLink.setDataZdbID(dbLinkDTO.getDataZdbID());

        logger.info("creating marker dblink: " + dbLinkDTO.getDataZdbID());

        // set marker
        dbLink.setMarker(getMarkerFromDBLinkDTO(dbLinkDTO));

        logger.info("got marker dblink: " + dbLink.getMarker());

        // reference DBs
        ReferenceDatabase referenceDatabase = DTOService.getReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());

        logger.info("referenceDB: " + referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink;
    }

    public static DBLink createDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        return (dbLinkDTO.isTranscriptDBLink() ? createTranscriptDBLinkFromDBLinkDTO(dbLinkDTO) : createMarkerDBLinkFromDBLinkDTO(dbLinkDTO));
    }

    //todo: do these need to get the dataZdbID set?

    public static MarkerDTO createMarkerDTOFromMarker(Marker marker) {
        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setName(marker.getName());
        markerDTO.setAbbreviation(marker.getAbbreviation());
        markerDTO.setAbbreviationOrder(marker.getAbbreviationOrder());
        markerDTO.setZdbID(marker.getZdbID());
        markerDTO.setLink(MarkerPresentation.getLink(marker));
        return markerDTO;
    }




    public static void handleUpdatesTable(Marker marker, String fieldname, String oldValue, String newValue) {
        if (!StringUtils.equals(oldValue, newValue)) {
            InfrastructureService.insertUpdate(marker, fieldname, oldValue, newValue);
        }
    }

    public static void handleUpdatesTable(Marker marker, String fieldname, Integer oldValue, Integer newValue) {
        String oldValueString = (oldValue == null ? null : oldValue.toString());
        String newValueString = (newValue == null ? null : newValue.toString());
        handleUpdatesTable(marker, fieldname, oldValueString, newValueString);
    }


    public static List<String> getSuppliers(Marker marker) {
        Set<MarkerSupplier> markerSuppliers = marker.getSuppliers();
        List<String> supplierList = new ArrayList<String>();
        for (MarkerSupplier markerSupplier : markerSuppliers) {
            supplierList.add(markerSupplier.getOrganization().getName());
        }
        return supplierList;
    }

    public static List<String> getDirectAttributions(Marker marker){
        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(marker.getZdbID());
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        List<String> attributions = new ArrayList<String>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        return attributions ;
    }

    public static List<NoteDTO> getCuratorNotes(Marker marker){
        // get notes
        List<NoteDTO> curatorNotes = new ArrayList<NoteDTO>();
        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteData(dataNote.getNote());
            noteDTO.setZdbID(dataNote.getZdbID());
//            noteDTO.setDataZdbID(dataNote.getDataZdbID());
            noteDTO.setDataZdbID(marker.getZdbID());
            noteDTO.setEditMode(MarkerNoteBox.EditMode.PRIVATE.name());
            curatorNotes.add(noteDTO);
        }
        return curatorNotes;
    }

    public static NoteDTO getPublicNote(Marker marker){

        NoteDTO publicNoteDTO = new NoteDTO();
        publicNoteDTO.setNoteData(marker.getPublicComments());
        publicNoteDTO.setZdbID(marker.getZdbID());
        publicNoteDTO.setDataZdbID(marker.getZdbID());
        publicNoteDTO.setEditMode(MarkerNoteBox.EditMode.PUBLIC.name());
        return publicNoteDTO;
    }

    public static List<RelatedEntityDTO> getAliases(Marker marker){
        // get alias's
        Set<MarkerAlias> aliases = marker.getAliases();
        List<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<RelatedEntityDTO>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                aliasRelatedEntities.addAll(DTOService.createRelatedEntitiesForPublications(marker.getZdbID(), alias.getAlias(), publicationAttributions));
            }
        }
        return aliasRelatedEntities;
    }


    /**
     * @param marker Marker
     * @return list of marker DTOs
     */
    public static List<MarkerDTO> getRelatedGenes(Marker marker){
        Set<MarkerRelationship> markerRelationships = marker.getFirstMarkerRelationships();
        logger.debug("# of marker relationships: " + markerRelationships.size());
        List<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            if (
                    markerRelationship.getSecondMarker().isInTypeGroup(Marker.TypeGroup.GENE)
                // todo: should use a different type
//                  &&
//                   markerRelationship.getType().equals(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)
                    ) {
                Marker internalGene = markerRelationship.getSecondMarker();
//                relatedGenes.addAll(DTOHelper.createAttributesForPublication(gene.getAbbreviation(),markerRelationship.getPublications())) ;
                relatedGenes.addAll(DTOService.createLinksForPublication(DTOService.createMarkerDTOFromMarker(internalGene), markerRelationship.getPublications()));
            }
        }
        logger.debug("# of related genes: " + relatedGenes.size());
        return relatedGenes ;
    }

    /**
     * @param marker Marker
     * @return list of DBlinkDTOs
     */
    public static List<DBLinkDTO> getSupportingSequences(Marker marker){
        // get sequences
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                DisplayGroup.GroupName.DBLINK_ADDING_ON_CLONE_EDIT);
        List<MarkerDBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinksForMarker(marker, (ReferenceDatabase[]) referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));
        List<DBLinkDTO> dbLinkDTOList = new ArrayList<DBLinkDTO>();
        for (MarkerDBLink markerDBLink : dbLinks) {
            DBLinkDTO dbLinkDTO = new DBLinkDTO();
            dbLinkDTO.setDataZdbID(markerDBLink.getDataZdbID());
            dbLinkDTO.setZdbID(markerDBLink.getZdbID());
            dbLinkDTO.setName(markerDBLink.getAccessionNumber());
            Publication publication = markerDBLink.getSinglePublication();
            if (publication != null) {
                dbLinkDTO.setPublicationZdbID(publication.getZdbID());
            }
            dbLinkDTO.setLength(markerDBLink.getLength());

            ReferenceDatabase referenceDatabase = markerDBLink.getReferenceDatabase();
            ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
            referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
            referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
            referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
            referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());

            dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
            dbLinkDTOList.addAll(DTOService.createDBLinkDTOsFromMarkerDBLink(markerDBLink));
        }
        return dbLinkDTOList;
    }

    public static List<NoteDTO> getExternalNotes(Antibody antibody){
        List<NoteDTO> externalNotes = new ArrayList<NoteDTO>();
        for (AntibodyExternalNote antibodyExternalNote: antibody.getExternalNotes()) {
            NoteDTO antibodyExternalNoteDTO = new NoteDTO();
            antibodyExternalNoteDTO.setZdbID(antibodyExternalNote.getZdbID());
            antibodyExternalNoteDTO.setEditMode(MarkerNoteBox.EditMode.EXTERNAL.name());
            antibodyExternalNoteDTO.setDataZdbID(antibody.getZdbID());
            if(antibodyExternalNote.getSinglePubAttribution()!=null){
                antibodyExternalNoteDTO.setPublicationZdbID(antibodyExternalNote.getSinglePubAttribution().getPublication().getZdbID());
            }
            antibodyExternalNoteDTO.setNoteData(antibodyExternalNote.getNote());
            externalNotes.add(antibodyExternalNoteDTO);
        }
        return externalNotes ;
    }

    public static FeatureDTO createFeatureDTOFromFeature(Feature feature) {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setName(feature.getAbbreviation());
        featureDTO.setZdbID(feature.getZdbID());
        return featureDTO;
    }

    public static GenotypeDTO createGenotypeDTOFromGenotype(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getHandle());
        genotypeDTO.setZdbID(genotype.getZdbID());
        return genotypeDTO;
    }

    public static GoTermDTO createGoTermDTOFromGoTerm(GoTerm goTerm) {
        GoTermDTO goTermDTO = new GoTermDTO();
        goTermDTO.setZdbID(goTerm.getZdbID());
        goTermDTO.setName(goTerm.getName());
        goTermDTO.setDataZdbID(goTerm.getOboID());
        goTermDTO.setSubOntology(goTerm.getSubOntology());
        return goTermDTO ;  //To change body of created methods use File | Settings | File Templates.
    }
}
