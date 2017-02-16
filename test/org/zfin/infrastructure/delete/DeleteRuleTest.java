package org.zfin.infrastructure.delete;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.service.DeleteService;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeleteRuleTest extends AbstractDatabaseTest {

    private DeleteService service = new DeleteService();

    @Test
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
        assertTrue(reportList.get(0).getValidationMessage().contains("features"));
    }

    @Test
    public void antibodyValidation() {
        // zn-5
        String zdbID = "ZDB-ATB-081002-19";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("expression"));
    }

    @Test
    public void labValidation() {
        // Halloran Lab
        String zdbID = "ZDB-LAB-000114-8";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("Markers"));
        assertTrue(reportList.get(1).getValidationMessage().contains("Features"));
        assertTrue(reportList.get(2).getValidationMessage().contains("members"));
        assertTrue(reportList.get(3).getValidationMessage().contains("lab designation"));
    }

    @Test
    public void companyValidation() {
        // Znomics, Inc
        String zdbID = "ZDB-COMPANY-031215-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("Features"));
        assertTrue(reportList.get(1).getValidationMessage().contains("members"));
    }

    @Test
    public void journalValidation() {
        // Encyclopedia fo Neurosciences
        String zdbID = "ZDB-JRNL-050621-687";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("associated"));
    }

    @Test
    public void personValidation() {
        // Monte
        String zdbID = "ZDB-PERS-960805-676";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("labs"));
        assertTrue(reportList.get(1).getValidationMessage().contains("publications"));
    }

    @Test
    public void eFGValidation() {
        // mCherry
        String zdbID = "ZDB-EFG-080214-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("constructs"));
        assertTrue(reportList.get(1).getValidationMessage().contains("figure"));
        assertTrue(reportList.get(2).getValidationMessage().contains("publications"));
    }

    @Test
    public void regionValidation() {
        // UAS
        String zdbID = "ZDB-EREGION-070122-1";
        DeleteEntityRule feature = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = feature.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("constructs"));
        assertTrue(reportList.get(1).getValidationMessage().contains("publications"));
    }

    @Test
    public void sTRValidation() {
        // Talen1-cdh5
        String zdbID = "ZDB-TALEN-131118-4";
        DeleteEntityRule deleteRule = service.getDeleteRule(zdbID);
        List<DeleteValidationReport> reportList = deleteRule.validate();
        assertNotNull(reportList);
        assertTrue(reportList.size() > 0);
        assertTrue(reportList.get(0).getValidationMessage().contains("fish"));
        assertTrue(reportList.get(1).getValidationMessage().contains("GO annotation"));
        assertTrue(reportList.get(2).getValidationMessage().contains("publication"));
    }
}
