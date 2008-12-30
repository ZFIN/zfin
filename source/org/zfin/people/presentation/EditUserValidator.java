package org.zfin.people.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.people.Person;
import org.zfin.people.User;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;


/**
 * Class CandidateBeanValidator.
 * ToDo: Put some of the validation into a central place so it can be reused in other
 * controllers.
 */
public class EditUserValidator implements Validator {

    public boolean supports(Class clazz) {
        return clazz.equals(ProfileBean.class);
    }

    public void validate(Object command, Errors errors) {

        ProfileBean profileBean = (ProfileBean) command;

        if (!profileBean.deleteRecord()) {
            ValidationUtils.rejectIfEmpty(errors, "user.zdbID", "code", "No user zdb ID found.");
            ValidationUtils.rejectIfEmpty(errors, "passwordOne", "code", "No password provided.");
            ValidationUtils.rejectIfEmpty(errors, "passwordTwo", "code", "No password provided.");
            ValidationUtils.rejectIfEmpty(errors, "user.login", "code", "No login provided.");
            ValidationUtils.rejectIfEmpty(errors, "user.role", "code", "No role provided.");

            // Check for double passwords
            String passwordOne = profileBean.getPasswordOne();
            if (passwordOne == null)
                return;
            String passwordTwo = profileBean.getPasswordTwo();
            if (passwordTwo == null)
                return;
            if (!passwordOne.equals(passwordTwo))
                errors.rejectValue("passwordOne", "code", "The two passwords are not the same");
        }
        Person submitPerson = Person.getCurrentSecurityUser();
        // if submitter changes own records
        if (submitPerson.getZdbID().equals(profileBean.getUser().getZdbID())) {
            // if submit roles
            if (submitPerson.getUser().getRole().equals(User.Role.SUBMIT.toString())) {
                // cannot change role
                if (!profileBean.getUser().getRole().equals(User.Role.SUBMIT.toString()))
                    errors.rejectValue("user.login", "code", "Your access level does not allowed to change your role");
            }
        }

        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        // check for login uniqueness if a new user should be created
        if (profileBean.isNewUser()) {
            if (pr.userExists(profileBean.getUser().getLogin()))
                errors.rejectValue("user.login", "code", "LOGIN name '" + profileBean.getUser().getLogin() + " is already in use. " +
                        "Please choose a different name.");

        } else {
            // check that the change in the login name does not conflict with an existing login account
            User user = pr.getUser(profileBean.getUser().getZdbID());
            if (!user.getLogin().equals(profileBean.getUser().getLogin())) {
                if (pr.userExists(profileBean.getUser().getLogin()))
                    errors.rejectValue("user.login", "code", "LOGIN name '" + profileBean.getUser().getLogin() + " is already in use. " +
                            "Please choose a different name.");
            }
        }


    }

}