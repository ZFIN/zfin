package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.curation.ui.CurationExperimentRPC;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.curation.ui.SessionVariable;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.util.StageRangeIntersection;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.ontology.*;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.people.CuratorSession;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Implementation of RPC calls from the client.
 * Handles curation related calls.
 */
public class CurationExperimentRPCImpl extends ZfinRemoteServiceServlet implements CurationExperimentRPC {

    private final static Logger LOG = RootLogger.getLogger(CurationExperimentRPCImpl.class);

    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
    private static ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
    private static InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
    private static AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private static MutantRepository mutantRep = RepositoryFactory.getMutantRepository();
    private static PhenotypeRepository phenotypeRep = RepositoryFactory.getPhenotypeRepository();
    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    public List<MarkerDTO> getGenes(String pubID) throws PublicationNotFoundException {

        Publication publication = pubRepository.getPublication(pubID);
        if (publication == null) {
            throw new PublicationNotFoundException(pubID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(pubID);
        List<MarkerDTO> genes = new ArrayList<MarkerDTO>();

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setName(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            genes.add(gene);
        }

        return genes;
    }

    /**
     * Delete a given experiment.
     *
     * @param experimentZdbID experiment id
     */
    public void deleteExperiment(String experimentZdbID) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment experiment = expRepository.getExpressionExperiment(experimentZdbID);
            expRepository.deleteExpressionExperiment(experiment);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Delete", e);
            tx.rollback();
            throw e;
        }
    }

    /**
     * Retrieve all experiments for a given publication.
     *
     * @param publicationID publication
     * @return list of experiments
     */
    public List<ExperimentDTO> readExperiments(String publicationID) {
        List<ExpressionExperiment> experiments = expRepository.getExperiments(publicationID);
        if (experiments == null)
            return null;

        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO experimentDTO = new ExperimentDTO();
            experimentDTO.setExperimentZdbID(experiment.getZdbID());
            Marker gene = experiment.getGene();
            if (gene != null) {
                experimentDTO.setGene(DTOConversionService.convertToMarkerDTO(gene));
                if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                    String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                    experimentDTO.setGenbankNumber(dblink);
                    experimentDTO.setGenbankID(experiment.getMarkerDBLink().getZdbID());
                }
            }
            if (experiment.getAntibody() != null) {
                experimentDTO.setAntibodyMarker(DTOConversionService.convertToMarkerDTO(experiment.getAntibody()));
            }
            experimentDTO.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
            experimentDTO.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
            experimentDTO.setEnvironment(DTOConversionService.convertToEnvironmentDTO(experiment.getGenotypeExperiment().getExperiment()));
            experimentDTO.setAssay(experiment.getAssay().getName());
            // check if there are expressions associated
            Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
            if (expressionResults != null)
                experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());

            dtos.add(experimentDTO);
        }
        return dtos;
    }

    public List<ExperimentDTO> getExperimentsByFilter(ExperimentDTO experimentFilter) {
        List<ExpressionExperiment> experiments =
                expRepository.getExperimentsByGeneAndFish2(experimentFilter.getPublicationID(),
                        experimentFilter.getGene() == null ? null : experimentFilter.getGene().getZdbID(),
                        experimentFilter.getFishID());
        if (experiments == null)
            return null;

        return convertExperimentsToDTO(experiments);
    }

    public static List<ExperimentDTO> convertExperimentsToDTO(List<ExpressionExperiment> experiments) {
        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO dto = DTOConversionService.convertToExperimentDTO(experiment);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<String> getAssays() {
        InfrastructureRepository infra = RepositoryFactory.getInfrastructureRepository();
        List<ExpressionAssay> assays = infra.getAllAssays();
        List<String> assayDtos = new ArrayList<String>();
        for (ExpressionAssay assay : assays)
            assayDtos.add(assay.getName());
        return assayDtos;
    }

    public List<EnvironmentDTO> getEnvironments(String publicationID) {
        List<Experiment> experiments = pubRepository.getExperimentsByPublication(publicationID);
        List<EnvironmentDTO> assayDtos = new ArrayList<EnvironmentDTO>();
        for (Experiment experiment : experiments) {
            EnvironmentDTO env = new EnvironmentDTO();
            env.setName(experiment.getName());
            env.setZdbID(experiment.getZdbID());
            assayDtos.add(env);
        }

        return assayDtos;
    }

    public List<FishDTO> getGenotypes(String publicationID) {
        List<FishDTO> genotypes = new ArrayList<FishDTO>();
        Genotype genotype = pubRepository.getGenotypeByHandle("WT");
        FishDTO fish = new FishDTO();
        fish.setZdbID(genotype.getZdbID());
        fish.setName(genotype.getHandle());
        genotypes.add(fish);
        fish = new FishDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        List<Genotype> genos = pubRepository.getNonWTGenotypesByPublication(publicationID);
        for (Genotype geno : genos) {
            FishDTO fishy = new FishDTO();
            fishy.setZdbID(geno.getZdbID());
            fishy.setName(geno.getHandle());
            genotypes.add(fishy);
        }
        fish = new FishDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        List<Genotype> wildtypes = mutantRep.getAllWildtypeGenotypes();
        for (Genotype wiltype : wildtypes) {
            FishDTO fishy = new FishDTO();
            fishy.setZdbID(wiltype.getZdbID());
            fishy.setName(wiltype.getNickname());
            genotypes.add(fishy);
        }
        return genotypes;
    }

    public List<MarkerDTO> getAntibodies(String publicationID) {
        List<Antibody> antibodies = pubRepository.getAntibodiesByPublication(publicationID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Antibody antibody : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setName(antibody.getName());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    public List<MarkerDTO> readAntibodiesByGene(String publicationID, String geneID) {
        if (StringUtils.isEmpty(geneID))
            return getAntibodies(publicationID);

        List<Antibody> antibodies = pubRepository.getAntibodiesByPublicationAndGene(publicationID, geneID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Antibody antibody : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setName(antibody.getName());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     */
    public List<MarkerDTO> readGenesByAntibody(String publicationID, String antibodyID) throws PublicationNotFoundException {
        if (StringUtils.isEmpty(antibodyID))
            return getGenes(publicationID);

        List<Marker> antibodies = pubRepository.getGenesByAntibody(publicationID, antibodyID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Marker gene : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setName(gene.getAbbreviation());
            env.setZdbID(gene.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    /**
     * Retrieve the accession numbers for a given gene
     *
     * @param geneID string
     */
    public List<ExperimentDTO> readGenbankAccessions(String publicationID, String geneID) {
        List<MarkerDBLink> geneDBLinks = pubRepository.getDBLinksByGene(publicationID, geneID);
        List<ExperimentDTO> accessionNumbers = new ArrayList<ExperimentDTO>();
        for (MarkerDBLink geneDBLink : geneDBLinks) {
            ExperimentDTO accession = new ExperimentDTO();
            accession.setGenbankNumber(geneDBLink.getAccessionNumber());
            accession.setGenbankID(geneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        List<MarkerDBLink> cloneDBLinks = pubRepository.getDBLinksForCloneByGene(publicationID, geneID);
        for (MarkerDBLink cloneDBLink : cloneDBLinks) {
            ExperimentDTO accession = new ExperimentDTO();
            accession.setGenbankNumber(cloneDBLink.getAccessionNumber() + " [" + cloneDBLink.getMarker().getType().toString() + "]");
            accession.setGenbankID(cloneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        return accessionNumbers;

    }

    /**
     * Update an existing experiment.
     *
     * @param experimentDTO experiment to be updated
     */
    public ExperimentDTO updateExperiment(ExperimentDTO experimentDTO) {
        if (experimentDTO == null)
            return null;
        String experimentID = experimentDTO.getExperimentZdbID();
        if (experimentID == null)
            throw new RuntimeException("No experiment ID provided");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = expRepository.getExpressionExperiment(experimentID);
            //createAuditRecordsForModifications(expressionExperiment, experimentDTO);
            // update assay: never null
            populateExpressionExperiment(experimentDTO, expressionExperiment);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return experimentDTO;
    }

    // ToDo: This will be done much more elegantly within an interceptor class in Hibernate.

    private void createAuditRecordsForModifications(ExpressionExperiment expressionExperiment, ExperimentDTO experimentDTO) {
        // check which attributes have changed when updating an experiment
        String comment = "updated Experiment";
        // gene
        Marker oldGene = expressionExperiment.getGene();
        String oldGeneID = null;
        if (oldGene != null)
            oldGeneID = oldGene.getZdbID();
        String newGeneID = experimentDTO.getGene().getZdbID();
        createAuditRecord(expressionExperiment, comment, oldGeneID, newGeneID, "Gene");

        // antibody
        Antibody oldAntibody = expressionExperiment.getAntibody();
        String oldAntibodyID = null;
        if (oldAntibody != null)
            oldAntibodyID = oldAntibody.getZdbID();
        String newAntibodyID = experimentDTO.getAntibodyMarker().getZdbID();
        createAuditRecord(expressionExperiment, comment, oldAntibodyID, newAntibodyID, "Antibody");

        String oldAssay = expressionExperiment.getAssay().getName();
        String newAssay = experimentDTO.getAssay();
        createAuditRecord(expressionExperiment, comment, oldAssay, newAssay, "Assay");

        String oldEnvironment = expressionExperiment.getGenotypeExperiment().getExperiment().getName();
        String newEnvironment = experimentDTO.getEnvironment().getName();
        createAuditRecord(expressionExperiment, comment, oldEnvironment, newEnvironment, "Environment");

        String oldGenotypeID = expressionExperiment.getGenotypeExperiment().getGenotype().getZdbID();
        String newGenotypeID = experimentDTO.getFishID();
        createAuditRecord(expressionExperiment, comment, oldGenotypeID, newGenotypeID, "Fish");

    }

    private void createAuditRecord(ExpressionExperiment expressionExperiment, String comment, String oldValue, String newValue, String fieldName) {
        if (!StringUtils.equals(oldValue, newValue)) {
            infraRep.insertUpdatesTable(expressionExperiment.getZdbID(), fieldName, oldValue, newValue, comment);
        }
    }


    public ExperimentDTO createExpressionExperiment(ExperimentDTO experimentDTO) throws Exception {
        if (experimentDTO == null)
            return null;

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = new ExpressionExperiment();
            populateExpressionExperiment(experimentDTO, expressionExperiment);

            expRepository.createExpressionExperiment(expressionExperiment);
            experimentDTO.setExperimentZdbID(expressionExperiment.getZdbID());
            tx.commit();
        } catch (ConstraintViolationException e) {
            tx.rollback();
            throw new Exception("Experiment already exist. Experiments have to be unique.");
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return experimentDTO;
    }

    /**
     * Check the visibility of the experiment section
     */
    public boolean readExperimentSectionVisibility(String publicationID) {
        CuratorSession session = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.SHOW_EXPERIMENT_SECTION);
        return session == null || session.getValue().equals("true");
    }

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID                publication ID
     * @param experimentVisibility experiment section visibility
     */
    public void setExperimentVisibilitySession(String pubID, boolean experimentVisibility) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            profileRep.setCuratorSession(pubID, CuratorSession.Attribute.SHOW_EXPERIMENT_SECTION, experimentVisibility);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Check if for a given publication there is no structure pile available.
     * It's been used to decide if a link to re-create the structure pile should be displayed.
     *
     * @param publicationID publication ID
     * @return boolean
     */
    public boolean isReCreatePhenotypePileLinkNeeded(String publicationID) {
        Publication publication = getPublicationRepository().getPublication(publicationID);
        if (publication.getCloseDate() != null)
            return false;
        List<ExperimentFigureStage> experiments = expRepository.getExperimentFigureStagesByGeneAndFish2(publicationID, null, null, null);
        Collection<ExpressionStructure> structures = expRepository.retrieveExpressionStructures(publicationID);
        return CollectionUtils.isNotEmpty(experiments) && CollectionUtils.isEmpty(structures);
    }

    /**
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO        dto
     * @param expressionExperiment expression experiment on which the changes are applied.
     */
    public static void populateExpressionExperiment(ExperimentDTO experimentDTO, ExpressionExperiment expressionExperiment) {
        // update assay: never null
        ExpressionAssay newAssay = expRepository.getAssayByName(experimentDTO.getAssay());
        expressionExperiment.setAssay(newAssay);
        experimentDTO.setAssayAbbreviation(newAssay.getAbbreviation());
        // update db link: could be null
        String genbankID = experimentDTO.getGenbankID();
        if (!StringUtils.isEmpty(genbankID)) {
            MarkerDBLink dbLink = expRepository.getMarkDBLink(genbankID);
            expressionExperiment.setMarkerDBLink(dbLink);
            Marker marker = dbLink.getMarker();
            // If the genbank number is an EST or a cDNA then persist their id in the clone column
            if (marker.getType().equals(Marker.Type.EST) ||
                    marker.getType().equals(Marker.Type.CDNA)) {
                // TOdo: Change to setClone(clone) when clone is subclassed from Marker
                Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(marker.getZdbID());
                expressionExperiment.setProbe(clone);
            }
        } else {
            expressionExperiment.setMarkerDBLink(null);
        }
        // update Environment (=experiment)
        GenotypeExperiment genox = expRepository.getGenotypeExperimentByExperimentIDAndGenotype(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
        LOG.info("Finding Genotype Experiment for :" + experimentDTO.getEnvironment().getZdbID() + ", " + experimentDTO.getFishID());
        // if no genotype experiment found create a new one.
        if (genox == null) {
            genox = expRepository.createGenoteypExperiment(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
            LOG.info("Created Genotype Experiment :" + genox.getZdbID());
        }
        expressionExperiment.setGenotypeExperiment(genox);
        // update antibody
        MarkerDTO antibodyDTO = experimentDTO.getAntibodyMarker();
        if (antibodyDTO != null) {
            AntibodyRepository antibodyRep = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRep.getAntibodyByID(antibodyDTO.getZdbID());
            expressionExperiment.setAntibody(antibody);
        } else {
            expressionExperiment.setAntibody(null);
        }
        // update gene
        MarkerDTO geneDto = experimentDTO.getGene();
        if (geneDto != null && geneDto.getZdbID() != null) {
            MarkerRepository antibodyRep = RepositoryFactory.getMarkerRepository();
            Marker gene = antibodyRep.getMarkerByID(geneDto.getZdbID());
            expressionExperiment.setGene(gene);
            experimentDTO.setGene(DTOConversionService.convertToMarkerDTO(gene));
        } else {
            expressionExperiment.setGene(null);
        }

        Publication pub = pubRepository.getPublication(experimentDTO.getPublicationID());
        expressionExperiment.setPublication(pub);
    }

    public List<String> readFigures(String publicationID) {
        return pubRepository.getDistinctFigureLabels(publicationID);
    }

    /**
     * Check the visibility of the expression section
     *
     * @param publicationID publication
     * @return boolean
     */
    public boolean readExpressionSectionVisibility(String publicationID) {
        CuratorSession session = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.SHOW_EXPRESSION_SECTION);
        return session == null || session.getValue().equals("true");
    }

    /**
     * Retrieve all expression records according to a given filter.
     *
     * @param experimentFilter filter object
     * @return expression figure stage records
     */
    public List<ExpressionFigureStageDTO> getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID) {
        List<ExperimentFigureStage> experiments = expRepository.getExperimentFigureStagesByGeneAndFish2(experimentFilter.getPublicationID(),
                experimentFilter.getGene() == null ? null : experimentFilter.getGene().getZdbID(),
                experimentFilter.getFishID(),
                figureID);
        if (experiments == null)
            return null;

        List<ExpressionFigureStageDTO> dtos = new ArrayList<ExpressionFigureStageDTO>();
        for (ExperimentFigureStage efs : experiments) {
            ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
            dto.setExperiment(DTOConversionService.convertToExperimentDTO(efs.getExpressionExperiment()));
            dto.setFigure(DTOConversionService.convertToFigureDTO(efs.getFigure()));
            dto.setStart(DTOConversionService.convertToStageDTO(efs.getStart()));
            dto.setEnd((DTOConversionService.convertToStageDTO(efs.getEnd())));
            List<ComposedFxTerm> terms = efs.getComposedTerms();
            Collections.sort(terms);
            List<ExpressedTermDTO> termStrings = new ArrayList<ExpressedTermDTO>(terms.size());
            for (ComposedFxTerm term : terms) {
                ExpressedTermDTO termDto = DTOConversionService.convertToExpressedTermDTO(term);
                termStrings.add(termDto);
            }
            Collections.sort(termStrings);
            dto.setExpressedTerms(termStrings);
            dto.setPatoExists(mutantRep.isPatoExists(efs.getExpressionExperiment().getGenotypeExperiment().getZdbID(),
                    efs.getFigure().getZdbID(), efs.getStart().getZdbID(), efs.getEnd().getZdbID(), experimentFilter.getPublicationID()));
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;
    }

    /**
     * Retrieve all Figures associated to a given publication.
     *
     * @param publicationID publication
     * @return list of figure dtos
     */
    public List<FigureDTO> getFigures(String publicationID) {
        List<Figure> figures = pubRepository.getFiguresByPublication(publicationID);
        if (figures == null)
            return null;

        List<FigureDTO> dtos = new ArrayList<FigureDTO>(figures.size());
        for (Figure figure : figures) {
            FigureDTO dto = new FigureDTO();
            dto.setLabel(figure.getLabel());
            dto.setZdbID(figure.getZdbID());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Retrieve all development stages.
     *
     * @return list of stages
     */
    public List<StageDTO> getStages() {
        List<DevelopmentStage> stages = anatomyRep.getAllStagesWithoutUnknown();
        List<StageDTO> dtos = new ArrayList<StageDTO>(stages.size());
        for (DevelopmentStage stage : stages) {
            dtos.add(DTOConversionService.convertToStageDTO(stage));
        }
        return dtos;
    }

    /**
     * Create multiple figure annotations.
     *
     * @param figureAnnotations figure annotations
     */
    @SuppressWarnings("unchecked")
    public List<ExpressionFigureStageDTO> createFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations) {
        if (figureAnnotations == null)
            return null;
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            createFigureAnnotation(figureAnnotation);
        }
        return figureAnnotations;
    }

    private void createFigureAnnotation(ExpressionFigureStageDTO figureAnnotation) {
        if (figureAnnotation == null)
            return;

        ExperimentFigureStage expressionExperiment = populateExpression(figureAnnotation);
        ExpressionResult result = new ExpressionResult();
        result.setEndStage(expressionExperiment.getEnd());
        result.setStartStage(expressionExperiment.getStart());
        result.setExpressionExperiment(expressionExperiment.getExpressionExperiment());
        populateFigureAnnotation(figureAnnotation, expressionExperiment);

        Set<Figure> figures = new HashSet<Figure>(1);
        figures.add(expressionExperiment.getFigure());
        result.setFigures(figures);

        GenericTerm unspecified = ontologyRepository.getTermByNameActive(Term.UNSPECIFIED, Ontology.ANATOMY);

        result.setSuperTerm(unspecified);
        result.setExpressionFound(true);
        ExpressedTermDTO unspecifiedTerm = DTOConversionService.convertToExpressedTermDTO(unspecified);
        unspecifiedTerm.setExpressionFound(true);
        figureAnnotation.addExpressedTerm(unspecifiedTerm);
        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            expressionRep.createExpressionResult(result, expressionExperiment.getFigure());
            tx.commit();
        } catch (ConstraintViolationException e) {
            tx.rollback();
            throw new RuntimeException("Figure Annotation already exist. Expressions have to be unique.");
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void populateFigureAnnotation(ExpressionFigureStageDTO figureAnnotation, ExperimentFigureStage expressionExperiment) {
        figureAnnotation.setStart(DTOConversionService.convertToStageDTO(expressionExperiment.getStart()));
        figureAnnotation.setEnd(DTOConversionService.convertToStageDTO(expressionExperiment.getEnd()));
        figureAnnotation.setFigure(DTOConversionService.convertToFigureDTO(expressionExperiment.getFigure()));
        figureAnnotation.setExperiment(DTOConversionService.convertToExperimentDTO(expressionExperiment.getExpressionExperiment()));
    }

    /**
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO dto
     * @return Experiment Figure stage
     */
    public ExperimentFigureStage populateExpression(ExpressionFigureStageDTO experimentDTO) {
        if (experimentDTO == null)
            return null;
        ExperimentFigureStage efs = new ExperimentFigureStage();
        ExpressionExperiment expressionExperiment = expRepository.getExpressionExperiment(experimentDTO.getExperiment().getExperimentZdbID());
        efs.setExpressionExperiment(expressionExperiment);

        Figure figure = pubRepository.getFigureByID(experimentDTO.getFigure().getZdbID());
        efs.setFigure(figure);

        DevelopmentStage start = anatomyRep.getStageByID(experimentDTO.getStart().getZdbID());
        efs.setStart(start);
        DevelopmentStage end = anatomyRep.getStageByID(experimentDTO.getEnd().getZdbID());
        efs.setEnd(end);
        return efs;
    }

    /**
     * Delete a figure annotation.
     *
     * @param figureAnnotation figure annotation
     */
    public void deleteFigureAnnotation(ExpressionFigureStageDTO figureAnnotation) {
        if (figureAnnotation == null)
            return;
        ExperimentDTO experiment = figureAnnotation.getExperiment();
        if (experiment == null || experiment.getExperimentZdbID() == null)
            return;
        if (figureAnnotation.getFigure() == null ||
                figureAnnotation.getStart().getZdbID() == null ||
                figureAnnotation.getEnd().getZdbID() == null)
            return;
        ExperimentFigureStage efs = expRepository.getExperimentFigureStage(experiment.getExperimentZdbID(), figureAnnotation.getFigure().getZdbID(),
                figureAnnotation.getStart().getZdbID(), figureAnnotation.getEnd().getZdbID());
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            expRepository.deleteFigureAnnotation(efs);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Set the expression visibility on the curation page.
     *
     * @param publicationID pub id
     * @param show          boolean
     */
    public void setExpressionVisibilitySession(String publicationID, boolean show) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            profileRep.setCuratorSession(publicationID, CuratorSession.Attribute.SHOW_EXPRESSION_SECTION, show);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Retrieve the figure for the filter on the fx page.
     *
     * @param publicationID publication
     */
    public FigureDTO getFigureFilter(String publicationID) {
        CuratorSession attribute = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID);
        Figure figure = pubRepository.getFigureByID(attribute.getValue());
        FigureDTO dto = new FigureDTO();
        dto.setLabel(figure.getLabel());
        dto.setZdbID(figure.getZdbID());
        return dto;
    }

    /**
     * Retrieve the gene for the filter on the fx page
     *
     * @param publicationID publication
     * @return marker DTO
     */
    public MarkerDTO getGeneFilter(String publicationID) {
        String uniqueKey = createSessionVariableName(publicationID, FX_GENE_FILTER);
        String geneID = (String) getServletContext().getAttribute(uniqueKey);
        MarkerDTO dto = new MarkerDTO();
        dto.setZdbID(geneID);
        return dto;
    }

    private String createSessionVariableName(String publicationID, String elementName) {
        return elementName + publicationID;
    }

    /**
     * Create a new Pato record
     *
     * @param efs figure annotation
     */
    public void createPatoRecord(ExpressionFigureStageDTO efs) {
        ExpressionExperiment expressionExperiment = expRepository.getExpressionExperiment(efs.getExperiment().getExperimentZdbID());
        Figure figure = pubRepository.getFigureByID(efs.getFigure().getZdbID());
        DevelopmentStage start = anatomyRep.getStageByID(efs.getStart().getZdbID());
        DevelopmentStage end = anatomyRep.getStageByID(efs.getEnd().getZdbID());
        PhenotypeExperiment phenoExperiment = new PhenotypeExperiment();
        phenoExperiment.setFigure(figure);
        phenoExperiment.setStartStage(start);
        phenoExperiment.setEndStage(end);
        phenoExperiment.setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            phenotypeRep.createPhenotypeExperiment(phenoExperiment);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    /**
     * Set the check mark status of a given figure annotation.
     *
     * @param checkedExpression figure annotation
     * @param checked           true or false (= checked or unchecked)
     */
    @SuppressWarnings("unchecked")
    public void setFigureAnnotationStatus(ExpressionFigureStageDTO checkedExpression, boolean checked) {
        String publicationID = checkedExpression.getExperiment().getPublicationID();
        String key = createSessionVariableName(publicationID, FX_FIGURE_ANNOTATION_CHECKBOX);
        Set<String> currentState = (Set<String>) getServletContext().getAttribute(key);
        if (currentState == null) {
            currentState = new HashSet<String>(10);
            getServletContext().setAttribute(key, currentState);
        }
        updateFigureAnnotationSessionSet(checkedExpression.getUniqueID(), currentState, checked);
    }


    /**
     * Add or remove the uniqueID to a set.
     * Method logs if check mark was already checked upon checking or
     * was already unchecked upon un-checking
     *
     * @param uniqueID     unique ID
     * @param currentState set of unique ids
     * @param checked      add or remove object
     */
    public void updateFigureAnnotationSessionSet(String uniqueID, Set<String> currentState, boolean checked) {
        if (checked) {
            boolean success = currentState.add(uniqueID);
            if (!success)
                LOG.debug("trying to check figure annotation  " + uniqueID + " that is already checked!");
        } else {
            boolean success = currentState.remove(uniqueID);
            if (!success)
                LOG.debug("trying to un-check figure annotation |" + uniqueID + "| that is not checked!");
        }
    }

    /**
     * Read the check mark status.
     *
     * @param publicationID Publication
     */
    @SuppressWarnings("unchecked")
    public CheckMarkStatusDTO getFigureAnnotationCheckmarkStatus(String publicationID) {
        CheckMarkStatusDTO vals = new CheckMarkStatusDTO();
        String uniqueKey = createSessionVariableName(publicationID, FX_FIGURE_ANNOTATION_CHECKBOX);
        Set<String> checkMarks = (Set<String>) getServletContext().getAttribute(uniqueKey);
        if (checkMarks == null || checkMarks.isEmpty())
            return null;

        Set<ExpressionFigureStageDTO> figs = createExpressionFigureStages(checkMarks);
        List<ExpressionFigureStageDTO> efs = new ArrayList<ExpressionFigureStageDTO>(figs);
        vals.setFigureAnnotations(efs);
        return vals;
    }

    /**
     * Check if the structure section should be hidden or displayed.
     *
     * @param publicationID publication id
     * @return show: true of false
     */
    public boolean readStructureSectionVisibility(String publicationID) {
        CuratorSession session = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.SHOW_STRUCTURE_SECTION);
        return session == null || session.getValue().equals("true");
    }

    /**
     * Set the visibility status for the structure section.
     *
     * @param publicationID publication id
     * @param show          true or false
     */
    public void setStructureVisibilitySession(String publicationID, boolean show) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            profileRep.setCuratorSession(publicationID, CuratorSession.Attribute.SHOW_STRUCTURE_SECTION, show);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Retrieve all structures on the structure pile except 'unspecified'
     *
     * @param publicationID Publication ID
     * @return list of structure dtos
     */
    public List<ExpressionPileStructureDTO> getStructures(String publicationID) {
        List<ExpressionStructure> structures = expRepository.retrieveExpressionStructures(publicationID);
        if (structures == null)
            return null;
        List<ExpressionPileStructureDTO> dtos = new ArrayList<ExpressionPileStructureDTO>(structures.size());
        for (ExpressionStructure es : structures) {
            // do not return 'unspecified'
            if (es.getSuperterm().getTermName().equals(Term.UNSPECIFIED))
                continue;
            ExpressionPileStructureDTO dto = DTOConversionService.convertToExpressionPileStructureDTO(es);
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;

    }

    /**
     * Update individual figure annotations with structures from the pile.
     *
     * @param updateEntity Update Expression dto
     * @return list of updated expression figure stage dtos
     */
    public List<ExpressionFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO updateEntity) {
        List<ExpressionFigureStageDTO> figureAnnotations = updateEntity.getFigureAnnotations();
        if (figureAnnotations == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructures = updateEntity.getStructures();
        if (pileStructures == null || pileStructures.isEmpty())
            return null;


        List<ExpressionFigureStageDTO> updatedAnnotations = new ArrayList<ExpressionFigureStageDTO>(figureAnnotations.size());
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            // for each figure annotation check which structures need to be added or removed
            for (ExpressionFigureStageDTO dto : figureAnnotations) {
                ExperimentFigureStage experiment = expRepository.getExperimentFigureStage(dto.getExperiment().getExperimentZdbID(),
                        dto.getFigure().getZdbID(),
                        dto.getStart().getZdbID(),
                        dto.getEnd().getZdbID());
                for (PileStructureAnnotationDTO pileStructure : pileStructures) {
                    ExpressionStructure expressionStructure = expRepository.getExpressionStructure(pileStructure.getZdbID());
                    if (expressionStructure == null)
                        LOG.error("Could not find pile structure " + pileStructure.getZdbID());
                    // add expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.ADD) {
                        ExpressedTermDTO expTerm = addExpressionToAnnotation(experiment, expressionStructure, pileStructure.isExpressed());
                        if (expTerm != null) {
                            dto.addExpressedTerm(expTerm);
                            updatedAnnotations.add(dto);
                        }
                    }
                    // remove expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.REMOVE) {
                        removeExpressionToAnnotation(experiment, pileStructure.isExpressed(), expressionStructure);
                    }
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        for (ExpressionFigureStageDTO dto : figureAnnotations) {
            setFigureAnnotationStatus(dto, false);
        }
        return updatedAnnotations;
    }

    /**
     * Retrieve a list of structures that could be used instead of the selected
     * structure with a stage overlap given by start and end.
     *
     * @param selectedPileStructure pile structure
     * @param intersection          StageIntersection
     * @return list of PileStructureDTO,
     */
    public List<RelatedPileStructureDTO> getTermsWithStageOverlap(ExpressionPileStructureDTO selectedPileStructure,
                                                                  StageRangeIntersection intersection) {
        GenericTerm term = ontologyRepository.getTermByZdbID(selectedPileStructure.getExpressedTerm().getEntity().getSuperTerm().getZdbID());
        List<TermRelationship> terms = term.getAllDirectlyRelatedTerms();
        List<RelatedPileStructureDTO> structures = new ArrayList<RelatedPileStructureDTO>(terms.size());
        for (TermRelationship rel : terms) {
            Term relatedTerm = rel.getRelatedTerm(term);
            // some terms may be stage terms and should be ignored.
            if (relatedTerm == null){
                throw new RuntimeException("No related term found for term: " + term.getZdbID());
            }

            if (!relatedTerm.getOntology().equals(Ontology.ANATOMY)){
                continue;
            }

            relatedTerm = ontologyRepository.getTermByZdbID(relatedTerm.getZdbID());
            AnatomyItem anatomyItem = anatomyRep.getAnatomyTermByOboID(relatedTerm.getOboID());
            StageDTO start = DTOConversionService.convertToStageDTO(anatomyItem.getStart());
            StageDTO end = DTOConversionService.convertToStageDTO(anatomyItem.getEnd());
            if (intersection.isFullOverlap(start, end)) {
                RelatedPileStructureDTO relatedStructure = populatePileStructureDTO(rel.getRelatedTerm(term));
                relatedStructure.setRelatedStructure(selectedPileStructure);
                relatedStructure.setRelationship(rel.getRelationshipType().getDbMappedName());
                relatedStructure.setStart(start);
                relatedStructure.setEnd(end);
                structures.add(relatedStructure);
            }
        }
        return structures;
    }

    private RelatedPileStructureDTO populatePileStructureDTO(Term term) {
        if (term == null)
            return null;

        RelatedPileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = DTOConversionService.convertToExpressedTermDTO(term);
        dto.setExpressedTerm(expDto);
        return dto;
    }

    /**
     * Save a given session variable in Application session.
     *
     * @param sessionVariable session variable
     */
    public void saveSessionVisibility(SessionVariable sessionVariable) {
        getServletContext().setAttribute(sessionVariable.getAttributeName(), sessionVariable);
    }

    /**
     * Add Expression record to an experiment.
     * expression is not added if it already exists for the given experiment including
     * the expressed-modifier. We allow to add the same composed term twice, once with
     * 'not'  modifier and once without it.
     *
     * @param experiment          ExpressionExperiment
     * @param expressed           structure is expressed or not [true/false]
     * @param expressionStructure structure selected from the pile
     * @return expressedTermDTO used to return to RPC caller
     */
    private ExpressedTermDTO addExpressionToAnnotation(ExperimentFigureStage experiment,
                                                       ExpressionStructure expressionStructure,
                                                       boolean expressed) {
        boolean termBeingUsed = false;
        for (ExpressionResult result : experiment.getExpressionResults()) {
            if (result.getSuperTerm().equals(expressionStructure.getSuperterm())) {
                String subtermID = null;
                Term term = result.getSubTerm();
                if (term != null)
                    subtermID = term.getZdbID();
                // check if subterms are equal or both null
                if (subtermID == null && expressionStructure.getSubterm() == null && expressed == result.isExpressionFound()) {
                    termBeingUsed = true;
                    break;
                }
                if (subtermID != null && expressionStructure.getSubterm() != null &&
                        subtermID.equals(expressionStructure.getSubterm().getZdbID()) &&
                        expressed == result.isExpressionFound()) {
                    termBeingUsed = true;
                    break;
                }
            }
        }
        // do nothing term already exists.
        if (termBeingUsed)
            return null;

        // create a new ExpressionResult record
        // create a new ExpressedTermDTO object that is passed back to the RPC caller
        ExpressedTermDTO expressedTerm = DTOConversionService.convertToExpressedTermDTO(expressionStructure);
        ExpressionResult newExpression = new ExpressionResult();
        newExpression.setSubTerm(expressionStructure.getSubterm());
        setMainAttributes(experiment, expressionStructure, expressed, newExpression);
        expRepository.createExpressionResult(newExpression, experiment.getFigure());
        TermDTO subtermDto = new TermDTO();
        subtermDto.setZdbID(expressionStructure.getSuperterm().getZdbID());
        subtermDto.setName(expressionStructure.getSuperterm().getZdbID());
        expressedTerm.setExpressionFound(expressed);
        return expressedTerm;
    }

    private void setMainAttributes(ExperimentFigureStage experiment, ExpressionStructure expressionStructure, boolean expressed, ExpressionResult newExpression) {
        newExpression.setExpressionExperiment(experiment.getExpressionExperiment());
        newExpression.setSuperTerm(expressionStructure.getSuperterm());
        newExpression.setExpressionFound(expressed);
        newExpression.setStartStage(experiment.getStart());
        newExpression.setEndStage(experiment.getEnd());
        newExpression.addFigure(experiment.getFigure());
    }

    private void removeExpressionToAnnotation(ExperimentFigureStage experiment, boolean expressed, ExpressionStructure expressionStructure) {
        for (ExpressionResult result : experiment.getExpressionResults()) {
            if (result.getSuperTerm().equals(expressionStructure.getSuperterm())) {
                String subtermID = null;
                Term term = result.getSubTerm();
                if (term != null)
                    subtermID = term.getZdbID();
                // check if subterms are equal or both null
                if (subtermID == null && expressionStructure.getSubterm() == null && expressed == result.isExpressionFound()) {
                    expRepository.deleteExpressionResultPerFigure(result, experiment.getFigure());
                    LOG.info("Removed Expression_Result:  " + result.getSuperTerm().getTermName());
                    break;
                }
                if (subtermID != null && expressionStructure.getSubterm().getZdbID() != null &&
                        subtermID.equals(expressionStructure.getSubterm().getZdbID()) &&
                        expressed == result.isExpressionFound()) {
                    expRepository.deleteExpressionResultPerFigure(result, experiment.getFigure());
                    Term subterm = result.getSubTerm();
                    if (subterm != null)
                        LOG.info("Removed Expression_Result:  " + result.getSuperTerm().getTermName() + " : " + subterm.getTermName());
                    else
                        LOG.info("Removed Expression_Result:  " + result.getSuperTerm().getTermName());
                    break;
                }
            }
        }
    }

    public Set<ExpressionFigureStageDTO> createExpressionFigureStages(Collection<String> checkMarks) {
        if (checkMarks == null)
            return null;
        Set<ExpressionFigureStageDTO> set = new HashSet<ExpressionFigureStageDTO>(checkMarks.size());
        for (String uniqueID : checkMarks) {
            ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
            dto.setUniqueID(uniqueID);
            set.add(dto);
        }
        return set;
    }

    private static final String FX_GENE_FILTER = "fx-gene-filter: ";
    private static final String FX_FIGURE_ANNOTATION_CHECKBOX = "fx-figure-annotation-checkbox: ";

}
