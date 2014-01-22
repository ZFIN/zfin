package org.zfin.marker.repository;

import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.marker.Talen;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Orthologue;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;

import java.util.*;

public interface MarkerRepository {
    Marker getMarker(Marker marker);

    Marker getMarkerByID(String zdbID);

    SNP getSNPByID(String zdbID);

    Marker getMarkerOrReplacedByID(String zdbID);

    Marker getGeneByID(String zdbID);

    Clone getCloneById(String zdbID);

    Transcript getTranscriptByZdbID(String zdbID);

    Transcript getTranscriptByName(String name);

    Transcript getTranscriptByVegaID(String vegaID);

    List<String> getTranscriptTypes();

    Marker getMarkerByAbbreviationIgnoreCase(String abbreviation);

    Marker getMarkerByAbbreviation(String abbreviation);

    SequenceTargetingReagent getMorpholinoByAbbreviation(String abbreviation);

    Marker getMarkerByName(String name);

    //Todo: should this move to another class?

    MarkerRelationship getMarkerRelationship(Marker firstMarker,
                                             Marker secondMarker,
                                             MarkerRelationship.Type type);

    List<Marker> getMarkersByAbbreviation(String name);

    List<Marker> getGenesByAbbreviation(String name);

    Marker getGeneByAbbreviation(String name);

    MarkerRelationship getMarkerRelationshipByID(String zdbID);

    MarkerAlias getSpecificDataAlias(Marker marker, String alias);

    TreeSet<String> getLG(Marker marker);

    MarkerRelationship addMarkerRelationship(MarkerRelationship mrel, String attributionZdbID);

    void addSmallSegmentToGene(Marker gene, Marker segment, String attributionZdbID);

    DataNote addMarkerDataNote(Marker marker, String note, Person curator);

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
    void createOrUpdateOrthologyExternalNote(Marker gene, String note);

    void editAntibodyExternalNote(String notezdbid, String note);

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

    /**
     * Delete a marker relationship
     *
     * @param mrel
     */
    void deleteMarkerRelationship(MarkerRelationship mrel);

    void addDataAliasAttribution(DataAlias alias, Publication attribution, Marker marker);

    void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution, Marker marker);

    /**
     * Add a publication to a given marker: Attribution.
     *
     * @param marker      valid marker object
     * @param publication publication object
     */
    void addMarkerPub(Marker marker, Publication publication);

    MarkerDBLink getDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb);

    DBLink addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID);

    void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accessionNumber);

    MarkerHistory getLastMarkerHistory(Marker marker, MarkerHistory.Event event);

    MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason, MarkerAlias markerAlias);

    MarkerType getMarkerTypeByName(String name);

    MarkerTypeGroup getMarkerTypeGroupByName(String name);

    void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason);

    List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring);

    MarkerFamilyName getMarkerFamilyName(String name);

    void runMarkerNameFastSearchUpdate(Marker marker);

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
    List<Marker> getMarkersByAbbreviationGroupAndAttribution(String name, Marker.TypeGroup markerType, String pubZdbId);

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

    /**
     * Create a gene for a given Morpholino which is targeting it.
     *
     * @param sequenceTargetingReagent valid Morpholino of Marker object.
     * @return the target gene of the Morpholino
     */
    List<Marker> getTargetGenesForMorpholino(SequenceTargetingReagent sequenceTargetingReagent);

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

    String getVariantForSnp(String zdbID);

    List<STRMarkerSequence> getSTRMarkerSequences(Marker marker);
    List<SNPMarkerSequence> getSNPMarkerSequences(SNP marker);

    List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayExcludeTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types);

    List<Marker> getMarkersByAlias(String key);

    List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayForTypes(Marker construct, boolean b, MarkerRelationship.Type... types);

    List<LinkDisplay> getMarkerDBLinksFast(Marker marker, DisplayGroup.GroupName groupName);

    List<MarkerRelationshipPresentation> getRelatedMarkerDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types);

    List<GeneProductsBean> getGeneProducts(String zdbID);

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
     * Retrieve list of genotypes being created with a TALEN or CRISPR
     *
     * @param streagentId TALEN or CRISPR ID
     * @return list of genotypes
     */
    List<Genotype> getTALENorCRISPRcreatedGenotypes(String streagentId);

    SequenceTargetingReagent getSequenceTargetingReagent(String markerID);

    List<Marker> getConstructsForGene(Marker gene);

    Genotype getStrainForTranscript(String zdbID);

    List<LinkDisplay> getVegaGeneDBLinksTranscript(Marker gene, DisplayGroup.GroupName summaryPage);

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

    List<OmimPhenotype> getOmimPhenotypesByGene(Marker zebrafishGene);


    List<SupplierLookupEntry> getSupplierNamesForString(String lookupString);

    List<TargetGeneLookupEntry> getTargetGenesWithNoTranscriptForString(String lookupString);
}
