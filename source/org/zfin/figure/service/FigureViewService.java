package org.zfin.figure.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.figure.presentation.*;
import org.zfin.framework.ComparatorCreator;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;

import java.util.*;

/**
 * This will likely end up merged with the existing FigureService
 */
@Service
public class FigureViewService {

    static Logger LOG = Logger.getLogger(FigureViewService.class);

    @Autowired
    private PhenotypeRepository phenotypeRepository;

    @Autowired
    private ProfileRepository profileRepository;

    /**
     * Get a list of ExpressionTableRows for the given figure
     */
    public List<ExpressionTableRow> getExpressionTableRows(Figure figure) {
        List<ExpressionTableRow> rows = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            if (expressionResult.getExpressionExperiment().getGene() != null) {
                rows.add(new ExpressionTableRow(expressionResult));
            }
        }


        //taking advantage of domain objects having their own comparators
        Collections.sort(rows, ComparatorCreator.orderBy("gene", "fishNameOrder", "experiment", "start", "end", "entity"));

        return rows;
    }

    public FigureExpressionSummary getFigureExpressionSummary(Figure figure) {
        FigureExpressionSummary summary = new FigureExpressionSummary();
        summary.setGenes(getExpressionGenes(figure));
        summary.setAntibodies(getAntibodies(figure));
        summary.setFish(getExpressionFish(figure));
        summary.setSequenceTargetingReagents(getExpressionSTR(figure));
        summary.setExperiments(getExpressionCondition(figure));
        summary.setEntities(getExpressionEntities(figure));
        summary.setStartStage(getExpressionStartStage(figure));
        summary.setEndStage(getExpressionEndStage(figure));
        Clone probe = getProbeForFigure(figure);
        summary.setProbe(probe);
        if (probe != null) {
            summary.setProbeSuppliers(profileRepository.getSupplierLinksForZdbId(probe.getZdbID()));
        }
        return summary;
    }

    public FigurePhenotypeSummary getFigurePhenotypeSummary(Figure figure) {
        FigurePhenotypeSummary summary = new FigurePhenotypeSummary();
        summary.setFish(getPhenotypeFish(figure));
        summary.setSequenceTargetingReagents(getPhenotypeSTR(figure));
        summary.setExperiments(getPhenotypeCondition(figure));
        summary.setEntities(getPhenotypeEntitiesFromWarehouse(phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())));
        summary.setStartStage(getPhenotypeStartStage(figure));
        summary.setEndStage(getPhenotypeEndStage(figure));
        return summary;
    }

    public boolean showExpressionQualifierColumn(List<ExpressionTableRow> rows) {
        for (ExpressionTableRow row : rows) {
            if (!row.getExpressionFound()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Get a a list of AntibodyTableRows for the given figure
     */
    public List<AntibodyTableRow> getAntibodyTableRows(Figure figure) {
        List<AntibodyTableRow> rows = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            if ((expressionResult.getExpressionExperiment().getGene() == null) &&
                    (expressionResult.getExpressionExperiment().getAntibody() != null)) {
                rows.add(new AntibodyTableRow(expressionResult));
            }
        }

        Collections.sort(rows, ComparatorCreator.orderBy("antibody", "assay", "fishNameOrder", "experiment", "start", "end", "entity"));

        return rows;
    }

    public boolean showAntibodyQualifierColumn(List<AntibodyTableRow> rows) {
        for (AntibodyTableRow row : rows) {
            if (!row.getExpressionFound()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Get a list of PhenotypeTableRows for the given figure
     */
    public List<PhenotypeTableRow> getPhenotypeTableRows(List<PhenotypeWarehouse> warehouseList) {
        List<PhenotypeTableRow> rows = new ArrayList<>();

        for (PhenotypeWarehouse warehouse : warehouseList) {
            for (PhenotypeStatementWarehouse phenotypeStatement : warehouse.getStatementWarehouseSet()) {
                rows.add(new PhenotypeTableRow(phenotypeStatement));
            }
        }

        //taking advantage of domain objects having their own comparators, though, in the case of genotype, we don't want it!
        Collections.sort(rows, ComparatorCreator.orderBy("fishNameOrder", "experiment", "start", "end", "phenotypeStatement"));
        return rows;
    }


    /**
     * Get a sorted list of genes for which expression is shown in this figure
     */
    public List<Marker> getExpressionGenes(Figure figure) {
        List<Marker> genes = new ArrayList<>();
        for (ExpressionResult er : figure.getExpressionResults()) {
            ExpressionExperiment ee = er.getExpressionExperiment();
            Marker marker = ee.getGene();

            if ((marker != null)
                    && (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG))
                    && !genes.contains(marker)) {
                genes.add(ee.getGene());
            }
        }
        Collections.sort(genes);
        LOG.debug("found " + genes.size() + " genes for " + figure.getZdbID());
        return genes;
    }

    /**
     * Get a sorted list of antibodies with labeled expression in this figure
     */
    public List<Marker> getAntibodies(Figure figure) {
        List<Marker> antibodies = new ArrayList<>();
        for (ExpressionResult er : figure.getExpressionResults()) {
            ExpressionExperiment ee = er.getExpressionExperiment();
            Marker antibody = ee.getAntibody();

            if ((antibody != null)
                    && (antibody.getType() == Marker.Type.ATB)
                    && !antibodies.contains(antibody)) {
                antibodies.add(antibody);
            }
        }
        Collections.sort(antibodies);
        LOG.debug("found " + antibodies.size() + " antibodies for " + figure.getZdbID());
        return antibodies;
    }

    /**
     * Sorted/unique list of genotypes from the gene expression in this figure
     */
    public List<Fish> getExpressionFish(Figure figure) {
        Set<Fish> fishSet = new TreeSet<>();
        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            fishSet.add(expressionResult.getExpressionExperiment().getFishExperiment().getFish());
        }
        return new ArrayList<>(fishSet);
    }

    /**
     * Get the list of sequence targeting reagent (knockdown reagents) shown in a figure
     */
    public List<SequenceTargetingReagent> getExpressionSTR(Figure figure) {
        List<SequenceTargetingReagent> strs = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            for (SequenceTargetingReagent str : expressionResult.getExpressionExperiment().getFishExperiment().getFish().getStrList()) {
                if (str != null && !strs.contains(str)) {
                    strs.add(str);
                }
            }
        }

        Collections.sort(strs);
        return strs;
    }

    /**
     * Sorted/unique list of conditions (experiments) from the expression shown in this Figure
     */
    public List<Experiment> getExpressionCondition(Figure figure) {
        List<Experiment> conditions = new ArrayList<>();

        for (ExpressionResult expressonResult : figure.getExpressionResults()) {
            if (canAddExperimentToConditionsList(expressonResult.getExpressionExperiment().getFishExperiment(), conditions)) {
                conditions.add(expressonResult.getExpressionExperiment().getFishExperiment().getExperiment());
            }
        }

        Collections.sort(conditions);
        return conditions;
    }

    /*
    * Sorted & unique list of superTerm & subTerm wrapped as PostComposedEntities from figure expression,
    * excluding if expressionFound is false
    * */
    public List<PostComposedEntity> getExpressionEntities(Figure figure) {
        List<PostComposedEntity> entities = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            if (expressionResult.isExpressionFound()) {
                if (!entities.contains(expressionResult.getEntity())) {
                    entities.add(expressionResult.getEntity());
                }
            }
        }
        Collections.sort(entities);
        return entities;

    }

    /**
     * Earliest stage from expression of this figure
     */
    public DevelopmentStage getExpressionStartStage(Figure figure) {

        List<DevelopmentStage> stages = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            stages.add(expressionResult.getStartStage());
        }

        if (stages.size() == 0) {
            return null;
        }

        return Collections.min(stages);
    }

    /**
     * Latest stage from expression of this figure
     */
    public DevelopmentStage getExpressionEndStage(Figure figure) {

        List<DevelopmentStage> stages = new ArrayList<>();

        for (ExpressionResult expressionResult : figure.getExpressionResults()) {
            stages.add(expressionResult.getEndStage());
        }

        if (stages.size() == 0) {
            return null;
        }

        return Collections.max(stages);
    }

    /**
     * Sorted/unique list of genotypes from the phenotype shown in this Figure
     */
    public List<Fish> getPhenotypeFish(Figure figure) {
        List<Fish> fishList = new ArrayList<>();

        for (PhenotypeWarehouse phenotypeExperiment : phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())) {
            if (!fishList.contains(phenotypeExperiment.getFishExperiment().getFish())) {
                fishList.add(phenotypeExperiment.getFishExperiment().getFish());
            }
        }

        Collections.sort(fishList);
        return fishList;
    }

    /**
     * Get the list of sequence targeting reagent (knockdown reagents) shown in a figure
     */
    public List<SequenceTargetingReagent> getPhenotypeSTR(Figure figure) {
        List<SequenceTargetingReagent> strs = new ArrayList<>();

        for (PhenotypeWarehouse phenotypeExperiment : phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())) {
            for (SequenceTargetingReagent str : phenotypeExperiment.getFishExperiment().getFish().getStrList()) {
                if (str != null && !strs.contains(str)) {
                    strs.add(str);
                }
            }
        }

        Collections.sort(strs);
        return strs;
    }

    /**
     * Sorted/unique list of conditions (experiments) from the phenotype shown in this Figure
     */
    public List<Experiment> getPhenotypeCondition(Figure figure) {
        List<Experiment> conditions = new ArrayList<>();

        for (PhenotypeWarehouse phenotypeExperiment : phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())) {
            if (canAddExperimentToConditionsList(phenotypeExperiment.getFishExperiment(), conditions)) {
                conditions.add(phenotypeExperiment.getFishExperiment().getExperiment());
            }
        }

        Collections.sort(conditions);
        return conditions;
    }

    private boolean canAddExperimentToConditionsList(FishExperiment fishExperiment, List<Experiment> conditions) {
        return !(fishExperiment == null
                || fishExperiment.isStandardOrGenericControl()
                || conditions.contains(fishExperiment.getExperiment()));
    }

    /**
     * Sorted & unique list of superTerm & subTerm wrapped as PostComposedEntities from figure phenotype, excluding qualities
     */
    public List<PostComposedEntity> getPhenotypeEntitiesFromWarehouse(List<PhenotypeWarehouse> warehouseList) {
        List<PostComposedEntity> entities = new ArrayList<>();

        for (PhenotypeWarehouse warehouse : warehouseList) {
            for (PhenotypeStatementWarehouse phenotypeStatement : warehouse.getStatementWarehouseSet()) {

                if (!entities.contains(phenotypeStatement.getEntity())) {
                    entities.add(phenotypeStatement.getEntity());
                }

                if (phenotypeStatement.getRelatedEntity() != null
                        && !entities.contains(phenotypeStatement.getRelatedEntity())) {
                    entities.add(phenotypeStatement.getRelatedEntity());
                }
            }
        }
        Collections.sort(entities);
        return entities;
    }

    /**
     * Earliest stage from phenotype of this figure
     */
    public DevelopmentStage getPhenotypeStartStage(Figure figure) {

        List<DevelopmentStage> stages = new ArrayList<>();

        for (PhenotypeWarehouse phenotypeExperiment : phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())) {
            stages.add(phenotypeExperiment.getStart());
        }

        if (stages.size() == 0) {
            return null;
        }

        return Collections.min(stages);
    }

    /**
     * Latest stage from phenotype of this figure
     */
    public DevelopmentStage getPhenotypeEndStage(Figure figure) {

        List<DevelopmentStage> stages = new ArrayList<>();

        for (PhenotypeWarehouse phenotypeExperiment : phenotypeRepository.getPhenotypeWarehouse(figure.getZdbID())) {
            stages.add(phenotypeExperiment.getEnd());
        }

        if (stages.size() == 0) {
            return null;
        }

        return Collections.max(stages);
    }

    public String getFullFigureLabel(Figure figure) {
        return figure.getPublication().getShortAuthorList().replace("<i>", "").replace("</i>", "")
                + ", " + figure.getLabel();
    }

    public Clone getProbeForFigure(Figure figure) {
        Clone probe = null;
        if (!CollectionUtils.isEmpty(figure.getExpressionResults())) {
            ExpressionResult firstExpressionResult = figure.getExpressionResults().iterator().next();
            if (firstExpressionResult != null) {
                probe = firstExpressionResult.getExpressionExperiment().getProbe();
            }
        }

        return probe;
    }

    public boolean showElsevierMessage(Publication publication) {
        if (publication == null || publication.getJournal() == null || publication.getJournal().getPublisher() == null) {
            return false;
        }
        return publication.getJournal().getPublisher().equals("Elsevier");
    }

    public boolean hasAcknowledgment(Publication publication) {
        if (publication == null || publication.getAcknowledgment() == null) {
            return false;
        }
        return !publication.getAcknowledgment().equals("");
    }

    /**
     * This logic was taken from the app page. For these 3 pubs, we show an extra link.
     */
    public boolean showThisseInSituLink(Publication publication) {
        return isALargeDirectSubmissionPublication(publication);
    }

    /* Same as above, for these pubs we show the errataAndNotes property of the publication */
    public boolean showErrataAndNotes(Publication publication) {
        return isALargeDirectSubmissionPublication(publication);
    }

    public boolean showMultipleMediumSizedImages(Publication publication) {
        return !isALargeDirectSubmissionPublication(publication);
    }

    public boolean isALargeDirectSubmissionPublication(Publication publication) {
        List<String> pubZdbIDs = new ArrayList<>();
        pubZdbIDs.add("ZDB-PUB-051025-1");
        pubZdbIDs.add("ZDB-PUB-040907-1");
        pubZdbIDs.add("ZDB-PUB-010810-1");

        return pubZdbIDs.contains(publication.getZdbID());
    }

}

