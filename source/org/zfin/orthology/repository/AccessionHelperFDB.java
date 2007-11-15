package org.zfin.orthology.repository;

/**
 * User: giles
 * Date: Sep 5, 2006
 * Time: 12:40:08 PM
 */

/**
 * Hibernate repository business object mapped to foreign_db table
*/
public class AccessionHelperFDB {
    private int significance;
    private String hyperLinkQuery;
    private String FDBname;

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }

    public String getHyperLinkQuery() {
        return hyperLinkQuery;
    }

    public void setHyperLinkQuery(String hyperLinkQuery) {
        this.hyperLinkQuery = hyperLinkQuery;
    }

    public String getFDBname() {
        return FDBname;
    }

    public void setFDBname(String FDBname) {
        this.FDBname = FDBname;
    }
}
