package org.zfin.anatomy.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.zfin.anatomy.DevelopmentStage;

/**
 * This is the test class that tests funcionality of the AnatomyPresentation class,
 * a helper class to provide convenience methods for presentation.
 */
public class StagePresentationTest {

    /**
     * This tests the formatHourToHourOrDay() method in the AnatomyPresentation class.
     */
    @Test
    public void hourAndDayConversion() {
        float hour = 10.25F;
        String testString = StagePresentation.formatHourToHourOrDay(hour);
        assertEquals("Hour string", "10.25h", testString);

        hour = 120;
        testString = StagePresentation.formatHourToHourOrDay(hour);
        assertEquals("Hour string", "120.0h", testString);

        hour = 168;
        testString = StagePresentation.formatHourToHourOrDay(hour);
        assertEquals("Hour string", "168.0h", testString);

        hour = 169;
        testString = StagePresentation.formatHourToHourOrDay(hour);
        assertEquals("Day string", "7d", testString);

        hour = 1240;
        testString = StagePresentation.formatHourToHourOrDay(hour);
        assertEquals("Day string", "51d", testString);

        // test negative hours: throws Exception

        boolean failed = false;
        try {
            StagePresentation.formatHourToHourOrDay(-12);
        } catch (Exception e) {
            assertEquals("Exception", "Hours have to be positive!", e.getMessage());
            failed = true;
        }
        if (!failed) {
            fail("Failed to throw Exception");
        }
    }

    /**
     * If no valid stage, no name given, then return a null value for the display String
     */
    @Test
    public void noValidStage() {
        DevelopmentStage stage = new DevelopmentStage();
        String displayName = StagePresentation.createDisplayEntry(stage);
        assertEquals(displayName, null);

    }

    @Test
    public void developmentStageShortVersion() {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setName("Pharyngula:Prim-15");
        stage.setAbbreviation("Prim-15");
        stage.setHoursStart(30);
        stage.setHoursEnd(36);
        String link = DevelopmentStagePresentation.getLink(stage, false);
        assertEquals("<a href=\"/zf_info/zfbook/stages/index.html#Pharyngula\">Prim-15</a>", link);
    }

    @Test
    public void developmentStageLongVersion() {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setName("Pharyngula:Prim-15");
        stage.setAbbreviation("Prim-15");
        stage.setHoursStart(30);
        stage.setHoursEnd(36);
        String link = DevelopmentStagePresentation.getLink(stage, true);
        assertEquals("<a href=\"/zf_info/zfbook/stages/index.html#Pharyngula\">Pharyngula:Prim-15 (30.0h-36.0h)</a>", link);
    }

    @Test
    public void developmentStageUnknown() {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setName(DevelopmentStage.UNKNOWN);
        stage.setAbbreviation("unk");
        stage.setHoursStart(0);
        stage.setHoursEnd(17520);
        String link = DevelopmentStagePresentation.getLink(stage, true);
        assertEquals("<a href=\"/zf_info/zfbook/stages/index.html#Unknown\">Unknown</a>", link);
    }

}
