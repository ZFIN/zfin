package org.zfin.anatomy.repository;

import org.hibernate.SessionFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

public class AnatomyRepositoryTest {

    private static AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    /**
     * Check that synonyms are not of group 'seconday id'
     */
    @Test
    public void getAnatomyTermWithSynomyms() {
        // optic primordium
        String termName = "optic primordium";

        AnatomyItem item = aoRepository.getAnatomyItem(termName);
        assertTrue(item != null);
        Set<AnatomySynonym> syns = item.getSynonyms();
        assertTrue(syns != null);
        // check that none of the synonyms are secondary ids
        for (AnatomySynonym syn : syns) {
            assertEquals(" Not a secondary id", true, syn.getGroup() != AnatomySynonym.Group.SECONDARY_ID);
        }
    }

    @Test
    public void getThisseProbesForBrain() {
        // brain
        String termName = "brain";
        AnatomyItem item = aoRepository.getAnatomyItem(termName);

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        PaginationResult<HighQualityProbe> probeResults = pr.getHighQualityProbeNames(item);
        List<HighQualityProbe> probes = probeResults.getPopulatedResults();
        assertTrue(probes != null);
        assertTrue(probes.size() > 0);

        int numberOHQProbes = probeResults.getTotalCount();
        assertTrue(numberOHQProbes > 0);
        assertTrue(probes.size() == numberOHQProbes);

    }

    @Test
    public void getTotalNumberOfFiguresPerAnatomy() {
        // brain
        String termName = "brain";
        AnatomyItem item = aoRepository.getAnatomyItem(termName);

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        int numOfFigures = pr.getTotalNumberOfFiguresPerAnatomyItem(item);
        //assertEquals(1036, numOfFigures);

    }

    @Test
    public void getAnatomyRelationships() {
        String termName = "neural rod";
        AnatomyItem item = aoRepository.getAnatomyItem(termName);

        List<AnatomyRelationship> relatedTerms = item.getAnatomyRelations();
        assertTrue(relatedTerms != null);

    }

    @Test
    public void getAnatomyTermsSearchResult() {
        String searchTerm = "bra";

        List<AnatomyItem> terms = aoRepository.getAnatomyItemsByName(searchTerm, true);
        assertNotNull(terms);
    }

    @Test
    public void compareWildTypeSelectionToFullForMorpholinos() {
        AnatomyItem item = aoRepository.getAnatomyItem("neural plate");
        PaginationResult<GenotypeExperiment> genosWildtype = mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(item, true, null);
        PaginationResult<GenotypeExperiment> genosNonWildtype = mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(item, false, null);

        assertNotNull(genosWildtype.getPopulatedResults());
        assertNotNull(genosNonWildtype.getPopulatedResults());
        assertNotSame("It is feasible, but unlikely that these will ever be the same", genosWildtype.getTotalCount(), genosNonWildtype.getTotalCount()); // its feasible, but not likely


    }

    @Test
    public void getWildtypeMorpholinos() {
        // String neuralPlateZdbID = "ZDB-ANAT-010921-560";
        AnatomyItem item = aoRepository.getAnatomyItem("neural plate");
        PaginationResult<GenotypeExperiment> genos = mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(item, true, null);
        assertNotNull(genos.getPopulatedResults());
        assertTrue(genos.getPopulatedResults().size() > 1);

        List<GenotypeExperiment> genosList = mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(item, true);
        assertNotNull(genosList);
        assertTrue(genosList.size() > 1);

    }


    /**
     * 1 - find anatomy item term
     * 2 - find anatomy item term by synonym
     * 3 - find anatomy item term not by data alias
     */
    @Test
    public void getAnatomyItemsWithoutDataAlias() {
        // 1 - get by name
        List<AnatomyItem> terms;
        terms = aoRepository.getAnatomyItemsByName("extrascapular", false);
        assertNotNull(terms);
        assertTrue(terms.size() == 1);

        AnatomyItem item = terms.get(0);
        Set<AnatomySynonym> synonyms = item.getSynonyms();
        assertEquals("Should be 1 synonym because filtered secondary", synonyms.size(), 1);

        // 2- get by synonym
        terms = aoRepository.getAnatomyItemsByName("supratemporal", false);
        assertNotNull(terms);
        assertTrue(terms.size() == 2);

        // 3- get by data alias
        terms = aoRepository.getAnatomyItemsByName("413", false);
        assertNotNull(terms);
        assertTrue("Should be no terms for '413'", terms.size() == 0);
    }

}
