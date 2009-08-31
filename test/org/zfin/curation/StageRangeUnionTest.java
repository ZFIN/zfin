package org.zfin.curation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.zfin.curation.dto.ExpressionFigureStageDTO;
import org.zfin.curation.dto.StageRangeUnion;
import org.zfin.curation.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class StageRangeUnionTest {

    private List<ExpressionFigureStageDTO> dtos;

    @Test
    public void singleRange() {
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        efs.setStart(start);
        start.setName("Zygote");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        end.setName("Prim-15");
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0, stageRange.getStartHours());
        assertEquals(0.75, stageRange.getEndHours());
        assertEquals("Zygote", stageRange.getStartStageName());
        assertEquals("Prim-15", stageRange.getEndStageName());

    }

    @Test
    public void twoOverlapppingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0);
        start.setName("Zygote");
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2);
        end.setName("Prim-5");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        startOne.setName("Cell-8");
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(3);
        endOne.setName("Prim-25");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0, stageRange.getStartHours());
        assertEquals(3.0, stageRange.getEndHours());
        assertEquals("Zygote", stageRange.getStartStageName());
        assertEquals("Prim-25", stageRange.getEndStageName());

    }


    @Before
    public void setup() {
        dtos = new ArrayList<ExpressionFigureStageDTO>();
    }

}