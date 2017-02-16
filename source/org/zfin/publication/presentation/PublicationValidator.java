package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.regex.Pattern;

/**
 * Validator that checks for a valid publication zdb ID.
 * ZDB ID is trimmed of trailing and leading blanks.
 * It checks successfully if:
 * 1) zdb ID is not null
 * 2) a) Publication found with given zdbID OR
 * b) zdb ID has nnnnnn-n* pattern and ZDB-PUB- prefixed to it gives a valid Publication.
 */

public class PublicationValidator {
    @Autowired
    private PublicationRepository publicationRepository;

    private static Logger LOG = Logger.getLogger(PublicationValidator.class);
    private static final String ZDB_PUB = "ZDB-PUB-";
    private static final String ZDB_ID_TIME_STAMP_PATTERN = "\\d{6}-\\d+";

    /**
     * Check that a given pub zdb ID is not empty and is found in the database.
     * Optionally, the ZDB-PUB- part can be omitted from the submission and will be added for
     * convenience.
     * If validation finds a problem it puts it onto the Errors object with
     * the field name in the JSP.
     *
     * @param publicationID publication ZDB ID
     * @param field         field name on the JSP
     * @param errors        error object
     */
    public static void validatePublicationID(String publicationID, String field, Errors errors) {
        // trim off trailing blanks
        publicationID = publicationID.trim();


        if (StringUtils.isEmpty(publicationID)) {
            LOG.debug("------- Failed not-null validation. ---");
            errors.rejectValue(field, "pub.empty");
            return;
        }
        if (isShortVersion(publicationID)) {
            publicationID=PublicationValidator.completeZdbID(publicationID);
        } 
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication pub=pr.getPublication(publicationID);
        if (pub==null){
            errors.rejectValue(field, "pub.invalid");
        }

    }

    private static boolean isValidTimeStamp(String publicationID) {
        return Pattern.matches(ZDB_ID_TIME_STAMP_PATTERN, publicationID.trim());
    }

    /**
     * Checks if the zdb ID is a short version of a full zdbID, i.e. a time stamp pattern.
     * leading and trailing white spaces are trimmed off.
     *
     * @param pubZdbID pub zdb ID
     * @return boolean
     */
    public static boolean isShortVersion(String pubZdbID) {
        return pubZdbID != null && isValidTimeStamp(pubZdbID);
    }

    /**
     * If the provided ID is a short version (valid time stamp pattern of a zdbID) then
     * it adds the prefix to make it a full publication zdbID.
     * Otherwise is returns the unchanged string including null.
     *
     * @param pubZdbID zdb ID
     * @return completed string or unaltered string
     */
    public static String completeZdbID(String pubZdbID) {
        if (pubZdbID == null) {
            return null;
        }
        pubZdbID = pubZdbID.trim();
        if (!pubZdbID.startsWith(ZDB_PUB) && isShortVersion(pubZdbID)) {
            return ZDB_PUB + pubZdbID;
        }
        return pubZdbID;
    }
}
