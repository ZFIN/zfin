package org.zfin.util;

import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.gwt.root.dto.*;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.*;
import org.zfin.people.CuratorSession;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * Convenience class to provide conversion from a DTO to a BO and vice versa.
 */
public class BODtoConversionService implements Serializable {

    private static ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
    private static AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();


    public static FishDTO getFishDTO(Genotype genotype) {
        FishDTO dto = new FishDTO();
        dto.setName(genotype.getHandle());
        dto.setZdbID(genotype.getZdbID());
        return dto;
    }

    public static GenotypeDTO getGenotypeDTO(Genotype genotype) {
        GenotypeDTO dto = new GenotypeDTO();
        dto.setName(genotype.getHandle());
        dto.setZdbID(genotype.getZdbID());
        dto.setHandle(genotype.getHandle());
        return dto;
    }

    public static FeatureDTO getFeatureDto(Feature feature) {
        FeatureDTO dto = new FeatureDTO();
        dto.setZdbID(feature.getZdbID());
        dto.setName(feature.getName());
        dto.setAbbreviation(feature.getAbbreviation());
        return dto;
    }

    public static CuratorSessionDTO getCuratorSessionDTO(CuratorSession session) {
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
    public static MutantFigureStage getMutantFigureStage(PhenotypeFigureStageDTO mutantFigureStage) {
        if (mutantFigureStage == null)
            return null;

        MutantFigureStage mfs = new MutantFigureStage();
        GenotypeExperiment genotypeExperiment =
                expRepository.getGenotypeExperimentByExperimentIDAndGenotype(mutantFigureStage.getEnvironment().getZdbID(),
                        mutantFigureStage.getGenotype().getZdbID());
        if (genotypeExperiment != null)
            mfs.setGenotypeExperiment(genotypeExperiment);

        Figure figure = pubRepository.getFigureByID(mutantFigureStage.getFigure().getZdbID());
        mfs.setFigure(figure);

        DevelopmentStage start = anatomyRep.getStageByID(mutantFigureStage.getStart().getZdbID());
        mfs.setStart(start);
        DevelopmentStage end = anatomyRep.getStageByID(mutantFigureStage.getEnd().getZdbID());
        mfs.setEnd(end);

        Publication publication = pubRepository.getPublication(mutantFigureStage.getPublicationID());
        mfs.setPublication(publication);

        return mfs;
    }

    public static PhenotypeFigureStageDTO getMutantFigureStage(MutantFigureStage mutantFigureStage) {
        PhenotypeFigureStageDTO dto = new PhenotypeFigureStageDTO();
        dto.setPublicationID(mutantFigureStage.getPublication().getZdbID());
        dto.setEnvironment(getEnvironmentDto(mutantFigureStage.getGenotypeExperiment().getExperiment()));
        dto.setStart(getStageDto(mutantFigureStage.getStart()));
        dto.setEnd(getStageDto(mutantFigureStage.getEnd()));
        dto.setFigure(getFigureDto(mutantFigureStage.getFigure()));
        dto.setGenotype(getGenotypeDTO(mutantFigureStage.getGenotypeExperiment().getGenotype()));
        Set<Phenotype> phenotypes = mutantFigureStage.getPhenotypes();
        List<PhenotypeTermDTO> phenotypeTerms = new ArrayList<PhenotypeTermDTO>(5);
        if (phenotypes != null) {
            for (Phenotype phenotype : phenotypes) {
                PhenotypeTermDTO phenotypeDto = getPhenotypeTermDTO(phenotype);
                phenotypeTerms.add(phenotypeDto);
            }
        }
        dto.setExpressedTerms(phenotypeTerms);
        return dto;
    }

    public static PhenotypeTermDTO getPhenotypeTermDTO(Phenotype phenotype) {
        PhenotypeTermDTO dto = new PhenotypeTermDTO();
        dto.setTag(phenotype.getTag());
        dto.setQuality(getTermDto(phenotype.getTerm()));
        dto.setSuperterm(getTermDto(phenotype.getSuperterm()));
        dto.setSubterm(getTermDto(phenotype.getSubTerm()));
        dto.setZdbID(phenotype.getZdbID());
        return dto;
    }

    public static TermDTO getTermDto(Term term) {
        if (term == null)
            return null;

        TermDTO dto = new TermDTO();
        dto.setTermName(term.getTermName());
        dto.setTermID(term.getID());
        Ontology ontology = term.getOntology();
        // ToDo: generalize this better...
        if (ontology == Ontology.QUALITY)
            ontology = OntologyManager.getInstance().getSubOntology(term.getOntology(), term.getID());
        OntologyDTO ontologyDTO = getOntologyDTO(ontology);
        dto.setOntology(ontologyDTO);
        // ToDO: This hack is needed as in the GO_TERM table the oboID is lacking the GO: string!!!!!!
        // whereas in the TERM table it does include it.
        if (OntologyDTO.isGoOntology(ontologyDTO) && !term.getOboID().startsWith("GO:"))
            dto.setTermOboID("GO:" + term.getOboID());
        else
            dto.setTermOboID(term.getOboID());
        return dto;
    }

    public static FigureDTO getFigureDto(Figure figure) {
        FigureDTO dto = new FigureDTO();
        dto.setZdbID(figure.getZdbID());
        dto.setLabel(figure.getLabel());
        dto.setOrderingLabel(figure.getOrderingLabel());
        return dto;
    }

    public static StageDTO getStageDto(DevelopmentStage stage) {
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
    public static EnvironmentDTO getEnvironmentDto(Experiment experiment) {
        EnvironmentDTO environment = new EnvironmentDTO();
        environment.setZdbID(experiment.getZdbID());
        if (experiment.getName().startsWith("_"))
            environment.setName(experiment.getName().substring(1));
        else
            environment.setName(experiment.getName());
        return environment;
    }

    public static TermDTO getTermDto(GenericTerm term) {
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

    public static PhenotypePileStructureDTO getPhenotypePileStructureDTO(PhenotypeStructure structure) {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(structure.getZdbID());
        dto.setPhenotypeTerm(getPhenotypeTerm(structure));
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        return dto;
    }

    public static PhenotypeTermDTO getPhenotypeTerm(PhenotypeStructure structure) {
        PhenotypeTermDTO phenotypeTerm = new PhenotypeTermDTO();
        TermDTO quality = BODtoConversionService.getTermDto(structure.getQuality());
        phenotypeTerm.setQuality(quality);

        TermDTO superterm = BODtoConversionService.getTermDto(structure.getSuperterm());
        phenotypeTerm.setSuperterm(superterm);

        if (structure.getSubterm() != null) {
            TermDTO subterm = BODtoConversionService.getTermDto(structure.getSubterm());
            phenotypeTerm.setSubterm(subterm);
        }
        phenotypeTerm.setTag(structure.getTag().toString());
        return phenotypeTerm;
    }

    public static ExperimentDTO getExperimentDto(ExpressionExperiment experiment) {
        ExperimentDTO experimentDTO = new ExperimentDTO();
        experimentDTO.setExperimentZdbID(experiment.getZdbID());
        Marker gene = experiment.getGene();
        if (gene != null) {
            experimentDTO.setGene(getMarkerDto(gene));
            if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                experimentDTO.setGenbankNumber(dblink);
                experimentDTO.setGenbankID(experiment.getMarkerDBLink().getZdbID());
            }
        }
        if (experiment.getAntibody() != null) {
            experimentDTO.setAntibodyMarker(BODtoConversionService.getMarkerDto(experiment.getAntibody()));
        }
        experimentDTO.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
        experimentDTO.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
        experimentDTO.setEnvironment(BODtoConversionService.getEnvironmentDto(experiment.getGenotypeExperiment().getExperiment()));
        experimentDTO.setAssay(experiment.getAssay().getName());
        experimentDTO.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
        experimentDTO.setGenotypeExperimentID(experiment.getGenotypeExperiment().getZdbID());
        experimentDTO.setPublicationID(experiment.getPublication().getZdbID());
        // check if there are expressions associated
        Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
        if (expressionResults != null)
            experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());
        // check if a clone is available
        if (experiment.getProbe()!=null) {
            Clone clone = getMarkerRepository().getCloneById(experiment.getProbe().getZdbID());
            experimentDTO.setCloneID(clone.getZdbID());
            experimentDTO.setCloneName(clone.getAbbreviation() + " [" + clone.getType().toString() + "]");
        }
        return experimentDTO;
    }

    public static MarkerDTO getMarkerDto(Marker marker) {
        MarkerDTO gene = new MarkerDTO();
        gene.setZdbID(marker.getZdbID());
        gene.setName(marker.getName());
        gene.setName(marker.getAbbreviation());
        return gene;
    }


    public static MutantFigureStage getMutantFigureStageFilter(PhenotypeFigureStageDTO dto) {
        MutantFigureStage mfs = new MutantFigureStage();
        Genotype genotype = new Genotype();
        genotype.setZdbID(dto.getGenotype().getZdbID());
        Experiment environment = new Experiment();
        environment.setZdbID(dto.getEnvironment().getZdbID());
        GenotypeExperiment genotypeExperiment = new GenotypeExperiment();
        genotypeExperiment.setGenotype(genotype);
        genotypeExperiment.setExperiment(environment);
        mfs.setGenotypeExperiment(genotypeExperiment);
        mfs.setStart(getStage(dto.getStart()));
        mfs.setEnd(getStage(dto.getEnd()));
        Publication pub = new Publication();
        pub.setZdbID(dto.getPublicationID());
        mfs.setPublication(pub);
        return mfs;
    }

    private static DevelopmentStage getStage(StageDTO start) {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setZdbID(start.getZdbID());
        stage.setName(start.getName());
        return stage;
    }

    public static TermInfo getTermInfo(Term term, OntologyDTO ontology, boolean includeSynonyms) {
        TermInfo info = new TermInfo();
        info.setID(term.getOboID());
        info.setName(term.getTermName());
        if (includeSynonyms)
            info.setSynonyms(OntologyService.createFormattedSynonymList(term));
        info.setDefinition(term.getDefinition());
        info.setComment(term.getComment());
        info.setOntology(ontology);
        info.setObsolete(term.isObsolete());
        return info;
    }

    public static Ontology getOntology(OntologyDTO ontology) {
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
        }
        return null;
    }

    public static OntologyDTO getOntologyDTO(Ontology ontology) {
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
        }
        return null;
    }


}
