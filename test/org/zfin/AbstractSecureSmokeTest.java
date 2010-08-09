package org.zfin;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.util.Date;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class AbstractSecureSmokeTest extends AbstractSmokeTest {

    protected Person person = null;
    protected String password = "veryeasypass";
    private final Logger logger = Logger.getLogger(AbstractSecureSmokeTest.class) ;

    @Override
    public void setUp() {
        super.setUp();
        createPerson();
    }

    @Override
    protected void tearDown() throws Exception {
        deletePerson();
        super.tearDown();
    }


    public Person getTestPerson() {
        Person person = new Person();
        person.setName("Test Person");
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
        return person;
    }

    protected void createPerson() {
        HibernateUtil.createTransaction();
        person = getTestPerson();
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
        if(activeSource!=null){
            HibernateUtil.currentSession().delete(activeSource);
        }
        else{
            logger.error("unable to delete user: "+ person.getZdbID() + " "+ person.getName());
        }
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public void login() throws Exception {
        webClient.setRedirectEnabled(true);
        HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + "/action/login");
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