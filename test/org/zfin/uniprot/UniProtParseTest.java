package org.zfin.uniprot;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.adapter.RichStreamReaderAdapter;
import org.zfin.uniprot.datfiles.DatFileWriter;
import org.zfin.uniprot.handlers.RemoveVersionHandler;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zfin.uniprot.datfiles.DatFileReader.getRichStreamReaderForUniprotDatString;
import static org.zfin.uniprot.datfiles.DatFileReader.getMapOfAccessionsToSequencesFromStreamReader;

public class UniProtParseTest extends AbstractDatabaseTest {

    @Test
    public void parseRefSeqLineCorrectly() {
        String record = testBareBonesDat();
        try {
            RichStreamReaderAdapter reader = getRichStreamReaderForUniprotDatString(record, true);
            RichSequenceAdapter sequence = reader.nextRichSequence();
            String rawData = DatFileWriter.sequenceToString(sequence);
            System.out.println(rawData);
            System.out.flush();
            assertTrue(rawData.contains("RefSeq; XP_001343958.4; XM_001343922.7"));
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
    }

    @Test
    public void parseRefSeqLineAndRemoveVersion() {
        String record = testBareBonesDat();
        try {
            RichStreamReaderAdapter reader = getRichStreamReaderForUniprotDatString(record, true);
            Map<String, RichSequenceAdapter> sequencesByAccession = getMapOfAccessionsToSequencesFromStreamReader(reader);
            RemoveVersionHandler handler = new RemoveVersionHandler();
            handler.handle(sequencesByAccession, null, null);
            String rawData = DatFileWriter.sequenceToString(sequencesByAccession.get("E9QDC9"));
            assertFalse(rawData.contains("RefSeq; XP_001343958; XM_001343922.7."));
            assertTrue(rawData.contains("RefSeq; XP_001343958; XM_001343922."));

        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
    }

    @Test
    public void getRefSeqsBoth() {
        String record = testDat();
        try {
            RichStreamReaderAdapter reader = getRichStreamReaderForUniprotDatString(record, true);
            Map<String, RichSequenceAdapter> sequencesByAccession = getMapOfAccessionsToSequencesFromStreamReader(reader);
            assertEquals(1, sequencesByAccession.size());
            RichSequenceAdapter sequence = sequencesByAccession.get("E9QDC9");
            assertEquals(2, sequence.getRefSeqs().size());

        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
    }


    @Test
    public void comparatorTest() {
        Set<UniProtLoadLink> links = new TreeSet<>();
        UniProtLoadLink link1 = new UniProtLoadLink("ZFIN", "https://zfin.org");
        UniProtLoadLink link2 = new UniProtLoadLink("ZFIN", "https://zfin.org");
        links.add(link1);
        links.add(link2);
        assertEquals(1, links.size());

    }

    private String testBareBonesDat() {
        return """
                ID   E9QDC9_DANRE          Unreviewed;         116 AA.
                AC   E9QDC9; A0A8M1PV84;
                DR   RefSeq; XP_001343958.4; XM_001343922.7.
                PE   3: Inferred from homology;
                KW   Metal-binding {ECO:0000256|ARBA:ARBA00022723}; Reference proteome {ECO:0000313|Proteomes:UP000000437}; Zinc {ECO:0000256|ARBA:ARBA00022833}.
                //
                """;
    }

    private String testDat() {
        return """
                ID   E9QDC9_DANRE          Unreviewed;         116 AA.
                AC   E9QDC9; A0A8M1PV84;
                DT   05-APR-2011, integrated into UniProtKB/TrEMBL.
                DT   05-APR-2011, sequence version 1.
                DT   28-JUN-2023, entry version 56.
                DE   RecName: Full=Protein yippee-like {ECO:0000256|RuleBase:RU110713};.
                OS   Danio rerio (Zebrafish) (Brachydanio rerio).
                OC   .
                OX   NCBI_TaxID=7955;
                CC   -!- SIMILARITY: Belongs to the yippee family.
                CC       {ECO:0000256|ARBA:ARBA00005613, ECO:0000256|RuleBase:RU110713}.
                CC   ---------------------------------------------------------------------------
                CC   Copyrighted by the UniProt Consortium, see https://www.uniprot.org/terms
                CC   Distributed under the Creative Commons Attribution (CC BY 4.0) License
                CC   ---------------------------------------------------------------------------
                DR   RefSeq; XP_001343958; -.
                PE   3: Inferred from homology;
                KW   Metal-binding {ECO:0000256|ARBA:ARBA00022723}; Reference proteome {ECO:0000313|Proteomes:UP000000437}; Zinc {ECO:0000256|ARBA:ARBA00022833}.
                //
                """;
    }

}
