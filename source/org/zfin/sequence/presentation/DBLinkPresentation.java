package org.zfin.sequence.presentation;

import org.apache.log4j.Logger;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptDBLink;

/**
 */
public class DBLinkPresentation extends EntityPresentation {

    private static Logger logger = Logger.getLogger(DBLinkPresentation.class);

    /**
     * Generates an Accession link
     *
     * @param dbLink DBLink
     * @return html for marker link
     */
    public static String getLink(DBLink dbLink) {
        StringBuilder sb = new StringBuilder("");
        if (dbLink != null) {
            sb.append("<a href=\"");

            sb.append(dbLink.getReferenceDatabase().getForeignDB().getDbUrlPrefix());
            sb.append(dbLink.getAccessionNumber());
            if (dbLink.getReferenceDatabase().getForeignDB().getDbUrlSuffix() != null) {
                sb.append(dbLink.getReferenceDatabase().getForeignDB().getDbUrlSuffix());
            }
            sb.append("\">");

            sb.append(dbLink.getReferenceDatabase().getForeignDB().getDbName());
            if (false == dbLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.MICROARRAY_EXPRESSION)) {
                sb.append(":");
                sb.append((dbLink.getAccessionNumberDisplay() != null ? dbLink.getAccessionNumberDisplay() : dbLink.getAccessionNumber()));
            }
            sb.append("</a>");
        }
        return sb.toString();
    }

    /**
     * Create an attribution link for a MarkerDBLink
     *
     * @param dblink link to attribute, ok if it has no attributions
     * @return link html
     */
    public static String getAttributionLink(MarkerDBLink dblink) {
        return getAttributionLink(dblink, dblink.getMarker().getZdbID());
    }

    public static String getAttributionLink(TranscriptDBLink dblink) {
        return getAttributionLink(dblink, dblink.getTranscript().getZdbID());
    }

    private static String getAttributionLink(DBLink dblink, String markerZdbId) {
        StringBuilder sb = new StringBuilder("");

        if (dblink.getPublicationCount() == 1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(dblink.getSinglePublication(), "1"));
            sb.append(")");
        } else if (dblink.getPublicationCount() > 1) {
            /* todo: there should be some more infrastructure for the showpubs links */
            StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
            uri.append("&orgOID=");
            uri.append(markerZdbId);
            uri.append("&rtype=marker&recattrsrctype=standard");
            uri.append("&OID=");
            String count = String.valueOf(dblink.getPublicationCount());

            sb.append(" (");
            sb.append(getWebdriverLink(uri.toString(), dblink.getZdbID(), count));
            sb.append(")");
        }

        return sb.toString();
    }


}
