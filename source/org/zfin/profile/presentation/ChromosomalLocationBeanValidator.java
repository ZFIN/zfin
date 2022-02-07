package org.zfin.profile.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.profile.Organization;
import org.zfin.repository.RepositoryFactory;

public class ChromosomalLocationBeanValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return ChromosomalLocationBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ChromosomalLocationBean bean = (ChromosomalLocationBean) o;

        //TODO: add some validation

        if (bean.getZdbID() != null) {
            //errors.rejectValue("zdbID", "marker.supplier.notfound");
        }

    }
}
