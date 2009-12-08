package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stage domain object for GWT.
 */
public class StageDTO implements IsSerializable {

    private String zdbID;
    private String name;
    private float startHours = -1;
    private float endHours = -1;

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

    public float getStartHours() {
        return startHours;
    }

    public void setStartHours(float startHours) {
        this.startHours = startHours;
    }

    public float getEndHours() {
        return endHours;
    }

    public void setEndHours(float endHours) {
        this.endHours = endHours;
    }
}