package org.zfin.gwt.root.util;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 * Holds the intersection of a given collection of stage ranges.
 * <p/>
 * Calculates the stage range that all ranges in the collection have in common.
 * If no overlapping stage range is found start and end stage set to a negative value.
 * <p/>
 * Exception occurs:
 * 1) either start or end stage is negative, i.e. uninitialized.
 * 2) start > end stage
 */
public class StageRangeIntersectionService implements IsSerializable {

    private List<ExpressionFigureStageDTO> efses;

    public StageRangeIntersectionService() {
    }

    public StageRangeIntersectionService(List<ExpressionFigureStageDTO> efs) {
        this.efses = efs;
    }

    /**
     * Checks if a given range has an overlap with all stage ranges of this class (efses)
     * It return true only if the given stage range
     * 1) If there is no overlap range
     * 2) the start or end value given are negative
     * 3) start > end
     *
     * @param startStage start stage
     * @param endStage   end stage
     * @return boolean
     */
    public boolean hasOverlapWithAllStageRanges(StageDTO startStage, StageDTO endStage) {
        if (efses == null || efses.size() == 0)
            return false;
        for (ExpressionFigureStageDTO dto : efses) {
            if (!hasSomeOverlap(startStage, endStage, dto.getStart(), dto.getEnd()))
                return false;
        }
        return true;
    }

    public boolean isFullOverlap(StageDTO startStageOuter, StageDTO endStageOuter, StageDTO startStageInner, StageDTO endStageInner) {

        float startOuter = startStageOuter.getStartHours();
        float endOuter = endStageOuter.getStartHours();
        float startInner = startStageInner.getStartHours();
        float endInner = endStageInner.getStartHours();
        if (startOuter > startInner)
            return false;
        if (endOuter < endInner)
            return false;
        return true;
    }

    public boolean hasSomeOverlap(StageDTO startStageOuter, StageDTO endStageOuter, StageDTO startStageInner, StageDTO endStageInner) {

        float startOuter = startStageOuter.getStartHours();
        float endOuter = endStageOuter.getStartHours();
        if (endStageOuter.getAbbreviation().equals(StageDTO.UNKNOWN_ABBREV))
            endOuter = endStageOuter.getEndHours();
        float startInner = startStageInner.getStartHours();
        float endInner = endStageInner.getStartHours();
        if (startOuter > endInner)
            return false;
        if (endOuter < startInner)
            return false;
        return true;
    }
}