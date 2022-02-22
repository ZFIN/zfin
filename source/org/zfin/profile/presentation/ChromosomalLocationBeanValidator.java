package org.zfin.profile.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ChromosomalLocationBeanValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return ChromosomalLocationBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ChromosomalLocationBean bean = (ChromosomalLocationBean) o;

        if (bean.getEntityID() == null) {
            errors.rejectValue("marker.zdbID", "marker.chromosomalLocation.notfound");
        }

        if (StringUtils.isEmpty(bean.getAssembly())) {
            errors.rejectValue("assembly", "marker.chromosomalLocation.assembly.empty");
        }

        if (StringUtils.isEmpty(bean.getChromosome())) {
            errors.rejectValue("chromosome", "marker.chromosomalLocation.chromosome.empty");
        }

        if (
                bean.getStartLocation() != null &&
                bean.getEndLocation() != null &&
                bean.getStartLocation() >= bean.getEndLocation()
        ) {
            errors.rejectValue("startLocation", "marker.chromosomalLocation.startLocation.invalid");
        }

    }
}
