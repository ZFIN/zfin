package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FishDTO implements IsSerializable {

    private String zdbID;
    private String name;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
