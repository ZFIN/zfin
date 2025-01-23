package org.zfin.sequence;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.datatransfer.webservice.NCBIRefSeqFetch;
import org.zfin.datatransfer.webservice.NCBIRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NCBIEfetchTest {

    @Test
    public void useEfetchForNucleotide() {
        String accession = "AY627769";
        NCBIEfetch.Type type = NCBIEfetch.Type.NUCLEOTIDE;

        try {
            String response = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                    .with("db", type.getVal())
                    .with("id", accession)
                    .with("retmax", 1)
                    .getFasta();
            String[] lines = response.split("\\n", 2);
            assertEquals(lines.length, 2);
            assertEquals(">AY627769.1 Danio rerio HMG-box transcription factor Sox9b (sox9b) gene, complete cds", lines[0]);

        } catch (IOException e) {
            fail("caught: " + e.getMessage());
        }
    }

    @Test
    public void useEfetchForMultipleNucleotides() {
        String accession = "GDQH01022534,GFIL01020719";
        NCBIEfetch.Type type = NCBIEfetch.Type.NUCLEOTIDE;

        try {
            String response = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                    .with("db", type.getVal())
                    .with("id", accession)
                    .with("retmax", 5000)
                    .getFasta();
            String[] records = response.split(">", 3);
            assertEquals(3, records.length);

            String record1 = records[1];
            String record2 = records[2];

            String[] record1lines = record1.split("\\n", 2);
            String[] record2lines = record2.split("\\n", 2);

            assertEquals("GDQH01022534.1 TSA: Danio rerio CG2_NonNorm_contig_22545 transcribed RNA sequence", record1lines[0]);
            assertEquals("GFIL01020719.1 TSA: Danio rerio FDR_LOC100700518.1.1 transcribed RNA sequence", record2lines[0]);

        } catch (IOException e) {
            fail("caught: " + e.getMessage());
        }
    }

    @Test
    public void useRefSeqFetchForNucleotide() throws FileNotFoundException, JsonProcessingException {
        List<String> refseqs = List.of("NM_131184", "XM_021480495");
        Map<String, NCBIRefSeqFetch.NCBIRefSeqData> details = new NCBIRefSeqFetch().fetchRefSeqsByID(refseqs);
        NCBIRefSeqFetch.NCBIRefSeqData nmData = details.get("NM_131184");
        assertEquals("NM_131184", nmData.caption());

        NCBIRefSeqFetch.NCBIRefSeqData xmData = details.get("XM_021480495");
        assertEquals("XM_021480495", xmData.caption());

        NCBIRefSeqFetch.writeCache(new File("/tmp/output.json"), details);
    }

    @Test
    public void useRefSeqFetchWithCacheForNucleotide() throws IOException {
        List<String> refseqs = List.of("NM_131184", "XM_021480495");

        NCBIRefSeqFetch fetcher = new NCBIRefSeqFetch();
        fetcher.setCacheFile(createTempFixtureFileForRefSeqJson());
        Map<String, NCBIRefSeqFetch.NCBIRefSeqData> details = fetcher.fetchRefSeqsByID(refseqs);
        NCBIRefSeqFetch.NCBIRefSeqData nmData = details.get("NM_131184");
        assertEquals("NM_131184", nmData.caption());
        assertEquals("from cache", nmData.comment());

        NCBIRefSeqFetch.NCBIRefSeqData xmData = details.get("XM_021480495");
        assertEquals("XM_021480495", xmData.caption());
        assertEquals("from cache", xmData.comment());
    }

    private File createTempFixtureFileForRefSeqJson() throws IOException {
        File tempFile = Files.createTempFile("temp", ".json").toFile();
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(refseqTestFixtureJsonData());
        }
        return tempFile;
    }

    private String refseqTestFixtureJsonData() {
        return """
            {
              "NM_131184" : {
                "uid" : "45433522",
                "caption" : "NM_131184",
                "comment" : "from cache",
                "status" : null,
                "replacedby" : null
              },
              "XM_021480495" : {
                "uid" : "2800552124",
                "caption" : "XM_021480495",
                "comment" : "from cache",
                "status" : null,
                "replacedby" : null
              }
            }
            """;
    }

}
