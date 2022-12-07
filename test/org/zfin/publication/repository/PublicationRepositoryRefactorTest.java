package org.zfin.publication.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.publication.Publication;

import java.util.List;
import static org.junit.Assert.*;

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
}
