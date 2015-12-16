package org.zfin.expression.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
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
import org.zfin.gwt.root.dto.*;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

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
        String dbLinkID = "ZDB-DBLINK-020710-33129";
        MarkerDBLink experiment = expRep.getMarkDBLink(dbLinkID);
        assertTrue(experiment != null);

    }

    @Test
    public void getFishExperimentByExperiment() {
        String experimentID = "ZDB-EXP-080714-9";
        String fishID = "ZDB-FISH-150901-5520";
        FishExperiment experiment = expRep.getFishExperimentByExperimentIDAndFishID(experimentID, fishID);
        assertThat(experiment, is(notNullValue()));
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
            FishExperiment genox = expRep.createFishExperiment(experimentID, genotypeID);
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


        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            CurationExperimentRPCImpl.populateExpressionExperiment(dto, expressionExperiment);
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
    public void retrieveExperimentFigureStages2() {
        String pubID = "ZDB-PUB-060105-3";

        List<ExperimentFigureStage> experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, null);
        // this represents 5 unique experiments
        assertThat(experiments.size(), greaterThan(14));
        assertThat(experiments.size(), lessThan(16));

        // Fig. 1
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, "ZDB-FIG-070109-23");
        assertThat(experiments.size(), greaterThan(2));
        assertThat(experiments.size(), lessThan(4));

        // Fig. S3
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, "ZDB-FIG-070109-26");
        assertThat(experiments.size(), greaterThan(1));
        assertThat(experiments.size(), lessThan(3));

        // mir206-1
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-GENE-050609-28", null, null);
        assertThat(experiments.size(), greaterThan(4));
        assertThat(experiments.size(), lessThan(6));

        // mir122
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-GENE-050609-27", null, null);
        assertThat(experiments.size(), greaterThan(3));
        assertThat(experiments.size(), lessThan(5));

        // genotype . .  .all the same
        //TODO needs a FISH rather than a genotype?
       /* experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, "ZDB-GENO-050209-5", null);
        assertThat(experiments.size(), greaterThan(14));
        assertThat(experiments.size(), lessThan(16));*/

        // genotype . .  .all the same
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, "ZDB-GENO-050209-3", null);
        assertThat(experiments.size(), equalTo(0));

        // genotype . .  .all the same
        //TODO needs a FISH rather than a genotype?
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-GENE-050609-28", "ZDB-GENO-050209-5", "ZDB-FIG-070109-23");
        assertThat(experiments.size(), equalTo(0));
        assertThat(experiments.size(), lessThan(2));

    }

    @Test
    public void getExpressionExperiments() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment> experiments = expRep.getExperiments(zdbID);
        assertTrue(experiments != null);
        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, null);
        assertTrue(experiments != null);

        // alcam and WT
//        String fishName = "WT";
//        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, fishName);
//        assertTrue(experiments != null);
        String fishZdbID = "ZDB-GENO-030619-2";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, fishZdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getExpressionExperimentsNew() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment2> experiments = expRep.getExperiments2(zdbID);
        assertTrue(experiments != null);
        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, null);
        assertTrue(experiments != null);

        // alcam and WT
//        String fishName = "WT";
//        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, fishName);
//        assertTrue(experiments != null);
        String fishZdbID = "ZDB-GENO-030619-2";
        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, fishZdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getExpressionExperiments2() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment> experiments = expRep.getExperiments(zdbID);
        assertThat(experiments.size(), greaterThan(3));
        assertThat(experiments.size(), lessThan(40));

        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, null);
        assertThat(experiments.size(), greaterThan(2));
        assertThat(experiments.size(), lessThan(40));

        // alcam and WT
        // TODO: once we have fish in prod we can adjust this test
        String fishZdbID = "ZDB-GENO-030619-2";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, fishZdbID);
/*
        assertThat(experiments.size(), greaterThan(2));
        assertThat(experiments.size(), lessThan(4));
*/
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
    @Test
    @Ignore("need ontologyManager loaded into memory for the unit tests")
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
        FishExperiment genox = expRep.getGenotypeExperimentByGenotypeID(genotypeID);
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
        String sql = "SELECT distinct term_zdb_id, term_name " +
                "FROM " +
                "expression_result , expression_experiment, term , fish_experiment , fish, genotype " +
                "WHERE " +
                "xpatex_gene_zdb_id = :zdbID " +
                "AND  xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "AND xpatres_expression_found= :expressionFound " +
                "AND xpatres_superterm_zdb_id = term_zdb_id " +
                "AND xpatex_genox_zdb_id = genox_zdb_id " +
                "AND fish_zdb_id  = genox_fish_zdb_id " +
                "AND fish_genotype_zdb_id  = geno_zdb_id " +
                "AND geno_is_wildtype = :wildType " +
                "ORDER BY term_name asc";
        List<Object[]> termZdbIds = (List<Object[]>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("zdbID", zdbID)
                .setBoolean("expressionFound", true)
                .setBoolean("wildType", true)
                .list();
        List<GenericTerm> anatomyItems = expRep.getWildTypeAnatomyExpressionForMarker(zdbID);
        assertEquals(termZdbIds.size(), anatomyItems.size());

        for (int i = 0; i < termZdbIds.size(); i++) {
            assertEquals(termZdbIds.get(i)[0], anatomyItems.get(i).getZdbID());
        }

    }

    @Test
    public void getExpressionPubCount() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = expRep.getExpressionPubCountForGene(m);
        Assert.assertTrue(count < 40);
        Assert.assertTrue(count > 10);
    }

    @Test
    public void getExpressionFigureCount() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = expRep.getExpressionFigureCountForEfg(m);
        Assert.assertTrue(count < 50);
        Assert.assertTrue(count > 20);
    }


    @Test
    public void getExpressionFigureCountForGivenFish() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-16069");
        int count = expRep.getExpressionFigureCountForFish(fish);
        assertThat(count, both(greaterThan(1)).and(lessThan(200)));

        List<ExpressionResult> expressionResults = expRep.getExpressionResultsByFish(fish);
        assertThat(expressionResults, is(notNullValue()));
    }

    @Test
    public void getExpressionResultsByFishAndPublication() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-29105");
        long count = expRep.getExpressionResultsByFishAndPublication(fish, "ZDB-PUB-140101-37");
        assertThat(count, greaterThan(0L));
    }

    @Test
    public void getDirectlySubmittedExpression() {
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
    public void getImagesFromPubAndClone() {
        PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
        publicationExpressionBean.setPublicationZdbID("ZDB-PUB-051025-1");
        publicationExpressionBean.setProbeFeatureZdbId("ZDB-EST-060130-308");
        int imageCount = expRep.getImagesFromPubAndClone(publicationExpressionBean);
        assertTrue(imageCount > 10);
        assertTrue(imageCount < 20);
    }

    @Test
    public void getImagesForEFG() {
        PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
        // specifies Kaed
        publicationExpressionBean.setPublicationZdbID("ZDB-PUB-090311-2");
        int imageCount = expRep.getImagesForEfg(publicationExpressionBean);
        assertThat(imageCount, greaterThan(1000));
        assertThat(imageCount, lessThan(1500));

        publicationExpressionBean.setPublicationZdbID("ZDB-PUB-090311-1");
        imageCount = expRep.getImagesForEfg(publicationExpressionBean);
        assertEquals(imageCount, 0);
    }

    @Test
    public void getStageExpressionForMarker() {
        StageExpressionPresentation stageExpressionPresentation = expRep.getStageExpressionForMarker("ZDB-GENE-010606-1");
        assertNotNull(stageExpressionPresentation);
        assertNotNull(stageExpressionPresentation.getStartStage());
        assertNotNull(stageExpressionPresentation.getEndStage());
    }

    @Test
    public void getWildTypeExpressionForMarker() {
        List<ExpressedStructurePresentation> wees = expRep.getWildTypeExpressionExperiments("ZDB-GENE-010606-1");
        assertThat(wees.size(), greaterThan(30));
        assertThat(wees.size(), lessThan(50));
    }

    @Test
    public void getExpressionSinglePub() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-060130-9");
        assertNotNull(m);
        Publication p = expRep.getExpressionSinglePub(m);
        assertNotNull(p);
        assertEquals("ZDB-PUB-051025-1", p.getZdbID());
    }

    @Test
    public void getExpressionPub() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-060130-9");
        assertNotNull(m);
        List<Publication> p = expRep.getExpressionPub(m);
        assertNotNull(p);
    }

    @Test
    public void getExpressionSingleFigure() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-040112-1");
        assertNotNull(m);
        FigureLink figureLink = expRep.getExpressionSingleFigure(m);
        assertNotNull(figureLink);
        assertEquals("ZDB-FIG-051013-9", figureLink.getFigureZdbId());
        assertEquals("Fig. 4", figureLink.getLinkContent());
        assertEquals("<a href=\"/ZDB-FIG-051013-9\" class=\"figure-link\" >Fig.&nbsp;4</a>", figureLink.getLink());
    }

    @Test
    public void getAllExpressedTerms() {
        Set<String> expressedTerms = expRep.getAllDistinctExpressionTermIDs();
        assertNotNull(expressedTerms);
        assertTrue(expressedTerms.size() > 100);
    }

    @Test
    public void getAllPhenotypeTerms() {
        Set<String> expressedTerms = expRep.getAllDistinctPhenotypeTermIDs();
        assertNotNull(expressedTerms);
        assertTrue(expressedTerms.size() > 100);
    }

    @Test
    public void pileStructureExists() {
        ExpressedTermDTO term = new ExpressedTermDTO();
        EntityDTO entity = new EntityDTO();
        TermDTO superterm = new TermDTO();
        superterm.setName("liver");
        superterm.setOntology(OntologyDTO.ANATOMY);
        entity.setSuperTerm(superterm);
        term.setEntity(entity);
        String publicationID = "ZDB-PUB-990507-16";
        expRep.pileStructureExists(term, publicationID);

        EapQualityTermDTO eap = new EapQualityTermDTO();
        eap.setTag("abnormal");
        TermDTO qual = new TermDTO();
        qual.setZdbID("PATO:0000462");
        eap.setTerm(qual);
        term.setQualityTerm(eap);
        expRep.pileStructureExists(term, publicationID);
    }

    @Test
    public void getExpressionResultsByTermAndStage() {
        String termOboID = "ZFA:0000823";
        GenericTerm term = getOntologyRepository().getTermByOboID(termOboID);
        DevelopmentStage start = new DevelopmentStage();
        start.setAbbreviation("5-9 somites");
        start = getOntologyRepository().getStageByExample(start);
        DevelopmentStage end = new DevelopmentStage();
        end.setAbbreviation("10-13 somites");
        end = getOntologyRepository().getStageByExample(end);
        TermFigureStageRange range = new TermFigureStageRange();
        range.setSuperTerm(term);
        range.setStart(start);
        range.setEnd(end);
        List<ExpressionResult> resultList = getExpressionRepository().getExpressionResultsByTermAndStage(range);
        assertNotNull(resultList);
    }

}
