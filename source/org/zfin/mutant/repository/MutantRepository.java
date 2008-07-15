package org.zfin.mutant.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.CachedRepository;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.*;
import org.zfin.ontology.GoTerm;

import java.util.List;

/**
 * This is the interface that provides access to the persistence layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public interface MutantRepository extends CachedRepository {

    /**
     * This returns a list genotypes (mutants) that are annotated
     * to a given anatomy item.
     *
     * @param item            Anatomy Item
     * @param wildtype return wildtype genotypes
     * @param numberOfRecords @return A list of Genotype objects.
     * @return list of genotypes
     */
    PaginationResult<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords);


    /**
     * Retrieve the number of images associated to a mutant marker and a given
     * anatomy structure.
     *
     * @param item     Anatomy Item
     * @param genotype Genotype
     * @return number
     */
    int getNumberOfImagesPerAnatomyAndMutant(AnatomyItem item, Genotype genotype);

    int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(AnatomyItem item, Genotype genotype);

    /**
     * Retrieve all genotypes that have a phenotype annotation for a given
     * anatomical structure. Gene expressions are not included in this list.
     *
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return list of statistics
     */
    List<Morpholino> getPhenotypeMorhpolinosByAnatomy(AnatomyItem item, int numberOfRecords);

    /**
     * Retrieve number of morpholinos that show a gene expression in a given structure.
     *
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return int number of morpholinos
     */
    int getMorhpolinoCountByAnatomy(AnatomyItem item, int numberOfRecords);

    /**
     * Retrieve a genotype object by PK.
     * @param genoteypZbID pk
     * @return genotype
     */
    Genotype getGenotypeByID(String genoteypZbID);

    /**
     * Retrieve the genotype objects that are assoicated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.

     * @param item anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, boolean isWildtype);


    /**
     * Retrieve the genotype objects that are assoicated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.

     * @param item anatomy structure
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item);



    /**
     * Retrieve the list of morpholinos for a given genotype.
     * @param genotype Genotype
     * @param item Anatomy Structure
     * @param isWildtype genotype is wild type or not
     * @return list of morpholinos
     */
    List<Morpholino> getMorpholinosByGenotype(Genotype genotype, AnatomyItem item, boolean isWildtype);


    /**
     * Method to set pagination-related parameters.
     * @param paginationBean Pagination Bean
     */
    void setPaginationParameters(PaginationBean paginationBean);

    /**
     *
     * @param name
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<GoTerm> getGoTermsByName(String name) ;

    /**
     *
     * @param name
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<Term> getQualityTermsByName(String name) ;



    /**
     *
     * @param name
     * @return A list of Features that contain the name in the abbreviation.
     */
    List<Feature> getFeaturesByAbbreviation(String name) ;
}

