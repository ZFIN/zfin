package org.zfin.profile.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.profile.Organization;
import org.zfin.repository.RepositoryFactory;

public class SupplierBeanValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return SupplierBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SupplierBean bean = (SupplierBean) o;

        if (bean.getZdbID() != null) {
            Organization org = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(bean.getZdbID());
            if (org == null) {
                errors.rejectValue("zdbID", "marker.supplier.notfound");
            }
        }

        if (bean.getName() != null) {
            Organization org = RepositoryFactory.getProfileRepository().getOrganizationByName(bean.getName());
            if (org == null) {
                errors.rejectValue("name", "marker.supplier.notfound");
            }
        }
    }
}
