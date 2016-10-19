package org.zfin.infrastructure.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.MarkerAlias;
import org.zfin.publication.presentation.PublicationPresentation;


public class DataAliasPresentation extends EntityPresentation {

    public static String getAttributionLink(MarkerAlias alias) {
        StringBuilder sb = new StringBuilder("");

        if (alias.getPublicationCount() == 1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(alias.getSinglePublication(), "1"));
            sb.append(")");
        } else if (alias.getPublicationCount() > 1) {
            String count = String.valueOf(alias.getPublicationCount());

            sb.append(" (");
            sb.append(getTomcatLink("/action/publication/list/" + alias.getMarker().getZdbID(), alias.getZdbID(), count));
            sb.append(")");
        }

        return sb.toString();
    }
}
