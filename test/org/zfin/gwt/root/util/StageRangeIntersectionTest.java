package org.zfin.gwt.root.util;

import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StageRangeIntersectionTest {

    private List<ExpressionFigureStageDTO> dtos;

    @Test
    public void singleRange() {
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs.setStart(start);
        start.setAbbreviation("Willibald");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.0F, stageRange.getStartHours(), 0.1);
        assertEquals(0.75F, stageRange.getEndHours(), 0.1);

        assertTrue(stageRange.isOverlap(start, start));

    }

    @Test
    public void twoOverlappingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2.0F);
        end.setAbbreviation("Harry");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(3.0F);
        endOne.setAbbreviation("Werner");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.75F, stageRange.getStartHours(), 0.1);
        assertEquals(2.0F, stageRange.getEndHours(), 0.1);

        start.setStartHours(0.0F);
        start.setAbbreviation("Guenther");
        assertTrue(!stageRange.isOverlap(start, start));
        start.setStartHours(1.0F);
        end.setStartHours(5.0F);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void overlappingRanges() {
        StageDTO startUnion = new StageDTO();
        startUnion.setStartHours(1.25F);
        StageDTO endUnion = new StageDTO();
        endUnion.setStartHours(10.33F);
        StageRangeIntersection intersection = new StageRangeIntersection(startUnion, endUnion);

        StageDTO start = new StageDTO();
        start.setStartHours(5.25F);
        start.setAbbreviation("Harry");
        StageDTO end = new StageDTO();
        end.setStartHours(10.32F);
        end.setAbbreviation("Kate");

        assertTrue(intersection.isOverlap(start, end));

    }

    @Test
    public void twoOverlappingRangesWithUnknown() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2.0F);
        end.setAbbreviation("Hilde");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.0F);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(17520.0F);
        endOne.setAbbreviation("Gertrud");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.0F, stageRange.getStartHours(), 0.1);
        assertEquals(2.0F, stageRange.getEndHours(), 0.1);

        start.setStartHours(0.0F);
        end.setStartHours(0.0F);
        assertTrue(stageRange.isOverlap(start, end));
        start.setStartHours(1.0F);
        end.setStartHours(5.0F);
        assertTrue(stageRange.isOverlap(start, end));
        start.setStartHours(0.0F);
        end.setStartHours(17520.0F);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void twoOverlappingRangesTwo() {
        StageDTO start = new StageDTO();
        start.setStartHours(19.0F);
        StageDTO end = new StageDTO();
        end.setStartHours(19.0F);
        end.setAbbreviation("Walter");
        StageRangeIntersection stageRange = new StageRangeIntersection(start, end);
        start.setStartHours(0.0F);
        end.setStartHours(2160.0F);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void twoNonOverlappingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs1.setStart(start);
        StageDTO end = getStageDTO(2.0F, 4F, "Holger");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(3.0F);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(4.0F);
        endOne.setAbbreviation("Mausi");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertTrue(!stageRange.hasOverlap());
        assertEquals(-2.0F, stageRange.getStartHours(), 0.1);
        assertEquals(-2.0F, stageRange.getEndHours(), 0.1);

        start.setStartHours(0.0F);
        end.setStartHours(0.0F);
        assertTrue(!stageRange.isOverlap(start, end));
        start.setStartHours(1.0F);
        end.setStartHours(6.0F);
        assertTrue(!stageRange.isOverlap(start, end));
    }

    @Test
    public void twoAdjacentStages() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO stage = getStageDTO(60F, 72F, "Hatching: Pec-fin");
        efs1.setStart(stage);
        efs1.setEnd(stage);
        dtos.add(efs1);

        StageRangeIntersectionService stageRange = new StageRangeIntersectionService(dtos);
        StageDTO one = getStageDTO(72F, 96F, "Larval: Protruding-mouth");
        assertFalse(stageRange.hasOverlapWithAllStageRanges(one, one));
    }

    private StageDTO getStageDTO(float start, float end, String abbreviation) {
        StageDTO dto = new StageDTO();
        dto.setStartHours(start);
        dto.setEndHours(end);
        dto.setAbbreviation(abbreviation);
        return dto;
    }

    @Test
    public void twoNonOverlappingRangesOverlap() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        efs1.setStart(getStageDTO(10.33F, 11.66F, "1-4 somites"));
        efs1.setEnd(getStageDTO(10.33F, 11.66F, "1-4 somites"));
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        efs.setStart(getStageDTO(11.66F, 14F, "5-9 somites"));
        efs.setEnd(getStageDTO(11.66F, 14F, "5-9 somites"));
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersectionService stageRange = new StageRangeIntersectionService(dtos);
        StageDTO one = getStageDTO(10.0F, 10.33F, "Bud");
        StageDTO two = getStageDTO(900000.0F, 100000000F, "Adult");

        assertTrue(stageRange.hasOverlapWithAllStageRanges(one, two));
    }

    @Test
    public void unknownStage() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        efs1.setStart(getStageDTO(60F, 72F, "Pec-fin"));
        efs1.setEnd(getStageDTO(60F, 72F, "Pec-fin"));
        dtos.add(efs1);

        StageRangeIntersectionService stageRange = new StageRangeIntersectionService(dtos);
        StageDTO one = getStageDTO(42F, 48F, "High-pec");
        StageDTO two = getStageDTO(0F, 17520F, "unk");

        assertTrue(stageRange.hasOverlapWithAllStageRanges(one, two));
    }

    @Before
    public void setup() {
        dtos = new ArrayList<>(4);
    }

}