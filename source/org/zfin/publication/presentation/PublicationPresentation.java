package org.zfin.publication.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;

/**
 * Presentation Class to create output from a Publication object.
 */
public class PublicationPresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-pubview2.apg&OID=";

    /**
     * Generates a Publication link using the name.
     *
     * @param publication Publication
     * @return html for Publication link
     */
    public static String getLink(Publication publication) {
        return getWebdriverLink(uri, publication.getZdbID(), publication.getShortAuthorList(), publication.getZdbID());
    }

    public static String getLink(Publication publication, String linkContent) {
        return getLink(publication.getZdbID(), linkContent);
    }

    public static String getLink(String publicationZdbID, String linkContent) {
        return getWebdriverLink(uri, publicationZdbID, linkContent);
    }

    public static String getWikiLink(Publication publication) {
        return getWikiLink(ZfinProperties.getWebDriver() + uri, publication.getZdbID(), publication.getAuthors(), publication.getTitle());
    }
}
