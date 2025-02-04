package org.zfin.publication.repository;

import org.springframework.web.multipart.MultipartFile;
import org.zfin.antibody.Antibody;
import org.zfin.curation.presentation.CorrespondenceDTO;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.SourceAlias;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.Person;
import org.zfin.publication.*;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.publication.presentation.MetricsByDateBean;
import org.zfin.publication.presentation.MetricsOnDateBean;
import org.zfin.publication.presentation.PublicationMetricsFormBean;
import org.zfin.repository.PaginationParameter;
import org.zfin.sequence.MarkerDBLink;

import java.io.IOException;
import java.util.*;

/**
 * Persistence class that deals with Publication objects.
 */
public interface PublicationRepository extends PaginationParameter {

    List<String> getSNPPublicationIDs(Marker marker);

    /**
     * Returns the appropriate # of records, as well as statistics on the total # of records.
     *
     * @param anatomyTerm term
     * @param pagination  Pagination bean
     * @return marker statistics
     */
    PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm, Pagination pagination);

    /**
     * Count the number of figures from all publications that have a gene
     * expression in a given anatomy structure.
     *
     * @param anatomyTerm term
     * @return number
     */
    int getTotalNumberOfFiguresPerAnatomyItem(GenericTerm anatomyTerm);

    /**
     * Retrieve a publication by its primary key.
     *
     * @param zdbID
     */
    Publication getPublication(String zdbID);

    List<Publication> getPublications(List<String> zdbIDs);

    /**
     * Check if a publication with the specified primary key exists.
     *
     * @param canonicalPublicationZdbID
     */
    boolean publicationExists(String canonicalPublicationZdbID);

    /**
     * Saves a list of publications in one transaction.
     *
     * @param publicationList List of publication
     * @return boolean
     */
    boolean updatePublications(List<Publication> publicationList);

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     * for mutant genotypes excluding SequenceTargetingReagent.
     *
     * @param fish genotype
     * @param term anatomy term
     * @return list of figures.
     */
    PaginationResult<Figure> getFiguresByFishAndAnatomy(Fish fish, GenericTerm term);

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     * for mutant genotypes excluding SequenceTargetingReagent.
     *
     * @param fish genotype
     * @param term anatomy term
     * @return list of figures.
     */
    PaginationResult<Figure> getFiguresByFishAndAnatomy(Fish fish, GenericTerm term, boolean includeSubstructures);

    /**
     * Retrieve figures for a given gene and anatomy term.
     *
     * @param gene        Gene
     * @param anatomyTerm anatomy
     * @return a set of figures
     */
    List<Figure> getFiguresByGeneAndAnatomy(Marker gene, GenericTerm anatomyTerm);

    List<Publication> getPubsForDisplay(String zdbID);

    List<Journal> getAllJournals();

    Journal getJournalByAbbreviation(String abbreviation);

    Journal getJournalByPrintIssn(String pIssn);

    Journal getJournalByEIssn(String eIssn);

    SourceAlias addJournalAlias(Journal journal, String alias);

    int getNumberAssociatedPublicationsForZdbID(String zdbID);

    Image getImageById(String zdbID);

    /**
     * Retrieve all Publications with Figures for a given marker and ao term.
     * Standard condition and wildtype fish
     *
     * @param marker      marker
     * @param anatomyTerm ao term
     * @return pagination result
     */
    PaginationResult<Publication> getPublicationsWithFigures(Marker marker, GenericTerm anatomyTerm);

    /**
     * Retrieve a list of distinct figures, labels not IDs
     *
     * @param publicationID pub id
     * @return list of labels
     */
    List<String> getDistinctFigureLabels(String publicationID);

    /**
     * Retrieve distinct list of genes that are attributed to a given
     * publication.
     *
     * @param pubID publication id
     * @return list of markers
     */
    List<Marker> getGenesByPublication(String pubID);

    List<Marker> getGenesByPublication(String pubID, boolean includeEFGs);

    List<Marker> getSTRByPublication(String pubID);

    List<Marker> getGenesAndMarkersByPublication(String pubID);

    List<Marker> getMarkersByTypeForPublication(String pubID, MarkerType markerType);

    List<SequenceTargetingReagent> getSTRsByPublication(String pubID, MarkerType markerType);

    PaginationResult<Clone> getClonesByPublication(String pubID, PaginationBean paginationBean);

    List<Feature> getFeaturesByPublication(String pubID);

    List<Fish> getFishByPublication(String pubID);

    /**
     * Retrieve list of Genotypes being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    List<Genotype> getGenotypesInPublication(String publicationID);

    /**
     * Retrieve all experiments that pertain to a given publication.
     * It adds the Standard and Generic-control
     *
     * @param publicationID publication
     * @return listof experiments
     */
    List<Experiment> getExperimentsByPublication(String publicationID);

    /**
     * Retrieve a genotype for a given handle.
     *
     * @param nickname string
     * @return genotype
     */
    Genotype getGenotypeByHandle(String nickname);

    /**
     * Retrieve all Genotypes attributed to a given publication.
     *
     * @param publicationID pub id
     * @return list of genoypes
     */
    List<Genotype> getNonWTGenotypesByPublication(String publicationID);

    /**
     * Retrieve antibodies attributes to a given publication
     *
     * @param publicationID String
     * @return list of antibodies
     */
    List<Antibody> getAntibodiesByPublication(String publicationID);

    /**
     * Retrieve antibodies by publication and associated gene
     *
     * @param publicationID String
     * @param geneID        String
     * @return list of antibodies
     */
    List<Antibody> getAntibodiesByPublicationAndGene(String publicationID, String geneID);

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     * @return list of markers
     */
    List<Marker> getGenesByAntibody(String publicationID, String antibodyID);

    /**
     * Retrieve access numbers for given pub and gene.
     *
     * @param publicationID string
     * @param geneID        string
     * @return list of db links
     */
    List<MarkerDBLink> getDBLinksByGene(String publicationID, String geneID);

    /**
     * Retrieve db link object of a clone for a gene and pub.
     *
     * @param pubID  pub is
     * @param geneID gene ID
     * @return list of MarkerDBLinks
     */
    List<MarkerDBLink> getDBLinksForCloneByGene(String pubID, String geneID);

    /**
     * Retrieve all figures that are associated to a given publication.
     *
     * @param pubID publication ID
     * @return list of figures
     */
    List<Figure> getFiguresByPublication(String pubID);

    List<Publication> getPublicationsWithAccessionButNoDOIAndLessAttempts(int maxAttempts, int maxProcesses);

    List<Publication> addDOIAttempts(List<Publication> publicationList);

    PaginationResult<Publication> getAllAssociatedPublicationsForGenotype(Genotype genotype, int maxPubs);

    List<Publication> getPublicationByPmid(Integer pubMedID);

    int getNumberDirectPublications(String zdbID);

    List<Ortholog> getOrthologListByPub(String pubID);

    List<Ortholog> getOrthologListByMrkr(String mrkrID);

    PaginationResult<Ortholog> getOrthologPaginationByPub(String pubID, GeneBean searchBean);

    List<Ortholog> getOrthologPaginationByPub(String pubID);

    List<Publication> getPublicationWithPubMedId(Integer maxResult);

    SortedSet<Publication> getAllPublicationsForFeature(Feature feature);

    SortedSet<Publication> getPublicationForJournal(Journal journal);

    Journal getJournalByID(String zdbID);

    SortedSet<Publication> getAllPublicationsForGenotypes(List<Genotype> genotypes);

    SortedSet<Publication> getAllPublicationsForGenotype(Genotype genotype);

    List<String> getPublicationIDsForGOwithField(String zdbID);

    void addPublication(Publication publication);

    void addPublication(Publication publication,
                        PublicationTrackingStatus.Name status,
                        PublicationTrackingLocation.Name location,
                        Person owner);

    List<String> getFeatureNamesWithNoGenotypesForPub(String pubZdbID);

    int deleteExpressionExperimentIDswithNoExpressionResult(Publication publication);

    List<String> getTalenOrCrisprFeaturesWithNoRelationship(String pubZdbID);

    long getMarkerCount(Publication publication);

    long getMorpholinoCount(Publication publication);

    long getTalenCount(Publication publication);

    long getCrisprCount(Publication publication);

    long getAntibodyCount(Publication publication);

    long getEfgCount(Publication publication);

    long getCloneProbeCount(Publication publication);

    long getExpressionCount(Publication publication);

    long getPhenotypeCount(Publication publication);

    long getPhenotypeAlleleCount(Publication publication);

    long getFeatureCount(Publication publication);

    long getFishCount(Publication publication);

    long getOrthologyCount(Publication publication);

    long getMappingDetailsCount(Publication publication);

    Boolean canDeletePublication(Publication publication);

    Fish getFishByHandle(String handle);

    List<Fish> getNonWTFishByPublication(String publicationID);

    List<Fish> getWildtypeFish();

    PublicationTrackingHistory currentTrackingStatus(Publication publication);

    List<PublicationTrackingHistory> fullTrackingHistory(Publication publication);

    List<PublicationTrackingStatus> getAllPublicationStatuses();

    PublicationTrackingStatus getPublicationTrackingStatus(long id);

    Long getPublicationTrackingStatus(Person person,
                                      int days,
                                      PublicationTrackingStatus... status);

    PublicationTrackingStatus getPublicationStatusByName(PublicationTrackingStatus.Name name);

    List<PublicationTrackingLocation> getAllPublicationLocations();

    List<Publication> getAllPublications();

    List<Publication> getAllOpenPublications();

    List<Publication> getAllOpenPublicationsOfJournalType(PublicationType type);

    PublicationTrackingLocation getPublicationTrackingLocation(long id);

    DashboardPublicationList getPublicationsByStatus(Long status, Long location, String owner, int count, int offset, String sort);

    List<PublicationFileType> getAllPublicationFileTypes();

    PublicationFileType getPublicationFileType(long id);

    PublicationFile getPublicationFile(long id);

    List<PublicationFile> getAllPublicationFiles();

    PublicationFileType getPublicationFileTypeByName(PublicationFileType.Name name);

    PublicationFile getOriginalArticle(Publication publication);

    PublicationFile addPublicationFile(Publication publication, PublicationFileType type, MultipartFile file) throws IOException;

    CorrespondenceSentMessage addSentCorrespondence(Publication publication, CorrespondenceDTO dto);

    CorrespondenceSentMessage addResentCorrespondence(Publication publication, CorrespondenceDTO dto);

    CorrespondenceReceivedMessage addReceivedCorrespondence(Publication publication, CorrespondenceDTO dto);

    List<String> getPublicationIdsForMarkerGo(String markerZdbID, String markerGoEvdTermZdbID, String evidenceCode, String inference);

    List<String> getPublicationIdsForFeatureType(String featureZdbID);

    GregorianCalendar getNewestPubEntryDate();

    GregorianCalendar getOldestPubEntryDate();

    List<String> getDirectlyAttributedZdbids(String publicationId, Pagination pagination);

    Long getDirectlyAttributed(Publication publication);

    List<MetricsByDateBean> getMetricsByDate(Calendar start,
                                             Calendar end,
                                             PublicationMetricsFormBean.QueryType query,
                                             PublicationMetricsFormBean.Interval groupInterval,
                                             PublicationMetricsFormBean.GroupType groupType);

    List<MetricsOnDateBean> getCumulativeMetrics(Calendar end, PublicationMetricsFormBean.GroupType groupType);

    List<MetricsOnDateBean> getSnapshotMetrics(PublicationMetricsFormBean.GroupType groupType);

    ProcessingChecklistTask getProcessingChecklistTask(ProcessingChecklistTask.Task task);

    PublicationProcessingChecklistEntry getProcessingChecklistEntry(long id);

    List<PubmedPublicationAuthor> getPubmedPublicationAuthorsByPublication(Publication publication);

    boolean isNewFeaturePubAttribution(Feature feature, String publicationId);

    boolean hasCuratedOrthology(Marker marker);

    Map<Marker, Boolean> areNewGenePubAttribution(List<Marker> attributedMarker, String publicationId);

    List<SequenceTargetingReagent> getSTRsByPublication(String publicationID, Pagination pagination);

    List<Image> getImages(Publication publication);

    Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> getAllFiguresForPhenotype();

    Map<Fish, Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>>> getAllChebiPhenotype();

    Map<Fish, Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>>> getAllPhenotypeWithChebiInEQE();
    List<PhenotypeStatementWarehouse> getAllChebiPhenotypeExperiment(Experiment experiment);

    List<CorrespondenceNeed> getCorrespondenceNeedByPublicationID(String zdbID);

    List<CorrespondenceNeedReason> getAllCorrespondenceNeedReasons();

    void deleteCorrespondenceNeedByPublicationID(String pubID);

    CorrespondenceNeedReason getCorrespondenceNeedReasonByID(long id);

    void insertCorrespondenceNeed(CorrespondenceNeed correspondenceNeed);

    List<CorrespondenceResolution> getCorrespondenceResolutionByPublicationID(String zdbID);

    List<CorrespondenceResolutionType> getAllCorrespondenceResolutionTypes();

    void deleteCorrespondenceResolutionByPublicationID(String pubID);

    CorrespondenceResolutionType getCorrespondenceResolutionTypeByID(long id);

    void insertCorrespondenceResolution(CorrespondenceResolution correspondenceResolution);

    Map<Publication, List<PublicationDbXref>> getAllDataSetsPublication();

    Map<Publication, List<STRTargetRow>> getAllAttributedSTRs(Pagination pagination);
}
