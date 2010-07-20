package org.zfin.gwt.root.server;

import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.ontology.*;
import org.zfin.orthology.Species;
import org.zfin.people.CuratorSession;
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
public class DTOConversionService {

    private static Logger logger = Logger.getLogger(DTOConversionService.class);


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

    public static FeatureDTO convertToFeatureDTO(Feature feature) {
        FeatureDTO dto = new FeatureDTO();
        dto.setZdbID(feature.getZdbID());
        dto.setName(feature.getName());
        dto.setAbbreviation(feature.getAbbreviation());
        return dto;
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
    public static MutantFigureStage convertToMutantFigureStageFromDTO(PhenotypeFigureStageDTO mutantFigureStage) {
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

    public static PhenotypeFigureStageDTO convertToPhenotypeFigureStageDTO(MutantFigureStage mutantFigureStage) {
        PhenotypeFigureStageDTO dto = new PhenotypeFigureStageDTO();
        dto.setPublicationID(mutantFigureStage.getPublication().getZdbID());
        dto.setEnvironment(convertToEnvironmentDTO(mutantFigureStage.getGenotypeExperiment().getExperiment()));
        dto.setStart(convertToStageDTO(mutantFigureStage.getStart()));
        dto.setEnd(convertToStageDTO(mutantFigureStage.getEnd()));
        dto.setFigure(convertToFigureDTO(mutantFigureStage.getFigure()));
        dto.setGenotype(convertToGenotypeDTO(mutantFigureStage.getGenotypeExperiment().getGenotype()));
        Set<Phenotype> phenotypes = mutantFigureStage.getPhenotypes();
        List<PhenotypeTermDTO> phenotypeTerms = new ArrayList<PhenotypeTermDTO>(5);
        if (phenotypes != null) {
            for (Phenotype phenotype : phenotypes) {
                PhenotypeTermDTO phenotypeDto = convertToPhenotypeTermDTO(phenotype);
                phenotypeTerms.add(phenotypeDto);
            }
        }
        dto.setExpressedTerms(phenotypeTerms);
        return dto;
    }

    public static PhenotypeTermDTO convertToPhenotypeTermDTO(Phenotype phenotype) {
        PhenotypeTermDTO dto = new PhenotypeTermDTO();
        dto.setTag(phenotype.getTag());
        dto.setQuality(convertToTermDTO(phenotype.getTerm()));
        dto.setSuperterm(convertToTermDTO(phenotype.getSuperterm()));
        dto.setSubterm(convertToTermDTO(phenotype.getSubterm()));
        dto.setZdbID(phenotype.getZdbID());
        return dto;
    }

    public static FishDTO convertToFishDTOFromGenotype(Genotype genotype) {
        FishDTO dto = new FishDTO();
        dto.setName(genotype.getHandle());
        dto.setZdbID(genotype.getZdbID());
        return dto;
    }

    public static TermDTO convertToTermDTO(Term term) {
        if (term == null)
            return null;

        TermDTO dto = new TermDTO();
        dto.setTermName(term.getTermName());
        dto.setTermID(term.getID());
        dto.setObsolete(term.isObsolete());
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
        if (ontology == Ontology.QUALITY){
            ontology = OntologyManager.getInstance().getSubOntology(term.getOntology(), term.getID());
        }
        OntologyDTO ontologyDTO = convertToOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        dto.setTermOboID(term.getOboID());
        return dto;
    }

    public static FigureDTO convertToFigureDTO(Figure figure) {
        FigureDTO dto = new FigureDTO();
        dto.setZdbID(figure.getZdbID());
        dto.setLabel(figure.getLabel());
        dto.setOrderingLabel(figure.getOrderingLabel());
        return dto;
    }

    public static StageDTO convertToStageDTO(DevelopmentStage stage) {
        StageDTO dto = new StageDTO();
        dto.setZdbID(stage.getZdbID());
        dto.setName(stage.getAbbreviation() + " " + stage.getTimeString());
        dto.setStartHours(stage.getHoursStart());
        dto.setEndHours(stage.getHoursEnd());
        dto.setAbbreviation(stage.getAbbreviation());
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
        if (experiment.getName().startsWith("_"))
            environment.setName(experiment.getName().substring(1));
        else
            environment.setName(experiment.getName());
        return environment;
    }

    public static TermDTO convertToTermDTOFromGenericTerm(GenericTerm term) {
        if (term == null)
            return null;

        TermDTO dto = new TermDTO();
        dto.setTermName(term.getTermName());
        dto.setTermID(term.getID());
        dto.setTermOboID(term.getOboID());
        dto.setDefinition(term.getDefinition());
        dto.setComment(term.getComment());
        String qualityOntologyName = term.getOntology().getOntologyName();
        dto.setOntology(OntologyDTO.getOntologyByDescriptor(qualityOntologyName));

        return dto;
    }

    public static PhenotypePileStructureDTO convertToPhenotypePileStructureDTO(PhenotypeStructure structure) {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(structure.getZdbID());
        dto.setPhenotypeTerm(convertToPhenotypeTermDTO(structure));
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        return dto;
    }

    public static PhenotypeTermDTO convertToPhenotypeTermDTO(PhenotypeStructure structure) {
        PhenotypeTermDTO phenotypeTerm = new PhenotypeTermDTO();
        TermDTO quality = convertToTermDTO(structure.getQuality());
        phenotypeTerm.setQuality(quality);

        TermDTO superterm = convertToTermDTO(structure.getSuperterm());
        phenotypeTerm.setSuperterm(superterm);

        if (structure.getSubterm() != null) {
            TermDTO subterm = convertToTermDTO(structure.getSubterm());
            phenotypeTerm.setSubterm(subterm);
        }
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

    public static MutantFigureStage convertToMutantFigureStageFilter(PhenotypeFigureStageDTO dto) {
        MutantFigureStage mfs = new MutantFigureStage();
        Genotype genotype = new Genotype();
        genotype.setZdbID(dto.getGenotype().getZdbID());
        Experiment environment = new Experiment();
        environment.setZdbID(dto.getEnvironment().getZdbID());
        GenotypeExperiment genotypeExperiment = new GenotypeExperiment();
        genotypeExperiment.setGenotype(genotype);
        genotypeExperiment.setExperiment(environment);
        mfs.setGenotypeExperiment(genotypeExperiment);
        mfs.setStart(convertToDevelopmentStage(dto.getStart()));
        mfs.setEnd(convertToDevelopmentStage(dto.getEnd()));
        Publication pub = new Publication();
        pub.setZdbID(dto.getPublicationID());
        mfs.setPublication(pub);
        return mfs;
    }

    public static DevelopmentStage convertToDevelopmentStage(StageDTO start) {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setZdbID(start.getZdbID());
        stage.setName(start.getName());
        return stage;
    }

    public static ExpressedTermDTO convertToExpressedTermDTO(ComposedFxTerm term) {
        ExpressedTermDTO termDto = new ExpressedTermDTO();
        termDto.setSuperterm(convertToTermDTO(term.getSuperTerm()));
        if (term.getSubterm() != null)
            termDto.setSubterm(convertToTermDTO(term.getSubterm()));
        termDto.setExpressionFound(term.isExpressionFound());
        termDto.setZdbID(term.getZdbID());
        return termDto;
    }

    public static ExpressionPileStructureDTO convertToExpressionPileStructureDTO(ExpressionStructure es) {
        ExpressionPileStructureDTO dto = new ExpressionPileStructureDTO();
        dto.setZdbID(es.getZdbID());
        dto.setCreator(es.getPerson().getName());
        dto.setDate(es.getDate());
        ExpressedTermDTO expressionTerm = new ExpressedTermDTO();
        Term superterm = es.getSuperterm();
        TermDTO supertermDTO = convertToTermDTO(superterm);
        expressionTerm.setSuperterm(supertermDTO);

        // if subterm available
        if (es.getSubterm() != null) {
            TermDTO subtermDTO = convertToTermDTO(es.getSubterm());
            expressionTerm.setSubterm(subtermDTO);
            Term term = OntologyManager.getInstance().getTermByID(es.getSubterm().getID());
        }
        dto.setExpressedTerm(expressionTerm);
        if (superterm.getOntology().equals(Ontology.ANATOMY)) {
            // awkward but needs to be done until we have a better way to join in the stage info for a term.
            Term term = OntologyManager.getInstance().getTermByID(superterm.getID());
            StageDTO start = convertToStageDTO(term.getStart());
            StageDTO end = convertToStageDTO(term.getEnd());
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
        }
        return null;
    }

    public static TermInfo convertToTermInfo(Term term, OntologyDTO ontologyDTO, boolean includeSynonyms) {
        TermInfo info = new TermInfo();
        info.setID(term.getID());
        info.setOboID(term.getOboID());
        info.setName(term.getTermName());
        if (includeSynonyms) {
            info.setSynonyms(OntologyService.createSortedSynonymsFromTerm(term));
        }
        info.setDefinition(term.getDefinition());
        info.setComment(term.getComment());

        // try to use the terms ontology unless not provided
        if(term.getOntology()==null){
            info.setOntology(ontologyDTO) ; 
        }
        else{
            info.setOntology(DTOConversionService.convertToOntologyDTO(term.getOntology()));
        }
        info.setObsolete(term.isObsolete());
        return info;
    }
}
