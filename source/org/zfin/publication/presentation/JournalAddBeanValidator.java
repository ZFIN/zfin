package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.presentation.JournalAddBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

public class JournalAddBeanValidator implements Validator {

    private PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return JournalAddBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        JournalAddBean form = (JournalAddBean) o;

        if (form.getPrintIssn()==null && form.geteIssn()==null) {
            errors.rejectValue("ISSN", "issn.empty");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "journal.name.empty");
        if (form.getPrintIssn()!=null) {
            if (pubRepository.getJournalByPrintIssn(form.getPrintIssn()) != null) {
                errors.rejectValue("issn", "Journal.printISSN.inuse");
            }
        }
        if (form.geteIssn()!=null) {
            if (pubRepository.getJournalByEIssn(form.geteIssn()) != null) {
                errors.rejectValue("issn", "Journal.eISSN.inuse");
            }
        }
        }
    }

