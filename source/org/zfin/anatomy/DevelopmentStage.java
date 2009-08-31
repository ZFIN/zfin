package org.zfin.anatomy;

import java.io.Serializable;

public class DevelopmentStage  implements Serializable, Comparable<DevelopmentStage> {

    public static final String ZYGOTE_STAGE = "Zygote:1-cell";
    public static final String ZYGOTE_STAGE_ZDB_ID = "ZDB-STAGE-010723-4";
    public static final String ADULT_STAGE = "Adult";
    public static final String ADULT_STAGE_ZDB_ID = "ZDB-STAGE-010723-39";
    public static final String UNKNOWN = "Unknown";

    private long stageID;
    private String zdbID;
    private String oboID;
    private String name;
    private float hoursStart;
    private float hoursEnd;
    private String otherFeature;
    private String abbreviation;
    private String timeString;
    public static final String NEWLINE_PLUS_INDENT = System.getProperty("line.separator") + "    ";


    public long getStageID() {
        return stageID;
    }

    public void setStageID(long stageID) {
        this.stageID = stageID;
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

    public float getHoursStart() {
        return hoursStart;
    }

    public void setHoursStart(float hoursStart) {
        this.hoursStart = hoursStart;
    }

    public float getHoursEnd() {
        return hoursEnd;
    }

    public void setHoursEnd(float hoursEnd) {
        this.hoursEnd = hoursEnd;
    }

    public String getOtherFeature() {
        return otherFeature;
    }

    public void setOtherFeature(String otherFeature) {
        this.otherFeature = otherFeature;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    /**
     * The name of a stage is a concatenation of a short name and an additional description.
     * @return string
     */
    public String abbreviation() {
        if (name == null)
            throw new RuntimeException("No name for stage found yet.");

        int colonIndex = name.indexOf(":");
        if(colonIndex == -1)
            return name;

        return name.substring(0, colonIndex);
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    /**
     * Checks if the provided development stage comes after the stage of this object.
     * If they are the same it returns false;
     * @param stage stage
     * @return boolean
     */
    public boolean earlierThan(DevelopmentStage stage){
        if(stage == null)
            throw new RuntimeException("No valid stage object provided for comparison.");

        if(this == stage)
            return false;

        if(this.getName().equals(UNKNOWN))
            return true;

        if(stage.getName().equals(UNKNOWN))
            return false;

        return hoursStart <= stage.getHoursStart();

    }

    public int compareTo(DevelopmentStage anotherStage) {
        if (anotherStage == null)
            return +1;
        return (int)(hoursStart - anotherStage.getHoursStart());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Developmental Stage [BO]");
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("name: ").append(name);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("zdbID: ").append(zdbID);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Starting Hour: ").append(hoursStart);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Ending Hour: ").append(hoursEnd);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Other Feature: ").append(otherFeature);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Abbreviation: ").append(abbreviation);
        sb.append(NEWLINE_PLUS_INDENT);
        return sb.toString();
    }

    public boolean equals(DevelopmentStage anotherStage) {
        return anotherStage.getZdbID().equalsIgnoreCase(zdbID);
    }
}
