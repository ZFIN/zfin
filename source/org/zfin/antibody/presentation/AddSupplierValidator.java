package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Organization;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Prita Mani
 * Date: Jul 15, 2008
 * Time: 11:38:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddSupplierValidator implements Validator {

    public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
        if (StringUtils.isEmpty(formBean.getSupplierName())) {
            errors.rejectValue("supplierNameErrorString", "code", " Supplier cannot be null.");
            return;
        }
        if (!StringUtils.isEmpty(formBean.getSupplierName())) {

            Organization or = profileRepository.getOrganizationByName(formBean.getSupplierName());
            if (or == null) {
                errors.rejectValue("supplierNameErrorString", "code", formBean.getSupplierName() + " is not a valid supplier in ZFIN.");
                return;
            }
        }
        Organization or = profileRepository.getOrganizationByName(formBean.getSupplierName());
        MarkerSupplier sup = profileRepository.getSpecificSupplier(formBean.getAntibody(), or);
        if (sup != null) {
            errors.rejectValue("supplierNameErrorString", "code", " This organization has already been added to the list of suppliers of this antibody.");
        }
    }
}
