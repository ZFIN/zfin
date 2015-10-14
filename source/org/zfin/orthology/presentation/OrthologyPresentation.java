package org.zfin.orthology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.orthology.OrthologExternalReference;
import org.zfin.orthology.OrthologySpecies;

/**
 * Presentation Class to create output from a Orthology object.
 */
public class OrthologyPresentation extends EntityPresentation {

    private static final String uri = "/action/orthology/";
    public static final String ORTHOLOGY_DETAILS = "Orthology Details";

    /**
     * Generates a OrthologySpecies link using the name.
     *
     * @return html for Orthology link
     * @param orthologySpecies OrthologySpecies
     */
    public static String getLink(OrthologySpecies orthologySpecies) {
        return getWebdriverLink(uri, orthologySpecies.getMarker().getZdbID(), ORTHOLOGY_DETAILS);
    }

    public static String getLink(OrthologExternalReference reference) {
        String url = reference.getReferenceDatabase().getBaseURL() + reference.getAccessionNumber();
        String text = String.format("%s:%s",
                reference.getReferenceDatabase().getForeignDB().getDbName().toString(),
                reference.getAccessionNumber());
        return getGeneralHyperLink(url, text);
    }

}
