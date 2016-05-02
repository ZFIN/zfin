package org.zfin;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;

import java.util.Date;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSecureSmokeTest extends AbstractSmokeTest {

    protected Person person;
    protected String password = "veryeasypass";
    private final Logger logger = Logger.getLogger(AbstractSecureSmokeTest.class);

    public AbstractSecureSmokeTest(WebClient webClient) {
        super(webClient);
    }

    public AbstractSecureSmokeTest() {
    }



    @Before
    public void setUp() throws Exception {
        super.setUp();
        insertTestPersonIntoDatabase();
        login(webClient);
    }

    @Override
    protected void tearDown() throws Exception {
        deletePerson();
        super.tearDown();
    }

    @BeforeClass
    public static void setUpGLobal() {

    }


        private void createTestPersonObject() {

        person = new Person();

        person.setFirstName("Test");
        person.setLastName("Person");
        person.generateNameVariations();
        person.setEmail("Email Address Test");
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLogin("newUser");
        accountInfo.setRole("root");

        String saltedPassword = new Md5PasswordEncoder().encodePassword(password, "dedicated to George Streisinger");
        accountInfo.setPassword(saltedPassword);
        accountInfo.setName("Test Person");
        accountInfo.setLoginDate(new Date());
        accountInfo.setAccountCreationDate(new Date());
        accountInfo.setCookie("somecookie");
        person.setAccountInfo(accountInfo);

    }

    protected void insertTestPersonIntoDatabase() {
        createTestPersonObject();
        Person existingPerson = RepositoryFactory.getProfileRepository().getPersonByName(person.getAccountInfo().getLogin());
        if(existingPerson != null && existingPerson.getAccountInfo() != null && existingPerson.getAccountInfo().getLogin() != null){
            person = existingPerson;
            return;
        }
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(person);
        HibernateUtil.currentSession().flush();

        // need to evict object so that it can be reloaded later
        HibernateUtil.currentSession().evict(person);

        // because persons add active source is added with person, need to evict it, too
        ActiveSource activeSource = RepositoryFactory.getInfrastructureRepository().getActiveSource(person.getZdbID());
        assertNotNull(activeSource);
        HibernateUtil.currentSession().evict(activeSource);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    protected void deletePerson() {
        ActiveSource activeSource = RepositoryFactory.getInfrastructureRepository().getActiveSource(person.getZdbID());
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().delete(person);
        if (activeSource != null) {
            HibernateUtil.currentSession().delete(activeSource);
        } else {
            logger.error("unable to delete user: " + person.getZdbID() + " " + person.getShortName());
        }
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public void login(WebClient webClient) throws Exception {
        HtmlPage page = webClient.getPage(secureUrlDomain + "/action/login-redirect");
        HtmlForm loginForm = page.getFormByName("login");
        HtmlInput nameField = loginForm.getInputByName("username");
        nameField.setValueAttribute(person.getAccountInfo().getLogin());
        HtmlInput passwordField = loginForm.getInputByName("password");
        passwordField.setValueAttribute(password);
        HtmlInput loginButton = loginForm.getInputByName("action");
        try {
            HtmlPage pageLogin = loginButton.click();
        } catch (Throwable t) {
              logger.error("Couldn't login %n" + t.toString());
            // ignore the 404 error
        }
    }


}