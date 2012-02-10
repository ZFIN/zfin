package org.zfin.fish.repository;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FishServiceTest extends AbstractDatabaseTest {

    private FishSearchCriteria criteria;

    @Before
    public void initCriteria() {
        criteria = new FishSearchCriteria(new FishSearchFormBean());
    }

    @Test
    public void getPhenotypeFigures() {
        List<FigureSummaryDisplay> figureSummaryDisplays = FishService.getPhenotypeSummary("ZDB-GENO-030619-2,ZDB-GENOX-090731-5", criteria);
        assertNotNull(figureSummaryDisplays);
    }

    @Test
    public void getPhenotypeStatementsPerFigureAndFish() {
        // WT+MO1-pitx3
        String fishID = "ZDB-GENO-030619-2,ZDB-GENOX-050512-12,ZDB-GENOX-050512-3,ZDB-GENOX-061218-1,ZDB-GENOX-080512-4";
        // Fig S11
        String figureID = "ZDB-FIG-080512-1";
        Figure figure = RepositoryFactory.getPublicationRepository().getFigure(figureID);
        List<PhenotypeStatement> phenotypeStatements = FishService.getPhenotypeStatements(figure, fishID);
        assertNotNull(phenotypeStatements);
        assertTrue(phenotypeStatements.size() > 3);
    }

    @Test
    public void matchingOnGeneAbbreviation() {
        Fish fish = FishService.getFish("ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
        FishMatchingService service = new FishMatchingService(fish);
        criteria.getGeneOrFeatureNameCriteria().setValue("Shha");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);

        criteria.getGeneOrFeatureNameCriteria().setValue("MO1");
        matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    private FeatureGene getSampleFeatureGene() {
        ZfinEntity gene = new ZfinEntity();
        gene.setID("ZDB-GENE-980526-166");
        ZfinEntity feature = new ZfinEntity();
        feature.setID("ZDB-ALT-020526-1");
        FeatureGene featureGene = new FeatureGene();
        featureGene.setFeature(feature);
        featureGene.setGene(gene);
        return featureGene;
    }

    @Test
    public void matchingOnGeneName() {
        Fish fish = FishService.getFish("ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
        FishMatchingService service = new FishMatchingService(fish);

        criteria.getGeneOrFeatureNameCriteria().setValue("hedgehog");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnGeneAlias() {
        Fish fish = FishService.getFish("ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
        FishMatchingService service = new FishMatchingService(fish);

        criteria.getGeneOrFeatureNameCriteria().setValue("you");
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnFeatureName() {
        Fish fish = FishService.getFish("ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnMultipleFeatureNameEntries() {
        Fish fish = FishService.getFish("ZDB-GENO-091027-2,ZDB-GENOX-091027-5");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha tbx");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
        assertTrue(matchingTexts.size() > 2);
    }

    @Test
    public void matchingOnFeatureAbbreviation() {
        Fish fish = FishService.getFish("ZDB-GENO-110410-1,ZDB-GENOX-110410-1");
        criteria.getGeneOrFeatureNameCriteria().setName("tku");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnFeatureAlias() {
        Fish fish = FishService.getFish("ZDB-GENO-101025-2,ZDB-GENOX-101025-22");

        criteria.getGeneOrFeatureNameCriteria().setValue("k18");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);
    }

    @Test
    public void matchingOnDirectAoTerm() {
        Fish fish = FishService.getFish("ZDB-GENO-091027-2,ZDB-GENOX-091027-5");
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
        Fish fish = FishService.getFish("ZDB-GENO-060619-1");
        criteria.getGeneOrFeatureNameCriteria().setValue("shha");
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTexts = service.getMatchingText(criteria);
        assertNotNull(matchingTexts);

    }

    @Test
    public void matchOnConstructWithRelationship() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("gfp");
        formBean.setFishID("ZDB-GENO-100122-2,ZDB-GENOX-100122-7");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = FishService.getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        assertTrue("One Match", matchingTextList.size() == 1);
        assertEquals("Related Marker to Construct [Marker Symbol]", matchingTextList.iterator().next().getDescriptor());
        assertEquals("[Is Coding Sequence of Gt(GBT-P9)]", matchingTextList.iterator().next().getAppendix());
    }

    @Test
    public void matchOnMultipleWords() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("sonic hedgehog a");
        // shha^tbx392/+
        formBean.setFishID("ZDB-GENO-091027-2,ZDB-GENOX-091027-5");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = FishService.getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        assertTrue("One Match", matchingTextList.size() == 1);
        assertEquals("Affected Gene Name", matchingTextList.iterator().next().getDescriptor());
    }

    @Test
    public void matchOnMultipleWordsDifferentGenes() {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setGeneOrFeatureName("oep gfp");
        // oeptz257;Tg(dharma:GFP)e100
        formBean.setFishID("ZDB-GENO-110131-44");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = FishService.getFish(formBean.getFishID());
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
        formBean.setFishID("ZDB-GENO-980202-1115,ZDB-GENOX-041102-68,ZDB-GENOX-081006-2");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        Fish fish = FishService.getFish(formBean.getFishID());
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        assertNotNull(matchingTextList);
        // At least two matches on the two structures.
        assertTrue("One Match", matchingTextList.size() > 1);
    }

}
