package org.zfin.gwt.root.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAssay;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.ontology.*;
import org.zfin.ontology.service.OntologyService;
import org.zfin.orthology.Species;
import org.zfin.people.CuratorSession;
import org.zfin.people.Lab;
import org.zfin.people.Organization;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.presentation.DBLinkPresentation;

import java.util.*;

/**
 */
public class DTOConversionService {

    private static Logger logger = Logger.getLogger(DTOConversionService.class);
//    private static SynonymSorting synonymSorting = new SynonymSorting();

    public static String escapeString(String uncleansedCharacter) {
//        return StringEscapeUtils.escapeJavaScript(uncleansedCharacter);
        return StringEscapeUtils.escapeHtml(uncleansedCharacter);
    }

    public static String unescapeString(String cleansedCharacter) {
//        return StringEscapeUtils.escapeJavaScript(uncleansedCharacter);
        return StringEscapeUtils.unescapeHtml(cleansedCharacter);
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> escapeStrings(Collection<String> uncleansedCharacter) {
        return CollectionUtils.collect(uncleansedCharacter, new Transformer() {
            @Override
            public String transform(Object o) {
                return DTOConversionService.escapeString(o.toString());
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

    public static Set<SequenceDTO> convertToSequenceDTOs(Sequence sequence, String markerName) {
        Set<SequenceDTO> sequenceDTOs = new HashSet<SequenceDTO>();
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
                    Species.ZEBRAFISH);
        } else {
            referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, referenceDatabaseDTO.getZdbID());
        }
        return referenceDatabase;
    }

    public static List<DBLinkDTO> convertToDBLinkDTOs(List<DBLink> dbLinks, String markerZdbID, String markerName) {
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>();

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
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>();

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
        List<ReferenceDatabaseDTO> referenceDatabaseDTOList = new ArrayList<ReferenceDatabaseDTO>();

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

    //todo: do these need to get the dataZdbID set?

    public static MarkerDTO convertToMarkerDTO(Marker marker) {
        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setName(marker.getName());
        markerDTO.setName(marker.getAbbreviation());
        markerDTO.setCompareString(marker.getAbbreviationOrder());
        markerDTO.setZdbID(marker.getZdbID());
        markerDTO.setLink(MarkerPresentation.getLink(marker));
        return markerDTO;
    }


    public static GenotypeDTO convertToGenotypeDTO(Genotype genotype) {
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setName(genotype.getHandle());
        genotypeDTO.setZdbID(genotype.getZdbID());
        genotypeDTO.setHandle(genotype.getHandle());
        return genotypeDTO;
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
        Set<String> inferredFromSet = new HashSet<String>();
        Set<String> inferredFromLinks = new HashSet<String>();
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

        if (featureDTO.getKnownInsertionSite()) {
            feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());
        }

        if (featureDTO.getPublicNote() != null) {
            feature.setPublicComments(featureDTO.getPublicNote().getNoteData());
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
            PublicNoteDTO noteDTO = new PublicNoteDTO(feature.getZdbID(), feature.getPublicComments());
            featureDTO.setPublicNote(noteDTO);
        }

        Set<DataNote> curatorNotes = feature.getDataNotes();
        if (CollectionUtils.isNotEmpty(curatorNotes)) {
            List<NoteDTO> curatorNoteDTOs = new ArrayList<NoteDTO>();
            for (DataNote dataNote : curatorNotes) {
                NoteDTO noteDTO = new CuratorNoteDTO(dataNote.getZdbID(), dataNote.getDataZdbID(), dataNote.getNote());
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


        featureDTO.setFeatureAliases(new ArrayList<String>(unescapeStrings(FeatureService.getFeatureAliases(feature))));
       featureDTO.setFeatureSequences(new ArrayList<String>(unescapeStrings(FeatureService.getFeatureSequences(feature))));
        return featureDTO;
    }

    public static CuratorSessionDTO convertToCuratorSessionDTO(CuratorSession session) {
        if (session == null)
            return null;

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
        if (mutantFigureStage == null)
            return null;

        MutantFigureStage mfs = new MutantFigureStage();
        GenotypeExperiment genotypeExperiment =
                RepositoryFactory.getExpressionRepository().getGenotypeExperimentByExperimentIDAndGenotype(mutantFigureStage.getEnvironment().getZdbID(),
                        mutantFigureStage.getGenotype().getZdbID());
        if (genotypeExperiment != null)
            mfs.setGenotypeExperiment(genotypeExperiment);

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
        } else
            dto.setFigure(convertToFigureDTO(mutantFigureStage.getFigure()));
        dto.setPublicationID(figure.getPublication().getZdbID());
        dto.setEnvironment(convertToEnvironmentDTO(mutantFigureStage.getGenotypeExperiment().getExperiment()));
        dto.setStart(convertToStageDTO(mutantFigureStage.getStartStage()));
        dto.setEnd(convertToStageDTO(mutantFigureStage.getEndStage()));
        dto.setGenotype(convertToGenotypeDTO(mutantFigureStage.getGenotypeExperiment().getGenotype()));
        Set<PhenotypeStatement> phenoStatements = mutantFigureStage.getPhenotypeStatements();
        List<PhenotypeStatementDTO> phenotypeTerms = new ArrayList<PhenotypeStatementDTO>(5);
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
        if (quality == null)
            throw new TermNotFoundException("No valid quality term found: " + phenotypeDTO.getQuality().getTermName() +
                    " and " + phenotypeDTO.getQuality().getOntology());
        structure.setQualityTerm(quality);
        if (phenotypeDTO.getTag() != null)
            structure.setTag(PhenotypeStatement.Tag.getTagFromName(phenotypeDTO.getTag()));
        return structure;
    }

    private static PostComposedEntity populatePostComposedEntity(EntityDTO entityDTO) throws TermNotFoundException {
        if (entityDTO == null)
            return null;
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

//    public static AliasDTO convertToAliasDTO(TermAlias termAlias){
//        AliasDTO aliasDTO = new AliasDTO();
//
//        aliasDTO.setAlias(termAlias.getAlias());
//        aliasDTO.setSignificance(termAlias.getAliasGroup().getSignificance());
//
//        return  aliasDTO ;
//    }
//
//    private static List<AliasDTO> convertToAliasDTO(Set<TermAlias> aliases) {
//        List<AliasDTO> aliasDTOs = new ArrayList<AliasDTO>();
//        for(TermAlias termAlias : aliases){
//            aliasDTOs.add(convertToAliasDTO(termAlias)) ;
//        }
//        return aliasDTOs ;
//    }

    private static Set<String> convertToAliasDTO(Set<TermAlias> aliases) {
        Set<String> aliasDTOs = new HashSet<String>();
        for(TermAlias termAlias : aliases){
            aliasDTOs.add(termAlias.getAlias()) ;
        }
        return aliasDTOs ;
    }

    public static TermDTO convertToTermDTOWithDirectRelationships(Term term) {
        if (term == null){
            return null;
        }
        TermDTO dto = convertToTermDTO(term);

        Set<TermDTO> childTerms = new HashSet<TermDTO>();
        for(TermRelationship termRelationship : term.getChildTermRelationships()){
            TermDTO childTerm = convertToTermDTO(termRelationship.getTermTwo()) ;
            childTerm.setRelationshipType(termRelationship.getType());
            childTerms.add(childTerm) ;
        }
        dto.setChildrenTerms(childTerms);

        Set<TermDTO> parentTerms = new HashSet<TermDTO>();
        for(TermRelationship termRelationship : term.getParentTermRelationships()){
            TermDTO parentTerm = convertToTermDTO(termRelationship.getTermOne()) ;
            parentTerm.setRelationshipType(termRelationship.getType());
            parentTerms.add(parentTerm) ;
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
        if (termDTO == null)
            return null;
        if (termDTO.getOntology() == null || StringUtils.isEmpty(termDTO.getTermName()))
            return null;
        Ontology ontology = convertToOntology(termDTO.getOntology());
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByName(termDTO.getTermName(),ontology);
        // if no term found throw exception
        if (term == null)
            throw new TermNotFoundException("Could not find valid term for: " + ontology.getCommonName() + ":" + termDTO.getTermName());
        return term;
    }

    public static TermDTO convertToTermDTO(Term term) {
        if (term == null){
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

        if(term.getOntology()==Ontology.ANATOMY){
            DevelopmentStage startStage = OntologyService.getStartStageForTerm(term) ;
            dto.setStartStage(convertToStageDTO(startStage));
            DevelopmentStage endStage = OntologyService.getEndStageForTerm(term) ;
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
        if (ontology == Ontology.MPATH){
            ontology = Ontology.MPATH_NEOPLASM;
        }
        dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        return dto;

    }

    public static TermDTO convertQualityToTermDTO(PhenotypeStructure structure) {
        if (structure == null)
            return null;

        GenericTerm term = structure.getQualityTerm();
        Ontology entityOntology = getDefiningOntology(structure);
        TermDTO dto = new TermDTO();
        dto.setName(term.getTermName());
        dto.setZdbID(term.getZdbID());
        dto.setObsolete(term.isObsolete());
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
        if (ontology == Ontology.QUALITY) {
            if (entityOntology == Ontology.GO_BP || entityOntology == Ontology.GO_MF)
                ontology = Ontology.QUALITY_PROCESSES;
            else
                ontology = Ontology.QUALITY_QUALITIES;
        }
        dto.setSubsets(convertToSubsetDTO(term.getSubsets()));
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        dto.setOboID(term.getOboID());
        return dto;
    }

    private static Ontology getDefiningOntology(PhenotypeStructure structure) {
        if (structure == null)
            return null;
        PostComposedEntity entity = structure.getEntity();
        if (entity == null)
            return null;
        if (entity.getSubterm() != null)
            return entity.getSubterm().getOntology();
        return entity.getSuperterm().getOntology();
    }

    private static Set<String> convertToSubsetDTO(Set<Subset> subsets) {
        if (subsets == null)
            return null;
        Set<String> subsetDtos = new HashSet<String>(subsets.size());
        for (Subset subset : subsets) {
            subsetDtos.add(subset.getInternalName());
        }
        return subsetDtos;
    }

    public static FigureDTO convertToFigureDTO(Figure figure) {
        FigureDTO dto = new FigureDTO();
        dto.setZdbID(figure.getZdbID());
        dto.setLabel(figure.getLabel());
        dto.setOrderingLabel(figure.getOrderingLabel());
        return dto;
    }

    public static StageDTO convertToStageDTO(DevelopmentStage stage) {
        if(stage == null ) return null ;
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
        if (experiment.getName().startsWith("_"))
            environment.setName(experiment.getName().substring(1));
        else
            environment.setName(experiment.getName());
        return environment;
    }

    public static PhenotypePileStructureDTO convertToPhenotypePileStructureDTO(PhenotypeStructure structure) {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(structure.getZdbID());
        dto.setPhenotypeTerm(convertToPhenotypeTermDTO(structure));
        dto.setCreator(structure.getPerson().getName());
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
        experimentDTO.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
        experimentDTO.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
        experimentDTO.setEnvironment(convertToEnvironmentDTO(experiment.getGenotypeExperiment().getExperiment()));
        experimentDTO.setAssay(experiment.getAssay().getName());
        experimentDTO.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
        experimentDTO.setGenotypeExperimentID(experiment.getGenotypeExperiment().getZdbID());
        experimentDTO.setPublicationID(experiment.getPublication().getZdbID());
        // check if there are expressions associated
        Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
        if (expressionResults != null)
            experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());
        // check if a clone is available
        Marker probe = experiment.getProbe();
        if (probe != null) {
            experimentDTO.setCloneID(probe.getZdbID());
            experimentDTO.setCloneName(probe.getAbbreviation() + " [" + probe.getType().toString() + "]");
        }
        return experimentDTO;
    }

    public static PhenotypeExperiment convertToPhenotypeExperimentFilter(PhenotypeExperimentDTO dto) {
        if (dto == null)
            return null;

        PhenotypeExperiment phenoExperiment = new PhenotypeExperiment();
        phenoExperiment.setId(dto.getId());
        Genotype genotype = new Genotype();
        genotype.setZdbID(dto.getGenotype().getZdbID());
        Experiment environment = new Experiment();
        environment.setZdbID(dto.getEnvironment().getZdbID());
        GenotypeExperiment genotypeExperiment = new GenotypeExperiment();
        genotypeExperiment.setGenotype(genotype);
        genotypeExperiment.setExperiment(environment);
        phenoExperiment.setGenotypeExperiment(genotypeExperiment);
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
        if (term.getSubterm() != null)
            entity.setSubTerm(convertToTermDTO(term.getSubterm()));
        termDto.setEntity(entity);
        termDto.setExpressionFound(term.isExpressionFound());
        termDto.setZdbID(term.getZdbID());
        return termDto;
    }

    public static ExpressionPileStructureDTO convertToExpressionPileStructureDTO(ExpressionStructure es) {
        ExpressionPileStructureDTO dto = new ExpressionPileStructureDTO();
        dto.setZdbID(es.getZdbID());
        dto.setCreator(es.getPerson().getName());
        dto.setDate(es.getDate());
        ExpressedTermDTO expressionTerm = convertToExpressedTermDTO(es);
        dto.setExpressedTerm(expressionTerm);
        if (es.getSuperterm().getOntology().equals(Ontology.ANATOMY)) {
            // awkward but needs to be done until we have a better way to join in the stage info for a term.
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(es.getSuperterm().getZdbID());
            AnatomyItem anatomyItem = RepositoryFactory.getAnatomyRepository().getAnatomyItem(term.getTermName());
	                StageDTO start = convertToStageDTO(anatomyItem.getStart());
            StageDTO end = convertToStageDTO(anatomyItem.getEnd());
            dto.setStart(start);
            dto.setEnd(end);
        }
        return dto;
    }


    public static OntologyDTO convertToOntologyDTO(Ontology ontology) {
        switch (ontology) {
            case QUALITY:
                return OntologyDTO.QUALITY;
            case QUALITY_PROCESSES:
                return OntologyDTO.QUALITY_PROCESSES;
            case QUALITY_QUALITIES:
                return OntologyDTO.QUALITY_QUALITIES;
            case QUALITY_QUALITATIVE:
                return OntologyDTO.QUALITY_QUALITATIVE;
            case QUALITY_PROCESSES_RELATIONAL:
                return OntologyDTO.QUALITY_PROCESSES_RELATIONAL;
            case QUALITY_OBJECT_RELATIONAL:
                return OntologyDTO.QUALITY_QUALITIES_RELATIONAL;
            case ANATOMY:
                return OntologyDTO.ANATOMY;
            case GO_MF:
                return OntologyDTO.GO_MF;
            case GO_CC:
                return OntologyDTO.GO_CC;
            case GO_BP:
                return OntologyDTO.GO_BP;
            case GO:
                return OntologyDTO.GO;
            case STAGE:
                return OntologyDTO.STAGE;
            case SPATIAL:
                return OntologyDTO.SPATIAL;
            case MPATH:
                return OntologyDTO.MPATH;
            case MPATH_NEOPLASM:
                return OntologyDTO.MPATH_NEOPLASM;
            case BEHAVIOR:
                return OntologyDTO.BEHAVIOR;
        }
        return null;
    }

    public static Ontology convertToOntology(OntologyDTO ontology) {
        switch (ontology) {
            case QUALITY:
                return Ontology.QUALITY;
            case QUALITY_PROCESSES:
                return Ontology.QUALITY_PROCESSES;
            case QUALITY_QUALITIES:
                return Ontology.QUALITY_QUALITIES;
            case QUALITY_QUALITATIVE:
                return Ontology.QUALITY_QUALITATIVE;
            case QUALITY_QUALITIES_RELATIONAL:
                return Ontology.QUALITY_OBJECT_RELATIONAL;
            case QUALITY_PROCESSES_RELATIONAL:
                return Ontology.QUALITY_PROCESSES_RELATIONAL;
            case ANATOMY:
                return Ontology.ANATOMY;
            case GO_MF:
                return Ontology.GO_MF;
            case GO_CC:
                return Ontology.GO_CC;
            case GO_BP:
                return Ontology.GO_BP;
            case GO_BP_MF:
                return Ontology.GO_BP_MF;
            case GO:
                return Ontology.GO;
            case STAGE:
                return Ontology.STAGE;
            case SPATIAL:
                return Ontology.SPATIAL;
            case MPATH:
                return Ontology.MPATH;
             case MPATH_NEOPLASM:
                return Ontology.MPATH_NEOPLASM;
            case BEHAVIOR:
                return Ontology.BEHAVIOR;

        }
        return null;
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

    public static LabDTO convertToLabDTO(Lab lab) {
        LabDTO labDTO = new LabDTO();
        labDTO.setZdbID(lab.getZdbID());
        labDTO.setName(lab.getName());
        return labDTO;
    }

    public static List<LabDTO> convertToLabDTO(List<Lab> labsOfOrigin) {
        List<LabDTO> labDTO = new ArrayList<LabDTO>();
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
        List<OrganizationDTO> organizationDTO = new ArrayList<OrganizationDTO>();
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
        List<FeaturePrefixDTO> featurePrefixDTOs = new ArrayList<FeaturePrefixDTO>();
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

    public static void unescapeFeatureDTO(FeatureDTO featureDTO) {
        featureDTO.setName(unescapeString(featureDTO.getName()));
        featureDTO.setAbbreviation(unescapeString(featureDTO.getAbbreviation()));
        featureDTO.setAlias(unescapeString(featureDTO.getAlias()));
        featureDTO.setFeatureSequence(unescapeString(featureDTO.getFeatureSequence()));
        featureDTO.setOptionalName(unescapeString(featureDTO.getOptionalName()));
    }

//    public static List<String> createSortedSynonymsFromTerm(TermDTO term) {
//        List<AliasDTO> synonyms = sortSynonyms(term);
//        if (synonyms == null) {
//            return null;
//        }
//        List<String> list = new ArrayList<String>();
//        for (AliasDTO synonym : synonyms) {
//            list.add(synonym.getAlias());
//        }
//        return list;
//    }


    /**
     * @param anatomyItem anatomy term
     * @return set of synonyms
     */
//    public static List<AliasDTO> sortSynonyms(TermDTO anatomyItem) {
//        if (anatomyItem.getAliases() == null)
//            return null;
//        List<AliasDTO> aliases = anatomyItem.getAliases();
//        Collections.sort(aliases,synonymSorting);
//        return aliases ;
//    }


    /**
     * Inner class: Comparator that compares the alias names of the AnatomySynonym
     * and orders them alphabetically.
     */
//    public static class SynonymSorting implements Comparator<AliasDTO> {
//
//        public int compare(AliasDTO synOne, AliasDTO synTwo) {
//
//            int aliassig1 = synOne.getSignificance();
//
//            int aliassig2 = synTwo.getSignificance();
//            String alias = synOne.getAlias();
//            String alias1 = synTwo.getAlias();
//
//            if (aliassig1 < aliassig2)
//                return -1;
//            else if (aliassig1 > aliassig2)
//                return 1;
//            else if (aliassig1 == aliassig2)
//                return alias.compareToIgnoreCase(alias1);
//            else
//                return 0;
//        }
//    }


    public static EntityDTO convertToEntityDTO(PostComposedEntity entity) {
        if (entity == null)
            return null;

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

}
