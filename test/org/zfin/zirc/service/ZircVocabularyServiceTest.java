package org.zfin.zirc.service;

import org.junit.Test;

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
    public void unknownVocabularyIsNotFoundRatherThanAQuery() {
        // Guards the whitelist: an unmapped name must fail before it can
        // reach HibernateControlledVocabularyRepository, which interpolates
        // the class simple name straight into an HQL string.
        ZircEntityNotFoundException e = assertThrows(
                ZircEntityNotFoundException.class,
                () -> new ZircVocabularyService().terms("Marker"));
        assertEquals(true, e.getMessage().contains("No such vocabulary: Marker"));
    }
}
