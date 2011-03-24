package org.zfin;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSmokeTest extends WebTestCase {

    private static boolean initDatabase = false;

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

    private void initDatabase() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    protected String getApgNonSecureUrl() {
        // http://<stuff>/mutant/webdriver
        return nonSecureUrlDomain + "/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT;
    }

    @Override
    public void setUp() {
        TestConfiguration.configure();
        domain = ZfinPropertiesEnum.DOMAIN_NAME.toString();
        mutant = ZfinPropertiesEnum.MUTANT_NAME.toString();
        nonSecureUrlDomain = ZfinPropertiesEnum.NON_SECURE_HTTP + domain;
        secureUrlDomain = ZfinPropertiesEnum.SECURE_HTTP + domain;
        if (!initDatabase) {
            initDatabase();
        }
        initDatabase = true;
        setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
    }

    private static final Logger LOG = Logger.getLogger(AbstractSmokeTest.class);

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

    /**
     * Verifies that for a given page there is a span-tag with a given title.
     * If not this method call fail() and returns false;
     * The fail() will give an error message provided.
     * The error message will replace ${title} with the title provided.
     *
     * @param page         HtmlPage
     * @param title        title string
     * @param url          url
     * @param errorMessage error message
     * @return true or false
     */
    public static boolean checkForSpanTitle(HtmlPage page, String title, String url, String spanBody, String errorMessage) {
        List<HtmlSpan> element = (List<HtmlSpan>) page.getByXPath("//span[@title='" + title + "']");
        if (element == null || element.size() == 0) {
            String finalErrorMessage = errorMessage.replace("${title}", title);
            fail(finalErrorMessage + " on page " + url);
            return false;
        }
        HtmlSpan htmlSpan = element.get(0);
        assertNotNull(htmlSpan);
        if (spanBody != null)
            assertEquals(spanBody, htmlSpan.getTextContent());
        return true;
    }

}
