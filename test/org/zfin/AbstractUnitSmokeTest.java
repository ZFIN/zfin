package org.zfin;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractUnitSmokeTest extends WebTestCase {

    protected String mutant;
    protected String domain;

    protected String nonSecureUrlDomain;
    protected String secureUrlDomain;

    protected final WebClient[] curationWebClients = {
            new WebClient(BrowserVersion.FIREFOX_3),  // 30-50%
            new WebClient(BrowserVersion.INTERNET_EXPLORER_8),  // 20-30%
//            new WebClient(BrowserVersion.SAFARI),  // 20%
    };

    protected final WebClient[] publicWebClients = {
            new WebClient(BrowserVersion.FIREFOX_3),  // 30-50%
            new WebClient(BrowserVersion.INTERNET_EXPLORER_6),  // 10%
            new WebClient(BrowserVersion.INTERNET_EXPLORER_7),  // 10%
            new WebClient(BrowserVersion.INTERNET_EXPLORER_8),  // 20-30%
//            new WebClient(BrowserVersion.SAFARI),  // 20%
    };


    @Override
    public void setUp() {
        TestConfiguration.configure();
        domain = ZfinPropertiesEnum.DOMAIN_NAME.toString();
        mutant = ZfinPropertiesEnum.MUTANT_NAME.toString();
        nonSecureUrlDomain = ZfinPropertiesEnum.NON_SECURE_HTTP + domain;
        secureUrlDomain = ZfinPropertiesEnum.SECURE_HTTP + domain;
        setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
    }

    private static final Logger LOG = Logger.getLogger(AbstractUnitSmokeTest.class);

    @Override
    protected void tearDown() throws Exception {
        for (WebClient client : curationWebClients) {
            try {
                client.closeAllWindows();
            } catch (Exception e) {
                // nothing else we can do
                LOG.error(e);
            }
        }
        for (WebClient client : publicWebClients) {
            try {
                client.closeAllWindows();
            } catch (Exception e) {
                // nothing else we can do
                LOG.error(e);
            }
        }
    }

}
