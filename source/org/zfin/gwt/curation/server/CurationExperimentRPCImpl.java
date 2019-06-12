package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.web.bind.annotation.PathVariable;
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
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.util.StageRangeIntersection;
import org.zfin.gwt.root.util.StageRangeIntersectionService;
import org.zfin.infrastructure.EntityID;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GenericTermRelationship;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.profile.CuratorSession;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Implementation of RPC calls from the client.
 * Handles curation related calls.
 */
public class CurationExperimentRPCImpl extends ZfinRemoteServiceServlet implements CurationExperimentRPC {

    private final static Logger LOG = LogManager.getLogger(CurationExperimentRPCImpl.class);

    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
    private static ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
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
        List<MarkerDTO> genes = new ArrayList<>();

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setName(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            gene.setMarkerType(marker.getMarkerType().getDisplayName());
            genes.add(gene);
        }

        return genes;
    }

    public List<ExpressionExperimentDTO> getExperimentsByFilter(ExpressionExperimentDTO experimentFilter) {
        List<ExpressionExperiment2> experiments =
                expRepository.getExperimentsByGeneAndFish(experimentFilter.getPublicationID(),
                        experimentFilter.getGene() == null ? null : experimentFilter.getGene().getZdbID(),
                        experimentFilter.getFishID());
        if (experiments == null)
            return null;

        return convertExperiments2ToDTO(experiments);
    }

    private List<ExpressionExperimentDTO> convertExperiments2ToDTO(List<ExpressionExperiment2> experiments) {
        List<ExpressionExperimentDTO> dtos = new ArrayList<>();
        for (ExpressionExperiment2 experiment : experiments) {
            ExpressionExperimentDTO dto = DTOConversionService.convertToExperimentDTO(experiment);
            if (experiment.getFigureStageSet() != null)
                dto.setNumberOfExpressions(experiment.getFigureStageSet().size());
            dtos.add(dto);
        }
        return dtos;
    }

    // Will be cached
    private static List<FishDTO> wildtypeFishList;

    public List<FishDTO> getWildTypeFishList() {
        if (wildtypeFishList != null)
            return wildtypeFishList;

        List<FishDTO> fishDTOList = new ArrayList<>();
        List<Fish> wildtypeList = pubRepository.getWildtypeFish();
        for (Fish wFish : wildtypeList) {
            FishDTO fishy = DTOConversionService.convertToFishDtoFromFish(wFish, true);
            fishDTOList.add(fishy);
        }
        wildtypeFishList = fishDTOList;
        return fishDTOList;
    }

    // THe presentation elements - empty fish records indicating a divider - should be removed here
    // and go into the GWT code.
    public List<FishDTO> getFishList(String publicationID) {
        List<FishDTO> fishDTOList = new ArrayList<>();
        Fish wtFish = pubRepository.getFishByHandle("WT");
        FishDTO fish = DTOConversionService.convertToFishDtoFromFish(wtFish, true);
        fishDTOList.add(fish);
        fish = new FishDTO();
        fish.setZdbID("");
        fish.setName("---------");
        fish.setHandle("---------");
        fishDTOList.add(fish);
        List<Fish> fishList = pubRepository.getNonWTFishByPublication(publicationID);
        for (Fish nonWTFish : fishList) {
            if (nonWTFish.getHandle().equals("WT"))
                continue;
            FishDTO fishy = DTOConversionService.convertToFishDtoFromFish(nonWTFish, true);
            fishDTOList.add(fishy);
        }
        FishDTO separator = new FishDTO();
        separator.setZdbID("");
        separator.setName("---------");
        separator.setHandle("---------");
        fishDTOList.add(separator);
        List<Fish> wildtypeList = pubRepository.getWildtypeFish();
        for (Fish wFish : wildtypeList) {
            FishDTO fishy = DTOConversionService.convertToFishDtoFromFish(wFish, true);
            fishDTOList.add(fishy);
        }
        return fishDTOList;
    }


    public List<GenotypeDTO> getGenotypes(String publicationID) {
        List<GenotypeDTO> genotypes = new ArrayList<>();
        Genotype genotype = pubRepository.getGenotypeByHandle("WT");
        GenotypeDTO fish = new GenotypeDTO();
        fish.setZdbID(genotype.getZdbID());
        fish.setName(genotype.getHandle());
        genotypes.add(fish);
        fish = new GenotypeDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        List<Genotype> genos = pubRepository.getNonWTGenotypesByPublication(publicationID);
        for (Genotype geno : genos) {
            GenotypeDTO fishy = new GenotypeDTO();
            fishy.setZdbID(geno.getZdbID());
            fishy.setName(geno.getHandle());
            genotypes.add(fishy);
        }
        fish = new GenotypeDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        List<Genotype> wildtypes = mutantRep.getAllWildtypeGenotypes();
        for (Genotype wiltype : wildtypes) {
            // only add non-WT wildtypes as WT is placed at the top
            if (wiltype.getHandle().equals(Genotype.WT))
                continue;
            GenotypeDTO fishy = new GenotypeDTO();
            fishy.setZdbID(wiltype.getZdbID());
            fishy.setName(wiltype.getNickname());
            genotypes.add(fishy);
        }
        return genotypes;
    }

    public List<MarkerDTO> getAntibodies(@PathVariable String publicationID) {
        List<Antibody> antibodies = pubRepository.getAntibodiesByPublication(publicationID);
        List<MarkerDTO> markers = getListOfMarkerDtos(antibodies);
        return markers;
    }

    public List<MarkerDTO> getListOfMarkerDtos(List<? extends EntityID> antibodies) {
        List<MarkerDTO> markers = new ArrayList<>();
        for (EntityID antibody : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setName(antibody.getName());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    /**
     * Update an existing experiment.
     *
     * @param experimentDTO experiment to be updated
     */
    public ExpressionExperimentDTO updateExperiment(ExpressionExperimentDTO experimentDTO) {
        if (experimentDTO == null)
            return null;
        String experimentID = experimentDTO.getExperimentZdbID();
        if (experimentID == null)
            throw new RuntimeException("No experiment ID provided");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment2 expressionExperiment = expRepository.getExpressionExperiment2(experimentID);
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
        List<ExpressionFigureStage> experiments = expRepository.getExperimentFigureStagesByGeneAndFish(publicationID, null, null, null);
        Collection<ExpressionStructure> structures = expRepository.retrieveExpressionStructures(publicationID);
        return CollectionUtils.isNotEmpty(experiments) && CollectionUtils.isEmpty(structures);
    }

    @Override
    public List<RelatedEntityDTO> getBackgroundGenotypes(String publicationID) {
        List<Genotype> wildtypes = mutantRep.getAllWildtypeGenotypes();
        List<RelatedEntityDTO> backgroundList = new ArrayList<>(wildtypes.size());
        GenotypeDTO wildtype = null;
        for (Genotype wiltype : wildtypes) {
            // only add non-WT wildtypes as WT is placed at the top
            GenotypeDTO fishy = new GenotypeDTO();
            fishy.setZdbID(wiltype.getZdbID());
            fishy.setName(wiltype.getNickname());
            backgroundList.add(fishy);
            if (wiltype.getHandle().equals(Genotype.WT)) {
                wildtype = fishy;
            }
        }
        backgroundList.remove(wildtype);
        backgroundList.add(0, wildtype);
        return backgroundList;
    }

    /**
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO        dto
     * @param expressionExperiment expression experiment on which the changes are applied.
     */
    public void populateExpressionExperiment(ExpressionExperimentDTO experimentDTO, ExpressionExperiment2 expressionExperiment) {
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
        FishExperiment genox = expRepository.getFishExperimentByExperimentIDAndFishID(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
        LOG.info("Finding Genotype Experiment for :" + experimentDTO.getEnvironment().getZdbID() + ", " + experimentDTO.getFishID());
        // if no genotype experiment found create a new one.
        if (genox == null) {
            genox = expRepository.createFishExperiment(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
            LOG.info("Created Genotype Experiment :" + genox.getZdbID());
        }
        expressionExperiment.setFishExperiment(genox);
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
    public List<ExpressionFigureStageDTO> getExpressionsByFilter(ExpressionExperimentDTO experimentFilter, String figureID) {
        List<ExpressionFigureStage> experiments = expRepository.getExperimentFigureStagesByGeneAndFish(experimentFilter.getPublicationID(),
                experimentFilter.getGene() == null ? null : experimentFilter.getGene().getZdbID(),
                experimentFilter.getFishID(),
                figureID);
        if (experiments == null)
            return null;

        List<ExpressionFigureStageDTO> dtos = new ArrayList<>();
        for (ExpressionFigureStage efs : experiments) {
            ExpressionFigureStageDTO dto = DTOConversionService.convertToExpressionFigureStageDTO(efs);
            dto.setPatoExists(getMutantRepository().isPatoExists(efs));
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

        List<FigureDTO> dtos = new ArrayList<>(figures.size());
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
        List<StageDTO> dtos = new ArrayList<>(stages.size());
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
        List<ExpressionFigureStageDTO> returnList = new ArrayList<>(figureAnnotations.size());
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            returnList.add(createFigureAnnotation(figureAnnotation));
        }
        return returnList;
    }

    @Override
    public List<ExpressionFigureStageDTO> copyExpressions(List<ExpressionFigureStageDTO> copyFromExpressions,
                                                          List<ExpressionFigureStageDTO> copyToExpressions) throws ValidationException {
        if (copyFromExpressions == null || copyToExpressions == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructureAnnotationDTOList = new ArrayList<>(copyFromExpressions.size());
        for (ExpressionFigureStageDTO copyFromAnnotation : copyFromExpressions) {
            pileStructureAnnotationDTOList.addAll(DTOConversionService.getPileStructureDTO(copyFromAnnotation, PileStructureAnnotationDTO.Action.ADD));
        }
        for (ExpressionFigureStageDTO expressionFigureStageDTO : copyToExpressions) {
            UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateExpressionDTO = new UpdateExpressionDTO<>();
            updateExpressionDTO.addFigureAnnotation(expressionFigureStageDTO);
            updateExpressionDTO.setStructures(pileStructureAnnotationDTOList);
            // remove expressions outside the valid stage range from the new structures to be added
            removeInvalidExpressionsFromAnnotation(updateExpressionDTO);

            //updateExpressionDTO.
            updateStructuresForExpression(updateExpressionDTO);
        }
        return copyToExpressions;
    }

    protected void removeInvalidExpressionsFromAnnotation(UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateExpressionDTO) {
        List<ExpressionFigureStageDTO> figureAnnotations = updateExpressionDTO.getFigureAnnotations();
        if (figureAnnotations == null)
            return;
        if (figureAnnotations.size() > 1)
            throw new RuntimeException("Can only handle single figure annotation");

        ExpressionFigureStageDTO expressionFigureStageDTO = figureAnnotations.get(0);
        List<PileStructureAnnotationDTO> newStructureList = new ArrayList<>(updateExpressionDTO.getStructures().size());
        StageRangeIntersection intersection = new StageRangeIntersection(expressionFigureStageDTO.getStart(), expressionFigureStageDTO.getEnd());
        for (PileStructureAnnotationDTO pileStructure : updateExpressionDTO.getStructures()) {
            ExpressedTermDTO expressedTermShallow = pileStructure.getExpressedTerm();
            // retrieve expressionResult2 to obtain stage info for expressed Terms
            ExpressionResult2 expressionResult2 = getExpressionRepository().getExpressionResult2(pileStructure.getExpressedTerm().getId());
            ExpressedTermDTO expressedTerm = DTOConversionService.convertToExpressedTermDTO(expressionResult2, false, false);
            if (intersection.isOverlap(expressedTerm))
                newStructureList.add(pileStructure);
        }
        updateExpressionDTO.setStructures(newStructureList);
    }

    private ExpressionFigureStageDTO createFigureAnnotation(ExpressionFigureStageDTO figureAnnotation) {
        if (figureAnnotation == null)
            return null;

        ExpressionFigureStageDTO fullDto = null;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionFigureStage expressionFigureStage = DTOConversionService.getExpressionFigureStageFromDTO(figureAnnotation);
            ExpressionExperiment2 expressionExperiment = getExpressionRepository().getExpressionExperiment2(figureAnnotation.getExperiment().getExperimentZdbID());
            expressionFigureStage.setExpressionExperiment(expressionExperiment);
            RepositoryFactory.getExpressionRepository().createExpressionFigureStage(expressionFigureStage);
            fullDto = DTOConversionService.convertToExpressionFigureStageDTOShallow(expressionFigureStage);
            fullDto.setPatoExists(getMutantRepository().isPatoExists(expressionFigureStage));
            tx.commit();
        } catch (ConstraintViolationException e) {
            tx.rollback();
            throw new RuntimeException("Figure Annotation already exist. Expressions have to be unique.");
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return fullDto;
    }

    private void populateFigureAnnotation(ExpressionFigureStageDTO figureAnnotation, ExperimentFigureStage expressionExperiment) {
        figureAnnotation.setStart(DTOConversionService.convertToStageDTO(expressionExperiment.getStart()));
        figureAnnotation.setEnd(DTOConversionService.convertToStageDTO(expressionExperiment.getEnd()));
        figureAnnotation.setFigure(DTOConversionService.convertToFigureDTO(expressionExperiment.getFigure()));
        if (expressionExperiment.getExpressionExperiment() == null)
            LOG.error("Could not find an expression experiment for " + expressionExperiment);
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
        if (figureAnnotation.getID() == 0)
            return;
        ExpressionFigureStage efs = expRepository.getExperimentFigureStage(figureAnnotation.getID());
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
        ExpressionExperiment2 expressionExperiment = expRepository.getExpressionExperiment2(efs.getExperiment().getExperimentZdbID());
        Figure figure = pubRepository.getFigureByID(efs.getFigure().getZdbID());
        DevelopmentStage start = anatomyRep.getStageByID(efs.getStart().getZdbID());
        DevelopmentStage end = anatomyRep.getStageByID(efs.getEnd().getZdbID());
        PhenotypeExperiment phenoExperiment = new PhenotypeExperiment();
        phenoExperiment.setFigure(figure);
        phenoExperiment.setStartStage(start);
        phenoExperiment.setEndStage(end);
        phenoExperiment.setFishExperiment(expressionExperiment.getFishExperiment());
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
        Set<Long> currentState = (Set<Long>) getServletContext().getAttribute(key);
        if (currentState == null) {
            currentState = new HashSet<>(10);
            getServletContext().setAttribute(key, currentState);
        }
        updateFigureAnnotationSessionSet(checkedExpression.getID(), currentState, checked);
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
    public void updateFigureAnnotationSessionSet(Long uniqueID, Set<Long> currentState, boolean checked) {
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
        List<ExpressionFigureStageDTO> efs = new ArrayList<>(figs);
        vals.setFigureAnnotations(efs);
        return vals;
    }

    /**
     * Check if the structure section should be hidden or displayed.
     *
     * @param publicationID publication id
     * @return show: true of false
     */
    public boolean readStructureSectionVisibility(String publicationID, boolean isPhenotype) {
        CuratorSession.Attribute attribute;
        if (isPhenotype)
            attribute = CuratorSession.Attribute.SHOW_PHENTOYPE_STRUCTURE_SECTION;
        else
            attribute = CuratorSession.Attribute.SHOW_STRUCTURE_SECTION;
        CuratorSession session = profileRep.getCuratorSession(publicationID, attribute);
        return session == null || session.getValue().equals("true");
    }

    /**
     * Set the visibility status for the structure section.
     *
     * @param publicationID publication id
     * @param show          true or false
     */
    public void setStructureVisibilitySession(String publicationID, boolean show, boolean isPhenotype) {
        CuratorSession.Attribute attribute;
        if (isPhenotype)
            attribute = CuratorSession.Attribute.SHOW_PHENTOYPE_STRUCTURE_SECTION;
        else
            attribute = CuratorSession.Attribute.SHOW_STRUCTURE_SECTION;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            profileRep.setCuratorSession(publicationID, attribute, show);
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
        List<ExpressionPileStructureDTO> dtos = new ArrayList<>(structures.size());
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
    public List<ExpressionFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateEntity) throws ValidationException {
        List<ExpressionFigureStageDTO> figureAnnotations = updateEntity.getFigureAnnotations();
        if (figureAnnotations == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructures = updateEntity.getStructures();
        if (pileStructures == null || pileStructures.isEmpty())
            return null;


        List<ExpressionFigureStage> updatedAnnotationList = new ArrayList<>(figureAnnotations.size());
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            // for each figure annotation check which structures need to be added or removed
            for (ExpressionFigureStageDTO dto : figureAnnotations) {
                ExpressionFigureStage experiment = expRepository.getExperimentFigureStage(dto.getID());
                updatedAnnotationList.add(experiment);
                // sort: first deletions then additions. This will allow to remove a non-EaP structure and replace it with an eap.
                // we do not allow to add an eap to a non-EaP annotation.
                Collections.sort(pileStructures);
                for (PileStructureAnnotationDTO pileStructure : pileStructures) {
                    ExpressionStructure expressionStructure;
                    if (pileStructure.getExpressedTerm() != null)
                        expressionStructure = DTOConversionService.getExpressionStructureFromDTO(pileStructure.getExpressedTerm());
                    else
                        expressionStructure = expRepository.getExpressionStructure(pileStructure.getZdbID());
                    if (expressionStructure == null)
                        throw new ValidationException("Could not find pile structure " + pileStructure.getZdbID());

                    // add expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.ADD) {
                        ExpressedTermDTO expTerm = addExpressionToAnnotation(experiment, expressionStructure, pileStructure.isExpressed());
                        if (experiment.getExpressionExperiment().isWildtype() && expTerm != null && expTerm.isEap())
                            throw new ValidationException("Cannot add an EaP annotation to a wildtype / standard fish");
                        if (experiment.hasInvalidCombination())
                            throw new ValidationException("Cannot add 'absent phenotypic with a non-absent phenotypic term");
                        if (expTerm != null) {
                            dto.addExpressedTerm(expTerm);
                        }
                    }
                    // remove expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.REMOVE) {
                        removeExpressionToAnnotation(experiment, expressionStructure);
                    }
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            String message = "Could not Add or Delete terms";
            message += "<br/>";
            message += e.getMessage();
            LOG.error(message, e);
            tx.rollback();
            throw new ValidationException(message);
        }
        for (ExpressionFigureStageDTO dto : figureAnnotations) {
            setFigureAnnotationStatus(dto, false);
        }
        List<ExpressionFigureStageDTO> updatedAnnotations =
                updatedAnnotationList.stream()
                        .map(expressionFigureStage ->
                                {
                                    ExpressionFigureStageDTO dto = DTOConversionService.convertToExpressionFigureStageDTO(expressionFigureStage);
                                    dto.setPatoExists(getMutantRepository().isPatoExists(expressionFigureStage));
                                    return dto;
                                }
                        )
                        .collect(toList());

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
                                                                  StageRangeIntersectionService intersection) {
        GenericTerm term = ontologyRepository.getTermByZdbID(selectedPileStructure.getExpressedTerm().getEntity().getSuperTerm().getZdbID());
        List<GenericTermRelationship> terms = term.getAllDirectlyRelatedTerms();
        List<RelatedPileStructureDTO> structures = new ArrayList<>(terms.size());
        ExpressionStructure expressionStructure = DTOConversionService.getExpressionStructureFromDTO(selectedPileStructure.getExpressedTerm());
        for (GenericTermRelationship rel : terms) {
            Term relatedTerm = rel.getRelatedTerm(term);
            // some terms may be stage terms and should be ignored.
            if (relatedTerm == null) {
                throw new RuntimeException("No related term found for term: " + term.getZdbID());
            }

            if (!relatedTerm.getOntology().equals(Ontology.ANATOMY)) {
                continue;
            }

            relatedTerm = ontologyRepository.getTermByZdbID(relatedTerm.getZdbID());
            Term anatomyItem = ontologyRepository.getTermByOboID(relatedTerm.getOboID());
            StageDTO start = DTOConversionService.convertToStageDTO(anatomyItem.getStart());
            StageDTO end = DTOConversionService.convertToStageDTO(anatomyItem.getEnd());
            if (intersection.hasOverlapWithAllStageRanges(start, end)) {
                RelatedPileStructureDTO relatedStructure = populatePileStructureDTO(rel.getRelatedTerm(term));
                relatedStructure.setRelatedStructure(selectedPileStructure);
                relatedStructure.setRelationship(rel.getRelationshipType().getDbMappedName());
                relatedStructure.setStart(start);
                relatedStructure.setEnd(end);
                relatedStructure.getExpressedTerm().setExpressionFound(selectedPileStructure.getExpressedTerm().isExpressionFound());
                EapQualityTermDTO qualityTerm = DTOConversionService.convertToEapQualityTermDTO(expressionStructure);
                if (qualityTerm != null)
                    relatedStructure.getExpressedTerm().setQualityTerm(qualityTerm);
                structures.add(relatedStructure);
            }
        }
        return structures;
    }

    private RelatedPileStructureDTO populatePileStructureDTO(GenericTerm term) {
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
     * @param expressionFigureStage ExpressionFigureStage
     * @param expressed             structure is expressed or not [true/false]
     * @param expressionStructure   structure selected from the pile
     * @return expressedTermDTO used to return to RPC caller
     */
    private ExpressedTermDTO addExpressionToAnnotation(ExpressionFigureStage expressionFigureStage,
                                                       ExpressionStructure expressionStructure,
                                                       boolean expressed) throws ValidationException {
        boolean termBeingUsed = experimentHasExpression(expressionFigureStage, expressionStructure, expressed);
        // do nothing term already exists.
        if (termBeingUsed)
            return null;

        // create a new ExpressionResult record
        // create a new ExpressedTermDTO object that is passed back to the RPC caller
        ExpressedTermDTO expressedTerm = DTOConversionService.convertToExpressedTermDTO(expressionStructure);
        // see if an expressionResult2 record exists.
        ExpressionResult2 result = getExpressionResult(expressionFigureStage, expressionStructure);
        if (result != null) {
            // cannot add EaP to non-EaP and vice versa
            if ((result.isEap() && !expressionStructure.isEap()) ||
                    (!result.isEap() && expressionStructure.isEap())) {
                throw new ValidationException("Cannot add a phenotypic term to a non-phenotypic one");
            }
            result.addPhenotypeTerm(expressionStructure);
        } else {
            ExpressionResult2 newExpressionResult = new ExpressionResult2();
            newExpressionResult.setSubTerm(expressionStructure.getSubterm());
            newExpressionResult.setSuperTerm(expressionStructure.getSuperterm());
            newExpressionResult.setExpressionFound(expressionStructure.isExpressionFound());
            newExpressionResult.addPhenotypeTerm(expressionStructure);
            expressionFigureStage.addExpressionResult(newExpressionResult);
            newExpressionResult.setExpressionFigureStage(expressionFigureStage);

        }
        TermDTO subtermDto = new TermDTO();
        subtermDto.setZdbID(expressionStructure.getSuperterm().getZdbID());
        subtermDto.setName(expressionStructure.getSuperterm().getZdbID());
        expressedTerm.setExpressionFound(expressed);
        return expressedTerm;
    }

    private ExpressionResult2 getExpressionResult(ExpressionFigureStage figureStage, ExpressionStructure expressionStructure) {
        for (ExpressionResult2 result : figureStage.getExpressionResultSet()) {
            if (result.getEntity().equals(expressionStructure))
                return result;
        }
        return null;
    }

    private boolean experimentHasExpression(ExpressionFigureStage experiment, ExpressionStructure expressionStructure, boolean expressed) {
        for (ExpressionResult2 result : experiment.getExpressionResultSet()) {
            if (expressionResultHasExpressionStructure(expressionStructure, result))
                return true;
        }
        return false;
    }

    private boolean expressionResultHasExpressionStructure(ExpressionStructure expressionStructure, ExpressionResult2 result) {
        if (!result.getSuperTerm().equals(expressionStructure.getSuperterm()))
            return false;
        String subtermID = null;
        Term term = result.getSubTerm();
        if (term != null)
            subtermID = term.getZdbID();
        if (subtermID == null && expressionStructure.getSubterm() != null ||
                subtermID != null && expressionStructure.getSubterm() == null)
            return false;
        if (subtermID != null && !subtermID.equals(expressionStructure.getSubterm().getZdbID()))
            return false;
        // check expressed_in
        if (result.isExpressionFound() != expressionStructure.isExpressionFound())
            return false;
        // check qualities
        if ((CollectionUtils.isEmpty(result.getPhenotypeTermSet()) && expressionStructure.getEapQualityTerm() != null) ||
                (CollectionUtils.isNotEmpty(result.getPhenotypeTermSet()) && expressionStructure.getEapQualityTerm() == null))
            return false;
        // if no EaP is found then they must equal
        if (CollectionUtils.isEmpty(result.getPhenotypeTermSet()))
            return true;

        // check if a matching quality is found
        boolean isMatchingQuality = false;
        boolean isMatchingTag = false;
        for (ExpressionPhenotypeTerm qualityTerm : result.getPhenotypeTermSet()) {
            if (qualityTerm.getQualityTerm().equals(expressionStructure.getEapQualityTerm())) {
                isMatchingQuality = true;
            }
            if (qualityTerm.getTag().equals(expressionStructure.getTag()))
                isMatchingTag = true;
        }
        return isMatchingQuality && isMatchingTag;
    }

    private void removeExpressionToAnnotation(ExpressionFigureStage expressionFigureStage, ExpressionStructure expressionStructure) {
        boolean termBeingUsed = experimentHasExpression(expressionFigureStage, expressionStructure, expressionStructure.isExpressionFound());
        // do nothing term already exists.
        if (!termBeingUsed)
            return;

        // create a new ExpressionResult record
        // create a new ExpressedTermDTO object that is passed back to the RPC caller
        ExpressedTermDTO expressedTerm = DTOConversionService.convertToExpressedTermDTO(expressionStructure);
        // get naked structure
        ExpressionResult2 result = getExpressionResult(expressionFigureStage, expressionStructure);
        if (result != null) {
            if (!expressionStructure.isEap()) {
                expressionFigureStage.getExpressionResultSet().remove(result);
            } else {
                // remove the whole result as it is an EaP if there is at least one quality on it
                // and cannot turn into a non-eap
                if (result.getPhenotypeTermSet().size() == 1) {
                    expressionFigureStage.getExpressionResultSet().remove(result);
                } else {

                    ExpressionPhenotypeTerm termToBeRemoved = null;
                    for (ExpressionPhenotypeTerm term : result.getPhenotypeTermSet())
                        if (term.getQualityTerm().equals(expressionStructure.getEapQualityTerm()))
                            termToBeRemoved = term;
                    if (termToBeRemoved != null) {
                        result.getPhenotypeTermSet().remove(termToBeRemoved);

                    }
                }
            }
        }
        // Removal queries have to happen before insert queries...
        // Hibernate may order removal and addition to other criterias
        HibernateUtil.currentSession().flush();
    }

    public Set<ExpressionFigureStageDTO> createExpressionFigureStages(Collection<String> checkMarks) {
        if (checkMarks == null)
            return null;
        Set<ExpressionFigureStageDTO> set = new HashSet<>(checkMarks.size());
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
