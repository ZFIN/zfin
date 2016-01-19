package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class RequestNewAnatomyTermBeanValidator implements Validator {

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        RequestNewAnatomyTermBean formBean = (RequestNewAnatomyTermBean) command;

        String firstName = formBean.getFirstName();
        if (StringUtils.isEmpty(firstName)) {
            errors.rejectValue("firstName", "code", "First name cannot be null.");
        }

        String lastName = formBean.getLastName();
        if (StringUtils.isEmpty(lastName)) {
            errors.rejectValue("lastName", "code", "Last name cannot be null.");
        }

        String email = formBean.getEmail();
        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", "code", "Email cannot be null.");
        }

        String termDetail = formBean.getTermDetail();
        if (StringUtils.isEmpty(termDetail)) {
            errors.rejectValue("termDetail", "code", "Description cannot be null.");
        }

    }
}

