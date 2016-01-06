package org.zfin.gwt.root.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.construct.presentation.ConstructPresentation;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAssay;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
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
import org.zfin.util.ZfinStringUtils;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

//import org.apache.commons.lang.StringEscapeUtils;

/**
 */
public class DTOConversionService {

    private static Logger logger = Logger.getLogger(DTOConversionService.class);
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
    public static Collection<String> escapeStrings(Collection<String> uncleansedCharacter) {
        return CollectionUtils.collect(uncleansedCharacter, new Transformer() {
            @Override
            public String transform(Object o) {
                return ZfinStringUtils.escapeHighUnicode(o.toString());
            }
        });
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
            List<FeatureDTO> featureDTOList = new ArrayList<>(4);
            for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
                featureDTOList.add(convertToFeatureDTO(genotypeFeature.getFeature()));
            }
            genotypeDTO.setFeatureList(featureDTOList);
        }
        return genotypeDTO;
    }

    public static GenotypeDTO convertToGenotypeDTOShallow(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getName());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        genotypeDTO.setNickName(genotype.getNickname());
        //genotypeDTO.setWildtype(genotype.isWildtype());
        if (genotype.getAssociatedGenotypes() != null) {
            for (Genotype background : genotype.getAssociatedGenotypes())
                genotypeDTO.addBackgroundGenotype(convertToPureGenotypeDTOs(background));
        }
        if (CollectionUtils.isNotEmpty(genotype.getExternalNotes())) {
            createExternalNotesOnGenotype(genotype, genotypeDTO);
        }
        if (CollectionUtils.isNotEmpty(genotype.getDataNotes())) {
            createCuratorNotesOnGenotype(genotype, genotypeDTO);
        }
        // add features
        if (CollectionUtils.isNotEmpty(genotype.getGenotypeFeatures())) {
            List<FeatureDTO> featureDTOList = new ArrayList<>(4);
            for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
                featureDTOList.add(convertToFeatureDTO(genotypeFeature.getFeature()));
            }
            genotypeDTO.setFeatureList(featureDTOList);
        }
        return genotypeDTO;
    }

    public static GenotypeDTO convertToPureGenotypeDTOs(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getName());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        if (genotype.getAssociatedGenotypes() != null) {
            for (Genotype background : genotype.getAssociatedGenotypes())
                genotypeDTO.addBackgroundGenotype(convertToPureGenotypeDTOs(background));
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
            if (note.getSinglePubAttribution() != null) {
                noteDTO.setPublicationZdbID(note.getSinglePubAttribution().getSourceZdbID());
            }
            externalNoteDTOList.add(noteDTO);
        }
        genotypeDTO.setPublicNotes(externalNoteDTOList);
    }

    public static GenotypeDTO convertToGenotypeDTO(Genotype genotype, boolean includePubInfo) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getHandle());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());

        if (includePubInfo) {
            List<PublicationDTO> associatedPublications = new ArrayList<>();
            for (Publication publication : genotype.getAssociatedPublications()) {
                associatedPublications.add(DTOConversionService.convertToPublicationDTO(publication));
            }
            genotypeDTO.setAssociatedPublications(associatedPublications);
        }
        return genotypeDTO;
    }

    public static PublicationDTO convertToPublicationDTO(Publication publication) {
        PublicationDTO publicationDTO = new PublicationDTO(publication.getTitle(), publication.getZdbID());
        publicationDTO.setAuthors(publication.getAuthors());
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
        returnDTO.setInferredFrom(inferredFromSet);
        returnDTO.setInferredFromLinks(inferredFromLinks);

        return returnDTO;
    }

    public static Feature convertToFeature(FeatureDTO featureDTO) {
        Feature feature = new Feature();
        feature.setAbbreviation(escapeString(featureDTO.getAbbreviation()));
        feature.setName(escapeString(featureDTO.getName()));

        // these two need to be added, but a trigger fixes them
        feature.setAbbreviationOrder(featureDTO.getAbbreviation());
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

       /* if (featureDTO.getKnownInsertionSite()) {
            feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());
        }*/
        feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());

        if (featureDTO.getPublicNote() != null) {
            feature.setPublicComments(escapeString(featureDTO.getPublicNote().getNoteData()));
        }

        return feature;
    }

    public static FeatureDTO convertToFeatureDTO(Feature feature) {
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

        FeatureAssay ftrAssay = feature.getFeatureAssay();
        if (ftrAssay != null) {
            if (feature.getFeatureAssay().getMutagee() != null) {
                featureDTO.setMutagee(feature.getFeatureAssay().getMutagee().toString());
            }
            if (feature.getFeatureAssay().getMutagen() != null) {
                featureDTO.setMutagen(feature.getFeatureAssay().getMutagen().toString());
            }
        }

        if (feature.getPublicComments() != null) {
            PublicNoteDTO noteDTO = new PublicNoteDTO(feature.getZdbID(), DTOConversionService.unescapeString(feature.getPublicComments()));
            featureDTO.setPublicNote(noteDTO);
        }

        Set<DataNote> curatorNotes = feature.getDataNotes();
        if (CollectionUtils.isNotEmpty(curatorNotes)) {
            List<NoteDTO> curatorNoteDTOs = new ArrayList<>();
            for (DataNote dataNote : curatorNotes) {
                NoteDTO noteDTO = new CuratorNoteDTO(dataNote.getZdbID(), dataNote.getDataZdbID(), DTOConversionService.unescapeString(dataNote.getNote()));
                curatorNoteDTOs.add(noteDTO);
            }
            featureDTO.setCuratorNotes(curatorNoteDTOs);
        }

        featureDTO.setDominant(feature.getDominantFeature());
        featureDTO.setKnownInsertionSite(feature.getKnownInsertionSite());
//       featureDTO.setFeatureSequence(feature.getFeatDBLink().getAccessionNumber());
        FeaturePrefix featurePrefix = feature.getFeaturePrefix();
        if (featurePrefix != null) {
            featureDTO.setLabPrefix(featurePrefix.getPrefixString());
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

        if (feature.getFeatureMarkerRelations() != null) {
            for (FeatureMarkerRelationship relationship : feature.getFeatureMarkerRelations()) {
                if (relationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF)) {
                    featureDTO.setDisplayNameForGenotypeBase(relationship.getMarker().getAbbreviation());
                    featureDTO.setDisplayNameForGenotypeSuperior(feature.getAbbreviation());
                }
            }
        }
        return featureDTO;
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
        dto.setEnvironment(convertToEnvironmentDTO(mutantFigureStage.getFishExperiment().getExperiment()));
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

    public static TermDTO convertToTermDTOWithDirectRelationships(Term term) {
        if (term == null) {
            return null;
        }
        TermDTO dto = convertToTermDTO(term);

        Set<TermDTO> childTerms = new HashSet<>();
        for (TermRelationship termRelationship : term.getChildTermRelationships()) {
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

    public static TermDTO convertToTermDTO(Term term) {
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
        dto.setAliases(convertToAliasDTO(term.getAliases()));

        if (term.getOntology() == Ontology.ANATOMY) {
            DevelopmentStage startStage = OntologyService.getStartStageForTerm(term);
            dto.setStartStage(convertToStageDTO(startStage));
            DevelopmentStage endStage = OntologyService.getEndStageForTerm(term);
            dto.setEndStage(convertToStageDTO(endStage));
        }

        // set stages here


        // set the ontology section here
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
//        String qualityOntologyName = term.getOntology().getOntologyName();
//        dto.setOntology(OntologyDTO.getOntologyByDescriptor(qualityOntologyName));
        // if QUALITY, then get the most specific instance for the ontology
        if (ontology == Ontology.QUALITY) {
            ontology = RepositoryFactory.getOntologyRepository().getProcessOrPhysicalObjectQualitySubOntologyForTerm(term);
        }
        if (ontology == Ontology.MPATH) {
            ontology = Ontology.MPATH_NEOPLASM;
        }
        dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        dto.setDoNotAnnotateWith(term.useForAnnotations());
        return dto;

    }

    public static TermDTO convertQualityToTermDTO(PhenotypeStructure structure) {
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
        dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
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
        if (figure.getLabel() == null)
            figure = getPublicationRepository().getFigureByID(figure.getZdbID());
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
    public static EnvironmentDTO convertToEnvironmentDTO(Experiment experiment) {
        EnvironmentDTO environment = new EnvironmentDTO();
        environment.setZdbID(experiment.getZdbID());
        if (experiment.getName() == null) {
            experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(experiment.getZdbID());
        }
        if (experiment.getName().startsWith("_")) {
            environment.setName(experiment.getName().substring(1));
        } else {
            environment.setName(experiment.getName());
        }
        return environment;
    }

    public static PhenotypePileStructureDTO convertToPhenotypePileStructureDTO(PhenotypeStructure structure) {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(structure.getZdbID());
        dto.setPhenotypeTerm(convertToPhenotypeTermDTO(structure));
        dto.setCreator(structure.getPerson().getShortName());
        dto.setDate(structure.getDate());
        return dto;
    }

    public static PhenotypeStatementDTO convertToPhenotypeTermDTO(PhenotypeStructure structure) {
        PhenotypeStatementDTO phenotypeTerm = new PhenotypeStatementDTO();
        TermDTO quality = convertQualityToTermDTO(structure);
        phenotypeTerm.setQuality(quality);
        phenotypeTerm.setEntity(convertToEntityDTO(structure.getEntity()));
        phenotypeTerm.setRelatedEntity(convertToEntityDTO(structure.getRelatedEntity()));
        phenotypeTerm.setTag(structure.getTag().toString());
        return phenotypeTerm;
    }

    public static ExperimentDTO convertToExperimentDTO(ExpressionExperiment experiment) {
        ExperimentDTO experimentDTO = new ExperimentDTO();
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
        experimentDTO.setEnvironment(convertToEnvironmentDTO(experiment.getFishExperiment().getExperiment()));
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

    private static EapQualityTermDTO convertToEapQualityTermDTO(ExpressionStructure structure) {
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

//    public static TermInfoDTO convertToTermInfoFromTermInfoDTO(TermDTO term, OntologyDTO ontologyDTO, boolean includeSynonyms) {
//        TermInfoDTO infoDTO = new TermInfoDTO();
//        infoDTO.setZdbID(term.getZdbID());
//        infoDTO.setOboID(term.getOboID());
//        infoDTO.setName(term.getName());
//        if (includeSynonyms) {
//            infoDTO.setAliases(sortSynonyms(term));
//        }
//        infoDTO.setDefinition(term.getDefinition());
//        infoDTO.setComment(term.getComment());
//
//        // try to use the terms ontology unless not provided
//        if (term.getOntology() == null) {
//            infoDTO.setOntology(ontologyDTO);
//        } else {
//            infoDTO.setOntology(term.getOntology());
//        }
//        infoDTO.setObsolete(term.isObsolete());
//        return infoDTO;
//    }
//        info.setSubsets(convertToSubsetDTO(term.getSubsets()));
//        // try to use the provided ontology first as it may be more specific than
    // the ontology from the term itself.
    //        if (ontologyDTO != null) {
    //            info.setOntology(ontologyDTO);/
    //       } else if (term.getOntology() != null) {
    //        info.setOntology(DTOConversionService.convertToOntologyDTO(term.getOntology()));
    //    }
    //    info.setObsolete(term.isObsolete());
    //    return info;
    // }

    public static FeatureMarkerRelationshipDTO convertToFeatureMarkerRelationshipDTO(FeatureMarkerRelationship featureMarkerRelationship) {
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = new FeatureMarkerRelationshipDTO();

        featureMarkerRelationshipDTO.setZdbID(featureMarkerRelationship.getZdbID());
        featureMarkerRelationshipDTO.setRelationshipType(featureMarkerRelationship.getFeatureMarkerRelationshipType().getName());
        featureMarkerRelationshipDTO.setFeatureDTO(convertToFeatureDTO(featureMarkerRelationship.getFeature()));
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
        if (entity == null) {
            return null;
        }

        EntityDTO dto = new EntityDTO();
        dto.setSuperTerm(convertToTermDTO(entity.getSuperterm()));
        dto.setSubTerm(convertToTermDTO(entity.getSubterm()));
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
        entity.setSuperTerm(DTOConversionService.convertToTermDTO(expressionStructure.getSuperterm()));
        entity.setSubTerm(DTOConversionService.convertToTermDTO(expressionStructure.getSubterm()));
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
    public static ExpressedTermDTO convertToExpressedTermDTO(Term term) {
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
        entity.setEnvironment(convertToEnvironmentDTO(str.getFishExperiment().getExperiment()));
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
        fish.setOrder(0);
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

    public static FishDTO convertToFishDtoFromFish(Fish fish) {
        FishDTO dto = new FishDTO();
        dto.setZdbID(fish.getZdbID());
        dto.setName(fish.getDisplayName());
        dto.setHandle(fish.getHandle());
        dto.setGenotypeDTO(DTOConversionService.convertToGenotypeDTO(fish.getGenotype(), false));
        if (CollectionUtils.isNotEmpty(fish.getStrList())) {
            List<RelatedEntityDTO> strs = new ArrayList<>(fish.getStrList().size());
            for (SequenceTargetingReagent str : fish.getStrList()) {
                strs.add(DTOConversionService.convertStrToRelatedEntityDTO(str));
            }
            dto.setStrList(strs);
        }
        dto.setOrder(fish.getOrder());
        dto.setNameOrder(fish.getNameOrder());
        return dto;
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
        dto.setEvidenceCode(model.getEvidenceCode());
        dto.setZdbID(model.getZdbID());
        List<DiseaseAnnotationModel> dam = getMutantRepository().getDiseaseAnnotationModelByZdb(model.getZdbID());
        if (CollectionUtils.isNotEmpty(dam)) {
            List<DiseaseAnnotationModelDTO> damoDTO = new ArrayList<>(dam.size());
            for (DiseaseAnnotationModel damo : dam) {
                damoDTO.add(DTOConversionService.convertDamoToDamoDTO(damo));

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
        dto.setPublication(DTOConversionService.convertToPublicationDTO(evidence.getPublication()));
        return dto;
    }

    public static NcbiOtherSpeciesGeneDTO convertToNcbiOtherSpeciesGeneDTO(NcbiOtherSpeciesGene ncbiGene) {
        if (ncbiGene == null)
            return null;
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
        return dto;
    }

    public static PersonDTO convertToPersonDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setDisplay(person.getFullName());
        dto.setZdbID(person.getZdbID());
        return dto;
    }

    public static ExperimentDTO convertToExperimentDTO(ExpressionExperiment2 experiment) {
        ExperimentDTO experimentDTO = new ExperimentDTO();
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
        experimentDTO.setEnvironment(convertToEnvironmentDTO(experiment.getFishExperiment().getExperiment()));
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
        ExpressedTermDTO dto = new ExpressedTermDTO();
        dto.setExpressionFound(result.isExpressionFound());
        dto.setEntity(convertToEntityDTO(result.getEntity()));
        dto.setId(result.getID());
        List<EapQualityTermDTO> dtoList = new ArrayList<>();
        if (result.getPhenotypeTermSet() != null) {
            for (ExpressionPhenotypeTerm qualTerm : result.getPhenotypeTermSet())
                dtoList.add(convertToEapQualityTermDTO(qualTerm));
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

    private static ExpressionExperiment2 convertToExperiment(ExperimentDTO experiment) {
        ExpressionExperiment2 expressionExperiment = new ExpressionExperiment2();
        //expressionExperiment.set
        return null;
    }

    public static ExpressionFigureStageDTO convertToExpressionFigureStageDTOShallow(ExpressionFigureStage efs) {
        ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
        dto.setExperiment(DTOConversionService.convertToExperimentDTO(efs.getExpressionExperiment()));
        dto.setFigure(DTOConversionService.convertToFigureDTO(efs.getFigure()));
        dto.setStart(DTOConversionService.convertToStageDTO(efs.getStartStage()));
        dto.setEnd((DTOConversionService.convertToStageDTO(efs.getEndStage())));
        return dto;
    }
}
