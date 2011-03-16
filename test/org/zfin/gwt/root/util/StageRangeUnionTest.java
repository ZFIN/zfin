package org.zfin.gwt.root.util;

import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class StageRangeUnionTest {

    private List<ExpressionFigureStageDTO> dtos;

    @Test
    public void singleRange() {
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs.setStart(start);
        start.setNameLong("Zygote");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        end.setNameLong("Prim-15");
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0F, stageRange.getStartHours());
        assertEquals(0.75F, stageRange.getEndHours());
        assertEquals("Zygote", stageRange.getStartStageName());
        assertEquals("Prim-15", stageRange.getEndStageName());

    }

    @Test
    public void twoOverlappingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        start.setNameLong("Zygote");
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2.0F);
        end.setNameLong("Prim-5");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        startOne.setNameLong("Cell-8");
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(3.0F);
        endOne.setNameLong("Prim-25");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0F, stageRange.getStartHours());
        assertEquals(3.0F, stageRange.getEndHours());
        assertEquals("Zygote", stageRange.getStartStageName());
        assertEquals("Prim-25", stageRange.getEndStageName());

    }


    @Before
    public void setup() {
        dtos = new ArrayList<ExpressionFigureStageDTO>();
    }

}