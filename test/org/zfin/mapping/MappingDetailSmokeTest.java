package org.zfin.mapping;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.util.List;

@SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
@RunWith(Parameterized.class)
public class MappingDetailSmokeTest extends AbstractSmokeTest {

    public MappingDetailSmokeTest(WebClient webClient) {
        super(webClient);
    }

    @Test
    public void checkMappingDetailPage() {
        try {
            // pax2a
            HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/mapping/detail/ZDB-GENE-990415-8");
            assertEquals("ZFIN Mapping: pax2a", page.getTitleText());
            // check that this clone is in the 'Mapped Clones containing pax2a' list
            List<?> byXPath = page.getByXPath("//span[@title = 'DKEY-254E7']");
            assertNotNull(byXPath);
            assertNotNull(byXPath.get(0));
            // check that T51 is in the genetic mapping panels section
            byXPath = page.getByXPath("//a[@id = 'ZDB-REFCROSS-990707-1']");
            assertNotNull(byXPath);
            assertNotNull(byXPath.get(0));
            // check that 'fgf8a' is in the mapping from publication section
            byXPath = page.getByXPath("//span[@title = 'fibroblast growth factor 8a']");
            assertNotNull(byXPath);
            assertNotNull(byXPath.get(0));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void checkMappingPanel() {
        try {
            // T51 mapping panel
            HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/mapping/panel-detail/ZDB-REFCROSS-990707-1");
            assertEquals("ZFIN Mapping Panel: Goodfellow T51", page.getTitleText());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void checkScoringPage() {
        try {
            // scoring data for pax2a on HS panel
            HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/mapping/show-scoring?panelID=ZDB-REFCROSS-000320-1&markerID=ZDB-GENE-990415-8&lg=13");
            assertEquals("ZFIN Scoring: pax2a on Heat Shock", page.getTitleText());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void checkFeaturePage() {
        try {
            // scoring data for ti282a
            HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/mapping/detail/ZDB-ALT-980203-1091");
            assertEquals("ZFIN Mapping: ti282a", page.getTitleText());
            List<?> byXPath = page.getByXPath("//a[@id = 'ZDB-ALT-980203-1091']");
            assertNotNull(byXPath);
            assertNotNull(byXPath.get(0));
            HtmlAnchor anchor = (HtmlAnchor) byXPath.get(0);
            assertEquals("ti282a", anchor.getTextContent());

        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
