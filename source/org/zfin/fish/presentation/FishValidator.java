package org.zfin.fish.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.antibody.presentation.AntibodyBean;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;


public class FishValidator implements Validator {

    public boolean supports(Class aClass) {
        return aClass.isAssignableFrom(Fish.class);
    }

    public void validate(Object id, Errors errors) {
        String fishID = (String) id;

        if (fishID == null) {
            errors.rejectValue("fish", "code", "Fish ID cannot be null: ");
            return;
        }
        if (StringUtils.isEmpty(fishID)) {
            errors.rejectValue("fish", "code", "Fish ID cannot be empty: ");
        }
    }
}
