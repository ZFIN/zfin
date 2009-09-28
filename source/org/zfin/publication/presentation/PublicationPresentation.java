package org.zfin.publication.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.publication.Publication;
import org.zfin.properties.ZfinProperties;

/**
 * Presentation Class to create output from a Publication object.
 */
public class PublicationPresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-pubview2.apg&OID=";

    /**
     * Generates a Publication link using the name.
     *
     * @return html for Publication link
     * @param publication Publication
     */
    public static String getLink(Publication publication) {
        return getWebdriverLink(uri, publication.getZdbID(), publication.getShortAuthorList());
    }

    public static String getLink(Publication publication, String linkContent) {
        return getWebdriverLink(uri, publication.getZdbID(), linkContent);
    }

    public static String getWikiLink(Publication publication) {
        return getWikiLink(ZfinProperties.getWebDriver()+uri,publication.getZdbID(),publication.getAuthors(),publication.getTitle()) ;
    }
}
