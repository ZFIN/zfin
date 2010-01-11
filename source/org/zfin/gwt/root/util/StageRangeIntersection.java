package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.StageDTO;

import java.util.List;

/**
 * Holds the intersection of a given collection of stage ranges.
 * <p/>
 * Calculates the stage range that all ranges in the collection have in common.
 * If no overlappping stage range is found start and end stage set to a negative value.
 * <p/>
 * Exception occurs:
 * 1) either start or end stage is negative, i.e. unitialized.
 * 2) start > end stage
 */
public class StageRangeIntersection implements IsSerializable {

    private List<ExpressionFigureStageDTO> efses;
    // calculated and cached values for the intersection
    private float startHours = -1.0f;
    private float endHours = -1.0f;
    private StageDTO start;
    private StageDTO end;
    private boolean overlapRangeExists = true;

    public StageRangeIntersection() {
    }

    public StageRangeIntersection(List<ExpressionFigureStageDTO> efs) {
        this.efses = efs;
        calculateIntersection();
    }

    public StageRangeIntersection(StageDTO start, StageDTO end) {
        this.start = start;
        this.end = end;
        startHours = start.getStartHours();
        if (end.getName().equals(StageRangeUnion.UNKNOWN_ABBREVIATION))
            endHours = StageRangeUnion.UNKNOWN_END_HOURS;
        else
            endHours = end.getStartHours();
    }

    /**
     * Calculates the stage range that all ranges in the collection have in common.
     * Exception occurs:
     * 1) either start or end stage is negative, i.e. unitialized.
     * 2) start > end stage
     */
    private void calculateIntersection() {
        if (efses == null)
            return;

        int index = 0;
        for (ExpressionFigureStageDTO efs : efses) {
            start = efs.getStart();
            end = efs.getEnd();
            if (efs.getStart().getStartHours() < 0.0F || efs.getEnd().getStartHours() < 0.0F)
                throw new RuntimeException("Stage hours are not initialized " + efs.getUniqueID());
            // check that start <= end
            if (efs.getStart().getStartHours() > efs.getEnd().getStartHours())
                throw new RuntimeException("Start stage is before end stage! " + efs.getUniqueID());
            // if first element
            if (index == 0) {
                startHours = efs.getStart().getStartHours();
                endHours = efs.getEnd().getStartHours();
            } else {
                addNewRange(efs.getStart(), efs.getEnd());
            }
            index++;
        }
    }

    public void addNewRange(StageDTO startStage, StageDTO endStage) {
        float start = startStage.getStartHours();
        float end = endStage.getStartHours();
        if (endStage.getName().equals(StageRangeUnion.UNKNOWN_ABBREVIATION))
            end = StageRangeUnion.UNKNOWN_END_HOURS;
        // first check if start < endHours and end > startHours
        if (start > endHours || end < startHours) {
            overlapRangeExists = false;
            endHours = -2.0F;
            startHours = -2.0F;
            return;
        }

        if (startHours < start)
            startHours = start;
        if (endHours > end)
            endHours = end;
    }

    public float getStartHours() {
        return startHours;
    }

    public float getEndHours() {
        return endHours;
    }

    /**
     * Checks if a given range has an overlap with the intersection overlap.
     * It return false if:
     * 1) If there is no overlap range
     * 2) the start or end value given are negative
     * 3) start > end
     *
     * @param startStage start stage
     * @param endStage   end stage
     * @return boolean
     */
    public boolean isOverlap(StageDTO startStage, StageDTO endStage) {
        float start = startStage.getStartHours();
        float end = endStage.getStartHours();
        if (endStage.getName().equals(StageRangeUnion.UNKNOWN_ABBREVIATION))
            end = StageRangeUnion.UNKNOWN_END_HOURS;
        if (!overlapRangeExists)
            return false;

        if (start < 0.0F || end < 0.0F)
            return false;

        if (start > end)
            return false;

        // either start or end falls in between the existing range
        if ((start >= startHours && start <= endHours) || (end <= endHours && end >= startHours))
            return true;
            // or the range [start,end] encompasses the existing range
        else if (start <= startHours && end >= endHours)
            return true;
        else
            return false;
    }

    /**
     * Checks if a given range overlaps with the intersection overlap fully.
     * It return false if:
     * 1) If there is no overlap range
     * 2) the start or end value given are negative
     * 3) start > end
     *
     * @param startStage start stage
     * @param endStage   end stage
     * @return boolean
     */
    public boolean isFullOverlap(StageDTO startStage, StageDTO endStage) {
        float start = startStage.getStartHours();
        float end = endStage.getStartHours();
        if (endStage.getName().equals(StageRangeUnion.UNKNOWN_ABBREVIATION))
            end = StageRangeUnion.UNKNOWN_END_HOURS;

        if (start < 0.0F || end < 0.0F)
            return false;

        if (start > end)
            return false;

            // start and end encompasses the existing range
        else if (start <= startHours && end >= endHours)
            return true;
        else
            return false;
    }

    public boolean hasOverlap() {
        return overlapRangeExists;
    }

    public StageDTO getStart() {
        return start;
    }

    public StageDTO getEnd() {
        return end;
    }
}