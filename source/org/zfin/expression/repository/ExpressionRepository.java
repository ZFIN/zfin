package org.zfin.expression.repository;

import org.zfin.expression.*;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.marker.Clone;
import org.zfin.marker.Gene;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface ExpressionRepository {
    ExpressionStageAnatomyContainer getExpressionStages(Gene gene);

    int getExpressionPubCount(Marker marker);

    int getExpressionFigureCount(Marker marker);

    /**
     * @param marker
     * @return List of Object[int figureCount,String pubZdbID,String cloneZdbID)
     */
    List getDirectlySubmittedExpressionSummaries(Marker marker);

    int getImagesFromPubAndClone(Publication publication, Clone clone);

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
     * @param publicationID publication
     * @param geneZdbID     gene ID
     * @param fishID        genotype ID
     * @return list of expression experiment
     */
    List<ExpressionExperiment> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID);

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
     * Retrieve a phenotype based on a given phenotype with
     * genox, start, end, pub, superterm = 'unspecified'
     *
     * @param pheno phenotype
     * @return phenotype
     */
    Phenotype getUnspecifiedPhenotypeFromGenoxStagePub(Phenotype pheno);

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
    List<ExpressionResult> getExpressionsWithEntity(Term term);

    /**
     * Retrieve Expressions for a given list of terms.
     * @param terms term
     * @return list of expressions
     */
    List<ExpressionResult> getExpressionsWithEntity(List<Term> terms);

    /**
     * Retrieve all expression results for a given genotype
     *
     * @param genotype genotype
     * @return list of expression results
     */
    List<ExpressionResult> getExpressionResultsByGenotype (Genotype genotype);
}
