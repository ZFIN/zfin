package org.zfin.sequence.load;

import org.junit.Test;
import org.zfin.marker.TranscriptType;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.zfin.marker.TranscriptType.Type.*;

/**
 * Pure unit tests for {@link EnsemblTranscriptFastaReadProcess#BIOTYPE_TO_ZFIN_TYPE}.
 * Verifies the biotype → ZFIN TranscriptType mapping introduced for ZFIN-10222.
 * Does not exercise the DB-touching loader path.
 */
public class EnsemblTranscriptBiotypeMappingTest {

    @Test
    public void mtRrnaMapsToRrna() {
        // The literal ticket: Mt_rRNA (Ensembl's biotype string, underscore form)
        // must map to ZFIN's rRNA, not the default mRNA.
        assertEquals(RRNA, EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE.get("Mt_rRNA"));
    }

    @Test
    public void proteinCodingAndCodingVariantsAreMrna() {
        Map<String, TranscriptType.Type> m = EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE;
        for (String biotype : List.of("protein_coding",
                                       "IG_C_gene", "TR_J_gene", "TR_V_gene", "TR_D_gene")) {
            assertEquals("expected MRNA for " + biotype, MRNA, m.get(biotype));
        }
    }

    @Test
    public void splicingVariantsAreNcrna() {
        // Per Sridhar on ZFIN-10222: retained_intron, nonsense_mediated_decay,
        // and non_stop_decay transcripts are non-coding, not mRNA.
        Map<String, TranscriptType.Type> m = EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE;
        for (String biotype : List.of("retained_intron",
                                       "nonsense_mediated_decay",
                                       "non_stop_decay")) {
            assertEquals("expected NCRNA for " + biotype, NCRNA, m.get(biotype));
        }
    }

    @Test
    public void rnaFamilyBiotypesMapToTheirZfinTypes() {
        Map<String, TranscriptType.Type> m = EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE;
        assertEquals(RRNA,     m.get("rRNA"));
        assertEquals(TRNA,     m.get("Mt_tRNA"));
        assertEquals(MIRNA,    m.get("miRNA"));
        assertEquals(SNRNA,    m.get("snRNA"));
        assertEquals(SNORNA,   m.get("snoRNA"));
        assertEquals(SNORNA,   m.get("scaRNA"));   // scaRNA is a class of snoRNA
        assertEquals(LINCRNA,  m.get("lincRNA"));
        assertEquals(ANTISENSE, m.get("antisense"));
    }

    @Test
    public void miscellaneousNonCodingMapsToNcrna() {
        Map<String, TranscriptType.Type> m = EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE;
        for (String biotype : List.of("misc_RNA", "sRNA", "sense_intronic",
                                       "sense_overlapping", "processed_transcript",
                                       "ribozyme", "TEC")) {
            assertEquals("expected NCRNA for " + biotype, NCRNA, m.get(biotype));
        }
    }

    @Test
    public void allPseudogeneFlavoursMapToPseudogenicTranscript() {
        Map<String, TranscriptType.Type> m = EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE;
        for (String biotype : List.of("pseudogene", "processed_pseudogene",
                                       "unprocessed_pseudogene",
                                       "transcribed_unprocessed_pseudogene",
                                       "polymorphic_pseudogene",
                                       "IG_V_pseudogene", "IG_C_pseudogene",
                                       "IG_J_pseudogene", "IG_pseudogene",
                                       "TR_V_pseudogene")) {
            assertEquals("expected PSEUDOGENIC_TRANSCRIPT for " + biotype,
                    PSEUDOGENIC_TRANSCRIPT, m.get(biotype));
        }
    }

    @Test
    public void unknownBiotypeReturnsNull() {
        // Loader treats null as "skip with warning" — never auto-load an unmapped biotype.
        assertNull(EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE.get("totally_made_up_biotype"));
    }

    @Test
    public void mapIsImmutable() {
        try {
            EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE.put("foo", MRNA);
            assertFalse("BIOTYPE_TO_ZFIN_TYPE must be unmodifiable", true);
        } catch (UnsupportedOperationException expected) {
            // ok
        }
    }
}
