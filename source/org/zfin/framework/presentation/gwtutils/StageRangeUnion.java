package org.zfin.framework.presentation.gwtutils;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.presentation.dto.StageDTO;
import org.zfin.framework.presentation.dto.ExpressionFigureStageDTO;

import java.util.List;

/**
 * Holds the union of a given collection of stage ranges.
 * <p/>
 * Calculates the stage range that all ranges comprise in total.
 */
public class StageRangeUnion implements IsSerializable {

    private List<ExpressionFigureStageDTO> efses;
    // calculated and cached values for the intersection
    private double startHours = -1;
    private double endHours = -1;
    private StageDTO startDTO = new StageDTO();
    private StageDTO endDTO = new StageDTO();
    private String startStageName;
    private String endStageName;
    private static final int UNKNOWN_END_HOURS = 17520;
    private static final String UNKNOWN_ABBREVIATION = "unk";

    public StageRangeUnion() {
    }

    public StageRangeUnion(List<ExpressionFigureStageDTO> efs) {
        this.efses = efs;
        calculateUnion();
    }

    public StageRangeUnion(StageDTO start, StageDTO end) {
        this.startDTO = start;
        this.endDTO = end;
        startHours = start.getStartHours();
        if (end.getName().equals(UNKNOWN_ABBREVIATION))
            endHours = UNKNOWN_END_HOURS;
        else
            endHours = end.getStartHours();
    }

    /**
     * Calculates the stage range that all ranges in the collection have in common.
     * Exception occurs:
     * 1) either start or end stage is negative, i.e. unitialized.
     * 2) start > end stage
     */
    private void calculateUnion() {
        if (efses == null)
            return;

        int index = 0;
        for (ExpressionFigureStageDTO efs : efses) {
            if (efs.getStart().getStartHours() < 0 || efs.getEnd().getStartHours() < 0)
                throw new RuntimeException("Stage hours are not initialized " + efs.getUniqueID());
            // check that start <= end
            if (efs.getStart().getStartHours() > efs.getEnd().getStartHours())
                throw new RuntimeException("Start stage is before end stage! " + efs.getUniqueID());
            // if first element
            if (index == 0) {
                startHours = efs.getStart().getStartHours();
                endHours = efs.getEnd().getStartHours();
                startStageName = efs.getStart().getName();
                endStageName = efs.getEnd().getName();
                startDTO = efs.getStart();
                endDTO = efs.getEnd();
            } else {
                addNewRange(efs.getStart(), efs.getEnd());
            }
            index++;
        }
    }

    public void addNewRange(StageDTO startStage, StageDTO endStage) {
        double start = startStage.getStartHours();
        double end = endStage.getStartHours();
        if (endStage.getName().equals(UNKNOWN_ABBREVIATION))
            end = UNKNOWN_END_HOURS;
        // first check if start < endHours and end > startHours

        if (startHours > start) {
            startHours = start;
            startStageName = startStage.getName();
            startDTO = startStage;
        }
        if (endHours < end) {
            endHours = end;
            endStageName = endStage.getName();
            endDTO = endStage;
        }
    }

    public double getStartHours() {
        return startHours;
    }

    public double getEndHours() {
        return endHours;
    }

    public StageDTO getStart() {
        if (startDTO.getName() == null)
            startDTO.setName("None");
        return startDTO;
    }

    public void setStart(StageDTO start) {
        this.startDTO = start;
    }

    public StageDTO getEnd() {
        if (endDTO.getName() == null)
            endDTO.setName("None");
        return endDTO;
    }

    public void setEnd(StageDTO end) {
        this.endDTO = end;
    }

    public String getStartStageName() {
        return startStageName;
    }

    public void setStartStageName(String startStageName) {
        this.startStageName = startStageName;
    }

    public String getEndStageName() {
        return endStageName;
    }

    public void setEndStageName(String endStageName) {
        this.endStageName = endStageName;
    }
}