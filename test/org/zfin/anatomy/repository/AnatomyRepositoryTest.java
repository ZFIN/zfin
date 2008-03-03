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
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Morpholino;
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
        List<HighQualityProbe> probes = pr.getHighQualityProbeNames(item);
        assertTrue(probes != null);
        assertEquals("10", 5, probes.size());

        int numberOHQProbes = pr.getNumberOfHighQualityProbes(item);
        assertEquals("10", 5, numberOHQProbes);

    }

    @Test
    public void getTotalNumberOfFiguresPerAnatomy(){
        // brain
        String termName = "brain";
        AnatomyItem item = aoRepository.getAnatomyItem(termName);

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        int numOfFigures = pr.getTotalNumberOfFiguresPerAnatomyItem(item);
        //assertEquals(1036, numOfFigures);
        
    }

    @Test
    public void getAnatomyRelationships(){
        String termName = "neural rod";
        AnatomyItem item = aoRepository.getAnatomyItem(termName);

        List<AnatomyRelationship> relatedTerms = item.getAnatomyRelations();
        assertTrue(relatedTerms != null);

    }

    @Test
    public void getAnatomyTermsSearchResult(){
        String searchTerm = "bra";

        List<AnatomyItem> terms = aoRepository.getAnatomyItemsByName(searchTerm);
        assertNotNull(terms);
    }

    @Test
    public void getWildtypeMorpholinos(){
        // String neuralPlateZdbID = "ZDB-ANAT-010921-560";
        AnatomyItem item = aoRepository.getAnatomyItem("neural plate");
        List<GenotypeExperiment> genos = mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(item, true);
        assertTrue(genos != null && genos.size() > 0);
        //assertEquals(2, genos.size());
        for(GenotypeExperiment genotypeExperiment: genos){
            Genotype geno = genotypeExperiment.getGenotype();
            System.out.println(genotypeExperiment.getZdbID());
            System.out.println(geno.getZdbID() + "\r");
        }

        assertTrue(genos.size()> 1);
    }
}
