package org.zfin.anatomy.presentation;

//import org.apache.commons.lang.StringUtils;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class RequestNewAnatomyTermBeanValidator implements Validator {

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        RequestNewAnatomyTermBean formBean = (RequestNewAnatomyTermBean) command;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "term.firstname.empty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "term.lastname.empty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "term.email.empty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "termDetail", "term.description.empty");


        /*
        String firstName = formBean.getFirstName();
        if (StringUtils.isEmpty(firstName)) {
            errors.rejectValue("firstName", "term.firstname.empty");
        }

        String lastName = formBean.getLastName();
        if (StringUtils.isEmpty(lastName)) {
            errors.rejectValue("lastName", "term.lastname.empty");
        }

        String email = formBean.getEmail();
        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", "term.email.empty");
        }

        String termDetail = formBean.getTermDetail();
        if (StringUtils.isEmpty(termDetail)) {
            errors.rejectValue("termDetail", "term.description.empty");
        }
                */
    }
}

