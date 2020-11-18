package org.zfin.anatomy;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.api.View;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "STAGE")
@Immutable
public class DevelopmentStage implements Serializable, Comparable<DevelopmentStage> {

    public static final String ZYGOTE_STAGE = "Zygote:1-cell";
    public static final String ZYGOTE_STAGE_ZDB_ID = "ZDB-STAGE-010723-4";
    public static final String ADULT_STAGE = "Adult";
    public static final String ADULT_STAGE_ZDB_ID = "ZDB-STAGE-010723-39";
    public static final String UNKNOWN = "Unknown";
    public static final Float MIN = 0.0f;
    public static final Float MAX = 17520f;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGeneratorStage")
    @GenericGenerator(name = "zfinGeneratorStage",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "STAGE")
            })
    @Column(name = "stg_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @Column(name = "stg_obo_id")
    private String oboID;

    @Column(name = "stg_name")
    @JsonView(View.API.class)
    private String name;

    @Column(name = "stg_name_long")
    private String nameLong;

    @Column(name = "stg_hours_start")
    @JsonView(View.GeneExpressionAPI.class)
    private float hoursStart;

    @Column(name = "stg_hours_end")
    @JsonView(View.GeneExpressionAPI.class)
    private float hoursEnd;

    @Column(name = "stg_other_features")
    private String otherFeature;

    @Column(name = "stg_abbrev")
    @JsonView(View.API.class)
    private String abbreviation;

    @Column(name = "stg_name_ext")
    private String timeString;

    @Transient
    private long stageID;
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

    public String getNameLong() {
        return nameLong;
    }

    public void setNameLong(String nameLong) {
        this.nameLong = nameLong;
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
     *
     * @return string
     */
    public String abbreviation() {
        if (name == null) {
            throw new RuntimeException("No name for stage found yet.");
        }

        int colonIndex = name.indexOf(":");
        if (colonIndex == -1) {
            return name;
        }

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
     *
     * @param stage stage
     * @return boolean
     */
    public boolean earlierThan(DevelopmentStage stage) {
        if (stage == null) {
            throw new RuntimeException("No valid stage object provided for comparison.");
        }

        if (this == stage) {
            return false;
        }

        if (this.getName().equals(UNKNOWN)) {
            return true;
        }

        if (stage.getName().equals(UNKNOWN)) {
            return false;
        }

        return hoursStart <= stage.getHoursStart();

    }

    public int compareTo(DevelopmentStage anotherStage) {
        if (anotherStage == null) {
            return +1;
        }
        return (int) (hoursStart - anotherStage.getHoursStart());
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevelopmentStage that = (DevelopmentStage) o;
        return Objects.equals(zdbID, that.zdbID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zdbID);
    }

    public static boolean stageRangeOverlapsRange(DevelopmentStage start, DevelopmentStage end, DevelopmentStage intervalStart, DevelopmentStage intervalEnd) {
        float startFull = start.getHoursStart();
        float endFull = end.getName().equals(UNKNOWN) ? end.getHoursEnd() : end.getHoursStart();
        float startInterval = intervalStart.getHoursStart();
        float endInterval = intervalEnd.getName().equals(UNKNOWN) ? intervalEnd.getHoursEnd() : intervalEnd.getHoursStart();

        // true if full range start is before range start and full range end after interval end
        return startFull <= startInterval && endFull >= endInterval;
    }
}
