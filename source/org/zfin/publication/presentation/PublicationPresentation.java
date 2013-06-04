package org.zfin.publication.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;

/**
 * Presentation Class to create output from a Publication object.
 */
public class PublicationPresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-pubview2.apg&OID=";
    private static final String person_uri = "profile/view/";

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
    /**
     * Per case 8749, an EST could be attributed to a person. So adding this clause to redirect to person page and not pubview
     * in case that happens. Apparently there are more than 6k such cases.
     */
    public static String getLink(String attributionZdbID, String linkContent) {
        if (attributionZdbID.contains("PERS")){
          return getTomcatLink(person_uri,attributionZdbID,"1");
        }
        else{
            return getWebdriverLink(uri, attributionZdbID, linkContent);
        }
    }

    public static String getWikiLink(Publication publication) {
        return getWikiLink(ZfinProperties.getWebDriver() + uri, publication.getZdbID(), publication.getAuthors(), publication.getTitle());
    }

    public static String getSingleAttributionLink(String publicationZdbID, int publicationCount) {
        StringBuilder sb = new StringBuilder("");

        sb.append(" (");
        sb.append(PublicationPresentation.getLink(publicationZdbID, "1"));
        sb.append(")");

        return sb.toString();
    }

    public static String getMultipleAttributionLink(String zdbID, String additionalZdbID,
                                                    String rType, String srcType, int publicationCount ) {
        StringBuilder sb = new StringBuilder("");

        StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
        if (!StringUtils.isEmpty(additionalZdbID)) {
            uri.append("&OID=");
            uri.append(additionalZdbID);
        }
        uri.append("&rtype=");
        uri.append(rType);
        uri.append("&recattrsrctype=");
        uri.append(srcType);
        uri.append("&orgOID=");

        sb.append(" (");
        sb.append(getWebdriverLink(uri.toString(), zdbID, String.valueOf(publicationCount)));
        sb.append(")");

        return sb.toString();
    }



}
