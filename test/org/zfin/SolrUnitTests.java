package org.zfin;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.search.service.MarkerSearchServiceSpec;
import org.zfin.search.service.SearchSuggestionServiceSpec;
import org.zfin.uniquery.CategoriesAndFacetsSpec;
import org.zfin.uniquery.QuerySpec;
import org.zfin.uniquery.RelatedLinksSpec;
import org.zfin.uniquery.ResultAttributesSpec;


/**
 * Solr tests
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ResultAttributesSpec.class,
        RelatedLinksSpec.class,
        CategoriesAndFacetsSpec.class,
        MarkerSearchServiceSpec.class,
        QuerySpec.class,
        SearchSuggestionServiceSpec.class
/*        SolrPrototypeTest.class   //this references fields that don't exist anymore, but would be useful if refactored */
})


public class SolrUnitTests {
    public static Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SolrUnitTests.class);
    }

}