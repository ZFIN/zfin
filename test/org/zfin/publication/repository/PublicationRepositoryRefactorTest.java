package org.zfin.publication.repository;

import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.ImageStage;
import org.zfin.feature.Feature;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.presentation.DashboardPublicationBean;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class PublicationRepositoryRefactorTest extends AbstractDatabaseTest {

    @Autowired
    private PublicationRepository publicationRepository;

    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private static FigureViewService figureViewService = new FigureViewService();

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



}

