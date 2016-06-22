package org.zfin.expression.repository;

import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.expression.presentation.ExpressedStructurePresentation;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.expression.presentation.StageExpressionPresentation;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;

import java.util.List;
import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface ExpressionRepository {

    int getExpressionPubCountForGene(Marker marker);

    int getExpressionPubCountForEfg(Marker marker);

    int getExpressionPubCountForClone(Clone marker);

    Publication getExpressionSinglePub(Marker marker);

    List<Publication> getExpressionPub(Marker marker);

    int getExpressionFigureCountForEfg(Marker marker);

    int getExpressionFigureCountForGene(Marker marker);
    int getExpressionFigureCountForGenotype(Genotype genotype);

    int getExpressionFigureCountForFish(Fish fish);

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
    ExpressionExperiment2 getExpressionExperiment2(String experimentID);

    ExpressionDetailsGenerated getExpressionExperiment2(long id);

    ExpressionDetailsGenerated getExpressionDetailsGenerated(String xpatZdbID, String figZdbID);

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

    FishExperiment getFishExperimentByID(String fishExpID);

    /**
     * Retrieve FishExperiment by Experiment ID
     *
     * @param experimentID id
     * @param fishID   fish id
     * @return FishExperiment
     */
    FishExperiment getFishExperimentByExperimentIDAndFishID(String experimentID, String fishID);

    /**
     * Create a new genotype experiment for given experiment and genotype.
     *
     * @param experiment genotype Experiment
     */
    void createFishExperiment(FishExperiment experiment);

    /**
     * Retrieve experiment by id.
     *
     * @param experimentID id
     * @return Experiment
     */
    Experiment getExperimentByID(String experimentID);
    Experiment getExperimentByPubAndName(String pubID, String experimentName);

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
    FishExperiment createFishExperiment(String experimentID, String genotypeID);

    /**
     * Create a new expression Experiment.
     *
     * @param expressionExperiment expression experiment
     */
    void createExpressionExperiment(ExpressionExperiment2 expressionExperiment);

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     *
     * @param experiment expression experiment
     */
    void deleteExpressionExperiment(ExpressionExperiment2 experiment);

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
    List<ExpressionExperiment2> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID);

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
    List<ExpressionFigureStage> getExperimentFigureStagesByGeneAndFish(String publicationID, String geneZdbID, String fishID, String figureID);


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

    List<ExpressionResult> checkForExpressionResultRecord(ExpressionResult result);
    List<ExpressionResult2> checkForExpressionResultRecord2(ExpressionResult2 result);

    /**
     * Delete a figure annotation, including all expression result records.
     *
     * @param efs experiment figure stage.
     */
    void deleteFigureAnnotation(ExpressionFigureStage efs);

    /**
     * Retrieve an efs by experiment, figure, start and end stage id.
     *
     * @param experimentZdbID experiment
     * @param figureID        figure
     * @param startStageID    start
     * @param endStageID      end
     * @return efs object
     */
    ExpressionFigureStage getExperimentFigureStage(String experimentZdbID, String figureID, String startStageID, String endStageID);

    ExpressionFigureStage getExpressionFigureStage(Long id);

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

    void deleteExpressionStructuresForPub(Publication publication);

    /**
     * Delete an expression result record for a given figure.
     * If the result has more than one figure it only removes the figure-result association.
     *
     * @param result expression result.
     * @param figure Figure
     */
    void deleteExpressionResultPerFigure(ExpressionResult2 result, Figure figure);

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
     * @return FishExperiment
     */
    FishExperiment getGenotypeExperimentByGenotypeID(String zdbID);

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
     * Retrieve all expression results for a given fish
     *
     * @param fish Fish
     * @return list of expression results
     */
    List<ExpressionResult> getExpressionResultsByFish (Fish fish);

    long getExpressionResultsByFishAndPublication(Fish fish, String publicationID);

    List<String> getExpressionFigureIDsByFish(Fish fish);

    List<String> getExpressionPublicationIDsByFish(Fish fish);

    /**
     * Retrieve all expression results for a given Sequenec Targeting Reagent
     *
     * @param sequenceTargetingReagent sequenceTargetingReagent
     * @return list of expression results
     */
    List<ExpressionResult> getExpressionResultsBySequenceTargetingReagent (SequenceTargetingReagent sequenceTargetingReagent);

    List<String> getExpressionFigureIDsBySequenceTargetingReagent (SequenceTargetingReagent sequenceTargetingReagent);

    List<String> getExpressionFigureIDsBySequenceTargetingReagentAndExpressedGene (SequenceTargetingReagent sequenceTargetingReagent, Marker expressedGene);

    List<String> getExpressionPublicationIDsBySequenceTargetingReagent (SequenceTargetingReagent sequenceTargetingReagent);

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
    List<ExpressionResult2> getExpressionOnObsoletedTerms();
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

    ExpressionResult getExpressionResult(Long expressionResultID);

    /**
     * Deletes a given ExpressionResult record and its associations to all figures.
     * @param expressionResult
     */
    void deleteExpressionResult(ExpressionResult2 expressionResult);

    /**
     * Retrieve list of expression experiment records for a given gene.
     * @return list of expression experiment records
     */
    List<ExpressionExperiment> getExpressionExperimentByGene(Marker gene);

    long getExpressionExperimentByFishAndPublication(Fish fish, String publicationID);

    List<ExpressionExperiment2> getExperiments2(String zdbID);

    void createExpressionFigureStage(ExpressionFigureStage experimentFigureStage);

    List<ExpressionResult2> getPhenotypeFromExpressionsByFigureFish(String publicationID, String figureID, String fishID, String featureID);

    /**
     * Retrieve list of expression experiment2 records for a given antibody.
     * @return list of expression experiment2 records
     */
    List<ExpressionExperiment2> getExperiment2sByAntibody(Antibody antibody);

    /**
     * Retrieve all expression experiment2 records for a given fish
     *
     * @param fish Fish
     * @return list of expression experiment2 records
     */
    List<ExpressionExperiment2> getExpressionExperiment2sByFish (Fish fish);

    List<ExpressionResult2> getExpressionResultList(Marker gene);

    List<Experiment> geExperimentByPublication(String publicationID);

    void deleteExperimentCondition(ExperimentCondition condition);

    ExperimentCondition getExperimentCondition(String conditionID);

    void saveExperimentCondition(ExperimentCondition condition);

    void saveExperiment(Experiment experiment);
}
