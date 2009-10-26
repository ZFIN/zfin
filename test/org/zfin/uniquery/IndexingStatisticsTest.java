package org.zfin.uniquery;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.io.File;
import java.util.Map;

/**
 * Unit Test class.
 */
public class IndexingStatisticsTest {

    @Test
    public void countIndexes() {
        String[] urls = {"http://wiki.zfin.org/display/prot/Linkage+Group+11",
                "http://wiki.zfin.org/display/prot/Linkage+Group+9",
                "http://nagel.zfin.org/zf_info/zfbook/stages/intro.html",
                "http://nagel.zfin.org/zf_info/zfbook/stages/org.html",
                "http://nagel.zfin.org/zf_info/zfbook/stages/refs.html",
                "http://nagel.zfin.org/zf_info/zfbook/stages/figs/fig3.html",
                "http://nagel.zfin.org/zf_info/zfbook/stages/figs/fig4.html",
                "http://nagel.zfin.org/zf_info/zfbook/stages/figs/fig5.html",
                "http://nagel.zfin.org/action/antibody/detail?antibody.zdbID=ZDB-ATB-090203-2",
                "http://nagel.zfin.org/action/antibody/detail?antibody.zdbID=ZDB-ATB-090204-1",
                "http://nagel.zfin.org/action/antibody/detail?antibody.zdbID=ZDB-ATB-090204-2",
                "http://frost.zfin.org/frost/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-990415-72",
                "http://frost.zfin.org/frost/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-990415-72",
                "http://frost.zfin.org/frost/webdriver?MIval=aa-markerview.apg&OID=ZDB-CDNA-990415-72",
                "http://frost.zfin.org/frost/webdriver?MIval=aa-markerview.apg&OID=ZDB-EFG-990415-72",
                "http://frost.zfin.org/frost/webdriver?MIval=aa-markerview.apg&OID=ZDB-TRANSG-990415-72"};
        IndexingStatistics stats = new IndexingStatistics();
        for (String url : urls) {
            stats.addUrl(url);
        }
        Map statisticsMap = stats.getStatisticsMap();
        assertNotNull(statisticsMap);
        assertEquals(3, statisticsMap.get("AntibodyView"));
        assertEquals(3, statisticsMap.get("MarkerView"));
        assertEquals(2, statisticsMap.get("GeneView"));
        assertEquals(6, statisticsMap.get("ZebrafishBook"));
        assertEquals(2, statisticsMap.get("CommunityWiki"));
    }

    @Before
    public void setup() {
        System.setProperty("COMMUNITY_WIKI_URL", "http://wiki.zfin.org");
        File categoryFile = new File("home", "WEB-INF");
        File file = new File(categoryFile, "conf");
        SiteSearchCategories.init(file.getAbsolutePath(), "site-search-categories.xml");
        SiteSearchCategories.getSearchCategories();

    }
}