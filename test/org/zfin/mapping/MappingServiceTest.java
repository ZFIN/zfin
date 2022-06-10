package org.zfin.mapping;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.Feature;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.ChromosomalLocationBean;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getLinkageRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class MappingServiceTest extends AbstractDatabaseTest {

    @Test
    public void getPanelMap() {
        Panel panel = getLinkageRepository().getPanelByName("Heat Shock");
        Map<String, Map<String, Long>> map = MappingService.getChromosomePanelCountMap(panel);
        assertNotNull(map);
        assertEquals(25, map.size());
    }

    @Test
    public void getZMAPPanel() {
        Panel panel = getLinkageRepository().getPanelByAbbreviation("ZMAP");
        Map<String, Map<String, Long>> map = MappingService.getChromosomePanelCountMap(panel);
        assertNotNull(map);
        assertEquals(25, map.size());
    }

    @Test
    public void getPanelMapStatistics() {
        Panel panel = getLinkageRepository().getPanelByName("Heat Shock");
        Map<String, Long> statisticsMap = MappingService.getStatisticMap(panel);
        assertNotNull(statisticsMap);
    }

    @Test
    public void getPanelTotalNumber() {
        Panel panel = getLinkageRepository().getPanelByName("Heat Shock");
        long totalNumber = MappingService.getTotalNumberOfMarker(panel);
        assertTrue(totalNumber > 3500);
    }

    @Test
    public void getGenomeLocation() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("pax2a");
        String location = MappingService.getChromosomeLocationDisplay(marker);
        assertNotNull(location);
        assertEquals(location,"13");

        marker = getMarkerRepository().getMarkerByID("ZDB-SSLP-980528-1241");
        location = MappingService.getChromosomeLocationDisplay(marker);
        assertNotNull(location);

        marker = getMarkerRepository().getMarkerByAbbreviation("rs3728557");
        location = MappingService.getChromosomeLocationDisplay(marker);
        assertNotNull(location);

        Feature feature=getFeatureRepository().getFeatureByID("ZDB-ALT-100505-3");
        FeatureLocation fl=getFeatureRepository().getLocationByFeature(feature);
        assertNotNull(fl);
    }

    @Test
    public void getMarkerLocation() {
        Marker marker = getMarkerRepository().getMarker("ZDB-ENHANCER-190916-7");
        List<MarkerLocation> initialMarkerLocations = getMarkerRepository().getMarkerLocation(marker.getZdbID());
        int initialSize = initialMarkerLocations.size();

        ChromosomalLocationBean clb = new ChromosomalLocationBean();
        clb.setAssembly("GRCz11");
        clb.setChromosome("1");
        clb.setStartLocation(2);
        clb.setEndLocation(3);
        clb.setLocationEvidence("IC");
        clb.setEntityID(marker.getZdbID());

        MarkerLocation markerLocation = new MarkerLocation();
        markerLocation.setFieldsByChromosomalLocationBean(clb);

        getMarkerRepository().addMarkerLocation(markerLocation);

        List<MarkerLocation> retrievedMarkerLocations = getMarkerRepository().getMarkerLocation("ZDB-ENHANCER-190916-7");
        assertEquals(retrievedMarkerLocations.size(), initialSize + 1);

        MarkerLocation retrievedMarkerLocation = retrievedMarkerLocations.get(initialSize);

        assertEquals(retrievedMarkerLocation.getAssembly(), clb.getAssembly());
        assertEquals(retrievedMarkerLocation.getStartLocation(), clb.getStartLocation());
        assertEquals(retrievedMarkerLocation.getEndLocation(), clb.getEndLocation());

    }


}
