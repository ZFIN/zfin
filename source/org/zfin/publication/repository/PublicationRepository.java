package org.zfin.publication.repository;

import org.springframework.web.multipart.MultipartFile;
import org.zfin.antibody.Antibody;
import org.zfin.curation.presentation.CorrespondenceDTO;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.SourceAlias;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.Person;
import org.zfin.publication.*;
import org.zfin.publication.presentation.MetricsOnDateBean;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.publication.presentation.MetricsByDateBean;
import org.zfin.publication.presentation.PublicationMetricsFormBean;
import org.zfin.repository.PaginationParameter;
import org.zfin.sequence.MarkerDBLink;

import java.io.IOException;
import java.util.*;

/**
 * Persistence class that deals with Publication objects.
 */
public interface PublicationRepository extends PaginationParameter {

    /**
     * Retrieve the number of publications that contain the
     * a term 'abstractText' in its abstract. This count is
     * done case insensitive.
     *
     * @param abstractText text
     * @return number of publications
     */
    int getNumberOfPublications(String abstractText);

    /**
     * Retrieve all distinct publications that contain a high quality probe
     * with a rating of 4.
     *
     * @param anatomyTerm Anatomy Term
     * @return list of publications
     */
    List<Publication> getHighQualityProbePublications(GenericTerm anatomyTerm);

    /**
     * Retrieve all publication for a given geneID and anatomical structure.
     *
     * @param geneID        gene ID
     * @param anatomyItemID term ID
     */
    List<Publication> getExpressedGenePublications(String geneID, String anatomyItemID);

    List<String> getSNPPublicationIDs(Marker marker);

    /**
     * Retrieve the total number of publications for a given geneID and anatomical structure
     * that contain figures.
     *
     * @param geneID        gene zdbID
     * @param anatomyItemID anatomy ID
     * @return number
     */
    int getNumberOfExpressedGenePublicationsWithFigures(String geneID, String anatomyItemID);

    /**
     * Retrieve all publication that are annotated to genes expressed in a given
     * anatomical structure.
     *
     * @param anatomyItemID
     */
    List<Publication> getExpressedGenePublications(String anatomyItemID);

    /**
     * Retrieve the genes and CDNA/EST for the high-quality probes with
     * rating of 4.
     *
     * @param term anatomy term
     * @return list of High quality probes.
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeNames(GenericTerm term);

    /**
     * Retrieve the genes and CDNA/EST for the high-quality probes with
     * rating of 4 (which equals 5 stars) associate to an anatomical structure.
     * Only return n records.
     *
     * @param term   anatomy term
     * @param maxRow max number of records
     * @return list of HighqQualityProbes
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeNames(Term term, int maxRow);

    /**
     * Retrieve marker records that have a gene expression in the
     * anatomy term, identified by the zdbID. The maxRow number
     * limits the result set.
     *
     * @param zdbID  of the anatomy term.
     * @param maxRow max number of records
     * @return list of markers
     */
    List<Marker> getAllExpressedMarkers(String zdbID, int maxRow);

    /**
     * Returns the appropriate # of records, as well as statistics on the total # of records.
     *
     * @param anatomyTerm term
     * @param firstRow    first   record
     * @param maxRow      last record
     * @return marker statistics
     */
    PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm, int firstRow, int maxRow);

    /**
     * Returns the appropriate # of records, as well as statistics on the total # of records.
     *
     * @param anatomyTerm term
     * @return marker statistics
     */
    PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm);


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
     * Retrieve a marker (gene) by its symbol name. If it is not unique a Hibernate runtime exception is thrown.
     *
     * @param symbol
     */
    Marker getMarker(String symbol);

    /**
     * Retrieve a marker by its zdbID
     *
     * @param zdbID
     */
    Marker getMarkerByZdbID(String zdbID);

    /**
     * Check if a publication with the specified primary key exists.
     *
     * @param canonicalPublicationZdbID
     */
    boolean publicationExists(String canonicalPublicationZdbID);


    /**
     * Figure getFigureById(String zdbID);
     * <p/>
     * Retrieve the figures that can be found for a given publication and gene.
     *
     * @param geneID
     * @param publicationID
     */
    List<Figure> getFiguresByGeneID(String geneID, String publicationID);

    Figure getFigureByID(String figureZdbID);

    List<Figure> getFiguresByGeneAndPublication(String geneID, String publicationID);

    List<FeatureMarkerRelationship> getFeatureMarkerRelationshipsByPubID(String publicationID);

    /**
     * Return all figures for a specified gene, probe and anatommical structure.
     * Clone information is not required.
     *
     * @param gene   Gene
     * @param clone  Probe
     * @param aoTerm anatomical structure
     * @return list of figures
     */
    List<Figure> getFiguresPerProbeAndAnatomy(Marker gene, Marker clone, GenericTerm aoTerm);

    /**
     * Return all Publications for a specified gene, probe and anatommical structure with figures associated.
     *
     * @param gene    Gene
     * @param subGene Probe
     * @param aoTerm  anatomical structure
     * @return list of figures
     */
    List<Publication> getPublicationsWithFiguresPerProbeAndAnatomy(Marker gene, Marker subGene, GenericTerm aoTerm);

    /**
     * Retrieve the figures that can be found for a given publication and probe.
     *
     * @param probeID
     * @param publicationID
     */
    List<Figure> getFiguresByProbeAndPublication(String probeID, String publicationID);


    /**
     * Used to add a sorting string onto the query.
     *
     * @param orderVariable
     */
    void addOrdering(String orderVariable);

    void removeOrderByFields();


    /**
     * Retrieves publications with Accession Number's (pubmed Ids) but with null or 'none' DOIs.
     *
     * @param maxResults number
     * @return list
     */
    List<Publication> getPublicationsWithAccessionButNoDOI(int maxResults);


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

    PaginationResult<Figure> getFiguresByGeno(Genotype geno);

    PaginationResult<Figure> getFiguresByGenoExp(Genotype geno);

    PaginationResult<Publication> getPublicationsWithFiguresbyGeno(Genotype genotype);

    PaginationResult<Publication> getPublicationsWithFiguresbyGenoExp(Genotype genotype);


    /**
     * Retrieve publications that have phenotype data for a given term and genotype including
     * substructures
     *
     * @param fish   Fish
     * @param aoTerm ao term  @return Number of publications with figures per genotype and anatomy
     */
    PaginationResult<Publication> getPublicationsWithFigures(Fish fish, GenericTerm aoTerm, boolean includeSubstructures);

    /**
     * @param genotype Genotype
     * @param aoTerm   ao term
     * @return Number of publications with figures per genotype and anatomy
     */
    int getNumPublicationsWithFiguresPerGenotypeAndAnatomy(Genotype genotype, GenericTerm aoTerm);

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
    Journal getJournalByTitle(String journalTitle);

    Journal findJournalByAbbreviation(String abbrevation);

    void createJournal(Journal journal);

    Journal getJournalByPrintIssn(String pIssn);

    Journal getJournalByEIssn(String eIssn);

    SourceAlias addJournalAlias(Journal journal, String alias);

    int getNumberAssociatedPublicationsForZdbID(String zdbID);

    PaginationResult<Publication> getAllAssociatedPublicationsForFeature(Feature feature, int maxPubs);

    /**
     * Retrieve Figue by ID
     *
     * @param zdbID ID
     * @return Figure
     */
    Figure getFigure(String zdbID);

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
     * Retrieve distinct list of genes that are attributed to a given
     * publication and used in an experiment.
     *
     * @param pubID publication id
     * @return list of markers
     */
    List<Marker> getGenesByExperiment(String pubID);

    /**
     * Retrieve list of Genotypes being used in experiments for a given publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    List<Genotype> getFishUsedInExperiment(String publicationID);

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

    Publication getSinglePublicationByPmid(Integer pubMedID);

    int getNumberDirectPublications(String zdbID);

    /**
     * Retrieve list of mutants and transgenics being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype (non-wt)
     */
    List<Genotype> getMutantsAndTgsByPublication(String publicationID);

    List<Ortholog> getOrthologListByPub(String pubID);

    List<Ortholog> getOrthologListByMrkr(String mrkrID);

    PaginationResult<Ortholog> getOrthologPaginationByPub(String pubID, GeneBean searchBean);
    List<Ortholog> getOrthologPaginationByPub(String pubID);

    List<Publication> getPublicationWithPubMedId(Integer maxResult);

    SortedSet<Publication> getAllPublicationsForFeature(Feature feature);

    SortedSet<Publication> getPublicationForJournal(Journal journal);

    Journal getJournalByID(String zdbID);

    List<Journal> findJournalByAbbreviationAndName(String query);

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

    PublicationTrackingStatus getPublicationStatusByName(PublicationTrackingStatus.Name name);

    List<PublicationTrackingLocation> getAllPublicationLocations();

    List<Publication> getAllPublications();

    List<Publication> getAllPubMedPublications();

    List<Publication> getAllOpenPublications();

    List<Publication> getAllOpenPublicationsOfJournalType(PublicationType type);

    PublicationTrackingLocation getPublicationTrackingLocation(long id);

    DashboardPublicationList getPublicationsByStatus(Long status, Long location, String owner, int count, int offset, String sort);

    List<PublicationFileType> getAllPublicationFileTypes();

    PublicationFileType getPublicationFileType(long id);

    PublicationFile getPublicationFile(long id);

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

    PublicationAttribution createPublicationAttribution(Publication publication, Marker marker);

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

    boolean isNewGenePubAttribution(Marker marker, String publicationId);

    boolean isNewFeaturePubAttribution(Feature feature, String publicationId);

    boolean hasCuratedOrthology(Marker marker);

    Map<Marker, Boolean> areNewGenePubAttribution(List<Marker> attributedMarker, String publicationId);

    List<SequenceTargetingReagent> getSTRsByPublication(String publicationID, Pagination pagination);

    List<Image> getImages(Publication publication);
}
