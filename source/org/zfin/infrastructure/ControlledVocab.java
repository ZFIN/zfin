package org.zfin.infrastructure;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 4/17/14
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */

public class ControlledVocab {
    private String zdbID;
    private String cvTermName;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getCvTermName() {
        return cvTermName;
    }

    public void setCvTermName(String cvTermName) {
        this.cvTermName = cvTermName;
    }
}
