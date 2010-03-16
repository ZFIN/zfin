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
import org.zfin.infrastructure.ActiveSource;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

import java.util.Date;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSecureJWebUnitTest extends WebTestCase{

    protected String mutant = System.getenv("MUTANT_NAME");
    protected String domain = System.getenv("DOMAIN_NAME");
//    protected String domain = "ogon.zfin.org" ;
    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
    protected Person person = null ;
    protected String password = "veryeasypass";

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
        HibernateUtil.createTransaction();
        createPerson();
    }

    @Override
    protected void tearDown() throws Exception {
        webClient.closeAllWindows();
        HibernateUtil.rollbackTransaction();
    }



    public Person getTestPerson() {
        Person person = new Person();
        person.setName("Test Person");
        person.setEmail("Email Address Test");
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLogin("newUser");
        accountInfo.setRole("root");

        String saltedPassword = new Md5PasswordEncoder().encodePassword(password,"dedicated to George Streisinger");
        accountInfo.setPassword(saltedPassword);
        accountInfo.setName("Test Person");
        accountInfo.setLoginDate(new Date());
        accountInfo.setAccountCreationDate(new Date());
        accountInfo.setCookie("somecookie");
        person.setAccountInfo(accountInfo);
        return person;
    }

    protected void createPerson() {
        person = getTestPerson();
        HibernateUtil.currentSession().save(person);
        HibernateUtil.currentSession().flush();

        // need to evict object so that it can be reloaded later
        HibernateUtil.currentSession().evict(person);

        // because persons add active source is added with person, need to evict it, too
        ActiveSource activeSource = RepositoryFactory.getInfrastructureRepository().getActiveSource(person.getZdbID()) ;
        assertNotNull(activeSource);
        HibernateUtil.currentSession().evict(activeSource);
    }

    public void login() throws Exception{
        webClient.setRedirectEnabled(true);
        HtmlPage page = webClient.getPage("http://"+domain +"/action/login");
        HtmlForm loginForm = page.getFormByName("login");
        HtmlInput nameField = loginForm.getInputByName("j_username");
        nameField.setValueAttribute(person.getAccountInfo().getLogin());
        HtmlInput passwordField = loginForm.getInputByName("j_password");
        passwordField.setValueAttribute(password);
        HtmlInput loginButton = loginForm.getInputByName("action");
        try {
            loginButton.click();
        } catch (Throwable t) {
//            t.printStackTrace();
            // ignore the 404 error
        }
    }


}