package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Convenience class that contains validation logic for
 * presentation purposes.
 */
public class PublicationValidator {

    private static Logger LOG = Logger.getLogger(PublicationValidator.class);

    /**
     * Check that a given pub zdb ID is not empty and is found in the database.
     * If validation finds a problem it puts it onto the Errors object with
     * the field name in the JSP.
     *
     * @param publicationID publication ZDB ID
     * @param field         field name on the JSP
     * @param errors        error object
     */
    public static void validatePublicationID(String publicationID, String field, Errors errors) {
        if (StringUtils.isEmpty(publicationID)) {
            LOG.debug("------- Failed not-null validation. ---");
            errors.rejectValue(field, "code", "Publication attribution can not be null. Please enter a valid ZDB ID.");
            return;
        }

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        if (pr.getPublication(publicationID.trim()) == null) {
            LOG.debug("----------Failed zdb id validation --");
            errors.rejectValue(field, "code", publicationID + " is not an publication zdb id in ZFIN.");
        }
    }
}
