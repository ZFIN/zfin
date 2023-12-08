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

public class UniProtLoadAddAttributionTest extends AbstractDatabaseTest {

    /**
     * If a UniProt record has a RefSeq match, meaning we want to load it, but it already has a manual curation attribution,
     * we want to keep that attribution and add the attribution to indicate the load logic agrees.
     */
    @Test
    public void handleUniprotLoadAgreesWithManualCuration() throws JsonProcessingException {
        UniProtLoadTask loadTask = new UniProtLoadTask("", "", "", false, "", "");
        String record = testDat();
        loadTask.setContext(testContext());
        try (BufferedReader inputFileReader = new BufferedReader(new StringReader(record)) ) {
            Map<String, RichSequenceAdapter> entries = loadTask.readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = loadTask.executePipeline(entries);

            assertEquals(1, actions.size());

            UniProtLoadAction action = actions.iterator().next();

            assertEquals(action.getType(), UniProtLoadAction.Type.LOAD);
            assertEquals(action.getSubType(), UniProtLoadAction.SubType.ADD_ATTRIBUTION);
            assertEquals(action.getAccession(), "A4IGB0");
            assertEquals(action.getGeneZdbID(), "ZDB-GENE-141215-12");


        } catch (IOException | BioException e) {
            throw new RuntimeException(e);
        }
    }

    private String testDat() {
        return """
                ID   A4IGB0_DANRE            Unreviewed;       521 AA.
                AC   A4IGB0;
                DT   01-MAY-2007, integrated into UniProtKB/TrEMBL.
                OS   Danio rerio (Zebrafish) (Brachydanio rerio).
                OX   NCBI_TaxID=7955 {ECO:0000313|EMBL:AAI35009.1};
                DR   RefSeq; NP_001077286.1; NM_001083817.1.
                //
                """;
    }

    /**
     * This context sets up the test with an existing gene/uniprot link with only a manual attribution.
     * And it contains the RefSeq link for matching so the load will automatically make the connection.
     * @return
     * @throws JsonProcessingException
     */
    private UniProtLoadContext testContext() throws JsonProcessingException {
        String json = """
                {
                  "uniprotDbLinks": {
                    "A4IGB0": [
                      {
                        "accession": "A4IGB0",
                        "dataZdbID": "ZDB-GENE-141215-12",
                        "markerAbbreviation": "si:ch73-42k18.1",
                        "dbName": "UNIPROTKB",
                        "publicationIDs": [
                          "ZDB-PUB-220705-2"
                        ]
                      }
                    ]
                  },
                  "refseqDbLinks": {
                    "NP_001077286": [
                      {
                        "accession": "NP_001077286",
                        "dataZdbID": "ZDB-GENE-141215-12",
                        "markerAbbreviation": "si:ch73-42k18.1",
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
