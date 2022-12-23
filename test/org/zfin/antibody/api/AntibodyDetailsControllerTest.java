package org.zfin.antibody.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.antibody.AntibodyAntigenGeneService;
import org.zfin.framework.ZfinConfiguration;
import org.zfin.infrastructure.PublicationAttribution;

import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("home")
@ContextConfiguration(classes = ZfinConfiguration.class)
public class AntibodyDetailsControllerTest extends AbstractDatabaseTest {

    @Autowired
    private AntibodyAntigenGeneService service;

    @Test
    public void getAntigenGenes() {
        // zn-5
        String zdbID = "ZDB-ATB-081002-19";

        List<MarkerRelationshipPresentation> results = service.getAntigenGenes(zdbID);

//        assertEquals(1, results.size());
        assertTrue(results.size() > 0);

//        JsonResultResponse<MarkerDBLink> links = controller.addAntigenForAntibody(zdbID, formData)
    }


    @Test
    public void getAntigenGenesWithMultiplePubs() {
        String zdbID = "ZDB-ATB-090616-1";

        List<MarkerRelationshipPresentation> results = service.getAntigenGenes(zdbID);

        assertTrue(results.size() > 0);
        Set<String> attributions = results.get(0).getAttributionZdbIDs();
        assertEquals(2, attributions.size());

    }

    @Test
    public void addAntigenGene() {
        // zn-5
        String zdbID = "ZDB-ATB-081002-19";

        int originalCount = service.getAntigenGenes(zdbID).size();
        service.addAntigenGeneForAntibody(zdbID,"alcamb", Set.of("ZDB-PUB-111111-1"));
        int newCount = service.getAntigenGenes(zdbID).size();
        assertEquals(originalCount + 1, newCount);

        Optional<MarkerRelationshipPresentation> results = service.getAntigenGenes(zdbID)
                .stream()
                .filter(gene -> gene.getAbbreviation().equals("alcamb"))
                .findFirst();

        assertTrue(results.isPresent());
        MarkerRelationshipPresentation alcamb = results.get();

        Set<String> attributions = alcamb.getAttributionZdbIDs();
        assertEquals(1, attributions.size());
        assertTrue(attributions.contains("ZDB-PUB-111111-1"));
    }

    @Test
    public void addAntigenGeneWithMultipleAttributions() throws Exception {
        // zn-5
        String zdbID = "ZDB-ATB-081002-19";

        int originalCount = service.getAntigenGenes(zdbID).size();

        //make changes
        service.addAntigenGeneForAntibody(zdbID,"alcamb", Set.of("ZDB-PUB-111111-1", "ZDB-PUB-111110-1"));

        //retrieve results to confirm
        int newCount = service.getAntigenGenes(zdbID).size();
        assertEquals(originalCount + 1, newCount);

        List<String> attributions = service.getPublicationAttributionsForAntibodyAntigenGeneRelationship(zdbID, "alcamb");
        assertEquals(2, attributions.size());

        assertTrue(attributions.contains("ZDB-PUB-111111-1"));
        assertTrue(attributions.contains("ZDB-PUB-111110-1"));
    }

    @Test
    public void getAntigenGeneRelationship() {
        String zdbID = "ZDB-ATB-090616-1";
        MarkerRelationship otofaMarkerRelationship = service.getAntibodyAntigenGeneMarkerRelationship(zdbID, "otofa");


        assertNotNull(otofaMarkerRelationship);
        Set<PublicationAttribution> attributions = otofaMarkerRelationship.getPublications();
        assertEquals(2, attributions.size());
    }

    @Test
    public void updateAntigenGeneWithChangedAttribution() throws Exception {
        // zn-5
        String zn5zdbID = "ZDB-ATB-081002-19";

        //make changes
        service.addAntigenGeneForAntibody(zn5zdbID,"alcamb", Set.of("ZDB-PUB-111111-1"));
        MarkerRelationship alcambMarkerRelationship = service.getAntibodyAntigenGeneMarkerRelationship(zn5zdbID, "alcamb");

        List<String> attributions = alcambMarkerRelationship.getPublications().stream().map(PublicationAttribution::getSourceZdbID).toList();
        assertEquals(1, attributions.size());
        assertEquals("ZDB-PUB-111111-1", attributions.get(0));

        service.updateAntigenGenePublicationsForAntibody(zn5zdbID, "alcamb", Set.of("ZDB-PUB-111110-1"));

        List<String> attributionsFetchedAgain = service.getPublicationAttributionsForAntibodyAntigenGeneRelationship(zn5zdbID, "alcamb");

        assertEquals(1, attributionsFetchedAgain.size());
        assertEquals("ZDB-PUB-111110-1", attributionsFetchedAgain.get(0));
    }

    @Test
    public void updateAntigenGeneWithChangedGeneAndAttribution() throws Exception {
        // zn-5
        String zn5zdbID = "ZDB-ATB-081002-19";

        //make changes
        MarkerRelationship alcambMarkerRelationship = service.addAntigenGeneForAntibody(zn5zdbID, "alcamb", Set.of("ZDB-PUB-111111-1"));

        List<String> attributions = alcambMarkerRelationship.getPublications().stream().map(PublicationAttribution::getSourceZdbID).toList();
        assertEquals(1, attributions.size());
        assertEquals("ZDB-PUB-111111-1", attributions.get(0));

        service.updateAntigenGeneForAntibody(alcambMarkerRelationship.getZdbID(), "calca", Set.of("ZDB-PUB-111110-1"));

        //should throw exception actually
        try {
            service.getPublicationAttributionsForAntibodyAntigenGeneRelationship(zn5zdbID, "alcamb");
            fail("should throw exception here");
        } catch (Exception e) {
            assertTrue("Ignore this catch block", true);
        }

        //new relationship
        List<String> newAttributionsFetchedAgain = service.getPublicationAttributionsForAntibodyAntigenGeneRelationship(zn5zdbID, "calca");
        assertEquals(1, newAttributionsFetchedAgain.size());
        assertEquals("ZDB-PUB-111110-1", newAttributionsFetchedAgain.get(0));
    }

}