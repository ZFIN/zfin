package org.zfin.mutant;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GoTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

public class MutantRepositoryTest {

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
     * Check that genotype anh^m149 has background AB.
     */
    @Test
    public void checkBackground() {
        //  genotype with given background: anh^m149
        String zdbID = "ZDB-GENO-980202-397";
        Genotype geno = mutantRepository.getGenotypeByID(zdbID);

        //  background genotype AB
        String bgZdbID = "ZDB-GENO-960809-7";
        Genotype background = mutantRepository.getGenotypeByID(bgZdbID);
        Assert.assertNotNull("Background exists", geno.getBackground());
        Assert.assertEquals("Background AB", background, geno.getBackground());

    }

    @Test
    public void checkMorpholinoRecords() {

        //  ao term: otic placode
        String name = "neural plate";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        List<Morpholino> morphs =
                mutantRepository.getPhenotypeMorhpolinosByAnatomy(ai, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        Assert.assertNotNull("morphs exist", morphs);

    }

    @Test
    public void checkPhenotypeDescriptions(){
        //  ao term: otic placode
        String name = "otic placode";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        PaginationResult<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(ai, true, null);
        Assert.assertNotNull("morphs exist", morphs.getPopulatedResults());

    }

    @Test
    public void checkGoTerms(){
        //  ao term: otic placode
        String name = "ribosome";
        MutantRepository mr = RepositoryFactory.getMutantRepository();
        List<GoTerm> goTerms = mr.getGoTermsByName(name);
        Assert.assertNotNull(goTerms);
        Assert.assertTrue(goTerms.size()>0);

        boolean findKnown = false ;
        for(GoTerm term: goTerms){
            if(term.getName().equals("polysomal ribosome")){
                findKnown = true ; 
            }
        }

        Assert.assertTrue(findKnown);
    }

    @Test
    public void checkQualityTerms(){
        //  ao term: otic placode
        String name = "red brown";
        MutantRepository mr = RepositoryFactory.getMutantRepository();
        List<Term> terms = mr.getQualityTermsByName(name);
        Assert.assertNotNull(terms);
        Assert.assertTrue(terms.size()>0);

        boolean findKnown = false ;
        for(Term term: terms){
            if(term.getName().equals("dark red brown")){
                findKnown = true ;
            }
        }

        Assert.assertTrue(findKnown);
    }


}
