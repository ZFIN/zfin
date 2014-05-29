package org.zfin.gwt.root.util;

import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.StageDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StageRangeUnionTest {

    private List<ExpressionFigureStageDTO> dtos;

    @Test
    public void singleRange() {
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        efs.setStart(start);
        start.setName("Zygote");
        StageDTO end = new StageDTO();
        end.setStartHours(0.75F);
        end.setName("Prim-15");
        efs.setEnd(end);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0F, stageRange.getStartHours(), 0.1);
        assertEquals(0.75F, stageRange.getEndHours(), 0.1);
        assertEquals("Zygote", stageRange.getStartStageName());
        assertEquals("Prim-15", stageRange.getEndStageName());

    }

    @Test
    public void twoOverlappingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        start.setName("Zygote:1-cell");
        start.setAbbreviation("1-cell");
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(2.0F);
        end.setName("Pharyngula:Prim-5");
        end.setAbbreviation("Prim-5");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        startOne.setAbbreviation("8-cell");
        startOne.setName("Cleavage:8-cell");
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(3.0F);
        endOne.setAbbreviation("Prim-25");
        endOne.setName("Pharyngula:Prim-25");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0F, stageRange.getStartHours(), 0.1);
        assertEquals(3.0F, stageRange.getEndHours(), 0.1);
        assertEquals("Zygote:1-cell", stageRange.getStartStageName());
        assertEquals("Pharyngula:Prim-25", stageRange.getEndStageName());

    }



    @Test
    public void twoNonOverlappingRanges() {
        ExpressionFigureStageDTO efs1 = new ExpressionFigureStageDTO();
        StageDTO start = new StageDTO();
        start.setStartHours(0.0F);
        start.setName("Zygote:1-cell");
        start.setAbbreviation("1-cell");
        efs1.setStart(start);
        StageDTO end = new StageDTO();
        end.setStartHours(0.0F);
        end.setName("Zygote:1-cell");
        end.setAbbreviation("1-cell");
        efs1.setEnd(end);
        ExpressionFigureStageDTO efs = new ExpressionFigureStageDTO();
        StageDTO startOne = new StageDTO();
        startOne.setStartHours(0.75F);
        startOne.setAbbreviation("8-cell");
        startOne.setName("Cleavage:8-cell");
        efs.setStart(startOne);
        StageDTO endOne = new StageDTO();
        endOne.setStartHours(0.75F);
        endOne.setAbbreviation("8-cell");
        endOne.setName("Cleavage:8-cell");
        efs.setEnd(endOne);
        dtos.add(efs1);
        dtos.add(efs);

        StageRangeUnion stageRange = new StageRangeUnion(dtos);
        assertEquals(0.0F, stageRange.getStartHours(), 0.1);
        assertEquals(0.75F, stageRange.getEndHours(), 0.1);
        assertEquals("Zygote:1-cell", stageRange.getStartStageName());
        assertEquals("Cleavage:8-cell", stageRange.getEndStageName());

    }


    @Before
    public void setup() {
        dtos = new ArrayList<ExpressionFigureStageDTO>();
    }

}