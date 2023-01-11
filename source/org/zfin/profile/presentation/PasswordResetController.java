package org.zfin.profile.presentation;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Person;
import org.zfin.profile.UserService;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.http.HttpServletRequest;

import java.util.Calendar;

import static org.zfin.security.MigratingAuthenticationFailureHandler.LAST_USERNAME_ATTEMPTED;

@Controller
@RequestMapping(value = "/profile")
public class PasswordResetController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;
    
    @RequestMapping(value = "/forgot-password", method = RequestMethod.GET)
    public String forgotPasswordForm() {
        return "profile/forgot-password-form";
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public String forgotPasswordSubmit(@RequestParam String emailOrLogin, Model model) {
        boolean errorsEncountered = false;
        Person person = profileService.getPersonByEmailOrLogin(emailOrLogin);

        if (person == null) {
            model.addAttribute("error", "No user found for email or login: " + emailOrLogin);
            errorsEncountered = true;
        }

        if (!errorsEncountered) {
            try {
                Transaction tx = HibernateUtil.createTransaction();
                UserService.setPasswordResetKey(person);
                tx.commit();
            } catch (Exception e) {
                errorsEncountered = true;
                model.addAttribute("error", "Error while resetting password");
                return "profile/forgot-password-form";
            }
        }

        if (!errorsEncountered) {
            MailSender mailer = AbstractZfinMailSender.getInstance();

            String url = "https://"
                    + ZfinPropertiesEnum.DOMAIN_NAME.value()
                    + "/action/profile/password-reset/"
                    + person.getZdbID()
                    + "?resetKey="
                    + person.getAccountInfo().getPasswordResetKey();

            String body = "<a href=\"" + url + "\">Follow this link</a> to reset the password for your ZFIN account " +
                    "(" + person.getAccountInfo().getLogin() + ").";

            boolean sent = mailer.sendHtmlMail(
                    "Reset ZFIN Password",
                    body,
                    false,
                    "ZFIN Admin <zfinadmn@zfin.org>",
                    new String[] { person.getFirstName() + " "
                            + person.getLastName()
                            + " <" + person.getEmail() + "> "});
            if (!sent) {
                model.addAttribute("errorMessage", "Unable to send email, please contact zfinadmn@zfin.org");
            }
        }

        return "profile/forgot-password-response";
    }

    @RequestMapping(value = "/password-reset/{zdbId}", method = RequestMethod.GET)
    public String passwordResetForm(@PathVariable String zdbId,
                                    @RequestParam String resetKey,
                                    Model model) {

        model.addAttribute("allowReset", false);
        model.addAttribute("zdbId", zdbId);
        model.addAttribute("resetKey", resetKey);

        Person person = profileRepository.getPerson(zdbId);

        if (person != null
                && person.getAccountInfo() != null
                && UserService.passwordResetKeyIsValid(person.getAccountInfo(), resetKey)) {
            model.addAttribute("allowReset", true);
        } else {
            model.addAttribute("errorMessage", "Password reset key is no longer valid");
        }

        return "profile/reset-password";
    }

    @RequestMapping(value = "/password-reset/{zdbId}", method = RequestMethod.POST)
    public String passwordResetSubmit(@PathVariable String zdbId,
                                      @RequestParam String pass1,
                                      @RequestParam String pass2,
                                      @RequestParam String resetKey,
                                      Model model) {

        Transaction tx = HibernateUtil.createTransaction();
        String page = "profile/reset-password";
        Person person = profileRepository.getPerson(zdbId);

        if (!UserService.passwordResetKeyIsValid(person.getAccountInfo(), resetKey)) {
            //something went wrong if you got here and the key isn't valid
            return page;
        }

        if (person == null
                || StringUtils.isEmpty(pass1)
                || StringUtils.isEmpty(pass2)
                || !UserService.passwordResetKeyIsValid(person.getAccountInfo(), resetKey)) {
            model.addAttribute("allowReset",true);
            model.addAttribute("errorMessage","Something went wrong");
            return page;
        }

        if (!StringUtils.equals(pass1,pass2)) {
            model.addAttribute("allowReset",true);
            model.addAttribute("errorMessage","Passwords entered must match");
            return page;
        }

        if (StringUtils.equals(pass1, person.getAccountInfo().getLogin())) {
            model.addAttribute("allowReset",true);
            model.addAttribute("errorMessage","We won't let you use your username as a password");
            return page;
        }

        person.getAccountInfo().setPassword(profileService.encodePassword(pass1));
        person.getAccountInfo().setPasswordResetDate(null);
        person.getAccountInfo().setPasswordResetKey(null);
        model.addAttribute("resetSuccessful",true);
        tx.commit();

        return page;
    }


    @RequestMapping(value = "/expired-password", method = RequestMethod.GET)
    public String expiredPasswordForm(HttpServletRequest request, Model model) throws Exception {
        String username = (String) request.getSession().getAttribute(LAST_USERNAME_ATTEMPTED);
        if (username == null) {
            throw new Exception("No username found");
        }
        model.addAttribute("username", username);
        return "profile/expired-password-form";
    }

}
