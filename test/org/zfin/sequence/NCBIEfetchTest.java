package org.zfin.sequence;

import org.junit.Test;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.datatransfer.webservice.NCBIRequest;

import java.io.IOException;

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

}
