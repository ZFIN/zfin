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

public class UniProtLoadRemoveAttributionTest extends AbstractDatabaseTest {

    /**
     * If an UniProt record no longer has a RefSeq match, meaning we want delete it, but it already has a manual curation attribution,
     * we keep the uniprot record and its gene association, but remove the automatic attribution from the dblink.
     */
    @Test
    public void handleUniprotLoadDisagreesWithManualCuration() throws JsonProcessingException {
        UniProtLoadTask loadTask = new UniProtLoadTask("", "", "", false, "", "");
        String record = testDat();
        loadTask.setContext(testContext());
        try (BufferedReader inputFileReader = new BufferedReader(new StringReader(record)) ) {
            Map<String, RichSequenceAdapter> entries = loadTask.readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = loadTask.executePipeline(entries);
            assertEquals(1, actions.size());

            UniProtLoadAction action = actions.iterator().next();

            assertEquals(action.getType(), UniProtLoadAction.Type.DELETE);
            assertEquals(action.getSubType(), UniProtLoadAction.SubType.REMOVE_ATTRIBUTION);
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
                          "ZDB-PUB-220705-2",
                          "ZDB-PUB-230615-71"
                        ]
                      }
                    ]
                  },
                  "refseqDbLinks": {}
                }
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, UniProtLoadContext.class);
    }

}
