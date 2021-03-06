package org.zfin.infrastructure;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests MarkerEnum
 */
public class EnumValidationTest extends AbstractDatabaseTest {


    private static Logger logger = LogManager.getLogger(EnumValidationTest.class);

    private EnumValidationService enumValidationService = new EnumValidationService();

    @Test
    public void validateAllServices() {
        try {
            enumValidationService.checkAllEnums();
        } catch (EnumValidationException eve) {
            logger.fatal(eve);
            fail(eve.toString());
        }
        String report = enumValidationService.getReport();
        if (enumValidationService.isReportError())
            assertEquals(null, report);
    }


    @Test
    public void getCollectionDifference() {
        List<String> namesOne = new ArrayList<String>();
        namesOne.add("Walter");
        namesOne.add("Heinrich");

        List<String> namesTwo = new ArrayList<String>();
        namesTwo.add("Walter");
        namesTwo.add("Heinrich");

        String errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNull(errorReport);

        namesTwo.add("Ingrid");
        errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNotNull(errorReport);

    }

}
