package org.zfin.ontology.datatransfer;

import org.junit.Test;
import org.zfin.ontology.datatransfer.service.LoadOntology;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Load a given ontology.
 */
public class LoadOntologyTest {


    @Test
    public void getRevisionFromGeneOntologyFile(){
        String revision = "cvs version: $Revision: 1.1416 $";
        assertEquals("revision number", "1.1416", LoadOntology.getRevisionFromComment(revision));
    }

    // CHEBI:760892 (ChEBI release 256): 336 characters, but cut to 255 characters
    // it is still more than 255 bytes, which broke the load into tmp_syns.
    private static final String CHEBI_760892_SYNONYM = "<em>N</em><small><sup>4</small></sup>-(α-<small>D</small>-Man-(1→2)-α-<small>D</small>-Man-(1→3)-[α-<small>D</small>-6-PO<small><sub>4</small></sub>-Man-(1→3)-[α-<small>D</small>-Man-(1→6)]-α-<small>D</small>-Man-(1→6)]-β-<small>D</small>-Man-(1→4)-β-<small>D</small>-GlcNAc-(1→4)-β-<small>D</small>-GlcNAc)-<small>L</small>-Asn residue";

    @Test
    public void truncateMultiByteSynonymToByteLimit() {
        String truncated = LoadOntology.truncateToUtf8Bytes(CHEBI_760892_SYNONYM, 255);
        int bytes = truncated.getBytes(StandardCharsets.UTF_8).length;
        assertTrue("expected at most 255 bytes, got " + bytes, bytes <= 255);
        assertTrue(CHEBI_760892_SYNONYM.startsWith(truncated));
        // nothing more fits: the next character would go over the limit
        String next = CHEBI_760892_SYNONYM.substring(0, truncated.length() + 1);
        assertTrue(next.getBytes(StandardCharsets.UTF_8).length > 255);
    }

    @Test
    public void truncateNeverSplitsACharacter() {
        // "→" is 3 bytes; 4 bytes leave room for "a" plus one arrow, not two
        assertEquals("a→", LoadOntology.truncateToUtf8Bytes("a→→", 4));
        // a 4-byte character outside the BMP (surrogate pair in Java) stays whole
        assertEquals("a", LoadOntology.truncateToUtf8Bytes("a𝛼", 4));
        assertEquals("a𝛼", LoadOntology.truncateToUtf8Bytes("a𝛼", 5));
    }

    @Test
    public void truncateLeavesShortTextUnchanged() {
        assertEquals("glucose", LoadOntology.truncateToUtf8Bytes("glucose", 255));
        assertEquals("", LoadOntology.truncateToUtf8Bytes("", 255));
        assertNull(LoadOntology.truncateToUtf8Bytes(null, 255));
    }
}
