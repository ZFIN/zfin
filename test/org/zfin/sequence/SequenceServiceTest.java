package org.zfin.sequence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.framework.api.Pagination;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.SequenceService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

/**
 * Class SequenceRepositoryTest.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class SequenceServiceTest extends AbstractDatabaseTest {

    private final static Logger logger = LogManager.getLogger(SequenceServiceTest.class);
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    @Autowired
    private SequenceService sequenceService;

    /**
     * This tests the behavior for sequence table generation for clone pages as
     * described in ZFIN-7799
     */
    @Test
    public void testSequenceServiceJsonResults() {
        var pagination = new Pagination();
        var markerZdbId = "ZDB-GENE-041014-357";

        var response = sequenceService.getMarkerDBLinkJsonResultResponse(markerZdbId, pagination, false);
        var results = response.getResults();

        assertEquals(16, results.size());
    }

    @Test
    public void testSequenceServiceJsonAggregation() {
        //Testing this result set that is retrieved for ZDB-GENE-041014-357 (adgrg6)
//        ZDB-DBLINK-060223-115:CAI11751:GENPEPT
//        ZDB-DBLINK-090926-31203:GQ202546:GENBANK
//        ZDB-DBLINK-200410-110909:GDQH01030811:GENBANK
//        ZDB-DBLINK-200410-137186:NM_001163291:REFSEQ
//        ZDB-DBLINK-200410-139634:NM_001369129:REFSEQ
//        ZDB-DBLINK-200410-181279:NP_001156763:REFSEQ
//        ZDB-DBLINK-200410-183708:NP_001356058:REFSEQ
//        ZDB-DBLINK-200410-62470:ACS94979:GENPEPT
//        ZDB-DBLINK-200410-64440:C6KFA3:GENPEPT
//        ZDB-DBLINK-200410-81082:DX504044:GENBANK
//        ZDB-DBLINK-220118-119663:U3JAV0:UNIPROTKB
//        ZDB-DBLINK-220118-150587:F1Q7U5:UNIPROTKB
//        ZDB-DBLINK-220118-15278:U3JAN0:UNIPROTKB
//        ZDB-DBLINK-220118-36814:E7F1G3:UNIPROTKB
//        ZDB-DBLINK-220118-97652:C6KFA3:UNIPROTKB
//        ZDB-DBLINK-090218-130:DX504044:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
        String markerZdbID = "ZDB-GENE-041014-357";
        String bacZdbID = "ZDB-BAC-041007-134";
        List<MarkerDBLink> links = new ArrayList<>();

        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "CAI11751", "GenPept"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "GQ202546", "GenBank"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "GDQH01030811", "GenBank"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "NM_001163291", "RefSeq"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "NM_001369129", "RefSeq"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "NP_001156763", "RefSeq"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "NP_001356058", "RefSeq"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "ACS94979", "GenPept"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "C6KFA3", "GenPept"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "DX504044", "GenBank"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "U3JAV0", "UniProtKB"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "F1Q7U5", "UniProtKB"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "U3JAN0", "UniProtKB"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "E7F1G3", "UniProtKB"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "C6KFA3", "UniProtKB"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(markerZdbID, "DX504044", "GenBank"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(bacZdbID, "BX004780", "GenBank"));
        links.add((MarkerDBLink)getSequenceRepository().getDBLink(bacZdbID, "BX004780", "GenBank"));

        for(int i = 0; i < links.size(); i++) {
            assertNotNull("Null Value at " + i, links.get(i));
        }
        assertEquals(links.size(), 18);

        List<MarkerDBLink> aggregatedLinks = MarkerService.aggregateDBLinksByPub(links);

        assertEquals(aggregatedLinks.size(), 16);
    }

}


