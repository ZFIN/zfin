package org.zfin.infrastructure.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

/**
 */
public class ControlledVocabularyAddBeanValidator implements Validator {

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {

        ControlledVocabularyAddBean controlledVocabularyAddBean = (ControlledVocabularyAddBean) command;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "termName", "controlledvocabulary.termname.empty");

        String termname = controlledVocabularyAddBean.getTermName();
        if (!termname.endsWith(".")) {
            errors.rejectValue("termName", "controlledvocabulary.termname.notendswithperiod");
        }

        if (infrastructureRepository.isTermNameForControlledVocabExists(termname)) {
            errors.rejectValue("termName", "controlledvocabulary.termname.inuse");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "foreignSpecies", "controlledvocabulary.foreignspecies.empty");

        String foreignSpecies = controlledVocabularyAddBean.getForeignSpecies();
        if (!StringUtils.isEmpty(foreignSpecies) && infrastructureRepository.isForeignSpeciesForControlledVocabExists(foreignSpecies)) {
            errors.rejectValue("foreignSpecies", "controlledvocabulary.foreignspecies.inuse");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nameDefinition", "controlledvocabulary.namedefinition.empty");

        String nameDefinition = controlledVocabularyAddBean.getNameDefinition();
        if (!StringUtils.isEmpty(nameDefinition) && infrastructureRepository.isNameDefForControlledVocabExists(nameDefinition)) {
            errors.rejectValue("nameDefinition", "controlledvocabulary.namedefinition.inuse");
        }
    }
}
