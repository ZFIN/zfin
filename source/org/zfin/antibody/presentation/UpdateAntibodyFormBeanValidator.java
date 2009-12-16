package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class UpdateAntibodyFormBeanValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        UpdateAntibodyFormBean formBean = (UpdateAntibodyFormBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyRenamePubZdbID(), formBean.AB_RENAMEPUB_ZDB_ID, errors);

        if (StringUtils.isEmpty(formBean.getAntibodyNewName())) {
            errors.rejectValue("antibodyNewName", "code", "Antibody name cannot be null.");
        }
        if (!StringUtils.isEmpty(formBean.getAntibodyNewName())) {
            Marker m = mr.getMarkerByName(formBean.getAntibodyNewName());
            if (m != null) {
                errors.rejectValue("antibodyNewName", "code", "This antibody already exists");
            }
        }

    }
}
