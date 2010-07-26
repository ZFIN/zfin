package org.zfin;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSmokeTest extends WebTestCase {

    protected String mutant = System.getenv("MUTANT_NAME");
//    protected String mutant = "ogon" ;
    protected String domain = System.getenv("DOMAIN_NAME");
//    protected String domain = "ogon.zfin.org" ;
    protected WebClient webClient;

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

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Override
    public void setUp() {
        TestConfiguration.configure();
        setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
    }

    @Override
    protected void tearDown() throws Exception {
        if(webClient!=null){
            webClient.closeAllWindows();
        }
    }

}
