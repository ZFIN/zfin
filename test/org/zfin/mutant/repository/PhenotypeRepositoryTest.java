package org.zfin.mutant.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.MutantFigureStage;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.*;
import static org.junit.Assert.fail;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * Test class for PhenotypeRepository.
 */
@SuppressWarnings({"SuppressionAnnotation", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class PhenotypeRepositoryTest {

    private static PhenotypeRepository phenotypeRep = RepositoryFactory.getPhenotypeRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
    }


    /**
     * phenotype structure pile for  ZDB-PUB-970210-18
     */
    @Test
    public void retrievePhenotypeStructures() {
        //String pubID = "ZDB-PUB-970210-18";
        String pubID = "ZDB-PUB-961014-496";

        List<PhenotypeStructure> structures = phenotypeRep.retrievePhenotypeStructures(pubID);
        assertNotNull(structures);
        for (PhenotypeStructure structure : structures) {
            System.out.println(structure.getSuperterm().getTermName());
            System.out.println(structure.getQuality().getTermName());
        }
    }

    @SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion"})
    @Test
    public void checkPhenotypeStructureExists() {
        String publicationID = "ZDB-PUB-970210-18";
        TermDTO superterm = createSuperterm("thalamus", OntologyDTO.ANATOMY);
        TermDTO quality = createSuperterm("degenerate", OntologyDTO.QUALITY);
        PhenotypeTermDTO term = new PhenotypeTermDTO();
        term.setSuperterm(superterm);
        term.setQuality(quality);
        boolean exists = phenotypeRep.isPhenotypePileStructureExists(term, publicationID);
        assertTrue(exists);
    }

    @Test
    public void retrievePhenotypeStructure() {
        String zdbID = "ZDB-API-091119-53";
        PhenotypeStructure structure = phenotypeRep.getPhenotypeStructureByID(zdbID);
        assertNotNull(structure);
        assertEquals("ventricular system", structure.getSuperterm().getTermName());

    }

    @Test
    public void retrieveMutantExpressions() {
        String publicationID = "ZDB-PUB-970210-18";
        List<MutantFigureStage> structure = phenotypeRep.getMutantExpressionsByFigureFish(publicationID, null, null, null);
        assertNotNull(structure);

    }

    @Test
    public void retrieveMultiMutantExpressions() {
        String publicationID = "ZDB-PUB-090731-2";
        List<MutantFigureStage> structure = phenotypeRep.getMutantExpressionsByFigureFish(publicationID, null, null, null);
        assertNotNull(structure);

    }

    @Test
    public void retrieveSingleMutantExpressions() {
        String publicationID = "ZDB-PUB-090731-2";
        String genotypeID = "ZDB-GENO-980202-327";
        String figureID = "ZDB-FIG-091113-10";
        String startID = "ZDB-STAGE-010723-27";
        MutantFigureStage structure = phenotypeRep.getMutant(genotypeID, figureID, startID, startID, publicationID);
        assertNotNull(structure);

    }

    @Test
    public void retrievePhenotypePileStructure() {
        String patoID = "ZDB-API-091119-60";
        PhenotypeStructure structure = phenotypeRep.getPhenotypePileStructure(patoID);
        assertNotNull(structure);
        Term superTerm = structure.getSuperterm();
        assertNotNull(superTerm);
        assertEquals("brain", superTerm.getTermName());
        assertEquals("zebrafish_anatomy", superTerm.getOntology().getOntologyName());

    }

    @Test
    public void getGoPhenotype() {
        // this phenotype has a single go term as a superterm.
        String patoID = "ZDB-APATO-100114-44";
        Session session = HibernateUtil.currentSession();
        Phenotype phenotype = (Phenotype) session.get(Phenotype.class, patoID);
        assertNotNull(phenotype);
    }

    @Test
    public void getDistinctPhenotypePileStructures() {
        // Johnson et al. 1995
        String publicationID = "ZDB-PUB-961014-496";
        List<Phenotype> phenotypes = getPhenotypeRepository().getAllPhenotypes(publicationID);
        assertNotNull(phenotypes);
    }

    @Test
    public void checkIfPhenotypeStructureExists() {
        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setTag(Phenotype.Tag.ABNORMAL);
        boolean exists = getPhenotypeRepository().isPhenotypeStructureOnPile(structure);
        assertTrue(exists);
    }

    @Test
    public void createPhenotypePile() {
        // Johnson et al. 1995
        String publicationID = "ZDB-PUB-961014-496";

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            getPhenotypeRepository().createPhenotypePile(publicationID);
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
            DevelopmentStage start = anatomyRepository.getStageByID(startID);
            DevelopmentStage end = anatomyRepository.getStageByID(endID);
            Publication pub = pubRepository.getPublication(pubZdbId);
            Phenotype phenotype = new Phenotype();
            phenotype.setPublication(pub);
            phenotype.setEndStage(end);
            phenotype.setStartStage(start);
            phenotype.setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
            Set<Figure> figures = new HashSet<Figure>(1);
            figures.add(figure);
            phenotype.setFigures(figures);

            phenotypeRep.createDefaultPhenotype(phenotype);
            assertNotNull(phenotype.getZdbID());
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


    private TermDTO createSuperterm(String supertermName, OntologyDTO ontology) {
        TermDTO dto = new TermDTO();
        dto.setTermName(supertermName);
        dto.setOntology(ontology);
        return dto;
    }


}