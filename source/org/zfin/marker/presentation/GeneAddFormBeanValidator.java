package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.publication.presentation.PublicationValidator;

public class GeneAddFormBeanValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return GeneAddFormBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        GeneAddFormBean form = (GeneAddFormBean) o;

        PublicationValidator.validatePublicationID(form.getPublicationId(), "publicationId", errors);

        ValidationUtils.rejectIfEmpty(errors, "type", "gene.type.empty");

        boolean isEFG = StringUtils.equals(form.getType(), Marker.Type.EFG.name());
        String nameValidationResult = NomenclatureValidationService.validateMarkerName(form.getName(), isEFG);
        if (nameValidationResult != null) {
            errors.rejectValue("name", nameValidationResult);
        }


        if (!isEFG) {
            // if this is not an EFG, there is an abbreviation field on the form. make sure
            // it is filled out, not already used, and is just lowercase letters and numbers
            String abbreviationValidationResult = NomenclatureValidationService.validateMarkerAbbreviation(form.getAbbreviation());
            if (abbreviationValidationResult != null) {
                errors.rejectValue("abbreviation", abbreviationValidationResult);
            }
        }
    }
}
