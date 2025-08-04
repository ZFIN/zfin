package org.zfin.datatransfer.ncbi;



import org.apache.commons.io.FileUtils;
import org.junit.Test;
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
