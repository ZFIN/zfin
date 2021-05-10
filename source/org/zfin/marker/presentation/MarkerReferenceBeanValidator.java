package org.zfin.marker.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

public class MarkerReferenceBeanValidator implements Validator {

    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return MarkerReferenceBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MarkerReferenceBean bean = (MarkerReferenceBean) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "zdbID", "pub.empty");
        if (!errors.hasFieldErrors("zdbID")) {
            if (!publicationRepository.publicationExists(bean.getZdbID())) {
                errors.rejectValue("zdbID", "pub.notfound");
            }
        }
    }
}
