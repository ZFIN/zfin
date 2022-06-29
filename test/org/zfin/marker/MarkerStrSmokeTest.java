package org.zfin.marker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;
import org.zfin.framework.api.JsonResultResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

@RunWith(Parameterized.class)
public class MarkerStrSmokeTest extends AbstractSmokeTest {

    public MarkerStrSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * Check if the foxa3 page is accessible
     */
    @Test
    public void testStrsAreLoadingForFoxa3() {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);

        try {
            Page page = webClient.getPage(nonSecureUrlDomain + "/action/api/marker/ZDB-GENE-980526-423/strs");
            WebResponse response = page.getWebResponse();
            assertEquals("application/json", response.getContentType());

            String jsonString = response.getContentAsString();

            ObjectMapper mapper = new ObjectMapper();
            JsonResultResponse deserializedResponse = mapper.readValue(jsonString, JsonResultResponse.class);

            Collection<LinkedHashMap> STRs = deserializedResponse.getResults();
            assertEquals(4, STRs.size());

            for( LinkedHashMap str : STRs ) {
                ArrayList<LinkedHashMap> genomicFeatures = (ArrayList) str.get("genomicFeatures");
                for (LinkedHashMap feature : genomicFeatures) {
                    assertNotNull(feature.get("zdbID"));
                }
            }

        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
