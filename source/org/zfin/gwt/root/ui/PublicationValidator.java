package org.zfin.gwt.root.ui;

/**
 */
public class PublicationValidator implements Validator {

    public final static String ZDB_PUB_PREFIX = "ZDB-PUB-";
    final static int MIN_LENGTH = 16;

    @Override
    public boolean validate(String publicationZdbID, HandlesError handlesError) {

        if ((publicationZdbID == null ||
                publicationZdbID.length() < MIN_LENGTH ||
                false == publicationZdbID.startsWith(ZDB_PUB_PREFIX))) {
            if (handlesError != null) {
                handlesError.setError("Need to attribute name changes. Pub is invalid: " + publicationZdbID);
            }
            return false;
        }
        return true;
    }
}
