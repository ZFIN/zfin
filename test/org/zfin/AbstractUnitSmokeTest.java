package org.zfin;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractUnitSmokeTest extends WebTestCase {

    protected String mutant;
    protected String domain;

    protected String nonSecureUrlDomain;
    protected String secureUrlDomain;

    //TODO: use google analytics to determine representative browsers
    protected final WebClient[] curationWebClients = {
            new WebClient(BrowserVersion.FIREFOX_38),
            new WebClient(BrowserVersion.INTERNET_EXPLORER),
//            new WebClient(BrowserVersion.SAFARI),
    };

    protected final WebClient[] publicWebClients = {
            new WebClient(BrowserVersion.FIREFOX_38),
            new WebClient(BrowserVersion.INTERNET_EXPLORER),
//            new WebClient(BrowserVersion.SAFARI),
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

    private static final Logger LOG = LogManager.getLogger(AbstractUnitSmokeTest.class);

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
