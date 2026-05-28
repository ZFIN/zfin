package org.zfin.zirc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.Date;

/**
 * Curator/submitter comment thread row attached to a line-submission field
 * or section. See migration 0060 for column docs and the rec_id scheme.
 
 * One curator-authored comment on a specific field or section of a ZIRC
 * line submission (or any nested aggregate). Keyed by:
 *
 *   - {@code recId}: the entity reference — {@code ZDB-ZLSUB-...} for the
 *     submission, {@code ZIRC-MUT-{id}} / {@code ZIRC-GENE-{id}} / etc.
 *     for nested aggregates.
 *   - {@code scope}: {@code "field"} or {@code "section"}.
 *   - {@code fieldName} or {@code sectionName}: which one within the entity.
 *
 * <p>Plain INSERT-only model: no edit, no delete (yet). Comments accumulate
 * for as long as the submission exists.
 
 */
@Entity(name = "ZircLineSubmissionComment")
@Table(schema = "zirc", name = "line_submission_comment")
@Getter
@Setter
public class LineSubmissionComment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lsc_pk_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "lsc_rec_id", nullable = false)
    private String recId;

    @Column(name = "lsc_scope", nullable = false)
    private String scope;

    @Column(name = "lsc_field_name")
    private String fieldName;

    @Column(name = "lsc_section_name")
    private String sectionName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lsc_author_zdb_id", referencedColumnName = "zdb_id", nullable = false)
    private Person author;

    @Column(name = "lsc_comment", nullable = false)
    private String comment;

    /** Marks this comment thread as finished. Defaults false. */
    @Column(name = "lsc_closed", nullable = false)
    private boolean closed;

    @Column(name = "lsc_created_at", insertable = false, updatable = false)
    private Date createdAt;

}
