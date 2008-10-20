package org.zfin.people.presentation;

import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.User;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

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

        String personID = request.getParameter("user.zdbID");
        if (StringUtils.isEmpty(personID)) {
            // error page
        }

        User user = profileRepository.getUser(personID);
        if (user != null)
            profileBean.setUser(user);
        else {
            user = new User();
            user.setZdbID(personID);
            Person person = profileRepository.getPerson(personID);
            String fullName = person.getFullName();
            int indexOfComma = fullName.indexOf(",");
            String lastName = fullName.substring(0, indexOfComma).trim();
            String firstName = fullName.substring(indexOfComma + 1).trim();
            user.setName(firstName + " " + lastName);
            profileBean.setNewUser(true);
            profileBean.setUser(user);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, profileBean);
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        ProfileBean bean = (ProfileBean) command;
        Person submitUser = Person.getCurrentSecurityUser();
        // check for invalid zdbID Write validator class

        // handle delete action
        if (bean.deleteRecord()) {
            deleteUser(bean);
            //redirect to confirmation page
            // need this redirect to switch from HTTPS to HTTP
            String url = "/action/people/edit-user-confirmation?action=" +
                    ProfileBean.ACTION_DELETE + "&user.zdbID=" + bean.getUser().getZdbID();
            return new ModelAndView(new RedirectView(url));
        }

        editOrCreateUser(bean);
        //redirect to confirmation page
        // need this redirect to switch from HTTPS to HTTP
        String url = "/action/people/edit-user-confirmation?action=" +
                ProfileBean.ACTION_EDIT + "&user.zdbID=" + bean.getUser().getZdbID();
        return new ModelAndView(new RedirectView(url));
    }

    private void deleteUser(ProfileBean bean) {
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        try {
            tx = session.beginTransaction();
            User user = profileRepository.getUser(bean.getUser().getZdbID());
            profileRepository.delete(user);
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

    private void editOrCreateUser(ProfileBean bean) {
        String passwordFirst = bean.getPasswordOne();
        String passwordSecond = bean.getPasswordTwo();
        if (!StringUtils.isEmpty(passwordFirst) && !StringUtils.isEmpty(passwordSecond)) {
            Session session = HibernateUtil.currentSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Person person = profileRepository.getPerson(bean.getUser().getZdbID());
                User user = person.getUser();
                if (user == null && bean.isNewUser())
                    user = new User();
                user.setName(bean.getUser().getName());
                user.setRole(bean.getUser().getRole());
                user.setLogin(bean.getUser().getLogin());
                Md5PasswordEncoder encoder = new Md5PasswordEncoder();
                String encodedPass = encoder.encodePassword(passwordFirst, salt);
                user.setPassword(encodedPass);
                if (bean.isNewUser()){
                    user.setAccountCreationDate(new Date());
                    user.setPerson(person);
                    user.setCookie(Math.random() + "-" + bean.getUser().getLogin());
                    person.setUser(user);
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