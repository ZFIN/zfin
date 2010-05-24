package org.zfin.mutant.repository;

import org.zfin.expression.Figure;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.mutant.MutantFigureStage;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeStructure;

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
     * @param phenotypeTerm phenotype structure
     * @param publicationID Publication
     * @return boolean
     */
    boolean isPhenotypePileStructureExists(PhenotypeTermDTO phenotypeTerm, String publicationID);

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
    List<MutantFigureStage> getMutantExpressionsByFigureFish(String publicationID, String figureID, String genotypeID, String featureID);

    /**
     * Retrieve Mutant Figure Stage record
     *
     * @param genotypeID    genotype
     * @param figureID      figure
     * @param startID       start stage
     * @param endID         end stage
     * @param publicationID Publication
     * @return MutantFigureStage
     */
    MutantFigureStage getMutant(String genotypeID, String figureID, String startID, String endID, String publicationID);

    /**
     * Retrieve a mutant figure stage record from the unique key
     * @param mutantFigureStage mutant figure stage unique key
     * @param figureID figure
     * @return full mutant figure stage record
     */
    MutantFigureStage getMutant(MutantFigureStage mutantFigureStage, String figureID);

    /**
     * Retrieve a pile phenotype structure
     *
     * @param pileStructureID primary key
     * @return PhenotypeStructure
     */
    public PhenotypeStructure getPhenotypePileStructure(String pileStructureID);

    /**
     * Create a new phenotype record.
     *
     * @param phenotype Phenotype
     * @param figure    Figure
     */
    void createPhenotype(Phenotype phenotype, Figure figure);

    /**
     * Run database script to regenerate fast search tables.
     *
     * @param phenotype Phenotype
     */
    public void runRegenGenotypeFigureScript(Phenotype phenotype);

    /**
     * Remove a phenotype associated to a given figure.
     * If the phenotype is associated to more than one figure then only
     * the association is removed. Otherwise, the phenotype record is deleted as well.
     *
     * @param phenotype phenotype
     * @param figure    Figure
     */
    void deletePhenotype(Phenotype phenotype, Figure figure);

    /**
     * Create a default phenotype record for a given mutant figure stage.
     *
     * @param mfs MutantFigureStage
     * @return Phenotype
     */
    Phenotype createDefaultPhenotype(MutantFigureStage mfs);

    /**
     * Create a default phenotype record.
     * Default:
     * AO term: unspecified
     * Quality term: quality
     * Tag: normal
     *
     * @param phenotype Phenotype
     * @return default phenotype
     */
    Phenotype createDefaultPhenotype(Phenotype phenotype);


    /**
     * Retrieve a list of phenotypes used for a given publication.
     *
     * @param publicationID publication
     * @return set of phenotypes
     */
    List<Phenotype> getAllPhenotypes(String publicationID);

    /**
     * Create the Phenotype pile structure pile if it does not already exist.
     * After closing a publication the structure pile is removed.
     *
     * @param publicationID publication
     */
    void createPhenotypePile(String publicationID);
}
