package org.zfin.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TermStageSplitStatementTest {

    private TermStageSplitStatement range;

    @Before
    public void setup() {
        range = new TermStageSplitStatement("fileName");
    }

    @Test
    public void getTermStageRangeNull() {
        range.addTermStageUpdateLine(null, 1);
    }

    @Test(expected = RuntimeException.class)
    public void getTermStageRangeInvalid() {
        String lineOne = "";
        range.addTermStageUpdateLine(lineOne, 1);
    }

    @Test
    public void getTermStageRange() {
        String lineOne = "ZFA:0000823|5-9 somites|5-9 somites|ZFA:0001211";
        range.addTermStageUpdateLine(lineOne, 1);
        assertEquals(1, range.getStartLine());
    }
}
