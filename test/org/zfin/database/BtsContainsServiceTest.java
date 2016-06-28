package org.zfin.database;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore("don't need to run this anymore?")
public class BtsContainsServiceTest {

    @Test
    public void singleGeneFeatureCriteria() {
        String queryString = "act";
        List<String> values = new ArrayList<>(1);
        values.add(queryString);
        BtsContainsService btsService = new BtsContainsService("fas_all");
        btsService.addBtsExpandedValueList("fas_all", values);
        String fullClause = btsService.getFullClause();
        assertNotNull(fullClause);
        assertEquals("bts_contains(fas_all, ' fas_all:(act^1000 or act*)', fas_all_score # real) ", fullClause);
    }


    @Test
    public void singleGeneFeatureMultipleTermCriteria() {
        String queryString = "act";
        List<String> geneFeatures = new ArrayList<>(1);
        geneFeatures.add(queryString);
        List<String> termIds = new ArrayList<>(2);
        termIds.add("ZDB-TERM-100331-8");
        termIds.add("ZDB-TERM-100331-107");

        BtsContainsService btsService = new BtsContainsService("fas_all");
        btsService.addBtsExpandedValueList("fas_all", geneFeatures);
        btsService.addBtsValueList("fas_pheno_term_group", termIds);
        String fullClause = btsService.getFullClause();
        assertNotNull(fullClause);
        assertEquals("bts_contains(fas_all, ' fas_all:(act^1000 or act*) and  " +
                "fas_pheno_term_group:zdb\\-term\\-100331\\-8 and  fas_pheno_term_group:zdb\\-term\\-100331\\-107', fas_all_score # real) ", fullClause);
    }

}
