package org.zfin.people.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class EditUserPasswordController extends SimpleFormController {

    private static final Logger LOG = Logger.getLogger(EditUserPasswordController.class);

    private ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
    private String salt;

    public EditUserPasswordController() {
        setCommandClass(ProfileBean.class);
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        ProfileBean profileBean = (ProfileBean) command;

        String personID = profileBean.getPerson().getZdbID();
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isEmpty(personID)) {
            setFormView("record-not-found.page");
            map.put(LookupStrings.ZDB_ID, "");
            return map;
        }

        Person person = profileRepository.getPerson(personID);
        AccountInfo accountInfo = person.getAccountInfo();
        if (accountInfo == null) {
            accountInfo = new AccountInfo();
            String fullName = person.getFullName();
            int indexOfComma = fullName.indexOf(",");
            String lastName = fullName.substring(0, indexOfComma).trim();
            String firstName = fullName.substring(indexOfComma + 1).trim();
            accountInfo.setName(firstName + " " + lastName);
            person.setAccountInfo(accountInfo);
            profileBean.setNewUser(true);
        }
        profileBean.setPerson(person);
        profileBean.setAccountInfo(person.getAccountInfo());
        setFormView("edit-user-password.page");
        map.put(LookupStrings.FORM_BEAN, profileBean);
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        ProfileBean bean = (ProfileBean) command;

        // handle delete action
        if (bean.deleteRecord()) {
            deleteAccountInfo(bean);
            //redirect to confirmation page
            // need this redirect to switch from HTTPS to HTTP
            String url = "/action/people/edit-user-confirmation?action=" +
                    ProfileBean.ACTION_DELETE + "&person.zdbID=" + bean.getPerson().getZdbID();
            return new ModelAndView(new RedirectView(url));
        }

        editOrCreateAccountInfo(bean);
        //redirect to confirmation page
        // need this redirect to switch from HTTPS to HTTP
        String url = "/action/people/edit-user-confirmation?action=" +
                ProfileBean.ACTION_EDIT + "&person.zdbID=" + bean.getPerson().getZdbID();
        return new ModelAndView(new RedirectView(url));
    }

    private void deleteAccountInfo(ProfileBean bean) {
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        try {
            tx = session.beginTransaction();
            Person person = profileRepository.getPerson(bean.getPerson().getZdbID());
            profileRepository.deleteAccountInfo(person);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        //person.setAccountInfo(null);
    }

    private void editOrCreateAccountInfo(ProfileBean bean) {
        String passwordFirst = bean.getPasswordOne();
        String passwordSecond = bean.getPasswordTwo();
        if (!StringUtils.isEmpty(passwordFirst) && !StringUtils.isEmpty(passwordSecond)) {
            Session session = HibernateUtil.currentSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Person person = profileRepository.getPerson(bean.getPerson().getZdbID());
                AccountInfo accountInfo = person.getAccountInfo();
                if (accountInfo == null && bean.isNewUser())
                    accountInfo = new AccountInfo();
                Md5PasswordEncoder encoder = new Md5PasswordEncoder();
                String encodedPass = encoder.encodePassword(passwordFirst, salt);
                accountInfo.setPassword(encodedPass);
                if (bean.isNewUser()) {
                    accountInfo.setAccountCreationDate(new Date());
                    accountInfo.setCookie(Math.random() + "-" + bean.getAccountInfo().getLogin());
                    accountInfo.setLogin(bean.getAccountInfo().getLogin());
                    accountInfo.setRole(bean.getAccountInfo().getRole());
                    accountInfo.setName(bean.getAccountInfo().getName());
                    person.setAccountInfo(accountInfo);
                } else {
                    profileRepository.updateAccountInfo(person, bean.getAccountInfo());
                }
                tx.commit();
            } catch (Exception e) {
                try {
                    tx.rollback();
                } catch (HibernateException he) {
                    LOG.error("Error during roll back of transaction", he);
                }
                LOG.error("Error in Transaction", e);
                throw new RuntimeException("Error during transaction. Rolled back.", e);
            }

        }
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}