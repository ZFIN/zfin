package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.publication.PublicationDbXref;
import org.zfin.sequence.ForeignDB;

public class PublicationDbXrefPresentation extends EntityPresentation {

    public static String getLink(PublicationDbXref dbXref) {
        if (dbXref == null) {
            return "";
        }

        ForeignDB foreignDB = dbXref.getReferenceDatabase().getForeignDB();
        String url = foreignDB.getDbUrlPrefix() +
                dbXref.getAccessionNumber() +
                (StringUtils.isNotEmpty(foreignDB.getDbUrlSuffix()) ? foreignDB.getDbUrlSuffix() : "");
        String display = foreignDB.getDbName() + ":" + dbXref.getAccessionNumber();
        return getGeneralHyperLink(url, display);
    }

}
