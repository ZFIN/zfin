package org.zfin.profile.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.profile.Lab;
import org.zfin.repository.RepositoryFactory;

public class LabValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Lab.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Lab lab = (Lab) o;
        Lab existingLab = RepositoryFactory.getProfileRepository().getLabByName(lab.getName());
        if (existingLab != null) {
            errors.rejectValue("name", "lab.name.duplicate", new String[]{existingLab.getZdbID()}, "");
        }
    }
}
