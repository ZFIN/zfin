package org.zfin.mapping.repository;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.*;
import org.zfin.mapping.importer.AGPEntry;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.sequence.gff.Assembly;

import jakarta.persistence.Tuple;

import java.util.List;
import java.util.TreeSet;

public interface LinkageRepository {

    List<String> getDirectMappedMarkers(Marker marker);

    TreeSet<String> getChromosomeLocations(Marker marker);

    /**
     * Retrieve all mapping panels.
     *
     * @return list of panels.
     */
    List<Panel> getAllPanels();

    /**
     * Retrieve meiotic panels
     *
     * @return
     */
    List<MeioticPanel> getMeioticPanels();

    /**
     * Retrieve  radiation panels
     *
     * @return
     */
    List<RadiationPanel> getRadiationPanels();

    /**
     * Retrieve panel by panel name
     *
     * @param name
     * @return
     */
    Panel getPanelByName(String name);

    /**
     * Retrieve panel by PK id
     *
     * @param panelID
     * @return
     */
    Panel getPanel(String panelID);

    /**
     * Retrieve PanelCount list for a given panel
     *
     * @param panel
     * @return
     */
    List<PanelCount> getPanelCount(Panel panel);

    Panel getPanelByAbbreviation(String abbreviation);

    /**
     * Retreeve mapped markers on mapping panels.
     *
     * @param marker
     * @return
     */
    List<MappedMarker> getMappedMarkers(ZdbID marker);

    /**
     * Retrieve the linkage for a given marker.
     *
     * @param marker
     * @return
     */
    List<Linkage> getLinkagesForMarker(Marker marker);

    /**
     * Retrieve list of mapped clones that contain a given marker
     *
     * @param marker
     * @return
     */
    List<Marker> getMappedClonesContainingGene(Marker marker);

    /**
     * Retrieve mapped marker for given panel, marker and chromosome
     *
     * @param panel
     * @param marker
     * @param lg
     * @return
     */
    List<MappedMarker> getMappedMarkers(Panel panel, ZdbID marker, String lg);

    List<PrimerSet> getPrimerSetList(Marker marker);

    List<Marker> getMarkersEncodedByMarker(Marker marker);

    List<Marker> getMarkersContainedIn(Marker marker);

    /**
     * Retrieve linkage members for a given marker.
     *
     * @param marker
     * @return
     */
    List<LinkageMember> getLinkageMemberForMarker(Marker marker);
    List<FeatureGenomeLocation> getFeatureLocations(Marker marker);

    List<GenomeLocation> getGenericGenomeLocation(ZdbID markerOrFeature);

    /**
     * Retrieves genome location information.
     *
     * @param marker
     * @return
     */
    List<MarkerGenomeLocation> getGenomeLocation(Marker marker);

    List<MarkerGenomeLocation> getGenomeLocationByMarkerAndAssembly(Marker marker, Assembly assembly);

    /**
     * Retrieves genome location information but only includes entries
     * where start and end are not null
     * @param marker
     * @return
     */
    List<MarkerGenomeLocation> getGenomeLocationWithCoordinates(Marker marker);

    /**
     * Retrieves genome location information.
     *
     * @param marker
     * @return
     */
    List<MarkerGenomeLocation> getGenomeLocation(Marker marker, GenomeLocation.Source... sources);

    /**
     * Retrieves genome location information.
     *
     * @param feature
     * @return
     */
    List<FeatureGenomeLocation> getGenomeLocation(Feature feature);

    /**
     * Retrieves genome location information.
     *
     * @param feature
     * @return
     */
    List<FeatureGenomeLocation> getGenomeLocation(Feature feature, GenomeLocation.Source... sources);

    /**
     * Retrieve non-genetic mapping genomic locations.
     *
     * @param marker Marker
     * @return
     */
    List<MarkerGenomeLocation> getPhysicalGenomeLocations(Marker marker);

    /**
     * Retrieve non-genetic mapping genomic locations.
     *
     * @param feature Feature
     * @return
     */
    List<FeatureGenomeLocation> getPhysicalGenomeLocations(Feature feature);

    /**
     * retrieve linkage info for given feature.
     *
     * @param feature
     * @return
     */
    List<LinkageMember> getLinkagesForFeature(Feature feature);

    /**
     * Retrieve ESTs that contain a SNP
     *
     * @param snp
     * @return
     */
    List<Marker> getESTContainingSnp(Marker snp);

    /**
     * Retrieve Gene that contain a SNP
     *
     * @param snp
     * @return
     */
    List<Marker> getGeneContainingSnp(Marker snp);

    /**
     * Retrieve singleton linkage records.
     *
     * @return
     */
    List<SingletonLinkage> getSingletonLinkage(ZdbID zdbID);

    /**
     * Retrieve linkage entity by ID
     *
     * @param linkageID id
     * @return linkage object
     */
    Linkage getLinkage(String linkageID);

    void saveLinkageComment(Linkage linkage, String newComment);

    void saveAGPEntry(AGPEntry entry) ;
    void deleteAllAGPEntries();
    void deleteAllGenomeLocationsBySource(GenomeLocation.Source source);

    void saveMarkerGenomeLocation(MarkerGenomeLocation markerGenomeLocation);

    boolean hasGenomeLocation(Marker gene, GenomeLocation.Source source);

    List<EntityZdbID> getMappedEntitiesByPub(Publication publication);

    /**
     * Every genome location from the given source whose (accession, entity) pairing db_link
     * no longer carries, together with the entity or entities the accession maps to now.
     *
     * <p>Each tuple carries the location's columns plus two derived ones:
     * {@code current_genes}, a comma-separated list of the ZDB IDs the accession maps to now
     * (empty when it maps to none), and {@code would_collide}, whether moving the row onto
     * {@code current_genes} would be refused by uq_sfclg_unique_location. The second is only
     * meaningful when {@code current_genes} names exactly one gene.
     *
     * @param source          value of sfclg_location_source to examine, e.g. NCBILoader
     * @param foreignDbContainerID the db_link container the accessions belong to
     */
    List<Tuple> getDriftedGenomeLocations(String source, String foreignDbContainerID);

    /**
     * Move one marker genome location onto a different gene, by primary key.
     *
     * <p>Throws if the move is refused by a unique constraint; the caller is expected to have
     * established that it would be accepted.
     */
    void reassignMarkerGenomeLocation(long locationID, String geneZdbID);

    /** Delete one marker genome location by primary key. */
    void deleteMarkerGenomeLocation(long locationID);
}
