package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.dto.VocabularyTermDTO;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Whitelist behavior for {@link ZircVocabularyService}. The query path
 * itself needs a live database and is exercised the same way the sibling
 * {@code ZircAutocompleteService} is — through the running app — but the
 * name check happens before any Hibernate call, so it can be asserted here.
 *
 * <p>JUnit 4 to stay in the test path that actually runs in CI.
 */
public class ZircVocabularyServiceTest {

    @Test
    public void servesExactlyTheKnownVocabularies() {
        assertEquals(
                Set.of("amino_acid_term",
                        "protein_consequence_term",
                        "transcript_consequence_term",
                        "dna_mutation_term",
                        "gene_localization_term"),
                ZircVocabularyService.vocabularyNames());
    }

    @Test
    public void mergeInterleavesSupplementsByOrder() {
        // The ZFIN-10380 shape: two ZIRC-only entries slot into the gap left
        // at 2 and 3 between mdcv terms, so the form sees one ordered list.
        List<VocabularyTermDTO> merged = ZircVocabularyService.merge(List.of(
                ordered(1, "polypeptide truncation", "ZDB-TERM-1"),
                ordered(4, "amino acid substitution", "ZDB-TERM-2"),
                ordered(2, "c-terminal peptide truncation", "zirc:c-term"),
                ordered(3, "n-terminal peptide truncation", "zirc:n-term")));

        assertEquals(
                List.of("polypeptide truncation", "c-terminal peptide truncation",
                        "n-terminal peptide truncation", "amino acid substitution"),
                merged.stream().map(VocabularyTermDTO::label).toList());
        // Supplements keep their provisional token as the stored id.
        assertEquals("zirc:c-term", merged.get(1).id());
    }

    @Test
    public void mergeBreaksOrderTiesOnLabel() {
        // mdcv_term_order has duplicates — amino acids have His and Ile both
        // at 10 — so without a tie-break the rendered order is whatever the
        // database happened to return.
        List<VocabularyTermDTO> merged = ZircVocabularyService.merge(List.of(
                ordered(10, "Ile", "ZDB-TERM-ILE"),
                ordered(10, "His", "ZDB-TERM-HIS")));

        assertEquals(List.of("His", "Ile"),
                merged.stream().map(VocabularyTermDTO::label).toList());
    }

    @Test
    public void unknownVocabularyIsNotFoundRatherThanAQuery() {
        // Guards the whitelist: an unmapped name must fail before it can
        // reach HibernateControlledVocabularyRepository, which interpolates
        // the class simple name straight into an HQL string.
        ZircEntityNotFoundException e = assertThrows(
                ZircEntityNotFoundException.class,
                () -> new ZircVocabularyService().terms("Marker"));
        assertEquals(true, e.getMessage().contains("No such vocabulary: Marker"));
    }

    private static ZircVocabularyService.Ordered ordered(int order, String label, String id) {
        return new ZircVocabularyService.Ordered(
                order, label, new VocabularyTermDTO(id, label, null));
    }
}
