package org.zfin.expression.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.expression.presentation.DirectlySubmittedExpression;
import org.zfin.expression.presentation.ExpressedStructurePresentation;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.expression.presentation.StageExpressionPresentation;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Test the ExpressionRepository class.
 */
public class ExpressionRepositoryTest extends AbstractDatabaseTest {

    private static Logger log = LogManager.getLogger(ExpressionRepositoryTest.class);

    private ExpressionRepository expRep = RepositoryFactory.getExpressionRepository();

    private ExpressionService expressionService = new ExpressionService();

    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
    }

    @Test
    public void getExperimentByID() {
        String experimentID = "ZDB-XPAT-090417-2";
        ExpressionExperiment2 experiment = expRep.getExpressionExperiment(experimentID);
        assertNotNull(experiment);
    }


    @Test
    public void getMarkerDBLinkByID() {
        String dbLinkID = "ZDB-DBLINK-020710-33129";
        MarkerDBLink experiment = expRep.getMarkDBLink(dbLinkID);
        assertNotNull(experiment);

    }

    @Test
    public void getAllPantherIds() {
        List<MarkerDBLink> dbLinks = expRep.getAllDbLinks(ForeignDB.AvailableName.PANTHER);
        assertNotNull(dbLinks);

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
        assertNotNull(experiment);

    }

    @Test
    public void getGenotype() {
        String genotypeID = "ZDB-GENO-960809-7";
        Genotype genotype = expRep.getGenotypeByID(genotypeID);
        assertNotNull(genotype);

    }


    @Test
    public void createNewGenotypeExperimentFromIds() {
        String experimentID = "ZDB-EXP-070511-5";
        String genotypeID = "ZDB-GENO-960809-7";
        FishExperiment genox = expRep.createFishExperiment(experimentID, genotypeID);
        assertTrue(genox.getZdbID() != null);
    }

    @Test
    public void createNewExpressionExperimentFromIds() {
        String experimentID = "ZDB-EXP-070511-5";
        String genotypeID = "ZDB-GENO-960809-7";
        String antibodyID = "ZDB-ATB-081002-19";
        String assay = "other";
        String pubID = "ZDB-PUB-990507-16";

        ExpressionExperimentDTO dto = new ExpressionExperimentDTO();
        dto.setAssay(assay);
        MarkerDTO abDto = new MarkerDTO();
        abDto.setZdbID(antibodyID);
        dto.setAntibodyMarker(abDto);
        ExperimentDTO envDto = new ExperimentDTO();
        envDto.setZdbID(experimentID);
        dto.setEnvironment(envDto);
        dto.setFishID(genotypeID);
        dto.setPublicationID(pubID);

        ExpressionExperiment2 expressionExperiment = new ExpressionExperiment2();

        expRep.createExpressionExperiment(expressionExperiment);
        assertNotNull(expressionExperiment.getZdbID());
    }


    @Test
    public void removeExperiment() {
        String experimentID = "ZDB-XPAT-090430-4";

        ExpressionExperiment2 experiment = expRep.getExpressionExperiment(experimentID);
        expRep.deleteExpressionExperiment(experiment);
    }

    @Test
    public void getExpressionFromPub() {
        String pubID = "ZDB-PUB-071023-13";

        List<ExpressionExperiment2> experiment = expRep.getExpressionExperiment2ByPub(pubID, null);
        assertNotNull(experiment);
    }

    @Test
    public void getExpressionFromIDs() {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        List<ExpressionFigureStage> experiment = expRep.getExperimentFigureStagesByIds(ids);
        assertNotNull(experiment);
    }

    @Test
    public void getExpressionResultwithEaPForGene() {
        String geneSymbol = "alcama";

        Marker gene = getMarkerRepository().getMarkerByAbbreviation(geneSymbol);
        List<ExpressionResult2> list = getExpressionRepository().getExpressionResultList(gene);
        assertNotNull(list);
    }

    @Test
    public void retrieveExperimentFigureStages2() {
        String pubID = "ZDB-PUB-060105-3";

        List<ExpressionFigureStage> experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, null);
        // this represents 5 unique experiments
        assertThat(experiments.size(), greaterThan(14));

        // Fig. 1
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, "ZDB-FIG-070109-23");
        assertThat(experiments.size(), greaterThan(2));

        // Fig. S3
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, "ZDB-FIG-070109-26");
        assertThat(experiments.size(), greaterThan(1));

        // mir206-1
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-MIRNAG-050609-28", null, null);
        assertThat(experiments.size(), greaterThan(4));

        // mir122
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-MIRNAG-050609-28", null, null);
        assertThat(experiments.size(), greaterThan(3));

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
        experiments = expRep.getExperimentFigureStagesByGeneAndFish(pubID, "ZDB-MIRNAG-050609-28", "ZDB-GENO-050209-5", "ZDB-FIG-070109-23");
        assertThat(experiments.size(), equalTo(0));

    }

    @Test
    public void getExpressionExperiments() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment2> experiments = expRep.getExperiments(zdbID);
        assertNotNull(experiments);
        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, null);
        assertNotNull(experiments);

        // alcam and WT
//        String fishName = "WT";
//        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, fishName);
//        assertTrue(experiments != null);
        String fishZdbID = "ZDB-GENO-030619-2";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, fishZdbID);
        assertNotNull(experiments);
    }

    @Test
    public void getExpressionExperimentsNew() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment2> experiments = expRep.getExperiments2(zdbID);
        assertNotNull(experiments);
        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, null);
        Set<ExpressionFigureStage> figureStageSet = experiments.get(0).getFigureStageSet();
/*
        System.out.println(figureStageSet.iterator().next().getExpressionResultSet());
        System.out.println(figureStageSet.iterator().next().getExpressionResultSet().size());
        for (ExpressionResult2 result : figureStageSet.iterator().next().getExpressionResultSet())
            System.out.println(result.getID() + " " + result.getSuperTerm());
*/

        String fishZdbID = "ZDB-GENO-030619-2";
        experiments = expRep.getExperimentsByGeneAndFish(zdbID, geneID, fishZdbID);
        assertNotNull(experiments);
    }

    @Test
    public void getExpressionExperiments2() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment2> experiments = expRep.getExperiments(zdbID);
        assertThat(experiments.size(), greaterThan(3));

        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, null);
        assertThat(experiments.size(), greaterThan(2));

        // alcam and WT
        String fishZdbID = "ZDB-FISH-150901-29105";
        experiments = expRep.getExperimentsByGeneAndFish2(zdbID, geneID, fishZdbID);
        assertThat(experiments.size(), greaterThan(2));
    }


    @Test
    public void retrieveExperimentFigureStages() {
        String pubID = "ZDB-PUB-060105-3";

        List<ExpressionFigureStage> experiment = expRep.getExperimentFigureStagesByGeneAndFish(pubID, null, null, null);
        assertNotNull(experiment);

        // mir122
        String markerID = "ZDB-MIRNAG-050609-28";
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
        List<ExpressionResult2> expressionResults = expRep.getExpressionOnObsoletedTerms();
        assertNotNull(expressionResults);
        Assert.assertEquals(0, expressionResults.size());
    }

    @Test
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
                .setParameter("expressionFound", true)
                .setParameter("wildType", true)
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
        Assert.assertTrue(count > 10);
    }

    @Test
    public void getExpressionFigureCount() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = expRep.getExpressionFigureCountForEfg(m);
        Assert.assertTrue(count > 20);
    }


    @Test
    public void getExpressionFigureStagesByFish() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-16069");
        int count = expRep.getExpressionFigureCountForFish(fish);
        assertThat(count, both(greaterThan(1)).and(lessThan(200)));

        List<ExpressionFigureStage> figureStages = expRep.getExpressionFigureStagesByFish(fish);
        assertThat(figureStages, is(notNullValue()));
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
    public void getWildTypeExpressionForMarker() {
        List<ExpressedStructurePresentation> wees = expRep.getWildTypeExpressionExperiments("ZDB-GENE-010606-1");
        assertThat(wees.size(), greaterThan(30));
        assertThat(wees.size(), lessThan(50));
    }

    @Test
    public void getExpressionSinglePub() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-030131-4357");
        assertNotNull(m);
        Publication p = expRep.getExpressionSinglePub(m);
        assertNotNull(p);
        assertEquals("ZDB-PUB-051025-1", p.getZdbID());
    }

    @Test
    public void getExpressionPub() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-030131-4357");
        assertNotNull(m);
        List<Publication> p = expRep.getExpressionPub(m);
        assertNotNull(p);
    }

    @Test
    @Ignore
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
    public void getNonEapExpressions() {
        Marker marker = getMarkerRepository().getMarkerByID("ZDB-GENE-990415-8");
        List<ExpressionResult2> expressedTerms = expRep.getNonEapExpressionResultList(marker);
        assertNotNull(expressedTerms);
        assertTrue(expressedTerms.size() > 100);
    }

    @Test
    public void getExpressionExperiment2sByFish() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-29173");
        List<ExpressionExperiment2> expressedTerms = expRep.getExpressionExperiment2sByFish(fish);
        assertNotNull(expressedTerms);
        assertTrue(expressedTerms.size() > 20);
        assertTrue(expressedTerms.size() < 40);
    }

    @Test
    public void getExperiment2sByAntibody() {
        Antibody antibody = getAntibodyRepository().getAntibodyByID("ZDB-ATB-190911-2");
        List<ExpressionExperiment2> expressedTerms = expRep.getExperiment2sByAntibody(antibody);
        assertNotNull(expressedTerms);
        assertTrue(expressedTerms.size() > 5);
        assertTrue(expressedTerms.size() < 15);
    }

    @Test
    public void getExpressionStructure() {
        ExpressionStructure expressionStructure = expRep.getExpressionStructure("ZDB-XPATINF-100601-22");
        assertNotNull(expressionStructure);
        assertEquals(expressionStructure.getDisplayName(), "presumptive rhombomere 3");
    }

    @Test
    public void getFishExperimentByID() {
        FishExperiment expressionStructure = expRep.getFishExperimentByID("ZDB-GENOX-210201-2");
        assertNotNull(expressionStructure);
        assertEquals(expressionStructure.getFish().getDisplayName(), "ago2<sup>ya100/ya100</sup>");
    }

    @Test
    public void getExpressionByExperiment() {
        List<ExpressionExperiment2> expressedTerms = expRep.getExpressionByExperiment("ZDB-EXP-070511-5");
        assertNotNull(expressedTerms);
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

    @Test
    public void getPhenotypeFromExpressions() {
        String publicationID = "ZDB-PUB-990507-16";
        String figureID = "ZDB-FIG-081003-3";
        String fishID = "ZDB-FISH-150901-19447";
        List<ExpressionResult2> expressionExperiment2s = getExpressionRepository().getPhenotypeFromExpressionsByFigureFish(publicationID, figureID, fishID, null);
        assertNotNull(expressionExperiment2s);
        List<ExpressionPhenotypeExperimentDTO> list = ExpressionService.createPhenotypeFromExpressions(expressionExperiment2s);
        assertNotNull(list);
    }

    @Test
    public void getPhenotypeFromExpressionsByFeature() {
        String featureID = "ZDB-ALT-161121-6";
        List<ExpressionResult2> expressionExperiment2s = getExpressionRepository().getPhenotypeFromExpressionsByFeature(featureID);
        assertNotNull(expressionExperiment2s);

        Set<Figure> figures = expressionExperiment2s.stream()
                .map(expressionResult2 ->
                        expressionResult2.getExpressionFigureStage().getFigure()
                )
                .collect(Collectors.toSet());
        assertNotNull(figures);
    }

    @Test
    public void getExperimentList() {
        List<Experiment> experimentSet = getExpressionRepository().geExperimentByPublication("ZDB-PUB-131112-24");
        assertNotNull(experimentSet);
    }

    @Test
    public void getExperimentCondition() {
        ExperimentCondition experimentCondition = getExpressionRepository().getExperimentCondition("ZDB-EXPCOND-041102-1");
        assertNotNull(experimentCondition);
    }

    @Test
    public void getExpressionById() {
        ExpressionFigureStage stage = getExpressionRepository().getExperimentFigureStage(78214);
        assertNotNull(stage);
    }

}
