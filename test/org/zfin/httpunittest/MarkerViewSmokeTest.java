package org.zfin.httpunittest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;
import org.zfin.marker.Marker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@RunWith(Parameterized.class)
public class MarkerViewSmokeTest extends AbstractSmokeTest {


    public MarkerViewSmokeTest(WebClient webClient) {
        super(webClient);
    }

    private static final Logger LOG = LogManager.getLogger(MarkerViewSmokeTest.class);

    static HashMap<String, String> markerTypes = new HashMap<>();

    static {
        markerTypes.put("fgf8a", "Gene");
        markerTypes.put("zgc:55262", "Gene");
        markerTypes.put("si:dkey-21k10.2", "Gene");
        markerTypes.put("cb46", "Clone");
        markerTypes.put("MGC:55191", "Clone");
        markerTypes.put("CH73-1F23", "Clone");
        markerTypes.put("BUSM1-25N23", "Clone");
    }


    @Test
    public void testGenePagePhenotypeAnchor() throws IOException {
        // gene eya1 with phenotype data
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-GENE-990712-18");
        List<?> pubs = page.getByXPath("//a[@name='phenotype']");
        assertEquals(1, pubs.size());
    }


    @Test
    public void markerViewPages() throws Exception {
        for (String markerName : markerTypes.keySet()) {
            Marker marker = getMarkerRepository().getMarkerByAbbreviation(markerName);
            if (marker == null) {
                LOG.error("No Marker found with symbol: " + markerName);
                break;
            }
            HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/" + marker.getZdbID());
            assertNotNull(page);
            String type = markerTypes.get(markerName);
            String title = "ZFIN " + type + ": " + markerName;
            assertEquals(title, page.getTitleText());

        }
    }

}
