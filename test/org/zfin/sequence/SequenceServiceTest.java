package org.zfin.sequence;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.SequenceService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

        var response = sequenceService.getMarkerDBLinkJsonResultResponse(markerZdbId, pagination, false, false);
        var results = response.getResults();

        assertEquals("ZDB-GENE-041014-357 should have 20 sequences, but has " + results.size(), 20, results.size());
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
//        ZDB-DBLINK-090218-130:DX504044:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
        String markerZdbID = "ZDB-GENE-041014-357";
        String bacZdbID = "ZDB-BAC-041007-134";

        List<Triple<String, String, String>> accessions = new ArrayList<>();
        accessions.add(new ImmutableTriple<>(markerZdbID, "CAI11751", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "GQ202546", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "GDQH01030811", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NM_001163291", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NM_001369129", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NP_001156763", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NP_001356058", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "ACS94979", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "C6KFA3", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "DX504044", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "C6KFA3", "UniProtKB"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "DX504044", "GenBank"));
        accessions.add(new ImmutableTriple<>(bacZdbID, "BX004780", "GenBank"));
        accessions.add(new ImmutableTriple<>(bacZdbID, "BX004780", "GenBank"));

        List<MarkerDBLink> links = new ArrayList<>();
        List<String> accessionsNotFound = new ArrayList<>();
        for (Triple<String, String, String> acc : accessions) {
            MarkerDBLink mdl = (MarkerDBLink) getSequenceRepository().getDBLink(acc.getLeft(), acc.getMiddle(), acc.getRight());
            links.add(mdl);
            if (mdl == null) {
                accessionsNotFound.add(acc.getLeft() + " " + acc.getMiddle() + " " + acc.getRight());
            }
        }

        assertEquals("Accessions not found: " + accessionsNotFound.toString(), 0, accessionsNotFound.size());
        assertEquals(links.size(), 14);

        List<MarkerDBLink> aggregatedLinks = MarkerService.aggregateDBLinksByPub(links);

        assertEquals(aggregatedLinks.size(), 12);
    }

    @Test
    @Ignore
    public void testAllGenes() {

        List<String> allGeneIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("genes_without_ids.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                allGeneIds.add(values[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileWriter writer = new FileWriter("sequenceIDs.txt", false);


//            List<String> ids = List.of("ZDB-GENE-141216-102");
            allGeneIds.forEach(id -> {
                try {
                    JsonResultResponse<MarkerDBLink> response = sequenceService.getMarkerDBLinkJsonResultResponse(id, new Pagination(), true, false);
                    response.getResults().forEach(markerDBLink -> {
                        try {
                            String line = id + "," + markerDBLink.getAccessionNumber();
                            writer.write(line + "\n");
                        } catch (Exception e) {
                            logger.error("Error writing to file", e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error writing to file", e);
                }
            });
            writer.close();
            List<Accession> accessions = RepositoryFactory.getSequenceRepository().getAccessionsByNumber("ENSDARG00000002898");
        } catch (Exception e) {
            logger.error("Error writing to file", e);
        }
        //assertNotNull(accessions);
    }
}
