package org.zfin;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import net.sourceforge.jwebunit.junit.WebTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSmokeTest extends WebTestCase {

    protected static String mutant;
    protected static String domain;

    protected static String nonSecureUrlDomain;
    protected static String secureUrlDomain;
    // browser that is used in a single test run
    // we loop over all browser emulations.
    protected WebClient webClient;

    // used to only initialize this class once for all unit tests run in the sub class.
    protected static boolean isInitialized;

    @Parameterized.Parameters
    public static Collection webClients() {
        return Arrays.asList(new Object[][]{{getBrowserClients()[0]}});
    }

    //TODO: use google analytics to determine representative browsers
    protected final WebClient[] curationWebClients = {
            new WebClient(BrowserVersion.FIREFOX),
            new WebClient(BrowserVersion.INTERNET_EXPLORER),
//            new WebClient(BrowserVersion.SAFARI),
    };

    protected static final WebClient[] publicWebClients = {
            new WebClient(BrowserVersion.FIREFOX),
            new WebClient(BrowserVersion.INTERNET_EXPLORER),
//            new WebClient(BrowserVersion.SAFARI),
    };

    private void initDatabase() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public AbstractSmokeTest(WebClient webClient) {
        this.webClient = webClient;
    }

    public AbstractSmokeTest() {
    }

    @Before
    public void setUp() throws Exception {
        if (isInitialized)
            return;
        TestConfiguration.configure();
        domain = ZfinPropertiesEnum.DOMAIN_NAME.toString();
        mutant = ZfinPropertiesEnum.MUTANT_NAME.toString();
        nonSecureUrlDomain = ZfinPropertiesEnum.NON_SECURE_HTTP + domain;
        secureUrlDomain = ZfinPropertiesEnum.SECURE_HTTP + domain;
        if (ZfinPropertiesEnum.USE_APACHE_FOR_SMOKE_TESTS.value().equals("false")) {
            nonSecureUrlDomain += ":" + ZfinPropertiesEnum.NON_SECUREPORT;
            secureUrlDomain += ":" + ZfinPropertiesEnum.SECUREPORT;
        }
        initDatabase();
        //setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
        isInitialized = true;
    }

    private static final Logger LOG = LogManager.getLogger(AbstractSmokeTest.class);

    @Override
    protected void tearDown() throws Exception {
        for (WebClient client : curationWebClients) {
            try {
                client.close();
            } catch (Exception e) {
                // nothing else we can do
                LOG.error(e);
            }
        }
        for (WebClient client : publicWebClients) {
            try {
                client.close();
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
        List element =  page.getByXPath("//span[@title='" + title + "']");
        if (element == null || element.size() == 0) {
            String finalErrorMessage = errorMessage.replace("${title}", title);
            fail(finalErrorMessage + " on page " + url);
            return false;
        }
        HtmlSpan htmlSpan = (HtmlSpan)element.get(0);
        assertNotNull(htmlSpan);
        if (spanBody != null)
            assertEquals(spanBody, htmlSpan.getTextContent());
        return true;
    }

    public static WebClient[] getBrowserClients() {
        for (WebClient client : publicWebClients) {
            prepare(client);
        }
        return publicWebClients;
    }

    private static void prepare(WebClient webClient) {
        final IncorrectnessListener il = new IncorrectnessListenerImpl();
        webClient.setIncorrectnessListener(il);
        final SilentCssErrorHandler eh = new SilentCssErrorHandler();
        webClient.setCssErrorHandler(eh);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        // increase timeout to 60 seconds
        webClient.getOptions().setTimeout(60000);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        CookieManager cm = new CookieManager();
        webClient.setCookieManager(cm);
        webClient.setJavaScriptTimeout(30000);
        webClient.getOptions().setTimeout(30000);
    }

}
