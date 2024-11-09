package org.zfin.infrastructure.delete;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.service.DeleteService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeleteRuleTest extends AbstractDatabaseTest {

    private DeleteService service = new DeleteService();

    @Test
    @Ignore
    // Test is failing to run correctly as a unit test since hibernate 5.6 upgrade
    public void featureValidation() {
        // ti282a
        String zdbID = "ZDB-ALT-980203-1091";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("genotypes"));
        assertTrue(reportList.get(1).getValidationMessage().contains("suppliers"));
    }

    @Test
    public void genotypeValidation() {
        //ignore this test until 2/1/25
        //depends on NOCTUA GPAD LOAD being fixed
        Assume.assumeTrue( new Date().after( new GregorianCalendar(2025,Calendar.FEBRUARY, 1).getTime() ) );

        // fgf8a^ti282a/ti282a
        String zdbID = "ZDB-GENO-980202-822";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("GO annotation"));
        assertTrue(reportList.get(1).getValidationMessage().contains("more than one publication"));
        assertTrue(reportList.get(2).getValidationMessage().contains("component of the following fish"));
    }

    @Test
    public void fishValidation() {
        // fgf8a^ti282a/ti282a
        String zdbID = "ZDB-FISH-150901-16069";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("more than one publication"));

        // maybe some day, but not yet...
        //assertTrue(reportList.get(1).getValidationMessage().contains("is used in more than one disease model"));

        assertTrue(reportList.get(1).getValidationMessage().contains("phenotype"));
        assertTrue(reportList.get(2).getValidationMessage().contains("expression"));
    }


    @Test
    public void constructValidation() {
        // Tg(-6.0itga2b:EGFP)
        String zdbID = "ZDB-TGCONSTRCT-070117-128";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("features"));
    }

    @Test
    public void antibodyValidation() {
        // zn-5
        String zdbID = "ZDB-ATB-081002-19";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("expression"));
    }

    @Test
    public void labValidation() {
        // Halloran Lab
        String zdbID = "ZDB-LAB-000114-8";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("Markers"));
        assertTrue(errorsConcatened.contains("Features"));
        assertTrue(errorsConcatened.contains("members"));
        assertTrue(errorsConcatened.contains("lab designation"));
    }

    @Test
    public void companyValidation() {
        // Znomics, Inc
        String zdbID = "ZDB-COMPANY-031215-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("Features"));
        assertTrue(errorsConcatened.contains("members"));
    }

    @Test
    public void journalValidation() {
        // Encyclopedia fo Neurosciences
        String zdbID = "ZDB-JRNL-050621-687";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("associated"));
    }

    @Test
    public void personValidation() {
        // Monte
        String zdbID = "ZDB-PERS-960805-676";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("labs"));
        assertTrue(errorsConcatened.contains("publications"));
    }

    @Test
    public void eFGValidation() {
        // mCherry
        String zdbID = "ZDB-EFG-080214-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String fullReportString = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(fullReportString.contains("constructs"));
        assertTrue(fullReportString.contains("antibod"));
        assertTrue(fullReportString.contains("figure"));
        assertTrue(fullReportString.contains("publications"));
    }

    @Test
    public void regionValidation() {
        // UAS
        String zdbID = "ZDB-EREGION-070122-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("constructs"));
        assertTrue(errorsConcatened.contains("publications"));
    }

    @Test
    public void sTRValidation() {
        //ignore this test until 2/1/25
        //depends on NOCTUA GPAD LOAD being fixed
        Assume.assumeTrue( new Date().after( new GregorianCalendar(2025,Calendar.FEBRUARY, 1).getTime() ) );

        // Talen1-cdh5
        String zdbID = "ZDB-TALEN-131118-4";
        DeleteEntityRule deleteRule = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = deleteRule.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() >= 2);
        String errorsConcatened = reportList.stream().map(DeleteValidationReport::getValidationMessage).collect(Collectors.joining());
        assertTrue(errorsConcatened.contains("fish"));
        assertTrue(errorsConcatened.contains("GO annotation"));
    }
}
