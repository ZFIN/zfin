package org.zfin.expression.repository;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.Experiment;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Genotype;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface ExpressionRepository {

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
     * @param expressionExperiment expression experiment
     */
    void createExpressionExperiment(ExpressionExperiment expressionExperiment);

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     * @param experiment expression experiment
     */
    void deleteExpressionExperiment(ExpressionExperiment experiment);
}
