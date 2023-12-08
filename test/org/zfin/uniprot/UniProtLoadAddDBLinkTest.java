package org.zfin.uniprot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.biojava.bio.BioException;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.task.UniProtLoadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class UniProtLoadAddDBLinkTest extends AbstractDatabaseTest {

    /**
     * If a UniProt record has a RefSeq match, meaning we want to load it, but it already has a manual curation attribution,
     * we want to keep that attribution and add the attribution to indicate the load logic agrees.
     */
    @Test
    public void handleUniprotBasicLoadLogicMatchRefSeq() throws JsonProcessingException {
        UniProtLoadTask loadTask = new UniProtLoadTask("", "", "", false, "", "");
        String record = testDat();
        loadTask.setContext(testContext());
        try (BufferedReader inputFileReader = new BufferedReader(new StringReader(record)) ) {
            Map<String, RichSequenceAdapter> entries = loadTask.readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = loadTask.executePipeline(entries);

            assertEquals(1, actions.size());

            UniProtLoadAction action = actions.iterator().next();

            assertEquals(action.getType(), UniProtLoadAction.Type.LOAD);
            assertEquals(action.getSubType(), UniProtLoadAction.SubType.MATCH_BY_REFSEQ);
            assertEquals(action.getAccession(), "ABCDEFG");
            assertEquals(action.getGeneZdbID(), "ZDB-GENE-123456-78");


        } catch (IOException | BioException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is the test data for the above test.
     * It contains a record like what we get from uniprot dat file releases
     * It has a RefSeq link that matches a gene in ZFIN.
     * @return
     */
    private String testDat() {
        return """
                ID   ABCDEFG_DANRE            Unreviewed;       521 AA.
                AC   ABCDEFG;
                DT   01-MAY-2007, integrated into UniProtKB/TrEMBL.
                OS   Danio rerio (Zebrafish) (Brachydanio rerio).
                OX   NCBI_TaxID=7955 {ECO:0000313|EMBL:AAI35009.1};
                DR   RefSeq; NP_001234567.1; NM_001234567.1.
                //
                """;
    }

    /**
     * This context sets up the test with no existing gene/uniprot link (uniprotDbLinks is empty)
     * And it contains the RefSeq link for matching (NP00123456) so the load will automatically make the connection.
     * @return
     * @throws JsonProcessingException
     */
    private UniProtLoadContext testContext() throws JsonProcessingException {
        //
        String json = """
                {
                  "uniprotDbLinks": {},
                  "refseqDbLinks": {
                    "NP_001234567": [
                      {
                        "accession": "NP_001234567",
                        "dataZdbID": "ZDB-GENE-123456-78",
                        "markerAbbreviation": "testgene",
                        "dbName": "REFSEQ",
                        "publicationIDs": [
                          "ZDB-PUB-130725-2"
                        ]
                      }
                    ]
                  }
                }
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, UniProtLoadContext.class);
    }

}
