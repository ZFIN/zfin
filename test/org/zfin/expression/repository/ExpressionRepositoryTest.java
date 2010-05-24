package org.zfin.expression.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.*;
import org.zfin.expression.presentation.DirectlySubmittedExpression;
import org.zfin.expression.presentation.MarkerExpressionInstance;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.server.CurationExperimentRPCImpl;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.Term;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test the ExpressionRepository class.
 */
public class ExpressionRepositoryTest {

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
        // TODO: this should load a specific database instance for testing purposes

    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }

    private ExpressionRepository expRep = RepositoryFactory.getExpressionRepository();
    private AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private PublicationRepository pubRep = RepositoryFactory.getPublicationRepository();

    @Test
    public void getExperimentByID() {
        String experimentID = "ZDB-XPAT-090417-2";
        ExpressionExperiment experiment = expRep.getExpressionExperiment(experimentID);
        assertTrue(experiment != null);
    }


    @Test
    public void getMarkerDBLinkByID() {
        String dblinkID = "ZDB-DBLINK-020710-33129";
        MarkerDBLink experiment = expRep.getMarkDBLink(dblinkID);
        assertTrue(experiment != null);

    }

    @Test
    public void getGenotypeExperimentByExperiment() {
        String experimentID = "ZDB-EXP-070511-5";
        String genotypeID = "ZDB-GENO-960809-7";
        GenotypeExperiment experiment = expRep.getGenotypeExperimentByExperimentIDAndGenotype(experimentID, genotypeID);
        assertTrue(experiment != null);

    }

    @Test
    public void getExperiment() {
        String experimentID = "ZDB-EXP-070511-5";
        Experiment experiment = expRep.getExperimentByID(experimentID);
        assertTrue(experiment != null);

    }

    @Test
    public void getGenotype() {
        String genotypeID = "ZDB-GENO-960809-7";
        Genotype genotype = expRep.getGenotypeByID(genotypeID);
        assertTrue(genotype != null);

    }


    @Test
    public void createNewGenotypeExperimentFromIds() {
        String experimentID = "ZDB-EXP-070511-5";
        String genotypeID = "ZDB-GENO-960809-7";
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            GenotypeExperiment genox = expRep.createGenoteypExperiment(experimentID, genotypeID);
            assertTrue(genox.getZdbID() != null);
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void createNewExpressionExperimentFromIds() {
        String experimentID = "ZDB-EXP-070511-5";
        String genotypeID = "ZDB-GENO-960809-7";
        String antibodyID = "ZDB-ATB-081002-19";
        String assay = "other";
        String pubID = "ZDB-PUB-990507-16";

        ExperimentDTO dto = new ExperimentDTO();
        dto.setAssay(assay);
        MarkerDTO abDto = new MarkerDTO();
        abDto.setZdbID(antibodyID);
        dto.setAntibodyMarker(abDto);
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setZdbID(experimentID);
        dto.setEnvironment(envDto);
        dto.setFishID(genotypeID);
        dto.setPublicationID(pubID);

        ExpressionExperiment expressionExperiment = new ExpressionExperiment();
        CurationExperimentRPCImpl.populateExpressionExperiment(dto, expressionExperiment);

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            expRep.createExpressionExperiment(expressionExperiment);
            assertTrue(expressionExperiment.getZdbID() != null);
        } finally {
            tx.rollback();
        }
    }


    @Test
    public void removeExperiment() {
        String experimentID = "ZDB-XPAT-090430-4";

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        ExpressionExperiment experiment = expRep.getExpressionExperiment(experimentID);
        try {
            expRep.deleteExpressionExperiment(experiment);
        } finally {
            tx.rollback();
        }
    }


    @Test
    public void retrieveExperimentFigureStages() {
        String pubID = "ZDB-PUB-060105-3";

        List<ExperimentFigureStage> experiment = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, null);
        assertNotNull(experiment);
    }

    @Test
    public void retrieveSingleExperimentFigureStage() {
        String experimentID = "ZDB-XPAT-050720-1";
        String startID = "ZDB-STAGE-010723-35";
        String endID = "ZDB-STAGE-010723-36";
        String figureID = "ZDB-FIG-081003-3";

        expRep.getExperimentFigureStage(experimentID, figureID, startID, endID);
    }

    @Test
    public void retrieveExpressionStructures() {
        String pubID = "ZDB-PUB-990507-16";

        List<ExpressionStructure> structures = expRep.retrieveExpressionStructures(pubID);
        assertNotNull(structures);
    }

    @Test
    public void retrieveFigureAnnotation() {
        String pubID = "ZDB-PUB-990507-16";

        List<ExpressionStructure> structures = expRep.retrieveExpressionStructures(pubID);
        assertNotNull(structures);
    }

    @Test
    public void createExpressionResult() {
        String xpatexID = "ZDB-XPAT-050128-4";

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment experiment = expRep.getExpressionExperiment(xpatexID);
            assertNotNull(experiment);
            ExpressionResult result = new ExpressionResult();
            result.setExpressionExperiment(experiment);

            DevelopmentStage start = anatomyRep.getStageByID("ZDB-STAGE-010723-35");
            result.setStartStage(start);
            DevelopmentStage end = anatomyRep.getStageByID("ZDB-STAGE-010723-36");
            result.setEndStage(end);
            AnatomyItem term = anatomyRep.getAnatomyItem(Term.UNSPECIFIED);
            result.setSuperterm(term);

            Figure figure = pubRep.getFigureByID("ZDB-FIG-041119-3");
            result.addFigure(figure);
            result.setExpressionFound(true);
            expRep.createExpressionResult(result, null);
            //tx.commit();
        } finally {
            tx.rollback();
        }
        assertTrue("yipie", true);
    }


    @Test
    public void testExpressionPubs() {
        Session session = HibernateUtil.currentSession();
//        Marker marker = (Marker) session.get(Marker.class, "ZDB-GENE-990415-72" ) ;
        Marker marker = (Marker) session.get(Marker.class, "ZDB-EST-010914-90");

        int numPubs = expRep.getExpressionPubCount(marker);
        assertTrue(numPubs > 0);
    }

    @Test
    public void testExpressionFigures() {
        Session session = HibernateUtil.currentSession();
        Marker marker = (Marker) session.get(Marker.class, "ZDB-EST-010914-90");

        int numFigs = expRep.getExpressionFigureCount(marker);
        assertTrue(numFigs > 0);
    }

    @Test
    public void testDirectlySubmittedExpression() {
        Session session = HibernateUtil.currentSession();
        Marker marker = (Marker) session.get(Marker.class, "ZDB-CDNA-040425-873");

        DirectlySubmittedExpression directlySubmittedExpression = ExpressionService.getDirectlySubmittedExpressionSummaries(marker);
//        assertEquals(117, numFigs);
        assertNotNull(directlySubmittedExpression);
        List<MarkerExpressionInstance> markerExpressionInstances = directlySubmittedExpression.getExpressionSummaryInstances();
        assertEquals(1, markerExpressionInstances.size());
        MarkerExpressionInstance markerExpressionInstance = markerExpressionInstances.get(0);
        assertEquals(6, markerExpressionInstance.getFigureCount());
        assertEquals(10, markerExpressionInstance.getImageCount());
        assertEquals("ZDB-PUB-040907-1", markerExpressionInstance.getSinglePublication().getZdbID());
    }

    @Test
    public void getGenoxFromGenotype() {

        String genotypeID = "ZDB-GENO-030530-1";
        GenotypeExperiment genox = expRep.getGenotypeExperimentByGenotypeID(genotypeID);
        assertNotNull(genox);
    }


}
