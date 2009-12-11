package org.zfin.mutant.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.CachedRepository;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.presentation.client.Ontology;
import org.zfin.mutant.*;
import org.zfin.ontology.GoTerm;
import org.zfin.marker.Marker;

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
     * @param wildtype        return wildtype genotypes
     * @param numberOfRecords @return A list of Genotype objects.
     * @return list of genotypes
     */
    PaginationResult<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords);

    List<Genotype> getGenotypesByFeature(Feature feature);


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
     * Retrieve a genotype,feature and marker object by PK.
     *
     * @param genotypeZbID pk
     * @return genotype
     */
    Genotype getGenotypeByID(String genotypeZbID);
    Feature getFeatureByID(String featureZdbID);
    Marker  getMarkerbyFeature(Feature feature);

    /**
     * Retrieve a genotype object by handle
     * @param genotypeHandle handle 
     * @return genotype
     */
    Genotype getGenotypeByHandle(String genotypeHandle);




    List<Marker> getDeletedMarker(Feature feat);
    List<String> getDeletedMarkerLG(Feature feat);


    /**
     * Retrieve the genotype objects that are assoicated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item            anatomy structure
     * @param isWildtype      wildtype of genotype
     * @param numberOfRecords defines the first n records to retrieve
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, Boolean isWildtype, int numberOfRecords);

    /**
     * Retrieve all genotype objects that are assoicated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    List<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, boolean isWildtype);

    /**
     * Retrieve genotype objects that are assoicated to a morpholino within the range specified
     * in the pagination bean object.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @param bean       PaginationBean
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, Boolean isWildtype, PaginationBean bean);

    /**
     * Retrieve the list of morpholinos for a given genotype.
     *
     * @param genotype   Genotype
     * @param item       Anatomy Structure
     * @param isWildtype genotype is wild type or not
     * @return list of morpholinos
     */
    List<Morpholino> getMorpholinosByGenotype(Genotype genotype, AnatomyItem item, boolean isWildtype);


    /**
     * @param name go term name
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<GoTerm> getGoTermsByName(String name);

    /**
     * @param name go term name
     * @param ontology subset of GO ontology
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<GoTerm> getGoTermsByNameAndSubtree(String name, Ontology ontology);

    /**
     * @param name go term name
     * @return A unique GoTerm.
     */
    GoTerm getGoTermByName(String name);

    /**
     * @param name name of quality term
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<Term> getQualityTermsByName(String name);


    /**
     * @param name name of feature
     * @return A list of Features that contain the name in the abbreviation.
     */
    List<Feature> getFeaturesByAbbreviation(String name);

    /**
     * Retrieve all distinct wild-type genotypes.
     *
     * @return list of wildtype fish
     */
    List<Genotype> getAllWildtypeGenotypes();

    /**
     * Check if for a given figure annotation a pato record (Phenotype)
     *
     * @param genotypeExperimentID expression experiment
     * @param figureID             figure
     * @param startID              start   stage
     * @param endID                end     stage
     * @return boolean
     */
    boolean isPatoExists(String genotypeExperimentID, String figureID, String startID, String endID);

    /**
     * Create a default phenotype record.
     * Default:
     * AO term: unspecified
     * Quality term: quality
     * Tag: normal
     *
     * @param pheno AnatomyPhenotype
     */
    void createDefaultPhenotype(Phenotype pheno);

    /**
     * Lookup a term by name. Term must not be obsolete.
     * @param termName term name
     * @return Term object
     */
    Term getQualityTermByName(String termName);
}

