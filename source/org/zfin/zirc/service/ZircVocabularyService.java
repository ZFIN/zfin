package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.feature.AminoAcidTerm;
import org.zfin.feature.DnaMutationTerm;
import org.zfin.feature.GeneLocalizationTerm;
import org.zfin.feature.MutationDetailControlledVocabularyTerm;
import org.zfin.feature.ProteinConsequence;
import org.zfin.feature.TranscriptConsequence;
import org.zfin.feature.repository.HibernateControlledVocabularyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.zirc.dto.VocabularyTermDTO;
import org.zfin.zirc.entity.ZircVocabularyTerm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Serves the controlled-vocabulary pick lists the lesion form needs
 * (amino acids, protein consequences, transcript consequences), primarily
 * from {@code mutation_detail_controlled_vocabulary} — the same table the
 * GWT curation interface reads — merged with any ZIRC-only supplements.
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
     * One entry on the way to being ordered — the sort key alongside the DTO
     * the client will see, so mdcv terms and ZIRC supplements can be
     * interleaved before either is exposed.
     */
    // Package-private so ZircVocabularyServiceTest can exercise merge()
    // without a database — the ordering is the part worth pinning down.
    record Ordered(int order, String label, VocabularyTermDTO dto) {}

    /**
     * Merge two sources into one display order: by sort order, then by label.
     *
     * <p>The label tie-break is what makes the mdcv side deterministic despite
     * duplicate {@code mdcv_term_order} values, and it applies across both
     * sources so a supplement sharing an order with an mdcv term still lands
     * somewhere predictable.
     */
    static List<VocabularyTermDTO> merge(List<Ordered> entries) {
        List<Ordered> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(Ordered::order)
                .thenComparing(Ordered::label, Comparator.nullsLast(Comparator.naturalOrder())));
        return sorted.stream().map(Ordered::dto).toList();
    }

    /**
     * All terms of one vocabulary, in display order.
     *
     * <p>Two sources: the mdcv rows, plus any ZIRC-only supplements for
     * values with no ontology term yet (see {@link ZircVocabularyTerm}).
     * {@link #merge} interleaves them by sort order.
     *
     * <p>Ordering does not rely on the repository's {@code order by order}
     * alone. {@code mdcv_term_order} contains duplicates within a vocabulary
     * — amino acids have His and Ile both at 10 — and an unbroken tie leaves
     * the rendered list at the mercy of whatever the database returns, so
     * {@link #merge} breaks ties on label.
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
        List<Ordered> entries = new ArrayList<>();
        for (MutationDetailControlledVocabularyTerm t
                : new HibernateControlledVocabularyRepository<>(clazz).getControlledVocabularyTermList()) {
            entries.add(new Ordered(
                    t.getOrder() == null ? Integer.MAX_VALUE : t.getOrder(),
                    t.getDisplayName(),
                    new VocabularyTermDTO(t.getZdbID(), t.getDisplayName(),
                            blankToNull(t.getAbbreviation()))));
        }
        for (ZircVocabularyTerm t : supplements(name)) {
            entries.add(new Ordered(
                    t.getSortOrder(), t.getLabel(),
                    new VocabularyTermDTO(t.getToken(), t.getLabel(), null)));
        }
        return merge(entries);
    }

    /**
     * ZIRC-only entries for this vocabulary — values with no ontology term
     * yet. Usually empty; see {@link ZircVocabularyTerm} for why the table
     * exists.
     */
    private List<ZircVocabularyTerm> supplements(String vocabulary) {
        return HibernateUtil.currentSession()
                .createQuery("from ZircVocabularyTerm where vocabulary = :v", ZircVocabularyTerm.class)
                .setParameter("v", vocabulary)
                .list();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
