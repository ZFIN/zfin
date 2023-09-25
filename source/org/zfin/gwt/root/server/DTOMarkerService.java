package org.zfin.gwt.root.server;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.ExternalNote;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class DTOMarkerService {

    private static Logger logger = LogManager.getLogger(DTOMarkerService.class);


    public static void insertMarkerUpdate(Marker marker, String fieldname, String oldValue, String newValue) {
        if (!StringUtils.equals(oldValue, newValue)) {
            InfrastructureService.insertUpdate(marker, fieldname, oldValue, newValue);
        }
    }

    public static void insertMarkerUpdate(Marker marker, String fieldname, Integer oldValue, Integer newValue) {
        String oldValueString = (oldValue == null ? null : oldValue.toString());
        String newValueString = (newValue == null ? null : newValue.toString());
        insertMarkerUpdate(marker, fieldname, oldValueString, newValueString);
    }

    public static CuratorNoteDTO convertToCuratorNoteDto(DataNote dataNote, Marker marker) {
        CuratorNoteDTO noteDTO = new CuratorNoteDTO();
        noteDTO.setNoteData(DTOConversionService.unescapeString(dataNote.getNote()));
        noteDTO.setZdbID(dataNote.getZdbID());
        noteDTO.setDataZdbID(marker.getZdbID());
        noteDTO.setCurator(DTOConversionService.convertToPersonDTO(dataNote.getCurator()));
        noteDTO.setDate(dataNote.getDate());
        noteDTO.setNoteEditMode(NoteEditMode.PRIVATE);
        return noteDTO;
    }

    public static List<NoteDTO> getCuratorNoteDTOs(Marker marker) {
        // get notes
        List<NoteDTO> curatorNotes = new ArrayList<>();
        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            curatorNotes.add(convertToCuratorNoteDto(dataNote, marker));
        }
        return curatorNotes;
    }

    public static NoteDTO getPublicNoteDTO(Marker marker) {
        NoteDTO publicNoteDTO = new NoteDTO();
        publicNoteDTO.setNoteData(DTOConversionService.unescapeString(marker.getPublicComments()));
        publicNoteDTO.setZdbID(marker.getZdbID());
        publicNoteDTO.setDataZdbID(marker.getZdbID());
        publicNoteDTO.setNoteEditMode(NoteEditMode.PUBLIC);
        return publicNoteDTO;
    }

    public static List<RelatedEntityDTO> getMarkerAliasDTOs(Marker marker) {
        // get alias's
        Set<MarkerAlias> aliases = marker.getAliases();
        List<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                Set<RelatedEntityDTO> dtos = DTOConversionService.convertPublicationAttributionsToDTOs(marker.getZdbID(), DTOConversionService.unescapeString(alias.getAlias()), publicationAttributions);
                dtos.forEach(relatedEntityDTO -> relatedEntityDTO.setZdbID(alias.getZdbID()));
                aliasRelatedEntities.addAll(dtos);
            }
        }
        return aliasRelatedEntities;
    }

    /**
     * @param marker Marker
     * @return list of marker DTOs
     */
    public static List<MarkerDTO> getRelatedGenesMarkerDTOs(Marker marker) {

        Set<MarkerRelationship> markerRelationships2 = marker.getSecondMarkerRelationships();


        List<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships2) {

                relatedGenes.addAll(DTOConversionService.createLinks(DTOConversionService.convertToMarkerDTO(markerRelationship.getFirstMarker()), markerRelationship.getPublications()));
        }

        return relatedGenes;
    }

    public static List<MarkerDTO> getGenesMarkerDTOs(Marker marker) {

        List<MarkerRelationship> markerRelationships = RepositoryFactory.getMarkerRepository().getMarkerRelationshipBySecondMarker(marker);
        List<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            Marker internalGene = markerRelationship.getFirstMarker();
            relatedGenes.addAll(DTOConversionService.createLinks(DTOConversionService.convertToMarkerDTO(internalGene), markerRelationship.getPublications()));
            logger.debug("# of related genes: " + relatedGenes.size());

        }


        return relatedGenes;
    }


    /**
     * @param marker Marker
     * @return list of DBlinkDTOs
     */
    public static List<DBLinkDTO> getSupportingSequenceDTOs(Marker marker) {
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
            dbLinkDTOList.addAll(DTOConversionService.convertToDBLinkDTOs(markerDBLink));
        }
        return dbLinkDTOList;
    }

    public static List<NoteDTO> getExternalNoteDTOs(Antibody antibody) {
        List<NoteDTO> externalNotes = new ArrayList<NoteDTO>();
        for (AntibodyExternalNote antibodyExternalNote : antibody.getExternalNotes()) {
            externalNotes.add(convertToNoteDTO(antibodyExternalNote));
        }
        return externalNotes;
    }

    public static NoteDTO convertToNoteDTO(ExternalNote note) {
        NoteDTO dto = new NoteDTO();
        dto.setZdbID(note.getZdbID());
        dto.setNoteEditMode(NoteEditMode.EXTERNAL);
        dto.setDataZdbID(note.getExternalDataZdbID());
        dto.setPublicationZdbID(note.getPublication().getZdbID());
        dto.setNoteData(DTOConversionService.unescapeString(note.getNote()));
        return dto;
    }
}