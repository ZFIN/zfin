package org.zfin;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.EmailPrivacyPreference;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;

import java.util.Date;
import java.util.UUID;

/**
 * Base class for smoke tests that require an authenticated user.
 * Creates a unique test user before each test and deletes it after.
 */
public class AbstractSecureSmokeTest extends AbstractSmokeTest {

    protected Person person;
    protected String password = UUID.randomUUID().toString();
    private final Logger logger = LogManager.getLogger(AbstractSecureSmokeTest.class);

    public AbstractSecureSmokeTest(WebClient webClient) {
        super(webClient);
    }

    public AbstractSecureSmokeTest() {
    }



    @Before
    public void setUp() throws Exception {
        super.setUp();
        createTestPersonObject();
        insertTestPersonIntoDatabase();
        login(webClient);
    }

    @After
    public void cleanUp() {
        deletePerson();
    }

    private void createTestPersonObject() {
        String uniqueLogin = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueCookie = "cookie_" + UUID.randomUUID();

        person = new Person();
        person.setFirstName("Test");
        person.setLastName("Person");
        person.generateNameVariations();
        person.setEmail("test_" + uniqueLogin + "@test.com");

        EmailPrivacyPreference emailPrivacyPreference = new EmailPrivacyPreference();
        emailPrivacyPreference.setId(3L);
        emailPrivacyPreference.setName(EmailPrivacyPreference.Name.HIDDEN.toString());
        person.setEmailPrivacyPreference(emailPrivacyPreference);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLogin(uniqueLogin);
        accountInfo.setRole("root");
        accountInfo.setPassword(new BCryptPasswordEncoder().encode(password));
        accountInfo.setName("Test Person");
        accountInfo.setLoginDate(new Date());
        accountInfo.setAccountCreationDate(new Date());
        accountInfo.setCookie(uniqueCookie);
        accountInfo.setPerson(person);
        person.setAccountInfo(accountInfo);
    }

    private void insertTestPersonIntoDatabase() {
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(person);
        HibernateUtil.currentSession().flush();
        HibernateUtil.currentSession().evict(person);

        ActiveSource activeSource = RepositoryFactory.getInfrastructureRepository().getActiveSource(person.getZdbID());
        assertNotNull(activeSource);
        HibernateUtil.currentSession().evict(activeSource);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void deletePerson() {
        try {
            ActiveSource activeSource = RepositoryFactory.getInfrastructureRepository().getActiveSource(person.getZdbID());
            HibernateUtil.createTransaction();
            HibernateUtil.currentSession().delete(person);
            if (activeSource != null) {
                HibernateUtil.currentSession().delete(activeSource);
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Failed to clean up test user: " + person.getZdbID(), e);
        }
    }

    public void login(WebClient webClient) throws Exception {
        HtmlPage page = webClient.getPage(secureUrlDomain + "/action/login-redirect");
        HtmlForm loginForm = page.getFormByName("login");
        HtmlInput nameField = loginForm.getInputByName("username");
        nameField.setValueAttribute(person.getAccountInfo().getLogin());
        HtmlInput passwordField = loginForm.getInputByName("password");
        passwordField.setValueAttribute(password);
        HtmlButton loginButton = loginForm.getButtonByName("loginButton");
        try {
            HtmlPage pageLogin = loginButton.click();
        } catch (Throwable t) {
              logger.error("Couldn't login %n" + t.toString());
            // ignore the 404 error
        }
    }


}
