package org.zfin.fish.repository;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureExpressionSummaryDisplay;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.presentation.FishResult;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.presentation.MatchingTextType;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.Fish;
import org.zfin.fish.FeatureGene;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.MatchType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class FishServiceTest extends AbstractDatabaseTest {

    private static FishSearchCriteria criteria;


    @BeforeClass
    public static void initCriteria() {
        criteria = new FishSearchCriteria(new FishSearchFormBean());
    }

    @Test
    public void getPhenotypeFigures() {

        // WT+MO1-pitx3
        Fish fish = getFishRepository().getFishByName("WT + MO1-pitx3");

        assertNotNull(fish);
        String fishID = fish.getZdbID();

        List<FigureSummaryDisplay> figureSummaryDisplays = FishService.getPhenotypeSummary(fishID, criteria);
        assertNotNull(figureSummaryDisplays);
    }

    @Test
    public void getPhenotypeStatementsPerFigureAndFish() {
        //todo: this should be done by fish_zdb_id, once they're generated
        // WT+MO1-pitx3
        Fish fish = getFishRepository().getFishByName("WT + MO1-pitx3");

        assertNotNull(fish);
        String fishID = fish.getZdbID();
        // Fig S11
        String figureID = "ZDB-FIG-080512-1";
        Figure figure = RepositoryFactory.getPublicationRepository().getFigure(figureID);
        List<PhenotypeStatementWarehouse> phenotypeStatements = FishService.getPhenotypeStatements(figure, fishID);
        assertNotNull(phenotypeStatements);
        assertTrue(phenotypeStatements.size() > 3);
    }

    @Test
    public void getPhenotypeFiguresForMultipleTerms() {
        // brain nucleus
        String brainNucleus = "ZDB-TERM-110313-4";
        // eye
        String eye = "ZDB-TERM-100331-100";
        // gli2aty17a/ty17a


        FishExperiment fishExperiment = RepositoryFactory.getMutantRepository().getFishExperiment("ZDB-GENOX-041102-68");

        Fish fish = fishExperiment.getFish();
        assertNotNull("Fish should not be null", fish);
        String fishID = fish.getZdbID();

        assertNotNull("Fish id should not be null", fishID);

        List<String> termList = new ArrayList<String>(2);
        termList.add(brainNucleus);
        termList.add(eye);

        Set<ZfinFigureEntity> zfinFigureEntities = FishService.getFiguresByFishAndTerms(fishID, termList);
        assertNotNull("FigureEntity collection should not be null", zfinFigureEntities);
        assertTrue("Should be more than 2 figure entities", zfinFigureEntities.size() >= 2);
    }




    @Test
    public void matchingOnGeneAbbreviation() {
        String fishID = "ZDB-FISH-150901-19155";
        Fish fish = getMutantRepository().getFish(fishID);
        Assert.assertNotNull("Could not find fish with fishID: " + fishID, fish);
        FishMatchingService service = new FishMatchingService(fish);
        criteria.getGeneOrFeatureNameCriteria().setValue("Shha");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);

        criteria.getGeneOrFeatureNameCriteria().setValue("MO1");
        matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnGeneName() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-19155");
        FishMatchingService service = new FishMatchingService(fish);

        criteria.getGeneOrFeatureNameCriteria().setValue("hedgehog");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnGeneAlias() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-19155");
        FishMatchingService service = new FishMatchingService(fish);

        criteria.getGeneOrFeatureNameCriteria().setValue("you");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnFeatureName() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-19155");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnMultipleFeatureNameEntries() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-7624");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha tbx");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
        assertTrue(matchingTexts.size() > 2);
    }

    @Test
    public void matchingOnFeatureAbbreviation() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-9476");
        criteria.getGeneOrFeatureNameCriteria().setName("tku");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnFeatureAlias() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-10377");

        criteria.getGeneOrFeatureNameCriteria().setValue("k18");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnDirectAoTerm() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-7624");
        criteria.getPhenotypeAnatomyCriteria().setValue("ZDB-TERM-100331-2186");

        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);

        criteria.getPhenotypeAnatomyCriteria().setValue("ZDB-TERM-100331-2060");
        matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchOnConstructNoPhenotype() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-10837");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);

    }

    @Test
    public void matchOnConstructWithRelationship() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("gfp");
        formBean.setFishID("ZDB-FISH-150901-24893");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = getMutantRepository().getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        assertThat("At least one matching condition", matchingTextList, hasSize(greaterThan(1)));
    }

    @Test
    public void matchOnMultipleWords() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("sonic hedgehog a");
        // shha^tbx392/+
        formBean.setFishID("ZDB-FISH-150901-7624");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = getMutantRepository().getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        assertTrue("One Match", matchingTextList.size() == 1);
        assertEquals("Affected Genomic Region Name", matchingTextList.iterator().next().getDescriptor());
    }

    @Test
    public void matchOnMultipleWordsDifferentGenes() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("oep gfp");
        // oeptz257;Tg(dharma:GFP)e100
        formBean.setFishID("ZDB-FISH-150901-22681");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = getMutantRepository().getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        assertTrue("One Match", matchingTextList.size() > 1);
    }

    @Test
    public void matchOnMultipleAoTerms() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        // brain nucleus and eye
        formBean.setAnatomyTermNames("brain nucleus|eye");
        formBean.setAnatomyTermIDs("ZDB-TERM-110313-4,ZDB-TERM-100331-100");
        // gli2aty17a/ty17a
        formBean.setFishID("ZDB-FISH-150901-4298");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = getMutantRepository().getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        // At least two matches on the two structures.
        assertTrue("One Match", matchingTextList.size() > 1);
    }

    @Test
    public void testExpressionSummary() {
        String fishID = "ZDB-FISH-150901-17338";
        String geneID = "ZDB-GENE-980526-397";
        List<FigureExpressionSummary> displaySummary = FishService.getExpressionSummary(fishID, geneID);
        Assert.assertNotNull(displaySummary);
    }

    @Test
    public void hasImages() {
        String fishID = "ZDB-FISH-150901-12093";
        boolean hasImages = FishService.hasImagesOnExpressionFigures(fishID);
        assertTrue(hasImages);
    }

    @Test
    public void matchingOnFeatureLineNumber() {
        Fish fish = getMutantRepository().getFish("ZDB-FISH-150901-22181");
        criteria.getGeneOrFeatureNameCriteria().setValue("F0104a");

        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
        assertThat(matchingTexts.size(), greaterThan(0));
        MatchingText matchingText = matchingTexts.iterator().next();
        assertEquals(matchingText.getMatchingQuality(), MatchType.EXACT);
        assertEquals(matchingText.getType(), MatchingTextType.FEATURE_LINE_NUMBER);
    }

    @Test
    public void getExpressionSummary() {
        // genotype: apchu745/hu745> no MOs just generic and std-control genox ids

        Criteria criteria = HibernateUtil.currentSession().createCriteria(Fish.class);
        criteria.add(Restrictions.eq("zdbID", "ZDB-FISH-150901-26664"));
        Fish fish = (Fish) criteria.uniqueResult();

        assertNotNull("fish should not be null", fish);

        List<FigureExpressionSummary> summaryList = FishService.getExpressionSummary(fish.getZdbID(), null);
        assertNotNull("The expression summary list should not be null", summaryList);

        String figID = "ZDB-FIG-101206-3";
        String geneAbbreviation = "fabp2";
        for (FigureExpressionSummary figureExpressionSummary : summaryList) {
            if (figureExpressionSummary.getFigure().getZdbID().equals(figID)) {
                for (ExpressedGene expressedGene : figureExpressionSummary.getExpressedGenes()) {
                    if (expressedGene.getGene().getAbbreviation().equals(geneAbbreviation)) {
                        assertTrue("There should be at least one expression statement on gene " + expressedGene.getGene().getAbbreviation(), expressedGene.getExpressionStatements().size() > 0);
                    }
                }
            }
        }

        List<FigureExpressionSummaryDisplay> list = PresentationConverter.getFigureExpressionSummaryDisplay(summaryList);
        assertNotNull("Should have expression summary list",list);
        for (FigureExpressionSummaryDisplay display : list) {
            if (display.getFigure().getZdbID().equals(figID)) {
                if (display.getExpressedGene().getGene().getAbbreviation().equals(geneAbbreviation)) {
                    assertTrue("No expression statement found: ", display.getExpressedGene().getExpressionStatements().size() > 0);
                }
            }
        }
    }


    @Test
    public void fishSearch() {
        FishSearchCriteria criteria = new FishSearchCriteria();
        criteria.setGeneOrFeatureNameCriteria(new SearchCriterion(SearchCriterionType.GENE_OR_FEATURE_NAME, "shha", false));
        criteria.setRows(10);
        criteria.setStart(0);
        FishSearchResult result = FishService.getFish(criteria);
        assertNotNull(result);
        assertTrue(result.getResults().size() > 0);

    }

    @Test
    public void doubleMutantCountTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fgf8a fgf3");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        for (FishResult fishResult : result.getResults()) {
            assertTrue(fishResult.getFish().getName() + " should have more than 2 affectors", (fishResult.getFish().getGenotype().getGenotypeFeatures().size() + fishResult.getFish().getStrList().size()) > 1);
        }

    }

    @Test
    public void excludeMorpholinoTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fgf8a");
        formBean.setFilter1("excludeMorphants");
        formBean.setMaxDisplayRecords(100);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        for (FishResult fishResult : result.getResults()) {
            //don't bother testing the non-fish records that get returned as well
            if (fishResult.getFish() != null) {
                assertTrue(fishResult.getFish().getName() + " should have no morpolinos", (fishResult.getFish().getStrList() == null || fishResult.getFish().getStrList().size() == 0));
            }
        }

    }

    @Test
    public void requireMorpholinoTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fgf8a");
        formBean.setFilter1("morphantsOnly");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        for (FishResult fishResult : result.getResults()) {
            assertTrue(fishResult.getFish().getName() + " should have morpolinos", fishResult.getFish().getStrList().size() > 0);
        }

    }

    @Test
    public void exactMatchesFirstTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("cz4");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);

        FishResult fishResult = result.getResults().iterator().next();

        List<String> names = new ArrayList<>();
        if (fishResult.getFish() != null) {
            names.add(fishResult.getFish().getName());
        }

        for (FeatureGene featureGene : fishResult.getFeatureGenes()) {
            names.add(featureGene.getFeature().getAbbreviation());
            names.add(featureGene.getFeature().getName());
        }
        assertTrue(names + " should contain 'cz4 '", names.contains("cz4"));

    }

    @Test
    public void geneAliasReturnsTransgenicsTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        //a fish with the ka1 transgenic allele should bring back all names of shha, so just the word sonic should work
        formBean.setGeneOrFeatureName("sonic sb15");
        formBean.setFilter1("transgenicsOnly");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        assertTrue("'sonic sb15' search should bring back at least one result ", result.getResults() != null && result.getResults().size() > 0);


    }

    @Test
    public void escapeRatherThanConcatenateTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("sid");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        FishSearchResult result = FishService.getFish(criteria);

        assertTrue("searching for 'sid' should return less than 500 (21 when the test is going in)," +
                " rather than 600+ that will match if 'si:d*' genes are also matched ", result.getResults() != null && result.getResults().size() < 500);

    }

    @Test
    public void termTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-449"); //preoptic area, a small number of results
        formBean.setAnatomyTermNames("preoptic area");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        assertTrue("'pre-optic area' term search returns results", result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void termAndGeneTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fez");
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-449"); //preoptic area, a small number of results
        formBean.setAnatomyTermNames("preoptic area");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        assertTrue("'pre-optic area' term + gene 'fez' search returns results",
                result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void includeSubstructuresTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-1442"); //cavitated compound organ, no direct phenotype or expression, all from subs
        formBean.setAnatomyTermNames("cavitated compound organ");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        assertTrue("'cavitated compound organ' term should have results, and they'll all be from substructures",
                result != null && result.getResults() != null && result.getResultsFound() > 0);
    }



    //single gene, full & partial
    @Test
    public void fgf8aTest() {
        genericGeneAlleleBoxTest("fgf8a");
    }

    @Test
    public void fgfTest() {
        genericGeneAlleleBoxTest("fgf");
    }


    //double gene
    @Test
    public void doubleGeneTest() {
        genericGeneAlleleBoxTest("fgf8a pax2a");
    }


    //case insensitivity
    @Test
    public void lowCaseMoTest() {
        genericGeneAlleleBoxTest("mo3-fgf8a");
    }

    @Test
    public void normalCaseMoTest() {
        genericGeneAlleleBoxTest("MO3-fgf8a");
    }

    @Test
    public void upperCaseMoTest() {
        genericGeneAlleleBoxTest("MO3-FGF8A");
    }


    //single quote
    @Test
    public void singleQuoteTest() {
        genericGeneAlleleBoxTest("popeye's sister");
    }


    //full Tg name, several cases including commas, spaces, underscores and colons
    @Test
    public void tgWithSpaceAndCommaTest() {
        genericGeneAlleleBoxTest("Tg(shhb:Gal4TA4, 5xUAS:mRFP)");
    }

    @Test
    public void superNastyTgTest() {
        genericGeneAlleleBoxTest("Tg(5xUAS:casp3a,5xUAS:Hsa.HIST1H2BJ-Citrine,cryaa:RFP)");
    }

    @Test
    public void tgWithCommaAndUnderscoreTest() {
        genericGeneAlleleBoxTest("Tg(ompb:Rno.Vamp2-EGFP,ompb:ptprsa_C1556S)");
    }


    //partial Tg
    @Test
    public void partialTgWithColonTest() {
        genericGeneAlleleBoxTest("hsp70l:dntbx5a");
    }

    @Test
    public void partialTgWithSpaceTest() {
        genericGeneAlleleBoxTest("hsp70 gal4");
    }

    //double gene morpoholino
    @Test
    public void doubleGeneMorpholinoNameTest() {
        genericGeneAlleleBoxTest("MO1-epcam zgc:110304");
    }

    @Test
    public void geneStartsWithTest() { genericGeneAlleleBoxTest("adssl sa11426"); }

    public void genericGeneAlleleBoxTest(String value) {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName(value);
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = FishService.getFish(criteria);
        assertTrue("\"" + value + "\" search should return results", result != null && result.getResults() != null && result.getResultsFound() > 0);

    }


}
