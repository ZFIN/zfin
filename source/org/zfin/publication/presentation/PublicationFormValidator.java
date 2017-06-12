package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


public class PublicationFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Publication.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final Publication form = (Publication) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "authors", "authors.empty");
        ValidationUtils.rejectIfEmpty(errors, "journal", "journal.empty");
        ValidationUtils.rejectIfEmpty(errors, "publicationDate", "publicationDate.empty");

        if (form.getAccessionNumber() != null && form.getAccessionNumber() != null) {

            if (!errors.hasFieldErrors("accessionNumber")) {
                List<Publication> pubsWithSamePubMedId = RepositoryFactory.getPublicationRepository().getPublicationByPmid(form.getAccessionNumber());
                CollectionUtils.filter(pubsWithSamePubMedId, new Predicate() {
                    @Override
                    public boolean evaluate(Object o) {
                        return !((Publication) o).getZdbID().equals(form.getZdbID());
                        }
                });
                if (CollectionUtils.isNotEmpty(pubsWithSamePubMedId)) {
                    errors.rejectValue("accessionNumber", "accessionNumber.duplicate", "This PubMedID is already used by " + pubsWithSamePubMedId.get(0).getZdbID());
                }
            }
        }

        if (form.getType() != null && form.getType() == Publication.Type.THESIS) {
            if (!form.getJournal().getName().contains("Thesis")) {
                errors.rejectValue("journal", "journal.type.thesis");
            }
        }
    }

}
