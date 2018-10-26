package org.zfin.zebrashare.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Objects;

public class SubmissionFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Objects.equals(SubmissionFormBean.class, aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "authors", "authors.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "abstractText", "abstract.empty");
    }
}
