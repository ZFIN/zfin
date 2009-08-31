package org.zfin.curation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.zfin.curation.dto.ExpressionFigureStageDTO;
import org.zfin.curation.dto.StageRangeIntersection;
import org.zfin.curation.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class StageRangeIntersectionTest {

    private List<ExpressionFigureStageDTO> dtos;

    @Test
    public void singleRange() {
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        efs.setStart(start);
        start.setName("Willibald");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.0, stageRange.getStartHours());
        assertEquals(0.75, stageRange.getEndHours());

        assertTrue(stageRange.isOverlap(start, start));

    }

    @Test
    public void twoOverlapppingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2);
        end.setName("Harry");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(3);
        endOne.setName("Werner");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.75, stageRange.getStartHours());
        assertEquals(2.0, stageRange.getEndHours());

        start.setStartHours(0);
        start.setName("Guenther");
        assertTrue(!stageRange.isOverlap(start, start));
        start.setStartHours(1);
        end.setStartHours(5);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void twoOverlapppingRangesWithUnknown() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2);
        end.setName("Hilde");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(17520);
        endOne.setName("Gertrud");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertEquals(0.0, stageRange.getStartHours());
        assertEquals(2.0, stageRange.getEndHours());

        start.setStartHours(0);
        end.setStartHours(0);
        assertTrue(stageRange.isOverlap(start, end));
        start.setStartHours(1);
        end.setStartHours(5);
        assertTrue(stageRange.isOverlap(start, end));
        start.setStartHours(0);
        end.setStartHours(17520);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void twoOverlapppingRangesTwo() {
        StageDTO start = new StageDTO();
        start.setStartHours(19);
        StageDTO end = new StageDTO();
        end.setStartHours(19);
        end.setName("Walter");
        StageRangeIntersection stageRange = new StageRangeIntersection(start, end);
        start.setStartHours(0);
        end.setStartHours(2160);
        assertTrue(stageRange.isOverlap(start, end));

    }

    @Test
    public void twoNonOverlapppingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2);
        end.setName("Holger");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(3);
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(4);
        endOne.setName("Mausi");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeIntersection stageRange = new StageRangeIntersection(dtos);
        assertTrue(!stageRange.hasOverlap());
        assertEquals(-2.0, stageRange.getStartHours());
        assertEquals(-2.0, stageRange.getEndHours());

        start.setStartHours(0);
        end.setStartHours(0);
        assertTrue(!stageRange.isOverlap(start, end));
        start.setStartHours(1);
        end.setStartHours(6);
        assertTrue(!stageRange.isOverlap(start, end));
    }

    @Before
    public void setup() {
        dtos = new ArrayList<ExpressionFigureStageDTO>();
    }

}