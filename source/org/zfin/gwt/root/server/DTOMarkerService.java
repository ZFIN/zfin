package org.zfin.gwt.root.server;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

    private static Logger logger = Logger.getLogger(DTOMarkerService.class);


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

    public static List<NoteDTO> getCuratorNoteDTOs(Marker marker) {
        // get notes
        List<NoteDTO> curatorNotes = new ArrayList<NoteDTO>();
        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteData(dataNote.getNote());
            noteDTO.setZdbID(dataNote.getZdbID());
//            noteDTO.setDataZdbID(dataNote.getDataZdbID());
            noteDTO.setDataZdbID(marker.getZdbID());
            noteDTO.setNoteEditMode(NoteEditMode.PRIVATE);
            curatorNotes.add(noteDTO);
        }
        return curatorNotes;
    }

    public static NoteDTO getPublicNoteDTO(Marker marker) {
        NoteDTO publicNoteDTO = new NoteDTO();
        publicNoteDTO.setNoteData(marker.getPublicComments());
        publicNoteDTO.setZdbID(marker.getZdbID());
        publicNoteDTO.setDataZdbID(marker.getZdbID());
        publicNoteDTO.setNoteEditMode(NoteEditMode.PUBLIC);
        return publicNoteDTO;
    }

    public static List<RelatedEntityDTO> getMarkerAliasDTOs(Marker marker) {
        // get alias's
        Set<MarkerAlias> aliases = marker.getAliases();
        List<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<RelatedEntityDTO>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                aliasRelatedEntities.addAll(DTOConversionService.convertPublicationAttributionsToDTOs(marker.getZdbID(), alias.getAlias(), publicationAttributions));
            }
        }
        return aliasRelatedEntities;
    }

    /**
     * @param marker Marker
     * @return list of marker DTOs
     */
    public static List<MarkerDTO> getRelatedGenesMarkerDTOs(Marker marker) {
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
                relatedGenes.addAll(DTOConversionService.createLinks(DTOConversionService.convertToMarkerDTO(internalGene), markerRelationship.getPublications()));
            }
        }
        logger.debug("# of related genes: " + relatedGenes.size());
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
            NoteDTO antibodyExternalNoteDTO = new NoteDTO();
            antibodyExternalNoteDTO.setZdbID(antibodyExternalNote.getZdbID());
            antibodyExternalNoteDTO.setNoteEditMode(NoteEditMode.EXTERNAL);
            antibodyExternalNoteDTO.setDataZdbID(antibody.getZdbID());
            if (antibodyExternalNote.getSinglePubAttribution() != null) {
                antibodyExternalNoteDTO.setPublicationZdbID(antibodyExternalNote.getSinglePubAttribution().getPublication().getZdbID());
            }
            antibodyExternalNoteDTO.setNoteData(antibodyExternalNote.getNote());
            externalNotes.add(antibodyExternalNoteDTO);
        }
        return externalNotes;
    }
}