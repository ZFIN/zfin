package org.zfin.zirc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * A vocabulary entry ZIRC needs that has no ontology term to hang on.
 *
 * <p>{@code mutation_detail_controlled_vocabulary} is the real home for these
 * lists, but its primary key is a foreign key to {@code term}, so an entry
 * there must be a loaded ontology term. Two of the protein consequences
 * ZFIN-10380 asks for have no Sequence Ontology term — confirmed with the SO
 * project, not merely absent from our load — and terms have been requested
 * upstream. This table holds them until those arrive, without weakening a
 * constraint three curation tables rely on.
 *
 * <p>{@link org.zfin.zirc.service.ZircVocabularyService} merges these with the
 * mdcv rows so the form sees one ordered list. Tokens carry a {@code zirc:}
 * prefix, which is what makes a provisional value recognisable in stored data
 * and makes the eventual cutover mechanical.
 */
@Entity(name = "ZircVocabularyTerm")
@Table(schema = "zirc", name = "vocabulary_term")
@Getter
@Setter
public class ZircVocabularyTerm implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vt_id", updatable = false, nullable = false)
    private Long id;

    /** An {@code mdcv_used_in} value — which list this entry belongs to. */
    @Column(name = "vt_vocabulary", nullable = false)
    private String vocabulary;

    /** Provisional id, e.g. {@code zirc:c-terminal-peptide-truncation}. */
    @Column(name = "vt_token", nullable = false)
    private String token;

    @Column(name = "vt_label", nullable = false)
    private String label;

    /** Sorts against {@code mdcv_term_order} in the merged list. */
    @Column(name = "vt_order", nullable = false)
    private Integer sortOrder;
}
