package org.zfin.infrastructure;

import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Log4j2
public class EnumValidationTest extends AbstractDatabaseTest {


    private final EnumValidationService enumValidationService = new EnumValidationService();

    @Test
    public void validateAllServices() {
        try {
            enumValidationService.checkAllEnums();
        } catch (EnumValidationException eve) {
            log.fatal(eve);
            fail(eve.toString());
        }
        String report = enumValidationService.getReport();
        if (enumValidationService.isReportError())
            assertNull(report);
    }


    @Test
    public void getCollectionDifference() {
        List<String> namesOne = new ArrayList<>();
        namesOne.add("Walter");
        namesOne.add("Heinrich");

        List<String> namesTwo = new ArrayList<>();
        namesTwo.add("Walter");
        namesTwo.add("Heinrich");

        String errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNull(errorReport);

        namesTwo.add("Ingrid");
        errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNotNull(errorReport);

    }

}
