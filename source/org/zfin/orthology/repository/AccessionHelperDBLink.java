package org.zfin.orthology.repository;

/**
 * User: giles
 * Date: Sep 5, 2006
 * Time: 12:31:41 PM
 */

/**
 * Hibernate repository business object mapped to db_link table
 */
public class AccessionHelperDBLink {

    private String zdbID;
    private String accessionNum;
    private String accessionHyperLinkNum;
    private String orthoID;
    private String fbcontID;
    private AccessionHelperFDBCont fdbContHelper;

    public String getAccessionNum() {
        return accessionNum;
    }

    public void setAccessionNum(String accessionNum) {
        this.accessionNum = accessionNum;
    }

    public String getAccessionHyperLinkNum() {
        return accessionHyperLinkNum;
    }

    public void setAccessionHyperLinkNum(String accessionHyperLinkNum) {
        this.accessionHyperLinkNum = accessionHyperLinkNum;
    }

    public String getOrthoID() {
        return orthoID;
    }

    public void setOrthoID(String orthoID) {
        this.orthoID = orthoID;
    }

    public String getFbcontID() {
        return fbcontID;
    }

    public void setFbcontID(String fbcontID) {
        this.fbcontID = fbcontID;
    }

    public AccessionHelperFDBCont getFdbContHelper() {
        return fdbContHelper;
    }

    public void setFdbContHelper(AccessionHelperFDBCont FDBContHelper) {
        this.fdbContHelper = FDBContHelper;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
