package org.zfin.marker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.MarkerLocation;
import org.zfin.publication.Publication;

import java.util.*;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * Tests for org.zfin.marker.service.MarkerService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class MarkerChromosomalLocationTest extends AbstractDatabaseTest {

    private Logger logger = LogManager.getLogger(MarkerChromosomalLocationTest.class);

    @Test
    public void citationTest() {
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-210814-8");
        Marker marker = getMarkerRepository().getMarker("ZDB-ENHANCER-180108-1");
        List<MarkerLocation> genomeLocations = getMarkerRepository().getMarkerLocation(marker.getZdbID());

        assertEquals(genomeLocations.size(),1);
        MarkerLocation genomeLocation = genomeLocations.get(0);
        assertEquals(15_243_487, (long)genomeLocation.getStartLocation());

        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication);

        Set<RecordAttribution> publications = genomeLocation.getReferences();
        assertEquals(1, publications.size());
    }

    @Test
    public void multipleCitationTest() {
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-210814-8");
        Publication publication2 = getPublicationRepository().getPublication("ZDB-PUB-200719-14");
        Marker marker = getMarkerRepository().getMarker("ZDB-ENHANCER-180108-1");
        List<MarkerLocation> genomeLocations = getMarkerRepository().getMarkerLocation(marker.getZdbID());
        MarkerLocation genomeLocation = genomeLocations.get(0);

        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication);
        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication2);

        Set<RecordAttribution> publications = genomeLocation.getReferences();
        assertEquals(2, publications.size());
    }


    @Test
    public void synchronizeCitationsTest() {
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-210814-8");
        Publication publication2 = getPublicationRepository().getPublication("ZDB-PUB-200719-14");
        Publication publication3 = getPublicationRepository().getPublication("ZDB-PUB-200719-15");
        Marker marker = getMarkerRepository().getMarker("ZDB-ENHANCER-180108-1");
        List<MarkerLocation> genomeLocations = getMarkerRepository().getMarkerLocation(marker.getZdbID());
        MarkerLocation genomeLocation = genomeLocations.get(0);

        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication);
        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication2);
        getMarkerRepository().addGenomeLocationAttribution(genomeLocation, publication3);

        Set<String> publicationsToSync = Set.of(publication2.getZdbID(), publication3.getZdbID());
        getMarkerRepository().synchronizeGenomeLocationAttributions(genomeLocation, publicationsToSync);

        Set<RecordAttribution> publications = genomeLocation.getReferences();
        assertEquals(2, publications.size());
    }

    @Test
    public void trivialTest() {
        assertTrue("trivial assert", 1 == 2-1);
    }

    @Test
    public void trivialFailingTest() {
        assertTrue("trivial failing assert", 1 == 2);
    }


}
