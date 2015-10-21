package org.zfin.orthology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.orthology.OrthologExternalReference;

/**
 * Presentation Class to create output from a Orthology object.
 */
public class OrthologyPresentation extends EntityPresentation {

    public static String getLink(OrthologExternalReference reference) {
        String url = reference.getReferenceDatabase().getBaseURL() + reference.getAccessionNumber();
        String text = String.format("%s:%s",
                reference.getReferenceDatabase().getForeignDB().getDbName().toString(),
                reference.getAccessionNumber());
        return getGeneralHyperLink(url, text);
    }

}
