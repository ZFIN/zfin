package org.zfin.antibody.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.apache.commons.lang.StringUtils;

public class CreateAntibodyFormBeanValidator implements Validator {
   private MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        CreateAntibodyFormBean formBean = (CreateAntibodyFormBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyPublicationZdbID(), formBean.AB_PUBLICATION_ZDB_ID, errors);

        if (StringUtils.isEmpty(formBean.getAntibodyName())) {
                errors.rejectValue("antibodyName", "code", "Antibody name cannot be null.");
        }
        if (!StringUtils.isEmpty(formBean.getAntibodyName())) {
           Marker m=mr.getMarkerByAbbreviation(formBean.getAntibodyName());
             if (m!=null) {
             errors.rejectValue("antibodyName", "code", "This marker already exists");
             }
        }
         if (!StringUtils.isEmpty(formBean.getAntibodyName())) {
           Marker marker=mr.getMarkerByName(formBean.getAntibodyName());
             if (marker!=null) {
             errors.rejectValue("antibodyName", "code", "This marker already exists");
        }
         }

    }
}

