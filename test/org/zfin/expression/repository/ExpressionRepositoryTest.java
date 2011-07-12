package org.zfin.expression.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.*;
import org.zfin.expression.presentation.DirectlySubmittedExpression;
import org.zfin.expression.presentation.ExpressedStructurePresentation;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.expression.presentation.StageExpressionPresentation;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.server.CurationExperimentRPCImpl;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
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
public class ExpressionRepositoryTest extends AbstractDatabaseTest {

    private ExpressionRepository expRep = RepositoryFactory.getExpressionRepository();
    private AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private PublicationRepository pubRep = RepositoryFactory.getPublicationRepository();

    private ExpressionService expressionService = new ExpressionService();

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

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

        // mir122
        String markerID = "ZDB-GENE-050609-27";
        String genoID = "ZDB-GENO-030619-2";
        String figureID = "ZDB-FIG-041119-4";
        experiment = expRep.getExperimentFigureStagesByGeneAndFish(pubID, markerID, genoID, figureID);
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
        String pubID = "ZDB-PUB-030728-18";

        List<ExpressionStructure> structures = expRep.retrieveExpressionStructures(pubID);
        assertNotNull(structures);
        assertTrue(structures.size() > 15);
        assertTrue(structures.size() < 40);
    }

    // Excluded until we have the ontologyManager loaded into memory for the unit tests.
    // right now it would load all ontologies when trying to create an expression result record.
    //@Test
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
            GenericTerm term = new GenericTerm();
            term.setTermName(Term.UNSPECIFIED);
            term.setZdbID("ZDB-TERM-100331-1055");

            result.setSuperTerm(term);

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
    public void testDirectlySubmittedExpression() {
        Session session = HibernateUtil.currentSession();
        Clone clone = (Clone) session.get(Clone.class, "ZDB-CDNA-040425-873");

        DirectlySubmittedExpression directlySubmittedExpression = expressionService.getDirectlySubmittedExpressionClone(clone);
//        assertEquals(117, numFigs);
        assertNotNull(directlySubmittedExpression);
        List<PublicationExpressionBean> markerExpressionInstances = directlySubmittedExpression.getMarkerExpressionInstances();
        assertEquals(1, markerExpressionInstances.size());
        PublicationExpressionBean markerExpressionInstance = markerExpressionInstances.get(0);
        assertEquals(6, markerExpressionInstance.getNumFigures());
        assertEquals(10, markerExpressionInstance.getNumImages());
        assertEquals("ZDB-PUB-040907-1", markerExpressionInstance.getPublicationZdbID());
    }

    @Test
    public void getGenoxFromGenotype() {

        String genotypeID = "ZDB-GENO-030530-1";
        GenotypeExperiment genox = expRep.getGenotypeExperimentByGenotypeID(genotypeID);
        assertNotNull(genox);
    }

    @Test
    public void getExpressionOnSecondaryTerms() {
        List<ExpressionResult> expressionResults = expRep.getExpressionOnSecondaryTerms();
        assertNotNull(expressionResults);
        Assert.assertEquals(0, expressionResults.size());
    }

    @Test
    public void getExpressionOnObsoletedTerms() {
        List<ExpressionResult> expressionResults = expRep.getExpressionOnObsoletedTerms();
        assertNotNull(expressionResults);
        Assert.assertEquals(0, expressionResults.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAnatomyForMarker() {

        String zdbID = "ZDB-GENE-980526-333";
        String sql = "SELECT distinct anatitem_zdb_id, anatitem_name_order " +
                "FROM " +
                "expression_result , expression_experiment, term , genotype_experiment, experiment , genotype, anatomy_item " +
                "WHERE " +
                "xpatex_gene_zdb_id = :zdbID " +
                "AND  xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "AND xpatres_expression_found= :expressionFound " +
                "AND xpatres_superterm_zdb_id = term_zdb_id " +
                "AND term_ont_id = anatitem_obo_id " +
                "AND xpatex_genox_zdb_id = genox_zdb_id " +
                "AND exp_zdb_id = genox_exp_zdb_id and exp_name = :experiment  " +
                "AND geno_zdb_id  = genox_geno_zdb_id " +
                "AND geno_is_wildtype = :wildType " +
                "ORDER BY anatitem_name_order asc";
        List<Object[]> termZdbIds = (List<Object[]>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("zdbID", zdbID)
                .setParameter("experiment", "_Standard")
                .setBoolean("expressionFound", true)
                .setBoolean("wildType", true)
                .list();
        List<AnatomyItem> anatomyItems = expRep.getWildTypeAnatomyExpressionForMarker(zdbID);
        assertEquals(termZdbIds.size(), anatomyItems.size());

        for (int i = 0; i < termZdbIds.size(); i++) {
            assertEquals(termZdbIds.get(i)[0], anatomyItems.get(i).getZdbID());
        }

    }

    @Test
    public void getExpressionPubCount(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = expRep.getExpressionPubCountForGene(m);
        Assert.assertTrue(count < 40);
        Assert.assertTrue(count > 10);
    }

    @Test
    public void getExpressionFigureCount(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = expRep.getExpressionFigureCountForEfg(m);
        Assert.assertTrue(count < 50);
        Assert.assertTrue(count > 20);
    }

    @Test
    public void getDirectlySubmittedExpression(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List pubList = expRep.getDirectlySubmittedExpressionForGene(m);
        assertEquals(1, pubList.size());

        // this may give duplicates
        Marker m2 = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-000210-25");
        pubList = expRep.getDirectlySubmittedExpressionForGene(m2);
        assertEquals(3, pubList.size());

        Marker m3 = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-031112-7");
        pubList = expRep.getDirectlySubmittedExpressionForGene(m3);
        assertEquals(3, pubList.size());
    }

    @Test
    public void getImagesFromPubAndClone(){
        PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
        publicationExpressionBean.setPublicationZdbID("ZDB-PUB-051025-1") ;
        publicationExpressionBean.setProbeFeatureZdbId("ZDB-EST-060130-308");
        int imageCount = expRep.getImagesFromPubAndClone(publicationExpressionBean);
        assertTrue(imageCount>10);
        assertTrue(imageCount<20);
    }

    @Test
    public void getStageExpressionForMarker(){
        StageExpressionPresentation stageExpressionPresentation = expRep.getStageExpressionForMarker("ZDB-GENE-010606-1");
        assertNotNull(stageExpressionPresentation);
        assertNotNull(stageExpressionPresentation.getStartStage());
        assertNotNull(stageExpressionPresentation.getEndStage());
    }

    @Test
    public void getWildTypeExpressionForMarker(){
        List<ExpressedStructurePresentation> wees = expRep.getWildTypeExpressionExperiments("ZDB-GENE-010606-1");
        assertEquals(33, wees.size());
    }

    @Test
    public void getExpressionSinglePub(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-060130-9");
        assertNotNull(m);
        Publication p = expRep.getExpressionSinglePub(m);
        assertNotNull(p);
        assertEquals("ZDB-PUB-051025-1",p.getZdbID());
    }

    @Test
    public void getExpressionSingleFigure(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-040112-1");
        assertNotNull(m);
        FigureLink figureLink = expRep.getExpressionSingleFigure(m);
        assertNotNull(figureLink);
        assertEquals("ZDB-FIG-051013-9", figureLink.getFigureZdbId());
        assertEquals("Fig. 4", figureLink.getLinkContent());
        assertEquals("<a href=\"/"+ ZfinProperties.getWebDriver()+"?MIval=aa-fxfigureview.apg&OID=ZDB-FIG-051013-9\">Fig. 4</a>",figureLink.getLink());
    }
}
