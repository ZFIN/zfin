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

import java.util.Date;

/**
 * Curator/submitter comment thread row attached to a line-submission field
 * or section. See migration 0060 for column docs and the rec_id scheme.
 */
@Entity(name = "ZircLineSubmissionComment")
@Table(schema = "zirc", name = "line_submission_comment")
@Getter
@Setter
public class LineSubmissionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lsc_pk_id")
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

    @Column(name = "lsc_created_at", insertable = false, updatable = false)
    private Date createdAt;
}
