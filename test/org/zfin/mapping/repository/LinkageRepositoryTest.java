package org.zfin.mapping.repository;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.*;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;

public class LinkageRepositoryTest extends AbstractDatabaseTest {

    private LinkageRepository linkageRepository = getLinkageRepository();

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void findLinkageForMappedRNAClones() {
        Session session = currentSession();

        Clone clone = (Clone) session.get(Clone.class, "ZDB-CDNA-040425-812");
        List<String> mappedMarkerList = linkageRepository.getDirectMappedMarkers(clone);
        assertNotNull(mappedMarkerList);
        assertEquals(1, mappedMarkerList.size());
        assertEquals("8", mappedMarkerList.get(0));

        clone = (Clone) session.get(Clone.class, "ZDB-EST-020816-240");
        mappedMarkerList = linkageRepository.getDirectMappedMarkers(clone);
        assertNotNull(mappedMarkerList);
        assertEquals(2, mappedMarkerList.size());
        assertEquals("16", mappedMarkerList.get(0));
        assertEquals("17", mappedMarkerList.get(1));
    }

    @Test
    public void findLinkageForUnMappedRNAClones() {
        Session session = currentSession();
        Clone clone = (Clone) session.get(Clone.class, "ZDB-EST-000329-419");
        Set<String> lgs = linkageRepository.getChromosomeLocations(clone);
        assertNotNull(lgs);
        assertEquals(1, lgs.size());
        assertEquals("16", lgs.iterator().next());
    }

    @Test
    public void findLinkageForGene() {
        Session session = currentSession();
        // pax2a
        Marker gene = (Marker) session.get(Marker.class, "ZDB-GENE-990415-8");
        assertNotNull(gene);
        Set<String> lgs = linkageRepository.getChromosomeLocations(gene);
        assertNotNull(lgs);
    }

    @Test
    public void findLinkageForMappedClones() {
        Session session = currentSession();

        Clone clone = (Clone) session.get(Clone.class, "ZDB-BAC-030616-10");
        String display = MappingService.getChromosomeLocationDisplay(clone);
        assertNotNull(display);
        assertEquals("14", display);
    }

    @Test
    public void displayScaffoldingLocation() {
        Session session = currentSession();

        Marker gene = (Marker) session.get(Marker.class, "ZDB-GENE-070831-1");
        String display = MappingService.getChromosomeLocationDisplay(gene);
        assertNotNull(display);
        assertTrue(display.startsWith("15"));
    }

    @Test
    public void getLinkage() {
        Session session = currentSession();
        Linkage linkage = (Linkage) session.get(Linkage.class, "ZDB-LINK-090608-1");
        assertNotNull(linkage);
    }

    @Test
    public void testGetLG() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisiting genes which might be merged
        LinkageRepository linkageRepository = getLinkageRepository();
        Marker marker1 = (Marker) currentSession().get(Marker.class, "ZDB-EST-000426-1181");
        assertTrue("marker lg list contains all self panel mappings", linkageRepository.getChromosomeLocations(marker1).contains("13") && linkageRepository.getChromosomeLocations(marker1).contains("23"));
        Marker marker2 = (Marker) currentSession().get(Marker.class, "ZDB-GENE-990415-72");
        assertTrue("gene lg list contains its est's panel mapping", linkageRepository.getChromosomeLocations(marker2).contains("23"));
        assertFalse("gene lg list contains no bogus mapping", linkageRepository.getChromosomeLocations(marker2).contains("1"));
        Marker marker3 = (Marker) currentSession().get(Marker.class, "ZDB-GENE-060526-178");
        assertTrue("gene lg list contains clone's panel mapping", linkageRepository.getChromosomeLocations(marker3).contains("13"));

        Marker marker4 = (Marker) currentSession().get(Marker.class, "ZDB-RAPD-980526-288");
        assertTrue("marker lg list contains self linkage group mapping", linkageRepository.getChromosomeLocations(marker4).contains("12"));
        Marker marker5 = (Marker) currentSession().get(Marker.class, "ZDB-BAC-030616-45");
        assertTrue("marker lg list contains linkage mapping of contained marker/segment", linkageRepository.getChromosomeLocations(marker5).contains("9"));
        Marker marker6 = (Marker) currentSession().get(Marker.class, "ZDB-GENE-061013-119");
        assertTrue("gene lg list contains clone's linkage mapping", linkageRepository.getChromosomeLocations(marker6).contains("19"));

        Marker marker7 = (Marker) currentSession().get(Marker.class, "ZDB-GENE-070117-36");
        assertTrue("gene lg list contains allele's linkage group mapping", linkageRepository.getChromosomeLocations(marker7).contains("23"));

        Marker marker8 = (Marker) currentSession().get(Marker.class, "ZDB-GENE-030131-5474");
        assertTrue("gene lg list contains linkage group mapping from clone", linkageRepository.getChromosomeLocations(marker8).contains("10"));

      /*  Marker marker8 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-070117-2287");
        assertTrue("marker lg list contains allele's panel mapping", linkageRepository.getChromosomeLocations(marker8).contains("7"));*/
    }

    @Test
    public void getAllPanels() {
        List<Panel> panels = getLinkageRepository().getAllPanels();
        assertNotNull(panels);
        assertTrue(panels.size() >= 7);
        List<MeioticPanel> mPanels = getLinkageRepository().getMeioticPanels();
        assertNotNull(mPanels);
        assertTrue(mPanels.size() >= 5);
        List<RadiationPanel> rPanels = getLinkageRepository().getRadiationPanels();
        assertNotNull(rPanels);
        assertTrue(rPanels.size() >= 2);
    }

    @Test
    public void getPanelDetail() {
        Panel panel = getLinkageRepository().getPanelByName("Heat Shock");
        assertNotNull(panel);
        Panel panel1 = getLinkageRepository().getPanel("ZDB-REFCROSS-980526-5");
        assertNotNull(panel);
        assertEquals("Mother of Pearl", panel1.getName());
    }

    @Test
    public void getPanelCount() {
        Panel panel = getLinkageRepository().getPanelByName("Heat Shock");
        List<PanelCount> panelList = getLinkageRepository().getPanelCount(panel);
        assertNotNull(panelList);
    }

    @Test
    public void getMappedMarker() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("pax2a");
        List<MappedMarker> mappedMarkers = getLinkageRepository().getMappedMarkers(marker);
        assertNotNull(mappedMarkers);
    }

    @Test
    public void getLinkedMarkersFromGene() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("pax2a");
        List<LinkageMember> linkageSet = getLinkageRepository().getLinkageMemberForMarker(marker);
        assertNotNull(linkageSet);
        LinkageMember linkageMember = linkageSet.get(0);
        assertEquals("cM", linkageMember.getMetric());
        assertEquals("13", linkageMember.getLinkage().getChromosome());
    }

    @Test
    public void getLinkedMarkersFromSSLP() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("z1059");
        List<LinkageMember> linkageSet = getLinkageRepository().getLinkageMemberForMarker(marker);
        Collections.sort(linkageSet);
        assertNotNull(linkageSet);
        LinkageMember linkageMember = linkageSet.get(0);
        assertEquals("7", linkageMember.getLinkage().getChromosome());
    }

    @Test
    public void getGenomeLocation() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("DKEY-76A19");
        List<MarkerGenomeLocation> genomeLocationList = getLinkageRepository().getGenomeLocation(marker);
        assertNotNull(genomeLocationList);

        List<GenomeLocation> genomeLocationList1 = getLinkageRepository().getPhysicalGenomeLocations(marker);
        assertNotNull(genomeLocationList1);

        marker = getMarkerRepository().getMarkerByAbbreviation("ahr2");
        genomeLocationList = getLinkageRepository().getGenomeLocation(marker);
        assertNotNull(genomeLocationList);
    }

    @Test
    public void getGenomeLocationForFeature() {
        Feature feature = getFeatureRepository().getFeatureByAbbreviation("vu304");
        List<FeatureGenomeLocation> genomeLocationList = getLinkageRepository().getGenomeLocation(feature);
        assertNotNull(genomeLocationList);

        feature = getFeatureRepository().getFeatureByAbbreviation("b16");
        genomeLocationList = getLinkageRepository().getGenomeLocation(feature);
        assertNotNull(genomeLocationList);
    }

    @Test
    public void getLinkedMarkersFromFeature() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("bmpr1aa");
        List<Linkage> linkage = getLinkageRepository().getLinkagesForMarker(marker);
        assertNotNull(linkage);
    }

    @Test
    public void getMappedMarkersFromFeature() {
        Feature feature = getFeatureRepository().getFeatureByAbbreviation("ty118b");
        List<MappedMarker> markerList = getLinkageRepository().getMappedMarkers(feature);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);
    }

    @Test
    public void getMappedMarkerEncodedByMarker() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("fgf8a");
        List<Marker> markerList = getLinkageRepository().getMarkersEncodedByMarker(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);
    }

    @Test
    public void getEstContainingSNP() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("rs3729447");
        List<Marker> markerList = getLinkageRepository().getESTContainingSnp(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);
    }

    @Test
    public void getGeneContainingSNP() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("rs3729447");
        List<Marker> markerList = getLinkageRepository().getGeneContainingSnp(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);

    }

    /**
     * Clones that are linked to a given gene (pax2a) and either a direct mapped_marker or
     * a record in the linkage_member table
     */
    @Test
    public void getMappedClonesByMarker() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("opn1mw1");
        List<Marker> markerList = getLinkageRepository().getMappedClonesContainingGene(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);

        marker = getMarkerRepository().getMarkerByAbbreviation("pax2a");
        markerList = getLinkageRepository().getMappedClonesContainingGene(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);

        marker = getMarkerRepository().getMarkerByAbbreviation("fgf8a");
        markerList = getLinkageRepository().getMappedClonesContainingGene(marker);
        assertNotNull(markerList);
        assertTrue(markerList.size() > 0);
    }


    @Test
    public void getMappedMarkers() {
        Panel panel = getLinkageRepository().getPanelByAbbreviation("LN54");
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("fgf8a");
        String lg = "13";
        List<MappedMarker> mmarkerList = getLinkageRepository().getMappedMarkers(panel, marker, lg);
        assertNotNull(mmarkerList);
        assertEquals(1, mmarkerList.size());
        mmarkerList = getLinkageRepository().getMappedMarkers(panel, null, lg);
        assertNotNull(mmarkerList);
        assertTrue(mmarkerList.size() > 4);
    }

    @Test
    public void getMappedFeatures() {
        Panel panel = getLinkageRepository().getPanelByAbbreviation("MGH");
        Feature marker = getFeatureRepository().getFeatureByAbbreviation("ty118b");
        String lg = "1";
        List<MappedMarker> mmarkerList = getLinkageRepository().getMappedMarkers(panel, marker, lg);
        assertNotNull(mmarkerList);
        assertEquals(1, mmarkerList.size());
        mmarkerList = getLinkageRepository().getMappedMarkers(panel, null, lg);
        assertNotNull(mmarkerList);
        assertTrue(mmarkerList.size() > 4);
    }

    @Test
    public void getClonesContainGene() {

        Marker marker = getMarkerRepository().getMarkerByID("ZDB-BAC-030616-1");
        List<Marker> list = getLinkageRepository().getMarkersContainedIn(marker);
        assertNotNull(list);
    }

    @Test
    public void getCloneLocation() {
        Marker marker = getMarkerRepository().getMarkerByID("ZDB-BAC-050218-2519");
        List<GenomeLocation> list = getLinkageRepository().getPhysicalGenomeLocations(marker);
        assertNotNull(list);
    }

    @Test
    public void getSingletonFeatureLinkage() {
        Feature feature = getFeatureRepository().getFeatureByID("ZDB-ALT-980203-605");
        List<SingletonLinkage> singletonLinkage = getLinkageRepository().getSingletonLinkage(feature);
        assertNotNull(singletonLinkage);
        assertEquals("12", singletonLinkage.get(0).getLinkage().getChromosome());

        Marker marker = getMarkerRepository().getMarkerByID("ZDB-BAC-050218-1912");
        singletonLinkage = getLinkageRepository().getSingletonLinkage(marker);
        assertNotNull(singletonLinkage);
        assertEquals("13", singletonLinkage.get(0).getLinkage().getChromosome());
    }

    @Test
    public void getLinkageInfo() {

        // pax2a linkage
        Linkage linkage = getLinkageRepository().getLinkage("ZDB-LINK-050822-1");
        assertNotNull(linkage);
        assertEquals("13", linkage.getChromosome());
    }

    @Test
    public void hasGenomeLocationInfo() {
        // pax2a
        Marker marker = getMarkerRepository().getMarkerByID("ZDB-GENE-990415-8");
        boolean hasLocation = getLinkageRepository().hasGenomeLocation(marker, GenomeLocation.Source.ENSEMBL);
        assertTrue(hasLocation);
    }
}
