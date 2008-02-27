package org.zfin.mutant;

import org.hibernate.Session;
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
import org.zfin.mutant.repository.MutantRepository;
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
        List<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(ai, true);
        Assert.assertNotNull("morphs exist", morphs);

/*
        Session session = HibernateUtil.currentSession();
        GenotypeExperiment exper = (GenotypeExperiment) session.load(GenotypeExperiment.class, "ZDB-GENOX-041102-1010");

*/
        for(GenotypeExperiment exp: morphs){
            for(Phenotype pheno: exp.getPhenotypes()){
                System.out.println("Tag: " +pheno.getTag());
                System.out.println("Term: " +pheno.getTerm().getName());

            }
        }

    }

}
