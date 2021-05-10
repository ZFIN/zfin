package org.zfin.publication.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.publication.Publication;

/**
 * Presentation Class to create output from a Publication object.
 */
public class PublicationPresentation extends EntityPresentation {

    private static final String person_uri = "profile/view/";

    /**
     * Generates a Publication link using the name.
     *
     * @param publication Publication
     * @return html for Publication link
     */
    public static String getLink(Publication publication) {
        return getViewLinkWithID(publication.getZdbID(), publication.getShortAuthorList(), publication.getZdbID());
    }

    public static String getLink(Publication publication, String linkContent) {
        return getViewLinkWithID(publication.getZdbID(), linkContent, publication.getZdbID());
    }

    /**
     * Per case 8749, an EST could be attributed to a person. So adding this clause to redirect to person page and not pubview
     * in case that happens. Apparently there are more than 6k such cases.
     */
    public static String getLink(String attributionZdbID, String linkContent) {
        if (attributionZdbID.contains("PERS")) {
            return getTomcatLink(person_uri, attributionZdbID, "1");
        } else {
            return getViewLinkWithID(attributionZdbID, linkContent, attributionZdbID);
        }
    }

    public static String getLinkStartTag(Publication publication) {
        return "<a href=\"/" + publication.getZdbID() + "\">";
    }

    public static String getWikiLink(Publication publication) {
        return getWikiLink("", publication.getZdbID(), publication.getAuthors(), publication.getTitle());
    }

    public static String getSingleAttributionLink(String publicationZdbID, int publicationCount) {
        StringBuilder sb = new StringBuilder("");

        sb.append(" (");
        sb.append(PublicationPresentation.getLink(publicationZdbID, "1"));
        sb.append(")");

        return sb.toString();
    }

    public static String getMultipleAttributionLink(String zdbID, String additionalZdbID, int publicationCount) {
        StringBuilder sb = new StringBuilder("");

        String count = String.valueOf(publicationCount);
        sb.append(" (<a href=\"/action/infrastructure/data-citation-list/");
        if (zdbID.equals(additionalZdbID)) {
            sb.append(zdbID);
        } else {
            sb.append(additionalZdbID);
        }
        sb.append("\">");
        sb.append(count);
        sb.append("</a>)");

        return sb.toString();
    }



}
