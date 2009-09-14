package org.zfin.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.curation.client.*;
import org.zfin.curation.dto.*;
import org.zfin.expression.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.ComposedFxTerm;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.OntologyTerm;
import org.zfin.people.CuratorSession;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

/**
 * Implementation of RPC calls from the client.
 * Handles curation related calls.
 */
public class CurationExperimentRPCImpl extends RemoteServiceServlet implements CurationExperimentRPC {

    private final static Logger LOG = RootLogger.getLogger(CurationExperimentRPCImpl.class);

    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
    private static InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
    private static AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private static MutantRepository mutantRep = RepositoryFactory.getMutantRepository();

    public List<MarkerDTO> getGenes(String pubID) throws PublicationNotFoundException {

        Publication publication = pubRepository.getPublication(pubID);
        if (publication == null) {
            throw new PublicationNotFoundException(pubID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(pubID);
        List<MarkerDTO> genes = new ArrayList<MarkerDTO>();

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setAbbreviation(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            genes.add(gene);
        }

        return genes;  //To change body of implemented methods use File | Settings | File Templates.
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
        List<ExpressionExperiment> experiments = pubRepository.getExperiments(publicationID);
        if (experiments == null)
            return null;

        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO dto = new ExperimentDTO();
            dto.setExperimentZdbID(experiment.getZdbID());
            Marker gene = experiment.getMarker();
            if (gene != null) {
                dto.setGeneName(gene.getAbbreviation());
                dto.setGeneZdbID(gene.getZdbID());
                if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                    String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                    dto.setGenbankNumber(dblink);
                    dto.setGenbankID(experiment.getMarkerDBLink().getZdbID());
                }
            }
            if (experiment.getAntibody() != null) {
                dto.setAntibody(experiment.getAntibody().getAbbreviation());
                dto.setAntibodyID(experiment.getAntibody().getZdbID());
            }
            dto.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
            dto.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
            dto.setEnvironment(experiment.getGenotypeExperiment().getExperiment().getName());
            dto.setEnvironmentID(experiment.getGenotypeExperiment().getExperiment().getZdbID());
            dto.setAssay(experiment.getAssay().getName());
            // check if there are expressions associated
            Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
            if (expressionResults != null)
                dto.setNumberOfExpressions(experiment.getDistinctExpressions());

            dtos.add(dto);
        }
        return dtos;
    }

    public List<ExperimentDTO> getExperimentsByFilter(ExperimentDTO experimentFilter) {
        List<ExpressionExperiment> experiments =
                pubRepository.getExperimentsByGeneAndFish(experimentFilter.getPublicationID(),
                        experimentFilter.getGeneZdbID(),
                        experimentFilter.getFishID());
        if (experiments == null)
            return null;

        return convertExperimentsToDTO(experiments);
    }

    public static List<ExperimentDTO> convertExperimentsToDTO(List<ExpressionExperiment> experiments) {
        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO dto = convertExperimentToDto(experiment);
            dtos.add(dto);
        }
        return dtos;
    }

    public static ExperimentDTO convertExperimentToDto(ExpressionExperiment experiment) {
        ExperimentDTO dto = new ExperimentDTO();
        dto.setExperimentZdbID(experiment.getZdbID());
        Marker gene = experiment.getMarker();
        if (gene != null) {
            dto.setGeneName(gene.getAbbreviation());
            dto.setGeneZdbID(gene.getZdbID());
            if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                dto.setGenbankNumber(dblink);
                dto.setGenbankID(experiment.getMarkerDBLink().getZdbID());
            }
        }
        if (experiment.getAntibody() != null) {
            dto.setAntibody(experiment.getAntibody().getAbbreviation());
            dto.setAntibodyID(experiment.getAntibody().getZdbID());
        }
        dto.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
        dto.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
        dto.setEnvironment(experiment.getGenotypeExperiment().getExperiment().getName());
        dto.setEnvironmentID(experiment.getGenotypeExperiment().getExperiment().getZdbID());
        dto.setAssay(experiment.getAssay().getName());
        dto.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
        dto.setGenotypeExperimentID(experiment.getGenotypeExperiment().getZdbID());
        dto.setPublicationID(experiment.getPublication().getZdbID());
        // check if there are expressions associated
        Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
        if (expressionResults != null)
            dto.setNumberOfExpressions(experiment.getDistinctExpressions());
        // check if a clone is available
        if (StringUtils.isNotEmpty(experiment.getCloneID())) {
            Marker clone = markerRepository.getMarkerByID(experiment.getCloneID());
            dto.setCloneID(clone.getZdbID());
            dto.setCloneName(clone.getAbbreviation() + " [" + clone.getType().toString() + "]");
        }
        return dto;
    }

    public FilterValuesDTO getPossibleFilterValues(String publicationID) throws PublicationNotFoundException {
        // read all fish
        FilterValuesDTO values = new FilterValuesDTO();

        List<Genotype> genos = pubRepository.getFishUsedInExperiment(publicationID);
        if (genos == null)
            return null;

        List<FishDTO> dtos = new ArrayList<FishDTO>();
        for (Genotype genotype : genos) {
            FishDTO dto = new FishDTO();
            dto.setZdbID(genotype.getZdbID());
            dto.setName(genotype.getHandle());
            dtos.add(dto);
        }
        values.setFishes(dtos);

        List<Figure> figures = pubRepository.getFiguresByPublication(publicationID);
        if (figures == null)
            return null;

        List<FigureDTO> figureDTOs = new ArrayList<FigureDTO>();
        for (Figure figure : figures) {
            FigureDTO dto = new FigureDTO();
            dto.setLabel(figure.getLabel());
            dto.setZdbID(figure.getZdbID());
            figureDTOs.add(dto);
        }
        values.setFigures(figureDTOs);

        // read all genes
        Publication publication = pubRepository.getPublication(publicationID);
        if (publication == null) {
            throw new PublicationNotFoundException(publicationID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(publicationID);
        List<MarkerDTO> genes = new ArrayList<MarkerDTO>();

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setAbbreviation(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            genes.add(gene);
        }
        values.setMarkers(genes);
        return values;
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
        Genotype genotype = pubRepository.getGenotypeByNickname("WT");
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
        MutantRepository mutantRep = RepositoryFactory.getMutantRepository();
        List<Genotype> wiltdyptes = mutantRep.getAllWildtypeGenotypes();
        for (Genotype wiltype : wiltdyptes) {
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
            env.setAbbreviation(antibody.getName());
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
            env.setAbbreviation(antibody.getName());
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
            env.setAbbreviation(gene.getAbbreviation());
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

        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = expressionRep.getExpressionExperiment(experimentID);
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
        Marker oldGene = expressionExperiment.getMarker();
        String oldGeneID = null;
        if (oldGene != null)
            oldGeneID = oldGene.getZdbID();
        String newGeneID = experimentDTO.getGeneZdbID();
        createAuditRecord(expressionExperiment, comment, oldGeneID, newGeneID, "Gene");

        // antibody
        Antibody oldAntibody = expressionExperiment.getAntibody();
        String oldAntibodyID = null;
        if (oldAntibody != null)
            oldAntibodyID = oldAntibody.getZdbID();
        String newAntibodyID = experimentDTO.getAntibodyID();
        createAuditRecord(expressionExperiment, comment, oldAntibodyID, newAntibodyID, "Antibody");

        String oldAssay = expressionExperiment.getAssay().getName();
        String newAssay = experimentDTO.getAssay();
        createAuditRecord(expressionExperiment, comment, oldAssay, newAssay, "Assay");

        String oldEnvironment = expressionExperiment.getGenotypeExperiment().getExperiment().getName();
        String newEnvironment = experimentDTO.getEnvironment();
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

        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = new ExpressionExperiment();
            populateExpressionExperiment(experimentDTO, expressionExperiment);

            expressionRep.createExpressionExperiment(expressionExperiment);
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
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO        dto
     * @param expressionExperiment expression experiment on which the changes are applied.
     */
    public static void populateExpressionExperiment(ExperimentDTO experimentDTO, ExpressionExperiment expressionExperiment) {
        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        // update assay: never null
        ExpressionAssay newAssay = expressionRep.getAssayByName(experimentDTO.getAssay());
        expressionExperiment.setAssay(newAssay);
        experimentDTO.setAssayAbbreviation(newAssay.getAbbreviation());
        // update db link: could be null
        String genbankID = experimentDTO.getGenbankID();
        if (!StringUtils.isEmpty(genbankID)) {
            MarkerDBLink dbLink = expressionRep.getMarkDBLink(genbankID);
            expressionExperiment.setMarkerDBLink(dbLink);
            Marker marker = dbLink.getMarker();
            // If the genbank number is an EST or a cDNA then persist their id in the clone column
            if (marker.getType().equals(Marker.Type.EST) ||
                    marker.getType().equals(Marker.Type.CDNA)) {
                // TOdo: Change to setClone(clone) when clone is subclassed from Marker
                expressionExperiment.setCloneID(marker.getZdbID());
            }
        } else {
            expressionExperiment.setMarkerDBLink(null);
        }
        // update Environment (=experiment)
        GenotypeExperiment genox = expressionRep.getGenotypeExperimentByExperimentIDAndGenotype(experimentDTO.getEnvironmentID(), experimentDTO.getFishID());
        LOG.info("Finding Genotype Experiment for :" + experimentDTO.getEnvironmentID() + ", " + experimentDTO.getFishID());
        // if no genotype experiment found create a new one.
        if (genox == null) {
            genox = expressionRep.createGenoteypExperiment(experimentDTO.getEnvironmentID(), experimentDTO.getFishID());
            LOG.info("Created Genotype Experiment :" + genox.getZdbID());
        }
        expressionExperiment.setGenotypeExperiment(genox);
        // update antibody
        String antibodyID = experimentDTO.getAntibodyID();
        if (!StringUtils.isEmpty(antibodyID)) {
            AntibodyRepository antibodyRep = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRep.getAntibodyByID(antibodyID);
            expressionExperiment.setAntibody(antibody);
        } else {
            expressionExperiment.setAntibody(null);
        }
        // update gene
        String geneID = experimentDTO.getGeneZdbID();
        if (!StringUtils.isEmpty(geneID)) {
            MarkerRepository antibodyRep = RepositoryFactory.getMarkerRepository();
            Marker gene = antibodyRep.getMarkerByID(geneID);
            expressionExperiment.setMarker(gene);
            experimentDTO.setGeneName(gene.getAbbreviation());
        } else {
            expressionExperiment.setMarker(null);
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
        List<ExperimentFigureStage> experiments = expRepository.getExperimentFigureStagesByGeneAndFish(experimentFilter.getPublicationID(),
                experimentFilter.getGeneZdbID(),
                experimentFilter.getFishID(),
                figureID);
        if (experiments == null)
            return null;

        List<ExpressionFigureStageDTO> dtos = new ArrayList<ExpressionFigureStageDTO>();
        for (ExperimentFigureStage efs : experiments) {
            ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
            dto.setExperiment(convertExperimentToDto(efs.getExpressionExperiment()));
            dto.setFigureID(efs.getFigure().getZdbID());
            dto.setFigureLabel(efs.getFigure().getLabel());
            dto.setFigureOrderingLabel(efs.getFigure().getOrderingLabel());
            dto.setStart(createStageDTO(efs.getStart()));
            dto.setEnd((createStageDTO(efs.getEnd())));
            List<ComposedFxTerm> terms = efs.getComposedTerms();
            Collections.sort(terms);
            List<ExpressedTermDTO> termStrings = new ArrayList<ExpressedTermDTO>();
            for (ComposedFxTerm term : terms) {
                ExpressedTermDTO termDto = new ExpressedTermDTO();
                termDto.setExpressionFound(term.isExpressionFound());
                if (term.getSubterm() != null) {
                    termDto.setSubtermName(term.getSubterm().getTermName());
                    termDto.setSubtermID(term.getSubterm().getId());
                }
                termDto.setSupertermName(term.getSuperTerm().getName());
                termDto.setSupertermID(term.getSuperTerm().getId());
                termStrings.add(termDto);
            }
            //dto.setExpressedIn(formatter.getFormattedString());
            Collections.sort(termStrings);
            dto.setExpressedTerms(termStrings);
            dto.setPatoExists(mutantRep.isPatoExists(efs.getExpressionExperiment().getGenotypeExperiment().getZdbID(),
                    efs.getFigure().getZdbID(), efs.getStart().getZdbID(), efs.getEnd().getZdbID()));
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;
    }

    private StageDTO createStageDTO(DevelopmentStage stage) {
        StageDTO stageDTO = new StageDTO();
        stageDTO.setZdbID(stage.getZdbID());
        stageDTO.setStartHours(stage.getHoursStart());
        stageDTO.setEndHours(stage.getHoursEnd());
        stageDTO.setName(stage.getAbbreviation());
        return stageDTO;
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

        List<FigureDTO> dtos = new ArrayList<FigureDTO>();
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
        List<StageDTO> dtos = new ArrayList<StageDTO>();
        for (DevelopmentStage stage : stages) {
            // exclude 'Unknown' stage
            if (stage.getName().equals(DevelopmentStage.UNKNOWN))
                continue;
            StageDTO dto = new StageDTO();
            dto.setName(stage.getAbbreviation() + " " + stage.getTimeString());
            dto.setZdbID(stage.getZdbID());
            dto.setStartHours(stage.getHoursStart());
            dto.setEndHours(stage.getHoursEnd());
            dtos.add(dto);
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
        GoTermExpressionResult result = new GoTermExpressionResult();
        result.setEndStage(expressionExperiment.getEnd());
        result.setStartStage(expressionExperiment.getStart());
        result.setExpressionExperiment(expressionExperiment.getExpressionExperiment());
        populateFigureAnnotation(figureAnnotation, expressionExperiment);

        Set<Figure> figures = new HashSet<Figure>(1);
        figures.add(expressionExperiment.getFigure());
        result.setFigures(figures);

        AnatomyRepository anatRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem unspecified = anatRep.getAnatomyItem(AnatomyItem.UNSPECIFIED);
        result.setAnatomyTerm(unspecified);
        result.setExpressionFound(true);
        ExpressedTermDTO unspecifiedTerm = new ExpressedTermDTO();
        unspecifiedTerm.setSupertermName(AnatomyItem.UNSPECIFIED);
        unspecifiedTerm.setSupertermID(unspecified.getZdbID());
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
        figureAnnotation.setStart(createStageDTO(expressionExperiment.getStart()));
        figureAnnotation.setEnd(createStageDTO(expressionExperiment.getEnd()));
        figureAnnotation.setFigureID(expressionExperiment.getFigure().getZdbID());
        figureAnnotation.setFigureLabel(expressionExperiment.getFigure().getLabel());
        figureAnnotation.setFigureOrderingLabel(expressionExperiment.getFigure().getOrderingLabel());
        figureAnnotation.setExperiment(convertExperimentToDto(expressionExperiment.getExpressionExperiment()));
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

        Figure figure = pubRepository.getFigureByID(experimentDTO.getFigureID());
        figure.setZdbID(experimentDTO.getFigureID());
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
        if (figureAnnotation.getFigureID() == null ||
                figureAnnotation.getStart().getZdbID() == null ||
                figureAnnotation.getEnd().getZdbID() == null)
            return;
        ExperimentFigureStage efs = expRepository.getExperimentFigureStage(experiment.getExperimentZdbID(), figureAnnotation.getFigureID(),
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
     * Save the filter element zdb ID.
     * If the zdb ID is null unset the value.
     * In this case the typeString is used to identify the type of element that should be unset.
     * If typeString is null do nothing.
     *
     * @param publicationID publication
     * @param zdbID         zdbID
     */
    public void setFilterType(String publicationID, String zdbID, String typeString) {

        ActiveData.Type type;
        if (zdbID != null) {
            ActiveData activeData = new ActiveData();
            activeData.setZdbID(zdbID);
            type = activeData.validateID();
        } else {
            if (typeString == null)
                return;
            type = ActiveData.Type.getType(typeString);
        }
        // save figure info into database
        if (type == ActiveData.Type.FIG) {
            Transaction tx = HibernateUtil.currentSession().beginTransaction();
            try {
                if (zdbID != null)
                    profileRep.setCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID, zdbID);
                else {
                    CuratorSession curSession = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID);
                    if (curSession != null)
                        profileRep.deleteCuratorSession(curSession);
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                throw e;
            }
        }
        // save gene info in session
        if (type == ActiveData.Type.GENE) {
            String uniqueKey = createSessionVariableName(publicationID, FX_GENE_FILTER);
            // if zdbID is null then this will unset the attribute
            getServletContext().setAttribute(uniqueKey, zdbID);
        }
        // save fish info in session
        if (type == ActiveData.Type.GENO) {
            String uniqueKey = createSessionVariableName(publicationID, FX_FISH_FILTER);
            getServletContext().setAttribute(uniqueKey, zdbID);
        }
    }

    private String createSessionVariableName(String publicationID, String elementName) {
        return elementName + publicationID;
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

    /**
     * Retrieve the values to be used for the fx filter bar.
     *
     * @param publicationID publication
     * @return FilterValuesDTO
     */
    public FilterValuesDTO getFilterValues(String publicationID) {
        FilterValuesDTO vals = new FilterValuesDTO();
        String uniqueKey = createSessionVariableName(publicationID, FX_FISH_FILTER);
        String fishID = (String) getServletContext().getAttribute(uniqueKey);
        if (fishID != null) {
            FishDTO dto = new FishDTO();
            dto.setZdbID(fishID);
            vals.setFish(dto);
        }

        uniqueKey = createSessionVariableName(publicationID, FX_GENE_FILTER);
        String geneID = (String) getServletContext().getAttribute(uniqueKey);
        if (geneID != null) {
            MarkerDTO marker = new MarkerDTO();
            marker.setZdbID(geneID);
            vals.setMarker(marker);
        }

        CuratorSession attribute = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.FIGURE_ID);
        if (attribute != null) {
            if (StringUtils.isNotEmpty(attribute.getValue())) {
                Figure figure = pubRepository.getFigureByID(attribute.getValue());
                FigureDTO figureDTO = new FigureDTO();
                figureDTO.setLabel(figure.getLabel());
                figureDTO.setZdbID(figure.getZdbID());
                vals.setFigure(figureDTO);
            }
        }
        return vals;
    }

    /**
     * Create a new Pato record
     *
     * @param efs figure annotation
     */
    public void createPatoRecord(ExpressionFigureStageDTO efs) {
        ExpressionExperiment expressionExperiment = expRepository.getExpressionExperiment(efs.getExperiment().getExperimentZdbID());
        Figure figure = pubRepository.getFigureByID(efs.getFigureID());
        DevelopmentStage start = anatomyRep.getStageByID(efs.getStart().getZdbID());
        DevelopmentStage end = anatomyRep.getStageByID(efs.getEnd().getZdbID());
        Phenotype pheno = new Phenotype();
        pheno.setPublication(expressionExperiment.getPublication());
        pheno.setEndStage(end);
        pheno.setStartStage(start);
        pheno.setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
        Set<Figure> figures = new HashSet<Figure>();
        figures.add(figure);
        pheno.setFigures(figures);

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            mutantRep.createDefaultPhenotype(pheno);
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
            currentState = new HashSet<String>();
            getServletContext().setAttribute(key, currentState);
        }
        updateFigureAnnotationSessionSet(checkedExpression.getUniqueID(), currentState, checked);
    }


    /**
     * Add or remove the uniqueID to a set.
     * Method logs if check mark was already checked upon checking or
     * was already unchecked upon unchecking
     *
     * @param uniqueID     unique ID
     * @param currentState set of unique ids
     * @param checked      add or remove object
     */
    public void updateFigureAnnotationSessionSet(String uniqueID, Set<String> currentState, boolean checked) {
        if (checked) {
            boolean success = currentState.add(uniqueID);
            if (!success)
                LOG.error("trying to check figure annotation  " + uniqueID + " that is already checked!");
        } else {
            boolean success = currentState.remove(uniqueID);
            if (!success)
                LOG.error("trying to uncheck figure annotation |" + uniqueID + "| that is not checked!");
        }
    }

    /**
     * Read the checkmark status.
     *
     * @param publicationID Publication
     */
    @SuppressWarnings("unchecked")
    public CheckMarkStatusDTO getFigureAnnotationCheckmarkStatus(String publicationID) {
        CheckMarkStatusDTO vals = new CheckMarkStatusDTO();
        String uniqueKey = createSessionVariableName(publicationID, FX_FIGURE_ANNOTATION_CHECKBOX);
        Set<String> checkmarks = (Set<String>) getServletContext().getAttribute(uniqueKey);
        if (checkmarks == null || checkmarks.size() == 0)
            return null;

        Set<ExpressionFigureStageDTO> figs = createExpressonFigureStages(checkmarks);
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
     * @return list of structure dto's
     */
    public List<PileStructureDTO> getStructures(String publicationID) {
        List<ExpressionStructure> structures = expRepository.retrieveExpressionStructures(publicationID);
        if (structures == null)
            return null;
        List<PileStructureDTO> dtos = new ArrayList<PileStructureDTO>();
        for (ExpressionStructure es : structures) {
            // do not return 'unspecified'
            if (es.getSuperterm().getName().equals(AnatomyItem.UNSPECIFIED))
                continue;
            PileStructureDTO dto = new PileStructureDTO();
            dto.setZdbID(es.getZdbID());
            dto.setCreator(es.getPerson().getName());
            dto.setDate(es.getDate());
            ExpressedTermDTO expressionTerm = new ExpressedTermDTO();
            AnatomyItem superterm = es.getSuperterm();
            expressionTerm.setSupertermID(superterm.getZdbID());
            expressionTerm.setSupertermName(superterm.getName());
            expressionTerm.setSupertermOboID(superterm.getOboID());
            expressionTerm.setSubtermID(es.getSubtermID());
            expressionTerm.setSubtermName(es.getSubtermName());
            if (es instanceof GOExpressionStructure) {
                GOExpressionStructure aes = (GOExpressionStructure) es;
                if (aes.getSubterm() != null) {
                    expressionTerm.setSubtermOboID(((GOExpressionStructure) es).getSubterm().getOboID());
                    expressionTerm.setSubtermOntology(ExpressedTermDTO.Ontology.GO.toString());
                }
            } else {
                expressionTerm.setSubtermOntology(ExpressedTermDTO.Ontology.AO.toString());
            }
            dto.setExpressedTerm(expressionTerm);
            StageDTO start = createStageDTO(superterm.getStart());
            StageDTO end = createStageDTO(superterm.getEnd());
            dto.setStart(start);
            dto.setEnd(end);
            StageRangeIntersection sri = new StageRangeIntersection(createStageDTO(superterm.getStart()), createStageDTO(superterm.getEnd()));
            if (es instanceof AnatomyExpressionStructure) {
                AnatomyExpressionStructure aes = (AnatomyExpressionStructure) es;
                if (aes.getSubterm() != null) {
                    expressionTerm.setSubtermOboID(aes.getSubterm().getOboID());
                    sri.addNewRange(createStageDTO(aes.getSuperterm().getStart()), createStageDTO(aes.getSuperterm().getEnd()));
                }
            }
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;

    }

    /**
     * Remove a structure from the structure pile.
     *
     * @param structureDto Structure DTO
     */
    public void deleteStructure(PileStructureDTO structureDto) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionStructure structure = expRepository.getExpressionStructure(structureDto.getZdbID());
            expRepository.deleteExpressionStructure(structure);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Delete", e);
            tx.rollback();
            throw e;
        }
    }

    /**
     * Update inidividual figure annotations with structures from the pile.
     *
     * @param updateEntity Update Expression dto
     * @return list of updated expression figure stage dtos
     */
    public List<ExpressionFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO updateEntity) {
        List<ExpressionFigureStageDTO> figureAnnotations = updateEntity.getFigureAnnotations();
        if (figureAnnotations == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructures = updateEntity.getStructures();
        if (pileStructures == null || pileStructures.size() == 0)
            return null;


        List<ExpressionFigureStageDTO> updatedAnnotations = new ArrayList<ExpressionFigureStageDTO>();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            // for each figure annotation check which structures need to be added or removed
            for (ExpressionFigureStageDTO dto : figureAnnotations) {
                ExperimentFigureStage experiment = expRepository.getExperimentFigureStage(dto.getExperiment().getExperimentZdbID(),
                        dto.getFigureID(),
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
     * @param selectedPileStructure pilse structure
     * @param intersection          StageIntersection
     * @return list of PileStructureDTO,
     */
    public List<RelatedPileStructureDTO> getTermsWithStageOverlap(PileStructureDTO selectedPileStructure,
                                                                  StageRangeIntersection intersection) {
        AnatomyRepository antRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem term = antRepository.getAnatomyTermByID(selectedPileStructure.getExpressedTerm().getSupertermID());
        List<AnatomyRelationship> relationships = antRepository.getAnatomyRelationships(term);
        List<RelatedPileStructureDTO> structures = new ArrayList<RelatedPileStructureDTO>();
        for (AnatomyRelationship rel : relationships) {
            StageDTO start = createStageDTO(rel.getAnatomyItem().getStart());
            StageDTO end = createStageDTO(rel.getAnatomyItem().getEnd());
            if (intersection.isFullOverlap(start, end)) {
                RelatedPileStructureDTO relatedStructure = populatePileStructureDTO(rel.getAnatomyItem());
                relatedStructure.setRelatedStructure(selectedPileStructure);
                relatedStructure.setRelationship(rel.getRelationship());
                relatedStructure.setStart(start);
                relatedStructure.setEnd(end);
                structures.add(relatedStructure);
            }
        }
        return structures;
/*
        if (getStructuresSelectedTermDevelopsInto) {
            List<AnatomyItem> terms = antRepository.getTermsDevelopingFromWithOverlap(
                    selectedPileStructure.getExpressedTerm().getSupertermID(), startHours, endHours);
            return populatePileStructureDTOs(terms);
        } else {
            List<AnatomyItem> terms = antRepository.getTermsDevelopingIntoWithOverlap(
                    selectedPileStructure.getExpressedTerm().getSupertermID(), startHours, endHours);
            return populatePileStructureDTOs(terms);
        }
*/
    }

    private RelatedPileStructureDTO populatePileStructureDTO(AnatomyItem term) {
        if (term == null)
            return null;

        RelatedPileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = new ExpressedTermDTO();
        expDto.setSupertermName(term.getName());
        expDto.setSupertermID(term.getId());
        expDto.setSupertermOboID(term.getOboID());
        dto.setExpressedTerm(expDto);
        return dto;
    }

    /**
     * Create a new structure for the pile.
     *
     * @param expressedTerm Expressed Term dto
     * @param publicationID pub id
     */
    public PileStructureDTO createPileStructure(ExpressedTermDTO expressedTerm, String publicationID)
            throws PileStructureExistsException, TermNotFoundException {
        if (expressedTerm == null || publicationID == null)
            throw new TermNotFoundException("No Term or publication provided");

        LOG.info("Request: Create Composed term: " + expressedTerm.getComposedTerm());
        if (expRepository.pileStructureExists(expressedTerm, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(expressedTerm);
            LOG.info(exception.getMessage());
            throw exception;
        }
        PileStructureDTO pileStructure = null;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            if (StringUtils.isEmpty(expressedTerm.getSubtermName())) {
                ExpressionStructure structure = createSuperterm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure);
            } else {
                String ontology = expressedTerm.getSubtermOntology();
                if (StringUtils.isEmpty(ontology))
                    throw new RuntimeException("No ontology provided:");

                ExpressionStructure structure = createPostcomposedTerm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure);
            }
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        return pileStructure;
    }

    private PileStructureDTO populatePileStructureDTOObject(ExpressionStructure structure) {
        if (structure == null)
            return null;

        PileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = new ExpressedTermDTO();
        expDto.setSupertermName(structure.getSuperterm().getName());
        expDto.setSupertermID(structure.getSuperterm().getId());
        expDto.setSupertermOboID(structure.getSuperterm().getOboID());
        dto.setExpressedTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        if (!StringUtils.isEmpty(structure.getSubtermID())) {
            if (structure instanceof AnatomyExpressionStructure) {
                AnatomyExpressionStructure aoStructure = (AnatomyExpressionStructure) structure;
                expDto.setSubtermID(aoStructure.getSubtermID());
                expDto.setSubtermName(aoStructure.getSubtermName());
                expDto.setSubtermOntology(ExpressedTermDTO.Ontology.AO.toString());
                expDto.setSubtermOboID(aoStructure.getSubterm().getOboID());
            } else {
                GOExpressionStructure goStructure = (GOExpressionStructure) structure;
                expDto.setSubtermID(goStructure.getSubtermID());
                expDto.setSubtermName(goStructure.getSubtermName());
                expDto.setSubtermOntology(ExpressedTermDTO.Ontology.GO.toString());
                expDto.setSubtermOboID(goStructure.getSubterm().getOboID());
            }
        }
        dto.setStart(createStageDTO(structure.getSuperterm().getStart()));
        dto.setEnd(createStageDTO(structure.getSuperterm().getEnd()));
        return dto;

    }

    private ExpressionStructure createSuperterm(ExpressedTermDTO expressedTerm, String publicationID) throws TermNotFoundException {
        AnatomyItem superterm = anatomyRep.getAnatomyItem(expressedTerm.getSupertermName());
        if (superterm == null)
            throw new TermNotFoundException("No superterm [" + expressedTerm.getSupertermName() + "] found.");
        ExpressionStructure structure = new AnatomyExpressionStructure();
        structure.setSuperterm(superterm);
        structure.setPerson(Person.getCurrentSecurityUser());
        Publication pub = pubRepository.getPublication(publicationID);
        structure.setPublication(pub);
        structure.setDate(new Date());
        anatomyRep.createPileStructure(structure);
        LOG.info("Issued Term creation " + expressedTerm.getComposedTerm());
        return structure;
    }

    private ExpressionStructure createPostcomposedTerm(ExpressedTermDTO expressedTerm, String publicationID)
            throws TermNotFoundException {
        AnatomyItem superterm = anatomyRep.getAnatomyItem(expressedTerm.getSupertermName());
        if (superterm == null)
            throw new TermNotFoundException("No Superterm term [" + expressedTerm.getSupertermName() + " found.");
        ExpressionStructure structure = null;
        String ontology = expressedTerm.getSubtermOntology();
        if (ontology.equals(ExpressedTermDTO.Ontology.AO.toString())) {
            AnatomyItem subterm = anatomyRep.getAnatomyItem(expressedTerm.getSubtermName());
            if (subterm == null)
                throw new TermNotFoundException(expressedTerm.getSubtermName(), ExpressedTermDTO.Ontology.AO.toString());
            AnatomyExpressionStructure aoStructure = new AnatomyExpressionStructure();
            aoStructure.setSuperterm(superterm);
            aoStructure.setSubterm(subterm);
            aoStructure.setPerson(Person.getCurrentSecurityUser());
            Publication pub = pubRepository.getPublication(publicationID);
            aoStructure.setPublication(pub);
            aoStructure.setDate(new Date());
            anatomyRep.createPileStructure(aoStructure);
            structure = aoStructure;
        } else {
            GOExpressionStructure goStructure = new GOExpressionStructure();
            goStructure.setSuperterm(superterm);
            goStructure.setPerson(Person.getCurrentSecurityUser());
            Publication pub = pubRepository.getPublication(publicationID);
            goStructure.setPublication(pub);
            goStructure.setDate(new Date());
            if (!StringUtils.isEmpty(expressedTerm.getSubtermName())) {
                GoTerm subterm = RepositoryFactory.getMutantRepository().getGoTermByName(expressedTerm.getSubtermName());
                if (subterm == null)
                    throw new TermNotFoundException(expressedTerm.getSubtermName(), ExpressedTermDTO.Ontology.GO.toString());
                goStructure.setSubterm(subterm);
            }
            anatomyRep.createPileStructure(goStructure);
            structure = goStructure;
        }
        LOG.info("Issued post-composed creation " + expressedTerm.getComposedTerm());
        return structure;
    }

    private List<PileStructureDTO> populatePileStructureDTOs(List<AnatomyItem> terms) {
        if (terms == null)
            return null;

        List<PileStructureDTO> dtos = new ArrayList<PileStructureDTO>();
        for (AnatomyItem term : terms) {
            dtos.add(populatePileStructureDTO(term));
        }
        return dtos;
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
            if (result.getAnatomyTerm().equals(expressionStructure.getSuperterm())) {
                String subtermID = null;
                OntologyTerm term = result.getSubTerm();
                if (term != null)
                    subtermID = term.getId();
                // check if subterms are equal or both null
                if (subtermID == null && expressionStructure.getSubtermID() == null && expressed == result.isExpressionFound()) {
                    termBeingUsed = true;
                    break;
                }
                if (subtermID != null && expressionStructure.getSubtermID() != null &&
                        subtermID.equals(expressionStructure.getSubtermID()) &&
                        expressed == result.isExpressionFound()) {
                    termBeingUsed = true;
                    break;
                }
            }
        }
        // do nothing term already exists.
        if (termBeingUsed)
            return null;

        // create a new ExpresssionResult record
        // create a new ExpressedTermDTO object that is passed back to the RPC caller
        ExpressedTermDTO expressedTerm = new ExpressedTermDTO();
        if (expressionStructure.getSubtermID() != null) {
            if (expressionStructure instanceof GOExpressionStructure) {
                GoTermExpressionResult newExpression = new GoTermExpressionResult();
                GOExpressionStructure goExpressionStructure = (GOExpressionStructure) expressionStructure;
                newExpression.setSubterm(goExpressionStructure.getSubterm());
                setMainAttributes(experiment, expressionStructure, expressed, newExpression);
                expRepository.createExpressionResult(newExpression, experiment.getFigure());
            } else {
                AnatomyExpressionResult newExpression = new AnatomyExpressionResult();
                AnatomyExpressionStructure anatomyExpression = (AnatomyExpressionStructure) expressionStructure;
                newExpression.setSubterm(anatomyExpression.getSubterm());
                setMainAttributes(experiment, expressionStructure, expressed, newExpression);
                expRepository.createExpressionResult(newExpression, experiment.getFigure());
            }
            expressedTerm.setSubtermID(expressionStructure.getSubtermID());
            expressedTerm.setSubtermName(expressionStructure.getSubtermName());
        } else {
            AnatomyExpressionResult newExpression = new AnatomyExpressionResult();
            setMainAttributes(experiment, expressionStructure, expressed, newExpression);
            expRepository.createExpressionResult(newExpression, experiment.getFigure());
        }
        expressedTerm.setSubtermID(expressionStructure.getSuperterm().getId());
        expressedTerm.setSupertermName(expressionStructure.getSuperterm().getName());
        expressedTerm.setExpressionFound(expressed);
        return expressedTerm;
    }

    private void setMainAttributes(ExperimentFigureStage experiment, ExpressionStructure expressionStructure, boolean expressed, ExpressionResult newExpression) {
        newExpression.setExpressionExperiment(experiment.getExpressionExperiment());
        newExpression.setAnatomyTerm(expressionStructure.getSuperterm());
        newExpression.setExpressionFound(expressed);
        newExpression.setStartStage(experiment.getStart());
        newExpression.setEndStage(experiment.getEnd());
        newExpression.addFigure(experiment.getFigure());
    }

    private void removeExpressionToAnnotation(ExperimentFigureStage experiment, boolean expressed, ExpressionStructure expressionStructure) {
        for (ExpressionResult result : experiment.getExpressionResults()) {
            if (result.getAnatomyTerm().equals(expressionStructure.getSuperterm())) {
                String subtermID = null;
                OntologyTerm term = result.getSubTerm();
                if (term != null)
                    subtermID = term.getId();
                // check if subterms are equal or both null
                if (subtermID == null && expressionStructure.getSubtermID() == null && expressed == result.isExpressionFound()) {
                    expRepository.deleteExpressionResultPerFigure(result, experiment.getFigure());
                    LOG.info("Removed Expression_Result:  " + result.getAnatomyTerm().getName());
                    break;
                }
                if (subtermID != null && expressionStructure.getSubtermID() != null &&
                        subtermID.equals(expressionStructure.getSubtermID()) &&
                        expressed == result.isExpressionFound()) {
                    expRepository.deleteExpressionResultPerFigure(result, experiment.getFigure());
                    OntologyTerm subterm = result.getSubTerm();
                    if (subterm != null)
                        LOG.info("Removed Expression_Result:  " + result.getAnatomyTerm().getName() + " : " + subterm.getTermName());
                    else
                        LOG.info("Removed Expression_Result:  " + result.getAnatomyTerm().getName());
                    break;
                }
            }
        }
    }

    public Set<ExpressionFigureStageDTO> createExpressonFigureStages(Set<String> checkmarks) {
        if (checkmarks == null)
            return null;
        Set<ExpressionFigureStageDTO> set = new HashSet<ExpressionFigureStageDTO>();
        for (String uniqueID : checkmarks) {
            ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
            dto.setUniqueID(uniqueID);
            set.add(dto);
        }
        return set;
    }

    private static final String FX_GENE_FILTER = "fx-gene-filter: ";
    private static final String FX_FISH_FILTER = "fx-fish-filter: ";
    private static final String FX_FIGURE_ANNOTATION_CHECKBOX = "fx-figure-annotation-checkbox: ";

}