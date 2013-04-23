package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

public class TermStageSplitStatementTest {

    @Test
    public void getTermStageRange() {
        TermStageSplitStatement range = new TermStageSplitStatement("fileName");
        range.addTermStageUpdateLine(null, 1);

        String lineOne = "";
        range = new TermStageSplitStatement("fileName");
        try {
            range.addTermStageUpdateLine(lineOne, 1);
        } catch (Exception e) {
            assertTrue("Error occured", true);
        }

        lineOne = "ZFA:0000823|5-9 somites|5-9 somites|ZFA:0001211";
        range = new TermStageSplitStatement("fileName");
        range.addTermStageUpdateLine(lineOne, 1);
        assertEquals(1, range.getStartLine());
    }
}
