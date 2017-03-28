package org.zfin.mutant.repository;

import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
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
import org.zfin.sequence.STRMarkerSequence;

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
    PaginationResult<Fish> getFishByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean);

    /**
     * This returns a list genotypes (mutants) that are annotated
     * to a given anatomy item.
     *
     * @param item     Anatomy Item
     * @param wildtype return wildtype genotypes
     * @param bean     Pagination bean info
     * @return list of genotypes
     */
    PaginationResult<Fish> getDirtyFishByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean);

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
    FishExperiment getGenotypeExperiment(String genotypeExperimentID);

    /**
     * Retrieve a genotype experiment by the natural key, geno & exp zdb_ids
     *
     * @param genotypeZdbID   ak
     * @param experimentZdbID ak
     * @return genox
     */
    FishExperiment getGenotypeExperiment(String genotypeZdbID, String experimentZdbID);

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

    List<STRMarkerSequence> getSequenceTargetingReagentsWithMarkerRelationships();

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
    List<PhenotypeStatementWarehouse> getPhenotypeWithEntity(GenericTerm term);

    List<PhenotypeStatementWarehouse> getPhenotypeWithEntity(List<GenericTerm> terms);

    List<MarkerGoTermEvidence> getMarkerGoEvidence(List<GenericTerm> terms);

    PhenotypeExperiment getPhenotypeExperiment(Long id);

    PhenotypeStatement getPhenotypeStatementById(Long Id);

    PhenotypeStatementWarehouse getPhenotypeStatementWarehouseById(Long id);

    PhenotypeWarehouse getPhenotypeWarehouseById(Long id);

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
    List<PhenotypeStatementWarehouse> getPhenotypeStatementsByGenotype(Genotype genotype);

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

    /**
     * Retrieve transgenocLines(genotypes) by construct
     *
     * @param construct
     * @return list of genotypes
     */
    List<Genotype> getTransgenicLinesForConstruct(Marker construct);

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param genotypeExperimentIDs genox ids
     * @return list of phenotype statements
     */
    List<PhenotypeStatement> getPhenotypeStatementsByGenotypeExperiments(List<String> genotypeExperimentIDs);

    List<PhenotypeStatement> getPhenotypeStatementsByFish(Fish fish);

    List<PhenotypeStatementWarehouse> getPhenotypeStatementWarehousesByFish(Fish fish);

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param genotypeExperimentIDs genox ids
     * @return list of expression statements
     */
    List<ExpressionStatement> getExpressionStatementsByGenotypeExperiments(Set<FishExperiment> genotypeExperimentIDs);

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
     * @param fish
     * @return
     */
    List<Publication> getFishAttributionList(Fish fish);

    /**
     * Retrieve sequence targeting reagent by its id
     *
     * @param sequenceTargetingReagentID sequence targeting reagent by its id
     * @return SequenceTargetingReagent object
     */
    SequenceTargetingReagent getSequenceTargetingReagentByID(String sequenceTargetingReagentID);

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
    List<ExpressionResult> getExpressionSummary(Set<FishExperiment> fishOx, String geneID);

    List<ExpressionResult> getConstructExpressionSummary(List<String> genoxIds);


    /**
     * Check if a given fish has expression data with at least one figure that has an image.
     *
     * @return
     */
    boolean hasImagesOnExpressionFigures(String genotypeID, Set<FishExperiment> fishOx);

    List<PhenotypeStatementWarehouse> getPhenotypeStatementForMutantSummary(GenericTerm term, Genotype genotype, boolean includeSubstructures);

    PaginationResult<Fish> getFishByAnatomyTermIncludingSubstructures(GenericTerm item, boolean wildtype, PaginationBean bean);

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

    /**
     * Retrieve list of STRs that are attributed to a pub
     *
     * @param publicationID
     * @return
     */
    List<SequenceTargetingReagent> getStrList(String publicationID);

    boolean createFish(Fish fish, Publication publication);

    /**
     * Retrieve all fish attributed to a given pub
     *
     * @param publicationID
     * @return
     */
    List<Fish> getFishList(String publicationID);

    /**
     * Retrieve Fish by ID
     *
     * @param fishID
     * @return
     */
    Fish getFish(String fishID);

    /**
     * Check if a fish is already in the database.
     *
     * @param fish
     * @return
     */
    Fish getFishByGenoStr(Fish fish);

    /**
     * create a new disease model
     *
     * @param diseaseAnnotation
     */
    void createDiseaseModel(DiseaseAnnotation diseaseAnnotation);

    /**
     * Retrieve publications that refer to a given fish
     *
     * @param zdbID
     * @return
     */
    List<Publication> getPublicationWithFish(String zdbID);

    /**
     * Retrieve fish model by composite index: (fish, experiment)
     *
     * @param fishID
     * @param expID
     * @return
     */
    FishExperiment getFishModel(String fishID, String expID);

    /**
     * Retrieve Disease Model by composite index: (disease term, pubID,Evidence code)
     *
     * @param diseaseAnnotation
     * @return
     */
    DiseaseAnnotation getDiseaseModel(DiseaseAnnotation diseaseAnnotation);

    /**
     * Retrieve disease model by ID
     *
     * @param zdbID
     * @return
     */
    DiseaseAnnotation getDiseaseModelByID(String zdbID);

    /**
     * Remove a disease model
     *
     * @param diseaseAnnotation
     */
    void deleteDiseaseModel(DiseaseAnnotation diseaseAnnotation);
    /**
     * Retrieve disease model by ID
     *
     * @param id
     * @return
     */
    DiseaseAnnotationModel getDiseaseAnnotationModelByID(Long id);
    List<DiseaseAnnotationModel> getDiseaseAnnotationModelByZdb(String zdbID);

    /**
     * Remove a disease model
     *
     * @param diseaseAnnotationModel
     */
    void deleteDiseaseAnnotationModel(DiseaseAnnotationModel diseaseAnnotationModel);

    /**
     * Retrieve disease / fish model by fish ID and pub.
     *
     * @param fishID fish id
     * @param pubID  pubID
     * @return
     */
    List<DiseaseAnnotation> getDiseaseModel(String fishID, String pubID);

    /**
     * Retrieve all fish with a given SequenceTargetingReagent
     *
     * @param sequenceTargetingReagent
     * @return
     */
    List<Fish> getFishListBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent);

    List<Fish> getAllWildtypeFish();

    List<Genotype> getGenotypesByFeatureAndBackground(Feature feature, Genotype background, Publication publication);

    FishExperiment getFishExperiment(String zdbID);

    List<FishExperiment> getFishExperiment(Genotype genotype);

    /**
     * Retrieve list of all zygosities
     *
     * @return
     */
    List<Zygosity> getListOfZygosity();

    /**
     * Retrieve Zygosity by ID
     *
     * @param ID
     * @return
     */
    Zygosity getZygosity(String ID);

    /**
     * Create Genotype
     *
     * @param genotype
     */
    void saveGenotype(Genotype genotype, String publicationID);

    void updateGenotypeNicknameWithHandleForPublication(Publication publication);

    List<PhenotypeStatementWarehouse> getPhenotypeStatementForMarker(Marker marker);

    /**
     * Retrieve FishExperiment by Fish ID and Experiment ID
     *
     * @param fishID   fish id
     * @param experimentID id
     * @return FishExperiment
     */
    FishExperiment getFishExperimentByFishAndExperimentID (String fishID, String experimentID);

    List<Fish> getFishByGenotype(Genotype genotype);

    List<Fish> getFishByGenotypeNoExperiment(Genotype genotype);

    long getFishCountByGenotype(String genotypeID, String publicationID);

    long getPhenotypeByFishAndPublication(Fish fish, String publicationID);

    long getInferredFromCountByGenotype(String genotypeID, String publicationID);

    long getFishExperimentCountByGenotype(Fish fish, String publicationID);

    List<PhenotypeStatementWarehouse> getPhenotypeObserved(GenericTerm term, Fish fish, boolean includeSubstructures);

    List<PhenotypeStatementWarehouse> getPhenotypeStatementObservedForMutantSummary(GenericTerm term, Fish fish, boolean includeSubstructures);

    List<DiseaseAnnotationModel> getDiseaseAnnotationModels(int numfOfRecords);
    List<OmimPhenotype> getDiseaseModelsFromGenes(int numfOfRecords);
}

