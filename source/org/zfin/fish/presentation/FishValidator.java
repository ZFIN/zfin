package org.zfin.fish.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


public class FishValidator implements Validator {

    public boolean supports(Class aClass) {
        return aClass.isAssignableFrom(MartFish.class);
    }

    public void validate(Object id, Errors errors) {
        String fishID = (String) id;

        if (fishID == null) {
            errors.rejectValue("fish", "code", "MartFish ID cannot be null: ");
            return;
        }
        if (StringUtils.isEmpty(fishID)) {
            errors.rejectValue("fish", "code", "MartFish ID cannot be empty: ");
        }
    }
}
