package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.feature.AminoAcidTerm;
import org.zfin.feature.DnaMutationTerm;
import org.zfin.feature.GeneLocalizationTerm;
import org.zfin.feature.MutationDetailControlledVocabularyTerm;
import org.zfin.feature.ProteinConsequence;
import org.zfin.feature.TranscriptConsequence;
import org.zfin.feature.repository.HibernateControlledVocabularyRepository;
import org.zfin.zirc.dto.VocabularyTermDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Serves the controlled-vocabulary pick lists the lesion form needs
 * (amino acids, protein consequences, transcript consequences) from
 * {@code mutation_detail_controlled_vocabulary} — the same table the GWT
 * curation interface reads.
 *
 * <p>Reading the live table rather than restating the lists in
 * {@code ZircLesionFormSchema} keeps the two UIs in agreement: a
 * reordering migration moves both at once. It also keeps {@code schema()}
 * free of database access, which matters because {@code
 * FormSchemaSnapshotTest} serializes it and {@code LesionStatusComputer}
 * walks it from a static initializer — both would need a live database if
 * the vocabulary were inlined at schema-build time.
 */
@Service
public class ZircVocabularyService {

    /**
     * The {@code mdcv_used_in} discriminator values we serve, mapped to the
     * entity subclass that carries each one. Doubles as the whitelist: a
     * name outside this map never reaches a query, so the path segment
     * can't be used to probe for other entities.
     */
    private static final Map<String, Class<? extends MutationDetailControlledVocabularyTerm>>
            VOCABULARIES = Map.of(
                    "amino_acid_term", AminoAcidTerm.class,
                    "protein_consequence_term", ProteinConsequence.class,
                    "transcript_consequence_term", TranscriptConsequence.class,
                    "dna_mutation_term", DnaMutationTerm.class,
                    "gene_localization_term", GeneLocalizationTerm.class);

    /** The vocabulary names this service will serve. */
    public static Set<String> vocabularyNames() {
        return VOCABULARIES.keySet();
    }

    /**
     * All terms of one vocabulary, in display order.
     *
     * <p>Sorted through {@link MutationDetailControlledVocabularyTerm}'s own
     * {@code compareTo} rather than relying on the repository's {@code order
     * by order} alone: {@code mdcv_term_order} currently contains duplicates
     * within a vocabulary (transcript consequences have four rows at order 3),
     * and an unbroken tie leaves the rendered list at the mercy of whatever
     * order the database returns. {@code compareTo} breaks ties on display
     * name, so the dropdown is stable even before the reordering migrations
     * for ZFIN-10399 / ZFIN-10380 land.
     *
     * @throws ZircEntityNotFoundException if {@code name} is not a served
     *         vocabulary — surfaces as a 404 via {@code ZircApiExceptionHandler}
     */
    public List<VocabularyTermDTO> terms(String name) {
        Class<? extends MutationDetailControlledVocabularyTerm> clazz = VOCABULARIES.get(name);
        if (clazz == null) {
            throw new ZircEntityNotFoundException(
                    "No such vocabulary: " + name + ". Known: " + new TreeSet<>(VOCABULARIES.keySet()));
        }
        List<? extends MutationDetailControlledVocabularyTerm> terms =
                new ArrayList<>(new HibernateControlledVocabularyRepository<>(clazz)
                        .getControlledVocabularyTermList());
        terms.sort(MutationDetailControlledVocabularyTerm::compareTo);
        return terms.stream()
                .map(t -> new VocabularyTermDTO(
                        t.getZdbID(),
                        t.getDisplayName(),
                        blankToNull(t.getAbbreviation())))
                .toList();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
