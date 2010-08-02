package org.zfin.mutant.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerSequenceMarker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.sequence.MorpholinoSequence;

import java.util.List;
import java.util.TreeSet;


/**
 * This is the interface that provides access to the persistence layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public interface MutantRepository {

    /**
     * This returns a list genotypes (mutants) that are annotated
     * to a given anatomy item.
     *
     * @param item            Anatomy Item
     * @param wildtype        return wildtype genotypes
     * @param numberOfRecords @return A list of Genotype objects.
     * @return list of genotypes
     */
    PaginationResult<Genotype> getGenotypesByAnatomyTerm(Term item, boolean wildtype, int numberOfRecords);

    List<Genotype> getGenotypesByFeature(Feature feature);


    /**
     * Retrieve the number of images associated to a mutant marker and a given
     * anatomy structure.
     *
     * @param item     Anatomy Term
     * @param genotype Genotype
     * @return number
     */
    int getNumberOfImagesPerAnatomyAndMutant(Term item, Genotype genotype);

    int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(Term item, Genotype genotype);

    /**
     * Retrieve all genotypes that have a phenotype annotation for a given
     * anatomical structure. Gene expressions are not included in this list.
     *
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return list of statistics
     */
    List<Morpholino> getPhenotypeMorpholinos(Term item, int numberOfRecords);

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

    List<Marker> getMarkerbyFeature(Feature feature);

    List<Marker> getMarkerPresent(Feature feature);

    /**
     * Retrieve a genotype object by handle
     *
     * @param genotypeHandle handle
     * @return genotype
     */
    Genotype getGenotypeByHandle(String genotypeHandle);


    /*List<Marker> getDeletedMarker(Feature feat);

    List<String> getDeletedMarkerLG(Feature feat);*/

    TreeSet<String> getFeatureLG(Feature feat);
    /*List<String> getMappedFeatureLG(Feature feat);
    List<String> getLinkageFeatureLG(Feature feat);*/


    /**
     * Retrieve the genotype objects that are associated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item            anatomy structure
     * @param isWildtype      wildtype of genotype
     * @param numberOfRecords defines the first n records to retrieve
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, Boolean isWildtype, int numberOfRecords);

    /**
     * Retrieve all genotype objects that are associated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    List<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, boolean isWildtype);

    /**
     * Retrieve genotype objects that are associated to a morpholino within the range specified
     * in the pagination bean object.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @param bean       PaginationBean
     * @return list of genotype object
     */
    PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, Boolean isWildtype, PaginationBean bean);

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
     * @param name name of quality term
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<GenericTerm> getQualityTermsByName(String name);


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
     * Lookup a term by name. Term must not be obsolete.
     *
     * @param termName term name
     * @return Term object
     */
    GenericTerm getQualityTermByName(String termName);

    /**
     * Retrieve the default phenotype for a given figure and genotype experiment.
     * If it does not exist, it return null.
     *
     * @param genotypeExperiment genotype Experiment
     * @param figureID           figure id
     * @return phenotype
     */
    Phenotype getDefaultPhenotype(GenotypeExperiment genotypeExperiment, String figureID);

    /**
     * Retrieve a genotype experiment by PK.
     *
     * @param genotypeExperimentID pk
     * @return genotype experiment
     */
    GenotypeExperiment getGenotypeExperiment(String genotypeExperimentID);

    /**
     * Remove a mutant figure stage record:
     * 1) All phenotypes and their association to figures.
     * 2) the genotype experiment if unused any more
     *
     * @param mutant Mutants
     */
    void deleteMutantFigureStage(MutantFigureStage mutant);

    List<Feature> getFeaturesForStandardAttribution(Publication publication);

    List<Genotype> getGenotypesForStandardAttribution(Publication publication);

    List<Term> getGoTermsByMarkerAndPublication(Marker marker, Publication publication);

    List<Term> getGoTermsByPhenotypeAndPublication(Publication publication);

    InferenceGroupMember addInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inferenceToAdd);

    void removeInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inference);

    List<Feature> getFeaturesForAttribution(String publicationZdbID);

    List<Genotype> getGenotypesForAttribution(String publicationZdbID);

    Feature getFeatureByAbbreviation(String featureAbbrev);

    int getZFINInferences(String zdbID, String zdbID1);

    int getNumberMarkerGoTermEvidences(MarkerGoTermEvidence markerGoTermEvidence);

    List<MorpholinoSequence> getMorpholinosWithMarkerRelationships();

    /**
     * Retrieve phenotypes that have an annotation to a given term
     * with tag=abnormal and the term either in super or sub position
     *
     * @param term Term
     * @return list of phenotypes
     */
    List<Phenotype> getPhenotypeWithEntity(Term term);

    /**
     * Retrieve all distinct marker go evidence objects for a given term.
     *
     * @param term term
     * @return list of marker go
     */
    List<MarkerGoTermEvidence> getMarkerGoEvidence(Term term);

    List<Phenotype> getPhenotypeWithEntity(List<Term> terms);

    List<MarkerGoTermEvidence> getMarkerGoEvidence(List<Term> terms);
}

