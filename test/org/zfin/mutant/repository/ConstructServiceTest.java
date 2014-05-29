package org.zfin.mutant.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureExpressionSummaryDisplay;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.repository.FishMatchingService;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.presentation.MatchingTextType;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.presentation.ConstructSearchFormBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.MatchType;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertTrue;

public class ConstructServiceTest extends AbstractDatabaseTest {

    private ConstructSearchCriteria criteria;

    @Before
    public void initCriteria() {
        criteria = new ConstructSearchCriteria(new ConstructSearchFormBean());
    }

    @Test
    public void getExpressionFigures() {
        List<FigureSummaryDisplay> figureSummaryDisplays = ConstructService.getExpressionSummary("ZDB-GTCONSTRCT-120209-1", criteria);
        assertNotNull(figureSummaryDisplays);
    }




}
