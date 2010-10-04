package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class CreateAntibodyFormBeanValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        CreateAntibodyFormBean formBean = (CreateAntibodyFormBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyPublicationZdbID(), CreateAntibodyFormBean.AB_PUBLICATION_ZDB_ID, errors);

        String antibodyName = formBean.getAntibodyName();
        if (StringUtils.isEmpty(antibodyName)) {
            errors.rejectValue("antibodyName", "code", "Antibody name cannot be null.");
        }
        if (!StringUtils.isEmpty(antibodyName)) {
            if (mr.isMarkerExists(antibodyName)) {
                errors.rejectValue("antibodyName", "code", "The marker abbreviation [" + antibodyName + "] is already taken by another marker");
                return;
            }
            Marker marker = mr.getMarkerByName(antibodyName);
            if (marker != null) {
                errors.rejectValue("antibodyName", "code", "The marker name [" + antibodyName + "] is already taken by another marker");
            }
        }

    }
}

