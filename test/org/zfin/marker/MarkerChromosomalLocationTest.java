package org.zfin.marker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.expression.service.ExpressionService;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.MarkerLocation;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.profile.Organization;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

import static org.hamcrest.Matchers.*;
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

        Set<RecordAttribution> publications = genomeLocation.getPublications();
        assertEquals(1, publications.size());

//        fail("are we here?");
//        getMarkerRepository().addDataAliasAttribution(genomeLocation, publication);

//        assertTrue("trivial assert", 1 == 2-1);
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
