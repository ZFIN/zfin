package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


public class PublicationFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return PublicationBean.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final PublicationBean formBean = (PublicationBean) o;
        final Publication publication = formBean.getPublication();

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "publication.title", "title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "publication.authors", "authors.empty");
        ValidationUtils.rejectIfEmpty(errors, "publication.journal", "journal.empty");
        ValidationUtils.rejectIfEmpty(errors, "publication.publicationDate", "publicationDate.empty");

        String accessionNumber = formBean.getAccessionNumber();
        if (accessionNumber != null) {
            if (!StringUtils.isNumeric(accessionNumber)) {
                errors.rejectValue("accessionNumber", "accessionNumber.not.number");
            } else {
                publication.setAccessionNumber(Integer.parseInt(accessionNumber));
            }
            if (!errors.hasFieldErrors("accessionNumber")) {
                List<Publication> pubsWithSamePubMedId = RepositoryFactory.getPublicationRepository().getPublicationByPmid(publication.getAccessionNumber());
                if (CollectionUtils.isNotEmpty(pubsWithSamePubMedId)) {
                    // in edit mode if the zdbIDs equal then it is valid...
                    if (formBean.getZdbID() == null || !pubsWithSamePubMedId.get(0).getZdbID().equals(formBean.getZdbID()))
                        errors.rejectValue("accessionNumber", "accessionNumber.duplicate", new String[]{pubsWithSamePubMedId.get(0).getZdbID()}, "");
                }
            }
        }

        if (publication.getType() != null && publication.getType() == Publication.Type.THESIS) {
            if (!publication.getJournal().getName().contains("Thesis")) {
                errors.rejectValue("journal", "journal.type.thesis");
            }
        }
    }

}
