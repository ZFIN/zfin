package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.DBLink;

/**
 */
public class IncludeExternalBlastBean {

    private String zdbID ;
    private DBLink dbLink ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public DBLink getDbLink() {
        return dbLink;
    }

    public void setDbLink(DBLink dbLink) {
        this.dbLink = dbLink;
    }
}
