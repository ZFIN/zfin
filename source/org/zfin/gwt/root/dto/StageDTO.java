package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stage domain object for GWT corresponding {@link org.zfin.anatomy.DevelopmentStage}
 */
public class StageDTO implements IsSerializable {

    private String zdbID;
    private String name;
    private String abbreviation;
    private float startHours = -1.0f;
    private float endHours = -1.0f;

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

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}