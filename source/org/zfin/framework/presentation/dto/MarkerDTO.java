package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class MarkerDTO implements IsSerializable {

    private String zdbID;
    private String abbreviation;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
