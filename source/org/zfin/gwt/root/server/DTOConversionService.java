package org.zfin.gwt.root.server;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.construct.presentation.ConstructPresentation;
import org.zfin.expression.*;
import org.zfin.feature.*;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.FeatureLocation;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.ontology.*;
import org.zfin.ontology.service.OntologyService;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.orthology.OrthologExternalReference;
import org.zfin.orthology.presentation.OrthologDTO;
import org.zfin.orthology.presentation.OrthologEvidenceDTO;
import org.zfin.orthology.presentation.OrthologExternalReferenceDTO;
import org.zfin.profile.CuratorSession;
import org.zfin.profile.Lab;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.presentation.DBLinkPresentation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

//import org.apache.commons.lang.StringEscapeUtils;

/**
 */
public class DTOConversionService {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private static Logger logger = LogManager.getLogger(DTOConversionService.class);
//    private static SynonymSorting synonymSorting = new SynonymSorting();

    public static String escapeString(String uncleansedCharacter) {
//        return StringEscapeUtils.escapeJavaScript(uncleansedCharacter);
        //    uncleansedCharacter=StringEscapeUtils.escapeHtml3(uncleansedCharacter);
        //  uncleansedCharacter=StringEscapeUtils.escapeHtml4(uncleansedCharacter);
        uncleansedCharacter = StringEscapeUtils.escapeXml(uncleansedCharacter);
        uncleansedCharacter = StringEscapeUtils.escapeHtml4(uncleansedCharacter);
        uncleansedCharacter = StringEscapeUtils.escapeJava(uncleansedCharacter);
        // uncleansedCharacter=StringEscapeUtils.escapeHtml3(uncleansedCharacter);
        //uncleansedCharacter=StringEscapeUtils.escapeXml(uncleansedCharacter);
        //uncleansedCharacter=StringEscapeUtils.escapeEcmaScript(uncleansedCharacter);

        return uncleansedCharacter;
    }

    public static String unescapeString(String cleansedCharacter) {
//        return StringEscapeUtils.escapeJavaScript(uncleansedCharacter);
        //      cleansedCharacter= StringEscapeUtils.unescapeJava(cleansedCharacter);
        cleansedCharacter = StringEscapeUtils.unescapeXml(cleansedCharacter);
        cleansedCharacter = StringEscapeUtils.unescapeHtml4(cleansedCharacter);
        cleansedCharacter = StringEscapeUtils.unescapeJava(cleansedCharacter);
        //     cleansedCharacter=StringEscapeUtils.unescapeHtml3(cleansedCharacter);
        return cleansedCharacter;
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> unescapeStrings(Collection<String> cleansedCharacter) {
        return CollectionUtils.collect(cleansedCharacter, new Transformer() {
            @Override
            public String transform(Object o) {
                return DTOConversionService.unescapeString(o.toString());
            }
        });
    }

    public static Set<RelatedEntityDTO> convertPublicationAttributionsToDTOs(String dataZdbID, String name, Set<PublicationAttribution> publications) {
        Set<RelatedEntityDTO> relatedEntityDTOs = new HashSet<>();
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

    public static Set<SequenceDTO> convertToSequenceDTOs(Sequence sequence, String markerName) {
        Set<SequenceDTO> sequenceDTOs = new HashSet<>();
        Set<PublicationAttribution> publications = sequence.getDbLink().getPublications();
        if (publications == null || publications.size() == 0) {
            sequenceDTOs.add(convertToSequenceDTO(sequence, markerName, null, null));
        } else {
            for (PublicationAttribution publication : publications) {
                sequenceDTOs.add(convertToSequenceDTO(sequence, markerName, publication.getPublication().getZdbID(), publication.getSourceType().toString()));
            }
        }
        return sequenceDTOs;
    }

    public static SequenceDTO convertToSequenceDTO(Sequence sequence, String markerName, String publicationZdbID, String attributionType) {

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

    public static <U extends HasLink> Set<U> createLinks(U linkableData, Set<PublicationAttribution> publications) {
        Set<U> attributeDTOs = new HashSet<>();
        if (publications == null || publications.size() == 0) {
            attributeDTOs.add(linkableData);
        } else {
            for (PublicationAttribution publicationAttribution : publications) {
                U newLink = linkableData.deepCopy();
                newLink.setPublicationZdbID(publicationAttribution.getPublication().getZdbID());
                attributeDTOs.add(newLink);
            }
        }
        return attributeDTOs;
    }


    public static List<DBLinkDTO> convertToDBLinkDTOs(TranscriptDBLink transcriptDBLink) {
        return convertToDBLinkDTOs(transcriptDBLink, transcriptDBLink.getTranscript().getZdbID(), transcriptDBLink.getTranscript().getAbbreviation());
    }

    public static List<DBLinkDTO> convertToDBLinkDTOs(MarkerDBLink markerDBLink) {
        return convertToDBLinkDTOs(markerDBLink, markerDBLink.getMarker().getZdbID(), markerDBLink.getMarker().getAbbreviation());
    }

    public static ReferenceDatabase convertToReferenceDatabase(ReferenceDatabaseDTO referenceDatabaseDTO) {
        if (referenceDatabaseDTO == null) return null;

        ReferenceDatabase referenceDatabase;
        if (referenceDatabaseDTO.getZdbID() == null
                && referenceDatabaseDTO.getName() != null) {
            referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.getType(referenceDatabaseDTO.getName()),
                    ForeignDBDataType.DataType.getType(referenceDatabaseDTO.getType()),
                    ForeignDBDataType.SuperType.getType(referenceDatabaseDTO.getSuperType()),
                    org.zfin.Species.Type.ZEBRAFISH);
        } else {
            referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, referenceDatabaseDTO.getZdbID());
        }
        return referenceDatabase;
    }

    public static List<DBLinkDTO> convertToDBLinkDTOs(List<DBLink> dbLinks, String markerZdbID, String markerName) {
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<>();

        for (DBLink dbLink : dbLinks) {
            dbLinkDTOs.addAll(convertToDBLinkDTOs(dbLink, markerZdbID, markerName));
        }
        return dbLinkDTOs;
    }

    public static Transcript convertToTranscriptFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        if (dbLinkDTO.getDataZdbID() != null) {
            return (Transcript) HibernateUtil.currentSession().get(Transcript.class, dbLinkDTO.getDataZdbID());
        } else if (dbLinkDTO.getDataName() != null) {
            return (Transcript) RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName());
        } else {
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null;
        }
    }

    public static Marker convertToMarkerFromDBLinkDTO(DBLinkDTO dbLinkDTO) {
        if (dbLinkDTO.getDataZdbID() != null) {
            return (Marker) HibernateUtil.currentSession().get(Marker.class, dbLinkDTO.getDataZdbID());
        } else if (dbLinkDTO.getDataName() != null) {
            return RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName());
        } else {
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null;
        }
    }

    public static DBLinkDTO convertToDBLinkDTO(DBLink dbLink, String markerZdbID, String markerName) {
        return convertToDBLinkDTO(dbLink, markerZdbID, markerName, null);
    }

    public static DBLinkDTO convertToDBLinkDTO(DBLink dbLink, String markerZdbID, String markerName, String publicationZdbID) {
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
    public static List<DBLinkDTO> convertToDBLinkDTOs(DBLink dbLink, String markerZdbID, String markerName) {
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<>();

        Set<PublicationAttribution> publicationAttributions = dbLink.getPublications();
        if (publicationAttributions == null || publicationAttributions.size() == 0) {
            dbLinkDTOs.add(convertToDBLinkDTO(dbLink, markerZdbID, markerName));
        } else {
            for (PublicationAttribution publicationAttribution : dbLink.getPublications()) {
                dbLinkDTOs.add(convertToDBLinkDTO(dbLink, markerZdbID, markerName, publicationAttribution.getPublication().getZdbID()));
            }
        }
        return dbLinkDTOs;
    }

    public static List<ReferenceDatabaseDTO> convertToReferenceDatabaseDTOs(List<ReferenceDatabase> referenceDatabases) {
        List<ReferenceDatabaseDTO> referenceDatabaseDTOList = new ArrayList<>();

        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            referenceDatabaseDTOList.add(convertToReferenceDatabaseDTO(referenceDatabase));
        }

        return referenceDatabaseDTOList;
    }

    public static ReferenceDatabaseDTO convertToReferenceDatabaseDTO(ReferenceDatabase referenceDatabase) {
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        if (referenceDatabase.getPrimaryBlastDatabase() != null) {
            referenceDatabaseDTO.setBlastName(referenceDatabase.getPrimaryBlastDatabase().getName());
        }
        referenceDatabaseDTO.setUrl(referenceDatabase.getForeignDB().getDbUrlPrefix());
        return referenceDatabaseDTO;
    }

    public static TranscriptDBLink createToTranscriptDBLink(DBLinkDTO dbLinkDTO) {
        TranscriptDBLink dbLink = new TranscriptDBLink();
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // set transcript
        Transcript transcript = convertToTranscriptFromDBLinkDTO(dbLinkDTO);
        dbLink.setTranscript(transcript);
        dbLink.setDataZdbID(transcript.getZdbID());

        // reference DBs
        ReferenceDatabase referenceDatabase = null;
        if (dbLinkDTO.getReferenceDatabaseDTO() != null) {
            referenceDatabase = DTOConversionService.convertToReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());
        }

        logger.info("referenceDB: " + referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink;
    }

    public static MarkerDBLink convertToMarkerDBLink(DBLinkDTO dbLinkDTO) {
        MarkerDBLink dbLink = new MarkerDBLink();
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // though we can't save this into the database, we can set it here to make things easier
        dbLink.setDataZdbID(dbLinkDTO.getDataZdbID());

        logger.info("creating marker dblink: " + dbLinkDTO.getDataZdbID());

        // set marker
        dbLink.setMarker(convertToMarkerFromDBLinkDTO(dbLinkDTO));

        logger.info("got marker dblink: " + dbLink.getMarker());

        // reference DBs
        ReferenceDatabase referenceDatabase = DTOConversionService.convertToReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());

        logger.info("referenceDB: " + referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink;
    }

    public static DBLink convertToDBLink(DBLinkDTO dbLinkDTO) {
        return (dbLinkDTO.isTranscriptDBLink() ? createToTranscriptDBLink(dbLinkDTO) : convertToMarkerDBLink(dbLinkDTO));
    }

    public static MarkerDTO convertToMarkerDTO(Marker marker) {
        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setName(marker.getName());
        if (marker.getAbbreviation() != null) {
            markerDTO.setName(marker.getAbbreviation());
        }
        markerDTO.setCompareString(marker.getAbbreviationOrder());
        markerDTO.setZdbID(marker.getZdbID());
        markerDTO.setMarkerType(marker.getMarkerType().getDisplayName());
        markerDTO.setLink(MarkerPresentation.getLink(marker));
        return markerDTO;
    }

    public static ConstructDTO convertToConstructDTO(ConstructCuration construct) {
        ConstructDTO constructDTO = new ConstructDTO();

        Construct constructDisp = new Construct();
        constructDTO.setName(construct.getName());

        constructDTO.setName(construct.getName());


        constructDTO.setZdbID(construct.getZdbID());
        constructDTO.setConstructType(construct.getConstructType().getDisplayName());
        constructDisp.setID(construct.getZdbID());
        constructDTO.setLink(ConstructPresentation.getLink(construct));
        return constructDTO;
    }

    public static GenotypeDTO convertToGenotypeDTO(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getName());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        if (CollectionUtils.isNotEmpty(genotype.getExternalNotes())) {
            createExternalNotesOnGenotype(genotype, genotypeDTO);
        }

        List<PublicationDTO> associatedPublications = new ArrayList<>();
        for (int i = 0; i < genotype.getAssociatedPublications().size(); i++) {
            associatedPublications.add(DTOConversionService.convertToPublicationDTO(genotype.getAssociatedPublications().get(i)));
        }
        genotypeDTO.setAssociatedPublications(associatedPublications);

        // add features
        if (CollectionUtils.isNotEmpty(genotype.getGenotypeFeatures())) {
            List<GenotypeFeatureDTO> featureDTOList = new ArrayList<>(4);
            for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
                featureDTOList.add(convertToGenotypeFeawtureDTO(genotypeFeature));
            }
            genotypeDTO.setGenotypeFeatureList(featureDTOList);
        }
        return genotypeDTO;
    }

    public static GenotypeDTO convertToGenotypeDTOShallow(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getName());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        genotypeDTO.setNickName(genotype.getNickname());
        genotypeDTO.setWildtype(genotype.isWildtype());
        if (genotype.getAssociatedGenotypes() != null) {
            for (Genotype background : genotype.getAssociatedGenotypes()) {
                genotypeDTO.addBackgroundGenotype(convertToPureGenotypeDTOs(background));
            }
        }
        if (CollectionUtils.isNotEmpty(genotype.getExternalNotes())) {
            createExternalNotesOnGenotype(genotype, genotypeDTO);
        }
        if (CollectionUtils.isNotEmpty(genotype.getDataNotes())) {
            createCuratorNotesOnGenotype(genotype, genotypeDTO);
        }
        // add features
        if (CollectionUtils.isNotEmpty(genotype.getGenotypeFeatures())) {
            List<GenotypeFeatureDTO> genotypeFeatureDTOList = new ArrayList<>(4);
            for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
                genotypeFeatureDTOList.add(convertToGenotypeFeatureDTO(genotypeFeature, true));
            }
            genotypeDTO.setGenotypeFeatureList(genotypeFeatureDTOList);
        }
        return genotypeDTO;
    }

    private static GenotypeFeatureDTO convertToGenotypeFeawtureDTO(GenotypeFeature genotypeFeature) {
        GenotypeFeatureDTO dto = new GenotypeFeatureDTO();
        dto.setFeatureDTO(convertToFeatureDTO(genotypeFeature.getFeature()));
        dto.setZygosity(convertToZygosityDTO(genotypeFeature.getZygosity()));
        dto.setMaternalZygosity(convertToZygosityDTO(genotypeFeature.getMomZygosity()));
        dto.setPaternalZygosity(convertToZygosityDTO(genotypeFeature.getDadZygosity()));
        return dto;
    }

    private static GenotypeFeatureDTO convertToGenotypeFeatureDTO(GenotypeFeature genotypeFeature, boolean shallow) {
        GenotypeFeatureDTO dto = new GenotypeFeatureDTO();
        dto.setFeatureDTO(convertToFeatureDTO(genotypeFeature.getFeature(), !shallow));
        dto.setZygosity(convertToZygosityDTO(genotypeFeature.getZygosity()));
        dto.setMaternalZygosity(convertToZygosityDTO(genotypeFeature.getMomZygosity()));
        dto.setPaternalZygosity(convertToZygosityDTO(genotypeFeature.getDadZygosity()));
        return dto;
    }

    public static GenotypeDTO convertToPureGenotypeDTOs(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getName());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        if (genotype.getAssociatedGenotypes() != null) {
            for (Genotype background : genotype.getAssociatedGenotypes()) {
                genotypeDTO.addBackgroundGenotype(convertToPureGenotypeDTOs(background));
            }
        }
        return genotypeDTO;
    }

    private static void createCuratorNotesOnGenotype(Genotype genotype, GenotypeDTO genotypeDTO) {
        List<CuratorNoteDTO> CuratorNoteDTOList = new ArrayList<>(genotype.getDataNotes().size());
        for (DataNote note : genotype.getDataNotes()) {
            CuratorNoteDTO noteDTO = new CuratorNoteDTO();
            noteDTO.setZdbID(note.getZdbID());
            noteDTO.setNoteData(note.getNote());
            CuratorNoteDTOList.add(noteDTO);
        }
        genotypeDTO.setPrivateNotes(CuratorNoteDTOList);
    }

    private static void createExternalNotesOnGenotype(Genotype genotype, GenotypeDTO genotypeDTO) {
        List<ExternalNoteDTO> externalNoteDTOList = new ArrayList<>(genotype.getExternalNotes().size());
        for (GenotypeExternalNote note : genotype.getExternalNotes()) {
            ExternalNoteDTO noteDTO = new ExternalNoteDTO();
            noteDTO.setZdbID(note.getZdbID());
            noteDTO.setNoteData(note.getNote());
            noteDTO.setPublicationDTO(convertToPublicationDTO(note.getPublication()));
            externalNoteDTOList.add(noteDTO);
        }
        genotypeDTO.setPublicNotes(externalNoteDTOList);
    }

    public static GenotypeDTO convertToGenotypeDTO(Genotype genotype, boolean includePubInfo) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getHandle());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        genotypeDTO.setWildtype(genotype.isWildtype());

        if (includePubInfo) {
            List<PublicationDTO> associatedPublications = new ArrayList<>();
            for (Publication publication : genotype.getAssociatedPublications()) {
                associatedPublications.add(DTOConversionService.convertToPublicationDTO(publication));
            }
            genotypeDTO.setAssociatedPublications(associatedPublications);
        }
        if (genotype.getAssociatedGenotypes() != null) {
            List<GenotypeDTO> backgroundList = new ArrayList<>(genotype.getAssociatedGenotypes().size());
            for (Genotype background : genotype.getAssociatedGenotypes()) {
                GenotypeDTO backgroundDTO = new GenotypeDTO();
                backgroundDTO.setName(background.getName());
                backgroundDTO.setZdbID(background.getZdbID());
                backgroundList.add(backgroundDTO);
            }
            genotypeDTO.setBackgroundGenotypeList(backgroundList);
        }
        // add features
        if (CollectionUtils.isNotEmpty(genotype.getGenotypeFeatures())) {
            List<GenotypeFeatureDTO> featureDTOList = new ArrayList<>(4);
            for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
                featureDTOList.add(convertToGenotypeFeawtureDTO(genotypeFeature));
            }
            genotypeDTO.setGenotypeFeatureList(featureDTOList);
        }
        return genotypeDTO;
    }

    public static PublicationDTO convertToPublicationDTO(Publication publication) {
        PublicationDTO publicationDTO = new PublicationDTO(publication.getTitle(), publication.getZdbID());
        publicationDTO.setAuthors(publication.getAuthors());
        List<PersonDTO> registeredAuthors = new ArrayList<>();
        for (Person author : publication.getPeople()) {
            registeredAuthors.add(convertToPersonDTO(author));
        }
        publicationDTO.setRegisteredAuthors(registeredAuthors);
        publicationDTO.setAbstractText(publication.getAbstractText());
        publicationDTO.setDoi(publication.getDoi());
        publicationDTO.setAccession(publication.getAccessionNumber());
        publicationDTO.setCitation(publication.getCitation());
        publicationDTO.setMiniRef(publication.getShortAuthorList());
        return publicationDTO;
    }

    public static GoEvidenceDTO convertToGoEvidenceDTO(MarkerGoTermEvidence markerGoTermEvidence) {

        GoEvidenceDTO returnDTO = new GoEvidenceDTO();
        returnDTO.setZdbID(markerGoTermEvidence.getZdbID());
        returnDTO.setDataZdbID(markerGoTermEvidence.getZdbID());
        returnDTO.setGoTerm(DTOConversionService.convertToTermDTO(markerGoTermEvidence.getGoTerm()));

        returnDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(markerGoTermEvidence.getEvidenceCode().getCode()));
        returnDTO.setFlag(markerGoTermEvidence.getFlag() == null ? null : markerGoTermEvidence.getFlag());
        returnDTO.setMarkerDTO(DTOConversionService.convertToMarkerDTO(markerGoTermEvidence.getMarker()));
        returnDTO.setOrganizationSource(markerGoTermEvidence.getGafOrganization().getOrganization());
        returnDTO.setNote(markerGoTermEvidence.getNote());
        Set<String> noctuaLinks = new HashSet<>();

        if (markerGoTermEvidence.getNoctuaModelId() != null) {
            for (NoctuaModel noctua : markerGoTermEvidence.getNoctuaModels()) {
                noctuaLinks.add(noctua.getId());
            }
            returnDTO.setNoctuaModels(noctuaLinks);
        }
        returnDTO.setPublicationZdbID(markerGoTermEvidence.getSource().getZdbID());
        // date modified and created not used here, as not needed
        // set name to the go term name
        returnDTO.setName(markerGoTermEvidence.getGoTerm().getTermName());

        // create inferences
        Set<String> inferredFromSet = new HashSet<>();
        Set<String> inferredFromLinks = new HashSet<>();
        if (markerGoTermEvidence.getInferredFrom() != null) {
            for (InferenceGroupMember inferenceGroupMember : markerGoTermEvidence.getInferredFrom()) {
                inferredFromSet.add(inferenceGroupMember.getInferredFrom());
                inferredFromLinks.add(MarkerGoEvidencePresentation.generateInferenceLink(inferenceGroupMember.getInferredFrom()));
            }
        }
        Set<MarkerGoTermAnnotationExtn> mgtaeSet = new HashSet<>();
        Set<String> annotLinks = new HashSet<>();
        if (markerGoTermEvidence.getGoTermAnnotationExtnGroup() != null) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeg : markerGoTermEvidence.getGoTermAnnotationExtnGroup()) {

                for (MarkerGoTermAnnotationExtn mgtae : mgtaeg.getMgtAnnoExtns()) {
                    mgtaeSet.add(mgtae);
                    annotLinks.add(MarkerGoEvidencePresentation.generateAnnotationExtensionLink(mgtae));
                }


            }
        }


        returnDTO.setInferredFrom(inferredFromSet);
        returnDTO.setInferredFromLinks(inferredFromLinks);

        returnDTO.setMgtaeLinks(annotLinks);
        //returnDTO.setInferredFromLinks(inferredFromLinks);

        return returnDTO;
    }

    public static Feature convertToFeature(FeatureDTO featureDTO) throws  ValidationException {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        dateFormat.setLenient(false);
        Date entryDate;
        Feature feature = new Feature();
        feature.setAbbreviation(escapeString(featureDTO.getAbbreviation()));
        feature.setName(escapeString(featureDTO.getName()));

        // these two need to be added, but a trigger fixes them
        feature.setAbbreviationOrder(featureDTO.getAbbreviation());



        if (org.zfin.gwt.root.util.StringUtils.isNotEmpty(featureDTO.getAssemblyInfoDate())) {
            try {
                entryDate = dateFormat.parse(featureDTO.getAssemblyInfoDate());
            } catch (ParseException e) {
                throw new ValidationException("Incorrect date format, please check");
            }
            feature.setFtrAssemblyInfoDate(entryDate);
        } else {
            feature.setFtrAssemblyInfoDate(null);
        }

        feature.setNameOrder(featureDTO.getAbbreviation());

        feature.setType(featureDTO.getFeatureType());
        feature.setDominantFeature(featureDTO.getDominant());
        feature.setKnownInsertionSite(featureDTO.getKnownInsertionSite());

        // if not unspecified
        if (!(featureDTO.getFeatureType().isUnspecified())) {
            if (featureDTO.getLineNumber() != null) {
                feature.setLineNumber(featureDTO.getLineNumber());
            }
            String labPrefix = featureDTO.getLabPrefix();
            if (StringUtils.isNotEmpty(labPrefix)) {
                feature.setFeaturePrefix(RepositoryFactory.getFeatureRepository().getFeatureLabPrefixID(featureDTO.getLabPrefix()));
            }
            feature.setUnspecifiedFeature(false);
        }
        // if unspecified
        else {
            feature.setUnspecifiedFeature(true);
        }

        feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());



        MutationDetailDnaChangeDTO dnaChangeDTO = featureDTO.getDnaChangeDTO();
        if (dnaChangeDTO != null) {
            FeatureDnaMutationDetail detail = convertToDnaMutationDetail(null, dnaChangeDTO);

            detail.setFeature(feature);
            feature.setFeatureDnaMutationDetail(detail);
            if (feature.getType().equals(FeatureTypeEnum.INDEL)) {
                if (feature.getFeatureDnaMutationDetail().getNumberAddedBasePair() == feature.getFeatureDnaMutationDetail().getNumberRemovedBasePair()) {
                    if (feature.getFeatureDnaMutationDetail().getNumberAddedBasePair() > 1) {

                        feature.setType(FeatureTypeEnum.MNV);

                    }
                }
            }
            ;
        }
        MutationDetailProteinChangeDTO proteinChangeDTO = featureDTO.getProteinChangeDTO();
        if (proteinChangeDTO != null) {
            FeatureProteinMutationDetail detail = convertToProteinMutationDetail(null, proteinChangeDTO);
            detail.setFeature(feature);
            feature.setFeatureProteinMutationDetail(detail);
        }

        FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO = featureDTO.getFgmdChangeDTO();
        if (fgmdChangeDTO != null) {
            FeatureGenomicMutationDetail detail = convertToFeatureGenomicMutationDetail(null, fgmdChangeDTO);
            detail.setFeature(feature);
            feature.setFeatureGenomicMutationDetail(detail);

            if (feature.getType().equals(FeatureTypeEnum.INDEL)) {
                if (feature.getFeatureGenomicMutationDetail().getFgmdSeqRef()!=null) {
                    if (feature.getFeatureGenomicMutationDetail().getFgmdSeqVar().length() == feature.getFeatureGenomicMutationDetail().getFgmdSeqRef().length()) {
                        if (feature.getFeatureGenomicMutationDetail().getFgmdSeqVar().length() > 1) {

                            feature.setType(FeatureTypeEnum.MNV);
                        }
                    }
                }

            }
        }

        if (CollectionUtils.isNotEmpty(featureDTO.getTranscriptChangeDTOSet())) {
            for (MutationDetailTranscriptChangeDTO dto : featureDTO.getTranscriptChangeDTOSet()) {
                FeatureTranscriptMutationDetail detail = new FeatureTranscriptMutationDetail();
                detail.setFeature(feature);
                detail.setExonNumber(dto.getExonNumber());
                detail.setIntronNumber(dto.getIntronNumber());
                detail.setTranscriptConsequence(getTranscriptTermRepository().getControlledVocabularyTerm(dto.getConsequenceOboID()));
                feature.addMutationDetailTranscript(detail);
            }
        }
        return feature;
    }

    private static FeatureDnaMutationDetail convertToDnaMutationDetail(FeatureDnaMutationDetail detail, MutationDetailDnaChangeDTO dnaChangeDTO) {
        if (detail == null) {
            detail = new FeatureDnaMutationDetail();
        }
        DnaMutationTerm term = getDnaMutationTermRepository().getControlledVocabularyTerm(dnaChangeDTO.getChangeTermOboId());
        detail.setDnaMutationTerm(term);
        detail.setExonNumber(dnaChangeDTO.getExonNumber());
        detail.setIntronNumber(dnaChangeDTO.getIntronNumber());
        detail.setNumberAddedBasePair(dnaChangeDTO.getNumberAddedBasePair());
        detail.setNumberRemovedBasePair(dnaChangeDTO.getNumberRemovedBasePair());
        /*detail.setInsertedSequence(dnaChangeDTO.getInsertedSequence());
        detail.setDeletedSequence(dnaChangeDTO.getDeletedSequence());*/
        detail.setDnaPositionStart(dnaChangeDTO.getPositionStart());
        detail.setDnaPositionEnd(dnaChangeDTO.getPositionEnd());
        detail.setGeneLocalizationTerm(getGeneLocalizationTermRepository().getControlledVocabularyTerm(dnaChangeDTO.getLocalizationTermOboID()));
        String sequenceReferenceAccessionNumber = dnaChangeDTO.getSequenceReferenceAccessionNumber();

        if (StringUtils.isNotEmpty(sequenceReferenceAccessionNumber)) {

            detail.setDnaSequenceReferenceAccessionNumber(sequenceReferenceAccessionNumber);
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna(sequenceReferenceAccessionNumber);
            if (referenceDatabase != null) {
                detail.setReferenceDatabase(referenceDatabase);
            }

        } else {
            detail.setDnaSequenceReferenceAccessionNumber(sequenceReferenceAccessionNumber);
        }
        return detail;
    }

    public static FeatureTranscriptMutationDetail convertToTranscriptMutationDetail(FeatureTranscriptMutationDetail detail, MutationDetailTranscriptChangeDTO dto) {
        if (detail == null) {
            detail = new FeatureTranscriptMutationDetail();
        }
        detail.setExonNumber(dto.getExonNumber());
        detail.setIntronNumber(dto.getIntronNumber());
        detail.setTranscriptConsequence(getTranscriptTermRepository().getControlledVocabularyTerm(dto.getConsequenceOboID()));

        return detail;
    }
    public static FeatureGenomicMutationDetail convertToFeatureGenomicMutationDetail(FeatureGenomicMutationDetail detail, FeatureGenomeMutationDetailChangeDTO dto) {
        if (detail == null) {
            detail = new FeatureGenomicMutationDetail();
        }

        detail.setFgmdSeqRef(dto.getFgmdSeqRef());
        detail.setFgmdSeqVar(dto.getFgmdSeqVar());
        detail.setFgmdVarStrand("+");


        return detail;
    }

    private static FeatureProteinMutationDetail convertToProteinMutationDetail(FeatureProteinMutationDetail detail,
                                                                               MutationDetailProteinChangeDTO proteinChangeDTO) {
        if (detail == null) {
            detail = new FeatureProteinMutationDetail();
        }
        ProteinConsequence term = getProteinConsequenceTermRepository().getControlledVocabularyTerm(proteinChangeDTO.getConsequenceTermOboID());
        detail.setProteinConsequences(term);
        detail.setNumberAminoAcidsAdded(proteinChangeDTO.getNumberAddedAminoAcid());
        detail.setNumberAminoAcidsRemoved(proteinChangeDTO.getNumberRemovedAminoAcid());
        detail.setNumberAminoAcidsAdded(proteinChangeDTO.getNumberAddedAminoAcid());
        detail.setNumberAminoAcidsRemoved(proteinChangeDTO.getNumberRemovedAminoAcid());
        detail.setProteinPositionStart(proteinChangeDTO.getPositionStart());
        detail.setProteinPositionEnd(proteinChangeDTO.getPositionEnd());
        detail.setWildtypeAminoAcid(getAminoAcidTermRepository().getControlledVocabularyTerm(proteinChangeDTO.getWildtypeAATermOboID()));
        detail.setMutantAminoAcid(getAminoAcidTermRepository().getControlledVocabularyTerm(proteinChangeDTO.getMutantAATermOboID()));
        String sequenceReferenceAccessionNumber = proteinChangeDTO.getSequenceReferenceAccessionNumber();
        if (StringUtils.isNotEmpty(sequenceReferenceAccessionNumber)) {
            detail.setProteinSequenceReferenceAccessionNumber(sequenceReferenceAccessionNumber);
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailProtein(sequenceReferenceAccessionNumber);
            if (referenceDatabase != null) {
                detail.setReferenceDatabase(referenceDatabase);
            }

        }
        return detail;
    }

    public static FeatureDTO convertToFeatureDTO(Feature feature) {
        return convertToFeatureDTO(feature, true);
    }

    public static FeatureDTO convertToFeatureDTO(Feature feature, boolean includeDetails) {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setZdbID(feature.getZdbID());
        featureDTO.setName(unescapeString(feature.getName()));
        featureDTO.setAbbreviation(unescapeString(feature.getAbbreviation()));
        featureDTO.setFeatureType(feature.getType());
        featureDTO.setKnownInsertionSite(feature.getKnownInsertionSite());
        featureDTO.setLink(FeaturePresentation.getLink(feature));
        featureDTO.setTransgenicSuffix(feature.getTransgenicSuffix());
        if (feature.getLineNumber() != null) {
            featureDTO.setLineNumber(feature.getLineNumber());
        }


        featureDTO.setDominant(feature.getDominantFeature());
        featureDTO.setKnownInsertionSite(feature.getKnownInsertionSite());
//       featureDTO.setFeatureSequence(feature.getFeatDBLink().getAccessionNumber());
        FeaturePrefix featurePrefix = feature.getFeaturePrefix();
        if (featurePrefix != null) {
            featureDTO.setLabPrefix(featurePrefix.getPrefixString());
        }
        if (includeDetails) {
            FeatureAssay ftrAssay = feature.getFeatureAssay();
            if (ftrAssay != null) {
                if (feature.getFeatureAssay().getMutagee() != null) {
                    featureDTO.setMutagee(feature.getFeatureAssay().getMutagee().toString());
                }
                if (feature.getFeatureAssay().getMutagen() != null) {
                    featureDTO.setMutagen(feature.getFeatureAssay().getMutagen().toString());
                }
            }
            if (feature.getFtrAssemblyInfoDate()!=null){
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YY");
                String s = formatter.format(feature.getFtrAssemblyInfoDate());
                featureDTO.setAssemblyInfoDate(s);
            }



            //FeatureLocation ftrLocation = RepositoryFactory.getFeatureRepository().getFeatureLocation(feature);
            FeatureLocation ftrLocation = RepositoryFactory.getFeatureRepository().getLocationByFeature(feature);
            if (ftrLocation != null) {
                featureDTO.setFeatureChromosome(ftrLocation.getFtrChromosome());
                featureDTO.setFeatureAssembly(ftrLocation.getFtrAssembly());
                featureDTO.setAssembly(ftrLocation.getFtrAssembly());
                featureDTO.setFeatureStartLoc(ftrLocation.getFtrStartLocation());
                featureDTO.setFeatureEndLoc(ftrLocation.getFtrEndLocation());
                featureDTO.setEvidence(FeatureService.getFeatureGenomeLocationEvidenceCode(ftrLocation.getFtrLocEvidence().getZdbID()));
            }




            Set<FeatureNote> featureNotes = feature.getExternalNotes();

            if (CollectionUtils.isNotEmpty(featureNotes)) {

                List<NoteDTO> curatorNoteDTOs = new ArrayList<>();
                for (FeatureNote dataNote : featureNotes) {
                 if (dataNote.getTag()!=null) {
                     NoteDTO noteDTO = new NoteDTO(dataNote.getZdbID(), feature.getZdbID(), NoteEditMode.PUBLIC, DTOConversionService.unescapeString(dataNote.getNote()), dataNote.getType(), dataNote.getTag());
                     noteDTO.setPublicationDTO(convertToPublicationDTO(dataNote.getPublication()));
                     curatorNoteDTOs.add(noteDTO);
                 }
                 else{
                     NoteDTO noteDTO = new NoteDTO(dataNote.getZdbID(), feature.getZdbID(), NoteEditMode.PUBLIC, DTOConversionService.unescapeString(dataNote.getNote()), dataNote.getType(),"");
                     noteDTO.setPublicationDTO(convertToPublicationDTO(dataNote.getPublication()));
                     curatorNoteDTOs.add(noteDTO);
                 }
                }
                featureDTO.setPublicNoteList(curatorNoteDTOs);
            }



            Set<DataNote> curatorNotes = feature.getDataNotes();
            if (CollectionUtils.isNotEmpty(curatorNotes)) {
                List<CuratorNoteDTO> curatorNoteDTOs = new ArrayList<>();
                for (DataNote dataNote : curatorNotes) {
                    CuratorNoteDTO noteDTO = new CuratorNoteDTO(dataNote.getZdbID(), dataNote.getDataZdbID(), DTOConversionService.unescapeString(dataNote.getNote()));
                    noteDTO.setCurator(convertToPersonDTO(dataNote.getCurator()));
                    curatorNoteDTOs.add(noteDTO);
                }
                featureDTO.setCuratorNotes(curatorNoteDTOs);
            }

            Organization labByFeature = RepositoryFactory.getFeatureRepository().getLabByFeature(feature);
            if (labByFeature != null) {
                featureDTO.setLabOfOrigin(labByFeature.getZdbID());
                logger.debug("Feature does not have a lab: " + feature.getAbbreviation() + " " + feature.getZdbID());
            }
            if (feature.getAliases() != null) {
                featureDTO.setFeatureAliases(new ArrayList<>(unescapeStrings(FeatureService.getFeatureAliases(feature))));
            }
            if (feature.getDbLinks() != null) {
                featureDTO.setFeatureSequences(new ArrayList<>(unescapeStrings(FeatureService.getFeatureSequences(feature))));
            }
        }

        if (includeDetails) {
            if (feature.getFeatureMarkerRelations() != null) {
                for (FeatureMarkerRelationship relationship : feature.getFeatureMarkerRelations()) {
                    if (relationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF)) {
                        featureDTO.setDisplayNameForGenotypeBase(relationship.getMarker().getAbbreviation());
                        featureDTO.setDisplayNameForGenotypeSuperior(feature.getAbbreviation());
                    }
                }
            }
            featureDTO.setProteinChangeDTO(convertToMutationDetailProteinDTO(feature.getFeatureProteinMutationDetail()));
            featureDTO.setDnaChangeDTO(convertToMutationDetailDnaDTO(feature.getFeatureDnaMutationDetail()));
            featureDTO.setFgmdChangeDTO(convertToFeatureGenomicMutationDetailDTO(feature.getFeatureGenomicMutationDetail()));
            if (CollectionUtils.isNotEmpty(feature.getFeatureTranscriptMutationDetailSet())) {
                Set<MutationDetailTranscriptChangeDTO> set = new HashSet<>(3);
                for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
                    set.add(convertToMutationDetailTranscriptDTO(detail));
                }
                featureDTO.setTranscriptChangeDTOSet(set);
            }
        }
        return featureDTO;
    }

    private static MutationDetailTranscriptChangeDTO convertToMutationDetailTranscriptDTO(FeatureTranscriptMutationDetail detail) {
        if (detail == null) {
            return null;
        }
        MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
        dto.setZdbID(detail.getZdbID());
        if (detail.getTranscriptConsequence() != null) {
            dto.setConsequenceOboID(detail.getTranscriptConsequence().getTerm().getOboID());
            dto.setConsequenceName(detail.getTranscriptConsequence().getDisplayName());
        }
        dto.setExonNumber(detail.getExonNumber());
        dto.setIntronNumber(detail.getIntronNumber());
        return dto;
    }

    private static MutationDetailDnaChangeDTO convertToMutationDetailDnaDTO(FeatureDnaMutationDetail detail) {
        if (detail == null) {
            return null;
        }
        MutationDetailDnaChangeDTO dto = new MutationDetailDnaChangeDTO();
        dto.setZdbID(detail.getZdbID());
        if (detail.getDnaMutationTerm() != null) {
            dto.setChangeTermOboId(detail.getDnaMutationTerm().getTerm().getOboID());
        }
        if (detail.getGeneLocalizationTerm() != null) {
            dto.setLocalizationTermOboID(detail.getGeneLocalizationTerm().getTerm().getOboID());
        }
        dto.setNumberAddedBasePair(detail.getNumberAddedBasePair());
        dto.setNumberRemovedBasePair(detail.getNumberRemovedBasePair());
        dto.setPositionStart(detail.getDnaPositionStart());
        dto.setPositionEnd(detail.getDnaPositionEnd());
        /*dto.setInsertedSequence(detail.getInsertedSequence());
        dto.setDeletedSequence(detail.getDeletedSequence());*/
        dto.setExonNumber(detail.getExonNumber());
        dto.setIntronNumber(detail.getIntronNumber());
        dto.setSequenceReferenceAccessionNumber(detail.getDnaSequenceReferenceAccessionNumber());
        return dto;
    }

    private static FeatureGenomeMutationDetailChangeDTO convertToFeatureGenomicMutationDetailDTO(FeatureGenomicMutationDetail detail) {
        if (detail == null) {
            return null;
        }

        FeatureGenomeMutationDetailChangeDTO dto = new FeatureGenomeMutationDetailChangeDTO();
        dto.setZdbID(detail.getZdbID());


        dto.setFgmdSeqVar(detail.getFgmdSeqVar());
        dto.setFgmdSeqRef(detail.getFgmdSeqRef());

        return dto;
    }

    private static MutationDetailProteinChangeDTO convertToMutationDetailProteinDTO(FeatureProteinMutationDetail detail) {
        if (detail == null) {
            return null;
        }
        MutationDetailProteinChangeDTO dto = new MutationDetailProteinChangeDTO();
        dto.setZdbID(detail.getZdbID());
        if (detail.getProteinConsequence() != null) {
            dto.setConsequenceTermOboID(detail.getProteinConsequence().getTerm().getOboID());
        }
        if (detail.getMutantAminoAcid() != null) {
            dto.setMutantAATermOboID(detail.getMutantAminoAcid().getTerm().getOboID());
        }
        if (detail.getWildtypeAminoAcid() != null) {
            dto.setWildtypeAATermOboID(detail.getWildtypeAminoAcid().getTerm().getOboID());
        }
        dto.setNumberAddedAminoAcid(detail.getNumberAminoAcidsAdded());
        dto.setNumberRemovedAminoAcid(detail.getNumberAminoAcidsRemoved());
        dto.setPositionStart(detail.getProteinPositionStart());
        dto.setPositionEnd(detail.getProteinPositionEnd());
        dto.setSequenceReferenceAccessionNumber(detail.getProteinSequenceReferenceAccessionNumber());
        return dto;
    }

    public static CuratorSessionDTO convertToCuratorSessionDTO(CuratorSession session) {
        if (session == null) {
            return null;
        }

        CuratorSessionDTO dto = new CuratorSessionDTO();
        dto.setField(session.getField());
        dto.setPublicationZdbID(session.getPublicationZdbID());
        dto.setValue(session.getValue());
        return dto;
    }

    /**
     * Create a MutantFigureStage object from the corresponding DTO.
     * If a genotype experiment is found for a given genotype/environment it
     * is used on the entity.
     *
     * @param mutantFigureStage Mutant Figure Stage DTO
     * @return mutant figure stage
     */
    public static MutantFigureStage convertToMutantFigureStageFromDTO(PhenotypeExperimentDTO mutantFigureStage) {
        if (mutantFigureStage == null) {
            return null;
        }

        MutantFigureStage mfs = new MutantFigureStage();
        FishExperiment fishExperiment =
                getExpressionRepository().getFishExperimentByExperimentIDAndFishID(mutantFigureStage.getEnvironment().getZdbID(),
                        mutantFigureStage.getFish().getZdbID());
        if (fishExperiment != null) {
            mfs.setGenotypeExperiment(fishExperiment);
        }

        Figure figure = RepositoryFactory.getPublicationRepository().getFigureByID(mutantFigureStage.getFigure().getZdbID());
        mfs.setFigure(figure);

        DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStageByID(mutantFigureStage.getStart().getZdbID());
        mfs.setStart(start);
        DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getStageByID(mutantFigureStage.getEnd().getZdbID());
        mfs.setEnd(end);

        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(mutantFigureStage.getPublicationID());
        mfs.setPublication(publication);

        return mfs;
    }

    public static PhenotypeExperimentDTO convertToPhenotypeFigureStageDTO(PhenotypeExperiment mutantFigureStage) {
        PhenotypeExperimentDTO dto = new PhenotypeExperimentDTO();
        Figure figure = mutantFigureStage.getFigure();
        // retrieve full figure object if not existent
        if (figure.getPublication() == null) {
            figure = RepositoryFactory.getPublicationRepository().getFigureByID(figure.getZdbID());
            dto.setFigure(convertToFigureDTO(figure));
        } else {
            dto.setFigure(convertToFigureDTO(mutantFigureStage.getFigure()));
        }
        dto.setPublicationID(figure.getPublication().getZdbID());
        dto.setEnvironment(convertToExperimentDTO(mutantFigureStage.getFishExperiment().getExperiment()));
        dto.setStart(convertToStageDTO(mutantFigureStage.getStartStage()));
        dto.setEnd(convertToStageDTO(mutantFigureStage.getEndStage()));
        dto.setFish(convertToFishDtoFromFish(mutantFigureStage.getFishExperiment().getFish()));
        Set<PhenotypeStatement> phenoStatements = mutantFigureStage.getPhenotypeStatements();
        List<PhenotypeStatementDTO> phenotypeTerms = new ArrayList<>(5);
        if (phenoStatements != null) {
            for (PhenotypeStatement phenotype : phenoStatements) {
                PhenotypeStatementDTO phenotypeDto = convertToPhenotypeTermDTO(phenotype);
                phenotypeTerms.add(phenotypeDto);
            }
        }
        dto.setExpressedTerms(phenotypeTerms);
        return dto;
    }

    public static PhenotypeStatementDTO convertToPhenotypeTermDTO(PhenotypeStatement phenotype) {
        PhenotypeStatementDTO dto = new PhenotypeStatementDTO();
        dto.setTag(phenotype.getTag());
        dto.setQuality(convertToTermDTO(phenotype.getQuality()));
        dto.setEntity(convertToEntityDTO(phenotype.getEntity()));
        dto.setRelatedEntity(convertToEntityDTO(phenotype.getRelatedEntity()));
        dto.setId(phenotype.getId());
        return dto;
    }

    public static PhenotypeStructure getPhenotypeStructure(PhenotypeStatementDTO phenotypeDTO) throws TermNotFoundException {
        PhenotypeStructure structure = new PhenotypeStructure();
        EntityDTO entityDTO = phenotypeDTO.getEntity();
        structure.setEntity(populatePostComposedEntity(entityDTO));
        structure.setRelatedEntity(populatePostComposedEntity(phenotypeDTO.getRelatedEntity()));
        GenericTerm quality = convertToTerm(phenotypeDTO.getQuality());
        if (quality == null) {
            throw new TermNotFoundException("No valid quality term found: " + phenotypeDTO.getQuality().getTermName() +
                    " and " + phenotypeDTO.getQuality().getOntology());
        }
        structure.setQualityTerm(quality);
        if (phenotypeDTO.getTag() != null) {
            structure.setTag(PhenotypeStatement.Tag.getTagFromName(phenotypeDTO.getTag()));
        }
        return structure;
    }

    private static PostComposedEntity populatePostComposedEntity(EntityDTO entityDTO) throws TermNotFoundException {
        if (entityDTO == null) {
            return null;
        }
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(convertToTerm(entityDTO.getSuperTerm()));
        entity.setSubterm(convertToTerm(entityDTO.getSubTerm()));
        return entity;
    }

    public static FishDTO convertToFishDTOFromGenotype(Genotype genotype) {
        FishDTO dto = new FishDTO();
        dto.setName(genotype.getHandle());
        dto.setZdbID(genotype.getZdbID());
        return dto;
    }

    private static Set<String> convertToAliasDTO(Set<TermAlias> aliases) {
        Set<String> aliasDTOs = new HashSet<>();
        for (TermAlias termAlias : aliases) {
            aliasDTOs.add(termAlias.getAlias());
        }
        return aliasDTOs;
    }

    public static TermDTO convertToTermDTOWithDirectRelationships(GenericTerm term) {
        if (term == null) {
            return null;
        }
        TermDTO dto = convertToTermDTO(term);

        Set<TermDTO> childTerms = new HashSet<>();
        for (GenericTermRelationship termRelationship : term.getChildTermRelationships()) {
            TermDTO childTerm = convertToTermDTO(termRelationship.getTermTwo());
            childTerm.setRelationshipType(termRelationship.getType());
            childTerms.add(childTerm);
        }
        dto.setChildrenTerms(childTerms);

        Set<TermDTO> parentTerms = new HashSet<>();
        for (TermRelationship termRelationship : term.getParentTermRelationships()) {
            TermDTO parentTerm = convertToTermDTO(termRelationship.getTermOne());
            parentTerm.setRelationshipType(termRelationship.getType());
            parentTerms.add(parentTerm);
        }
        dto.setParentTerms(parentTerms);


        return dto;
    }


    /**
     * Given an ontology and a term name.
     *
     * @param termDTO term DTO
     * @return term
     */
    public static GenericTerm convertToTerm(TermDTO termDTO) throws TermNotFoundException {
        if (termDTO == null) {
            return null;
        }
        if (termDTO.getOntology() == null || StringUtils.isEmpty(termDTO.getTermName())) {
            return null;
        }
        Ontology ontology = convertToOntology(termDTO.getOntology());
        // first search by
        GenericTerm term = null;
        if (!StringUtils.isEmpty(termDTO.getOboID())) {
            term = RepositoryFactory.getOntologyRepository().getTermByOboID(termDTO.getOboID());
        }
        if (term == null) {
            term = RepositoryFactory.getOntologyRepository().getTermByName(termDTO.getTermName(), ontology);
        }
        // if no term found throw exception
        if (term == null) {
            throw new TermNotFoundException("Could not find valid term for: " + ontology.getCommonName() + ":" + termDTO.getTermName());
        }
        return term;
    }

    // Shallow means: do not populate synonym and subset collections
    public static TermDTO convertToTermDTO(GenericTerm term, boolean shallow) {
        return convertToTermDTO(term, shallow, false);
    }

    public static TermDTO convertToTermDTO(GenericTerm term, boolean shallow, boolean supershallow) {
        if (term == null) {
            return null;
        }

        TermDTO dto = new TermDTO();
        dto.setZdbID(term.getZdbID());
        dto.setName(term.getTermName());
        dto.setOboID(term.getOboID());
        dto.setObsolete(term.isObsolete());
        dto.setDefinition(term.getDefinition());
        dto.setComment(term.getComment());
        if (!shallow)
            dto.setAliases(convertToAliasDTO(term.getAliases()));

        if (!supershallow && term.getOntology() == Ontology.ANATOMY) {
            DevelopmentStage startStage = OntologyService.getStartStageForTerm(term);
            dto.setStartStage(convertToStageDTO(startStage));
            DevelopmentStage endStage = OntologyService.getEndStageForTerm(term);
            dto.setEndStage(convertToStageDTO(endStage));
        }

        // set the ontology section here
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
        if (!supershallow && ontology == Ontology.QUALITY) {
            ontology = RepositoryFactory.getOntologyRepository().getProcessOrPhysicalObjectQualitySubOntologyForTerm(term);
        }
        if (ontology == Ontology.MPATH) {
            ontology = Ontology.MPATH_NEOPLASM;
        }
        if (!shallow)
            dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        if (!shallow)
            dto.setDoNotAnnotateWith(term.useForAnnotations());
        return dto;
    }

    public static TermDTO convertToTermDTO(GenericTerm term) {
        return convertToTermDTO(term, false);
    }

    public static TermDTO convertQualityToTermDTO(PhenotypeStructure structure) {
        return convertQualityToTermDTO(structure, false);
    }

    public static TermDTO convertQualityToTermDTO(PhenotypeStructure structure, boolean shallow) {
        if (structure == null) {
            return null;
        }

        GenericTerm term = structure.getQualityTerm();
        Ontology entityOntology = getDefiningOntology(structure);
        TermDTO dto = new TermDTO();
        dto.setName(term.getTermName());
        dto.setZdbID(term.getZdbID());
        dto.setObsolete(term.isObsolete());
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
        if (ontology == Ontology.QUALITY) {
            if (entityOntology == Ontology.GO_BP || entityOntology == Ontology.GO_MF) {
                ontology = Ontology.QUALITY_PROCESSES;
            } else {
                ontology = Ontology.QUALITY_QUALITIES;
            }
        }
        if (!shallow) {
            dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
        }
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        dto.setOboID(term.getOboID());
        return dto;
    }

    private static Ontology getDefiningOntology(PhenotypeStructure structure) {
        if (structure == null) {
            return null;
        }
        PostComposedEntity entity = structure.getEntity();
        if (entity == null) {
            return null;
        }
        if (entity.getSubterm() != null) {
            return entity.getSubterm().getOntology();
        }
        return entity.getSuperterm().getOntology();
    }

    private static Set<String> convertToSubsetDTO(Set<Subset> subsets) {
        if (subsets == null) {
            return null;
        }
        Set<String> subsetDtos = new HashSet<>(subsets.size());
        for (Subset subset : subsets) {
            subsetDtos.add(subset.getInternalName());
        }
        return subsetDtos;
    }

    public static FigureDTO convertToFigureDTO(Figure figure) {
        FigureDTO dto = new FigureDTO();
        dto.setZdbID(figure.getZdbID());
        if (figure.getLabel() == null) {
            figure = getPublicationRepository().getFigureByID(figure.getZdbID());
        }
        dto.setLabel(figure.getLabel());
        dto.setOrderingLabel(figure.getOrderingLabel());
        return dto;
    }

    public static StageDTO convertToStageDTO(DevelopmentStage stage) {
        if (stage == null) return null;
        StageDTO dto = new StageDTO();
        dto.setZdbID(stage.getZdbID());
        dto.setNameLong(stage.getNameLong());
        if (stage.getAbbreviation() == null) {
            stage = RepositoryFactory.getAnatomyRepository().getStageByID(stage.getZdbID());
        }
        dto.setName(stage.getAbbreviation() + " " + stage.getTimeString());
        dto.setStartHours(stage.getHoursStart());
        dto.setEndHours(stage.getHoursEnd());
        dto.setAbbreviation(stage.getAbbreviation());
        dto.setTimeString(stage.getTimeString());
        return dto;
    }

    /**
     * Remove underscores in the environment name.
     *
     * @param experiment Experiment
     * @return environmentDTO
     */
    public static ExperimentDTO convertToExperimentDTO(Experiment experiment) {
        ExperimentDTO environment = new ExperimentDTO();

        environment.setZdbID(experiment.getZdbID());
        if (experiment.getName() == null) {
            experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(experiment.getZdbID());
        }
        if (experiment.getName().startsWith("_")) {
            environment.setName(experiment.getName().substring(1));
        } else {
            environment.setName(experiment.getName());
        }


        if (CollectionUtils.isNotEmpty(experiment.getExperimentConditions())) {
            List<ConditionDTO> list = new ArrayList<>();
            for (ExperimentCondition condition : experiment.getExperimentConditions()) {
                ConditionDTO dto = new ConditionDTO();
                dto.setZdbID(condition.getZdbID());
                dto.setEnvironmentZdbID(experiment.getZdbID());
                dto.setZecoTerm(DTOConversionService.convertToTermDTO(condition.getZecoTerm()));
                dto.setAoTerm(DTOConversionService.convertToTermDTO(condition.getAoTerm()));
                dto.setGoCCTerm(DTOConversionService.convertToTermDTO(condition.getGoCCTerm()));
                dto.setTaxonTerm(DTOConversionService.convertToTermDTO(condition.getTaxaonymTerm()));
                dto.setChebiTerm(DTOConversionService.convertToTermDTO(condition.getChebiTerm()));
                list.add(dto);
            }
            environment.setConditionDTOList(list);

        }

        return environment;
    }

    public static ExperimentDTO convertToEnvironmentTabDTO(Experiment experiment) {
        ExperimentDTO environment = new ExperimentDTO();
        environment.setZdbID(experiment.getZdbID());
        if (experiment.getName() == null) {
            experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(experiment.getZdbID());
        }
        if (experiment.getName().startsWith("_")) {
            environment.setName(experiment.getName().substring(1));
        } else {
            environment.setName(experiment.getName());
        }


        if (CollectionUtils.isNotEmpty(experiment.getExperimentConditions())) {
            List<ConditionDTO> list = new ArrayList<>();
            for (ExperimentCondition condition : experiment.getExperimentConditions()) {
                ConditionDTO dto = new ConditionDTO();
                dto.setZdbID(condition.getZdbID());
                dto.setEnvironmentZdbID(experiment.getZdbID());
                dto.setZecoTerm(DTOConversionService.convertToTermDTO(condition.getZecoTerm()));
                dto.setAoTerm(DTOConversionService.convertToTermDTO(condition.getAoTerm()));
                dto.setGoCCTerm(DTOConversionService.convertToTermDTO(condition.getGoCCTerm()));
                dto.setTaxonTerm(DTOConversionService.convertToTermDTO(condition.getTaxaonymTerm()));
                dto.setChebiTerm(DTOConversionService.convertToTermDTO(condition.getChebiTerm()));
                dto.setSpatialTerm(DTOConversionService.convertToTermDTO(condition.getSpatialTerm()));
                list.add(dto);
            }
            environment.setConditionDTOList(list);

        }

        environment.setUsedInDisease(false);
        environment.setUsedInExpression(false);
        environment.setUsedInPhenotype(false);
        convertToExperimentDTO(experiment);

        if (CollectionUtils.isNotEmpty(RepositoryFactory.getExpressionRepository().getExpressionByExperiment(experiment.getZdbID()))) {
            environment.setUsedInExpression(true);

        }

        if (CollectionUtils.isNotEmpty(RepositoryFactory.getPhenotypeRepository().getPhenoByExperimentID(experiment.getZdbID()))) {
            environment.setUsedInPhenotype(true);
        }

        if (CollectionUtils.isNotEmpty(RepositoryFactory.getPhenotypeRepository().getHumanDiseaseModelsByExperiment(experiment.getZdbID()))) {
            environment.setUsedInDisease(true);
        }


        return environment;
    }


    public static PhenotypePileStructureDTO convertToPhenotypePileStructureDTO(PhenotypeStructure structure) {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(structure.getZdbID());
        dto.setPhenotypeTerm(convertToPhenotypeTermDTO(structure, true));
        dto.setCreator(structure.getPerson().getShortName());
        dto.setDate(structure.getDate());
        return dto;
    }

    public static PhenotypeStatementDTO convertToPhenotypeTermDTO(PhenotypeStructure structure) {
        return convertToPhenotypeTermDTO(structure, false);
    }

    public static PhenotypeStatementDTO convertToPhenotypeTermDTO(PhenotypeStructure structure, boolean shallow) {
        PhenotypeStatementDTO phenotypeTerm = new PhenotypeStatementDTO();
        TermDTO quality = convertQualityToTermDTO(structure, true);
        phenotypeTerm.setQuality(quality);
        phenotypeTerm.setEntity(convertToEntityDTO(structure.getEntity(), shallow, shallow));
        phenotypeTerm.setRelatedEntity(convertToEntityDTO(structure.getRelatedEntity(), shallow, shallow));
        phenotypeTerm.setTag(structure.getTag().toString());
        return phenotypeTerm;
    }

    public static ExpressionExperimentDTO convertToExperimentDTO(ExpressionExperiment experiment) {
        ExpressionExperimentDTO experimentDTO = new ExpressionExperimentDTO();
        experimentDTO.setExperimentZdbID(experiment.getZdbID());
        Marker gene = experiment.getGene();
        if (gene != null) {
            experimentDTO.setGene(convertToMarkerDTO(gene));
            if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                experimentDTO.setGenbankNumber(dblink);
                experimentDTO.setGenbankID(experiment.getMarkerDBLink().getZdbID());
            }
        }
        if (experiment.getAntibody() != null) {
            experimentDTO.setAntibodyMarker(convertToMarkerDTO(experiment.getAntibody()));
        }
        experimentDTO.setFishName(experiment.getFishExperiment().getFish().getHandle());
        experimentDTO.setFishID(experiment.getFishExperiment().getFish().getZdbID());
        experimentDTO.setEnvironment(convertToExperimentDTO(experiment.getFishExperiment().getExperiment()));
        experimentDTO.setAssay(experiment.getAssay().getName());
        experimentDTO.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
        experimentDTO.setGenotypeExperimentID(experiment.getFishExperiment().getZdbID());
        experimentDTO.setPublicationID(experiment.getPublication().getZdbID());
        // check if there are expressions associated
        Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
        if (expressionResults != null) {
            experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());
        }
        // check if a clone is available
        Marker probe = experiment.getProbe();
        if (probe != null) {
            experimentDTO.setCloneID(probe.getZdbID());
            experimentDTO.setCloneName(probe.getAbbreviation() + " [" + probe.getType().toString() + "]");
        }
        return experimentDTO;
    }

    public static PhenotypeExperiment convertToPhenotypeExperimentFilter(PhenotypeExperimentDTO dto) {
        if (dto == null) {
            return null;
        }

        PhenotypeExperiment phenoExperiment = new PhenotypeExperiment();
        phenoExperiment.setId(dto.getId());
        Fish genotype = new Fish();
        genotype.setZdbID(dto.getFish().getZdbID());
        Experiment environment = new Experiment();
        environment.setZdbID(dto.getEnvironment().getZdbID());
        FishExperiment fishExperiment = new FishExperiment();
        fishExperiment.setFish(genotype);
        fishExperiment.setExperiment(environment);
        phenoExperiment.setFishExperiment(fishExperiment);
        phenoExperiment.setStartStage(convertToDevelopmentStage(dto.getStart()));
        phenoExperiment.setEndStage(convertToDevelopmentStage(dto.getEnd()));
        phenoExperiment.setFigure(convertToFigure(dto.getFigure()));
        Publication pub = new Publication();
        pub.setZdbID(dto.getPublicationID());
        return phenoExperiment;
    }

    public static Figure convertToFigure(FigureDTO figureDto) {
        Figure figure = new FigureFigure();
        figure.setZdbID(figureDto.getZdbID());
        figure.setLabel(figureDto.getLabel());
        return figure;
    }

    public static DevelopmentStage convertToDevelopmentStage(StageDTO start) {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setZdbID(start.getZdbID());
        stage.setName(start.getName());
        return stage;
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(ComposedFxTerm term) {
        ExpressedTermDTO termDto = new ExpressedTermDTO();
        EntityDTO entity = new EntityDTO();
        entity.setSuperTerm(convertToTermDTO(term.getSuperTerm()));
        if (term.getSubterm() != null) {
            entity.setSubTerm(convertToTermDTO(term.getSubterm()));
        }
        termDto.setEntity(entity);
        termDto.setExpressionFound(term.isExpressionFound());
        termDto.setZdbID(term.getZdbID());
        return termDto;
    }

    public static ExpressionPileStructureDTO convertToExpressionPileStructureDTO(ExpressionStructure es) {
        ExpressionPileStructureDTO dto = new ExpressionPileStructureDTO();
        dto.setZdbID(es.getZdbID());
        dto.setCreator(es.getPerson().getShortName());
        dto.setDate(es.getDate());
        ExpressedTermDTO expressionTerm = convertToExpressedTermDTO(es);
        if (es.getEapQualityTerm() != null) {
            expressionTerm.setQualityTerm(convertToEapQualityTermDTO(es));
        }
        dto.setExpressedTerm(expressionTerm);
        if (es.getSuperterm().getOntology().equals(Ontology.ANATOMY)) {
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(es.getSuperterm().getZdbID());
            StageDTO start = convertToStageDTO(term.getStart());
            StageDTO end = convertToStageDTO(term.getEnd());
            dto.setStart(start);
            dto.setEnd(end);
        }
        return dto;
    }

    public static EapQualityTermDTO convertToEapQualityTermDTO(ExpressionStructure structure) {
        if (structure.getEapQualityTerm() == null) {
            return null;
        }
        EapQualityTermDTO dto = new EapQualityTermDTO();
        dto.setTerm(convertToTermDTO(structure.getEapQualityTerm()));
        dto.setTag(structure.getTag());
        return dto;
    }


    public static OntologyDTO convertToOntologyDTO(Ontology ontology) {
        if (ontology == null) {
            return null;
        }

        return OntologyDTO.valueOf(ontology.name());
    }

    public static Ontology convertToOntology(OntologyDTO ontology) {
        if (ontology == null) {
            return null;
        }

        return Ontology.valueOf(ontology.name());
    }

    public static FeatureMarkerRelationshipDTO convertToFeatureMarkerRelationshipDTO(FeatureMarkerRelationship featureMarkerRelationship) {
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = new FeatureMarkerRelationshipDTO();

        featureMarkerRelationshipDTO.setZdbID(featureMarkerRelationship.getZdbID());
        featureMarkerRelationshipDTO.setRelationshipType(featureMarkerRelationship.getFeatureMarkerRelationshipType().getName());
        featureMarkerRelationshipDTO.setFeatureDTO(convertToFeatureDTO(featureMarkerRelationship.getFeature(), false));
        featureMarkerRelationshipDTO.setMarkerDTO(convertToMarkerDTO(featureMarkerRelationship.getMarker()));

        return featureMarkerRelationshipDTO;
    }

    public static MarkerRelationshipDTO convertToMarkerRelationshipDTO(MarkerRelationship markerRelationship) {
        MarkerRelationshipDTO markerRelationshipDTO = new MarkerRelationshipDTO();

        markerRelationshipDTO.setZdbID(markerRelationship.getZdbID());
        markerRelationshipDTO.setRelationshipType(markerRelationship.getMarkerRelationshipType().getName());
        markerRelationshipDTO.setConstructDTO(convertToMarkerDTO(markerRelationship.getFirstMarker()));
        markerRelationshipDTO.setMarkerDTO(convertToMarkerDTO(markerRelationship.getSecondMarker()));

        return markerRelationshipDTO;
    }

    public static ConstructRelationshipDTO convertToConstructRelationshipDTO(ConstructRelationship constructRelationship) {
        ConstructRelationshipDTO ConstructRelationshipDTO = new ConstructRelationshipDTO();

        ConstructRelationshipDTO.setZdbID(constructRelationship.getZdbID());
        ConstructRelationshipDTO.setRelationshipType(constructRelationship.getType().toString());
        ConstructRelationshipDTO.setConstructDTO(convertToConstructDTO(constructRelationship.getConstruct()));
        ConstructRelationshipDTO.setMarkerDTO(convertToMarkerDTO(constructRelationship.getMarker()));

        return ConstructRelationshipDTO;
    }

    public static LabDTO convertToLabDTO(Lab lab) {
        LabDTO labDTO = new LabDTO();
        labDTO.setZdbID(lab.getZdbID());
        labDTO.setName(lab.getName());
        return labDTO;
    }

    public static List<LabDTO> convertToLabDTO(List<Lab> labsOfOrigin) {
        List<LabDTO> labDTO = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(labsOfOrigin)) {
            for (Lab lab : labsOfOrigin) {
                labDTO.add(DTOConversionService.convertToLabDTO(lab));
            }
        }
        return labDTO;
    }

    public static OrganizationDTO convertToOrganizationDTO(Organization lab) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setZdbID(lab.getZdbID());
        organizationDTO.setName(lab.getName());
        return organizationDTO;
    }

    public static List<OrganizationDTO> convertToOrganizationDTO(List<Organization> labsOfOrigin) {
        List<OrganizationDTO> organizationDTO = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(labsOfOrigin)) {
            for (Organization lab : labsOfOrigin) {
                organizationDTO.add(DTOConversionService.convertToOrganizationDTO(lab));
            }
        }
        return organizationDTO;
    }

    public static FeaturePrefixDTO convertToFeaturePrefixDTO(FeaturePrefix featurePrefix) {
        FeaturePrefixDTO featurePrefixDTO = new FeaturePrefixDTO();
        featurePrefixDTO.setPrefix(featurePrefix.getPrefixString());
        featurePrefixDTO.setActive(featurePrefix.isActiveForSet());
        return featurePrefixDTO;
    }

    public static List<FeaturePrefixDTO> convertToFeaturePrefixDTO(List<FeaturePrefix> labPrefixes) {
        List<FeaturePrefixDTO> featurePrefixDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(labPrefixes)) {
            for (FeaturePrefix featurePrefix : labPrefixes) {
                featurePrefixDTOs.add(convertToFeaturePrefixDTO(featurePrefix));
            }
        }
        return featurePrefixDTOs;  //To change body of created methods use File | Settings | File Templates.
    }

    public static void escapeFeatureDTO(FeatureDTO featureDTO) {
        featureDTO.setName(escapeString(featureDTO.getName()));
        featureDTO.setAbbreviation(escapeString(featureDTO.getAbbreviation()));
        featureDTO.setAlias(escapeString(featureDTO.getAlias()));
        featureDTO.setFeatureSequence(escapeString(featureDTO.getFeatureSequence()));
        featureDTO.setOptionalName(escapeString(featureDTO.getOptionalName()));

    }

    public static EntityDTO convertToEntityDTO(PostComposedEntity entity) {
        return convertToEntityDTO(entity, false);
    }

    public static EntityDTO convertToEntityDTO(PostComposedEntity entity, boolean shallow) {
        return convertToEntityDTO(entity, shallow, false);
    }

    public static EntityDTO convertToEntityDTO(PostComposedEntity entity, boolean shallow, boolean superShallow) {
        if (entity == null) {
            return null;
        }

        EntityDTO dto = new EntityDTO();
        dto.setSuperTerm(convertToTermDTO(entity.getSuperterm(), shallow, superShallow));
        dto.setSubTerm(convertToTermDTO(entity.getSubterm(), shallow, superShallow));
        return dto;
    }

    public static PhenotypeStructure convertToPhenotypeStructure(PhenotypeStatement phenotypeStatement) {
        PhenotypeStructure phenoStructure = new PhenotypeStructure();
        phenoStructure.setEntity(phenotypeStatement.getEntity());
        phenoStructure.setRelatedEntity(phenotypeStatement.getRelatedEntity());
        phenoStructure.setQualityTerm(phenotypeStatement.getQuality());
        phenoStructure.setTag(PhenotypeStatement.Tag.getTagFromName(phenotypeStatement.getTag()));
        return phenoStructure;
    }

    /**
     * Note that this conversion does not populate the expression found attribute.
     *
     * @param expressionStructure ExpressionStructure
     * @return ExpressedTermDTO
     */
    public static ExpressedTermDTO convertToExpressedTermDTO(ExpressionStructure expressionStructure) {
        ExpressedTermDTO expressedDTO = new ExpressedTermDTO();
        EntityDTO entity = new EntityDTO();
        entity.setSuperTerm(DTOConversionService.convertToTermDTO(expressionStructure.getSuperterm(), true));
        entity.setSubTerm(DTOConversionService.convertToTermDTO(expressionStructure.getSubterm(), true));
        expressedDTO.setEntity(entity);
        if (expressionStructure.getEapQualityTerm() != null) {
            expressedDTO.setQualityTerm(convertToEapQualityTermDTO(expressionStructure));
        }
        expressedDTO.setExpressionFound(expressionStructure.isExpressionFound());
        return expressedDTO;
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(PostComposedEntity postComposedEntity) {
        ExpressedTermDTO expressedDTO = new ExpressedTermDTO();
        EntityDTO entity = new EntityDTO();
        entity.setSuperTerm(DTOConversionService.convertToTermDTO(postComposedEntity.getSuperterm()));
        entity.setSubTerm(DTOConversionService.convertToTermDTO(postComposedEntity.getSubterm()));
        expressedDTO.setEntity(entity);
        return expressedDTO;
    }

    /**
     * Populates the superterm only.
     * expression found = false
     *
     * @param term Term
     * @return ExpressedTermDTO
     */
    public static ExpressedTermDTO convertToExpressedTermDTO(GenericTerm term) {
        ExpressedTermDTO expressedDTO = new ExpressedTermDTO();
        EntityDTO entity = new EntityDTO();
        entity.setSuperTerm(DTOConversionService.convertToTermDTO(term));
        expressedDTO.setEntity(entity);
        return expressedDTO;
    }

    public static List<PileStructureAnnotationDTO> getPileStructureDTO(ExpressionFigureStageDTO expressionFigureStageDTO, PileStructureAnnotationDTO.Action action) {
        if (expressionFigureStageDTO == null) {
            return null;
        }

        List<PileStructureAnnotationDTO> list = new ArrayList<>();
        for (ExpressedTermDTO termDto : expressionFigureStageDTO.getExpressedTerms()) {
            // check if there is more than one EaP quality. Each quality needs to
            // create an independent PileStructureAnnotationDTO object
            if (termDto.getQualityTermDTOList() != null && termDto.getQualityTermDTOList().size() > 1) {
                for (EapQualityTermDTO dto : termDto.getQualityTermDTOList()) {
                    ExpressedTermDTO expressedTermDTO = new ExpressedTermDTO();
                    expressedTermDTO.setId(termDto.getId());
                    expressedTermDTO.setEntity(termDto.getEntity());
                    expressedTermDTO.setExpressionFound(termDto.isExpressionFound());
                    expressedTermDTO.setZdbID(termDto.getZdbID());
                    expressedTermDTO.setQualityTerm(dto);
                    PileStructureAnnotationDTO annotation = getPileStructureAnnotationDTO(expressionFigureStageDTO, action, expressedTermDTO);
                    list.add(annotation);
                }
            } else {
                PileStructureAnnotationDTO annotation = getPileStructureAnnotationDTO(expressionFigureStageDTO, action, termDto);
                list.add(annotation);
            }
        }
        return list;
    }

    private static PileStructureAnnotationDTO getPileStructureAnnotationDTO(ExpressionFigureStageDTO expressionFigureStageDTO, PileStructureAnnotationDTO.Action action, ExpressedTermDTO termDto) {
        PileStructureAnnotationDTO annotation = new PileStructureAnnotationDTO();
        annotation.setAction(action);
        annotation.setExpressed(termDto.isExpressionFound());
        annotation.setStart(expressionFigureStageDTO.getStart());
        annotation.setEnd(expressionFigureStageDTO.getEnd());
        annotation.setExpressed(termDto.isExpressionFound());
        annotation.setExpressedTerm(termDto);
        return annotation;
    }

    public static ExpressionStructure getExpressionStructureFromDTO(ExpressedTermDTO expressedTerm) {
        if (expressedTerm == null) {
            return null;
        }
        ExpressionStructure entity = new ExpressionStructure();
        try {
            EntityDTO entityDTO = expressedTerm.getEntity();
            entity.setSuperterm(convertToTerm(entityDTO.getSuperTerm()));
            if (entityDTO.getSubTerm() != null) {
                entity.setSubterm(convertToTerm(entityDTO.getSubTerm()));
            }
            entity.setExpressionFound(expressedTerm.isExpressionFound());
            if (expressedTerm.getQualityTerm() != null) {
                entity.setEapQualityTerm(convertToTerm(expressedTerm.getQualityTerm().getTerm()));
                entity.setTag(expressedTerm.getQualityTerm().getTag());
            }
            if (CollectionUtils.isNotEmpty(expressedTerm.getQualityTermDTOList())) {
                EapQualityTermDTO qualityTermDTO = expressedTerm.getQualityTermDTOList().get(0);
                entity.setEapQualityTerm(convertToTerm(qualityTermDTO.getTerm()));
                entity.setTag(qualityTermDTO.getTag());
            }
        } catch (TermNotFoundException e) {
            return null;
        }
        return entity;
    }

    public static ExpressionFigureStage getExpressionFigureStageFromDTO(ExpressedTermDTO expressedTerm) {
        if (expressedTerm == null) {
            return null;
        }
        ExpressionFigureStage entity = new ExpressionFigureStage();
/*
        try {
            EntityDTO entityDTO = expressedTerm.getEntity();
            entity.setSuperterm(convertToTerm(entityDTO.getSuperTerm()));
            if (entityDTO.getSubTerm() != null) {
                entity.setSubterm(convertToTerm(entityDTO.getSubTerm()));
            }
        } catch (TermNotFoundException e) {
            return null;
        }
*/
        return entity;
    }

    public static RelatedEntityDTO convertStrToRelatedEntityDTO(SequenceTargetingReagent str) {
        RelatedEntityDTO entity = new RelatedEntityDTO();
        entity.setName(str.getName());
        entity.setZdbID(str.getZdbID());
        return entity;
    }

    public static DiseaseAnnotationModelDTO convertDamoToDamoDTO(DiseaseAnnotationModel str) {
        DiseaseAnnotationModelDTO entity = new DiseaseAnnotationModelDTO();
        entity.setEnvironment(convertToExperimentDTO(str.getFishExperiment().getExperiment()));
        entity.setFish(convertToFishDtoFromFish(str.getFishExperiment().getFish()));
        entity.setDamoID(str.getID());

        return entity;
    }

    public static Fish convertToFishFromFishDTO(FishDTO newFish) {
        Fish fish = new Fish();
        if (newFish.getZdbID() != null) {
            fish.setZdbID(newFish.getZdbID());
        }
        fish.setGenotype(convertToGenotypeFromGenotypeDTO(newFish.getGenotypeDTO()));
        fish.setName(fish.getGenotype().getName());
        fish.setNameOrder(fish.getName());
        // fish.setOrder(0);
        fish.setHandle(fish.getGenotype().getHandle());
        String strName = "";
        if (CollectionUtils.isNotEmpty(newFish.getStrList())) {
            Collections.sort(newFish.getStrList());
            List<SequenceTargetingReagent> strList = new ArrayList<>(newFish.getStrList().size());
            for (RelatedEntityDTO dto : newFish.getStrList()) {
                SequenceTargetingReagent str = getMarkerRepository().getSequenceTargetingReagent(dto.getZdbID());
                strList.add(str);
                strName += str.getName() + "+";
            }
            strName = strName.substring(0, strName.length() - 1);
            fish.setStrList(strList);
            fish.setHandle(fish.getGenotype().getHandle() + "+" + strName);
            fish.setName(fish.getGenotype().getName() + "+" + strName);
        }

        return fish;
    }

    public static Genotype convertToGenotypeFromGenotypeDTO(GenotypeDTO genotypeDTO) {
        if (genotypeDTO.getZdbID() != null) {
            return getMutantRepository().getGenotypeByID(genotypeDTO.getZdbID());
        }
        Genotype genotype = new Genotype();
        genotype.setName(genotypeDTO.getName());
        return genotype;
    }

    public static FishDTO convertToFishDtoFromFish(Fish fish, boolean shallow) {
        FishDTO dto = new FishDTO();
        dto.setZdbID(fish.getZdbID());
        dto.setName(fish.getDisplayName());
        dto.setHandle(fish.getHandle());
        dto.setWildtype(fish.isWildtype());
        if (!shallow) {
            dto.setGenotypeDTO(DTOConversionService.convertToGenotypeDTO(fish.getGenotype(), false));
            if (CollectionUtils.isNotEmpty(fish.getStrList())) {
                List<RelatedEntityDTO> strs = new ArrayList<>(fish.getStrList().size());
                for (SequenceTargetingReagent str : fish.getStrList()) {
                    strs.add(DTOConversionService.convertStrToRelatedEntityDTO(str));
                }
                dto.setStrList(strs);
            }
        }
        dto.setOrder(fish.getOrder());
        dto.setNameOrder(fish.getNameOrder());
        return dto;

    }

    public static FishDTO convertToFishDtoFromFish(Fish fish) {
        return convertToFishDtoFromFish(fish, false);
    }

    public static DiseaseAnnotation convertToDiseaseFromDiseaseDTO(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException {
        DiseaseAnnotation diseaseAnnotation = new DiseaseAnnotation();
        diseaseAnnotation.setDisease(convertToTerm(diseaseAnnotationDTO.getDisease()));
        diseaseAnnotation.setPublication(convertToPublication(diseaseAnnotationDTO.getPublication()));
        diseaseAnnotation.setEvidenceCode(diseaseAnnotationDTO.getEvidenceCode());


        return diseaseAnnotation;
    }

    public static DiseaseAnnotationModel convertToDiseaseModelFromDiseaseDTO(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException {

        DiseaseAnnotationModel damo = new DiseaseAnnotationModel();
        if (diseaseAnnotationDTO.getFish() != null && diseaseAnnotationDTO.getFish().getZdbID() != null) {
            FishExperiment model = DTOConversionService.convertToFishModel(diseaseAnnotationDTO);
            damo.setFishExperiment(model);

        }


        return damo;
    }

    private static FishExperiment convertToFishModel(DiseaseAnnotationDTO diseaseAnnotationDTO) {
        FishExperiment model = new FishExperiment();
        model.setFish(getMutantRepository().getFish(diseaseAnnotationDTO.getFish().getZdbID()));
        Experiment experiment = getExpressionRepository().getExperimentByID(diseaseAnnotationDTO.getEnvironment().getZdbID());
        model.setExperiment(experiment);
        model.setStandard(experiment.isOnlyStandard());
        model.setStandardOrGenericControl(experiment.isStandard());
        return model;
    }

    private static Publication convertToPublication(PublicationDTO pub) {
        return getPublicationRepository().getPublication(pub.getZdbID());
    }

    public static DiseaseAnnotationDTO convertToDiseaseModelDTO(DiseaseAnnotation model) {
        DiseaseAnnotationDTO dto = new DiseaseAnnotationDTO();
        dto.setPublication(convertToPublicationDTO(model.getPublication()));

        dto.setDisease(convertToTermDTO(model.getDisease()));
        if (model.getEvidenceCode().equals("ZDB-TERM-170419-250")) {
            dto.setEvidenceCode("TAS");
        }
        if (model.getEvidenceCode().equals("ZDB-TERM-170419-251")) {
            dto.setEvidenceCode("IC");
        }
        //dto.setEvidenceCode(model.getEvidenceCode());
        dto.setZdbID(model.getZdbID());
        List<DiseaseAnnotationModel> dam = getMutantRepository().getDiseaseAnnotationModelByZdb(model.getZdbID());
        if (CollectionUtils.isNotEmpty(dam)) {
            List<DiseaseAnnotationModelDTO> damoDTO = new ArrayList<>(dam.size());
            for (DiseaseAnnotationModel damo : dam) {
                DiseaseAnnotationModelDTO annotationModelDTO = DTOConversionService.convertDamoToDamoDTO(damo);
                annotationModelDTO.setDat(dto);
                damoDTO.add(annotationModelDTO);
            }
            dto.setDamoDTO(damoDTO);
        } else {
            dto.setDamoDTO(null);
        }

        return dto;
    }

    public static ZygosityDTO convertToZygosityDTO(Zygosity zygosity) {
        ZygosityDTO dto = new ZygosityDTO();
        dto.setName(zygosity.getName());
        dto.setZdbID(zygosity.getZdbID());
        return dto;
    }

    public static OrthologDTO convertToOrthologDTO(Ortholog ortholog) {
        OrthologDTO orthologDTO = new OrthologDTO();
        orthologDTO.setZdbID(ortholog.getZdbID());
        orthologDTO.setZebrafishGene(DTOConversionService.convertToMarkerDTO(ortholog.getZebrafishGene()));
        orthologDTO.setNcbiOtherSpeciesGeneDTO(DTOConversionService.convertToNcbiOtherSpeciesGeneDTO(ortholog.getNcbiOtherSpeciesGene()));

        orthologDTO.setName(ortholog.getName());
        orthologDTO.setChromosome(ortholog.getChromosome());
        orthologDTO.setSymbol(ortholog.getSymbol());
        Set<OrthologEvidenceDTO> orthologEvidenceDTOs = new HashSet<>();
        if (CollectionUtils.isNotEmpty(ortholog.getEvidenceSet())) {
            for (OrthologEvidence evidence : ortholog.getEvidenceSet()) {
                orthologEvidenceDTOs.add(DTOConversionService.convertToOrthologEvidenceDTO(evidence));
            }
        }
        orthologDTO.setEvidenceSet(orthologEvidenceDTOs);

        if (ortholog.getExternalReferenceList() != null) {
            Set<OrthologExternalReferenceDTO> referenceDTOList = new LinkedHashSet<>();
            for (OrthologExternalReference reference : ortholog.getExternalReferenceList()) {
                referenceDTOList.add(convertToOrthologExternalReferenceDTO(reference));
            }
            orthologDTO.setOrthologExternalReferenceDTOSet(referenceDTOList);
        }

        return orthologDTO;
    }

    public static OrthologEvidenceDTO convertToOrthologEvidenceDTO(OrthologEvidence evidence) {
        OrthologEvidenceDTO dto = new OrthologEvidenceDTO();
        dto.setEvidenceCode(evidence.getEvidenceCode().getCode());
        dto.setEvidenceName(evidence.getEvidenceCode().getName());
        dto.setEvidenceTerm(DTOConversionService.convertToTermDTO(evidence.getEvidenceTerm()));

        dto.setPublication(DTOConversionService.convertToPublicationDTO(evidence.getPublication()));
        return dto;
    }

    public static NcbiOtherSpeciesGeneDTO convertToNcbiOtherSpeciesGeneDTO(NcbiOtherSpeciesGene ncbiGene) {
        if (ncbiGene == null) {
            return null;
        }
        NcbiOtherSpeciesGeneDTO geneDTO = new NcbiOtherSpeciesGeneDTO();
        geneDTO.setID(ncbiGene.getID());
        geneDTO.setAbbreviation(ncbiGene.getAbbreviation());
        geneDTO.setName(ncbiGene.getName());
        geneDTO.setChromosome(ncbiGene.getChromosome());
        geneDTO.setOrganism(ncbiGene.getOrganism().getCommonName());
        return geneDTO;
    }

    public static OrthologExternalReferenceDTO convertToOrthologExternalReferenceDTO(OrthologExternalReference reference) {
        OrthologExternalReferenceDTO dto = new OrthologExternalReferenceDTO();
        dto.setAccessionNumber(reference.getAccessionNumber());
        dto.setReferenceDatabaseDTO(convertToReferenceDatabaseDTO(reference.getReferenceDatabase()));
        dto.setSymbol(reference.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation());
        return dto;
    }

    public static PersonDTO convertToPersonDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setDisplay(person.getFullName());
        dto.setZdbID(person.getZdbID());
        dto.setEmail(person.getEmail());
        return dto;
    }

    public static ExpressionExperimentDTO convertToExperimentDTO(ExpressionExperiment2 experiment) {
        ExpressionExperimentDTO experimentDTO = new ExpressionExperimentDTO();
        experimentDTO.setExperimentZdbID(experiment.getZdbID());
        Marker gene = experiment.getGene();
        if (gene != null) {
            experimentDTO.setGene(convertToMarkerDTO(gene));
            if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                experimentDTO.setGenbankNumber(dblink);
                experimentDTO.setGenbankID(experiment.getMarkerDBLink().getZdbID());
            }
        }
        if (experiment.getAntibody() != null) {
            experimentDTO.setAntibodyMarker(convertToMarkerDTO(experiment.getAntibody()));
        }
        experimentDTO.setFishName(experiment.getFishExperiment().getFish().getHandle());
        experimentDTO.setFishID(experiment.getFishExperiment().getFish().getZdbID());
        experimentDTO.setFishDTO(convertToFishDtoFromFish(experiment.getFishExperiment().getFish(), true));
        experimentDTO.setEnvironment(convertToExperimentDTO(experiment.getFishExperiment().getExperiment()));
        experimentDTO.setAssay(experiment.getAssay().getName());
        experimentDTO.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
        experimentDTO.setGenotypeExperimentID(experiment.getFishExperiment().getZdbID());
        experimentDTO.setPublicationID(experiment.getPublication().getZdbID());
        // check if there are expressions associated
        Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
        if (expressionResults != null) {
            experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());
        }
        // check if a clone is available
        Marker probe = experiment.getProbe();
        if (probe != null) {
            experimentDTO.setCloneID(probe.getZdbID());
            experimentDTO.setCloneName(probe.getAbbreviation() + " [" + probe.getType().toString() + "]");
        }
        return experimentDTO;
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(ExpressionResult2 result) {
        return convertToExpressedTermDTO(result, false);
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(ExpressionResult2 result, boolean shallow) {
        return convertToExpressedTermDTO(result, shallow, true);
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(ExpressionResult2 result, boolean shallow, boolean superShallow) {
        ExpressedTermDTO dto = new ExpressedTermDTO();
        dto.setExpressionFound(result.isExpressionFound());
        dto.setEntity(convertToEntityDTO(result.getEntity(), shallow, superShallow));
        dto.setId(result.getID());
        List<EapQualityTermDTO> dtoList = new ArrayList<>();
        if (result.getPhenotypeTermSet() != null) {
            for (ExpressionPhenotypeTerm qualTerm : result.getPhenotypeTermSet()) {
                dtoList.add(convertToEapQualityTermDTO(qualTerm));
            }
            dto.setQualityTermDTOList(dtoList);
        }
        return dto;
    }

    private static EapQualityTermDTO convertToEapQualityTermDTO(ExpressionPhenotypeTerm qualTerm) {
        EapQualityTermDTO dto = new EapQualityTermDTO();
        dto.setTag(qualTerm.getTag());
        dto.setTerm(convertToTermDTO(qualTerm.getQualityTerm()));
        return dto;
    }

    public static EapQualityTermDTO convertToEapQualityTerm(ExpressionPhenotypeTerm term) {
        EapQualityTermDTO dto = new EapQualityTermDTO();
        dto.setTag(term.getTag());
        dto.setTerm(convertToTermDTO(term.getQualityTerm()));
        return dto;
    }

    public static ExpressionFigureStage getExpressionFigureStageFromDTO(ExpressionFigureStageDTO figureAnnotation) {
        ExpressionFigureStage expressionFigureStage = new ExpressionFigureStage();
        expressionFigureStage.setStartStage(convertToDevelopmentStage(figureAnnotation.getStart()));
        expressionFigureStage.setEndStage(convertToDevelopmentStage(figureAnnotation.getEnd()));
        expressionFigureStage.setFigure(convertToFigure(figureAnnotation.getFigure()));
        //expressionFigureStage.setExpressionExperiment(convertToExperiment(figureAnnotation.getExperiment()));
        return expressionFigureStage;
    }

    private static ExpressionExperiment2 convertToExperiment(ExpressionExperimentDTO experiment) {
        ExpressionExperiment2 expressionExperiment = new ExpressionExperiment2();
        //expressionExperiment.set
        return null;
    }

    public static ExpressionFigureStageDTO convertToExpressionFigureStageDTOShallow(ExpressionFigureStage efs) {
        ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
        dto.setID(efs.getId());
        dto.setExperiment(DTOConversionService.convertToExperimentDTO(efs.getExpressionExperiment()));
        dto.setFigure(DTOConversionService.convertToFigureDTO(efs.getFigure()));
        dto.setStart(DTOConversionService.convertToStageDTO(efs.getStartStage()));
        dto.setEnd((DTOConversionService.convertToStageDTO(efs.getEndStage())));
        return dto;
    }

    public static ExpressionFigureStageDTO convertToExpressionFigureStageDTO(ExpressionFigureStage efs) {
        ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
        dto.setID(efs.getId());
        dto.setExperiment(DTOConversionService.convertToExperimentDTO(efs.getExpressionExperiment()));
        dto.setFigure(DTOConversionService.convertToFigureDTO(efs.getFigure()));
        dto.setStart(DTOConversionService.convertToStageDTO(efs.getStartStage()));
        dto.setEnd((DTOConversionService.convertToStageDTO(efs.getEndStage())));

        List<ExpressedTermDTO> termStrings = new ArrayList<>();
        for (ExpressionResult2 result : efs.getExpressionResultSet()) {
            termStrings.add(DTOConversionService.convertToExpressedTermDTO(result, true));
        }
        Collections.sort(termStrings);
        dto.setExpressedTerms(termStrings);
        return dto;
    }

    public static EntityZdbIdDTO convertToEntityZdbIdDTO(EntityZdbID entity) {
        EntityZdbIdDTO dto = new EntityZdbIdDTO();
        dto.setZdbID(entity.getZdbID());
        dto.setName(entity.getEntityName());
        dto.setAbbreviation(entity.getAbbreviation());
        dto.setType(entity.getEntityType());
        return dto;
    }

    public static MutationDetailControlledVocabularyTermDTO convertMutationDetailedControlledVocab(MutationDetailControlledVocabularyTerm controlledVocab) {
        MutationDetailControlledVocabularyTermDTO dto = new MutationDetailControlledVocabularyTermDTO();
        dto.setAbbreviation(controlledVocab.getAbbreviation());
        dto.setDisplayName(controlledVocab.getDisplayName());
        dto.setTerm(DTOConversionService.convertToTermDTO(controlledVocab.getTerm(), true));
        dto.setOrder(controlledVocab.getOrder());
        return dto;
    }

    public static FeatureDnaMutationDetail updateDnaMutationDetailWithDTO(FeatureDnaMutationDetail detail, MutationDetailDnaChangeDTO dnaChangeDTO) {
        if (detail == null) {
            detail = new FeatureDnaMutationDetail();
        }
        FeatureDnaMutationDetail changes = convertToDnaMutationDetail(detail, dnaChangeDTO);
        detail.setReferenceDatabase(changes.getReferenceDatabase());
        detail.setDnaSequenceReferenceAccessionNumber(changes.getDnaSequenceReferenceAccessionNumber());
        detail.setGeneLocalizationTerm(changes.getGeneLocalizationTerm());
        detail.setDnaPositionStart(changes.getDnaPositionStart());
        detail.setDnaPositionEnd(changes.getDnaPositionEnd());
        detail.setExonNumber(changes.getExonNumber());
        detail.setIntronNumber(changes.getIntronNumber());
        detail.setNumberAddedBasePair(changes.getNumberAddedBasePair());
        detail.setNumberRemovedBasePair(changes.getNumberRemovedBasePair());
        return detail;
    }

    public static FeatureProteinMutationDetail updateProteinMutationDetailWithDTO(FeatureProteinMutationDetail detail, MutationDetailProteinChangeDTO proteinChangeDTO) {
        if (detail == null) {
            detail = new FeatureProteinMutationDetail();
        }
        FeatureProteinMutationDetail changes = convertToProteinMutationDetail(detail, proteinChangeDTO);
        detail.setReferenceDatabase(changes.getReferenceDatabase());
        detail.setProteinSequenceReferenceAccessionNumber(changes.getProteinSequenceReferenceAccessionNumber());
        detail.setProteinConsequences(changes.getProteinConsequence());
        detail.setNumberAminoAcidsAdded(changes.getNumberAminoAcidsAdded());
        detail.setNumberAminoAcidsRemoved(changes.getNumberAminoAcidsRemoved());
        detail.setProteinPositionStart(changes.getProteinPositionStart());
        detail.setProteinPositionEnd(changes.getProteinPositionEnd());
        detail.setWildtypeAminoAcid(changes.getWildtypeAminoAcid());
        detail.setMutantAminoAcid(changes.getMutantAminoAcid());
        return detail;

    }

    public static FeatureGenomicMutationDetail updateFeatureGenomicMutationDetailWithDTO(FeatureGenomicMutationDetail detail, FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO) {
        if (detail == null) {
            detail = new FeatureGenomicMutationDetail();
        }
        FeatureGenomicMutationDetail changes = convertToFeatureGenomicMutationDetail(detail, fgmdChangeDTO);


            detail.setFgmdSeqVar(changes.getFgmdSeqVar());


            detail.setFgmdSeqRef(changes.getFgmdSeqRef());

        detail.setFgmdVarStrand("+");


        return detail;

    }

}
