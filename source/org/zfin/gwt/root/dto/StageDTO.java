package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Stage domain object for GWT corresponding {@link org.zfin.anatomy.DevelopmentStage}
 */
public class StageDTO implements IsSerializable , Serializable {

    private static final long serialVersionUID = 8631863184044243644L;

    private String zdbID;
    private String name;
    private String oboID ;
    private String nameLong;
    private String abbreviation;
    private String timeString;
    private float startHours = -1.0f;
    private float endHours = -1.0f;
    public static final String UNKNOWN = "Unknown";
    public static final String UNKNOWN_ABBREV = "unk";

    public String getDisplay(){
        return abbreviation + " " + timeString ;
    }

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

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public String getNameLong() {
        return nameLong;
    }

    public void setNameLong(String nameLong) {
        this.nameLong = nameLong;
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

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StageDTO)) return false;

        StageDTO stageDTO = (StageDTO) o;

        if (zdbID != null ? !zdbID.equals(stageDTO.zdbID) : stageDTO.zdbID != null) return false;
        return oboID != null ? oboID.equals(stageDTO.oboID) : stageDTO.oboID == null;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (oboID != null ? oboID.hashCode() : 0);
        return result;
    }
}