package org.zfin.mapping;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.Marker;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
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
        assertThat(location, is("13"));

        marker = getMarkerRepository().getMarkerByID("ZDB-SSLP-980528-1241");
        location = MappingService.getChromosomeLocationDisplay(marker);
        assertNotNull(location);

        marker = getMarkerRepository().getMarkerByAbbreviation("rs3728557");
        location = MappingService.getChromosomeLocationDisplay(marker);
        assertNotNull(location);

    }

}
