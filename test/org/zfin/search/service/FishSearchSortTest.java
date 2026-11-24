package org.zfin.search.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FishSearchSortTest {

    private static List<String> sortFields(SolrQuery query) {
        return query.getSorts().stream().map(SolrQuery.SortClause::getItem).collect(Collectors.toList());
    }

    @Test
    public void fishWithEmptyQueryGetsReporterDemotionThenComplexityThenName() {
        SolrQuery query = new SolrQuery();
        assertTrue(FishSearchSort.applyTo(query, "Fish", "", null));

        assertEquals(List.of(FishSearchSort.REPORTER_LINE_DEMOTION, "complexity", "name_sort"), sortFields(query));
        query.getSorts().forEach(clause -> assertEquals(SolrQuery.ORDER.asc, clause.getOrder()));
    }

    @Test
    public void nullAndBlankQueriesCountAsEmpty() {
        assertTrue(FishSearchSort.appliesTo("Fish", null, null));
        assertTrue(FishSearchSort.appliesTo("Fish", "   ", ""));
    }

    @Test
    public void fishWithQueryKeepsRelevance() {
        SolrQuery query = new SolrQuery();
        assertFalse(FishSearchSort.applyTo(query, "Fish", "shha", null));
        assertTrue(query.getSorts().isEmpty());
    }

    @Test
    public void otherCategoriesAreUnchanged() {
        for (String category : new String[]{"Gene", "Publication", "Any", "", null}) {
            SolrQuery query = new SolrQuery();
            assertFalse("category " + category, FishSearchSort.applyTo(query, category, "", null));
            assertTrue("category " + category, query.getSorts().isEmpty());
        }
    }

    @Test
    public void explicitSortIsRespected() {
        for (String sort : new String[]{"A to Z", "Z to A", "Newest", "Oldest"}) {
            SolrQuery query = new SolrQuery();
            assertFalse(sort, FishSearchSort.applyTo(query, "Fish", "", sort));
            assertTrue(sort, query.getSorts().isEmpty());
        }
    }
}
