package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.presentation.PublicationValidator;

public class MarkerAliasBeanValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return MarkerAliasBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MarkerAliasBean bean = (MarkerAliasBean) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "alias", "marker.alias.empty");
        if (CollectionUtils.isEmpty(bean.getReferences())) {
            errors.rejectValue("references", "pub.empty");
        } else {
            for (MarkerReferenceBean reference : bean.getReferences()) {
                PublicationValidator.validatePublicationID(reference.getZdbID(), "references", errors);
            }
        }
    }
}
