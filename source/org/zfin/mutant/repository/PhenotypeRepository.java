package org.zfin.mutant.repository;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.PublicationLink;

import java.util.List;

/**
 * Class defines methods to retrieve phenotypic data for annotation purposes
 */
public interface PhenotypeRepository {

    /**
     * Retrieve all structures on the structure pile except 'unspecified'
     *
     * @param publicationID Publication ID
     * @return list of annotations term dtos
     */
    public List<PhenotypeStructure> retrievePhenotypeStructures(String publicationID);


    /**
     * Create a new phenotype structure on the pile.
     *
     * @param structure     Phenotype Structure
     * @param publicationID publication
     */
    void createPhenotypeStructure(PhenotypeStructure structure, String publicationID);

    /**
     * Check if a phenotype structure is already on the pile.
     *
     * @param phenotypeStructure PhenotypeStructure
     * @param publicationID      Publication
     * @return boolean true or false
     */
    boolean isPhenotypePileStructureExists(PhenotypeStructure phenotypeStructure, String publicationID);

    /**
     * Check if a given Phenotype Structure already exists on the pile
     *
     * @param structure Phenotype structure
     * @return boolean
     */
    public boolean isPhenotypeStructureOnPile(PhenotypeStructure structure);

    /**
     * Retrieve a phenotype structure by given primary key.
     *
     * @param zdbID primary key
     * @return phenotype structure
     */
    PhenotypeStructure getPhenotypeStructureByID(String zdbID);

    /**
     * Delete a phenotype structure object given by its primary key.
     *
     * @param zdbID primary key
     */
    void deletePhenotypeStructureById(String zdbID);

    /**
     * Retrieve all phenotypes for a given publication filtered by figure and fish.
     *
     * @param publicationID publication
     * @param figureID      figure
     * @param genotypeID    genotype
     * @param featureID     feature
     * @return list of phenotype experiment figure stage records
     */
    List<PhenotypeExperiment> getMutantExpressionsByFigureFish(String publicationID, String figureID, String genotypeID, String featureID);

    /**
     * Retrieve phenotype experiment record from the unique key
     *
     * @param phenotypeExperimentFilter phenotype experiment:
     *                                  unique figure, stage, genotype, environment
     * @return full mutant figure stage record
     */
    PhenotypeExperiment getPhenotypeExperiment(PhenotypeExperiment phenotypeExperimentFilter);

    /**
     * Retrieve a pile phenotype structure
     *
     * @param pileStructureID primary key
     * @return PhenotypeStructure
     */
    public PhenotypeStructure getPhenotypePileStructure(String pileStructureID);

    /**
     * Run database script to regenerate fast search tables.
     *
     * @param phenotype PhenotypeStatement
     */
    public void runRegenGenotypeFigureScript(PhenotypeExperiment phenotype);

    /**
     * Retrieve a list of phenotypes used for a given publication.
     *
     * @param publicationID publication
     * @return set of phenotypes
     */
    List<PhenotypeExperiment> getAllPhenotypes(String publicationID);

    /**
     * Create the Phenotype pile structure pile if it does not already exist.
     * After closing a publication the structure pile is removed.
     *
     * @param publicationID publication
     */
    void createPhenotypePile(String publicationID);

    /**
     * Create a new phenotype statement.
     *
     * @param phenoStatement PhenotypeStatement
     */
    void createPhenotypeStatement(PhenotypeStatement phenoStatement);

    /**
     * Delete an existing phenotype statement.
     *
     * @param phenoStatement PhenotypeStatement
     */
    void deletePhenotypeStatement(PhenotypeStatement phenoStatement);

    /**
     * Create a new phenotype experiment record.
     *
     * @param phenoExperiment
     */
    void createPhenotypeExperiment(PhenotypeExperiment phenoExperiment);

    /**
     * Retrieve phenotype experiment by id.
     *
     * @param id PK
     * @return Phenotype experiment
     */
    PhenotypeExperiment getPhenotypeExperiment(long id);

    /**
     * Retrieve a list of phenotype experiment objects that do not have a phenotype statements.
     *
     * @param publicationID pub id
     * @return list of phenotype experiments
     */
    List<PhenotypeExperiment> getPhenotypeExperimentsWithoutAnnotation(String publicationID);

    /**
     * Retrieve all phenotype experiments that have been created in the last n days.
     *
     * @param days in the last days
     * @return list of phenotype experiments
     */
    List<PhenotypeExperiment> getLatestPhenotypeExperiments(int days);

    /**
     * Retrieve all phenotype statements that have been created in the last n days.
     *
     * @param experimentID phenotype Experiment ID
     * @param days         in the last days
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getLatestPhenotypeStatements(int experimentID, int days);

    int getNumPhenotypeFigures(Marker gene);

    int getNumPhenotypePublications(Marker gene);

    List<PostComposedPresentationBean> getPhenotypeAnatomy(Marker gene);

    PublicationLink getPhenotypeFirstPublication(Marker gene);

    FigureLink getPhenotypeFirstFigure(Marker gene);

    /**
     * Retrieve all phenotype statements for a given figure.
     *
     * @param figure figure
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getPhenotypeStatements(Figure figure);

    /**
     * Retrieve all phenotypes for a given genotype experiment.
     *
     * @param genoxID genotype experiment id
     * @return list of phenotypes
     */
    List<PhenotypeStatement> getPhenotypeStatements(String genoxID);

    /**
     * Retrieve list of phenotype figures associated with a genotype
     *
     * @param genotypeID genotype zdbID
     * @return list of figures
     */
    List<Figure> getPhenoFiguresByGenotype(String genotypeID);

    /**
     * Retrieve phenotype statement for a given figure and fish.
     * @param figure figure
     * @param fishID fish ID
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getPhenotypeStatements(Figure figure, String fishID);
}
