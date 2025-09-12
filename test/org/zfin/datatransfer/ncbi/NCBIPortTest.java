package org.zfin.datatransfer.ncbi;



import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.publication.Publication;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class NCBIPortTest {

    @Test
    public void testLengthCalculation() throws IOException {
        NCBIDirectPort port = new NCBIDirectPort();
        port.workingDir = new File("/tmp");
        File testFixtureRefSeqCatalog = new File("/tmp/RefSeqCatalog.gz");
        org.zfin.util.FileUtil.writeToFileOrZip(testFixtureRefSeqCatalog, "7955\tDanio rerio\tXR_662331.5\tcomplete|vertebrate_other\tMODEL\t670", "utf-8");
        Map<String, Integer> results = port.parseRefSeqCatalogFileForSequenceLength();
        assertEquals(1, results.size());
        assertEquals(Integer.valueOf(670), results.get("XR_662331"));
    }

    @Test
    public void filterRedundantWarningsTest() {
        List<LoadReportAction> existingWarnings = new ArrayList<>();
        LoadReportAction warning1 = new LoadReportAction();
        warning1.setType(LoadReportAction.Type.WARNING);
        warning1.setSubType("N to 1");
        warning1.setGeneZdbID("ZDB-GENE-030113-2 ZDB-GENE-030131-2083 ZDB-GENE-030616-413 ZDB-GENE-050208-245");
        warning1.setAccession("100001684 317731");
        existingWarnings.add(warning1);

        LoadReportAction warning2 = new LoadReportAction();
        warning2.setType(LoadReportAction.Type.WARNING);
        warning2.setSubType("N to 1");
        warning2.setGeneZdbID("ZDB-GENE-030616-413");
        warning2.setAccession("317731");
        NCBIDirectPort.addToWarningActionsIfNotDuplicate(existingWarnings, warning2);
        assertEquals(1, existingWarnings.size());
    }

    @Test
    public void compare() {
        NCBIDirectPort port = new NCBIDirectPort();

//        public Integer ctGenesWithRefSeqBefore;
//        public Integer numNCBIgeneIdBefore;
//        public Integer numRefSeqRNABefore;
//        public Integer numRefPeptBefore;
//        public Integer numRefSeqDNABefore;
//        public Integer numGenBankRNABefore;
//        public Integer numGenPeptBefore;
//        public Integer numGenBankDNABefore;
//        public Integer numGenesRefSeqRNABefore;
//        public Integer numGenesRefSeqPeptBefore;
//        public Integer numGenesGenBankBefore;

        port.beforeFile = new File("/tmp/before.csv");
        port.afterFile = new File("/tmp/after.csv");

        port.ctGenesWithRefSeqBefore = 100;
        port.ctGenesWithRefSeqAfter = 150;
        port.numNCBIgeneIdBefore = 200;
        port.numRefSeqRNABefore = 300;
        port.numRefSeqRNAAfter = 350;
        port.numRefPeptBefore = 400;
        port.numRefPeptAfter = 450;
        port.numRefSeqDNABefore = 500;
        port.numRefSeqDNAAfter = 550;
        port.numGenBankRNABefore = 600;
        port.numGenBankRNAAfter = 650;
        port.numGenPeptBefore = 700;
        port.numGenPeptAfter = 750;
        port.numGenBankDNABefore = 800;
        port.numGenBankDNAAfter = 850;
        port.numGenesRefSeqRNABefore = 900;
        port.numGenesRefSeqRNAAfter = 950;
        port.numGenesRefSeqPeptBefore = 1000;
        port.numGenesRefSeqPeptAfter = 1050;
        port.numGenesGenBankBefore = 1100;
        port.numGenesGenBankAfter = 1150;
        port.numNCBIgeneIdBefore = 1200;
        port.numNCBIgeneIdAfter = 1300;

        port.workingDir = new File("/tmp");


        port.writeHtmlReport();
    }
}
