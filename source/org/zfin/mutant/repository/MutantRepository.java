package org.zfin.mutant.repository;

import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.presentation.TermHistogramBean;
import org.zfin.publication.Publication;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.MorpholinoSequence;

import java.util.List;
import java.util.Map;
import java.util.Set;


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
     * @param item     Anatomy Item
     * @param wildtype return wildtype genotypes
     * @param bean     Pagination bean info
     * @return list of genotypes
     */
    PaginationResult<Genotype> getGenotypesByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean);

    List<Genotype> getGenotypesByFeature(Feature feature);

    List<GenotypeFeature> getGenotypeFeaturesByGenotype(Genotype genotype);

    List<GenotypeFeature> getGenotypeFeaturesByGenotype(String genotypeID);

    /**
     * Retrieve the number of images associated to a mutant marker and a given
     * anatomy structure.
     *
     * @param item     Anatomy Term
     * @param genotype Genotype
     * @return number
     */
    int getNumberOfImagesPerAnatomyAndMutant(GenericTerm item, Genotype genotype);

    int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(GenericTerm item, Genotype genotype);


    /**
     * Retrieve all genotypes that have a phenotype annotation for a given
     * anatomical structure. Gene expressions are not included in this list.
     *
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return list of statistics
     */
    List<SequenceTargetingReagent> getPhenotypeMorpholinos(GenericTerm item, int numberOfRecords);

    /**
     * Retrieve a genotype,feature and marker object by PK.
     *
     * @param genotypeZbID pk
     * @return genotype
     */
    Genotype getGenotypeByID(String genotypeZbID);


    /**
     * Retrieve a genotype object by handle
     *
     * @param genotypeHandle handle
     * @return genotype
     */
    Genotype getGenotypeByHandle(String genotypeHandle);
    Genotype getGenotypeByName(String genotypeName);


    /*List<Marker> getDeletedMarker(Feature feat);

    List<String> getDeletedMarkerLG(Feature feat);*/

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
    PaginationResult<GenotypeExperiment> getGenotypeExperimentSequenceTargetingReagents(GenericTerm item, Boolean isWildtype, int numberOfRecords);

    /**
     * Retrieve all genotype objects that are associated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    List<GenotypeExperiment> getGenotypeExperimentSequenceTargetingReagents(GenericTerm item, Boolean isWildtype);

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
    PaginationResult<GenotypeExperiment> getGenotypeExperimentSequenceTargetingReagents(GenericTerm item, Boolean isWildtype, PaginationBean bean);

    /**
     * @param name name of quality term
     * @return A list of GoTerms that contain the parameter handed in.
     */
    List<GenericTerm> getQualityTermsByName(String name);


    /**
     * Check if for a given figure annotation a pato record (Phenotype)
     *
     * @param genotypeExperimentID expression experiment
     * @param figureID             figure
     * @param startID              start   stage
     * @param endID                end     stage
     * @param publicationID        publication
     * @return boolean
     */
    boolean isPatoExists(String genotypeExperimentID, String figureID, String startID, String endID, String publicationID);

    /**
     * Lookup a term by name. Term must not be obsolete.
     *
     * @param termName term name
     * @return Term object
     */
    GenericTerm getQualityTermByName(String termName);

    /**
     * Retrieve a genotype experiment by PK.
     *
     * @param genotypeExperimentID pk
     * @return genotype experiment
     */
    GenotypeExperiment getGenotypeExperiment(String genotypeExperimentID);

    /**
     * Retrieve a genotype experiment by the natural key, geno & exp zdb_ids
     *
     * @param genotypeZdbID   ak
     * @param experimentZdbID ak
     * @return genox
     */
    GenotypeExperiment getGenotypeExperiment(String genotypeZdbID, String experimentZdbID);

    /**
     * Remove a PhenotypeExperiment record:
     * 1) All phenotype statements
     * 2) the genotype experiment if unused any more
     *
     * @param phenotypeExperiment PhenotypeExperiment
     */
    void deletePhenotypeExperiment(PhenotypeExperiment phenotypeExperiment);

    List<Genotype> getGenotypesForStandardAttribution(Publication publication);

    List<GenericTerm> getGoTermsByMarkerAndPublication(Marker marker, Publication publication);

    List<GenericTerm> getGoTermsByPhenotypeAndPublication(Publication publication);

    InferenceGroupMember addInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inferenceToAdd);

    void removeInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inference);


    List<Genotype> getGenotypesForAttribution(String publicationZdbID);

    FeatureAlias getSpecificDataAlias(Feature feature, String alias);

    FeatureDBLink getSpecificDBLink(Feature feature, String sequence);

    int getZFINInferences(String zdbID, String zdbID1);

    int getNumberMarkerGoTermEvidences(MarkerGoTermEvidence markerGoTermEvidence);

    List<MorpholinoSequence> getMorpholinosWithMarkerRelationships();

    /**
     * Retrieve all distinct marker go evidence objects for a given term.
     *
     * @param term term
     * @return list of marker go
     */
    List<MarkerGoTermEvidence> getMarkerGoEvidence(GenericTerm term);

    /**
     * Retrieve phenotypes that have an annotation to a given term
     * with tag=abnormal and the term either in super or sub position
     *
     * @param term Term
     * @return list of phenotypes
     */
    List<PhenotypeStatement> getPhenotypeWithEntity(GenericTerm term);

    List<PhenotypeStatement> getPhenotypeWithEntity(List<GenericTerm> terms);

    List<MarkerGoTermEvidence> getMarkerGoEvidence(List<GenericTerm> terms);

    List<GenotypeFigure> getCleanGenoFigsByGenotype(Genotype genotype);

    PhenotypeExperiment getPhenotypeExperiment(Long id);

    PhenotypeStatement getPhenotypeStatementById(Long Id);

    /**
     * Retrieve the phenotypes that are annotated with obsoleted terms.
     *
     * @return list of phenotypes
     */
    List<PhenotypeStatement> getPhenotypesOnObsoletedTerms();

    /**
     * Returns a list phenotype statements that are related to
     * a given genotype.
     *
     * @param genotype Genotype
     * @return list of phenotype statement objects
     */
    List<PhenotypeStatement> getPhenotypeStatementsByGenotype(Genotype genotype);

    void runFeatureNameFastSearchUpdate(Feature feature);

    /**
     * Returns list of phenotype statements that are annotated with a term marked secondary.
     *
     * @return list of PhenotypeStatement statements.
     */
    List<PhenotypeStatement> getPhenotypesOnSecondaryTerms();

    /**
     * Returns list of MarkerGoTermEvidence statements that are annotated with a term marked obsolete.
     *
     * @return list of MarkerGoTermEvidence statements.
     */
    List<MarkerGoTermEvidence> getGoEvidenceOnObsoletedTerms();

    /**
     * Retrieve a histogram of phenotype terms usage.
     *
     * @return list of histograms
     */
    Map<TermHistogramBean, Long> getTermPhenotypeUsage();

    List<Feature> getAllelesForMarker(String zdbID, String type);

    List<Marker> getKnockdownReagents(Marker gene);

    List<String> getTransgenicLines(Marker construct);

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param genotypeExperimentIDs genox ids
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getPhenotypeStatementsByGenotypeExperiments(List<String> genotypeExperimentIDs);

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param genotypeExperimentIDs genox ids
     * @return list of expression statements
     */
    List<ExpressionStatement> getExpressionStatementsByGenotypeExperiments(List<String> genotypeExperimentIDs);

    /**
     * Retrieve citation list of pub ids
     *
     * @param genotypeExperimentIDs
     * @return
     */
    Set<String> getGenoxAttributions(List<String> genotypeExperimentIDs);

    /**
     * Retrieve citation list of pubs for fish annotations.
     *
     * @param genotypeExperimentIDs
     * @return
     */
    List<Publication> getFishAttributionList(List<String> genotypeExperimentIDs);

    /**
     * Retrieve Morpholinos by mo Ids
     *
     * @param moIds MO ids
     * @return list of MOs
     */
    SequenceTargetingReagent getMorpholinosById(String moIds);

    /**
     * Retrieve all wildtype genotypes.
     *
     * @return
     */
    List<Genotype> getAllWildtypeGenotypes();

    /**
     * Retrieve a list of expression result records that show expression data for a given fish
     *
     * @return list of expression results
     */
    List<ExpressionResult> getExpressionSummary(String genotypeID, List<String> genoxIds, String geneID);

    List<ExpressionResult> getConstructExpressionSummary(List<String> genoxIds);


    /**
     * Check if a given fish has expression data with at least one figure that has an image.
     *
     * @return
     */
    boolean hasImagesOnExpressionFigures(String genotypeID, List<String> genoxIds);

    /**
     * Retrieve figures for phenotypes for a given genotype and structure.
     *
     * @param term                 structure
     * @param genotype             genotype
     * @param includeSubstructures true or false
     * @return list of figures
     */
    List<Figure> getPhenotypeFigures(GenericTerm term, Genotype genotype, boolean includeSubstructures);

    /**
     * Retrieve phenotype statements for given structure and genotype.
     *
     * @param term
     * @param genotype
     * @param includeSubstructures
     * @return
     */
    List<PhenotypeStatement> getPhenotypeStatement(GenericTerm term, Genotype genotype, boolean includeSubstructures);

    List<PhenotypeStatement> getPhenotypeStatementForMutantSummary(GenericTerm term, Genotype genotype, boolean includeSubstructures);

    /**
     * Retrieve phenotype statements for given structure and genotype.
     *
     * @param genotype
     * @param includeSubstructures
     * @return
     */
    List<PhenotypeStatement> getPhenotypeStatement(Genotype genotype, boolean includeSubstructures);

    PaginationResult<Genotype> getGenotypesByAnatomyTermIncludingSubstructures(GenericTerm item, boolean wildtype, PaginationBean bean);

    List<Genotype> getGenotypes(List<String> genotypeExperimentIDs);

    /**
     * Retrieve list of phenotype statements that use obsoleted terms for given ontology.
     *
     * @param ontology ontology
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getPhenotypesOnObsoletedTerms(Ontology ontology);

    /**
     * Retrieve phenotypes for a given marker
     *
     * @param gene
     * @return
     */
    List<PhenotypeStatement> getPhenotypeStatementsByMarker(Marker gene);


    List<GenotypeFigure> getGenotypeFiguresBySTR(SequenceTargetingReagent str);

}

