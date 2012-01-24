package org.zfin.fish.repository;

import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getFishRepository;

public class FishRepositoryTest extends AbstractDatabaseTest {

/*
    @Test
    public void fishSearch() {
        FishSearchCriteria criteria = new FishSearchCriteria();
        criteria.setGeneOrFeatureNameCriteria(new SearchCriterion(SearchCriterionType.GENE_OR_FEATURE_NAME, "shha", false));
        criteria.setRows(10);
        criteria.setStart(0);
        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        assertNotNull(result);
        assertTrue(result.getResults().size() > 0);
        
    }
*/

    @Test
    public void getPhenotypeFigures() {
        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getAllFigures("ZDB-GENOX-100423-2");
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getPhenotypeFiguresForSingleTerm() {
        // brain
        String termID = "ZDB-TERM-100331-8";
        // WT (unspecified) + MO1-acd
        String fishID = "ZDB-GENO-030619-2,ZDB-GENOX-110325-3";
        List<String> termList = new ArrayList<String>(1);
        termList.add(termID);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getFiguresByFishAndTerms(fishID, termList);
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getPhenotypeFiguresForMultipleTerms() {
        // brain nucleus
        String brainNucleus = "ZDB-TERM-110313-4";
        // eye
        String eye = "ZDB-TERM-100331-100";
        // gli2aty17a/ty17a
        String fishID = "ZDB-GENO-980202-1115,ZDB-GENOX-041102-68,ZDB-GENOX-081006-2";
        List<String> termList = new ArrayList<String>(2);
        termList.add(brainNucleus);
        termList.add(eye);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getFiguresByFishAndTerms(fishID, termList);
        assertNotNull(zfinFigureEntities);
        assertTrue(zfinFigureEntities.size() >= 2);
    }

    @Test
    public void getFish() {
        String genoxIds = "ZDB-GENOX-050518-3,ZDB-GENO-030619-2";
        Fish fish = RepositoryFactory.getFishRepository().getFish(genoxIds);
        assertNotNull(fish);
    }

/*    @Test
    public void getFishByBtsContainClause() {
        String hql = " FROM FunctionalAnnotation where bts_contains(featureGroupName, \'t4\')";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        List<FunctionalAnnotation> fas = query.list();
        assertNotNull(fas);
    }*/

    @Test
    public void doubleMutantCountTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fgf8a fgf3");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        for (Fish fish : result.getResults()) {
            assertTrue(fish.getName() + " should have more than 2 affectors", (fish.getFeatures().size() + fish.getMorpholinos().size()) > 1);
        }

    }

    @Test
    public void excludeMorpholinoTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fgf8a");
        formBean.setFilter1("excludeMorphants");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        for (Fish fish : result.getResults()) {
            assertTrue(fish.getName() + " should have no morpolinos", (fish.getMorpholinos() == null || fish.getMorpholinos().size() == 0));
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

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        for (Fish fish : result.getResults()) {
            assertTrue(fish.getName() + " should have morpolinos", fish.getMorpholinos().size() > 0);
        }

    }

    @Test
    public void exactMatchesFirstTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("cz3");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());

        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        for (int i = 0; i < 2; i++) {
            Fish fish = result.getResults().get(i);
            if (fish != null)
                assertTrue("result " + i + ", " + fish.getGeneOrFeatureText() + " should contain 'cz3 '", fish.getGeneOrFeatureText().contains(" cz3 "));
        }
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

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
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
        FishSearchResult result = getFishRepository().getFish(criteria);

        assertTrue("searching for 'sid' should return less than 500 (21 when the test is going in)," +
                " rather than 600+ that will match if 'si:d*' genes are also matched ", result.getResults() != null && result.getResults().size() < 500);

    }



    @Test
    public void retrieveFishByGenoAlone() {
        String genoID = "ZDB-GENO-110210-2";
        String fishID = genoID;
        Fish fish = getFishRepository().getFish(fishID);
        assertNotNull(fish);
    }

    @Test
    public void retrieveFishByGenoxGeno() {
        String genoxID = "ZDB-GENOX-110211-2,ZDB-GENO-110210-2";
        String fishID = genoxID;
        Fish fish = getFishRepository().getFish(fishID);
        assertNotNull(fish);
    }
    //@Test  - gene name with comma test  "adaptor-related protein complex 1, mu 2 subunit"
    //@Test  - add a test to insure that quote characters are stripped...


    @Test
    public void termTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-449"); //pre-optic area, a small number of results
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        assertTrue("'pre-optic area' term search returns results", result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void termAndGeneTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("fez");
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-449"); //preoptic area, a small number of results
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        assertTrue("'pre-optic area' term + gene 'fez' search returns results",
                result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void includeSubstructuresTest() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-1442"); //cavitated compound organ, no direct phenotype or expression, all from subs
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        assertTrue("'cavitated compound organ' term should have results, and they'll all be from substructures",
                result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void genoxMaxLength() {
        String fishID = RepositoryFactory.getFishRepository().getGenoxMaxLength();
        assertNotNull(fishID);
        Assert.assertTrue(fishID.length() > 50);
    }


    //single gene, full & partial
    @Test
    public void fgf8aTest() {  genericGeneAlleleBoxTest("fgf8a");  }
    @Test
    public void fgfTest() { genericGeneAlleleBoxTest("fgf"); };



    //double gene
    @Test
    public void doubleGeneTest() { genericGeneAlleleBoxTest("fgf8a pax2a"); }


    //case insensitivity
    @Test
    public void lowCaseMoTest() { genericGeneAlleleBoxTest("mo3-fgf8a"); }
    @Test
    public void normalCaseMoTest() { genericGeneAlleleBoxTest("MO3-fgf8a"); }
    @Test
    public void upperCaseMoTest() { genericGeneAlleleBoxTest("MO3-FGF8A"); }



    //single quote
    @Test
    public void singleQuoteTest() { genericGeneAlleleBoxTest("popeye's sister"); }


    //full Tg name, several cases including commas, spaces, underscores and colons
    @Test
    public void tgTest() { genericGeneAlleleBoxTest("Tg(-2.4shha-M12:GFP)ka1"); }
    @Test
    public void tgWithSpaceAndCommaTest() {genericGeneAlleleBoxTest("Tg(shhb:Gal4TA4, 5xUAS:mRFP)"); }
    @Test
    public void superNastyTgTest() { genericGeneAlleleBoxTest("Tg(5xUAS:casp3a,5xUAS:Hsa.HIST1H2BJ-Citrine-YFP,cryaa:RFP)"); }
    @Test
    public void tgWithCommaAndUnderscoreTest() { genericGeneAlleleBoxTest("Tg(ompb:Rno.Vamp2-EGFP,ompb:ptprsa_C1556S)"); }


    //partial Tg
    @Test
    public void partialTgWithColonTest() { genericGeneAlleleBoxTest("hsp70l:dntbx5a"); }
    @Test
    public void partialTgWithSpaceTest() { genericGeneAlleleBoxTest("hsp70 gal4"); }

    //double gene morpoholino
    @Test
    public void doubleGeneMorpholinoNameTest() { genericGeneAlleleBoxTest("MO1-epcam,zgc:110304"); }




    public void genericGeneAlleleBoxTest(String value) {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName(value);
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);
        formBean.setSortBy(SortBy.BEST_MATCH.toString());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);

        FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);
        assertTrue("\"" + value + "\" search should return results", result != null && result.getResults() != null && result.getResultsFound() > 0);

    }

}
