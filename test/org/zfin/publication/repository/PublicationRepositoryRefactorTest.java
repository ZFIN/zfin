package org.zfin.publication.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;

import java.util.List;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class PublicationRepositoryRefactorTest extends AbstractDatabaseTest {

    @Autowired
    private PublicationRepository publicationRepository;

    @Test
    public void getExpressedGenePublications() {
        List<Publication> pubs = publicationRepository.getExpressedGenePublications("ZDB-GENE-001103-4 ", "ZDB-TERM-100331-8");
        assertNotNull(pubs);

        pubs = publicationRepository.getExpressedGenePublications("ZDB-GENE-001103-4", "ZDB-TERM-100331-8");
        assertNotNull(pubs);
        assertTrue(pubs.size() > 5); //was 10 at the time of this test writing

        //basilar artery -> cyp1a
        pubs = publicationRepository.getExpressedGenePublications("ZDB-GENE-011219-1", "ZDB-TERM-100331-1678");
        assertNotNull(pubs);
        assertTrue(pubs.size() >= 1);
        assertEquals("ZDB-PUB-170818-4", pubs.get(0).getZdbID());
    }

    @Test
    public void getSNPPublicationIDs() {
        Marker marker = getMarkerRepository().getMarker("ZDB-BAC-050218-656");
        List<String> pubs = publicationRepository.getSNPPublicationIDs(marker);
        assertNotNull(pubs);
        assertEquals(1, pubs.size());
        assertEquals("ZDB-PUB-070427-10", pubs.get(0));

    }

    @Test
    public void getHighQualityProbeNames() {
        GenericTerm anatomyTerm = getOntologyRepository().getTermByZdbID("ZDB-TERM-100331-107");
        PaginationResult<HighQualityProbe> hqp = publicationRepository.getHighQualityProbeNames(anatomyTerm, 5);

        int total = hqp.getTotalCount();
        assertEquals(27, total);

        HighQualityProbe firstResult = hqp.getPopulatedResults().get(0);
        Marker firstGene = ((Marker)(firstResult.getGenes().toArray()[0]));
        assertEquals("ZDB-GENE-010328-3", firstGene.getZdbID());
    }

    @Test
    public void getPublications() {
        List<String> testPubs = List.of("ZDB-PUB-180130-17", "ZDB-PUB-190613-8");
        List<Publication> pubs = publicationRepository.getPublications(testPubs);
        assertEquals(2, pubs.size());

        testPubs.containsAll(List.of(pubs.get(0).getZdbID(),pubs.get(1).getZdbID()));

        pubs = publicationRepository.getPublications(null);
        assertEquals(0, pubs.size());
    }

    @Test
    public void publicationExists() {
        boolean exists = publicationRepository.publicationExists("ZDB-PUB-180130-17");
        assertTrue(exists);

        boolean notExists = publicationRepository.publicationExists("BOGUS-IDENTIFIER");
        assertFalse(notExists);
    }

    @Test
    public void getFigureById() {
        Figure fig = publicationRepository.getFigureByID("ZDB-FIG-080617-24"); //has xpat, pheno & AB
        assertNotNull(fig);
    }

    @Test
    public void getPubsForDisplay() {
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-040426-1855").size() > 10);
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-051005-1").size() > 15);
        assertEquals(0, publicationRepository.getPubsForDisplay("ZDB-SSLP-000315-3").size());
    }

    @Test
    public void getAllJournals() {
        List<Journal> journals = publicationRepository.getAllJournals();
        assertTrue( journals.size() > 3000);
    }

    @Test
    public void getJournalByAbbreviation() {
        Journal journal = publicationRepository.getJournalByAbbreviation("Food Chem X");
        assertEquals( "Food chemistry: X", journal.getName());
    }

    @Test
    public void getJournalByPrintIssn() {
        Journal journal = publicationRepository.getJournalByPrintIssn("2590-1575");
        assertEquals( "Food chemistry: X", journal.getName());
    }

    @Test
    public void getJournalByEIssn() {
        Journal journal = publicationRepository.getJournalByEIssn("2041-1723");
        assertEquals( "Nature communications", journal.getName());
    }

    @Test
    public void getNumberAssociatedPublicationsForMarker() {

        Marker m;
        int numberPubs;

        m = getMarkerRepository().getMarkerByID("ZDB-GENE-051005-1");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertTrue(numberPubs > 15);
        assertTrue(numberPubs < 35);
//        assertEquals(28, numberPubs);

        m = getMarkerRepository().getMarkerByAbbreviation("pax6a");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertTrue(numberPubs > 190);
        assertTrue(numberPubs < 500);
//        assertEquals(334, numberPubs);

    }

    @Test
    public void getNumberOfPublicationForPax2aAndMHB() {
        String termName = "midbrain hindbrain boundary";
        OntologyRepository aoRepository = getOntologyRepository();
        GenericTerm item = aoRepository.getTermByName(termName, Ontology.ANATOMY);
        Marker pax2a = getMarkerRepository().getMarkerByAbbreviation("pax2a");

        PaginationResult<Publication> qualityPubs = publicationRepository.getPublicationsWithFigures(pax2a, item);
        assertTrue(qualityPubs != null);
        assertEquals("122 pubs", 122, qualityPubs.getPopulatedResults().size());

    }

    @Test
    public void getPublicationsWithFigures() {
        Marker marker = getMarkerRepository().getMarker("ZDB-GENE-990415-8");
        OntologyRepository aoRepository = getOntologyRepository();
        GenericTerm item = aoRepository.getTermByZdbID("ZDB-TERM-100331-40");

        PaginationResult<Publication> pubs = publicationRepository.getPublicationsWithFigures(marker, item);
        assertEquals("122 pubs", 122, pubs.getPopulatedResults().size());

//        pubs = publicationRepository.getPublicationsWithFigures_New(marker, item);
//        assertEquals("122 pubs", 122, pubs.getPopulatedResults().size());
    }

    @Test
    public void getPublicationsWithFigures2() {
        //tbxta
        Marker marker = getMarkerRepository().getMarker("ZDB-GENE-980526-437");
        OntologyRepository aoRepository = getOntologyRepository();

        //dorsal region
        GenericTerm item = aoRepository.getTermByZdbID("ZDB-TERM-100722-81");

        PaginationResult<Publication> pubs = publicationRepository.getPublicationsWithFigures(marker, item);
        assertEquals("5 pubs", 5, pubs.getPopulatedResults().size());

//        pubs = publicationRepository.getPublicationsWithFigures_New(marker, item);
//        assertEquals("5 pubs", 5, pubs.getPopulatedResults().size());

    }
}
