package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.hibernate.SessionFactory;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;

import java.util.Date;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractJWebUnitTest extends WebTestCase{

    protected String mutant = System.getenv("MUTANT_NAME");
//    protected String mutant = "ogon" ;
    protected String domain = System.getenv("DOMAIN_NAME");
//    protected String domain = "ogon.zfin.org" ;
    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);

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
        webClient.closeAllWindows();
    }

}