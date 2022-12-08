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

}
