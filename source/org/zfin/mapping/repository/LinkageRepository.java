package org.zfin.mapping.repository;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.*;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

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

    /**
     * Retrieves genome location information.
     *
     * @param marker
     * @return
     */
    List<MarkerGenomeLocation> getGenomeLocation(Marker marker);

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

    boolean hasGenomeLocation(Marker gene, GenomeLocation.Source source);

    List<EntityZdbID> getMappedEntitiesByPub(Publication publication);
}
