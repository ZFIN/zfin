package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

public class LinkDisplayValidator implements Validator {

    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return LinkDisplay.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        LinkDisplay link = (LinkDisplay) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "referenceDatabaseZdbID", "marker.link.database.empty");
        if (!errors.hasFieldErrors("referenceDatabaseZdbID")) {
            if (sequenceRepository.getReferenceDatabaseByID(link.getReferenceDatabaseZdbID()) == null) {
                errors.rejectValue("referenceDatabaseZdbID", "marker.link.database.notfound");
            }
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accession", "marker.link.accession.empty");

        if (CollectionUtils.isEmpty(link.getReferences())) {
            errors.rejectValue("references", "pub.empty");
        } else {
            for (MarkerReferenceBean reference : link.getReferences()) {
                PublicationValidator.validatePublicationID(reference.getZdbID(), "references", errors);
            }
        }
    }
}
