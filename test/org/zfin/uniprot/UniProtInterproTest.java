package org.zfin.uniprot;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.adapter.RichStreamReaderAdapter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zfin.uniprot.datfiles.DatFileReader.getRichStreamReaderForUniprotDatString;

public class UniProtInterproTest extends AbstractDatabaseTest {

    @Test
    public void parseECCorrectly() {
        String record = testDat();
        try {
            RichStreamReaderAdapter reader = getRichStreamReaderForUniprotDatString(record, true);
            RichSequenceAdapter sequence = reader.nextRichSequence();
            List<CrossRefAdapter> result = sequence.getECCrossReferences().stream().toList();
            assertEquals(2, result.size());
            assertTrue(result.get(0).getAccession().equals("2.7.11.27"));
            assertTrue(result.get(1).getAccession().equals("2.7.11.31"));
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
    }

    private String testDat() {
        return """
                ID   E9QDC9_DANRE          Unreviewed;         116 AA.
                AC   E9QDC9; A0A8M1PV84;
                DE   RecName: Full=Acetyl-CoA carboxylase kinase {ECO:0000256|ARBA:ARBA00032270};
                DE            EC=2.7.11.27 {ECO:0000256|ARBA:ARBA00012412};
                DE            EC=2.7.11.31 {ECO:0000256|ARBA:ARBA00012403};
                DE   AltName: Full=Hydroxymethylglutaryl-CoA reductase kinase {ECO:0000256|ARBA:ARBA00032865};                
                DR   RefSeq; XP_001343958.4; XM_001343922.7.
                PE   3: Inferred from homology;
                KW   Metal-binding {ECO:0000256|ARBA:ARBA00022723}; Reference proteome {ECO:0000313|Proteomes:UP000000437}; Zinc {ECO:0000256|ARBA:ARBA00022833}.
                //
                """;
    }

}
