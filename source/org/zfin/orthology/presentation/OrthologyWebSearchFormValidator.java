package org.zfin.orthology.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

/**
 * This class initiates the validation of the input given by the user in the orhtology search form.
 */
public class OrthologyWebSearchFormValidator implements Validator {

    public boolean supports(Class clazz) {
        return OrthologySearchBean.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        OrthologySearchBean bean = (OrthologySearchBean) target;
        List<SpeciesCriteriaBean> criteriaBeans = bean.getCriteria();
        if (criteriaBeans == null) {
            errors.rejectValue("zebrafishCriteriaBean.geneSearchTerm", "code", "No Species is provided");
            return;
        }

        // Case: specify species selected but no species checked
        if (criteriaBeans.size() == 1 && !bean.isAnyComparisonSpecies()) {
            errors.rejectValue("zebrafishCriteriaBean.geneSearchTerm", "code", "Please select at least one Comparison Species");
            return;
        }

        for (SpeciesCriteriaBean criteria : criteriaBeans) {

            OrthologyFormValidator validator = new OrthologyFormValidator(criteria.getName(),
                    "symbol", criteria.getGeneSymbolFilterType(), criteria.getGeneSearchTerm());
            String errorField = getErrorFieldValue(criteria);
            if (!validator.isValid()) {
                errors.rejectValue(errorField + ".geneSearchTerm", "code", validator.getErrors().getErrors().get(0));
            }

            validator = new OrthologyFormValidator(criteria.getName(),
                    "chromosome", criteria.getChromosomeFilterType(), criteria.getChromosome());
            if (!validator.isValid()) {
                errors.rejectValue(errorField + ".chromosome", "code", validator.getErrors().getErrors().get(0));
            }
        }


    }

    private String getErrorFieldValue(SpeciesCriteriaBean criteria) {
        return criteria.getName().toLowerCase() + "CriteriaBean";
    }
}
