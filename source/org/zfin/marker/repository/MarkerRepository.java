package org.zfin.marker.repository;

import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.presentation.ConstructComponentPresentation;
import org.zfin.feature.Feature;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.mapping.Location;
import org.zfin.mapping.MarkerLocation;
import org.zfin.marker.*;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.presentation.*;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.MarkerSupplier;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationDbXref;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Database;

import java.util.*;

public interface MarkerRepository {
    Marker getMarker(Marker marker);

    Marker getMarker(String id);

    Marker getMarkerByID(String zdbID);

    List<Marker> getMarkersByZdbIDs(List<String> zdbIDs);

    SNP getSNPByID(String zdbID);

    ConstructCuration getConstructByID(String zdbID);
    AllianceGeneDesc getGeneDescByMkr(Marker marker);

    Marker getMarkerOrReplacedByID(String zdbID);

    Marker getGeneByID(String zdbID);

    List<Marker> getMarkersForRelation(String mrkrid, String pubId);

    Clone getCloneById(String zdbID);

    Transcript getTranscriptByZdbID(String zdbID);

    Transcript getTranscriptByName(String name);

    Transcript getTranscriptByVegaID(String vegaID);

    List<String> getTranscriptTypes();

    Marker getMarkerByAbbreviationIgnoreCase(String abbreviation);

    Marker getMarkerByAbbreviation(String abbreviation);

    Marker getMarkerByAbbreviationAndAttribution(String name, String pubZdbId);

    SequenceTargetingReagent getSequenceTargetingReagentByAbbreviation(String abbreviation);

    Marker getMarkerByName(String name);

    MarkerRelationship getMarkerRelationship(Marker firstMarker,
                                             Marker secondMarker,
                                             MarkerRelationship.Type type);
    MarkerRelationship getMarkerRelationship(Marker firstMarker,
                                             Marker secondMarker
                                             );

    List<MarkerRelationship> getMarkerRelationshipsByPublication(String publicationZdbID);

    List<Marker> getMarkersByAbbreviation(String name);

    List<Marker> getGenesByAbbreviation(String name);

    Marker getGeneByAbbreviation(String name);

    MarkerRelationship getMarkerRelationshipByID(String zdbID);

    MarkerAlias getSpecificDataAlias(Marker marker, String alias);

    TreeSet<String> getLG(Marker marker);

    MarkerRelationship addMarkerRelationship(MarkerRelationship mrel, String attributionZdbID);

    void addSmallSegmentToGene(Marker gene, Marker segment, String attributionZdbID);

    void updateMarkerPublicNote(Marker marker, String note);

    DataNote addMarkerDataNote(Marker marker, String note);

    AntibodyExternalNote addAntibodyExternalNote(Antibody antibody, String note, String sourcezdbid);

    /**
     * Creates a new note in regards to the orthology to a gene.
     * Only a single note allowed per gene, i.e. if no note exists then
     * create a new one, if there is already one available then it gets replaced
     * with this one (updated) as we typically display the old note in the updates field.
     * Attribution is done on the current security person.
     *
     * @param gene gene
     * @param note note text
     */
    OrthologyNote createOrUpdateOrthologyExternalNote(Marker gene, String note);

    /**
     * Create a new alias for a given marker. IF no alias is found no alias is created.
     *
     * @param marker      valid marker object.
     * @param alias       alias string
     * @param publication publication object
     */
    MarkerAlias addMarkerAlias(Marker marker, String alias, Publication publication);

    /**
     * Delete an existing alias that belongs to a given marker.
     *
     * @param marker Marker Object
     * @param alias  Marker alias object
     */
    void deleteMarkerAlias(Marker marker, MarkerAlias alias);

    void updateCuratorNote(Marker marker, DataNote note, String newNote);

    void removeCuratorNote(Marker marker, DataNote note);

    /**
     * Delete a marker relationship
     *
     * @param mrel
     */
    void deleteMarkerRelationship(MarkerRelationship mrel);

    void deleteConstructComponentByID(String constructID);

    void addDataAliasAttribution(DataAlias alias, Publication attribution, Marker marker);

    void addGenomeLocationAttribution(Location genomeLocation, String publicationID);

    void addGenomeLocationAttribution(Location genomeLocation, Publication attribution);

    void synchronizeGenomeLocationAttributions(MarkerLocation genomeLocation, Set<String> publicationIDsToSync);

    void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution);

    void addDBLinkAttribution(DBLink dbLink, Publication attribution, String dataZdbId);

    void addDBLinkAttribution(DBLink dbLink, Publication attribution, Marker marker);

    /**
     * Add a publication to a given marker: Attribution.
     *
     * @param marker      valid marker object
     * @param publication publication object
     */
    void addMarkerPub(Marker marker, Publication publication);

    MarkerDBLink getDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb);

    DBLink addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID);

    MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason, MarkerAlias markerAlias);

    MarkerType getMarkerTypeByName(String name);

    MarkerType getMarkerTypeByDisplayName(String displayName);

    MarkerTypeGroup getMarkerTypeGroupByName(String name);

    void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason, String oldSymbol, String oldGeneName);

    List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring);

    MarkerFamilyName getMarkerFamilyName(String name);

    void createMarker(Marker marker, Publication publication, boolean insertUpdate);

    void createMarker(Marker marker, Publication publication);

    /**
     * Checks if a gene has a small segment relationship with a given small segment.
     *
     * @param associatedMarker Gene
     * @param smallSegment     small segment marker
     * @return boolean
     */
    boolean hasSmallSegmentRelationship(Marker associatedMarker, Marker smallSegment);


    /**
     * @param associatedMarker Associated Marker.
     * @param transcript       Transcript
     * @return Marker has a relationship with this Transcript.
     */
    boolean hasTranscriptRelationship(Marker associatedMarker, Marker transcript);

    /**
     * Retrieve all markers of a given type group whose abbreviation
     * contains the 'name' string
     *
     * @param name       String
     * @param markerType Marker.MarkerType
     * @return list of marker objects
     */
    List<Marker> getMarkersByAbbreviationAndGroup(String name, Marker.TypeGroup markerType);

    /**
     * Retrieve all markers of a given type group whose abbreviation
     * contains the 'name' string
     *
     * @param name       String
     * @param markerType Marker.MarkerType
     * @param pubZdbId
     * @return list of marker objects
     */

    List<Marker> getConstructsByAttribution(String name);

    // clone methods

    List<String> getPolymeraseNames();

    List<String> getVectorNames();

    List<String> getProbeLibraryNames();

    List<ProbeLibrary> getProbeLibraries();

    ProbeLibrary getProbeLibrary(String zdbID);

    List<String> getDigests();

    List<String> getCloneSites();

    /**
     * Retrieve a marker alias by zdb ID
     *
     * @param aliasZdbID id
     * @return Marker Alias object
     */
    MarkerAlias getMarkerAlias(String aliasZdbID);


    List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions();

    List<TranscriptType> getAllTranscriptTypes();

    TranscriptType getTranscriptTypeForName(String typeString);

    TranscriptStatus getTranscriptStatusForName(String statusString);

    //these are pulled from push4genomix.pl

    boolean getGeneHasExpression(Marker gene);

    boolean getGeneHasExpressionImages(Marker gene);

    boolean getGeneHasGOEvidence(Marker gene);

    boolean getGeneHasPhenotype(Marker gene);

    boolean getGeneHasPhenotypeImage(Marker gene);


    /**
     * Get all high quality probes AO Statistics records for a given ao term.
     * Note: for the case to include substructures the result set is not returned just the total number
     * in the PaginationResult object!
     *
     * @param aoTerm               ao term
     * @param pagination           pagination bean
     * @param includeSubstructures boolean
     * @return pagination result
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(GenericTerm aoTerm, PaginationBean pagination, boolean includeSubstructures);

    /**
     * Retrieve all distinct publications that contain a high quality probe
     * with a rating of 4.
     *
     * @param anatomyTerm Anatomy Term
     * @return list of publications
     */
    List<Publication> getHighQualityProbePublications(GenericTerm anatomyTerm);

    /**
     * Retrieve marker types by marker type groups
     *
     * @param typeGroup type group
     * @return list of marker types
     */
    List<MarkerType> getMarkerTypesByGroup(Marker.TypeGroup typeGroup);

    List<Marker> getMarkersForStandardAttributionAndType(Publication publication, String type);

    List<Marker> getMarkersForAttribution(String publicationZdbID);

    List<ConstructCuration> getConstructsForAttribution(String publicationZdbID);

    List<ConstructComponent> getConstructComponent(String constructZdbID);

    void deleteConstructComponents(String constructZdbID);

    List<ProteinToPDB> getPDB(String uniProtID);

    /**
     * Create a gene for a given SequenceTargetingReagent which is targeting it.
     *
     * @param sequenceTargetingReagent valid SequenceTargetingReagent of Marker object.
     * @return the target gene of the SequenceTargetingReagent
     */
    List<Marker> getTargetGenesAsMarkerForSequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent);

    /**
     * Checks to see if a marker with the abbreviation given is already in the database.
     * The check is case insensitive.
     *
     * @param abbreviation string
     * @return true/false
     */
    boolean isMarkerExists(String abbreviation);

    /**
     * Retrieves all marker IDs if no number is given or the first N markers for firstN = 0;
     *
     * @param firstN number of markers to be returned
     * @return list of markers
     */
    List<String> getNMarkersPerType(int firstN);

    /**
     * Retrieves all marker IDs.
     *
     * @return list of markers
     */
    List<String> getAllMarkers();

    /**
     * Retrieve all distinct marker types used in the marker table
     *
     * @return list of marker types
     */
    List<MarkerType> getAllMarkerTypes();

    /**
     * Retrieve all gene ids of genes that have a SwissProt external note.
     *
     * @param firstNIds number of records to be returned
     * @return list of gene ids
     */
    List<String> getNMarkersWithUniProtNote(int firstNIds);

    boolean getHasMarkerHistory(String zdbId);

    List<PreviousNameLight> getPreviousNamesLight(Marker gene);


    List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayExcludeTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types);

    List<Marker> getMarkersByAlias(String key);

    List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayForTypes(Marker construct, boolean is1to2, MarkerRelationship.Type... types);

    MarkerDBLink getMarkerDBLink(String linkId);
    String getABRegID(String zdbID);
    List<String> getABRegIDs(String zdbID);

    List<LinkDisplay> getMarkerLinkDisplay(String dbLinkId);

    List<LinkDisplay> getMarkerDBLinksFast(Marker marker, DisplayGroup.GroupName groupName);

    List<MarkerRelationshipPresentation> getRelatedMarkerDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types);
    List<String> getMarkerRelationshipTypesForMarkerEdit(Marker marker,Boolean interacts);

    List<GeneProductsBean> getGeneProducts(String zdbID);

     List<InterProProtein> getInterProForMarker(Marker marker);

    void insertInterProForMarker(String markerZdbID, String uniprot);

    void deleteInterProForMarker(String markerZdbID, String uniprot);

    List<String> getProteinType(Marker marker);
    List<String> getIPNames( String uniprot);

    boolean isFromChimericClone(String zdbID);

    boolean cloneHasSnp(Clone clone);

    List<MarkerSupplier> getSuppliersForMarker(String zdbID);

    boolean markerExistsForZdbID(String zdbID);

    List<String> getMarkerZdbIdsForType(Marker.Type gene);

    Map<String, String> getGeoMarkerCandidates();

    /**
     * Retrieve list of mutants and transgenics being associated with a gene
     *
     * @param geneID gene ID
     * @return list of genotype (non-wt)
     */
    List<Genotype> getMutantsAndTgsByGene(String geneID);

    /**
     * Retrieve list of Feature objects with the features created with a TALEN or CRISPR
     *
     * @param sequenceTargetingReagent (TALEN or CRISPR)
     * @return list of Feature
     */
    List<Feature> getFeaturesBySTR(Marker sequenceTargetingReagent);

    SequenceTargetingReagent getSequenceTargetingReagent(String markerID);

    SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence);

    SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence1, String sequence2);

    List<Marker> getConstructsForGene(Marker gene);

    Genotype getStrainForTranscript(String zdbID);

    List<LinkDisplay> getVegaGeneDBLinksTranscript(Marker gene, DisplayGroup.GroupName summaryPage);
    List<LinkDisplay> getAllVegaGeneDBLinksTranscript();

    /**
     * Retrieves all engineered region markers.
     *
     * @return
     */
    List<Marker> getAllEngineeredRegions();

    List<MarkerRelationshipPresentation> getClonesForGeneTranscripts(String zdbID);

    List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID, MarkerRelationship.Type type1, MarkerRelationship.Type type2);

    List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID, MarkerRelationship.Type type1, MarkerRelationship.Type type2, String resultType);

    List<Marker> getCodingSequence(Marker construct);
    List<Marker> getGenesforTranscript(Marker tscript);
    Marker getGeneforTranscript(Marker tscript);

    List<SupplierLookupEntry> getSupplierNamesForString(String lookupString);

    List<LookupEntry> getRelationshipTargetsForString(String lookupString);
    List<String> getMarkerTypesforRelationship(String relType);
    List<LookupEntry> getConstructComponentsForString(String lookupString, String pubZdbId);


    List<ConstructComponentPresentation> getConstructComponents(String constructZdbID);

    ConstructComponentPresentation getConstructComponentsForDisplay(String constructZdbID);

    void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, Marker marker, String pubID);

    void addConstructComponent(int cassetteNumber, int ccOrder, String constructId, String ccValue, ConstructComponent.Type type, String ccCategory, String ccZdbID);


    /**
     * Return list of markers that have a specified relationship to the main marker
     *
     * @param marker
     * @param types
     * @return
     */
    List<Marker> getRelatedMarkersForTypes(Marker marker, MarkerRelationship.Type... types);

    List<Marker> getRelatedGenesViaTranscript(Marker marker, MarkerRelationship.Type relType1, MarkerRelationship.Type relType2);

    /**
     * Retrieve makrer from feature via feature marker relationship
     * 'is allele of', 'markers present', 'markers missing'
     *
     * @param feature
     * @return
     */
    Marker getMarkerByFeature(Feature feature);

    /**
     * Retrieve accession number for a given marker and database.
     *
     * @param marker
     * @param database
     * @return
     */
    String getAccessionNumber(Marker marker, Database.AvailableAbbrev database);
    int deleteMarkerDBLinksNotInList(ReferenceDatabase referenceDatabase, List<String> ids);
    int deleteMarkerDBLinksByIDList(ReferenceDatabase referenceDatabase, List<String> ids);


    List<LookupEntry> getMarkerSuggestionList(String lookupString, Marker.TypeGroup ...groups);

    List<LookupEntry> getGeneSuggestionList(String lookupString);

    List<TranscriptPresentation> getTranscriptsForGeneId(String geneZdbId);

    List<SequenceTargetingReagentLookupEntry> getSequenceTargetingReagentForString(String lookupString, String type);

    List<TargetGenePresentation> getTargetGenesForSequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent);

    List<Marker> getSecondMarkersByFirstMarkerAndMarkerRelationshipType(Marker firstMarker, MarkerRelationship.Type relationshipType);
     List<MarkerRelationship> getMarkerRelationshipBySecondMarker(Marker secondMarker);

    List<Marker> getMarkerWithRelationshipsBySecondMarkers(Set<String> secondMarkerAbbreviations);

    PaginationResult<Marker> getRelatedMarker(Marker marker, Set<MarkerRelationship.Type> types, PaginationBean paginationBean);

    List<OmimPhenotype> getOmimPhenotype(Marker marker);
    List<Marker> getZfinOrtholog(String humanAbbrev);
    int getCrisprCount(String geneAbbrev);

    MarkerHistory getMarkerHistory(String zdbID);

    MarkerLocation addMarkerLocation(MarkerLocation markerLocation);
    MarkerLocation getMarkerLocationByID(String zdbID);
    MarkerLocation saveMarkerLocation(MarkerLocation markerLocation);
    int deleteMarkerLocation(String zdbId);

    DBLink addDBLinkWithLenth(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID, int length);



    List<Marker> getMarkerByGroup(Marker.TypeGroup group, int number);
     List<Transcript> getAllNonCodingTranscripts();
    List<Transcript> getTranscriptsForNonCodingGenes();

    List<Marker> getWithdrawnMarkers();

    Map<String,GenericTerm> getSoTermMapping();

    void copyStrSequence(SequenceTargetingReagent str1, SequenceTargetingReagent str2);

    List<LookupEntry> getRegionListForString(String lookupString, String type);

    ZfinSoTerm getSoIdByMarkerType(String markerType);

    String getDbsnps(String cloneId);

    List<AntibodyLookupEntry> getAntibodyForString(String lookupString, String type);

    Set<Antibody> getAntibodies(Set<String> antibodyIds);
    TranscriptSequence getTranscriptSequence(Transcript transcript);

    MarkerRelationshipType getMarkerRelationshipType(String name);

    List<FluorescentProtein> getAllFluorescentProteins();

    List<FluorescentMarker> getAllFluorescentEfgs();

    List<FluorescentMarker> getAllFluorescentConstructs();

    int addMarkerDBLinks(ReferenceDatabase referenceDatabase, List<String> geneIdList);

    Long getSignafishLinkCount(ReferenceDatabase referenceDatabase);

    List<String> getProblemTypes();

    List<FluorescentProtein> getFluorescentProteins(String query);
    FluorescentProtein getFluorescentProtein(Long identifier);

    void updateMarkerName(String constructZdbID, String toString);


    List<SequenceTargetingReagent> getSequenceTargetingReagents(List<String> markerIDs);

    List<SequenceTargetingReagent> getRecentSequenceTargetingReagents(int limit);

    Map<Marker, List<Transcript>> getAllTranscripts(Pagination pagination);

    Map<Marker, List<MarkerDBLink>> getAllPlasmids(DisplayGroup.GroupName... groupNames);

    Map<Marker, List<TranscriptBean>> getAllTranscriptBeans(Pagination pagination);

}
