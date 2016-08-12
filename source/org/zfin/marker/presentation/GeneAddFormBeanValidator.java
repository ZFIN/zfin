package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class GeneAddFormBeanValidator implements Validator {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return GeneAddFormBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        GeneAddFormBean form = (GeneAddFormBean) o;

        PublicationValidator.validatePublicationID(form.getPublicationId(), "publicationId", errors);

        ValidationUtils.rejectIfEmpty(errors, "type", "gene.type.empty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "gene.name.empty");
        if (markerRepository.getMarkerByName(form.getName()) != null) {
            errors.rejectValue("name", "gene.name.inuse");
        }

        if (StringUtils.equals(form.getType(), Marker.Type.EFG.name())) {
            // if this is an EFG, abbreviation is not on the form, and the abbreviation will
            // be set using the name value, so make sure it isn't used
            if (markerRepository.isMarkerExists(form.getName())) {
                errors.rejectValue("name", "gene.name.inuse");
            }
        } else {
            // if this is not an EFG, there is an abbrevation field on the form. make sure
            // it is filled out and not already used
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "abbreviation", "gene.abbreviation.empty");
            if (markerRepository.isMarkerExists(form.getAbbreviation())) {
                errors.rejectValue("abbreviation", "gene.abbreviation.inuse");
            }
        }
    }
}
