package org.zfin.mutant;

import static junit.framework.Assert.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.AnatomyPhenotype;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GoTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MutantRepositoryTest {

    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static AnatomyRepository anatRepository = RepositoryFactory.getAnatomyRepository();

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
        assertNotNull("Background exists", geno.getBackground());
        assertEquals("Background AB", background, geno.getBackground());

    }

    @Test
    public void checkMorpholinoRecords() {

        //  ao term: otic placode
        String name = "neural plate";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        List<Morpholino> morphs =
                mutantRepository.getPhenotypeMorhpolinosByAnatomy(ai, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        assertNotNull("morphs exist", morphs);

    }

    @Test
    public void checkPhenotypeDescriptions() {
        //  ao term: otic placode
        String name = "otic placode";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        PaginationResult<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(ai, true, null);
        assertNotNull("morphs exist", morphs.getPopulatedResults());

    }

    @Test
    public void checkGoTerms() {
        //  ao term: otic placode
        String name = "ribosome";
        List<GoTerm> goTerms = mutantRepository.getGoTermsByName(name);
        assertNotNull(goTerms);
        assertTrue(goTerms.size() > 0);

        boolean findKnown = false;
        for (GoTerm term : goTerms) {
            if (term.getName().equals("polysomal ribosome")) {
                findKnown = true;
            }
        }

        assertTrue(findKnown);
    }

    @Test
    public void checkQualityTerms() {
        //  ao term: otic placode
        String name = "red brown";
        List<Term> terms = mutantRepository.getQualityTermsByName(name);
        assertNotNull(terms);
        assertTrue(terms.size() > 0);

        boolean findKnown = false;
        for (Term term : terms) {
            if (term.getName().equals("dark red brown")) {
                findKnown = true;
            }
        }

        Assert.assertTrue(findKnown);
    }


    @Test
    public void checkForPatoRecord() {
        String expressionExperimentID = "ZDB-XPAT-081003-1";
        String genoxID = "ZDB-GENOX-041102-700";
        String figureID = "ZDB-FIG-050720-1";
        String startID = "ZDB-STAGE-010723-4";
        String endID = "ZDB-STAGE-010723-4";

        boolean patoExists = mutantRepository.isPatoExists(genoxID, figureID, startID, endID);
        assertTrue(!patoExists);

    }


    @Test
    public void createPatoRecord() {
        String expressionExperimentID = "ZDB-XPAT-081003-1";
        String genoxID = "ZDB-GENOX-041102-700";
        String figureID = "ZDB-FIG-050720-1";
        String startID = "ZDB-STAGE-010723-4";
        String endID = "ZDB-STAGE-010723-4";
        String pubZdbId = "ZDB-PUB-990507-16";

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {

            ExpressionExperiment expressionExperiment = expressionRepository.getExpressionExperiment(expressionExperimentID);
            Figure figure = pubRepository.getFigureByID(figureID);
            DevelopmentStage start = anatRepository.getStageByID(startID);
            DevelopmentStage end = anatRepository.getStageByID(endID);
            Publication pub = pubRepository.getPublication(pubZdbId);
            Phenotype pheno = new AnatomyPhenotype();
            pheno.setPublication(pub);
            pheno.setEndStage(end);
            pheno.setStartStage(start);
            pheno.setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
            Set<Figure> figures = new HashSet<Figure>();
            figures.add(figure);
            pheno.setFigures(figures);

            mutantRepository.createDefaultPhenotype(pheno);
            assertNotNull(pheno.getZdbID());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }


    }

    @Test
    public void getQualityTerm() {

        Term term = mutantRepository.getQualityTermByName(Term.QUALITY);
        assertTrue(term != null);

    }

    @Test
    public void retrieveAllWildtypeGenotypes() {
        MutantRepository mr = RepositoryFactory.getMutantRepository();
        List<Genotype> terms = mr.getAllWildtypeGenotypes();
        assertNotNull(terms);
    }


}
