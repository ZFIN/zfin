package org.zfin.publication;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PubMedValidationReportTest {

    @Test
    public void testPageNumbers() {
        String value = "120-125";
        assertEquals("120-125", PubMedValidationReport.getCompletePageNumbers(value));

        value = "120";
        assertEquals("120", PubMedValidationReport.getCompletePageNumbers(value));

        value = "120-5";
        assertEquals("120-125", PubMedValidationReport.getCompletePageNumbers(value));

        value = "120-5";
        assertEquals("120-125", PubMedValidationReport.getCompletePageNumbers(value));

        value = "120-1222";
        assertEquals("120-1222", PubMedValidationReport.getCompletePageNumbers(value));


        Publication pub = new Publication();
        pub.setPages("120-125");
        assertTrue(PubMedValidationReport.isSamePageNumbers(pub, "120-5"));
    }
}
