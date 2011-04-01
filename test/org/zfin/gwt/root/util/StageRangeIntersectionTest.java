package org.zfin.gwt.root.util;

import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
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
        start.setAbbreviation("Willibald");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.0F, stageRange.getStartHours());
        assertEquals(0.75F, stageRange.getEndHours());

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
        assertEquals(0.75F, stageRange.getStartHours());
        assertEquals(2.0F, stageRange.getEndHours());

        start.setStartHours(0.0F);
        start.setAbbreviation("Guenther");
        assertTrue(!stageRange.isOverlap(start, start));
        start.setStartHours(1.0F);
        end.setStartHours(5.0F);
        assertTrue(stageRange.isOverlap(start, end));

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
        assertEquals(0.0F, stageRange.getStartHours());
        assertEquals(2.0F, stageRange.getEndHours());

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
        StageDTO end = new StageDTO();
        end.setStartHours(2.0F);
        end.setAbbreviation("Holger");
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
        assertEquals(-2.0F, stageRange.getStartHours());
        assertEquals(-2.0F, stageRange.getEndHours());

        start.setStartHours(0.0F);
        end.setStartHours(0.0F);
        assertTrue(!stageRange.isOverlap(start, end));
        start.setStartHours(1.0F);
        end.setStartHours(6.0F);
        assertTrue(!stageRange.isOverlap(start, end));
    }

    @Before
    public void setup() {
        dtos = new ArrayList<ExpressionFigureStageDTO>(4);
    }

}