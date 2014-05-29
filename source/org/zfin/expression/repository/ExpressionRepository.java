package org.zfin.expression.repository;

import org.zfin.expression.*;
import org.zfin.expression.presentation.ExpressedStructurePresentation;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.expression.presentation.StageExpressionPresentation;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface ExpressionRepository {

    int getExpressionPubCountForGene(Marker marker);

    int getExpressionPubCountForEfg(Marker marker);

    int getExpressionPubCountForClone(Clone marker);

    Publication getExpressionSinglePub(Marker marker);

    int getExpressionFigureCountForEfg(Marker marker);

    int getExpressionFigureCountForGene(Marker marker);

    int getExpressionFigureCountForGenotype(Genotype genotype);

    FigureLink getExpressionSingleFigure(Marker marker);

    int getExpressionFigureCountForClone(Clone clone);

    /**
     *
     * @param marker marker
     * @return List of Object[int figureCount,String pubZdbID,String cloneZdbID)
     */
    List<PublicationExpressionBean> getDirectlySubmittedExpressionForGene(Marker marker);

    List<PublicationExpressionBean> getDirectlySubmittedExpressionForEfg(Marker marker);

    List<PublicationExpressionBean> getDirectlySubmittedExpressionForClone(Clone clone) ;

    /**
     * Retrieve an expression experiment by ID.
     *
     * @param experimentID expression experiment ID
     * @return ExpressionExperiment
     */
    ExpressionExperiment getExpressionExperiment(String experimentID);

    /**
     * Retrieve an assay by name.
     *
     * @param assay assay name
     * @return expression Assay
     */
    ExpressionAssay getAssayByName(String assay);

    /**
     * Retrieve db link by id.
     *
     * @param genbankID genbank id
     * @return MarkerDBLink
     */
    MarkerDBLink getMarkDBLink(String genbankID);

    /**
     * Retrieve GenotypeExperiment by Experiment ID
     *
     * @param experimentID id
     * @param genotypeID   genotype id
     * @return GenotypeExperiment
     */
    GenotypeExperiment getGenotypeExperimentByExperimentIDAndGenotype(String experimentID, String genotypeID);

    /**
     * Create a new genotype experiment for given experiment and genotype.
     *
     * @param experiment genotype Experiment
     */
    void createGenoteypExperiment(GenotypeExperiment experiment);

    /**
     * Retrieve experiment by id.
     *
     * @param experimentID id
     * @return Experiment
     */
    Experiment getExperimentByID(String experimentID);

    /**
     * Retrieve Genotype by PK.
     *
     * @param genotypeID id
     * @return genotype
     */
    Genotype getGenotypeByID(String genotypeID);

    /**
     * Convenience method to create a genotype experiment from
     * experiment ID and genotype ID
     *
     * @param experimentID id
     * @param genotypeID   id
     * @return genotype Experiment
     */
    GenotypeExperiment createGenoteypExperiment(String experimentID, String genotypeID);

    /**
     * Create a new expression Experiment.
     *
     * @param expressionExperiment expression experiment
     */
    void createExpressionExperiment(ExpressionExperiment expressionExperiment);

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     *
     * @param experiment expression experiment
     */
    void deleteExpressionExperiment(ExpressionExperiment experiment);

    /**
     * Retrieves experiment that pertain to a given
     * publication
     * gene
     * fish
     *
     * @deprecated Use getExperimentsByGeneAndFish2
     *
     * @param publicationID publication
     * @param geneZdbID     gene ID
     * @param fishID        genotype ID
     * @return list of expression experiment
     */
    List<ExpressionExperiment> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID);

    /**
     * Retrieves experiment that pertain to a given
     * publication
     * gene
     * fish
     *
     * @param publicationID publication
     * @param geneZdbID     gene ID
     * @param fishID        genotype ID
     * @return list of expression experiment
     */
    List<ExpressionExperiment> getExperimentsByGeneAndFish2(String publicationID, String geneZdbID, String fishID);

    /**
     * Retrieve an experiment figure stage for given pub, gene and fish.
     *
     * @param publicationID Publication
     * @param geneZdbID     gene
     * @param fishID        fish
     * @param figureID      figure ID
     * @return list of experiment figure stages.
     */
    List<ExperimentFigureStage> getExperimentFigureStagesByGeneAndFish(String publicationID, String geneZdbID, String fishID, String figureID);


    /**
     * Retrieve all experiments for a given publication.
     *
     * @param publicationID pub id
     * @return list of experiments
     */
    List<ExpressionExperiment> getExperiments(String publicationID);

    /**
     * Create a single figure annotation.
     * If no composed term is provided 'Unspecified is used'
     *
     * @param expressionResult experiment figure stage
     * @param singleFigure     Figure
     */
    void createExpressionResult(ExpressionResult expressionResult, Figure singleFigure);

    /**
     * Delete a figure annotation, including all expression result records.
     *
     * @param efs experiment figure stage.
     */
    void deleteFigureAnnotation(ExperimentFigureStage efs);

    /**
     * Retrieve an efs by experiment, figure, start and end stage id.
     *
     * @param experimentZdbID experiment
     * @param figureID        figure
     * @param startStageID    start
     * @param endStageID      end
     * @return efs object
     */
    ExperimentFigureStage getExperimentFigureStage(String experimentZdbID, String figureID, String startStageID, String endStageID);

    /**
     * Retrieve all expression structures for a given publication, which is the same as the
     * structure pile.
     *
     * @param publicationID publication ID
     * @return list of expression structures.
     */
    List<ExpressionStructure> retrieveExpressionStructures(String publicationID);

    /**
     * Retrieve a single expression structure by ID.
     *
     * @param zdbID structure ID
     * @return expression structure
     */
    ExpressionStructure getExpressionStructure(String zdbID);

    /**
     * Delete a structure from the pile.
     *
     * @param structure expression structure
     */
    void deleteExpressionStructure(ExpressionStructure structure);

    /**
     * Delete an expression result record for a given figure.
     * If the result has more than one figure it only removes the figure-result association.
     *
     * @param result expression result.
     * @param figure Figure
     */
    void deleteExpressionResultPerFigure(ExpressionResult result, Figure figure);

    /**
     * Check if a pile structure already exists.
     * check for:
     * suberterm
     * subterm
     * publication ID
     *
     * @param expressedTerm term
     * @param publicationID publication
     * @return boolean
     */
    boolean pileStructureExists(ExpressedTermDTO expressedTerm, String publicationID);

    /**
     * Retrieve a genotype experiment for a given genotype ID.
     * @param zdbID genotype id
     * @return GenotypeExperiment
     */
    GenotypeExperiment getGenotypeExperimentByGenotypeID(String zdbID);

    /**
     * Create all expression structures being used in a given publication.
     *
     * @param publicationID publication id
     */
    void createExpressionPile(String publicationID);

    void createPileStructure(ExpressionStructure structure);

    /**
     * Retrieve Expressions for a given term.
     * @param term term
     * @return list of expressions
     */
    List<ExpressionResult> getExpressionsWithEntity(GenericTerm term);

    /**
     * Retrieve Expressions for a given list of terms.
     * @param terms term
     * @return list of expressions
     */
    List<ExpressionResult> getExpressionsWithEntity(List<GenericTerm> terms);

    /**
     * Retrieve all expression results for a given genotype
     *
     * @param genotype genotype
     * @return list of expression results
     */
    List<ExpressionResult> getExpressionResultsByGenotype (Genotype genotype);

    List<GenericTerm> getWildTypeAnatomyExpressionForMarker(String zdbID);

    /**
     * Query to populate the figure/pub list for an expression summary page
     * @param expressionCriteria entities / boolean that should be in the matching
     * xpatex records
     * @return a list of figures matching the criteria
     */
    List<Figure> getFigures(ExpressionSummaryCriteria expressionCriteria);


    /**
     * Get ExpressionResults matching given criteria
     * @param expressionCriteria criteria to match
     * @return ExpressionStatements
     */
    Set<ExpressionStatement> getExpressionStatements(ExpressionSummaryCriteria expressionCriteria);

    /**
     * Retrieve all expression result objects that annotate with a secondary term.
     * @return list of expression result objects
     */
    List<ExpressionResult> getExpressionOnSecondaryTerms();

    /**
     * Retrieve list of expression result records that use obsoleted terms in the annotation.
     * @return list of expression results records
     */
    List<ExpressionResult> getExpressionOnObsoletedTerms();
    int getImagesFromPubAndClone(PublicationExpressionBean publicationExpressionBean);
    int getImagesForEfg(PublicationExpressionBean publicationExpressionBean);

    StageExpressionPresentation getStageExpressionForMarker(String zdbID);

    List<ExpressedStructurePresentation> getWildTypeExpressionExperiments(String zdbID);

    /**
     * Retrieve all terms that are used in an expression statement.
     * @return set of expressed Terms.
     */
    Set<String> getAllDistinctExpressionTermIDs();

    /**
     * Retrieve all terms that are used in a phenotype statement except pato terms.
     * @return set of expressed Terms.
     */
    Set<String> getAllDistinctPhenotypeTermIDs();

    /**
     * Retrieve expression results for given super term and stage range.
     * @param range
     * @return
     */
    List<ExpressionResult> getExpressionResultsByTermAndStage(TermFigureStageRange range);

    ExpressionResult getExpressionResult(String expressionResultID);

    /**
     * Deletes a given ExpressionResult record and its associations to all figures.
     * @param expressionResult
     */
    void deleteExpressionResult(ExpressionResult expressionResult);

    /**
     * Retrieve list of expression experiment records for a given gene.
     * @return list of expression experiment records
     */
    List<ExpressionExperiment> getExpressionExperimentByGene(Marker gene);

    SortedSet<Experiment> getSequenceTargetingReagentExperiments(SequenceTargetingReagent sequenceTargetingReagent);
}
