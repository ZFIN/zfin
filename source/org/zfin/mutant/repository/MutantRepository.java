package org.zfin.mutant.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.CachedRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;

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
    List<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords);

    /**
     * Retrieve the number of mutants that are annotated to
     * a given anatomy item.
     *
     * @param zdbID           anatomy item id
     * @param wildtype boolean
     * @return number of mutants
     */
    int getNumberOfMutants(String zdbID, boolean wildtype);

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
     * Retrieve all morpholinos that have a phenotype annotation for a given
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
}

